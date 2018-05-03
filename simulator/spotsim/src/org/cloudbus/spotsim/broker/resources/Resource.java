package org.cloudbus.spotsim.broker.resources;

import static org.cloudbus.spotsim.broker.resources.ResourceState.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.util.workload.Task;
import org.cloudbus.spotsim.enums.AZ;
import org.cloudbus.spotsim.enums.InstanceType;
import org.cloudbus.spotsim.enums.OS;
import org.cloudbus.spotsim.enums.PriceModel;
import org.cloudbus.spotsim.enums.Region;
import org.cloudbus.spotsim.main.config.SimProperties;

/**
 * @author williamv
 * 
 * @see ResourceTest
 * 
 */
public class Resource {

    public final class IdlePeriod {

	private final long start;

	private final long end;

	IdlePeriod(final long start, final long end) {
	    this.start = start;
	    this.end = end;
	}

	@Override
	public String toString() {
	    final StringBuilder builder = new StringBuilder();
	    builder.append("[").append(this.start).append(':').append(this.end).append("]");
	    return builder.toString();
	}

	long getEnd() {
	    return this.end;
	}

	long getStart() {
	    return this.start;
	}
    }

    public static final int MAX_TASKS_PER_RESOURCE = 1;

    private final int id;

    private final Region region;

    private final AZ az;

    private final InstanceType type;

    private final OS os;

    private ResourceState state;

    private final PriceModel priceModel;

    private transient final StringBuilder stateHistory;

    private transient final Deque<Task> scheduledTasks;

    private transient final List<Task> runningTasks;

    private transient long latestIdleStart = 0L;

    private long timeRequested = -1;

    private long timeReceived = -1;

    private long timeTerminated = -1;

    private final List<IdlePeriod> idlePeriods = new ArrayList<IdlePeriod>();

    private static int nextID = 0;

    private double bid;

    private long token;

    private double cost;

    private int tasksRun = 0;

    public Resource(final InstanceType type, final OS os, final PriceModel priceModel,
	    final double bid, AZ az, Region region) {
	this.type = type;
	this.os = os;
	this.az = az;
	this.region = region;
	setBid(bid);
	this.priceModel = priceModel;
	this.id = getNextID();
	this.scheduledTasks = new LinkedList<Task>();
	this.runningTasks = new ArrayList<Task>(MAX_TASKS_PER_RESOURCE);
	this.state = PENDING;
	this.stateHistory = new StringBuilder(PENDING.toString());
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
	final Resource other = (Resource) obj;
	if (this.id != other.id) {
	    return false;
	}
	return true;
    }

    public Collection<Task> getAndRemoveScheduledTasks() {
	final ArrayList<Task> ret = new ArrayList<Task>(this.scheduledTasks);
	this.scheduledTasks.clear();
	return ret;
    }

    public AZ getAz() {
	return this.az;
    }

    public double getBid() {
	return this.bid;
    }

    public int getId() {
	return this.id;
    }

    public List<IdlePeriod> getIdlePeriods() {
	return this.idlePeriods;
    }

    public long getLatestIdleStart() {
	return this.latestIdleStart;
    }

    public long getNextFullHour() {
	return nextFullHour(CloudSim.clock());
    }

    public long getNextFullHourAfterBecomingIdle() {
	return nextFullHour(getTimeThatItWillBecomeIdle());
    }

    public int getNumberOfRunningTasks() {
	return this.runningTasks.size();
    }

    public int getNumberOfScheduledTasks() {
	return this.scheduledTasks.size();
    }

    public OS getOs() {
	return this.os;
    }

    public PriceModel getPriceModel() {
	return this.priceModel;
    }

    public List<Task> getRunningTasks() {
	return this.runningTasks;
    }

    public Collection<Task> getScheduledTasks() {
	return new ArrayList<Task>(this.scheduledTasks);
    }

    public long getSecondsToNextFullHour() {
	return getNextFullHour() - CloudSim.clock();
    }

    public long getSecondsToNextHourAfterBecomingIdle() {
	final long timeThatItWillBecomeIdle = getTimeThatItWillBecomeIdle();
	return nextFullHour(timeThatItWillBecomeIdle) - timeThatItWillBecomeIdle;
    }

    public ResourceState getState() {
	return this.state;
    }

    public long getTimeReceived() {
	return this.timeReceived;
    }

    public long getTimeTerminated() {
	return this.timeTerminated;
    }

    public final long getTimeThatItWillBecomeIdle() {

	final long clock = CloudSim.clock();

	if (this.state == IDLE) {
	    return clock;
	}

	long ret = clock;

	if (getState() == PENDING) {
	    ret = Math.max(clock, this.timeRequested + SimProperties.DC_VM_INIT_TIME.asInt());
	} else if (hasRunningTasks()) {
	    ret = Math.max(clock, this.runningTasks.get(0).getExpectedCompletionTime());
	}

	if (hasScheduledTasks()) {
	    for (final Task task : this.scheduledTasks) {
		ret += task.getEstimatedRunTime();
	    }
	}
	return ret;
    }

    public long getTimeToIdle() {
	if (getState() == IDLE) {
	    return 0;
	}
	return getTimeThatItWillBecomeIdle() - CloudSim.clock();
    }

