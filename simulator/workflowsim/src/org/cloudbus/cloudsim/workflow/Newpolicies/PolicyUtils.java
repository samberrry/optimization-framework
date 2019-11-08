package org.cloudbus.cloudsim.workflow.Newpolicies;

import static org.cloudbus.cloudsim.util.workload.TaskState.POSTPONED;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.util.workload.Job;
import org.cloudbus.cloudsim.util.workload.Task;
import org.cloudbus.cloudsim.util.workload.WFEdge;
import org.cloudbus.cloudsim.util.workload.WorkflowUtilis;
import org.cloudbus.cloudsim.util.workload.Job.JobStatus;
import org.cloudbus.spotsim.broker.ModelParameters;
import org.cloudbus.spotsim.broker.SchedulingDecision;
import org.cloudbus.spotsim.broker.resources.Resource;
import org.cloudbus.spotsim.broker.resources.ResourceState;
import org.cloudbus.spotsim.broker.rtest.RuntimeEstimator;
import org.cloudbus.spotsim.enums.AZ;
import org.cloudbus.spotsim.enums.InstanceType;
import org.cloudbus.spotsim.enums.OS;
import org.cloudbus.spotsim.enums.PriceModel;
import org.cloudbus.spotsim.enums.Region;
import org.cloudbus.spotsim.main.config.SimProperties;
import org.cloudbus.spotsim.simrecords.SimulationData;
import org.cloudbus.spotsim.simrecords.Profiler.Metric;

public class PolicyUtils {

	public static SchedulingDecision findFreeSlot(final Collection<Resource> myInstances,
			boolean reachedMax, Region region, EnumSet<AZ> usableAZsForTask,
			final Map<InstanceType, Long> estimates, Task task, PriceModel model, ArrayList<InstanceType> choosenTypes, boolean critical, WorkflowUtilis wfUtil,ArrayList<Resource> instancesWithoutFreeSlot) {
    		
    	final long clock = CloudSim.clock();
		long maxWaitTime = critical ? 0 : task.getJob().getLatestStartTime();
		final long maxStartTime = clock + maxWaitTime;
		
		
		//If you make LST as the max wait time then, for critical nodes it must be zero
		/*if(criticalNode && maxWaitTime >= SimProperties.DC_VM_INIT_TIME.asLong()){
			maxWaitTime = 0;
		}*/
		
		SimulationData.singleton().getProfiler().startPeriod(Metric.SCHED_RECYCLE);
		
		Resource suitableResource = null;
		SchedulingDecision dec = null;
		
		ArrayList<Resource> resOfNotChosenType = new ArrayList<>();
		
		for (final Resource res : myInstances) {
			Boolean modelFlag = model==null? true : res.getPriceModel()== model ;
			Boolean typeFlag = (choosenTypes==null || choosenTypes.isEmpty())? true : choosenTypes.contains(res.getType()) ;
			if (typeFlag && modelFlag && usableAZsForTask.contains(res.getAz())) {
				final long estimatedTime = estimates.get(res.getType());
				final long expectedIdleTime = res.getTimeThatItWillBecomeIdle();
				final long nextFullHour = res .getNextFullHourAfterBecomingIdle();
				final long gratisSeconds = nextFullHour - expectedIdleTime;
				double earliestCompletionTime = wfUtil.getWorkflow().getDeadline() - (wfUtil.getCriticalPathTime(res.getType()) - estimatedTime);
				if (expectedIdleTime <= maxStartTime || reachedMax) {
					if (estimatedTime <= gratisSeconds) {
						/*
						 * Job can be fit in less than one hour that has already
						 * been paid (execution is thus free, gratis)
						 */
						final double complTime = expectedIdleTime + estimatedTime;
						if (complTime < earliestCompletionTime && res.getState() != ResourceState.PENDING) {
							earliestCompletionTime = complTime;
							suitableResource = res;
						}else {
							instancesWithoutFreeSlot.add(res);
						}
					}else {
						instancesWithoutFreeSlot.add(res);
					}
				}else {
					instancesWithoutFreeSlot.add(res);
				}
			}else{
				resOfNotChosenType.add(res);
			}
		}
		
		// If resource of the choosen type are not found, find atleast the next best thing possibleee
		for (final Resource res : resOfNotChosenType) {
			Boolean modelFlag = model==null? true : res.getPriceModel()== model ;
			if (modelFlag && usableAZsForTask.contains(res.getAz())) {
				final long estimatedTime = estimates.get(res.getType());
				final long expectedIdleTime = res.getTimeThatItWillBecomeIdle();
				final long nextFullHour = res .getNextFullHourAfterBecomingIdle();
				final long gratisSeconds = nextFullHour - expectedIdleTime;
				double earliestCompletionTime = wfUtil.getWorkflow().getDeadline() - (wfUtil.getCriticalPathTime(res.getType()) - estimatedTime);
				if (expectedIdleTime <= maxStartTime || reachedMax) {
					if (estimatedTime <= gratisSeconds) {
						/*
						 * Job can be fit in less than one hour that has already
						 * been paid (execution is thus free, gratis)
						 */
						final double complTime = expectedIdleTime + estimatedTime;
						if (complTime < earliestCompletionTime && res.getState() != ResourceState.PENDING) {
							earliestCompletionTime = complTime;
							suitableResource = res;
						}else{
							instancesWithoutFreeSlot.add(res);
						}
					}else {
						instancesWithoutFreeSlot.add(res);
					}
				}else {
					instancesWithoutFreeSlot.add(res);
				}
			}
		}
		SimulationData.singleton().getProfiler().endPeriod(Metric.SCHED_RECYCLE);
		
		// found a suitable existing instance, will recycle it.
		if (suitableResource != null ) {
			final long startTime = suitableResource.getTimeThatItWillBecomeIdle();
			if (Log.logger.isLoggable(Level.INFO)) {
				Log.logger.info(Log.clock()
					+ "Reusing instance "
					+ suitableResource.getId()
					+ " for job "
					+ task.getId()
					+ ", idle time: "
					+ startTime
					+ ", status: "
					+ suitableResource.getState());
			}
			final long estimatedRunTime = estimates.get(suitableResource.getType());
			dec = new SchedulingDecision(task, false, Region.getDefault(),
			suitableResource.getAz(), suitableResource.getType(), OS.getDefault(),
			suitableResource, false, startTime, estimatedRunTime, 0D, suitableResource.getBid(), maxWaitTime, suitableResource.getPriceModel());
		}
		
		return dec;
		
    	
    }
	
