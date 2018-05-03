package org.cloudbus.spotsim.simrecords;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.cloudbus.cloudsim.util.workload.BidPojo;
import org.cloudbus.spotsim.enums.InstanceType;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * Various simulation statistics. Although most of these statistics could be
 * calculated from other data structures, they are kept in these primitive
 * variables for efficiency purposes, especially for logging and debugging
 * during runtime.
 */
@XStreamAlias("stats")
public class Statistics {

    @XStreamAlias("ert")
    @XStreamAsAttribute
    private double estimatedRunTime = 0D;

    @XStreamAlias("ec")
    @XStreamAsAttribute
    private double estimatedCost = 0D;

    @XStreamAlias("ac")
    @XStreamAsAttribute
    private double actualCost = 0D;

    @XStreamAlias("art")
    @XStreamAsAttribute
    private double actualRuntime = 0D;

    @XStreamAlias("wt")
    @XStreamAsAttribute
    private double waitingTime = 0D;

    @XStreamAlias("js")
    @XStreamAsAttribute
    private int jobsSubmitted = 0;

    @XStreamAlias("jc")
    @XStreamAsAttribute
    private int jobsCompleted = 0;

    @XStreamAlias("jf")
    @XStreamAsAttribute
    private int totalInstancesFailed = 0;
    
    @XStreamAlias("resourFault")
    @XStreamAsAttribute
    private int resourceFault = 0;

    @XStreamAlias("jl")
    @XStreamAsAttribute
    private int jobsLapsed;
    
    @XStreamAlias("jobf")
    @XStreamAsAttribute
    private int jobsFailed;

    @XStreamAlias("irq")
    @XStreamAsAttribute
    private int instancesRequested = 0;

    @XStreamAlias("irc")
    @XStreamAsAttribute
    private int instancesReceived = 0;

    @XStreamAlias("itr")
    @XStreamAsAttribute
    private int instancesTerminated = 0;

    @XStreamAlias("idtime")
    @XStreamAsAttribute
    private double totalIdleTime = 0D;

    @XStreamAlias("insttime")
    @XStreamAsAttribute
    private double totalInstanceTime = 0D;

    @XStreamAlias("ut")
    @XStreamAsAttribute
    private double utilization = 0D;

    @XStreamAlias("dlbr")
    @XStreamAsAttribute
    private int deadlineBreaches = 0;
    
    @XStreamAlias("perdlbr")
    @XStreamAsAttribute
    private double precentDlBreaches = 0;

    @XStreamAlias("wm")
    @XStreamAsAttribute
    private double wasted = 0D;

    @XStreamAlias("typesused")
    private final Map<InstanceType, AtomicInteger> onDemandInstanceTypesUsed;
    
    //Added by deepak to report number of instances used by spot instances
    @XStreamAlias("spottypesused")
    private final Map<InstanceType, AtomicInteger> spotInstanceTypesUsed;
    
    //Added by deepak to report number of instances used by spot instances
    @XStreamAlias("spotpicehist")
    private final Map<Integer, Double> spotPriceHistory;
    
    //Added by deepak to report number of instances used by spot instances
    @XStreamAlias("spotpicepojo")
    private final ArrayList<BidPojo> spotPricePojo;

    @XStreamAlias("finishtime")
    @XStreamAsAttribute
    private long finishTime;

    @XStreamAlias("ftoverhead")
    @XStreamAsAttribute
    private long ftOverhead = 0L;

    @XStreamAlias("redunt")
    @XStreamAsAttribute
    private long redudantProcessing;
    
    //for workflows -- changed by deepak
    @XStreamAlias("totalExecTime")
    @XStreamAsAttribute
    private double totalExecutiontime;
    
    //Added by deepak for task duplication
    @XStreamAlias("replicas")
    @XStreamAsAttribute
    private int replicas;

    public Statistics() {
	this.onDemandInstanceTypesUsed = new HashMap<InstanceType, AtomicInteger>();
	for (final InstanceType instanceType : InstanceType.values()) {
	    this.onDemandInstanceTypesUsed.put(instanceType, new AtomicInteger());
	}
	this.spotInstanceTypesUsed = new HashMap<InstanceType, AtomicInteger>();
	for (final InstanceType instanceType : InstanceType.values()) {
	    this.spotInstanceTypesUsed.put(instanceType, new AtomicInteger());
	}
	this.spotPriceHistory = new TreeMap<Integer, Double>();
	this.spotPricePojo = new ArrayList<>();
    }

    public static String getTime(final long t) {

	long secs = t;

	final StringBuilder b = new StringBuilder();

	if (secs >= 3600) {
	    final int hours = (int) Math.floor(secs / 3600);
	    b.append(hours).append('h');
	    secs %= 3600;
	}
	if (secs >= 60) {
	    final int minutes = (int) Math.floor(secs / 60);
	    b.append(minutes).append('m');
	    secs %= 60;
	}
	if (secs > 0) {
	    b.append(secs).append('s');
	}

	return b.toString();
    }