    public long getToken() {
	return this.token;
    }

    public InstanceType getType() {
	return this.type;
    }

    @Override
    public int hashCode() {
	return this.id;
    }

    public boolean hasRunningTasks() {
	return !this.runningTasks.isEmpty();
    }

    public boolean hasScheduledTasks() {
	return !this.scheduledTasks.isEmpty();
    }

    public boolean hasTasks() {
	return hasScheduledTasks() || hasRunningTasks();
    }

    public long idleTime() {
	long total = 0L;
	for (final IdlePeriod i : this.idlePeriods) {
	    total += i.getEnd() - i.getStart();
	}
	return total;
    }

    public double idleTimeInHours() {
	return idleTime() / 3600D;
    }

    public boolean isFull() {
	return this.runningTasks.size() >= MAX_TASKS_PER_RESOURCE;
    }

    public Task pollNextScheduledTask() {
	return this.scheduledTasks.poll();
    }

    public void received() {
	this.timeReceived = CloudSim.clock();
	setLatestIdleStart(CloudSim.clock());
    }

    public void removeScheduledTask(final Task task) {
	this.scheduledTasks.remove(task);
    }

    public Resource requested(final long token1, final long startDelay) {
	this.token = token1;
	this.timeRequested = CloudSim.clock() + startDelay;
	return this;
    }

    public void runTask(final Task task) {
	if (isFull()) {
	    throw new IllegalStateException("Can only run "
		    + MAX_TASKS_PER_RESOURCE
		    + " task at a time at one resource"
		    + ". State: "
		    + getState()
		    + ". History: "
		    + this.stateHistory
		    + ". Running tasks: "
		    + this.runningTasks);
	}
	if (this.timeReceived < 0) {
	    throw new IllegalStateException("Cannot run task on resource "
		    + getId()
		    + ", not yet received");
	}
	this.tasksRun++;
	this.runningTasks.add(task);
    }

    public long runTime() {
	if (getTimeTerminated() == -1) {
	    return CloudSim.clock() - getTimeReceived();
	}
	return getTimeTerminated() - getTimeReceived();
    }

    public double runTimeInHours() {
	return (double) runTime() / 3600;
    }

    public Resource scheduleTask(final Task task) {
	if (task.getEstimatedRunTime() <= 0) {
	    throw new IllegalArgumentException("Task "
		    + task.getId()
		    + " does not have an estimated runtime");
	}
	this.scheduledTasks.add(task);
	return this;
    }

    public void setBid(final double bid) {
	this.bid = bid;
    }

    public void setLatestIdleStart(final long latestIdleStart) {
	this.latestIdleStart = latestIdleStart;
    }

    public void setTimeReceived(final long timeReceived) {
	this.timeReceived = timeReceived;
    }

    public void setTimeTerminated(final long timeTerminated) {
	this.timeTerminated = timeTerminated;
    }

    public Resource setToken(final long token) {
	this.token = token;
	return this;
    }

    public void removeRunningTask(final Task task) {
	getRunningTasks().remove(task);
    }

    public void terminated(double finalCost) {
	this.cost = finalCost;
	this.timeTerminated = CloudSim.clock();
	addIdlePeriod(this.latestIdleStart, CloudSim.clock());
    }

    public double getUtilization() {
	return (double) (runTime() - idleTime()) / runTime() * 100;
    }

    private void addIdlePeriod(final long start, final long end) {

	if (end - start > 0) {
	    this.idlePeriods.add(new IdlePeriod(start, end));
	}
    }

    private int getNextID() {
	return nextID++;
    }

    private long nextFullHour(final long timeRef) {

	if (this.timeRequested < 0) {
	    throw new IllegalStateException(
		"Cannot compute next full hour as resource hasn't been requested");
	}

	final long start = this.timeReceived > 0 ? this.timeReceived : this.timeRequested
		+ SimProperties.DC_VM_INIT_TIME.asInt();
	return start + Math.max(1, (long) Math.ceil((timeRef - start) / SimProperties.PRICING_CHARGEABLE_PERIOD.asDouble())) * SimProperties.PRICING_CHARGEABLE_PERIOD.asLong();
    }

    void setState(final ResourceState newState) {
	if (!this.state.transitionAllowed(newState)) {
	    throw new IllegalStateException("Resource state "
		    + this.state
		    + " cannot be suceeded by "
		    + newState
		    + ". Allowed transitions: "
		    + (this.state.getAllowedTransitions() == null ? "none"
			    : this.state.getAllowedTransitions())
		    + ". Transition history: "
		    + this.stateHistory.toString());
	}
	if (newState == IDLE) {
	    setLatestIdleStart(CloudSim.clock());
	} else if (this.state == IDLE && newState == ACTIVE) {
	    addIdlePeriod(this.latestIdleStart, CloudSim.clock());
	}

	this.state = newState;
	this.stateHistory.append('-').append(this.state);
    }

    public double getCost() {
	return this.cost;
    }

    public void setCost(double cost) {
	this.cost = cost;
    }

    public int getTasksRun() {
	return this.tasksRun;
    }

    public Region getRegion() {
	return this.region;
    }

	public static void setNextID(int nextID) {
		Resource.nextID = nextID;
	}
}