	public static SchedulingDecision findRunningResource(final Collection<Resource> myInstances,
			boolean reachedMax, Region region, EnumSet<AZ> usableAZsForTask,
			final Map<InstanceType, Long> estimates, PriceModel priceModel, Task task, double bidPrice, 
			ArrayList<InstanceType> choosenTypes, WorkflowUtilis wfUtil, ArrayList<Resource> instancesWithoutFreeSlot) {
		final long clock = CloudSim.clock();
		//long maxWaitTime = critical ? 0 : task.getJob().getLatestStartTime();
		final long maxStartTime =  clock+ task.getJob().getLatestStartTime();
	//	double earliestCompletionTime = clock + SimProperties.DC_VM_INIT_TIME.asLong();
		
		//Most often starting a VM can take a long time and we could use a running VM which is available before the start of a new VM
		//final long avoidNewVmCreationTime = clock+ SimProperties.DC_VM_INIT_TIME.asLong();
		
		SimulationData.singleton().getProfiler().startPeriod(Metric.SCHED_RECYCLE);
		
		Resource suitableResource = null;
		SchedulingDecision dec = null;
		ArrayList<Resource> resOfNotChosenType = new ArrayList<>();
		
		//If there are no instances with free slots, we pick instances with the earliest start time
		// this avoids the time to boot up an instance. 
		
		if( null == suitableResource && instancesWithoutFreeSlot.size() > 0){
			long earlyStart = Long.MAX_VALUE;
			for(final Resource res : instancesWithoutFreeSlot){
				final long estimatedTime = estimates.get(res.getType());
				final long expectedIdleTime = res.getTimeThatItWillBecomeIdle();
				double earliestCompletionTime = wfUtil.getWorkflow().getDeadline() - (wfUtil.getCriticalPathTime(res.getType()) - estimatedTime);
				Boolean modelFlag = priceModel==null? true : res.getPriceModel()== priceModel ;
				Boolean typeFlag = (choosenTypes==null || choosenTypes.isEmpty())? true : choosenTypes.contains(res.getType()) ;
				if (typeFlag && modelFlag) {
					if (expectedIdleTime <= maxStartTime || reachedMax) {
						final double complTime = expectedIdleTime + estimatedTime;
						if (complTime < earliestCompletionTime && expectedIdleTime < earlyStart && res.getState() != ResourceState.PENDING) {
							//earliestCompletionTime = complTime;
							earlyStart = expectedIdleTime;
							suitableResource = res;
						}else{
							resOfNotChosenType.add(res);
						}
					}
				}else{
					resOfNotChosenType.add(res);
				}
			}
		}
		
		// If resource of the choosen type are not found, find atleast the next best thing possibleee
		long earlyStart = Long.MAX_VALUE;
		for (final Resource res : resOfNotChosenType) {
			Boolean modelFlag = priceModel==null? true : res.getPriceModel()== priceModel ;
			if (modelFlag) {
				final long expectedIdleTime = res.getTimeThatItWillBecomeIdle();
				final long estimatedTime = estimates.get(res.getType());
				double earliestCompletionTime = wfUtil.getWorkflow().getDeadline() - (wfUtil.getCriticalPathTime(res.getType()) - estimatedTime);
				if (expectedIdleTime <= maxStartTime || reachedMax) {
					final double complTime = expectedIdleTime + estimatedTime;
					if (complTime < earliestCompletionTime && expectedIdleTime < earlyStart && res.getState() != ResourceState.PENDING) {
						earlyStart = expectedIdleTime;
						suitableResource = res;
					}
				}
			}
		}
		
		SimulationData.singleton().getProfiler().endPeriod(Metric.SCHED_RECYCLE);
		
		// found a suitable existing instance, will recycle it.
		if (suitableResource != null ) {
			final long startTime = suitableResource.getTimeThatItWillBecomeIdle();
			if (Log.logger.isLoggable(Level.INFO)) {
				Log.logger.fine(Log.clock()
					+ "Reusing instance "
					+ suitableResource.getId()
					+ " for job "
					+ task.getId()
					+ ", idle time: "
					+ startTime
					+ ", status: "
					+ suitableResource.getState());
			}
			final long estimatedRunTime = estimates.get(suitableResource.getType());
			dec = new SchedulingDecision(task, false, Region.getDefault(),
			suitableResource.getAz(), suitableResource.getType(), OS.getDefault(),
			suitableResource, false, startTime, estimatedRunTime, 0D, bidPrice, suitableResource.getTimeThatItWillBecomeIdle() - clock, suitableResource.getPriceModel());
		}
		
		return dec;
	}
	
