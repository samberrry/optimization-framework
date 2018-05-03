package org.cloudbus.cloudsim.workflow.broker;

import static org.cloudbus.cloudsim.util.workload.TaskState.CANCELED;
import static org.cloudbus.cloudsim.util.workload.TaskState.COMPLETED;
import static org.cloudbus.cloudsim.util.workload.TaskState.FAILED;
import static org.cloudbus.cloudsim.util.workload.TaskState.POSTPONED;
import static org.cloudbus.cloudsim.util.workload.TaskState.READY;
import static org.cloudbus.cloudsim.util.workload.TaskState.RUNNING;
import static org.cloudbus.cloudsim.util.workload.TaskState.SCHEDULED;
import static org.cloudbus.spotsim.broker.resources.ResourceState.ACTIVE;
import static org.cloudbus.spotsim.broker.resources.ResourceState.IDLE;
import static org.cloudbus.spotsim.broker.resources.ResourceState.OUT_OF_BID;
import static org.cloudbus.spotsim.broker.resources.ResourceState.PENDING;
import static org.cloudbus.spotsim.broker.resources.ResourceState.TERMINATION_REQUESTED;
import static org.cloudbus.spotsim.broker.resources.ResourceState.RES_FAILED;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.EventTag;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.util.workload.Job;
import org.cloudbus.cloudsim.util.workload.Job.JobStatus;
import org.cloudbus.cloudsim.util.workload.LSTcomparator;
import org.cloudbus.cloudsim.util.workload.Task;
import org.cloudbus.cloudsim.util.workload.TaskState;
import org.cloudbus.cloudsim.util.workload.WFEdge;
import org.cloudbus.cloudsim.util.workload.Workflow;
import org.cloudbus.cloudsim.util.workload.WorkflowUtilis;
import org.cloudbus.cloudsim.workflow.failure.RandomTaskFailureModel;
import org.cloudbus.cloudsim.workflow.failure.TaskFailureGenerator;
import org.cloudbus.spotsim.ComputeCloudTags;
import org.cloudbus.spotsim.broker.ModelParameters;
import org.cloudbus.spotsim.broker.SchedulingDecision;
import org.cloudbus.spotsim.broker.resources.Resource;
import org.cloudbus.spotsim.broker.resources.ResourceFactory;
import org.cloudbus.spotsim.broker.resources.ResourceState;
import org.cloudbus.spotsim.cloudprovider.ComputeCloud;
import org.cloudbus.spotsim.cloudprovider.instance.Instance;
import org.cloudbus.spotsim.cloudprovider.instance.InstanceState;
import org.cloudbus.spotsim.enums.AZ;
import org.cloudbus.spotsim.enums.FaultToleranceMethod;
import org.cloudbus.spotsim.enums.InstanceType;
import org.cloudbus.spotsim.enums.OS;
import org.cloudbus.spotsim.enums.PriceModel;
import org.cloudbus.spotsim.enums.Region;
import org.cloudbus.spotsim.enums.WorkflowPolicies;
import org.cloudbus.spotsim.main.config.Config;
import org.cloudbus.spotsim.main.config.SimProperties;
import org.cloudbus.spotsim.payloads.InstanceCreatedNotification;
import org.cloudbus.spotsim.payloads.InstanceTerminatedNotification;
import org.cloudbus.spotsim.pricing.Accounting;
import org.cloudbus.spotsim.pricing.SpotPriceHistory;
import org.cloudbus.spotsim.pricing.db.PriceDB;
import org.cloudbus.spotsim.simrecords.Profiler.Metric;
import org.cloudbus.spotsim.simrecords.SimulationData;

public class WorkflowBroker extends SimEntity {

    protected final ComputeCloud provider;

    /** Contains all jobs */
    private List<Job> jobList;

    private int jobsToDo = 0;

    protected boolean schedEventSent = false;

    protected final WorkflowSchedulingPolicy policy;

    /** Contains tasks that had their scheduling postponed */
    private final Map<String, Task> nonUrgentTasks;

    /**
     * Contains only unscheduled jobs, they will be removed from this list in
     * the next scheduling cycle
     */
    List<Job> toSchedule;

    protected final Map<Long, Resource> requests;

    protected final ResourceFactory resources;

    private final Set<Job> runningJobs;
    
    private ArrayList<Integer> completedJobs;
    
    private final Workflow worklfow;
    
    //Hashmap which stores the resource Ids and the last completion hr, so it helps in calculating the cost used as and when tasks are completed.
    // used in the method taskFinished()
    private HashMap<Integer, Long> lastHourRes = new HashMap<>();
    
    public WorkflowBroker(final String name, final ComputeCloud provider, final WorkflowSchedulingPolicy policy, Workflow workflow)
	    throws Exception {
	super(name);
	this.provider = provider;
	this.policy = policy;
	this.jobList = new LinkedList<Job>();
	SimulationData.singleton().setStartDate(Config.getSimPeriodStart());
	this.nonUrgentTasks = new LinkedHashMap<String, Task>();
	this.toSchedule = new LinkedList<Job>();
	this.requests = new HashMap<Long, Resource>();
	this.resources = new ResourceFactory();
	this.runningJobs = new LinkedHashSet<Job>();
	this.worklfow = workflow;
	this.completedJobs = new ArrayList<>();
    }

    public void addJobs(final Collection<Job> jobs) {
	this.jobList.addAll(jobs);
	this.jobsToDo += this.jobList.size();
    }

    public void addJobs(final Job... jobs) {

	for (final Job job : jobs) {
	    this.jobList.add(job);
	}
	this.jobsToDo += this.jobList.size();
    }

    public double priceQuery(final InstanceType type, final OS os) {
	return this.priceQuery(type, os, AZ.ANY);
    }

    public double priceQuery(final InstanceType type, final OS os, final AZ az) {
	return this.provider.priceQuery(type, os, az);
    }

