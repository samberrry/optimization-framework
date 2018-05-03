package org.cloudbus.cloudsim.util.workload;

import static org.cloudbus.cloudsim.util.workload.TaskState.*;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.spotsim.broker.resources.Resource;

public class Task {

    private long actualEndTime;

    private long actualStartTime = -1;

    private Cloudlet cloudlet = null;

    private long estimatedRunTime = -1;

    private final String id;

    private final Job job;

    private long maxWaitTime;

    private final int replicaId;

    private Resource resource;

    private TaskState state;

    private int userID;

    public Task(final Job job, final int replicaId) {
		super();
		this.job = job;
		this.replicaId = replicaId;
		this.id = Long.toString(job.getId()) + '-' + replicaId;
		this.state = READY;
		reset();
    }
    
    public Task(Integer id) {
		super();
		this.id = id.toString();
		this.job = null;
		this.replicaId = -1;
		this.state = READY;
		reset();
    }

    public void checkSanity() {

	if (getState() == RUNNING) {
	    if (this.resource == null) {
		throw new IllegalStateException(
		    "Task is insane. Resource cannot be null at this point");
	    }
	    if (this.actualStartTime == -1) {
		throw new IllegalStateException(
		    "Task is insane. Actual start time must be set before running");
	    }

	    if (this.estimatedRunTime == -1) {
		throw new IllegalStateException(
		    "Task is insane. Actual start time must be set before running");
	    }
	}
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
	final Task other = (Task) obj;
	if (!this.id.equals(other.id)) {
	    return false;
	}
	return true;
    }

    public long getActualEndTime() {
	return this.actualEndTime;
    }

    public long getActualStartTime() {
	return this.actualStartTime;
    }

    public Cloudlet getCloudlet() {
	return this.cloudlet;
    }

    public long getCompletedSoFar() {
	return this.cloudlet == null ? 0 : this.cloudlet.getTotalProgress();
    }

    public long getEstimatedRunTime() {
	return this.estimatedRunTime;
    }

    public String getId() {
	return this.id;
    }

    public Job getJob() {
	return this.job;
    }

    public long getLostComputation() {
	return this.cloudlet == null ? 0 : this.cloudlet.getLostComputation();
    }

    public long getMaxWaitTime() {
	return this.maxWaitTime;
    }

    public long getRemainingTime() {
	return Math.max(0L, this.estimatedRunTime - getTimeElapsedSinceStart());
    }

    public int getReplicaId() {
	return this.replicaId;
    }

    public Resource getResource() {
	return this.resource;
    }

    public TaskState getState() {
	return this.state;
    }

    public long getExpectedCompletionTime() {
	return this.actualStartTime + this.estimatedRunTime;
    }

    public long getTimeElapsedSinceStart() {
	return this.actualStartTime == -1 ? 0 : CloudSim.clock() - this.actualStartTime;
    }

    public int getUserID() {
	return this.userID;
    }

    @Override
    public int hashCode() {
	return this.id.hashCode();
    }

    public void reset() {
	this.actualStartTime = -1;
	this.actualEndTime = -1;
	this.estimatedRunTime = -1;
    }

    public void scheduled(final long est, final long mwt) {
	setState(SCHEDULED);
	setEstimatedRunTime(est);
	setMaxWaitTime(mwt);
    }

    public void setActualEndTime(final long actualEndTime) {
	this.actualEndTime = actualEndTime;
    }

    public void setActualStartTime(final long actualStartTime) {
	this.actualStartTime = actualStartTime;
    }

    public void setCloudlet(final Cloudlet cloudlet) {
	this.cloudlet = cloudlet;
    }

    public void setEstimatedRunTime(final long estimatedRuntime) {
	this.estimatedRunTime = estimatedRuntime;
    }

    public void setMaxWaitTime(final long maxWaitTime) {
	this.maxWaitTime = maxWaitTime;
    }

    public void setResource(final Resource resource) {
	this.resource = resource;
    }

    public void setState(final TaskState status) {
	if (!this.state.isTransitionAllowed(status)) {
	    throw new IllegalStateException("State "
		    + this.state
		    + " of task "
		    + getId()
		    + " cannot be suceeded by "
		    + status
		    + ". Allowed transitions: "
		    + (this.state.getAllowedTransitions() == null ? "none"
			    : this.state.getAllowedTransitions()));
	}
	this.state = status;
    }

    public void setUserID(final int userID) {
	this.userID = userID;
    }

    public long timeTaken() {
	return this.actualEndTime - this.actualStartTime;
    }

    @Override
    public String toString() {
	return "" + this.id + '=' + this.state;
    }
}
