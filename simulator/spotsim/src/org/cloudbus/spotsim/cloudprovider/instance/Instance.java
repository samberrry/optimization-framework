package org.cloudbus.spotsim.cloudprovider.instance;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.cloudletscheduler.CloudletSchedulerSingleProcessor;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.util.workload.Task;
import org.cloudbus.spotsim.enums.InstanceType;
import org.cloudbus.spotsim.enums.OS;
import org.cloudbus.spotsim.enums.PriceModel;
import org.cloudbus.spotsim.main.config.SimProperties;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * @see DatacenterManager
 */
public class Instance {

    @XStreamOmitField
    private final Vm vm;

    private final InstanceType type;

    private final OS os;

    private final PriceModel pricing;

    private transient InstanceState state;

    private static final AtomicInteger uniqueID = new AtomicInteger();

    private double bidPrice;

    private long accStart = -1;

    private long accEnd = -1;

    private int brokerID;

    private double cost;

    private long latestCkpt;

    private long requestToken;

    private transient InstanceState previousState;

    private final int id;

    private transient Task task = null;

    private transient final DatacenterManager parentDatacenter;

    /* To be used only by the factory */
    Instance(final double bidprice) {
	this.bidPrice = bidprice;
	this.id = Integer.MAX_VALUE;
	this.vm = null;
	this.brokerID = -1;
	this.type = null;
	this.os = null;
	this.pricing = null;
	this.state = null;
	this.parentDatacenter = null;
    }

    /* To be used only by the factory */
    Instance(final InstanceType type, final OS os, final PriceModel pricing, final int cloudId,
	    final double bid, final DatacenterManager parentDatacenter) {
	this.bidPrice = bid;
	this.parentDatacenter = parentDatacenter;
	this.id = nextId();
	this.vm = new Vm(this.id, cloudId, type.getComputePower(), 1, type.getMem(),
	    type.getBandwidth(), type.getStorage(), "Xen", new CloudletSchedulerSingleProcessor());
	this.type = type;
	this.os = os;
	this.pricing = pricing;
	this.state = InstanceState.PENDING;
	this.previousState = InstanceState.PENDING;
    }

    private static int nextId() {
	return uniqueID.getAndIncrement();
    }

    @Override
    public boolean equals(final Object obj) {
	if (this == obj) {
	    return true;
	}
	if (obj == null) {
	    return false;
	}
	if (this.getClass() != obj.getClass()) {
	    return false;
	}
	final Instance other = (Instance) obj;
	if (getId() != other.getId()) {
	    return false;
	}
	return true;
    }

    public long getAccEnd() {
	return this.accEnd;
    }

    public long getAccStart() {
	return this.accStart;
    }

    public double getBidPrice() {
	return this.bidPrice;
    }

    public int getBrokerID() {
	return this.brokerID;
    }

    public double getCost() {
	return this.cost;
    }

    public int getId() {
	return this.id;
    }

    public long getLatestCkpt() {
	return this.latestCkpt;
    }

    public OS getOs() {
	return this.os;
    }

    public DatacenterManager getParentDatacenter() {
	return this.parentDatacenter;
    }

    public InstanceState getPreviousState() {
	return this.previousState;
    }

    public PriceModel getPricing() {
	return this.pricing;
    }

    public long getRequestToken() {
	return this.requestToken;
    }

    public InstanceState getState() {
	return this.state;
    }

    public Task getTask() {
	return this.task;
    }

    public InstanceType getType() {
	return this.type;
    }

    public long getUsedTime() {
	return getAccEnd() - getAccStart();
    }

    public Vm getVm() {
	return this.vm;
    }

    @Override
    public int hashCode() {
	return 31 * getId();
    }

    public boolean isOnDemand() {
	return this.pricing == PriceModel.ON_DEMAND;
    }

    public boolean isReserved() {
	return this.pricing == PriceModel.RESERVED;
    }

    public boolean isSpot() {
	return this.pricing == PriceModel.SPOT;
    }

    public long runTime() {
	if (getAccEnd() == -1) {
	    return CloudSim.clock() - getAccStart();
	}
	return getAccEnd() - getAccStart();
    }

    public double runTimeInHours() {
	return (double) runTime() / 3600;
    }

    public void setAccEnd(final long accEnd) {
	this.accEnd = accEnd;
    }

    public void setAccStart(final long accStart) {
	this.accStart = accStart;
    }

    public void setBrokerID(final int brokerID) {
	this.brokerID = brokerID;
    }

    public void setCost(final double cost) {
	this.cost = cost;
    }

    public void setLatestCkpt(final long latestCkpt) {
	this.latestCkpt = latestCkpt;
    }

    public void setRequestToken(final long requestToken) {
	this.requestToken = requestToken;
    }

    public void setState(final InstanceState newState) {
	if (!this.state.transitionAllowed(newState)) {
	    throw new IllegalStateException("Instance "
		    + getId()
		    + ". Invalid instance state transition ("
		    + this.state
		    + " to "
		    + newState
		    + "). Allowed transitions: "
		    + (this.state.getAllowedTransitions() == null ? "none"
			    : this.state.getAllowedTransitions()));
	}

	if (newState.isTerminated() || newState == InstanceState.IDLE) {
	    setTask(null);
	}

	if (Log.logger.isLoggable(Level.FINE)) {
	    Log.logger.fine(Log.clock()
		    + " Instance "
		    + getId()
		    + ". Changing state from "
		    + this.state.toString()
		    + " to "
		    + newState.toString());
	}
	this.previousState = this.state;
	this.state = newState;
    }

    public void setTask(final Task task) {
	this.task = task;
    }

    public void startAccounting() {
	if (getAccStart() != -1D) {
	    throw new RuntimeException("Cannot start instance accounting: Already started at "
		    + getAccStart());
	}
	final long c = CloudSim.clock();
	setAccStart(c);
    }

    public void stopAccounting() {
	if (getAccEnd() != -1D) {
	    throw new RuntimeException("["+CloudSim.clock()+"]"+"Cannot stop instance accounting of instance "
		    + getId()
		    + " Already stopped at "
		    + getAccEnd());
	}
	final long clock = CloudSim.clock();
	setAccEnd(clock);
    }

    @Override
    public String toString() {
	final StringBuilder builder = new StringBuilder();
	builder.append("Instance [pricing=").append(this.pricing).append(", type=")
	    .append(this.type).append(", bidPrice=").append(this.bidPrice).append(", getId()=")
	    .append(getId()).append("]");
	return builder.toString();
    }

    protected void setBidPrice(final double bidPrice) {
	this.bidPrice = bidPrice;
    }

    public int chargeablePeriods() {
	double runTimeFract = runTime() / SimProperties.PRICING_CHARGEABLE_PERIOD.asInt();
	int fullPeriods = (int) runTimeFract;
	double partialPeriod = runTimeFract - fullPeriods;
	return getState() == InstanceState.OUT_OF_BID ? fullPeriods
		: partialPeriod > 0 ? fullPeriods + 1 : fullPeriods;
    }
}