    @Override
    public void processEvent(final SimEvent ev) {

	final EventTag eventTag = ev.getEventTag();
	if (eventTag instanceof ComputeCloudTags) {
	    final ComputeCloudTags tag = (ComputeCloudTags) eventTag;
	    switch (tag) {
	    case INSTANCE_CREATED:
		final InstanceCreatedNotification createNotif = (InstanceCreatedNotification) ev
		    .getData();
		instanceCreated(createNotif.getToken(), createNotif.getAvailabilityZone());
		break;
	    case INSTANCE_TERMINATED:
		final InstanceTerminatedNotification termNotif = (InstanceTerminatedNotification) ev
		    .getData();
		instanceTerminated(termNotif.getToken(), termNotif.getFinalStatus(),
		    termNotif.getCost(), termNotif.getHoursCharged());
		break;
	    case INSTANCE_FAILED:
			final InstanceTerminatedNotification termnateNotif = (InstanceTerminatedNotification) ev
			    .getData();
			instanceFailed(termnateNotif.getToken(), termnateNotif.getCost(), termnateNotif.getHoursCharged());
			break;
	    case NEW_JOB_ARRIVED:
		newJobArrived((Job) ev.getData());
		break;
	    case RETRY_NONURGENT_TASK:
		retryNonUrgentTask((Task) ev.getData());
		break;
	    case TASK_FINISHED:
		taskFinished((Task) ev.getData());
		break;
	    case TASK_CANCELED:
		taskCanceled((Task) ev.getData());
		break;
	    case TASK_FAILED:
	    taskFailed((Task) ev.getData());
		break;
	    case FAIL_RESOURCE:
	    	failResource((Resource) ev.getData());
		break;
	    case CHECK_ESTIMATION:
		checkEstimation((Task) ev.getData());
		break;
	    case SCHEDULING:
		SimulationData.singleton().getProfiler().startPeriod(Metric.SCHED_TIME);
		this.schedule();
		SimulationData.singleton().getProfiler().endPeriod(Metric.SCHED_TIME);
		this.schedEventSent = false;
		break;
	    case INSTANCE_CREATION_FAILED:
		final InstanceCreatedNotification notif = (InstanceCreatedNotification) ev
		    .getData();
		final Resource resource = this.requests.get(notif.getToken());
		if (SimProperties.FT_INSTANCE_FAILURES_EXPECTED.asBoolean()) {
		    resourceFailed(resource);
		} else {
		    throw new IllegalStateException("Request "
			    + notif.getToken()
			    + " failures not expected");
		}
		break;
	    case CHECK_PENDING_BIDS:
		checkInstanceBid((Resource) ev.getData());
		break;
	    case TERMINATE_INSTANCES:
		terminateMarkedResource((Long) ev.getData());
		break;
	    default:
		throw new RuntimeException("Unexpected event "
			+ tag
			+ " cannot be processed by the client");
	    }
	} else {
	    throw new RuntimeException("Unexpected event "
		    + eventTag
		    + " cannot be processed by the client");
	}
    }

   	public boolean reachedMax() {
	return this.requests.size() >= SimProperties.DC_VM_MAX.asInt();
    }

    @Override
    public void shutdownEntity() {
	System.out.println("Min max cache hits" + SpotPriceHistory.cacheHits);
	SimulationData.singleton().getProfiler().endPeriod(Metric.SIM);
	SimulationData.singleton().commit();
    }

    @Override
    public void startEntity() {
	Log.logger.info("Starting simulation at date: "
		+ Log.formatDate(Config.getSimPeriodStart().getTime()));
	SimulationData.singleton().getProfiler().startPeriod(Metric.SIM);
	submit();
    }
    
    protected void instanceCreated(final long requestToken, final AZ az) {

	final Resource resource = this.requests.get(requestToken);

	if (resource != null) {

	    resource.received();

	    if (Log.logger.isLoggable(Level.INFO)) {
		Log.logger.info(Log.clock()
			+ "Resource received. Request "
			+ requestToken
			+ ". Resource: "
			+ resource.getId()
			+ ", tasks waiting: "
			+ resource.getNumberOfScheduledTasks());
	    }

	    SimulationData.singleton().getStats().incrInstancesReceived();
	   //For persisting results takes care of only demand and spot Price models. 
	    // to change check Statistics class.
	    if(resource.getPriceModel() == PriceModel.ON_DEMAND){
	    	SimulationData.singleton().getStats().incrODInstanceTypeUsed(resource.getType());
	    }else{
	    	SimulationData.singleton().getStats().incrspotInstanceTypeUsed(resource.getType());
	    }

	    if ((FaultToleranceMethod) SimProperties.FT_METHOD.asEnum() == FaultToleranceMethod.CHKPT) {
		this.provider.checkpointInstance(requestToken, getId(),
		    SimProperties.FT_CKPT_FREQ.asInt());
	    }

	    if (resource.hasScheduledTasks()) {
		submitTaskToCloud(resource, resource.pollNextScheduledTask());
	    } else {
		// the task was a canceled before receiving the instance
		terminateIfNotNeeded(resource);
	    }
	} else {
	    throw new RuntimeException("Unexpected bug. Resource of request "
		    + requestToken
		    + " does not exist");
	}
    }

    protected void instanceTerminated(final long token, final boolean outOfBid, final double cost,
	    int periodsCharged) {

	if (this.requests.containsKey(token)) {
	    final Resource resource = this.requests.remove(token);
	    	// Resource should not be already terminated
			if (resource.getState() != RES_FAILED) {
				if (resource.getTimeReceived() == -1) {
					Log.logger
							.fine(Log.clock() + "Pending request terminated: "
									+ resource.getId());
				} else {
					resource.terminated(cost);
					SimulationData.singleton().getStats()
							.incrInstancesTerminated();

					if (outOfBid && resource.getState() != TERMINATION_REQUESTED) {
						this.resources
								.updateResourceState(resource, OUT_OF_BID);
						this.worklfow.computeRemainingBudget(cost);
						resourceFailed(resource);
					}
					SimulationData.singleton().instanceFinished(resource);
					SimulationData.singleton().getStats().incrActualCost(cost);
					SimulationData.singleton().getStats()
							.incrInstancesIdleTime(resource.idleTime());
					SimulationData.singleton().getStats()
							.incrTotalInstanceTime(resource.runTime());

					if (Log.logger.isLoggable(Level.INFO)) {
						Log.logger.info(Log.clock() + "Instance terminated: "
								+ resource.getId() + ", type "
								+ resource.getType() + " status: "
								+ resource.getState() + ", cost: " + cost
								+ ", run time: " + resource.runTimeInHours()
								+ "(from " + resource.getTimeReceived()
								+ " to " + resource.getTimeTerminated()
								+ "), idle time: " + resource.idleTime()
								+ ", full periods charged " + periodsCharged
								+ ", tasks run " + resource.getTasksRun()
								+ ", remaining wf Budget: "
								+ this.worklfow.getRemainingBudget());
					}
				}

				this.resources.destroy(resource);
				if (this.jobsToDo <= 0
						&& this.resources.getTotalNumberOfResources() == 0) {
					farewell();
				}
			}
	}
    }
    