    public void computeUtilization() {
	this.utilization = (this.totalInstanceTime - this.totalIdleTime)
		/ this.totalInstanceTime
		* 100;
    }

    public double getActualCost() {
	return this.actualCost;
    }

    public double getActualRuntime() {
	return this.actualRuntime;
    }

    public double getAverageRuntime() {

	return this.actualRuntime / this.jobsCompleted;
    }

    public double getAverageWaitingTime() {

	return this.waitingTime / this.jobsCompleted;
    }

    public int getDeadlineBreaches() {
	return this.deadlineBreaches;
    }

    public double getEstimatedCost() {
	return this.estimatedCost;
    }

    public double getEstimatedRunTime() {
	return this.estimatedRunTime;
    }

    public long getFinishTime() {
	return this.finishTime;
    }

    public long getFtOverhead() {
	return this.ftOverhead;
    }

    public int getInstancesReceived() {
	return this.instancesReceived;
    }

    public int getInstancesRequested() {
	return this.instancesRequested;
    }

    public int getInstancesRunning() {
	return this.instancesReceived - this.instancesTerminated;
    }

    public int getInstancesTerminated() {
	return this.instancesTerminated;
    }

    public Map<InstanceType, AtomicInteger> getODInstanceTypesUsed() {
	return this.onDemandInstanceTypesUsed;
    }
    
    public Map<InstanceType, AtomicInteger> getspotInstanceTypesUsed() {
    return this.spotInstanceTypesUsed;
    }
    
    public Map<Integer,Double> getSpotPriceHist() {
        return this.spotPriceHistory;
    }
    
    public ArrayList<BidPojo> getSpotPricePojo() {
        return this.spotPricePojo;
    }

    public int getJobsCompleted() {
	return this.jobsCompleted;
    }

    public int getTotalResourcesFailed() {
	return this.totalInstancesFailed;
    }

    public int getJobsLapsed() {
	return this.jobsLapsed;
    }

    public int getJobsFailed() {
    	return this.jobsFailed;
    }
    
    public int getJobsRunning() {
	return this.jobsSubmitted - this.jobsCompleted;
    }

    public int getJobsSubmitted() {
	return this.jobsSubmitted;
    }

    public long getRedudantProcessing() {
	return this.redudantProcessing;
    }

    public double getTotalIdleTime() {
	return this.totalIdleTime;
    }

    public double getTotalInstanceTime() {
	return this.totalInstanceTime;
    }

    public double getUtilization() {
	return this.utilization;
    }

    public double getWaitingTime() {
	return this.waitingTime;
    }

    public double getWasted() {
	return this.wasted;
    }

    public void incrActualCost(final double add) {
	this.actualCost += add;
    }

    public void incrActualRuntime(final double add) {
	this.actualRuntime += add;
    }

    public void incrDeadlineBreach() {
	this.deadlineBreaches++;
    }

    public void incrEstimatedCost(final double add) {
	this.estimatedCost += add;
    }

    public void incrEstimatedRunTime(final double add) {
	this.estimatedRunTime += add;
    }

    public void incrTotalResourcesFailed() {
	this.totalInstancesFailed++;
    }
    
    public void incrJobsFailed() {
    	this.jobsFailed++;
    }

    public void incrFTOverhead(final long overhead) {
	this.ftOverhead += overhead;
    }

    public void incrInstancesIdleTime(final double idleTime) {
	this.totalIdleTime = this.totalIdleTime + idleTime;
    }

    public void incrInstancesReceived() {
	this.instancesReceived++;

    }

    public void incrInstancesRequested() {
	this.instancesRequested++;
    }

    public void incrInstancesTerminated() {
	this.instancesTerminated++;
    }

    public void incrODInstanceTypeUsed(final InstanceType type) {
	this.onDemandInstanceTypesUsed.get(type).incrementAndGet();
    }
    
    public void incrspotInstanceTypeUsed(final InstanceType type) {
    this.spotInstanceTypesUsed.get(type).incrementAndGet();
    }
    
    public void addSpotPrice(final Double price, Integer id) {
        this.spotPriceHistory.put(id, price);
    }
    
    public void addSpotPojo(final Double price, long lto, double spotPrice) {
    	final BidPojo bidPojo = new BidPojo(lto, price, spotPrice);
    	this.spotPricePojo.add(bidPojo);

    }

    public void incrJobsCompleted() {
	this.jobsCompleted++;
    }

    public void incrJobsLapsed() {
	this.jobsLapsed++;
    }

    public void incrJobsSubmitted() {
	this.jobsSubmitted++;
    }

    public void incrRedudantProcessing(final long overhead) {
	this.redudantProcessing += overhead;
    }

    public void incrTotalInstanceTime(final double runTime) {
	this.totalInstanceTime += runTime;
    }