	public static SchedulingDecision findCriticalRunningResource(final Collection<Resource> myInstances,
			boolean reachedMax, Region region, EnumSet<AZ> usableAZsForTask,
			final Map<InstanceType, Long> estimates, PriceModel priceModel, Task task, double bidPrice, 
			ArrayList<InstanceType> choosenTypes, boolean critical, WorkflowUtilis wfUtil, ArrayList<Resource> instancesWithoutFreeSlot) {
		final long clock = CloudSim.clock();
		long maxWaitTime = critical ? 0 : task.getJob().getLatestStartTime();
		final long maxStartTime =  clock+ maxWaitTime;
	//	double earliestCompletionTime = clock + SimProperties.DC_VM_INIT_TIME.asLong();
		
		//Most often starting a VM can take a long time and we could use a running VM which is available before the start of a new VM
		//final long avoidNewVmCreationTime = clock+ SimProperties.DC_VM_INIT_TIME.asLong();
		
		SimulationData.singleton().getProfiler().startPeriod(Metric.SCHED_RECYCLE);
		
		Resource suitableResource = null;
		SchedulingDecision dec = null;
		ArrayList<Resource> resOfNotChosenType = new ArrayList<>();
		
		//If there are no instances with free slots, we pick instances with the earliest start time
		// this avoids the time to boot up an instance. 
		
		if( null == suitableResource && instancesWithoutFreeSlot.size() > 0){
			long earlyStart = Long.MAX_VALUE;
			for(final Resource res : instancesWithoutFreeSlot){
				final long estimatedTime = estimates.get(res.getType());
				final long expectedIdleTime = res.getTimeThatItWillBecomeIdle();
				double earliestCompletionTime = wfUtil.getWorkflow().getDeadline() - (wfUtil.getCriticalPathTime(res.getType()) - estimatedTime);
				Boolean modelFlag = priceModel==null? true : res.getPriceModel()== priceModel ;
				Boolean typeFlag = (choosenTypes==null || choosenTypes.isEmpty())? true : choosenTypes.contains(res.getType()) ;
				if (typeFlag && modelFlag) {
					if (expectedIdleTime <= maxStartTime || reachedMax) {
						final double complTime = expectedIdleTime + estimatedTime;
						if (complTime < earliestCompletionTime && expectedIdleTime < earlyStart && res.getState() != ResourceState.PENDING) {
							//earliestCompletionTime = complTime;
							earlyStart = expectedIdleTime;
							suitableResource = res;
						}else{
							resOfNotChosenType.add(res);
						}
					}
				}else{
					resOfNotChosenType.add(res);
				}
			}
		}
		
		// If resource of the choosen type are not found, find atleast the next best thing possibleee
		long earlyStart = Long.MAX_VALUE;
		for (final Resource res : resOfNotChosenType) {
			Boolean modelFlag = priceModel==null? true : res.getPriceModel()== priceModel ;
			if (modelFlag) {
				final long estimatedTime = estimates.get(res.getType());
				final long expectedIdleTime = res.getTimeThatItWillBecomeIdle();
				double earliestCompletionTime = wfUtil.getWorkflow().getDeadline() - (wfUtil.getCriticalPathTime(res.getType()) - estimatedTime);
				if (expectedIdleTime <= maxStartTime || reachedMax) {
					final double complTime = expectedIdleTime + estimatedTime;
					if (complTime < earliestCompletionTime && expectedIdleTime < earlyStart && res.getState() != ResourceState.PENDING) {
						earlyStart = expectedIdleTime;
						suitableResource = res;
					}
				}
			}
		}
		
		SimulationData.singleton().getProfiler().endPeriod(Metric.SCHED_RECYCLE);
		
		// found a suitable existing instance, will recycle it.
		if (suitableResource != null ) {
			final long startTime = suitableResource.getTimeThatItWillBecomeIdle();
			if (Log.logger.isLoggable(Level.INFO)) {
				Log.logger.fine(Log.clock()
					+ "Reusing instance "
					+ suitableResource.getId()
					+ " for job "
					+ task.getId()
					+ ", idle time: "
					+ startTime
					+ ", status: "
					+ suitableResource.getState());
			}
			final long estimatedRunTime = estimates.get(suitableResource.getType());
			dec = new SchedulingDecision(task, false, Region.getDefault(),
			suitableResource.getAz(), suitableResource.getType(), OS.getDefault(),
			suitableResource, false, startTime, estimatedRunTime, 0D, bidPrice, suitableResource.getTimeThatItWillBecomeIdle() - clock, suitableResource.getPriceModel());
		}
		
		return dec;
	}
	