    protected void instanceFailed(final long token, final double cost,
			int periodsCharged) {

		if (this.requests.containsKey(token)) {
			final Resource resource = this.requests.remove(token);
			if (resource.getState() != OUT_OF_BID) {
				if (resource.getTimeReceived() == -1) {
					Log.logger.fine(Log.clock() + "Pending request terminated: "+ resource.getId());
				} else {
					resource.terminated(cost);
					SimulationData.singleton().getStats()
							.incrInstancesTerminated();

					if (resource.getState() != TERMINATION_REQUESTED) {
						this.resources.updateResourceState(resource,
								ResourceState.RES_FAILED);
						this.worklfow.computeRemainingBudget(cost);
						// resourceFailed(resource);
					}
					SimulationData.singleton().instanceFinished(resource);
					SimulationData.singleton().getStats().incrActualCost(cost);
					SimulationData.singleton().getStats()
							.incrInstancesIdleTime(resource.idleTime());
					SimulationData.singleton().getStats()
							.incrTotalInstanceTime(resource.runTime());

					if (Log.logger.isLoggable(Level.INFO)) {
						Log.logger.info(Log.clock() + "Instance Failed: "
								+ resource.getId() + ", type "
								+ resource.getType() + " status: "
								+ resource.getState() + ", cost: " + cost
								+ ", run time: " + resource.runTimeInHours()
								+ "(from " + resource.getTimeReceived()
								+ " to " + resource.getTimeTerminated()
								+ "), idle time: " + resource.idleTime()
								+ ", full periods charged " + periodsCharged
								+ ", tasks run " + resource.getTasksRun()
								+ ", remaining wf Budget: "
								+ this.worklfow.getRemainingBudget());
					}
				}

				this.resources.destroy(resource);
				if (this.jobsToDo <= 0
						&& this.resources.getTotalNumberOfResources() == 0) {
					farewell();
				}
			}
		}
	}

    private void farewell() {

	this.provider.disconnect(getId());
    }

    protected void newJobArrived(final Job j) {
	SimulationData.singleton().addNewJob(j);
	this.runningJobs.add(j);
	addToScheduler(j);
    }

    protected void schedule() {

	final long soFar = SimulationData.singleton().getProfiler().soFar(Metric.SIM);
	final int jobsProcessed = SimulationData.singleton().getStats().getJobsSubmitted();

	double jps = 0;
	final long secondsPast = soFar / 1000;
	if (secondsPast > 1) {
	    jps = jobsProcessed / secondsPast;
	}

	if (Log.logger.isLoggable(Level.WARNING) && this.jobsToDo % 100 == 0) {
	    Log.logger.warning(Log.clock()
		    + "SCHED "
		    + this.toSchedule.size()
		    + " jobs"
		    + ", to do: "
		    + this.jobsToDo
		    + ", non urgent: "
		    + this.nonUrgentTasks.size()
		    + ", Instances: "
		    + this.resources.getTotalNumberOfResources()
		    + ", time passed: "
		    + secondsPast
		    + ", Jobs processed per second: "
		    + jps);
	}

	final Iterator<Job> iterator = this.toSchedule.iterator();

	while (iterator.hasNext()) {
	    final Job j = iterator.next();
	    allocateTask(j.newTask());
	}

	if (!this.nonUrgentTasks.isEmpty()) {
	    final Collection<Resource> idle = this.resources.getResourcesByState(IDLE);
	    if (idle.size() > 0) {
		// there are still idle instances, so let's try to fit some
		// non-urgent jobs
		fitNonUrgentTasks(idle);
	    }
	}
	this.toSchedule.clear();
    }

    private void fitNonUrgentTasks(final Collection<Resource> idleResources) {
	Iterator<Entry<String, Task>> iterator2 = this.nonUrgentTasks.entrySet().iterator();
	while (iterator2.hasNext()) {
	    final Task tt = iterator2.next().getValue();
	    final SchedulingDecision dec2 = this.policy.fitsOnIdle(tt, idleResources);
	    if (dec2 != null) {
		if (Log.logger.isLoggable(Level.INFO)) {
		    Log.logger.info(Log.clock()
			    + "SCHED. Job "
			    + tt.getId()
			    + " has been allocated to IDLE instance: "
			    + dec2);
		}
		iterator2.remove();
		SimulationData.singleton().jobScheduled(dec2);
		tt.setState(SCHEDULED);
		tt.setEstimatedRunTime(dec2.getEstimatedRuntime());
		final Resource chosenResource = dec2.getResource();
		idleResources.remove(chosenResource);
		submitTaskToCloud(chosenResource, tt);
		if (idleResources.size() <= 0) {
		    break;
		}
	    }
	}
    }
    
  private double billInstance(final Instance instance, boolean resFailed, Region region) {
    	final long accStart = instance.getAccStart();
    	if (accStart < 0) {
    	    return 0.0;
    	}
    	return Accounting.computeCost(accStart, instance.getAccEnd(), resFailed,
    	    PriceDB.getPriceTrace(region, instance.getParentDatacenter().getAz())
    		.getIteratorAtTime(instance.getType(), instance.getOs(), accStart));
        }
    
    protected double calculateCost(Instance instance, boolean resFailed, Region region){
    	double cost = 0;
    	if(instance.getPricing() == PriceModel.SPOT){
    		cost = billInstance(instance, resFailed, region);
    		return cost;
    	}else if(instance.getPricing() == PriceModel.ON_DEMAND){
    		OS os = instance.getOs();
    		long from = instance.getAccStart();
    		long to = instance.getAccEnd();
    		final int minChargeablePeriod = SimProperties.PRICING_CHARGEABLE_PERIOD.asInt();
    		//If less than hr do not charge as the resource failed
    		if (to - from <= minChargeablePeriod) {
    			cost = 0;
    		}else{
    			//the resource is charged for the hrs before it failed.
    			final double hours = Math.ceil((to - from) / minChargeablePeriod);
    			cost = (hours-1) * instance.getType().getOnDemandPrice(region, os);
    		}
    		return cost;
    	}else{
    		throw new IllegalArgumentException("Undefined Pricing Model");
    	}
    }
    
