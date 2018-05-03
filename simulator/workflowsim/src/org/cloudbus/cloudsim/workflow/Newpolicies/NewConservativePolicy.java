package org.cloudbus.cloudsim.workflow.Newpolicies;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.util.workload.Job;
import org.cloudbus.cloudsim.util.workload.Task;
import org.cloudbus.cloudsim.util.workload.WorkflowUtilis;
import org.cloudbus.cloudsim.workflow.biddingstrategy.IntelligentBiddingStrategy;
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

public class NewConservativePolicy implements WorkflowSchedulingPolicy {

	private static final String NAME = "NewConservative";

    private WorkflowBroker broker;

    private WorkflowUtilis wfUtil;
    
    protected RuntimeEstimator estimator;
    
    protected ArrayList<Resource> instancesWithoutFreeSlot;
    
    private WorkflowBiddingStrategy biddingStrategy = new IntelligentBiddingStrategy();
    
    public NewConservativePolicy() {
    	
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
    public SchedulingDecision sched(final Task task, final Collection<Resource> myInstances,
	    boolean reachedMax, Region region) {

		EnumSet<AZ> usableAZsForTask = region.getAvailabilityZones();
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
		
		LTO = SimProperties.CONTENTION_FLAG.asBoolean() ? PolicyUtils.computeRelaxedLTO(LTO, currentTime) : LTO;
		
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
		Region preferedRegion = Region.DEEPAK_TEST;
		AZ chosenAz = AZ.ANY;
		
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
				return dec;
			}
			
			// finds if an existing instance of the same Price Model can accommodate the new task
			dec = PolicyUtils.findRunningResource(myInstances, reachedMax, preferedRegion, usableAZsForTask, estimates, decPrModel, task, prevBidPr, null, wfUtil, instancesWithoutFreeSlot);
			if(dec != null){
				PolicyUtils.printScheDec(task, LTO, criticalPathTime, remainingBudget, reqODBudget,	failureProb, dec.getBidPrice(), dec);
				setJobDeadline(dec,task);
				return dec;
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
				PriceFailureEstimator prFailEst = new PriceFailureEstimator();
				failureProb = prFailEst.computeFailureProbability(bid, preferedRegion, chosenAz, InstanceType.M1SMALL, preferedOS);

				if (failureProb < SimProperties.FAILURE_THRESHOLD.asDouble()) {
					decPrModel = PriceModel.SPOT;
					dec = new SchedulingDecision(task, false, Region.getDefault(), AZ.ANY, InstanceType.M1SMALL, preferedOS, null, true, currentTime, 
							longestEstimatedRunTime, bid, bid, 0, PriceModel.SPOT);
					PolicyUtils.printScheDec(task, LTO, criticalPathTime, remainingBudget, reqODBudget,	failureProb, dec.getBidPrice(), dec);
					setJobDeadline(dec,task);					
					return dec;
				}
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
			dec = PolicyUtils.findRunningResource(myInstances, reachedMax, preferedRegion, usableAZsForTask, 
					estimates, decPrModel, task, prevBidPr, choosenTypes, wfUtil, instancesWithoutFreeSlot);
		}
		if (dec != null) {
			PolicyUtils.printScheDec(task, LTO, criticalPathTime, remainingBudget, reqODBudget, failureProb, dec.getBidPrice(), dec);
			setJobDeadline(dec, task);
			return dec;
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
				dec = new SchedulingDecision(task, false, Region.getDefault(), AZ.ANY, cheapestInstance, preferedOS, null, true, currentTime, 
						estimates.get(cheapestInstance), PriceDB.getOnDemandPrice(region, cheapestInstance, preferedOS), 
						PriceDB.getOnDemandPrice(region, cheapestInstance, preferedOS), 0, PriceModel.ON_DEMAND);
			}
		}
		SimulationData.singleton().getProfiler().endPeriod(Metric.SCHED_NEW);
		
		PolicyUtils.printScheDec(task, LTO, criticalPathTime, remainingBudget, reqODBudget,	failureProb, bid, dec);
		
		setJobDeadline(dec,task);
			
		return dec;

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
	public ArrayList<SchedulingDecision> schedule(Task task,
			Collection<Resource> myInstances, boolean reachedMax, Region region) {

		ArrayList<SchedulingDecision> schDec = new ArrayList<>();
		SchedulingDecision sched = this.sched(task, myInstances, reachedMax, region);
		schDec.add(sched);
		return schDec;
	}
}