	/*public static SchedulingDecision findCriticalRunningResource(final Collection<Resource> myInstances,
			boolean reachedMax, Region region, EnumSet<AZ> usableAZsForTask,
			final Map<InstanceType, Long> estimates, PriceModel priceModel, Task task, double bidPrice, 
			ArrayList<InstanceType> choosenTypes, boolean critical, long criticalPathTime, ArrayList<Resource> instancesWithoutFreeSlot) {
		final long clock = CloudSim.clock();
		long maxWaitTime = critical ? 0 : task.getJob().getLatestStartTime();
		final long maxStartTime =  clock+ maxWaitTime;
	//	double earliestCompletionTime = clock + SimProperties.DC_VM_INIT_TIME.asLong();
		
		//Most often starting a VM can take a long time and we could use a running VM which is available before the start of a new VM
		//final long avoidNewVmCreationTime = clock+ SimProperties.DC_VM_INIT_TIME.asLong();
		
		SimulationData.singleton().getProfiler().startPeriod(Metric.SCHED_RECYCLE);
		
		Resource suitableResource = null;
		SchedulingDecision dec = null;
		ArrayList<Resource> resOfNotChosenType = new ArrayList<>();
		
		//If there are no instances with free slots, we pick instances with the earliest start time
		// this avoids the time to boot up an instance. 
		
		if( null == suitableResource && instancesWithoutFreeSlot.size() > 0){
			long earlyStart = Long.MAX_VALUE;
			for(final Resource res : instancesWithoutFreeSlot){
				final long estimatedTime = estimates.get(res.getType());
				final long expectedIdleTime = res.getTimeThatItWillBecomeIdle();
				Boolean modelFlag = priceModel==null? true : res.getPriceModel()== priceModel ;
				Boolean typeFlag = (choosenTypes==null || choosenTypes.isEmpty())? true : choosenTypes.contains(res.getType()) ;
				if (typeFlag && modelFlag) {
					if (expectedIdleTime <= maxStartTime || reachedMax) {
						final double complTime = expectedIdleTime + estimatedTime;
						if (expectedIdleTime < earlyStart && res.getState() != ResourceState.PENDING) {
							//earliestCompletionTime = complTime;
							earlyStart = expectedIdleTime;
							suitableResource = res;
						}else{
							resOfNotChosenType.add(res);
						}
					}
				}else{
					resOfNotChosenType.add(res);
				}
			}
		}
		
		// If resource of the choosen type are not found, find atleast the next best thing possibleee
		long earlyStart = Long.MAX_VALUE;
		for (final Resource res : resOfNotChosenType) {
			Boolean modelFlag = priceModel==null? true : res.getPriceModel()== priceModel ;
			if (modelFlag) {
				final long expectedIdleTime = res.getTimeThatItWillBecomeIdle();
				if (expectedIdleTime <= maxStartTime || reachedMax) {
					if (expectedIdleTime < earlyStart && res.getState() != ResourceState.PENDING) {
						earlyStart = expectedIdleTime;
						suitableResource = res;
					}
				}
			}
		}
		
		SimulationData.singleton().getProfiler().endPeriod(Metric.SCHED_RECYCLE);
		
		// found a suitable existing instance, will recycle it.
		if (suitableResource != null ) {
			final long startTime = suitableResource.getTimeThatItWillBecomeIdle();
			if (Log.logger.isLoggable(Level.INFO)) {
				Log.logger.fine(Log.clock()
					+ "Reusing instance "
					+ suitableResource.getId()
					+ " for job "
					+ task.getId()
					+ ", idle time: "
					+ startTime
					+ ", status: "
					+ suitableResource.getState());
			}
			final long estimatedRunTime = estimates.get(suitableResource.getType());
			dec = new SchedulingDecision(task, false, Region.getDefault(),
			suitableResource.getAz(), suitableResource.getType(), OS.getDefault(),
			suitableResource, false, startTime, estimatedRunTime, 0D, bidPrice, suitableResource.getTimeThatItWillBecomeIdle() - clock, suitableResource.getPriceModel());
		}
		
		return dec;
	}*/
	