    protected void failResource(final Resource resource){
    	if(resource.getState().isUsable()){
    		this.resources.updateResourceState(resource,
					ResourceState.RES_FAILED);
    		this.provider.failInstance(getId(), resource.getToken());
    		SimulationData.singleton().getStats().incrResourceFault();
    		resourceFailed(resource);
    	}
    }
    
    
    //New for task Duplication work by deepak 16 july 2014
    //Failure event should trigger this function and neccessary action shud be taken .. still unclear
    protected void taskFailed(final Task task) {
    	if (task.getState() == RUNNING) {
    		final Resource resource = task.getResource();
    		Job job = task.getJob();
    		/*if(FaultToleranceMethod.CHKPT_PERFECT == (FaultToleranceMethod) SimProperties.FT_METHOD.asEnum() 
    				|| FaultToleranceMethod.CHKPT == (FaultToleranceMethod) SimProperties.FT_METHOD.asEnum()){
				final long computationDone = (long) (CloudSim.clock() - task
						.getCloudlet().getExecStartTime());
				task.getCloudlet().setLostComputation(computationDone);
				task.getCloudlet().updateProcessingDoneSoFar(computationDone);
				long lostComputation = task.getLostComputation();
				if (resource.getType().getEc2units() > 1) {
					lostComputation = ModelParameters.execTimeParallel(job
							.getA(), job.getSigma(), resource.getType()
							.getEc2units(), lostComputation);
				}
				SimulationData.singleton().getStats()
						.incrRedudantProcessing(lostComputation);
				SimulationData.singleton().getStats()
						.incrRedudantProcessing(task.timeTaken());
    		}*/
     		job.setEstimatedLength(-1);
     		long realLengthRemaining = task.getJob().getLength() - task.getCompletedSoFar();
     		if (realLengthRemaining < 0) {
     			throw new IllegalArgumentException("Task "+ task.getId()+ " should have been finished, real length remaining" + realLengthRemaining);
     		}
     		//job.setLength(realLengthRemaining);
     		   		
     		if(realLengthRemaining > 0){
				resource.removeRunningTask(task);
				task.setState(FAILED);
				task.setActualEndTime(CloudSim.clock());
				this.resources.updateResourceState(resource,
						ResourceState.RES_FAILED);
						
				//this.provider.failTask(getId(), task);
				this.provider.failInstance(getId(), task.getResource().getToken());
				
				this.sendNow(
						getId(),
						ComputeCloudTags.TERMINATE_INSTANCES,resource.getToken());

				//SimulationData.singleton().getStats().incrJobsFailed();
				//SimulationData.singleton().getStats().incrResourcesFailed();
				if (Log.logger.isLoggable(Level.INFO)) {
					Log.logger.info(Log.clock() + "Recovering job "
							+ task.getId() + " done so far: "
							+ task.getCompletedSoFar() + " actual length: "
							+ task.getJob().getLength()
							+ " original estimate: "
							+ task.getJob().getEstimatedLength());
				}

				if(job.getNumberOfActiveTasks()==0){
					// reschedule failed job
					allocateTask(job.newTask());
				}
    		}
    	}
    }
    
    public int chargeablePeriods(long runTime) {
    	double runTimeFract = runTime / SimProperties.PRICING_CHARGEABLE_PERIOD.asInt();
    	int fullPeriods = (int) runTimeFract;
    	double partialPeriod = runTimeFract - fullPeriods;
    	return partialPeriod > 0 ? fullPeriods + 1 : fullPeriods;
        }

    protected void taskCanceled(final Task task) {
	if (task.getState() == RUNNING) {
	    final Resource resource = task.getResource();
	    resource.removeRunningTask(task);
	    final int runningTasks = task.getJob().getNumberOfActiveTasks();
	    if (runningTasks > 0) {
		    final List<Task> tasks = task.getJob().getTasks();
		    for (final Task replica : tasks) {
			if (!replica.getState().finished()) {
			    killTask(replica);
			}
		    }
		}
	    task.setState(CANCELED);
	    task.setActualEndTime(CloudSim.clock());
	    SimulationData.singleton().getStats().incrRedudantProcessing(task.timeTaken());
	    moreWork(resource);
	}
    }

    protected void taskFinished(final Task task) {

	// test if task is not already finished, it may have been killed
	if (!task.getState().finished()) {
	    final Resource resource = task.getResource();
	    task.setActualEndTime(CloudSim.clock());
	    task.setState(COMPLETED);
	    resource.removeRunningTask(task);
	    
	    final Job finishedJob = task.getJob();
	    final int runningTasks = finishedJob.getNumberOfActiveTasks();
	    if (Log.logger.isLoggable(Level.FINE)) {
		Log.logger.fine(Log.clock()
			+ "Task "
			+ task.getId()
			+ " finished. Running tasks for job "
			+ task.getJob().getId()
			+ ": "
			+ runningTasks);
	    }
	    
	    if(runningTasks == 0 && (finishedJob.getStatus() != JobStatus.COMPLETED)){
	    	this.runningJobs.remove(finishedJob);
			finishedJob.setStatus(JobStatus.COMPLETED);
			finishedJob.setTimeTaken(task.timeTaken());
			finishedJob.setCompletionTime(CloudSim.clock());
			this.jobsToDo--;
	    	SimulationData.singleton().jobFinished(finishedJob, CloudSim.clock());
	    	if (Log.logger.isLoggable(Level.INFO)) {
			    Log.logger.info(Log.clock()
				    + "Job "
				    + task.getJob().getId()
				    + " of length "
				    + task.getJob().getLength()
				    + " finished. Time taken: "
				    + task.timeTaken()
				    + " on resource type "
				    + resource.getType()
				    + " Jobs yet to finish "
				    + this.jobsToDo);
			}
	    }

	    if (finishedJob.getStatus() != JobStatus.COMPLETED) {
		this.runningJobs.remove(finishedJob);
		finishedJob.setStatus(JobStatus.COMPLETED);
		finishedJob.setTimeTaken(task.timeTaken());
		finishedJob.setCompletionTime(CloudSim.clock());
		this.jobsToDo--;
		final InstanceType typeUsed = resource.getType();
		if (this.policy.usesEstimation()) {
		    long length = finishedJob.getTimeTaken();
		    if (typeUsed.getEc2units() > 1) {
			length = ModelParameters.execTimeSerial(finishedJob.getA(),
			    finishedJob.getSigma(), typeUsed.getEc2units(), length);
		    }
		    this.policy.getRuntimeEstimator()
			.recordRuntime(finishedJob.getUserID(), length);
		}
		SimulationData.singleton().jobFinished(finishedJob, CloudSim.clock());

		if (runningTasks > 0) {
		    final List<Task> tasks = finishedJob.getTasks();
		    for (final Task replica : tasks) {
			if (!replica.getState().finished()) {
			    killTask(replica);
			}
		    }
		}
		if (Log.logger.isLoggable(Level.INFO)) {
		    Log.logger.info(Log.clock()
			    + "Job "
			    + task.getJob().getId()
			    + " of length "
			    + task.getJob().getLength()
			    + " finished. Time taken: "
			    + task.timeTaken()
			    + " on resource type "
			    + typeUsed
			    + " Jobs yet to finish "
			    + this.jobsToDo);
		}
		
			//First decides on the pricing model and the price of the instance hour
			double price=0;
			switch(resource.getPriceModel()){
			
			case ON_DEMAND: price = resource.getType().getOnDemandPrice(resource.getRegion(), resource.getOs());
							break;
			
			case SPOT: price = priceQuery(resource.getType(), resource.getOs());
						break;
						
			default: System.out.println(" Unknown Pricing Model");
			
			}
			// if the resource is already accounted during the completion of an earlier task, we ignore
			// else we calculate the cost for the hours used by the task.
			long totalExecTime=0;			
			long lastFullHour = resource.getNextFullHour();
			if(lastHourRes.containsKey(resource.getId())){
				long nextFullHr = lastHourRes.get(resource.getId());
				totalExecTime = nextFullHr - lastFullHour;
			}else{
				totalExecTime = lastFullHour - task.getActualStartTime(); 
				lastHourRes.put(resource.getId(), lastFullHour);
				this.worklfow.computeRemainingBudget(Math.ceil(totalExecTime/SimProperties.PRICING_CHARGEABLE_PERIOD.asDouble())*price);
			}
			//this.completedJobs.add(finishedJob.getIntId());
			
	    }
	    
	    if(null != this.completedJobs){
	    	if(!this.completedJobs.contains(finishedJob.getIntId())){
	    		this.completedJobs.add(finishedJob.getIntId());
	    		inititateChildTasks(finishedJob);
	    	}
	    }
	        
	    if(isExitNode(finishedJob)){
	    	SimulationData.singleton().getStats().setTotalExecutiontime(CloudSim.clock());
	    }
	    moreWork(resource);

	    if (this.jobsToDo == 0) {
		Log.logger.warning(Log.clock()
			+ " All Cloudlets executed. Finishing. Instances running "
			+ this.resources.getTotalNumberOfResources());
		SimulationData.singleton().getStats().setFinishTime(CloudSim.clock());
	    }
	}
    }

