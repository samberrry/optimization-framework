/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2010, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.cloudletscheduler;

import static org.cloudbus.cloudsim.CloudletStatus.SUCCESS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletStatus;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.ResCloudlet;

/**
 * CloudletScheduler is an abstract class that represents the policy of
 * scheduling performed by a virtual machine. So, classes extending this must
 * execute Cloudlets. Also, the interface for cloudlet management is also
 * implemented in this class.
 * 
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 1.0
 */
public abstract class AbstractCloudletScheduler implements CloudletScheduler {

    /** The previous time. */
    private long previousTime;

    /** The current mips share. */
    private List<Double> currentMipsShare;

    /** The cloudlet waiting list. */
    protected final Map<Integer, ResCloudlet> waitingList;

    /** The cloudlet exec list. */
    protected final Map<Integer, ResCloudlet> execList;

    /** The cloudlet paused list. */
    protected final Map<Integer, ResCloudlet> pausedList;

    /** The cloudlet paused list. */
    protected final Map<Integer, ResCloudlet> finishedList;

    /**
     * Creates a new CloudletScheduler object. This method must be invoked
     * before starting the actual simulation.
     * 
     * @pre $none
     * @post $none
     */
    public AbstractCloudletScheduler() {
	this.execList = new LinkedHashMap<Integer, ResCloudlet>();
	this.waitingList = new LinkedHashMap<Integer, ResCloudlet>();
	this.pausedList = new LinkedHashMap<Integer, ResCloudlet>();
	this.finishedList = new LinkedHashMap<Integer, ResCloudlet>();
	setPreviousTime(0);
    }

    protected Collection<ResCloudlet> getWaitingCloudlets() {
	return new ArrayList<ResCloudlet>(this.waitingList.values());
    }

    protected Collection<ResCloudlet> getRunningCloudlets() {
	return new ArrayList<ResCloudlet>(this.execList.values());
    }

    protected Collection<ResCloudlet> getPausedCloudlets() {
	return new ArrayList<ResCloudlet>(this.pausedList.values());
    }

    protected Collection<ResCloudlet> getFinishedCloudlets() {
	return new ArrayList<ResCloudlet>(this.finishedList.values());
    }

