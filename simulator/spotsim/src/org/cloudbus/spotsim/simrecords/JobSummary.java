package org.cloudbus.spotsim.simrecords;

import org.cloudbus.cloudsim.util.workload.Job;
import org.cloudbus.cloudsim.util.workload.Job.JobStatus;
import org.cloudbus.spotsim.enums.InstanceType;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * For simulation statistics only. Adds some fields that are not contained in
 * CloudSim's Job class
 */
@XStreamAlias("job")
public class JobSummary {

    @XStreamAlias("id")
    @XStreamAsAttribute
    private final long jobID;

    @XStreamAlias("stime")
    @XStreamAsAttribute
    private final long submitTime;

    @XStreamAlias("size")
    @XStreamAsAttribute
    private final long jobSize;

    @XStreamAlias("sst")
    @XStreamAsAttribute
    private long scheduledStartTime;

    @XStreamAlias("act")
    @XStreamAsAttribute
    private long actualStartTime;

    @XStreamAlias("aet")
    @XStreamAsAttribute
    private long actualEndTime;

    @XStreamAlias("ddl")
    @XStreamAsAttribute
    private final long deadline;

    @XStreamAlias("bud")
    @XStreamAsAttribute
    private final double budget;

    @XStreamAlias("itype")
    private InstanceType instanceTypeUsed;

    @XStreamAlias("newi")
    @XStreamAsAttribute
    private boolean startedANewInstance;

    @XStreamAlias("ert")
    @XStreamAsAttribute
    private long estimatedRunTime;

    @XStreamAlias("st")
    private JobStatus finalStatus;

    @XStreamAlias("ec")
    @XStreamAsAttribute
    private double estimatedCost;

    @XStreamAlias("ac")
    @XStreamAsAttribute
    private double actualCost;

    @XStreamAlias("iid")
    @XStreamAsAttribute
    private int instanceID;

    public JobSummary(final Job j) {
	this.jobID = j.getId();
	this.submitTime = j.getSubmitTime();
	this.jobSize = j.getLength();
	this.deadline = j.getDeadline();
	this.budget = j.getBudget();
    }

    public double getActualCost() {
	return this.actualCost;
    }

    public long getActualEndTime() {
	return this.actualEndTime;
    }

    public long getActualStartTime() {
	return this.actualStartTime;
    }

    public double getBudget() {
	return this.budget;
    }

    public long getDeadline() {
	return this.deadline;
    }

    public double getEstimatedCost() {
	return this.estimatedCost;
    }

    public long getEstimatedRunTime() {
	return this.estimatedRunTime;
    }

    public JobStatus getFinalStatus() {
	return this.finalStatus;
    }

    public int getInstanceID() {
	return this.instanceID;
    }

    public InstanceType getInstanceTypeUsed() {
	return this.instanceTypeUsed;
    }

    public long getJobID() {
	return this.jobID;
    }

    public long getJobSize() {
	return this.jobSize;
    }

    public long getScheduledStartTime() {
	return this.scheduledStartTime;
    }

    public long getSubmitTime() {
	return this.submitTime;
    }

    public boolean isStartedANewInstance() {
	return this.startedANewInstance;
    }

    public void setActualCost(final double actualCost) {
	this.actualCost = actualCost;
    }

    public void setActualEndTime(final long actualEndTime) {
	this.actualEndTime = actualEndTime;
    }

    public void setActualStartTime(final long actualStartTime) {
	this.actualStartTime = actualStartTime;
    }

    public void setEstimatedCost(final double estimatedCost) {
	this.estimatedCost = estimatedCost;
    }

    public void setEstimatedRunTime(final long estimatedRunTime) {
	this.estimatedRunTime = estimatedRunTime;
    }

    public void setFinalStatus(final JobStatus finalStatus) {
	this.finalStatus = finalStatus;
    }

    public void setInstanceID(final int instanceID) {
	this.instanceID = instanceID;
    }

    public void setInstanceTypeUsed(final InstanceType instanceTypeUsed) {
	this.instanceTypeUsed = instanceTypeUsed;
    }

    public void setScheduledStartTime(final long scheduledStartTime) {
	this.scheduledStartTime = scheduledStartTime;
    }

    public void setStartedANewInstance(final boolean startedANewInstance) {
	this.startedANewInstance = startedANewInstance;
    }

    @Override
    public String toString() {
	final StringBuilder builder = new StringBuilder();
	builder.append(this.jobID).append(",").append(this.submitTime).append(",")
	    .append(this.jobSize).append(",").append(this.scheduledStartTime).append(",")
	    .append(this.actualStartTime).append(",").append(this.actualEndTime).append(",")
	    .append(this.deadline).append(",").append(this.budget).append(",")
	    .append(this.instanceTypeUsed).append(",").append(this.startedANewInstance).append(",")
	    .append(this.estimatedRunTime).append(",").append(this.finalStatus).append(",")
	    .append(this.estimatedCost).append(",").append(this.actualCost);
	return builder.toString();
    }
}