	private void inititateChildTasks(final Job finishedJob) {
		
	    for(Job job : childJobToExecute(finishedJob)){
	    	final long edgeWght = finishedJob.getEdge(job.getIntId());
	    	job.setSubmitTime(CloudSim.clock()+edgeWght);
	    	job.setParentEdgeWeight(edgeWght);
	    	//System.out.println("Job ID: " + job.getId() + " Clock " + CloudSim.clock()+ " Parent Job finsih time " + task.getJob().getReqRunTime() +  "edgeee" +edgeWght);
	    	//this.send(getId(),edgeWght ,ComputeCloudTags.NEW_JOB_ARRIVED, job);
	    	this.sendNow(getId(), ComputeCloudTags.NEW_JOB_ARRIVED, job);
	    }
	    
	    //Changed by Deepak as the Job is finished will schedule the Child Nodes
	    finishedJob.setCriticalPathWeight(0);
	    finishedJob.setEdge(0);
	    //Should recompute Critical here ...........
	    final WorkflowUtilis wfUtil = new WorkflowUtilis(worklfow);
	    
	    if(this.policy.policyName().equals(WorkflowPolicies.NEW_LIBERAL.getPolicyName()) ||
	    		this.policy.policyName().equals(WorkflowPolicies.LIBERAL_NAIVE.getPolicyName())||
	    		this.policy.policyName().equals(WorkflowPolicies.LIBERAL_ODB.getPolicyName())){
	    	wfUtil.recomputeCriticalPath(finishedJob.getIntId(), finishedJob.getLength(),SimProperties.LIBERAL_POLICY_REF_INSTANCE.asEnum(InstanceType.class));
    	} else {
    		wfUtil.recomputeCriticalPath(finishedJob.getIntId(), finishedJob.getLength(),InstanceType.M1SMALL);
    	}
	    
	    //Remove critical jobs from critical path jobs map
	    if(wfUtil.isCriticalPathJob(finishedJob)){
	    	wfUtil.recomputeCritcalPathFromMap(finishedJob);
	    }
	}

    /**
     * For a given finished Job, it find out all the children jobs whose parents have completed execution
     * @param finishedJob
     * @return an ArrayList of Job ready for execution
     */
    private ArrayList<Job> childJobToExecute(Job finishedJob){
    	ArrayList<Integer> children = this.worklfow.getWfDAG().getChildren(finishedJob.getIntId());
    	ArrayList<Job> childJobs = new ArrayList<>();
    	if(children == null){
    		return null;
    	}else{
    		for(Integer jobId : children){
    			ArrayList<Integer> parents = this.worklfow.getWfDAG().getParents(jobId);
    			if(true == this.completedJobs.containsAll(parents)){
    				Job job = this.worklfow.getWfDAG().getNode(jobId);
    				childJobs.add(job);
    			}
    		}
    		return childJobs;
    	}
    }
    
    private boolean isExitNode(Job finishedJob){
    	ArrayList<Integer> children = this.worklfow.getWfDAG().getChildren(finishedJob.getIntId());
    	if(children.size()==0){
    		return true;
    	}else{
    		return false;
    	}
    }
    
    protected void terminateMarkedResource(final long requestId) {

	final Resource resourceToTerminate = this.requests.get(requestId);

	if (resourceToTerminate != null && resourceToTerminate.getState() == IDLE) {
	    this.resources.updateResourceState(resourceToTerminate, TERMINATION_REQUESTED);
	    this.provider.terminateInstance(getId(), requestId);
	}else if(resourceToTerminate != null && (resourceToTerminate.getState() == ResourceState.RES_FAILED)){
		this.provider.terminateInstance(getId(), requestId);
	}
    }

    private void addToScheduler(final Job j) {
	if (!this.schedEventSent) {
	    // a new scheduling event will be sent, only if another one
	    // hasn't been sent since the last scheduling cycle
	    sendSchedulingEvent(j.getParentEdgeWeight());
	    this.schedEventSent = true;
	}
	this.toSchedule.add(j);
    }

