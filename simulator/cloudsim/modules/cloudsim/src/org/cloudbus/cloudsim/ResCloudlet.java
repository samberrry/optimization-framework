/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2010, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

import static org.cloudbus.cloudsim.CloudletStatus.*;

import org.cloudbus.cloudsim.core.CloudSim;

/**
 * CloudSim ResCloudlet represents a Cloudlet submitted to CloudResource for
 * processing. This class keeps track the time for all activities in the
 * CloudResource for a specific Cloudlet. Before a Cloudlet exits the
 * CloudResource, it is RECOMMENDED to call this method
 * {@link #finalizeCloudlet()}.
 * <p>
 * It contains a Cloudlet object along with its arrival time and the ID of the
 * machine and the Pe (Processing Element) allocated to it. It acts as a
 * placeholder for maintaining the amount of resource share allocated at various
 * times for simulating any scheduling using internal events.
 * 
 * @author Manzur Murshed
 * @author Rajkumar Buyya
 * @since CloudSim Toolkit 1.0
 */
public class ResCloudlet {

    /** The cloudlet. */
    private final Cloudlet cloudlet; // a Cloudlet object

    /** The arrival time. */
    private double arrivalTime; // Cloudlet arrival time for the first time

    /** The finished time. */
    private double finishedTime; // estimation of Cloudlet finished time

    /** The cloudlet finished so far. */
    private double processingDoneSoFar; // length of Cloudlet finished so far

    // Cloudlet execution start time. This attribute will only hold the latest
    // time since a Cloudlet can be cancel, paused or resumed.
    /** The start exec time. */
    private double startExecTime;

    /** The total completion time. */
    private double totalCompletionTime; // total time to complete this Cloudlet

    // The below attributes are only be used by the SpaceShared policy
    /** The machine id. */
    private int machineId; // machine id this Cloudlet is assigned to

    /** The pe id. */
    private int peId; // Pe id this Cloudlet is assigned to

    /** The machine array id. */
    private int[] machineArrayId = null; // an array of machine IDs

    /** The pe array id. */
    private int[] peArrayId = null; // an array of Pe IDs

    /** The index. */
    private int index; // index of machine and Pe arrays

    // NOTE: Below attributes are related to AR stuff
    /** The Constant NOT_FOUND. */
    private static final int NOT_FOUND = -1;

    /** The start time. */
    private final long startTime; // reservation start time

    /** The duration. */
    private final int duration; // reservation duration time

    /** The reserv id. */
    private final int reservId; // reservation id

    /** The pes number. */
    private int pesNumber; // num Pe needed to execute this Cloudlet

    private long latestCheckpoint;

    private long pauseTime;

    /**
     * Allocates a new ResCloudlet object upon the arrival of a Cloudlet object.
     * The arriving time is determined by {@link gridsim.CloudSim#minTime()}.
     * 
     * @param cloudlet
     *        a cloudlet object
     * 
     * @see gridsim.CloudSim#minTime()
     * @pre cloudlet != null
     * @post $none
     */
    public ResCloudlet(Cloudlet cloudlet) {
	// when a new ResCloudlet is created, then it will automatically set
	// the submission time and other properties, such as remaining length
	this.cloudlet = cloudlet;
	this.startTime = 0;
	this.reservId = NOT_FOUND;
	this.duration = 0;
	init();
    }

    /**
     * Allocates a new ResCloudlet object upon the arrival of a Cloudlet object.
     * Use this constructor to store reserved Cloudlets, i.e. Cloudlets that
     * done reservation before. The arriving time is determined by
     * {@link gridsim.CloudSim#minTime()}.
     * 
     * @param cloudlet
     *        a cloudlet object
     * @param startTime
     *        a reservation start time. Can also be interpreted as starting time
     *        to execute this Cloudlet.
     * @param duration
     *        a reservation duration time. Can also be interpreted as how long
     *        to execute this Cloudlet.
     * @param reservID
     *        a reservation ID that owns this Cloudlet
     * 
     * @see gridsim.CloudSim#minTime()
     * @pre cloudlet != null
     * @pre startTime > 0
     * @pre duration > 0
     * @pre reservID > 0
     * @post $none
     */
    public ResCloudlet(Cloudlet cloudlet, long startTime, int duration, int reservID) {
	this.cloudlet = cloudlet;
	this.startTime = startTime;
	this.reservId = reservID;
	this.duration = duration;

	init();
    }

    /**
     * Gets the Cloudlet or reservation start time.
     * 
     * @return Cloudlet's starting time
     * 
     * @pre $none
     * @post $none
     */
    public long getStartTime() {
	return this.startTime;
    }

    /**
     * Gets the reservation duration time.
     * 
     * @return reservation duration time
     * 
     * @pre $none
     * @post $none
     */
    public int getDurationTime() {
	return this.duration;
    }

    /**
     * Gets the number of PEs required to execute this Cloudlet.
     * 
     * @return number of Pe
     * 
     * @pre $none
     * @post $none
     */
    public int getPesNumber() {
	return this.pesNumber;
    }