	/**
	 * Choose the most appropriate instance based on the critical path and the deadline i.e time left
	 * @param estimates
	 * @param timeRatio
	 * @param longestEstimatedRunTime
	 * @param estimator 
	 * @return
	 */
	public static InstanceType chooseAppropriateInstanceType(Map<InstanceType, Long> estimates,
			double timeRatio, long longestEstimatedRunTime, Task task, WorkflowUtilis wfUtil, RuntimeEstimator estimator) {
		
		InstanceType choosenInstance = null;
		ArrayList<InstanceType> instanceTypes = new ArrayList<>();
		Map<InstanceType, Double> priceInstanceMap = new HashMap<>();
		Map<InstanceType, Double> ratioInstanceMap = new HashMap<>();
		class ValueComparator implements Comparator<InstanceType> {

		    Map<InstanceType, Double> base;
		    public ValueComparator(Map<InstanceType, Double> base) {
		        this.base = base;
		    }

		    // Note: this comparator imposes orderings that are inconsistent with equals.    
		    public int compare(InstanceType a, InstanceType b) {
		        if (base.get(a) > base.get(b)) {
		            return -1;
		        }else if(base.get(a) == base.get(b)){ 
		        	if(a.getOnDemandPrice(null, null) >= b.getOnDemandPrice(null, null)){
		        		return -1;
		        	}else{
		        		return 1;
		        	}
		        }else {
		            return 1;
		        } // returning 0 would merge keys
		    }
		}
		ValueComparator bvc =  new ValueComparator(priceInstanceMap);
		ValueComparator ratiobvc =  new ValueComparator(ratioInstanceMap);
        TreeMap<InstanceType,Double> sorted_map = new TreeMap<InstanceType,Double>(bvc);
        TreeMap<InstanceType,Double> instaceRatioMap = new TreeMap<InstanceType,Double>(ratiobvc);
		
		long remainingDeadline = wfUtil.getWorkflow().getDeadline() - CloudSim.clock();
		ArrayList<Job> criticalPathJobs;
		// Instead of the critical path jobs I am trying to get the longest path and work on them 
		//final ArrayList<Job> criticalPathJobs = wfUtil.getJobsLongestPath(task.getJob().getIntId());
		for (Map.Entry<InstanceType, Long> entry : estimates.entrySet())
		{
			long criticalPathTime =0;
			Job prevJob = null;
			long edgeTime = 0L;
			long totalEdgeTime = 0L;
			criticalPathJobs = wfUtil.getCriticalPathJobs(entry.getKey());
			if(null == criticalPathJobs || criticalPathJobs.isEmpty()){
				criticalPathJobs = wfUtil.computeCriticalPathJobs(entry.getKey());
				wfUtil.setCriticalPathJobs(entry.getKey(), criticalPathJobs);
			}
			//criticalPathJobs = wfUtil.computeCriticalPathJobs(entry.getKey());
			for(Job job : criticalPathJobs){
				if(null != job){
					if (prevJob != null) {
						edgeTime = job.getEdge(prevJob.getIntId());
						edgeTime = (edgeTime > 0) ? edgeTime : prevJob
								.getEdge(job.getIntId());
					}
					criticalPathTime += ModelParameters.execTimeParallel(job
							.getA(), job.getSigma(), entry.getKey()
							.getEc2units(), job.getLength());
					totalEdgeTime += edgeTime;
					prevJob = job;
					// System.out.println("CP "+ job);
				}
	    	}
			//System.out.println("========");
    		//System.out.println(" CPT " + criticalPathTime +" Type " + entry.getKey() + " deadline " + remainingDeadline);
			long fullTime = criticalPathTime + SimProperties.DC_VM_INIT_TIME.asLong();
			remainingDeadline -= totalEdgeTime;
			
			
			double ratio = (double)fullTime/(double)remainingDeadline;
			//System.out.println(" Time ratioooo "+ timeRatio + " ratioooo " + ratio);
			//formatting just to get a rough approximation and not to pring on details
			DecimalFormat df = new DecimalFormat("#.##");
			double ratioFormatted = Double.parseDouble(df.format(ratio));
			ratioInstanceMap.put(entry.getKey(), ratioFormatted);
			if( ratio > 0 && ratio < timeRatio){
				instanceTypes.add(entry.getKey());
				double instancePrice = Math.ceil((fullTime+totalEdgeTime)/SimProperties.PRICING_CHARGEABLE_PERIOD.asDouble())*entry.getKey().getOnDemandPrice(null, null);
				priceInstanceMap.put(entry.getKey(), instancePrice);
				//System.out.println(" Instance " + entry.getKey() + " Price " + instancePrice);
			}  
		}
		if(instanceTypes.isEmpty()){
			//added on 24/09/2014
			instaceRatioMap.putAll(ratioInstanceMap);
			choosenInstance = instaceRatioMap.lastKey();
			//choosenInstance=SimProperties.LIBERAL_POLICY_REF_INSTANCE.asEnum(InstanceType.class);
			//System.out.println("Summary: " + SimulationData.singleton().getStats());
			//throw new IllegalArgumentException("No possible solution, moved past deadline @ " + CloudSim.clock() + " trying to schedule task " + task.getId() + " parent of " + wfUtil.getWorkflow().getWfDAG().getParents(task.getJob().getIntId()));
		}else{
			 sorted_map.putAll(priceInstanceMap);
			 choosenInstance = sorted_map.lastKey();
			/*for(InstanceType type : instanceTypes){
				if(instanceBudget > type.getOnDemandPrice(null, null)){
					instanceBudget = type.getOnDemandPrice(null, null);
					choosenInstance = type;
				}
			}*/
		}
		return choosenInstance;
	}
	
	
	private static int getInstanceDetails(
			Map<InstanceType, AtomicInteger> odInstanceTypesUsed) {
		int instanceDetails = 0;
		for(InstanceType type : odInstanceTypesUsed.keySet()){
			instanceDetails += odInstanceTypesUsed.get(type).intValue();	
		}
		return instanceDetails;
	}