    protected void allocateTask(final Task task) {

	final TaskState state = task.getState();
	if (state != READY && state != POSTPONED) {
	    throw new IllegalArgumentException(
		"Only READY and POSTPONED tasks can be scheduled. Probably a bug. State of task "
			+ task.getId()
			+ " is "
			+ state);
	}

	/* Call the actual scheduling policy */
	final SchedulingDecision dec = this.policy.sched(task,
	    this.resources.getAllUsableResources(), reachedMax(), this.provider.getRegion());

	final long maxWaitTime = dec.getMaxWaitTime();
	if (dec.postponed()) {
	    postpone(task, maxWaitTime);
	} else {
	    dec.checkSanity();
	    SimulationData.singleton().jobScheduled(dec);

	    Resource resource;
	    if (dec.isStartNewInstance()) {
		task.scheduled(dec.getEstimatedRuntime(), maxWaitTime);
		resource = requestNewInstance(task, dec.getRegion(), dec.getAz(),
		    dec.getInstanceType(), dec.getOs(), dec.getBidPrice(), maxWaitTime, dec.getPriceModel());
		task.getJob().setStatus(JobStatus.RUNNING);
	    } else {
		resource = dec.getResource();
		task.setResource(resource);
		task.scheduled(dec.getEstimatedRuntime(), maxWaitTime);
		runTaskOnExistingInstance(task, resource);
		task.getJob().setStatus(JobStatus.RUNNING);
	    }

	    if ((FaultToleranceMethod) SimProperties.FT_METHOD.asEnum() == FaultToleranceMethod.REPLICATION) {
		final int replicasToCreate = replicasToCreate(dec.getEstimatedRuntime(),
		    dec.getBidPrice(), this.priceQuery(dec.getInstanceType(), dec.getOs()));
		if (replicasToCreate > 0 && task.getReplicaId() == 0) {
		    replicate(task.getJob(), replicasToCreate);
		}
	    }

	    if (Log.logger.isLoggable(Level.INFO)) {
		Log.logger.info(Log.clock()
			+ " Scheduling Decision: "
			+ dec
			+ ", total cost: "
			+ SimulationData.singleton().getStats().getEstimatedCost()
			+ ", total runtime: "
			+ SimulationData.singleton().getStats().getEstimatedRunTime()
			+ ", Resource: "
			+ resource.getId());
	    }
	}
    }

    protected void postpone(final Task task, final long maxWaitTime) {
	if (Log.logger.isLoggable(Level.INFO)) {
	    Log.logger.info(Log.clock()
		    + " Scheduling Decision: Task "
		    + task.getId()
		    + " has been postponed by "
		    + maxWaitTime
		    + " (to: "
		    + (CloudSim.clock() + maxWaitTime)
		    + ")");
	}
	this.send(getId(), maxWaitTime, ComputeCloudTags.RETRY_NONURGENT_TASK, task);
	task.setState(POSTPONED);
	if (this.nonUrgentTasks.containsKey(task.getId())) {
	    throw new IllegalStateException("already here " + task.getId());
	}
	this.nonUrgentTasks.put(task.getId(), task);
    }

    protected void checkEstimation(final Task delayedTask) {

	final Resource resource = delayedTask.getResource();
	final Job job = delayedTask.getJob();
	if (resource.getState() == ACTIVE && delayedTask.getState() == RUNNING) {
	    final long oldRunTime = delayedTask.getEstimatedRunTime();
	    if (Log.logger.isLoggable(Level.INFO)) {
		Log.logger.info(Log.clock()
			+ "Task "
			+ delayedTask.getId()
			+ "of job "
			+ job.getId()
			+ " user "
			+ job.getUserID()
			+ " (status "
			+ delayedTask.getState()
			+ ") running on instance "
			+ resource.getId()
			+ " - "
			+ resource.getType()
			+ " - "
			+ resource.getState()
			+ " finish "
			+ (delayedTask.getActualStartTime() + oldRunTime)
			+ ", started at "
			+ delayedTask.getActualStartTime()
			+ ", estimated run time was "
			+ oldRunTime
			+ ". Correct from "
			+ job.getEstimatedLength()
			+ " to "
			+ job.getEstimatedLength()
			* 2
			+ ", actual: "
			+ job.getLength()
			+ ", affected jobs: "
			+ resource.getNumberOfScheduledTasks());
	    }

	    final long estimatedRunTime = oldRunTime;
	    if (estimatedRunTime > Math.max(3600, job.getReqRunTime() * 10L)) {
		if (Log.logger.isLoggable(Level.INFO)) {
		    Log.logger.info(Log.clock()
			    + "Job "
			    + delayedTask.getId()
			    + " has exceeded its requested time. Requested: "
			    + job.getReqRunTime()
			    + ". Elapsed "
			    + estimatedRunTime
			    + ". Actual length "
			    + job.getLength()
			    + ", tasks created "
			    + job.getNumberOfTasks());
		}
		killTask(delayedTask);
		job.setStatus(JobStatus.TIME_LAPSED);
		SimulationData.singleton().getStats().incrJobsLapsed();
	    } else {
		final long newLength = job.getEstimatedLength() * 2;
		job.setEstimatedLength(newLength);
		final long newEstimatedRunTime = ModelParameters.execTimeParallel(job.getA(),
		    job.getSigma(), resource.getType().getEc2units(), newLength);
		this.policy.getRuntimeEstimator().recordRuntime(job.getUserID(), newLength);
		delayedTask.setEstimatedRunTime(newEstimatedRunTime);
		final long delayedBy = 1 + (newEstimatedRunTime - oldRunTime);
		this.send(getId(), delayedBy, ComputeCloudTags.CHECK_ESTIMATION, delayedTask);
		final Collection<Task> toReschedule = resource.getScheduledTasks();

		for (final Task t : toReschedule) {
		    if (t.getMaxWaitTime() <= delayedBy) {
			t.setState(READY);
			resource.removeScheduledTask(t);
			allocateTask(t);
		    } else {
			t.setMaxWaitTime(t.getMaxWaitTime() - delayedBy);
		    }
		}
	    }
	}
    }

    protected void checkInstanceBid(final Resource resource) {
	if (resource.getState() == PENDING) {
	    final double bid = this.priceQuery(resource.getType(), resource.getOs(),
		resource.getAz()) + 0.001;
	    resource.setBid(bid);
	    this.provider.changeBid(resource.getToken(), getId(), bid);
	}
    }

    private void killTask(final Task task) {
	final Resource resource = task.getResource();
	if (task.getState() == RUNNING) {
	    this.provider.cancelTask(getId(), task);
	    if (Log.logger.isLoggable(Level.INFO)) {
		Log.logger.info(Log.clock()
			+ "Canceling task: "
			+ task.getId()
			+ " "
			+ task.getState()
			+ " on VM "
			+ resource.getId()
			+ " of status: "
			+ resource.getState());
	    }
	} else if (task.getState() == SCHEDULED) {
	    task.setState(CANCELED);
	    resource.removeScheduledTask(task);
	    moreWork(resource);
	} else if (this.nonUrgentTasks.containsKey(task.getId())) {
	    this.nonUrgentTasks.remove(task);
	}
    }

