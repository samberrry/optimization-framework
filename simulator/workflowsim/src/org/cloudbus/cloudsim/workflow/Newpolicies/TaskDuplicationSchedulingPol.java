package org.cloudbus.cloudsim.workflow.Newpolicies;

import static org.cloudbus.cloudsim.util.workload.TaskState.POSTPONED;
import static org.cloudbus.cloudsim.util.workload.TaskState.READY;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Map;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.util.workload.Job;
import org.cloudbus.cloudsim.util.workload.Task;
import org.cloudbus.cloudsim.util.workload.TaskState;
import org.cloudbus.cloudsim.util.workload.WorkflowUtilis;
import org.cloudbus.cloudsim.workflow.biddingstrategy.IntelligentBiddingStrategy;
import org.cloudbus.cloudsim.workflow.biddingstrategy.NaiveBiddingStrategy;
import org.cloudbus.cloudsim.workflow.biddingstrategy.SimpleBiddingStrategy;
import org.cloudbus.cloudsim.workflow.biddingstrategy.WorkflowBiddingStrategy;
import org.cloudbus.cloudsim.workflow.broker.WorkflowBroker;
import org.cloudbus.cloudsim.workflow.broker.WorkflowSchedulingPolicy;
import org.cloudbus.cloudsim.workflow.failure.PriceFailureEstimator;
import org.cloudbus.spotsim.broker.ModelParameters;
import org.cloudbus.spotsim.broker.SchedulingDecision;
import org.cloudbus.spotsim.broker.resources.Resource;
import org.cloudbus.spotsim.broker.rtest.RuntimeEstimator;
import org.cloudbus.spotsim.enums.AZ;
import org.cloudbus.spotsim.enums.InstanceType;
import org.cloudbus.spotsim.enums.OS;
import org.cloudbus.spotsim.enums.PriceModel;
import org.cloudbus.spotsim.enums.Region;
import org.cloudbus.spotsim.main.config.SimProperties;
import org.cloudbus.spotsim.pricing.db.PriceDB;
import org.cloudbus.spotsim.simrecords.Profiler.Metric;
import org.cloudbus.spotsim.simrecords.SimulationData;

public class TaskDuplicationSchedulingPol implements WorkflowSchedulingPolicy {

	private static final String NAME = "TaskDuplicationSchPol";

    private WorkflowBroker broker;

    private WorkflowUtilis wfUtil;
    
    protected RuntimeEstimator estimator;
    
    protected ArrayList<Resource> instancesWithoutFreeSlot;
    
    private boolean computeLFTFlag = true;
    
    private WorkflowBiddingStrategy biddingStrategy = new IntelligentBiddingStrategy();
    
    
    public TaskDuplicationSchedulingPol() {
    	
    }

    @Override
    public SchedulingDecision fitsOnIdle(final Task task, final Collection<Resource> idle) {

	for (final Resource idleResource : idle) {
	    final long estimatedParallelTime = ModelParameters.execTimeParallel(task.getJob()
		.getA(), task.getJob().getSigma(), idleResource.getType().getEc2units(), task
		.getJob().getEstimatedLength());
	    final long gratisSeconds = idleResource.getSecondsToNextFullHour();

	    if (gratisSeconds < estimatedParallelTime) {
		return new SchedulingDecision(task, idleResource, estimatedParallelTime);
	    }
	}
	return null;
    }

    @Override
    public RuntimeEstimator getRuntimeEstimator() {
	return this.estimator;
    }

    @Override
    public String policyName() {
	return NAME;
    }