    public void incrWaitingTime(final long jobWaitTime) {
	this.waitingTime += jobWaitTime;
    }

    public void incrWastedMoney(final double add) {
	this.wasted += add;
    }

    public void setFinishTime(final long clock) {
	this.finishTime = clock;
    }

    public double getTotalExecutiontime() {
		return totalExecutiontime;
	}

	public void setTotalExecutiontime(long totalExecutiontime) {
		this.totalExecutiontime = totalExecutiontime;
	}

	@Override
    public String toString() {
    	
    	System.out.println(" Actual run time " + this.actualRuntime);
	this.estimatedRunTime /= 3600D;
	this.actualRuntime /= 3600D;
	this.totalInstanceTime /= 3600D;
	this.totalIdleTime /= 3600D;
	this.redudantProcessing /= 3600D;
	this.totalExecutiontime /= 3600D;

	final StringBuilder builder = new StringBuilder();
	builder.append("Statistics [estimatedRunTime=").append(this.estimatedRunTime)
	    .append(", estimatedCost=").append(this.estimatedCost).append(", actualCost=")
	    .append(this.actualCost).append(", actualRuntime=").append(this.actualRuntime)
	    .append(", totalExecutionTime=").append(this.totalExecutiontime)
	    .append(", waitingTime=").append(this.waitingTime).append(", jobsSubmitted=")
	    .append(this.jobsSubmitted).append(", jobsCompleted=").append(this.jobsCompleted)
	    .append(", instancesFailed=").append(this.totalInstancesFailed).append(", jobsFailed=").append(this.jobsFailed).append(", jobsLapsed=")
	    .append(this.jobsLapsed).append(", instancesRequested=")
	    .append(this.instancesRequested).append(", instancesReceived=")
	    .append(this.instancesReceived).append(", instancesTerminated=")
	    .append(this.instancesTerminated).append(", totalIdleTime=").append(this.totalIdleTime)
	    .append(", totalInstanceTime=").append(this.totalInstanceTime).append(", utilization=")
	    .append(this.utilization).append(", deadlineBreaches=").append(this.deadlineBreaches)
	    .append(", wasted=").append(this.wasted).append(", onDemInstanceTypesUsed=")
	    .append(this.onDemandInstanceTypesUsed).append(", spotInstanceTypesUsed=")
	    .append(this.spotInstanceTypesUsed).append(", spotPriceBids=")
	    .append(this.spotPriceHistory).append(", finishTime=").append(this.finishTime)
	    .append(", ftOverhead=").append(this.ftOverhead).append(", redudantProcessing=")
	    .append(this.redudantProcessing).append(", TaskReplicas=")
	    .append(this.replicas).append(", ResourceFailuresDuetoFaults=")
	    .append(this.resourceFault).append("]");
	return builder.toString();
    }

	public void flush() {
		this.onDemandInstanceTypesUsed.clear();
		for (final InstanceType instanceType : InstanceType.values()) {
		    this.onDemandInstanceTypesUsed.put(instanceType, new AtomicInteger());
		}
		this.spotInstanceTypesUsed.clear();
		for (final InstanceType instanceType : InstanceType.values()) {
		    this.spotInstanceTypesUsed.put(instanceType, new AtomicInteger());
		}
		this.spotPriceHistory.clear();
		this.spotPricePojo.clear();
		
		
		estimatedRunTime = 0D;
		estimatedCost = 0D;
		actualCost = 0D;
		actualRuntime = 0D;
	    waitingTime = 0D;
	    jobsSubmitted = 0;
	    jobsCompleted = 0;
	    totalInstancesFailed = 0;
	    jobsLapsed =0;
	    jobsFailed =0;
	    instancesRequested = 0;
	    instancesReceived = 0;
	    instancesTerminated = 0;
	    totalIdleTime = 0D;
	    totalInstanceTime = 0D;
	    utilization = 0D;
	    deadlineBreaches = 0;
	    precentDlBreaches =0;
	    wasted = 0D;
	    finishTime = 0L;
	    ftOverhead = 0L;
	    redudantProcessing = 0L;
	    totalExecutiontime = 0D;
	    replicas = 0;
	    resourceFault=0;
			
	}

	public double getPrecentDlBreaches() {
		return precentDlBreaches;
	}

	public void setPrecentDlBreaches(double precentDlBreaches) {
		this.precentDlBreaches = precentDlBreaches;
	}

	public int getReplicas() {
		return replicas;
	}

	public void setReplicas(int replicas) {
		this.replicas = replicas;
	}
	
	public void incrReplicas(){
		this.replicas++;
	}

	public int getResourceFault() {
		return resourceFault;
	}

	public void setResourceFault(int resourceFault) {
		this.resourceFault = resourceFault;
	}
	
	public void incrResourceFault(){
		this.resourceFault++;
	}
	
}