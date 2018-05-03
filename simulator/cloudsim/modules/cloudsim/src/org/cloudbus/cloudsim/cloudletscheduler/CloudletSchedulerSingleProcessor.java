package org.cloudbus.cloudsim.cloudletscheduler;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletStatus;
import org.cloudbus.cloudsim.ResCloudlet;
import org.cloudbus.cloudsim.core.CloudSim;

public class CloudletSchedulerSingleProcessor implements CloudletScheduler {

    private ResCloudlet rcl;
    private int capacity;

    private long previousTime;

    @Override
    public long updateVmProcessing() {

	long t = CloudSim.clock();

	if (this.rcl == null) {
	    setPreviousTime(t);
	    return 0;
	}

	long timeSpan = t - getPreviousTime(); // time since last
	if (timeSpan > 0) {

	    if (this.rcl.getCloudletStatus() == CloudletStatus.INEXEC) {
		long workDone = computeProgress(timeSpan);
		this.rcl.updateProgress(workDone);
		if (this.rcl.getRemainingCloudletLength() <= 0) {// finished
		    cloudletFinish(this.rcl);
		    setPreviousTime(t);
		    return 0;
		}
	    }
	}

	setPreviousTime(t);
	return estimateFinishTime();
    }

    private long getPreviousTime() {
	return this.previousTime;
    }

    private void setPreviousTime(long t) {
	this.previousTime = t;
    }

    @Override
    public long cloudletSubmit(Cloudlet gl) {
	if (this.rcl != null) {
	    throw new IllegalStateException("There is already a cloudlet running on this VM");
	}
	this.rcl = new ResCloudlet(gl);
	this.rcl.setStatus(CloudletStatus.INEXEC);
	return estimatedRunTime();
    }

    @Override
    public boolean cloudletPause(int clId) {
	if (this.rcl != null) {
	    this.rcl.setStatus(CloudletStatus.PAUSED);
	    return true;
	}
	return false;
    }

    @Override
    public long cloudletResume(int clId) {
	if (this.rcl != null) {
	    setPreviousTime(CloudSim.clock());
	    this.rcl.setStatus(CloudletStatus.INEXEC);
	    this.rcl.commitProgress();
	    return estimatedRunTime();
	}
	return 0;
    }

    private long estimateFinishTime() {
	return CloudSim.clock() + estimatedRunTime();
    }

    public long estimatedRunTime() {
	if (this.capacity == 1) {
	    return (long) this.rcl.getRemainingCloudletLength();
	}
	return (long) (this.rcl.getRemainingCloudletLength() / this.capacity);
    }

    @Override
    public CloudletStatus getCloudletStatus(int clId) {
	return this.rcl == null ? CloudletStatus.UNKNOWN : this.rcl.getCloudletStatus();
    }

    @Override
    public int runningCloudlets() {
	return this.rcl == null ? 0 : 1;
    }

    @Override
    public Cloudlet migrateCloudlet() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public double getTotalUtilizationOfCpu(long time) {
	return 1;
    }

    @Override
    public List<Double> getCurrentRequestedMips() {
	return null;
    }

    @Override
    public Cloudlet cloudletCancel(int cloudletId) {

	if (this.rcl != null) {
	    this.rcl.setStatus(CloudletStatus.CANCELED);
	    final Cloudlet cloudlet = this.rcl.getCloudlet();
	    long lostComputation = computeProgress(CloudSim.clock()
		    - this.rcl.getLatestCheckpoint());
	    cloudlet.setLostComputation(lostComputation);
	    this.rcl = null;
	    return cloudlet;
	}
	return null;
    }

    @Override
    public Cloudlet cloudletSuspend(int cloudletId) {
	if (this.rcl != null) {
	    if (this.rcl.getRemainingCloudletLength() == 0.0) {
		cloudletFinish(this.rcl);
	    } else {
		this.rcl.setStatus(CloudletStatus.SUSPENDED);
		this.rcl.finalizeCloudlet();
		this.rcl.getCloudlet().setLostComputation(0);
		Cloudlet cloudlet = this.rcl.getCloudlet();
		this.rcl = null;
		return cloudlet;
	    }
	}
	return null;
    }

    private void cloudletFinish(ResCloudlet finishedRcl) {
	finishedRcl.finalizeCloudlet();
	finishedRcl.setStatus(CloudletStatus.SUCCESS);
    }

    private long computeProgress(long timeSpan) {
	if (timeSpan <= 0) {
	    return 0;
	}
	return Math.max(1, this.capacity * timeSpan);
    }

    @Override
    public boolean areThereFinishedCloudlets() {
	return this.rcl == null ? false : this.rcl.getCloudletStatus() == CloudletStatus.SUCCESS;
    }

    private void updateCurrentCapacity(List<Double> mipsShare) {
	this.capacity = 0;
	for (double mips : mipsShare) {
	    if (mips > 0) {
		this.capacity += mips;
	    }
	}
    }

    @Override
    public void pauseAllCloudlets() {
	cloudletPause(0);
    }

    @Override
    public void resumeAllCloudlets() {
	cloudletResume(0);
    }

    @Override
    public void cancelAllCloudlets() {
	cloudletCancel(0);
    }

    @Override
    public List<Cloudlet> getAndRemoveFinishedCloutlets() {
	ArrayList<Cloudlet> ret = new ArrayList<Cloudlet>(1);
	ret.add(this.rcl.getCloudlet());
	this.rcl = null;
	return ret;
    }

    public int freeCPUs() {
	return this.rcl == null ? 1 : 0;
    }

    @Override
    public void mipsChanged(List<Double> allocatedMipsForVm) {
	updateVmProcessing();
	updateCurrentCapacity(allocatedMipsForVm);
    }
}