    /**
     * Does the scheduling. Firstly: tries to fit the job in an existing idle
     * instance. Secondly: tries to queue the job in an instance that will be
     * idle soon. Finally, allocates a brand new instance for this job
     */
    @Override
    public ArrayList<SchedulingDecision> schedule(final Task task, final Collection<Resource> myInstances,
	    boolean reachedMax, Region region) {

		EnumSet<AZ> usableAZsForTask = region.getAvailabilityZones();
		ArrayList<SchedulingDecision> schDecArr = new ArrayList<>();
		final EnumSet<AZ> usedOnes = task.getJob().getAZInUse();
		if (usedOnes.size() < usableAZsForTask.size()) {
			usableAZsForTask.removeAll(usedOnes);
		}

		if (task.getJob().getEstimatedLength() < 0) {
			final long estimateJobLength = this.estimator
					.estimateJobLength(task.getJob());
			task.getJob().setEstimatedLength(estimateJobLength);
		}

		final Map<InstanceType, Long> estimates = PolicyUtils.computeRunTimes(task);
		final long longestEstimatedRunTime = estimates.get(InstanceType.M1SMALL);

		SimulationData.singleton().getProfiler().startPeriod(Metric.SCHED_RECYCLE);
	
		int taskId = task.getJob().getIntId();
				
		long LTO = wfUtil.reComputeLTO();
		long currentTime = CloudSim.clock();
		
		LTO -= SimProperties.DC_VM_INIT_TIME.asLong();
		
		//LTO = PolicyUtils.computeRelaxedLTO(LTO, currentTime);
		
		//LTO = SimProperties.CONTENTION_FLAG.asBoolean() ? PolicyUtils.computeRelaxedLTO(LTO, currentTime) : LTO;
		
		//Set the submit time for the Job to help accurately recompute the critical Path
		task.getJob().setSubmitTime(currentTime);
		
		// minimum critical path time with VM initiation time assuming atleast one VM is needed to run the critical path.
		long criticalPathTime = wfUtil.getCriticalPath();
		
	//	long criticalPathTime = PolicyUtils.refineCriticalPathTime(wfUtil);
		
		double remainingBudget = wfUtil.getWorkflow().getRemainingBudget();
		double reqODBudget = wfUtil.computeBudgetLTO(taskId);
		
		// Compute the max wait for each node in the workflow
		for(Integer node : wfUtil.getWorkflow().getWfDAG().getLastLevel()){
			wfUtil.computeLST(node);
		}
		
		long bufferTime = (LTO - currentTime) - task.getJob().getLength();
		PriceModel decPrModel = null;
		double failureProb =0;
		double bid =0;
		DecimalFormat df = new DecimalFormat("##.###");
		
		OS preferedOS = OS.LINUX;
//		HESSAM COMMENTED:
//		Region preferedRegion = Region.DEEPAK_TEST;
//		AZ chosenAz = AZ.ANY;
//		HESSAM COMMENTED END
		Region preferedRegion = Region.EUROPE;
		AZ chosenAz = AZ.A;

		instancesWithoutFreeSlot = new ArrayList<>();
		
		// Current time has exceeded the deadline
		if (currentTime >= wfUtil.getWorkflow().getDeadline()) {
			//System.out.println("Summary: " + SimulationData.singleton().getStats());
			//throw new IllegalArgumentException("No possible solution, moved past deadline @ " + CloudSim.clock() + " trying to schedule task " + task.getId() + " parent of " + wfUtil.getWorkflow().getWfDAG().getParents(taskId));
		}
		// No Sufficient Budget
		if (0 >= wfUtil.getWorkflow().getRemainingBudget()) {
			System.out.println("Summary: " + SimulationData.singleton().getStats());
			throw new IllegalArgumentException("No possible solution, Insufficient Budget. Remaining Budget @ "+ CloudSim.clock() + " trying to schedule task " + task.getId());
		}
		
		SchedulingDecision dec = null;
		/**
		 * If no free slot is available we decide on the pricing model and the bid price for the task
		 */
		double prevBidPr = 0;
		ArrayList<InstanceType> choosenTypes = new ArrayList<>();
		if (bufferTime > 0) {

			// Find a free task for the task to be allocated in an already running instance
			dec = PolicyUtils.findFreeSlot(myInstances, reachedMax, preferedRegion, usableAZsForTask, estimates, task, null, null, false, wfUtil, this.instancesWithoutFreeSlot);
			
			if(dec != null){
				PolicyUtils.printScheDec(task, LTO, criticalPathTime, remainingBudget, reqODBudget,	failureProb, dec.getBidPrice(), dec);
				setJobDeadline(dec,task);
				schDecArr.add(dec);
				return schDecArr;
			}
			
			// finds if an existing instance of the same Price Model can accommodate the new task
			dec = PolicyUtils.findRunningResource(myInstances, reachedMax, preferedRegion, usableAZsForTask, estimates, decPrModel, task, prevBidPr, null, wfUtil, instancesWithoutFreeSlot);
			if(dec != null){
				PolicyUtils.printScheDec(task, LTO, criticalPathTime, remainingBudget, reqODBudget,	failureProb, dec.getBidPrice(), dec);
				setJobDeadline(dec,task);
				schDecArr.add(dec);
				return schDecArr;
			}
		}
		if( ( bufferTime - SimProperties.DC_VM_INIT_TIME.asLong()) > 0) {
			if(this.broker.priceQuery(InstanceType.M1SMALL, preferedOS) < InstanceType.M1SMALL.getOnDemandPrice(null, null)){
			bid = biddingStrategy.bid(LTO, currentTime, InstanceType.M1SMALL, preferedOS, preferedRegion, chosenAz);
			bid = Double.parseDouble(df.format(bid));

			if (remainingBudget < bid) {
				if (remainingBudget <= reqODBudget) {
					throw new IllegalArgumentException(	"No possible solution, Insufficient Budget. Remaining Budget " + remainingBudget + " Lowest Bid Price "+ bid);
				} 
			} else {
				/*PriceFailureEstimator prFailEst = new PriceFailureEstimator();
				failureProb = prFailEst.computeFailureProbability(bid, preferedRegion, chosenAz, InstanceType.M1SMALL, preferedOS);
*/
				//if (failureProb < SimProperties.FAILURE_THRESHOLD.asDouble()) {
					decPrModel = PriceModel.SPOT;
					dec = new SchedulingDecision(task, false,
//							HESSAM
//							Region.getDefault(), AZ.ANY, InstanceType.M1SMALL,
							Region.EUROPE, AZ.A, InstanceType.M1SMALL,
							preferedOS, null, true, currentTime,
							longestEstimatedRunTime, bid, bid, 0,
							PriceModel.SPOT);
					PolicyUtils.printScheDec(task, LTO, criticalPathTime,
							remainingBudget, reqODBudget, failureProb,
							dec.getBidPrice(), dec);
					setJobDeadline(dec, task);
					schDecArr.add(dec);
					return schDecArr;
				//}
			}
			}
		}
		

		
		// If no spot instances are found, then find an on-demand instance.		
		long timeLeft = wfUtil.getWorkflow().getDeadline() - currentTime;
		double timeRatio = (double) timeLeft / ((double) (criticalPathTime + SimProperties.DC_VM_INIT_TIME.asLong()));
		//choosenTypes = PolicyUtils.chooseInstanceTypes(estimates, timeRatio, longestEstimatedRunTime);
		choosenTypes = PolicyUtils.chooseLiberalInstanceTypes(estimates, wfUtil);
		dec = PolicyUtils.findFreeSlot(myInstances, reachedMax, preferedRegion, usableAZsForTask, estimates, 
				task, PriceModel.ON_DEMAND,	choosenTypes, true, wfUtil, instancesWithoutFreeSlot);
		// If no instance found find a running instance
		if (dec == null) {
			// finds if an existing instance of the same Price Model can
			// accommodate the new task
			dec = PolicyUtils.findCriticalRunningResource(myInstances, reachedMax, preferedRegion, usableAZsForTask, 
					estimates, decPrModel, task, prevBidPr, choosenTypes, true,wfUtil, instancesWithoutFreeSlot);
		}
		if (dec != null) {
			setJobDeadline(dec, task);
			schDecArr.add(dec);
			//return schDecArr;
		} else {
			decPrModel = PriceModel.ON_DEMAND;
		}

		SimulationData.singleton().getProfiler().startPeriod(Metric.SCHED_NEW);
		
		/// If no running instance is found then start a new instance of the pricing model decided.
		if(null == dec){			
			if(decPrModel == PriceModel.ON_DEMAND){
				InstanceType cheapestInstance = InstanceType.M1SMALL;
				timeLeft = wfUtil.getWorkflow().getDeadline() - currentTime;
				timeRatio = (double) timeLeft / (double) (criticalPathTime + SimProperties.DC_VM_INIT_TIME.asLong());
				cheapestInstance = PolicyUtils.chooseAppropriateInstanceType(estimates, timeRatio, longestEstimatedRunTime, task, wfUtil, estimator);
//				HESSAM COMMENTED
//				dec = new SchedulingDecision(task, false, Region.getDefault(), AZ.ANY, cheapestInstance, preferedOS, null, true, currentTime,
				dec = new SchedulingDecision(task, false, Region.EUROPE, AZ.A, cheapestInstance, preferedOS, null, true, currentTime,
						estimates.get(cheapestInstance), PriceDB.getOnDemandPrice(region, cheapestInstance, preferedOS),
						PriceDB.getOnDemandPrice(region, cheapestInstance, preferedOS), 0, PriceModel.ON_DEMAND);
				schDecArr.add(dec);
			}
		}
		SimulationData.singleton().getProfiler().endPeriod(Metric.SCHED_NEW);
		
		
		PolicyUtils.printScheDec(task, LTO, criticalPathTime, remainingBudget, reqODBudget,	failureProb, bid, dec);
		
		setJobDeadline(dec,task);
		
		//if the code has come here means the slack time is minimal and there is a need to replicate the task
		if(computeLFTFlag){
			long deadline = wfUtil.getWorkflow().getDeadline();
			wfUtil.computeLFT(deadline);
			computeLFTFlag = false;
		}
		
		long EFT = task.getJob().getDeadline();
		long latestFinishTime = task.getJob().getLatestFinishTime();
		if (task.getJob().getNumberOfActiveTasks() <= 1) {
		for(int i=0; i < SimProperties.TASKDUP_NUM.asInt() ; i++){
		if ((EFT + SimProperties.DC_VM_INIT_TIME.asLong()) >= latestFinishTime) {
			//Making sure that the replication does not happen on the same resource as the other replica 
			Collection<Resource> unusedInstance = new LinkedHashSet<Resource>(myInstances);
			for(SchedulingDecision schDec : schDecArr){
				if(null != schDec.getResource()){
					unusedInstance.remove(schDec.getResource());
					instancesWithoutFreeSlot.remove(schDec.getResource());
				}
			}
						
			Task duplicateTask = task.getJob().newTask();
			final TaskState state = duplicateTask.getState();
			if (state != READY && state != POSTPONED) {
				throw new IllegalArgumentException(
						"Only READY and POSTPONED tasks can be scheduled. Probably a bug. State of task "
								+ duplicateTask.getId() + " is " + state);
			}

			SchedulingDecision dupDec = null;
			
			SimulationData.singleton().getStats().incrReplicas();

			dupDec = PolicyUtils.findFreeSlot(unusedInstance, reachedMax,
					preferedRegion, usableAZsForTask, estimates, duplicateTask,
					null, choosenTypes, true, wfUtil,
					instancesWithoutFreeSlot);
			// If no instance found find a running instance
			if (dupDec == null) {
				// finds if an existing instance of the same Price Model can
				// accommodate the new task
				dupDec = PolicyUtils.findCriticalRunningResource(unusedInstance,
						reachedMax, preferedRegion, usableAZsForTask,
						estimates, null, duplicateTask, prevBidPr,
						choosenTypes, true, wfUtil,
						instancesWithoutFreeSlot);
			}
			if (dupDec != null) {
				PolicyUtils.printScheDec(duplicateTask, LTO, criticalPathTime,
						remainingBudget, reqODBudget, failureProb,
						dupDec.getBidPrice(), dupDec);
				setJobDeadline(dupDec, duplicateTask);
				schDecArr.add(dupDec);
				return schDecArr;
			} else {
				decPrModel = PriceModel.SPOT;
			}
			
			SimulationData.singleton().getProfiler()
					.startPeriod(Metric.SCHED_NEW);

			// / If no running instance is found then start a new instance of
			// the pricing model decided.
			if (null == dupDec) {
				if (decPrModel == PriceModel.SPOT) {
					InstanceType cheapestInstance = InstanceType.M1SMALL;
					timeLeft = wfUtil.getWorkflow().getDeadline() - currentTime;
					timeRatio = (double) timeLeft
							/ (double) (criticalPathTime + SimProperties.DC_VM_INIT_TIME
									.asLong());
					cheapestInstance = PolicyUtils
							.chooseAppropriateInstanceType(estimates,
									timeRatio, longestEstimatedRunTime,
									duplicateTask, wfUtil, estimator);

					bid = biddingStrategy.bid(LTO, currentTime,
							cheapestInstance, preferedOS, preferedRegion,
							chosenAz);
					
					bid = Double.parseDouble(df.format(bid));
					if (remainingBudget < bid) {
						if (remainingBudget <= reqODBudget) {
							throw new IllegalArgumentException(
									"No possible solution, Insufficient Budget. Remaining Budget "
											+ remainingBudget
											+ " Lowest Bid Price " + bid);
						}
					} else {
						PriceFailureEstimator prFailEst = new PriceFailureEstimator();
						failureProb = prFailEst.computeFailureProbability(bid,
								preferedRegion, chosenAz, cheapestInstance,
								preferedOS);
						decPrModel = PriceModel.SPOT;
						dupDec = new SchedulingDecision(duplicateTask, false,
//								HESSAM COMMENTED
//								Region.getDefault(), AZ.ANY, cheapestInstance,
								Region.EUROPE, AZ.A, cheapestInstance,
								preferedOS, null, true, currentTime,
								estimates.get(cheapestInstance), bid, bid, 0,
								PriceModel.SPOT);

						schDecArr.add(dupDec);

						PolicyUtils.printScheDec(duplicateTask, LTO,
								criticalPathTime, remainingBudget, reqODBudget,
								failureProb, dupDec.getBidPrice(), dupDec);
						setJobDeadline(dupDec, duplicateTask);

					}

				}
			}
		}
		}
    	}
		return schDecArr;

    }
    
	private void setJobDeadline(SchedulingDecision dec, Task task) {
		long deadline = dec.getStartTime() + dec.getEstimatedRuntime();
		if(dec.isStartNewInstance()){
			deadline += SimProperties.DC_VM_INIT_TIME.asLong();
		}
		task.getJob().setDeadline(deadline);
	}
	
    @Override
    public void setBroker(final WorkflowBroker broker) {
	this.broker = broker;
	setWfUtil(new WorkflowUtilis(this.broker.getWorklfow()));
	this.biddingStrategy.setBroker(broker);
    }

    @Override
    public void setRuntimeEstimator(final RuntimeEstimator runtimeEstimator) {
	this.estimator = runtimeEstimator;
    }

    @Override
    public boolean usesEstimation() {
	return true;
    }
    
    public WorkflowUtilis getWfUtil() {
		return wfUtil;
	}

	public void setWfUtil(WorkflowUtilis workflowUtilis) {
		this.wfUtil = workflowUtilis;
	}

	@Override
	public SchedulingDecision sched(Task task,
			Collection<Resource> myInstances, boolean reachedMax, Region region) {
		// TODO Auto-generated method stub
		return null;
	}
}