    /**
     * Gets the reservation ID that owns this Cloudlet.
     * 
     * @return a reservation ID
     * 
     * @pre $none
     * @post $none
     */
    public int getReservationID() {
	return this.reservId;
    }

    /**
     * Checks whether this Cloudlet is submitted by reserving or not.
     * 
     * @return <tt>true</tt> if this Cloudlet has reserved before,
     *         <tt>false</tt> otherwise
     * 
     * @pre $none
     * @post $none
     */
    public boolean hasReserved() {
	if (this.reservId == NOT_FOUND) {
	    return false;
	}

	return true;
    }

    /**
     * Initialises all local attributes.
     * 
     * @pre $none
     * @post $none
     */
    private void init() {
	// get number of PEs required to run this Cloudlet
	this.pesNumber = this.cloudlet.getPesNumber();

	// if more than 1 Pe, then create an array
	if (this.pesNumber > 1) {
	    this.machineArrayId = new int[this.pesNumber];
	    this.peArrayId = new int[this.pesNumber];
	}

	this.arrivalTime = CloudSim.clock();
	this.cloudlet.setSubmissionTime(this.arrivalTime);

	// default values
	this.finishedTime = NOT_FOUND; // Cannot finish in this hourly slot.
	this.machineId = NOT_FOUND;
	this.peId = NOT_FOUND;
	this.index = 0;
	this.totalCompletionTime = 0.0;
	this.startExecTime = 0.0;
	this.latestCheckpoint = CloudSim.clock();

	// In case a Cloudlet has been partially executed by some other resource
	this.processingDoneSoFar = this.cloudlet.getTotalProgress();
    }

    /**
     * Gets this Cloudlet entity Id.
     * 
     * @return the Cloudlet entity Id
     * 
     * @pre $none
     * @post $none
     */
    public int getCloudletId() {
	return this.cloudlet.getCloudletId();
    }

    /**
     * Gets the user or owner of this Cloudlet.
     * 
     * @return the Cloudlet's user Id
     * 
     * @pre $none
     * @post $none
     */
    public int getUserId() {
	return this.cloudlet.getUserId();
    }

    /**
     * Gets the Cloudlet's length.
     * 
     * @return Cloudlet's length
     * 
     * @pre $none
     * @post $none
     */
    public double getCloudletLength() {
	return this.cloudlet.getLength();
    }

    /**
     * Gets the total Cloudlet's length (across all PEs).
     * 
     * @return total Cloudlet's length
     * 
     * @pre $none
     * @post $none
     */
    public double getCloudletTotalLength() {
	return this.cloudlet.getLength();
    }

    /**
     * Gets the Cloudlet's class type.
     * 
     * @return class type of the Cloudlet
     * 
     * @pre $none
     * @post $none
     */
    public int getCloudletClassType() {
	return this.cloudlet.getClassType();
    }

    /**
     * Sets the Cloudlet status.
     * 
     * @param status
     *        the Cloudlet status
     * 
     * @return <tt>true</tt> if the new status has been set, <tt>false</tt>
     *         otherwise
     * 
     * @pre status >= 0
     * @post $none
     */
    public boolean setStatus(CloudletStatus status) {
	// gets Cloudlet's previous status
	CloudletStatus prevStatus = this.cloudlet.getStatus();

	// if the status of a Cloudlet is the same as last time, then ignore
	if (prevStatus == status) {
	    return false;
	}

	try {
	    final long clock = CloudSim.clock(); // gets the current clock

	    // sets Cloudlet's current status
	    this.cloudlet.setStatus(status);

	    // if a previous Cloudlet status is INEXEC
	    if (prevStatus == INEXEC) {
		if (status == PAUSED || status == SUCCESS) {
		    this.pauseTime = clock;
		    this.index = 0;
		}
	    }

	    // if a Cloudlet is now in execution
	    if (status == INEXEC || prevStatus == PAUSED) {
		// then update the Cloudlet completion time
		this.totalCompletionTime += this.pauseTime - this.startExecTime;
		this.latestCheckpoint = clock;
		this.startExecTime = clock;
		this.cloudlet.setExecStartTime(this.startExecTime);
	    }
	    return true;
	} catch (Exception e) {
	    return false;
	}
    }

    /**
     * Gets the Cloudlet's execution start time.
     * 
     * @return Cloudlet's execution start time
     * 
     * @pre $none
     * @post $none
     */
    public double getExecStartTime() {
	return this.cloudlet.getExecStartTime();
    }

    /**
     * Sets this Cloudlet's execution parameters. These parameters are set by
     * the CloudResource before departure or sending back to the original
     * Cloudlet's owner.
     * 
     * @param wallClockTime
     *        the time of this Cloudlet resides in a CloudResource (from arrival
     *        time until departure time).
     * @param actualCPUTime
     *        the total execution time of this Cloudlet in a CloudResource.
     * 
     * @pre wallClockTime >= 0.0
     * @pre actualCPUTime >= 0.0
     * @post $none
     */
    public void setExecParam(double wallClockTime, double actualCPUTime) {
	this.cloudlet.setExecParam(wallClockTime, actualCPUTime);
    }