	public static ArrayList<InstanceType> chooseLiberalInstanceTypes(Map<InstanceType, Long> estimates, WorkflowUtilis wfUtil) {
		
		ArrayList<InstanceType> instanceTypes = new ArrayList<>();
		Map<InstanceType, Double> priceInstanceMap = new HashMap<>();
		long deadline = wfUtil.getWorkflow().getDeadline() - CloudSim.clock();
		ArrayList<Job> criticalPathJobs;
		
		// Instead of the critical path jobs I am trying to get the longest path and work on them 
		for (Map.Entry<InstanceType, Long> entry : estimates.entrySet())
		{
			long criticalPathTime =0;
			Job prevJob = null;
			long edgeTime = 0L;
			long totalEdgeTime = 0L;
			criticalPathJobs = wfUtil.getCriticalPathJobs(entry.getKey());
			if(null == criticalPathJobs || criticalPathJobs.isEmpty()){
				criticalPathJobs = wfUtil.computeCriticalPathJobs(entry.getKey());
				wfUtil.setCriticalPathJobs(entry.getKey(), criticalPathJobs);
			}
			for(Job job : criticalPathJobs){
				if(null != job){
					if (prevJob != null) {
						edgeTime = job.getEdge(prevJob.getIntId());
						edgeTime = (edgeTime > 0) ? edgeTime : prevJob
								.getEdge(job.getIntId());
					}
					criticalPathTime += ModelParameters.execTimeParallel(job
							.getA(), job.getSigma(), entry.getKey()
							.getEc2units(), job.getLength());
					totalEdgeTime += edgeTime;
					prevJob = job;
				}
	    	}
			long fullTime = criticalPathTime + SimProperties.DC_VM_INIT_TIME.asLong();
			deadline -= totalEdgeTime;
			
			if(  fullTime <= deadline){
				instanceTypes.add(entry.getKey());
				double instancePrice = Math.ceil((fullTime+totalEdgeTime)/SimProperties.PRICING_CHARGEABLE_PERIOD.asDouble())*entry.getKey().getOnDemandPrice(null, null);
				priceInstanceMap.put(entry.getKey(), instancePrice);
			}  
		}
		if(instanceTypes.isEmpty()){
			instanceTypes.add(SimProperties.LIBERAL_POLICY_REF_INSTANCE.asEnum(InstanceType.class));
		}
		return instanceTypes;
	}
	
	
	/**
     * Choose all instances with a runtime suitable enough to finish the critical path
     * @param estimates
     * @param timeRatio
     * @param longestEstimatedRunTime
     * @return
     */
	public static ArrayList<InstanceType> chooseInstanceTypes(Map<InstanceType, Long> estimates,
			double timeRatio, long longestEstimatedRunTime) {
		
		double tempRatio = 0.0;
		ArrayList<InstanceType> choosenInstances = new ArrayList<>();
		for (Map.Entry<InstanceType, Long> entry : estimates.entrySet())
		{
			double ratio = entry.getValue().doubleValue()/longestEstimatedRunTime;
			if(ratio <= timeRatio){
				tempRatio = ratio;
				choosenInstances.add(entry.getKey());
			}  
		}
		/*if(choosenInstances.isEmpty()){
			choosenInstances.add(InstanceType.M1SMALL);
		}*/
		return choosenInstances;
	}
	