    private void moreWork(final Resource resource) {

	if (resource.getState() == ACTIVE && !resource.isFull()) {
	    if (resource.hasScheduledTasks()) {
		final Task nextTask = resource.pollNextScheduledTask();
		submitTaskToCloud(resource, nextTask);
	    } else {
		// will be marked as IDLE and terminated at next full hour if no
		// job
		// reuses it
		terminateIfNotNeeded(resource);
	    }
	}else{
		if (Log.logger.isLoggable(Level.INFO)) {
			Log.logger.info(Log.clock()
				+ " Resource id "
				+ resource.getId()
				+ " Resource State "
				+ resource.getState());
		    }
		if (resource.getState() == IDLE) {
			final long delay = resource.getSecondsToNextFullHour();
			this.send(getId(), delay, ComputeCloudTags.TERMINATE_INSTANCES, resource.getToken());
		}
	}
    }

    private void fitNonUrgentTasks(Resource resource) {
	ArrayList<Resource> list = new ArrayList<Resource>();
	list.add(resource);
	fitNonUrgentTasks(list);
    }

    private void recover(final Task failedTask) {

	final Job job = failedTask.getJob();

	switch ((FaultToleranceMethod) SimProperties.FT_METHOD.asEnum()) {
	case CHKPT:
	case CHKPT_PERFECT:
	    long realLengthRemaining = failedTask.getJob().getLength()
		    - failedTask.getCompletedSoFar();

	    if ((FaultToleranceMethod) SimProperties.FT_METHOD.asEnum() == FaultToleranceMethod.CHKPT_PERFECT) {
		realLengthRemaining += failedTask.getResource().getType().getSuspendOverhead();
	    }

	    if (realLengthRemaining <= 0) {
		taskFinished(failedTask);
	    } else {
		if (Log.logger.isLoggable(Level.INFO)) {
		    Log.logger.info(Log.clock()
			    + "Recovering job "
			    + failedTask.getId()
			    + " done so far: "
			    + failedTask.getCompletedSoFar()
			    + " actual length: "
			    + failedTask.getJob().getLength()
			    + " original estimate: "
			    + failedTask.getJob().getEstimatedLength());
		}
		failedTask.setState(FAILED);
		job.setEstimatedLength(-1);
		job.setLength(realLengthRemaining);
		// reschedule failed job
		allocateTask(job.newTask());
	    }
	    break;
	// case MIGRATION:
	case NONE:
	    failedTask.setState(FAILED);
	    job.setEstimatedLength(-1);
	    allocateTask(job.newTask());
	    break;
	case REPLICATION:
	    failedTask.setState(FAILED);
	    if (job.getNumberOfActiveTasks() == 0) {
		// last running task failed
		allocateTask(job.newTask());
	    }
	    break;
	default:
	    break;
	}
    }

    /*
     * Formula that decides how many replicas to create to achieve a certain
     * level of fault tolerance
     */
    private int replicasToCreate(final long estimatedRunTime, final double bid,
	    final double currentPrice) {

	/*
	 * currently, create one replica of every job that is estimated to take
	 * more than one hour
	 */
	final double hours = estimatedRunTime / 3600D;
	if (hours > 2) {
	    return 1;
	}
	return 0;
    }

    private void replicate(final Job jobToRun, final int numReplicas) {

	for (int i = 0; i < numReplicas; i++) {
	    final Task newReplica = jobToRun.newTask();
	    allocateTask(newReplica);
	}
    }

    private Resource requestNewInstance(final Task task, Region region, final AZ az,
	    final InstanceType type, OS os, final double bid, final long maxWaitTime) {

	final double currentPrice = this.priceQuery(type, os, az);
	double newBid = bid;
	if (maxWaitTime <= 0 && bid <= currentPrice) {
	    newBid = currentPrice + 0.001D;
	}

	if (Log.logger.isLoggable(Level.INFO)) {
	    Log.logger.info(Log.clock()
		    + "Requesting new instance of type "
		    + type
		    + " requested for task "
		    + task.getId()
		    + " of job "
		    + task.getJob().getId()
		    + " bid "
		    + newBid
		    + " current price "
		    + currentPrice
		    + " maxwaittime = "
		    + maxWaitTime);
	}
	final long token = this.provider.runInstance(getId(), 1, 1, type, os, PriceModel.SPOT,
	    newBid, 0, az);
	final Resource resource = this.resources.newResource(region, az, type, os, newBid)
	    .requested(token, 0).scheduleTask(task);

	this.requests.put(token, resource);
	task.setResource(resource);

	SimulationData.singleton().getStats().incrInstancesRequested();
	this.send(getId(), Math.max(maxWaitTime, SimProperties.DC_VM_INIT_TIME.asInt() + 5),
	    ComputeCloudTags.CHECK_PENDING_BIDS, resource);
	return resource;
    }
    
	protected Resource requestNewInstance(final Task task, Region region,
			final AZ az, final InstanceType type, OS os, final double bid,
			final long maxWaitTime, PriceModel priceModel) {

		final double currentPrice = this.priceQuery(type, os, az);
		double newBid = bid;
		if (maxWaitTime <= 0 && bid <= currentPrice) {
			newBid = currentPrice + 0.001D;
		}

		if (Log.logger.isLoggable(Level.INFO)) {
			Log.logger.info(Log.clock() + "Requesting new instance of type "
					+ type + " requested for task " + task.getId() + " of job "
					+ task.getJob().getId() + " bid " + newBid
					+ " current price " + currentPrice + " maxwaittime = "
					+ maxWaitTime);
		}
		final long token = this.provider.runInstance(getId(), 1, 1, type, os,
				priceModel, newBid, 0, az);
		final Resource resource = this.resources
				.newResource(region, az, type, os, newBid, priceModel).requested(token, 0)
				.scheduleTask(task);

		this.requests.put(token, resource);
		task.setResource(resource);
		
		if(priceModel == PriceModel.SPOT){
			WorkflowUtilis wfUtils = new WorkflowUtilis(worklfow);
			long lto = wfUtils.reComputeLTO();
			SimulationData.singleton().getStats().addSpotPojo(bid, lto, currentPrice);
			SimulationData.singleton().getStats().addSpotPrice(bid, resource.getId());
		}

		SimulationData.singleton().getStats().incrInstancesRequested();
		this.send(getId(), Math.max(maxWaitTime,
				SimProperties.DC_VM_INIT_TIME.asInt() + 5),
				ComputeCloudTags.CHECK_PENDING_BIDS, resource);
		return resource;
	}