    protected boolean schedulerIsEmpty() {
	return this.waitingList.isEmpty() && this.execList.isEmpty();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * CloudletScheduler#updateVmProcessing
     * (java.util.List)
     */
    @Override
    public abstract long updateVmProcessing();

    /*
     * (non-Javadoc)
     * 
     * @see
     * CloudletScheduler#cloudletSubmit
     * (Cloudlet)
     */
    @Override
    public abstract long cloudletSubmit(Cloudlet gl);

    /*
     * (non-Javadoc)
     * 
     * @see
     * CloudletScheduler#cloudletPause
     * (int)
     */
    @Override
    public abstract boolean cloudletPause(int clId);

    /*
     * (non-Javadoc)
     * 
     * @see
     * CloudletScheduler#cloudletResume
     * (int)
     */
    @Override
    public abstract long cloudletResume(int clId);

    /**
     * Processes a finished cloudlet.
     * 
     * @param rcl
     *        finished cloudlet
     * 
     * @pre rgl != $null
     * @post $none
     */
    protected void cloudletFinish(ResCloudlet rcl) {
	if (Log.logger.isLoggable(Level.FINE)) {
	    Log.logger.fine(Log.clock()
		    + "Cloudlet "
		    + rcl.getCloudletId()
		    + " of status "
		    + rcl.getCloudletStatus()
		    + " finishing on VM "
		    + rcl.getCloudlet().getVmId());
	}
	rcl.finalizeCloudlet();
	rcl.setStatus(SUCCESS);
	this.execList.remove(rcl.getCloudletId());
	this.finishedList.put(rcl.getCloudletId(), rcl);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * CloudletScheduler#getCloudletStatus
     * (int)
     */
    @Override
    public abstract CloudletStatus getCloudletStatus(int clId);

    /**
     * Informs about completion of some cloudlet in the VM managed by this
     * scheduler.
     * 
     * @return $true if there is at least one finished cloudlet; $false
     *         otherwise
     * 
     * @pre $none
     * @post $none
     */

    /*
     * (non-Javadoc)
     * 
     * @see
     * CloudletScheduler#runningCloudlets
     * ()
     */
    @Override
    public abstract int runningCloudlets();

    /*
     * (non-Javadoc)
     * 
     * @see
     * CloudletScheduler#migrateCloudlet
     * ()
     */
    @Override
    public abstract Cloudlet migrateCloudlet();

    /*
     * (non-Javadoc)
     * 
     * @see CloudletScheduler#
     * getTotalUtilizationOfCpu(long)
     */
    @Override
    public abstract double getTotalUtilizationOfCpu(long time);

    /*
     * (non-Javadoc)
     * 
     * @see CloudletScheduler#
     * getCurrentRequestedMips()
     */
    @Override
    public abstract List<Double> getCurrentRequestedMips();

    /**
     * Gets the total current mips for the Cloudlet.
     * 
     * @param rcl
     *        the rcl
     * @param mipsShare
     *        the mips share
     * 
     * @return the total current mips
     */
    protected abstract double getTotalCurrentAvailableMipsForCloudlet(ResCloudlet rcl,
	    List<Double> mipsShare);

    /**
     * Gets the total current requested mips for cloudlet.
     * 
     * @param rcl
     *        the rcl
     * @param time
     *        the time
     * 
     * @return the total current requested mips for cloudlet
     */
    protected abstract double getTotalCurrentRequestedMipsForCloudlet(ResCloudlet rcl, long time);

    /**
     * Gets the total current allocated mips for cloudlet.
     * 
     * @param rcl
     *        the rcl
     * @param time
     *        the time
     * 
     * @return the total current allocated mips for cloudlet
     */
    protected abstract double getTotalCurrentAllocatedMipsForCloudlet(ResCloudlet rcl, long time);

    /*
     * (non-Javadoc)
     * 
     * @see
     * CloudletScheduler#getPreviousTime
     * ()
     */
    protected long getPreviousTime() {
	return this.previousTime;
    }

    /**
     * Sets the previous time.
     * 
     * @param previousTime
     *        the new previous time
     */
    protected void setPreviousTime(long previousTime) {
	this.previousTime = previousTime;
    }

    /**
     * Sets the current mips share.
     * 
     * @param currentMipsShare
     *        the new current mips share
     */
    protected void setCurrentMipsShare(List<Double> currentMipsShare) {
	this.currentMipsShare = currentMipsShare;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * CloudletScheduler#getCurrentMipsShare
     * ()
     */
    protected List<Double> getCurrentMipsShare() {
	return this.currentMipsShare;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * CloudletScheduler#pauseAllCloudlets
     * ()
     */
    @Override
    public void pauseAllCloudlets() {

	List<Integer> toPause = new LinkedList<Integer>();
	toPause.addAll(this.execList.keySet());
	toPause.addAll(this.waitingList.keySet());

	for (Integer resCloudlet : toPause) {
	    cloudletPause(resCloudlet);
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * CloudletScheduler#resumeAllCloudlets
     * ()
     */
    @Override
    public void resumeAllCloudlets() {

	List<Integer> toResume = new LinkedList<Integer>();
	toResume.addAll(this.pausedList.keySet());

	for (Integer i : toResume) {
	    cloudletResume(i);
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * CloudletScheduler#cancelAllCloudlets
     * ()
     */
    @Override
    public void cancelAllCloudlets() {
	List<Integer> toCancel = new LinkedList<Integer>();

	toCancel.addAll(this.execList.keySet());
	toCancel.addAll(this.waitingList.keySet());

	for (Integer i : toCancel) {
	    cloudletCancel(i);
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * CloudletScheduler#cloudletCancel
     * (int)
     */
    @Override
    public abstract Cloudlet cloudletCancel(int cloudletId);

    /*
     * (non-Javadoc)
     * 
     * @see
     * CloudletScheduler#cloudletSuspend
     * (int)
     */
    @Override
    public abstract Cloudlet cloudletSuspend(int cloudletId);

    /*
     * (non-Javadoc)
     * 
     * @see CloudletScheduler#
     * areThereFinishedCloudlets()
     */
    @Override
    public boolean areThereFinishedCloudlets() {
	return !this.finishedList.isEmpty();
    }

    /*
     * (non-Javadoc)
     * 
     * @see CloudletScheduler#
     * getAndRemoveFinishedCloutlets()
     */
    @Override
    public List<Cloudlet> getAndRemoveFinishedCloutlets() {
	LinkedList<Cloudlet> ret = new LinkedList<Cloudlet>();
	for (ResCloudlet cloudlet : this.finishedList.values()) {
	    ret.add(cloudlet.getCloudlet());
	}
	this.finishedList.clear();

	return ret;
    }
}