	public static Map<InstanceType, Long> computeRunTimes(final Task task) {
		
		EnumMap<InstanceType, Long> estimates = new EnumMap<InstanceType, Long>(InstanceType.class);

		//mowsc

		// pre-compute task running times on all instance types
//		InstanceType[] values = {InstanceType.M1SMALL, InstanceType.M1MEDIUM, InstanceType.M1LARGE, InstanceType.M1XLARGE, InstanceType.M3XLARGE, InstanceType.M32XLARGE, InstanceType.M22XLARGE, InstanceType.M24XLARGE, InstanceType.M2XLARGE};
//		//InstanceType[] values = {InstanceType.M1SMALL};
//		//InstanceType[] values = InstanceType.values();
//		for (final InstanceType instanceType : values) {
//		    final long execTimeParallel = ModelParameters.execTimeParallel(task.getJob().getA(),
//			task.getJob().getSigma(), instanceType.getEc2units(), task.getJob().getLength());
//		    if (execTimeParallel <= 0) {
//			throw new IllegalStateException("Task runtime estimate must be greater than 0");
//		    }
//		    estimates.put(instanceType, execTimeParallel);
//		}
		
		return estimates;
	}
	
	public static long computeRelaxedLTO(long strictLTO, long currentTime) {
		double threshold = SimProperties.CONTENTION_THRESHOLD_FACTOR.asDouble();
		double sigma = SimProperties.CONTENTION_FACTOR_SIGMA.asDouble();
		
		double gamma = threshold * Math.exp(sigma*(strictLTO-currentTime));
		double relaxedLTO = gamma*strictLTO;
		return (long)relaxedLTO;
	}
	