    /**
     * Sets the machine and Pe (Processing Element) ID.
     * 
     * @param machineId
     *        machine ID
     * @param peId
     *        Pe ID
     * 
     * @pre machineID >= 0
     * @pre peID >= 0
     * @post $none
     */
    public void setMachineAndPeId(int machineId, int peId) {
	// if this job only requires 1 Pe
	this.machineId = machineId;
	this.peId = peId;

	// if this job requires many PEs
	if (this.peArrayId != null && this.pesNumber > 1) {
	    this.machineArrayId[this.index] = machineId;
	    this.peArrayId[this.index] = peId;
	    this.index++;
	}
    }

    /**
     * Gets machine ID.
     * 
     * @return machine ID or <tt>-1</tt> if it is not specified before
     * 
     * @pre $none
     * @post $result >= -1
     */
    public int getMachineId() {
	return this.machineId;
    }

    /**
     * Gets Pe ID.
     * 
     * @return Pe ID or <tt>-1</tt> if it is not specified before
     * 
     * @pre $none
     * @post $result >= -1
     */
    public int getPeId() {
	return this.peId;
    }

    /**
     * Gets a list of Pe IDs. <br>
     * NOTE: To get the machine IDs corresponding to these Pe IDs, use
     * {@link #getMachineIdList()}.
     * 
     * @return an array containing Pe IDs.
     * 
     * @pre $none
     * @post $none
     */
    public int[] getPeIdList() {
	return this.peArrayId;
    }

    /**
     * Gets a list of Machine IDs. <br>
     * NOTE: To get the Pe IDs corresponding to these machine IDs, use
     * {@link #getPeIdList()}.
     * 
     * @return an array containing Machine IDs.
     * 
     * @pre $none
     * @post $none
     */
    public int[] getMachineIdList() {
	return this.machineArrayId;
    }

    /**
     * Gets the remaining cloudlet length.
     * 
     * @return cloudlet length
     * 
     * @pre $none
     * @post $result >= 0
     */
    public double getRemainingCloudletLength() {
	return Math.max(0D, this.cloudlet.getLength() - this.processingDoneSoFar);
    }

    /**
     * Finalizes all relevant information before <tt>exiting</tt> the
     * CloudResource entity. This method sets the final data of:
     * <ul>
     * <li>wall clock time, i.e. the time of this Cloudlet resides in a
     * CloudResource (from arrival time until departure time).
     * <li>actual CPU time, i.e. the total execution time of this Cloudlet in a
     * CloudResource.
     * <li>Cloudlet's finished so far
     * </ul>
     * 
     * @pre $none
     * @post $none
     */
    public void finalizeCloudlet() {
	// Sets the wall clock time and actual CPU time
	double wallClockTime = CloudSim.clock() - this.arrivalTime;
	this.cloudlet.setExecParam(wallClockTime, this.totalCompletionTime);
	commitProgress();
    }

    public void commitProgress() {
	this.cloudlet.updateProcessingDoneSoFar(Math.min(this.cloudlet.getLength(),
	    (long) Math.ceil(this.processingDoneSoFar)));
    }

    /**
     * A method that updates the length of cloudlet that has been completed.
     * 
     * @param miLength
     *        cloudlet length in Million Instructions (MI)
     * 
     * @pre miLength >= 0.0
     * @post $none
     */
    public void updateProgress(double miLength) {
	this.processingDoneSoFar += miLength;
    }

    /**
     * Gets arrival time of a cloudlet.
     * 
     * @return arrival time
     * 
     * @pre $none
     * @post $result >= 0.0
     */
    public double getCloudletArrivalTime() {
	return this.arrivalTime;
    }

    /**
     * Sets the finish time for this Cloudlet. If time is negative, then it is
     * being ignored.
     * 
     * @param time
     *        finish time
     * 
     * @pre time >= 0.0
     * @post $none
     */
    public void setFinishTime(double time) {
	if (time < 0.0) {
	    return;
	}

	this.finishedTime = time;
    }

    /**
     * Gets the Cloudlet's finish time.
     * 
     * @return finish time of a cloudlet or <tt>-1.0</tt> if it cannot finish in
     *         this hourly slot
     * 
     * @pre $none
     * @post $result >= -1.0
     */
    public double getClouddletFinishTime() {
	return this.finishedTime;
    }

    /**
     * Gets this Cloudlet object.
     * 
     * @return cloudlet object
     * 
     * @pre $none
     * @post $result != null
     */
    public Cloudlet getCloudlet() {
	return this.cloudlet;
    }

    /**
     * Gets the Cloudlet status.
     * 
     * @return Cloudlet status
     * 
     * @pre $none
     * @post $none
     */
    public CloudletStatus getCloudletStatus() {
	return this.cloudlet.getStatus();
    }

    /**
     * Get unique string identificator of the VM.
     * 
     * @return string uid
     */
    public String getUid() {
	return getUserId() + "-" + getCloudletId();
    }

    public double getProcessingDoneSoFar() {
	return this.processingDoneSoFar;
    }

    public long getLatestCheckpoint() {
	return this.latestCheckpoint;
    }

    public void setLatestCheckpoint(long latestCheckpoint) {
	this.latestCheckpoint = latestCheckpoint;
    }
}
