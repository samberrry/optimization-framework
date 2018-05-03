/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2010, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.cloudletscheduler;

import static org.cloudbus.cloudsim.CloudletStatus.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletStatus;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.ResCloudlet;
import org.cloudbus.cloudsim.core.CloudSim;

/**
 * CloudletSchedulerSpaceShared implements a policy of scheduling performed by a
 * virtual machine. It consider that there will be only one cloudlet per VM.
 * Other cloudlets will be in a waiting list. We consider that file transfer
 * from cloudlets waiting happens before cloudlet execution. I.e., even though
 * cloudlets must wait for CPU, data transfer happens as soon as cloudlets are
 * submitted.
 * 
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 1.0
 */
public class CloudletSchedulerSpaceShared extends AbstractCloudletScheduler implements
	CloudletScheduler {

    protected static final long MIN_UPDATE_INTERVAL = 5;

    /** The used PEs. */
    protected int usedPes;

    private long capacity = 0;

    private int cpus = 0;

    /**
     * Creates a new CloudletSchedulerSpaceShared object. This method must be
     * invoked before starting the actual simulation.
     * 
     * @pre $none
     * @post $none
     */
    public CloudletSchedulerSpaceShared() {
	super();
	this.usedPes = 0;
    }

    /**
     * Updates the processing of cloudlets running under management of this
     * scheduler.
     * 
     * @param t
     *        current simulation time
     * 
     * @return time predicted completion time of the earliest finishing
     *         cloudlet, or 0 if there is no next events
     * 
     * @pre currentTime >= 0
     * @post $none
     */
    @Override
    public long updateVmProcessing() {

	long t = CloudSim.clock();
	long timeSpan = t - getPreviousTime(); // time since last
	if (timeSpan == 0) {
	    return 0;
	}
	int finished = 0;
	for (ResCloudlet rcl : getRunningCloudlets()) { // each machine
	    double workDone = computeProgress(timeSpan, rcl.getPesNumber());
	    rcl.updateProgress((long) workDone);
	    if (rcl.getRemainingCloudletLength() == 0) {// finished anyway,
		cloudletFinish(rcl);
		finished++;
	    }
	}

	if (schedulerIsEmpty()) {
	    setPreviousTime(t);
	    return 0;
	}

	// for each finished cloudlet, add a new one from the waiting list
	if (!this.waitingList.isEmpty()) {
	    for (int i = 0; i < finished; i++) {
		for (Iterator<Entry<Integer, ResCloudlet>> it = this.waitingList.entrySet()
		    .iterator(); it.hasNext();) {
		    ResCloudlet rcl = it.next().getValue();
		    if (freeCPUs() >= rcl.getPesNumber()) {
			rcl.setStatus(INEXEC);
			for (int k = 0; k < rcl.getPesNumber(); k++) {
			    rcl.setMachineAndPeId(0, i);
			}
			this.execList.put(rcl.getCloudletId(), rcl);
			this.usedPes += rcl.getPesNumber();
			it.remove();
			break;
		    }
		}
	    }
	}

	// estimate finish time of cloudlets in the execution queue
	long nextEvent = Long.MAX_VALUE;
	for (ResCloudlet rcl : this.execList.values()) {
	    double remainingLength = rcl.getRemainingCloudletLength();
	    long remainingTime = (long) Math.ceil(remainingLength
		    / (this.capacity * rcl.getPesNumber()));
	    long estimatedFinishTime = t + remainingTime;

	    if (estimatedFinishTime < nextEvent) {
		nextEvent = estimatedFinishTime;
	    }
	}

	setPreviousTime(t);
	return nextEvent;
    }

    public int freeCPUs() {
	return this.cpus - this.usedPes;
    }

    public void updateCurrentCapacity(List<Double> mipsShare) {

	setCurrentMipsShare(mipsShare);

	this.capacity = 0;
	this.cpus = 0;
	for (double mips : getCurrentMipsShare()) { // count the CPUs available
	    if (mips > 0) {
		this.capacity += mips;
		this.cpus++;
	    }
	}
    }

    private long computeProgress(long timeSpan, final int pesNumber) {
	if (timeSpan <= 0) {
	    return 0;
	}
	long workDone = (long) Math.ceil(this.capacity / this.cpus * timeSpan * pesNumber);
	if (workDone < 1) {
	    workDone = 1;
	}
	return workDone;
    }

    /**
     * Pauses execution of a cloudlet.
     * 
     * @param cloudletId
     *        ID of the cloudlet being paused
     * 
     * @return $true if cloudlet paused, $false otherwise
     * 
     * @pre $none
     * @post $none
     */
    @Override
    public boolean cloudletPause(int cloudletId) {

	ResCloudlet rcl = null;

	if (this.waitingList.containsKey(cloudletId)) {
	    rcl = this.waitingList.remove(cloudletId);
	} else if (this.execList.containsKey(cloudletId)) {
	    rcl = this.execList.remove(cloudletId);
	} else {
	    return false;
	}

	Log.logger.info(Log.clock()
		+ "Pausing cloudlet "
		+ cloudletId
		+ " on VM "
		+ rcl.getCloudlet().getVmId()
		+ ", done so far: "
		+ rcl.getProcessingDoneSoFar());

	if (rcl.getCloudletStatus() == INEXEC) {
	    this.usedPes -= rcl.getPesNumber();
	}
	rcl.setStatus(PAUSED);
	this.pausedList.put(rcl.getCloudletId(), rcl);

	return true;
    }

    /**
     * Processes a finished cloudlet.
     * 
     * @param rcl
     *        finished cloudlet
     * 
     * @pre rgl != $null
     * @post $none
     */
    @Override
    public void cloudletFinish(ResCloudlet rcl) {
	super.cloudletFinish(rcl);
	this.usedPes -= rcl.getPesNumber();
    }

    /**
     * Resumes execution of a paused cloudlet.
     * 
     * @param cloudletId
     *        ID of the cloudlet being resumed
     * 
     * @return $true if the cloudlet was resumed, $false otherwise
     * 
     * @pre $none
     * @post $none
     */
    @Override
    public long cloudletResume(int cloudletId) {

	if (this.pausedList.containsKey(cloudletId)) {
	    ResCloudlet rcl = this.pausedList.remove(cloudletId);
	    rcl.commitProgress();

	    if (freeCPUs() >= rcl.getPesNumber()) {
		Log.logger.info(Log.clock()
			+ "Resuming cloudlet "
			+ cloudletId
			+ ", done so far: "
			+ rcl.getProcessingDoneSoFar());
		rcl.setStatus(INEXEC);
		for (int i = 0; i < rcl.getPesNumber(); i++) {
		    rcl.setMachineAndPeId(0, i);
		}

		this.execList.put(rcl.getCloudletId(), rcl);
		this.usedPes += rcl.getPesNumber();

		double remainingLength = rcl.getRemainingCloudletLength();
		long estimatedFinishTime = CloudSim.clock()
			+ (long) Math.ceil(remainingLength / (this.capacity * rcl.getPesNumber()));

		return estimatedFinishTime;
	    }

	    Log.logger.fine(Log.clock()
		    + "Cloudlet "
		    + cloudletId
		    + " could not be resumed, queueing it");
	    rcl.setStatus(QUEUED);

	    this.waitingList.put(rcl.getCloudletId(), rcl);
	    return 0;

	}

	/*
	 * not found in the paused list: either it is in in the queue, executing
	 * or does not exist
	 */
	return 0;
    }

    /**
     * Receives an cloudlet to be executed in the VM managed by this scheduler.
     * 
     * @param cloudlet
     *        the submited cloudlet
     * 
     * @return expected finish time of this cloudlet, or 0 if it is in the
     *         waiting queue
     * 
     * @pre gl != null
     * @post $none
     */
    @Override
    public long cloudletSubmit(Cloudlet cloudlet) {
	ResCloudlet rcl = new ResCloudlet(cloudlet);
	if (freeCPUs() >= cloudlet.getPesNumber()) {
	    rcl.setStatus(INEXEC);
	    for (int i = 0; i < cloudlet.getPesNumber(); i++) {
		rcl.setMachineAndPeId(0, i);
	    }

	    this.execList.put(rcl.getCloudletId(), rcl);
	    this.usedPes += cloudlet.getPesNumber();
	    Log.logger.fine("Receiving cloudlet: "
		    + cloudlet.getCloudletId()
		    + " of length: "
		    + cloudlet.getLength());
	} else { // no enough free PEs: go to the waiting queue
	    rcl.setStatus(QUEUED);
	    this.waitingList.put(rcl.getCloudletId(), rcl);
	    Log.logger.fine("Queueing cloudlet: "
		    + cloudlet.getCloudletId()
		    + " of length: "
		    + cloudlet.getLength());
	    return 0;
	}

	// calculate the expected time for cloudlet completion
	double length = rcl.getRemainingCloudletLength();

	final long estimTime = (long) Math.ceil(length
		/ (this.capacity / this.cpus)
		* cloudlet.getPesNumber());

	Log.logger.fine("Estimated time for cloudlet: "
		+ cloudlet.getCloudletId()
		+ " of length: "
		+ cloudlet.getLength()
		+ ", remaining :"
		+ length
		+ " on capacity: "
		+ this.capacity
		+ " is "
		+ estimTime);
	return estimTime;
    }

    /**
     * Gets the status of a cloudlet.
     * 
     * @param cloudletId
     *        ID of the cloudlet
     * 
     * @return status of the cloudlet, or UNKNOWN if the cloudlet does not exist
     * 
     * @pre $none
     * @post $none
     */
    @Override
    public CloudletStatus getCloudletStatus(int cloudletId) {

	if (this.execList.containsKey(cloudletId)) {
	    final CloudletStatus cloudletStatus = this.execList.get(cloudletId).getCloudletStatus();
	    if (cloudletStatus != INEXEC) {
		return INCONSISTENT;
	    }
	    return cloudletStatus;
	}

	if (this.pausedList.containsKey(cloudletId)) {
	    final CloudletStatus cloudletStatus = this.pausedList.get(cloudletId)
		.getCloudletStatus();
	    if (cloudletStatus != PAUSED) {
		return INCONSISTENT;
	    }
	    return cloudletStatus;
	}

	if (this.waitingList.containsKey(cloudletId)) {
	    final CloudletStatus cloudletStatus = this.waitingList.get(cloudletId)
		.getCloudletStatus();
	    if (cloudletStatus != QUEUED) {
		return INCONSISTENT;
	    }
	    return cloudletStatus;
	}

	return UNKNOWN;
    }

    /**
     * Get utilization created by all cloudlets.
     * 
     * @param time
     *        the time
     * 
     * @return total utilization
     */
    @Override
    public double getTotalUtilizationOfCpu(long time) {
	double totalUtilization = 0;
	for (ResCloudlet gl : this.execList.values()) {
	    totalUtilization += gl.getCloudlet().getUtilizationOfCpu(time);
	}
	return totalUtilization;
    }

    /**
     * Returns the number of cloudlets runnning in the virtual machine.
     * 
     * @return number of cloudlets runnning
     * 
     * @pre $none
     * @post $none
     */
    @Override
    public int runningCloudlets() {
	return this.execList.size();
    }

    /**
     * Returns one cloudlet to migrate to another vm.
     * 
     * @return one running cloudlet
     * 
     * @pre $none
     * @post $none
     */
    @Override
    public Cloudlet migrateCloudlet() {
	ResCloudlet rcl = this.execList.remove(0);
	rcl.finalizeCloudlet();
	Cloudlet cl = rcl.getCloudlet();
	this.usedPes -= cl.getPesNumber();
	return cl;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.cloudbus.cloudsim.CloudletScheduler#getCurrentRequestedMips()
     */
    @Override
    public List<Double> getCurrentRequestedMips() {
	List<Double> mipsShare = new ArrayList<Double>();
	final List<Double> currentMipsShare = getCurrentMipsShare();
	if (currentMipsShare != null) {
	    mipsShare.addAll(currentMipsShare);
	}
	return mipsShare;
    }

    @Override
    public double getTotalCurrentAvailableMipsForCloudlet(ResCloudlet rcl, List<Double> mipsShare) {
	// TODO Auto-generated method stub
	return 0;
    }

    @Override
    public double getTotalCurrentRequestedMipsForCloudlet(ResCloudlet rcl, long time) {
	// TODO Auto-generated method stub
	return 0;
    }

    @Override
    public double getTotalCurrentAllocatedMipsForCloudlet(ResCloudlet rcl, long time) {
	// TODO Auto-generated method stub
	return 0;
    }

    /**
     * Cancels execution of a cloudlet.
     * 
     * @param cloudletId
     *        ID of the cloudlet being cancealed
     * 
     * @pre $none
     * @post $none
     * @return TODO
     */
    @Override
    public Cloudlet cloudletCancel(int cloudletId) {
	ResCloudlet rcl = null;

	if (this.execList.containsKey(cloudletId)) {
	    rcl = this.execList.remove(cloudletId);
	    rcl.setStatus(CloudletStatus.CANCELED);
	    long lostComputation = computeProgress(CloudSim.clock() - rcl.getLatestCheckpoint(),
		rcl.getPesNumber());
	    rcl.getCloudlet().setLostComputation(lostComputation);
	    this.usedPes -= rcl.getPesNumber();
	}

	if (this.pausedList.containsKey(cloudletId)) {
	    rcl = this.pausedList.remove(cloudletId);
	    rcl.setStatus(CloudletStatus.CANCELED);
	}

	if (this.waitingList.containsKey(cloudletId)) {
	    rcl = this.waitingList.remove(cloudletId);
	    rcl.setStatus(CloudletStatus.CANCELED);
	}

	if (rcl == null) {
	    return null;
	}

	return rcl.getCloudlet();
    }

    @Override
    public Cloudlet cloudletSuspend(int cloudletId) {

	ResCloudlet rcl = null;

	if (this.execList.containsKey(cloudletId)) {
	    rcl = this.execList.remove(cloudletId);
	    if (rcl.getRemainingCloudletLength() == 0.0) {
		cloudletFinish(rcl);
	    } else {
		this.usedPes -= rcl.getPesNumber();
		rcl.setStatus(CloudletStatus.SUSPENDED);
		rcl.finalizeCloudlet();
		rcl.getCloudlet().setLostComputation(0);
	    }
	}

	if (this.pausedList.containsKey(cloudletId)) {
	    rcl = this.pausedList.remove(cloudletId);
	    rcl.setStatus(CloudletStatus.CANCELED);
	    rcl.finalizeCloudlet();
	}

	if (this.waitingList.containsKey(cloudletId)) {
	    rcl = this.waitingList.remove(cloudletId);
	    rcl.setStatus(CloudletStatus.CANCELED);
	}

	if (rcl == null) {
	    return null;
	}

	return rcl.getCloudlet();
    }

    @Override
    public void mipsChanged(List<Double> allocatedMipsForVm) {
	updateVmProcessing();
	updateCurrentCapacity(allocatedMipsForVm);
    }
}