	public static void printScheDec(final Task task, long LTO, long criticalPathTime,
			double remainingBudget, double reqODBudget, double failureProb,
			double bid, SchedulingDecision dec) {
		
		DecimalFormat df = new DecimalFormat("##.###");
		if (Log.logger.isLoggable(Level.INFO)) {
		    Log.logger.info(Log.clock()
			    + " SCHEDULING JOB: "
			    + task.getId()
			    + ", length: "
			    + task.getJob().getLength()
			    + ", LTO: "
			    + LTO
			    + ", Critical Path: "
			    + criticalPathTime
			    + ", Task CP wght: "
			    + task.getJob().getCriticalPathWeight()
			    + ", Type: "
			    + dec.getInstanceType()
			    + ", LST "
			    + task.getJob().getLatestStartTime()
			    + ", Remaining Budget "
			    + df.format(remainingBudget)
			    + ", Bid: "
			    + bid
			    + ", Failure Probability: "
			    + df.format(failureProb)
			    + ", Price Model: "
			    + dec.getPriceModel()
			    + ", New Instance: "
			    + dec.isStartNewInstance()
			    + ", Resource Id: "
			    + (null == dec.getResource() ? null : dec.getResource().getId()));
		}
	}
	
	public static boolean toPostponeOrNotToPostpone(final Task task, final long LST, final boolean reachedMax) {
		return // postpone if
		       // maximum number of instances reached, OR
		//reachedMax
		// the LST if more than twice of VM init time, and hasn't been
		// postponed earlier
			LST > (2 * SimProperties.DC_VM_INIT_TIME.asLong())
			&& !(task.getState() == POSTPONED);
	}
	
	public static long refineCriticalPathTime(WorkflowUtilis wfUtil){
		long cpTime = 0;
		ArrayList<Job> criticalPathJobs = wfUtil.getCriticalPathJobs();
		Collections.reverse(criticalPathJobs);
		Job prevJob = null;
		for(Job job : criticalPathJobs){
			long cpParentWeight = job.getLength();
			if(job.getStatus() == JobStatus.RUNNING){
				cpParentWeight = job.getDeadline() - CloudSim.clock();
			}
			if(prevJob != null){
				cpTime += cpParentWeight + prevJob.getEdge(job.getIntId());
			}else{
				cpTime += cpParentWeight;
			}
			prevJob = job;
			//Log.logger.warning("JOB id "+ job.getIntId()+ "CPtime " + cpTime);
		}
		
		
		return cpTime;
		
	}
	
}
