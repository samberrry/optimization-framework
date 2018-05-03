package org.cloudbus.spotsim.broker;

import java.util.Comparator;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.util.workload.Task;
import org.cloudbus.spotsim.broker.resources.Resource;
import org.cloudbus.spotsim.enums.AZ;
import org.cloudbus.spotsim.enums.InstanceType;
import org.cloudbus.spotsim.enums.OS;
import org.cloudbus.spotsim.enums.PriceModel;
import org.cloudbus.spotsim.enums.Region;
import org.cloudbus.spotsim.pricing.db.PriceDB;

public class SchedulingDecision {

    private final Task task;

    private final Region region;

    private final AZ az;

    private final InstanceType instanceType;

    private final OS os;

    private final Resource resource;

    private boolean startNewInstance;

    private final long startTime;

    private final long estimatedRuntime;

    private final double cost;

    private final double bidPrice;

    private final boolean postponed;

    private final long maxWaitTime;
    
    private PriceModel priceModel;

    private static final Comparator<SchedulingDecision> timeComparator = new Comparator<SchedulingDecision>() {

	@Override
	public int compare(final SchedulingDecision o1, final SchedulingDecision o2) {

	    return new Long(o1.getStartTime()).compareTo(o2.getStartTime());
	}
    };

    public SchedulingDecision(final Task task, final long maxWaitTime) {
	this(task, true, null, null, null, null, null, false, 0, 0, 0, 0, maxWaitTime);
    }

    public SchedulingDecision(final Task task, final InstanceType instanceType, OS os, Region region) {
	this(task, false, Region.getDefault(), AZ.ANY, instanceType, os, null, true, CloudSim
	    .clock(), 0, 0, PriceDB.getOnDemandPrice(region, instanceType, os), 0);
    }

    public SchedulingDecision(final Task task, final Resource resource, long estimatedRuntime) {
	this(task, false, resource.getRegion(), AZ.ANY, resource.getType(), resource.getOs(),
	    resource, false, resource.getTimeThatItWillBecomeIdle(), estimatedRuntime, 0, 0, 0);
    }

    public SchedulingDecision(final Task task, boolean postpone, Region region, final AZ az,
	    final InstanceType chosenInstanceType, OS os, final Resource resource,
	    final boolean startNewInstance, final long startTime, final long estimatedTime,
	    final double cost, final double bidPrice, final long maxWaitTime) {
	super();
	this.task = task;
	this.instanceType = chosenInstanceType;
	this.resource = resource;
	this.startNewInstance = startNewInstance;
	this.startTime = startTime;
	this.estimatedRuntime = estimatedTime;
	this.cost = cost;
	this.bidPrice = bidPrice;
	this.az = az;
	this.region = region;
	this.os = os;
	this.postponed = postpone;
	this.maxWaitTime = maxWaitTime;
	checkSanity();
	this.priceModel = PriceModel.SPOT;
    }
    
    public SchedulingDecision(final Task task, final InstanceType instanceType, OS os, Region region, PriceModel priceModel) {
    	this(task, false, Region.getDefault(), AZ.ANY, instanceType, os, null, true, CloudSim
    	    .clock(), 0, PriceDB.getOnDemandPrice(region, instanceType, os), PriceDB.getOnDemandPrice(region, instanceType, os), 0, priceModel);
    }
    
    public SchedulingDecision(final Task task, final InstanceType instanceType, final long estimatedTime ,OS os, Region region, PriceModel priceModel) {
    	this(task, false, Region.getDefault(), AZ.ANY, instanceType, os, null, true, CloudSim
    	    .clock(), estimatedTime, PriceDB.getOnDemandPrice(region, instanceType, os), PriceDB.getOnDemandPrice(region, instanceType, os), 0, priceModel);
    }
    
    public SchedulingDecision(final Task task, boolean postpone, Region region, final AZ az,
    	    final InstanceType chosenInstanceType, OS os, final Resource resource,
    	    final boolean startNewInstance, final long startTime, final long estimatedTime,
    	    final double cost, final double bidPrice, final long maxWaitTime, PriceModel priceModel) {
    	super();
    	this.task = task;
    	this.instanceType = chosenInstanceType;
    	this.resource = resource;
    	this.startNewInstance = startNewInstance;
    	this.startTime = startTime;
    	this.estimatedRuntime = estimatedTime;
    	this.cost = cost;
    	this.bidPrice = bidPrice;
    	this.az = az;
    	this.region = region;
    	this.os = os;
    	this.postponed = postpone;
    	this.maxWaitTime = maxWaitTime;
    	checkSanity();
    	this.priceModel = priceModel;
        }

    public Region getRegion() {
	return this.region;
    }

    public AZ getAz() {
	return this.az;
    }

    public OS getOs() {
	return this.os;
    }

    public static Comparator<SchedulingDecision> getTimeComparator() {
	return timeComparator;
    }

    public static Comparator<SchedulingDecision> timeComparator() {
	return timeComparator;
    }

    /**
     * Check potential inconsistencies (for debugging purposes)
     */
    public void checkSanity() {
	final String insanityDetected = "Insane scheduling decision: " + this;
	if (this.postponed) {
	    if (this.maxWaitTime <= 0) {
		throw new Error(insanityDetected
			+ " When postponed, maxWaitTime must be greater than 0");
	    }
	} else {
	    if (this.startTime < 0) {
		throw new Error(insanityDetected + " Start time is negative: " + this.startTime);
	    }
	    if (this.startTime < CloudSim.clock()) {
		throw new Error(insanityDetected
			+ " Start time is in the past: "
			+ this.startTime
			+ " (now: "
			+ CloudSim.clock()
			+ ")");
	    }

	    if (this.resource == null && this.instanceType == null) {
		throw new Error(insanityDetected + "No instance was chosen");
	    }

	    if (this.startNewInstance && this.cost == 0D) {
		throw new Error(insanityDetected
			+ " When starting a new instance, cost cannot be zero");
	    }

	    if (this.estimatedRuntime <= 0) {
		throw new Error(insanityDetected + " Estimated runtime must be greater than 0");
	    }
	}
    }

    public double getBidPrice() {
	return this.bidPrice;
    }

    public InstanceType getInstanceType() {
	return this.instanceType;
    }

    public double getCost() {
	return this.cost;
    }

    public long getEstimatedRuntime() {
	return this.estimatedRuntime;
    }

    public long getMaxWaitTime() {
	return this.maxWaitTime;
    }

    public Resource getResource() {
	return this.resource;
    }

    public long getStartTime() {
	return this.startTime;
    }

    public Task getTask() {
	return this.task;
    }

    public boolean isStartNewInstance() {
	return this.startNewInstance;
    }

    public boolean postponed() {
	return this.postponed;
    }

    public void setStartNewInstance(final boolean startNewInstance) {
	this.startNewInstance = startNewInstance;
    }

    @Override
    public String toString() {
	final StringBuilder builder = new StringBuilder();
	builder.append("SchedulingDecision [task=").append(this.task).append(", bidPrice=")
	    .append(this.bidPrice).append(", chosenInstanceType=").append(this.instanceType)
	    .append(", cost=").append(this.cost).append(", estimatedRunTime=")
	    .append(this.estimatedRuntime).append(", startNewInstance=")
	    .append(this.startNewInstance).append(", startTime=").append(this.startTime)
	    .append(", instance status=")
	    .append(this.resource == null ? "null" : this.resource.getState()).append("]");
	return builder.toString();
    }

	public PriceModel getPriceModel() {
		return priceModel;
	}
}