    protected void resourceFailed(final Resource resource) {
    SimulationData.singleton().getStats().incrTotalResourcesFailed();
	final List<Task> failed = new ArrayList<Task>(resource.getRunningTasks());
	for (final Task failedTask : failed) {
	    Job job = failedTask.getJob();
	    long lostComputation = failedTask.getLostComputation();
	    if (resource.getType().getEc2units() > 1) {
		lostComputation = ModelParameters.execTimeParallel(job.getA(), job.getSigma(),
		    resource.getType().getEc2units(), lostComputation);
	    }
	    SimulationData.singleton().getStats().incrJobsFailed();
	    SimulationData.singleton().getStats().incrRedudantProcessing(lostComputation);
	    recover(failedTask);
	}

	final Collection<Task> scheduledTask = resource.getAndRemoveScheduledTasks();
	for (final Task task : scheduledTask) {
	    task.setState(READY);
	    allocateTask(task);
	}
    }

    protected void retryNonUrgentTask(final Task t) {
	if (this.nonUrgentTasks.containsKey(t.getId())) {
	    this.nonUrgentTasks.remove(t.getId());
	    if (!(t.getJob().getStatus() == JobStatus.COMPLETED)) {
		allocateTask(t);
	    }
	}
    }

    protected void runTaskOnExistingInstance(final Task task, final Resource resource) {
	if (resource.getState() == IDLE) {
	    // instance is idle. submit job immediately
	    submitTaskToCloud(resource, task);
	} else {
	    // instance is busy, put task in the queue
	    resource.scheduleTask(task);
	}
    }

    private void sendSchedulingEvent() {

	this.send(getId(), SimProperties.SCHED_INTERVAL.asInt(), ComputeCloudTags.SCHEDULING);
    }
    
    //changed by deepak
    private void sendSchedulingEvent(long delay) {

    	this.send(getId(), delay, ComputeCloudTags.SCHEDULING);
    }

    //Changes by deepak
    /*
     * Decides, based on the workload trace, when to submit which job and sends
     * an event to itself to schedule the job
     */
    private void submit() {

    	// Computes the critical path weights of the node and the Max Wait Time or the LST of the node
    	if(this.policy.policyName().equals(WorkflowPolicies.NEW_LIBERAL.getPolicyName())){
    		workflowInit(SimProperties.LIBERAL_POLICY_REF_INSTANCE.asEnum(InstanceType.class));
    	} else {
    		workflowInit(InstanceType.M1SMALL);
    	}
    	ArrayList<Job> firstLevelJobs = new ArrayList<>();
    	for(final Integer jobId : this.worklfow.getWfDAG().getFirstLevel()){
    		Job job = this.worklfow.getWfDAG().getNode(jobId);
    		firstLevelJobs.add(job);
    	}
    	Collections.sort(firstLevelJobs, new LSTcomparator());
    	for(final Job job : firstLevelJobs){
    		job.setSubmitTime(CloudSim.clock());
    		this.sendNow(getId(), ComputeCloudTags.NEW_JOB_ARRIVED, job);
    	}
		this.jobList = null;
    }

    /**
     * Computes the critical path weights of the node and the Max Wait Time or the LST of the node
     */
    private void workflowInit(InstanceType type){
    	final WorkflowUtilis wfUtil = new WorkflowUtilis(this.worklfow);
    	/*//Initializing the criticalPathJobsMap
    	for(InstanceType instanceType : InstanceType.values()){
			ArrayList<Job> criticalPathJobs = wfUtil.getCriticalPathJobs(instanceType);
			if (null == criticalPathJobs || criticalPathJobs.isEmpty()) {
				criticalPathJobs = wfUtil.computeCriticalPathJobs(instanceType);
				wfUtil.setCriticalPathJobs(instanceType, criticalPathJobs);
			}
    	}*/
    	for(final Integer node : this.worklfow.getWfDAG().getFirstLevel()){
    		if(type != null){
    			wfUtil.computeLTO(node,type);
    		}else{
    			wfUtil.computeLTO(node);
    		}
    	}
    	
    	for(final Integer node : this.worklfow.getWfDAG().getLastLevel()){
    		wfUtil.computeLST(node);
    	}
    }
    
    private void terminateIfNotNeeded(final Resource resource) {
	if (Log.logger.isLoggable(Level.INFO)) {
	    Log.logger.info(Log.clock()
		    + " Requesting termination of instance "
		    + resource.getId()
		    + " of state "
		    + resource.getState());
	}

	if (resource.getState() == PENDING) {
	    this.resources.updateResourceState(resource, IDLE);
	    this.sendNow(getId(), ComputeCloudTags.TERMINATE_INSTANCES, resource.getToken());
	} else {
	    this.resources.updateResourceState(resource, IDLE);
	    // try fitting any task that had been postponed
	    fitNonUrgentTasks(resource);
	    if (resource.getState() == IDLE) {
		final long delay = resource.getSecondsToNextFullHour();
		this.send(getId(), delay, ComputeCloudTags.TERMINATE_INSTANCES, resource.getToken());
	    }
	}
    }

    void submitTaskToCloud(final Resource resource, final Task task) {
	task.setResource(resource);
	final long estimatedRunTime = task.getEstimatedRunTime();
	task.setActualStartTime(CloudSim.clock());
	SimulationData.singleton().taskSubmittedToCloud(task, resource);
	resource.runTask(task);
	this.resources.updateResourceState(resource, ACTIVE);
	task.setState(RUNNING);
	task.setUserID(getId());

	if (this.policy.usesEstimation()) {
		//Changes - Deepak
	    //this.send(getId(), estimatedRunTime + 1L, ComputeCloudTags.CHECK_ESTIMATION, task);
	}

	if (Log.logger.isLoggable(Level.FINE)) {
	    Log.logger.fine(Log.clock()
		    + getName()
		    + ": Sending task "
		    + task.getId()
		    + " of job "
		    + task.getJob().getId()
		    + " to VM #"
		    + resource.getId()
		    + ", length: "
		    + task.getJob().getLength()
		    + ". Expected completion time: "
		    + (CloudSim.clock() + estimatedRunTime));
	}
	task.checkSanity();

	this.provider.runTask(resource.getToken(), getId(), task);
    }

	public Workflow getWorklfow() {
		return worklfow;
	}

	public Set<Job> getRunningJobs() {
		return runningJobs;
	}
}