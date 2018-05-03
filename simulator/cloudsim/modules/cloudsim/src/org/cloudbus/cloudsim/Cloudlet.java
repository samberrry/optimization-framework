/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2010, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

import static org.cloudbus.cloudsim.CloudletStatus.*;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.core.CloudSim;

/**
 * Cloudlet is an extension to the cloudlet. It stores, despite all the
 * information encapsulated in the Cloudlet, the ID of the VM running it.
 * 
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 1.0
 */
public class Cloudlet implements Serializable {

    // the User or Broker ID. It is advisable that broker set this ID
    // with its own ID, so that CloudResource returns to it after the execution
    /** The user id. */
    private int userId;

    // the size of this Cloudlet to be executed in a CloudResource (unit: in MI)
    /** The cloudlet length. */
    private final long length;

    // the input file size of this Cloudlet before execution (unit: in byte)
    /** The cloudlet file size. */
    private final long cloudletFileSize; // in byte = program + input data size

    // the output file size of this Cloudlet after execution (unit: in byte)
    /** The cloudlet output size. */
    private final long cloudletOutputSize;

    /** The pes number. */
    private int pesNumber; // num of Pe required to execute this job

    /** The cloudlet id. */
    private int cloudletId; // this Cloudlet ID

    /** The status. */
    private CloudletStatus status; // status of this Cloudlet

    /** The num. */
    private DecimalFormat num; // to format the decimal number

    /** The finish time. */
    private double finishTime; // the time where this Cloudlet completes

    // start time of executing this Cloudlet.
    // With new functionalities, such as CANCEL, PAUSED and RESUMED, this
    // attribute only stores the latest execution time. Previous execution time
    // are ignored.
    /** The exec start time. */
    private double execStartTime; // in simulation time

    /** The reservation id. */
    private int reservationId = -1; // the ID of a reservation made for this
    // cloudlet

    // records the transaction history for this Cloudlet
    /** The record. */
    private final boolean record; // record a history or not

    /** The newline. */
    private String newline;

    /** The history. */
    private StringBuffer history;

    /** The res list. */
    private final List<Resource> resList;

    /** The index. */
    private int index;

    // differentiated service
    /** The class type. */
    private int classType; // class type of Cloudlet for resource scheduling

    /** The net to s. */
    private int netToS; // ToS for sending Cloudlet over the network

    /** The vm id. */
    protected int vmId;

    /** The cost per bw. */
    protected double costPerBw;

    /** The accumulated bw cost. */
    protected double accumulatedBwCost;

    // Utilization

    /** The utilization of cpu model. */
    private UtilizationModel utilizationModelCpu;

    /** The utilization of memory model. */
    private UtilizationModel utilizationModelRam;

    /** The utilization of bw model. */
    private UtilizationModel utilizationModelBw;

    // Data cloudlet
    private List<String> requiredFiles = null; // list of required filenames

    private long lostComputation = 0;

    private long totalProgress;

    public Cloudlet(final int cloudletId, final long cloudletLength, final int pesNumber) {
	this(cloudletId, cloudletLength, pesNumber, 0, 0, new UtilizationModelFull(),
	    new UtilizationModelFull(), new UtilizationModelFull(), false);
	this.vmId = -1;
	this.accumulatedBwCost = 0.0;
	this.costPerBw = 0.0;
	this.requiredFiles = new LinkedList<String>();
	this.totalProgress = 0;
    }

    /**
     * Allocates a new Cloudlet object. The Cloudlet length, input and output
     * file sizes should be greater than or equal to 1. By default this
     * constructor sets the history of this object.
     * 
     * @param cloudletId
     *        the unique ID of this Cloudlet
     * @param cloudletLength
     *        the length or size (in MI) of this cloudlet to be executed in a
     *        PowerDatacenter
     * @param pesNumber
     *        the pes number
     * @param cloudletFileSize
     *        the file size (in byte) of this cloudlet <tt>BEFORE</tt>
     *        submitting to a PowerDatacenter
     * @param cloudletOutputSize
     *        the file size (in byte) of this cloudlet <tt>AFTER</tt> finish
     *        executing by a PowerDatacenter
     * @param utilizationModelCpu
     *        the utilization model cpu
     * @param utilizationModelRam
     *        the utilization model ram
     * @param utilizationModelBw
     *        the utilization model bw
     * @pre cloudletID >= 0
     * @pre cloudletLength >= 0.0
     * @pre cloudletFileSize >= 1
     * @pre cloudletOutputSize >= 1
     * @post $none
     */
    public Cloudlet(final int cloudletId, final long cloudletLength, final int pesNumber,
	    final long cloudletFileSize, final long cloudletOutputSize,
	    final UtilizationModel utilizationModelCpu, final UtilizationModel utilizationModelRam,
	    final UtilizationModel utilizationModelBw) {
	this(cloudletId, cloudletLength, pesNumber, cloudletFileSize, cloudletOutputSize,
	    utilizationModelCpu, utilizationModelRam, utilizationModelBw, true);
	this.vmId = -1;
	this.accumulatedBwCost = 0.0;
	this.costPerBw = 0.0;

	this.requiredFiles = new LinkedList<String>();
    }

    /**
     * Allocates a new Cloudlet object. The Cloudlet length, input and output
     * file sizes should be greater than or equal to 1.
     * 
     * @param cloudletId
     *        the unique ID of this cloudlet
     * @param cloudletLength
     *        the length or size (in MI) of this cloudlet to be executed in a
     *        PowerDatacenter
     * @param pesNumber
     *        the pes number
     * @param cloudletFileSize
     *        the file size (in byte) of this cloudlet <tt>BEFORE</tt>
     *        submitting to a PowerDatacenter
     * @param cloudletOutputSize
     *        the file size (in byte) of this cloudlet <tt>AFTER</tt> finish
     *        executing by a PowerDatacenter
     * @param utilizationModelCpu
     *        the utilization model cpu
     * @param utilizationModelRam
     *        the utilization model ram
     * @param utilizationModelBw
     *        the utilization model bw
     * @param record
     *        record the history of this object or not
     * @param fileList
     *        list of files required by this cloudlet
     * @pre cloudletID >= 0
     * @pre cloudletLength >= 0.0
     * @pre cloudletFileSize >= 1
     * @pre cloudletOutputSize >= 1
     * @post $none
     */
    public Cloudlet(final int cloudletId, final long cloudletLength, final int pesNumber,
	    final long cloudletFileSize, final long cloudletOutputSize,
	    final UtilizationModel utilizationModelCpu, final UtilizationModel utilizationModelRam,
	    final UtilizationModel utilizationModelBw, final boolean record,
	    final List<String> fileList) {
	this(cloudletId, cloudletLength, pesNumber, cloudletFileSize, cloudletOutputSize,
	    utilizationModelCpu, utilizationModelRam, utilizationModelBw, record);
	this.vmId = -1;
	this.accumulatedBwCost = 0.0;
	this.costPerBw = 0.0;

	this.requiredFiles = fileList;
    }

    /**
     * Allocates a new Cloudlet object. The Cloudlet length, input and output
     * file sizes should be greater than or equal to 1. By default this
     * constructor sets the history of this object.
     * 
     * @param cloudletId
     *        the unique ID of this Cloudlet
     * @param cloudletLength
     *        the length or size (in MI) of this cloudlet to be executed in a
     *        PowerDatacenter
     * @param pesNumber
     *        the pes number
     * @param cloudletFileSize
     *        the file size (in byte) of this cloudlet <tt>BEFORE</tt>
     *        submitting to a PowerDatacenter
     * @param cloudletOutputSize
     *        the file size (in byte) of this cloudlet <tt>AFTER</tt> finish
     *        executing by a PowerDatacenter
     * @param utilizationModelCpu
     *        the utilization model cpu
     * @param utilizationModelRam
     *        the utilization model ram
     * @param utilizationModelBw
     *        the utilization model bw
     * @param fileList
     *        list of files required by this cloudlet
     * @pre cloudletID >= 0
     * @pre cloudletLength >= 0.0
     * @pre cloudletFileSize >= 1
     * @pre cloudletOutputSize >= 1
     * @post $none
     */
    public Cloudlet(final int cloudletId, final long cloudletLength, final int pesNumber,
	    final long cloudletFileSize, final long cloudletOutputSize,
	    final UtilizationModel utilizationModelCpu, final UtilizationModel utilizationModelRam,
	    final UtilizationModel utilizationModelBw, final List<String> fileList) {
	this(cloudletId, cloudletLength, pesNumber, cloudletFileSize, cloudletOutputSize,
	    utilizationModelCpu, utilizationModelRam, utilizationModelBw, true);
	this.vmId = -1;
	this.accumulatedBwCost = 0.0;
	this.costPerBw = 0.0;

	this.requiredFiles = fileList;
    }

    /**
     * Allocates a new Cloudlet object. The Cloudlet length, input and output
     * file sizes should be greater than or equal to 1.
     * 
     * @param cloudletId
     *        the unique ID of this cloudlet
     * @param cloudletLength
     *        the length or size (in MI) of this cloudlet to be executed in a
     *        PowerDatacenter
     * @param pesNumber
     *        the pes number
     * @param cloudletFileSize
     *        the file size (in byte) of this cloudlet <tt>BEFORE</tt>
     *        submitting to a PowerDatacenter
     * @param cloudletOutputSize
     *        the file size (in byte) of this cloudlet <tt>AFTER</tt> finish
     *        executing by a PowerDatacenter
     * @param utilizationModelCpu
     *        the utilization model cpu
     * @param utilizationModelRam
     *        the utilization model ram
     * @param utilizationModelBw
     *        the utilization model bw
     * @param record
     *        record the history of this object or not
     * @pre cloudletID >= 0
     * @pre cloudletLength >= 0.0
     * @pre cloudletFileSize >= 1
     * @pre cloudletOutputSize >= 1
     * @post $none
     */
    public Cloudlet(final int cloudletId, final long cloudletLength, final int pesNumber,
	    final long cloudletFileSize, final long cloudletOutputSize,
	    final UtilizationModel utilizationModelCpu, final UtilizationModel utilizationModelRam,
	    final UtilizationModel utilizationModelBw, final boolean record) {
	this.userId = -1; // to be set by a Broker or user
	this.status = CREATED;
	this.cloudletId = cloudletId;
	this.pesNumber = pesNumber;
	this.execStartTime = 0.0;
	this.finishTime = -1.0; // meaning this Cloudlet hasn't finished yet
	this.classType = 0;
	this.netToS = 0;

	// Cloudlet length, Input and Output size should be at least 1 byte.
	this.length = Math.max(1, cloudletLength);
	this.cloudletFileSize = Math.max(1, cloudletFileSize);
	this.cloudletOutputSize = Math.max(1, cloudletOutputSize);

	// Normally, a Cloudlet is only executed on a resource without being
	// migrated to others. Hence, to reduce memory consumption, set the
	// size of this ArrayList to be less than the default one.
	this.resList = new ArrayList<Resource>(2);
	this.index = -1;
	this.record = record;

	this.vmId = -1;
	this.accumulatedBwCost = 0.0;
	this.costPerBw = 0.0;

	this.requiredFiles = new LinkedList<String>();

	setUtilizationModelCpu(utilizationModelCpu);
	setUtilizationModelRam(utilizationModelRam);
	setUtilizationModelBw(utilizationModelBw);
    }

    // ////////////////////// INTERNAL CLASS ///////////////////////////////////

    /**
     * Internal class that keeps track Cloudlet's movement in different
     * CloudResources.
     */
    private static class Resource implements Serializable {

	private static final long serialVersionUID = 1L;

	/** Cloudlet's submission time to a CloudResource. */
	public double submissionTime = 0.0;

	/**
	 * The time of this Cloudlet resides in a CloudResource (from arrival
	 * time until departure time).
	 */
	public double wallClockTime = 0.0;

	/** The total execution time of this Cloudlet in a CloudResource. */
	public double actualCPUTime = 0.0;

	/** Cost per second a CloudResource charge to execute this Cloudlet. */
	public double costPerSec = 0.0;

	/** Cloudlet's length finished so far. */
	public long processingDoneSoFar = 0;

	/** a CloudResource id. */
	public int resourceId = -1;

	/** a CloudResource name. */
	public String resourceName = null;

    } // end of internal class

    // ////////////////////// End of Internal Class //////////////////////////

    public void reset() {

	this.resList.clear();
	this.index = -1;
	this.vmId = -1;
	this.accumulatedBwCost = 0.0;
	this.costPerBw = 0.0;
	this.execStartTime = 0.0;
	this.finishTime = -1.0;
	this.status = READY;
    }

    /**
     * Sets the id of the reservation made for this cloudlet.
     * 
     * @param resId
     *        the reservation ID
     * 
     * @return <tt>true</tt> if the ID has successfully been set or
     *         <tt>false</tt> otherwise.
     */
    public boolean setReservationId(final int resId) {
	if (resId <= 0) {
	    return false;
	}
	this.reservationId = resId;
	return true;
    }

    /**
     * Gets the reservation ID that owns this Cloudlet.
     * 
     * @return a reservation ID
     * 
     * @pre $none
     * @post $none
     */
    public int getReservationId() {
	return this.reservationId;
    }

    /**
     * Checks whether this Cloudlet is submitted by reserving or not.
     * 
     * @return <tt>true</tt> if this Cloudlet has reserved before,
     *         <tt>false</tt> otherwise
     */
    public boolean hasReserved() {
	if (this.reservationId == -1) {
	    return false;
	}
	return true;
    }

    /**
     * Sets the network service level for sending this cloudlet over a network.
     * 
     * @param netServiceLevel
     *        determines the kind of service this cloudlet receives in the
     *        network (applicable to selected PacketScheduler class only)
     * 
     * @return <code>true</code> if successful.
     * 
     * @pre netServiceLevel >= 0
     * @post $none
     */
    public boolean setNetServiceLevel(final int netServiceLevel) {
	boolean success = false;
	if (netServiceLevel > 0) {
	    this.netToS = netServiceLevel;
	    success = true;
	}

	return success;
    }

    /**
     * Gets the network service level for sending this cloudlet over a network.
     * 
     * @return the network service level
     * 
     * @pre $none
     * @post $none
     */
    public int getNetServiceLevel() {
	return this.netToS;
    }

    /**
     * Gets the waiting time of this cloudlet executed on a resource.
     * 
     * @return the waiting time
     * 
     * @pre $none
     * @post $none
     */
    public double getWaitingTime() {
	if (this.index == -1) {
	    return 0;
	}

	// use the latest resource submission time
	final double subTime = this.resList.get(this.index).submissionTime;
	return this.execStartTime - subTime;
    }

    /**
     * Sets the classType or priority of this Cloudlet for scheduling on a
     * resource.
     * 
     * @param classType
     *        classType of this Cloudlet
     * 
     * @return <tt>true</tt> if it is successful, <tt>false</tt> otherwise
     * 
     * @pre classType > 0
     * @post $none
     */
    public boolean setClassType(final int classType) {
	boolean success = false;
	if (classType > 0) {
	    this.classType = classType;
	    success = true;
	}

	return success;
    }

    /**
     * Gets the classtype or priority of this Cloudlet for scheduling on a
     * resource.
     * 
     * @return classtype of this cloudlet
     * 
     * @pre $none
     * @post $none
     */
    public int getClassType() {
	return this.classType;
    }

    /**
     * Sets the number of PEs required to run this Cloudlet. <br>
     * NOTE: The Cloudlet length is computed only for 1 Pe for simplicity. <br>
     * For example, this Cloudlet has a length of 500 MI and requires 2 PEs.
     * This means each Pe will execute 500 MI of this Cloudlet.
     * 
     * @param pesNumber
     *        number of Pe
     * 
     * @return <tt>true</tt> if it is successful, <tt>false</tt> otherwise
     * 
     * @pre numPE > 0
     * @post $none
     */
    public boolean setPesNumber(final int pesNumber) {
	if (pesNumber > 0) {
	    this.pesNumber = pesNumber;
	    return true;
	}
	return false;
    }

    /**
     * Gets the number of PEs required to run this Cloudlet.
     * 
     * @return number of PEs
     * 
     * @pre $none
     * @post $none
     */
    public int getPesNumber() {
	return this.pesNumber;
    }

    /**
     * Gets the history of this Cloudlet. The layout of this history is in a
     * readable table column with <tt>time</tt> and <tt>description</tt> as
     * headers.
     * 
     * @return a String containing the history of this Cloudlet object.
     * 
     * @pre $none
     * @post $result != null
     */
    public String getCloudletHistory() {
	String msg = null;
	if (this.history == null) {
	    msg = "No history is recorded for Cloudlet #" + this.cloudletId;
	} else {
	    msg = this.history.toString();
	}

	return msg;
    }

    /**
     * Gets the length of this Cloudlet that has been executed so far from the
     * latest CloudResource. This method is useful when trying to move this
     * Cloudlet into different CloudResources or to cancel it.
     * 
     * @return the length of a partially executed Cloudlet or the full Cloudlet
     *         length if it is completed
     * 
     * @pre $none
     * @post $result >= 0.0
     */
    public long getTotalProgress() {
	return Math.min(this.length, this.totalProgress);
    }

    public long getRemainingProgress() {
	return this.length - getTotalProgress();
    }

    /**
     * Checks whether this Cloudlet has finished execution or not.
     * 
     * @return <tt>true</tt> if this Cloudlet has finished execution,
     *         <tt>false</tt> otherwise
     * 
     * @pre $none
     * @post $none
     */
    public boolean isFinished() {
	return this.totalProgress >= this.length;
    }

    /**
     * Sets the length of this Cloudlet that has been executed so far. This
     * method is used by ResCloudlet class when an application is decided to
     * cancel or to move this Cloudlet into different CloudResources.
     * 
     * @param doneSoFar
     *        length of this Cloudlet
     * 
     * @see gridsim.AllocPolicy
     * @see gridsim.ResCloudlet
     * @pre length >= 0.0
     * @post $none
     */
    public void updateProcessingDoneSoFar(final long doneSoFar) {
	// if length is -ve then ignore
	if (doneSoFar < 0.0 || this.index < 0) {
	    return;
	}

	final Resource res = this.resList.get(this.index);
	res.processingDoneSoFar = doneSoFar;
	this.totalProgress = doneSoFar;

	if (this.record) {
	    write("Sets the length's finished so far to " + this.totalProgress);
	}
    }

    /**
     * Sets the user or owner ID of this Cloudlet. It is <tt>VERY</tt> important
     * to set the user ID, otherwise this Cloudlet will not be executed in a
     * CloudResource.
     * 
     * @param id
     *        the user ID
     * 
     * @pre id >= 0
     * @post $none
     */
    public void setUserId(final int id) {
	this.userId = id;
	if (this.record) {
	    write("Assigns the Cloudlet to " + CloudSim.getEntityName(id) + " (ID #" + id + ")");
	}
    }

    /**
     * Gets the user or owner ID of this Cloudlet.
     * 
     * @return the user ID or <tt>-1</tt> if the user ID has not been set before
     * 
     * @pre $none
     * @post $result >= -1
     */
    public int getUserId() {
	return this.userId;
    }

    /**
     * Gets the latest resource ID that processes this Cloudlet.
     * 
     * @return the resource ID or <tt>-1</tt> if none
     * 
     * @pre $none
     * @post $result >= -1
     */
    public int getResourceId() {
	if (this.index == -1) {
	    return -1;
	}
	return this.resList.get(this.index).resourceId;
    }

    /**
     * Gets the input file size of this Cloudlet <tt>BEFORE</tt> submitting to a
     * CloudResource.
     * 
     * @return the input file size of this Cloudlet
     * 
     * @pre $none
     * @post $result >= 1
     */
    public long getCloudletFileSize() {
	return this.cloudletFileSize;
    }

    /**
     * Gets the output size of this Cloudlet <tt>AFTER</tt> submitting and
     * executing to a CloudResource.
     * 
     * @return the Cloudlet output file size
     * 
     * @pre $none
     * @post $result >= 1
     */
    public long getCloudletOutputSize() {
	return this.cloudletOutputSize;
    }

    /**
     * Sets the resource parameters for which this Cloudlet is going to be
     * executed. <br>
     * NOTE: This method <tt>should</tt> be called only by a resource entity,
     * not the user or owner of this Cloudlet.
     * 
     * @param resourceID
     *        the CloudResource ID
     * @param cost
     *        the cost running this CloudResource per second
     * 
     * @pre resourceID >= 0
     * @pre cost > 0.0
     * @post $none
     */
    public void setResourceParameter(final int resourceID, final double cost) {
	final Resource res = new Resource();
	res.resourceId = resourceID;
	res.costPerSec = cost;
	res.resourceName = CloudSim.getEntityName(resourceID);

	// add into a list if moving to a new grid resource
	this.resList.add(res);

	if (this.index == -1 && this.record) {
	    write("Allocates this Cloudlet to "
		    + res.resourceName
		    + " (ID #"
		    + resourceID
		    + ") with cost = $"
		    + cost
		    + "/sec");
	} else if (this.record) {
	    final int id = this.resList.get(this.index).resourceId;
	    final String name = this.resList.get(this.index).resourceName;
	    write("Moves Cloudlet from "
		    + name
		    + " (ID #"
		    + id
		    + ") to "
		    + res.resourceName
		    + " (ID #"
		    + resourceID
		    + ") with cost = $"
		    + cost
		    + "/sec");
	}

	this.index++; // initially, index = -1
    }

    /**
     * Sets the submission or arrival time of this Cloudlet into a
     * CloudResource.
     * 
     * @param clockTime
     *        the submission time
     * 
     * @pre clockTime >= 0.0
     * @post $none
     */
    public void setSubmissionTime(final double clockTime) {
	if (clockTime < 0.0 || this.index < 0) {
	    return;
	}

	final Resource res = this.resList.get(this.index);
	res.submissionTime = clockTime;

	if (this.record) {
	    write("Sets the submission time to " + this.num.format(clockTime));
	}
    }

    /**
     * Gets the submission or arrival time of this Cloudlet from the latest
     * CloudResource.
     * 
     * @return the submission time or <tt>0.0</tt> if none
     * 
     * @pre $none
     * @post $result >= 0.0
     */
    public double getSubmissionTime() {
	if (this.index == -1) {
	    return 0.0;
	}
	return this.resList.get(this.index).submissionTime;
    }

    /**
     * Sets the execution start time of this Cloudlet inside a CloudResource.
     * <b>NOTE:</b> With new functionalities, such as being able to cancel / to
     * pause / to resume this Cloudlet, the execution start time only holds the
     * latest one. Meaning, all previous execution start time are ignored.
     * 
     * @param clockTime
     *        the latest execution start time
     * 
     * @pre clockTime >= 0.0
     * @post $none
     */
    public void setExecStartTime(final double clockTime) {
	this.execStartTime = clockTime;
	if (this.record) {
	    write("Sets the execution start time to " + this.num.format(clockTime));
	}
    }

    /**
     * Gets the latest execution start time.
     * 
     * @return the latest execution start time
     * 
     * @pre $none
     * @post $result >= 0.0
     */
    public double getExecStartTime() {
	return this.execStartTime;
    }

    /**
     * Sets this Cloudlet's execution parameters. These parameters are set by
     * the CloudResource before departure or sending back to the original
     * Cloudlet's owner.
     * 
     * @param wallTime
     *        the time of this Cloudlet resides in a CloudResource (from arrival
     *        time until departure time).
     * @param actualTime
     *        the total execution time of this Cloudlet in a CloudResource.
     * 
     * @pre wallTime >= 0.0
     * @pre actualTime >= 0.0
     * @post $none
     */
    public void setExecParam(final double wallTime, final double actualTime) {
	if (wallTime < 0.0 || actualTime < 0.0 || this.index < 0) {
	    return;
	}

	final Resource res = this.resList.get(this.index);
	res.wallClockTime = wallTime;
	res.actualCPUTime = actualTime;

	if (this.record) {
	    write("Sets the wall clock time to "
		    + this.num.format(wallTime)
		    + " and the actual CPU time to "
		    + this.num.format(actualTime));
	}
    }

    /**
     * Sets the status code of this Cloudlet.
     * 
     * @param newStatus
     *        the status code of this Cloudlet
     * 
     * @throws Exception
     *         Invalid range of Cloudlet status
     * 
     * @pre newStatus >= 0 && newStatus <= 8
     * @post $none
     */
    public void setStatus(final CloudletStatus newStatus) throws Exception {
	// if the new status is same as current one, then ignore the rest
	if (this.status == newStatus) {
	    return;
	}

	if (newStatus == SUCCESS) {
	    this.finishTime = CloudSim.clock();
	}

	if (this.record) {
	    write("Sets Cloudlet status from " + this.status + " to " + newStatus);
	}

	this.status = newStatus;
    }

    /**
     * Gets the length of this Cloudlet.
     * 
     * @return the length of this Cloudlet
     * 
     * @pre $none
     * @post $result >= 0.0
     */
    public long getLength() {
	return this.length;
    }

    /**
     * Gets the cost running this Cloudlet in the latest CloudResource.
     * 
     * @return the cost associated with running this Cloudlet or <tt>0.0</tt> if
     *         none
     * 
     * @pre $none
     * @post $result >= 0.0
     */
    public double getCostPerSec() {
	if (this.index == -1) {
	    return 0.0;
	}
	return this.resList.get(this.index).costPerSec;
    }

    /**
     * Gets the time of this Cloudlet resides in the latest CloudResource (from
     * arrival time until departure time).
     * 
     * @return the time of this Cloudlet resides in a CloudResource
     * 
     * @pre $none
     * @post $result >= 0.0
     */
    public double getWallClockTime() {
	if (this.index == -1) {
	    return 0.0;
	}
	return this.resList.get(this.index).wallClockTime;
    }

    /**
     * Gets all the CloudResource names that executed this Cloudlet.
     * 
     * @return an array of CloudResource names or <tt>null</tt> if it has none
     * 
     * @pre $none
     * @post $none
     */
    public String[] getAllResourceName() {
	final int size = this.resList.size();
	String[] data = null;

	if (size > 0) {
	    data = new String[size];
	    for (int i = 0; i < size; i++) {
		data[i] = this.resList.get(i).resourceName;
	    }
	}

	return data;
    }

    /**
     * Gets all the CloudResource IDs that executed this Cloudlet.
     * 
     * @return an array of CloudResource IDs or <tt>null</tt> if it has none
     * 
     * @pre $none
     * @post $none
     */
    public int[] getAllResourceId() {
	final int size = this.resList.size();
	int[] data = null;

	if (size > 0) {
	    data = new int[size];
	    for (int i = 0; i < size; i++) {
		data[i] = this.resList.get(i).resourceId;
	    }
	}

	return data;
    }

    /**
     * Gets the total execution time of this Cloudlet in a given CloudResource
     * ID.
     * 
     * @param resId
     *        a CloudResource entity ID
     * 
     * @return the total execution time of this Cloudlet in a CloudResource or
     *         <tt>0.0</tt> if not found
     * 
     * @pre resId >= 0
     * @post $result >= 0.0
     */
    public double getActualCPUTime(final int resId) {
	Resource resource = getResourceById(resId);
	if (resource != null) {
	    return resource.actualCPUTime;
	}
	return 0.0;
    }

    /**
     * Gets the cost running this Cloudlet in a given CloudResource ID.
     * 
     * @param resId
     *        a CloudResource entity ID
     * 
     * @return the cost associated with running this Cloudlet or <tt>0.0</tt> if
     *         not found
     * 
     * @pre resId >= 0
     * @post $result >= 0.0
     */
    public double getCostPerSec(final int resId) {
	Resource resource = getResourceById(resId);
	if (resource != null) {
	    return resource.costPerSec;
	}
	return 0.0;
    }

    /**
     * Gets the length of this Cloudlet that has been executed so far in a given
     * CloudResource ID. This method is useful when trying to move this Cloudlet
     * into different CloudResources or to cancel it.
     * 
     * @param resId
     *        a CloudResource entity ID
     * 
     * @return the length of a partially executed Cloudlet or the full Cloudlet
     *         length if it is completed or <tt>0.0</tt> if not found
     * 
     * @pre resId >= 0
     * @post $result >= 0.0
     */
    public double getCloudletFinishedSoFar(final int resId) {
	Resource resource = getResourceById(resId);
	if (resource != null) {
	    return resource.processingDoneSoFar;
	}
	return 0;
    }

    /**
     * Gets the submission or arrival time of this Cloudlet in the given
     * CloudResource ID.
     * 
     * @param resId
     *        a CloudResource entity ID
     * 
     * @return the submission time or <tt>0.0</tt> if not found
     * 
     * @pre resId >= 0
     * @post $result >= 0.0
     */
    public double getSubmissionTime(final int resId) {
	Resource resource = getResourceById(resId);
	if (resource != null) {
	    return resource.submissionTime;
	}
	return 0.0;
    }

    /**
     * Gets the time of this Cloudlet resides in a given CloudResource ID (from
     * arrival time until departure time).
     * 
     * @param resId
     *        a CloudResource entity ID
     * 
     * @return the time of this Cloudlet resides in the CloudResource or
     *         <tt>0.0</tt> if not found
     * 
     * @pre resId >= 0
     * @post $result >= 0.0
     */
    public double getWallClockTime(final int resId) {
	Resource resource = getResourceById(resId);
	if (resource != null) {
	    return resource.wallClockTime;
	}
	return 0.0;
    }

    /**
     * Gets the CloudResource name based on its ID.
     * 
     * @param resId
     *        a CloudResource entity ID
     * 
     * @return the CloudResource name or <tt>null</tt> if not found
     * 
     * @pre resId >= 0
     * @post $none
     */
    public String getResourceName(final int resId) {
	Resource resource = getResourceById(resId);
	if (resource != null) {
	    return resource.resourceName;
	}
	return null;
    }

    /**
     * Gets the resource by id.
     * 
     * @param resourceId
     *        the resource id
     * 
     * @return the resource by id
     */
    public Resource getResourceById(final int resourceId) {
	for (Resource resource : this.resList) {
	    if (resource.resourceId == resourceId) {
		return resource;
	    }
	}
	return null;
    }

    /**
     * Gets the finish time of this Cloudlet in a CloudResource.
     * 
     * @return the finish or completion time of this Cloudlet or <tt>-1</tt> if
     *         not finished yet.
     * 
     * @pre $none
     * @post $result >= -1
     */
    public double getFinishTime() {
	return this.finishTime;
    }

    // //////////////////////// PROTECTED METHODS //////////////////////////////

    /**
     * Writes this particular history transaction of this Cloudlet into a log.
     * 
     * @param str
     *        a history transaction of this Cloudlet
     * 
     * @pre str != null
     * @post $none
     */
    protected void write(final String str) {
	if (!this.record) {
	    return;
	}

	if (this.num == null || this.history == null) { // Creates the history
	    // or transactions of
	    // this Cloudlet
	    this.newline = System.getProperty("line.separator");
	    this.num = new DecimalFormat("#0.00#"); // with 3 decimal spaces
	    this.history = new StringBuffer(1000);
	    this.history.append("Time below denotes the simulation time.");
	    this.history.append(System.getProperty("line.separator"));
	    this.history.append("Time (sec)       Description Cloudlet #" + this.cloudletId);
	    this.history.append(System.getProperty("line.separator"));
	    this.history.append("------------------------------------------");
	    this.history.append(System.getProperty("line.separator"));
	    this.history.append(this.num.format(CloudSim.clock()));
	    this.history.append("   Creates Cloudlet ID #" + this.cloudletId);
	    this.history.append(System.getProperty("line.separator"));
	}

	this.history.append(this.num.format(CloudSim.clock()));
	this.history.append("   " + str + this.newline);
    }

    /**
     * Get the status of the Cloudlet.
     * 
     * @return status of the Cloudlet
     * 
     * @pre $none
     * @post $none
     */
    public CloudletStatus getStatus() {
	return this.status;
    }

    /**
     * Gets the ID of this Cloudlet.
     * 
     * @return Cloudlet Id
     * 
     * @pre $none
     * @post $none
     */
    public int getCloudletId() {
	return this.cloudletId;
    }

    /**
     * Gets the ID of the VM that will run this Cloudlet.
     * 
     * @return VM Id, -1 if the Cloudlet was not assigned to a VM
     * 
     * @pre $none
     * @post $none
     */
    public int getVmId() {
	return this.vmId;
    }

    /**
     * Sets the ID of the VM that will run this Cloudlet.
     * 
     * @param vmId
     *        the vm id
     * 
     * @pre id >= 0
     * @post $none
     */
    public void setVmId(final int vmId) {
	this.vmId = vmId;
    }

    /**
     * Returns the time the Cloudlet actually run.
     * 
     * @return time in which the Cloudlet was running
     * 
     * @pre $none
     * @post $none
     */
    public double getActualCPUTime() {
	return getFinishTime() - getExecStartTime();
    }

    /**
     * Sets the resource parameters for which this Cloudlet is going to be
     * executed. <br>
     * NOTE: This method <tt>should</tt> be called only by a resource entity,
     * not the user or owner of this Cloudlet.
     * 
     * @param resourceID
     *        the CloudResource ID
     * @param costPerCPU
     *        the cost running this Cloudlet per second
     * @param costPerBw
     *        the cost of data transfer to this PowerDatacenter
     * 
     * @pre resourceID >= 0
     * @pre cost > 0.0
     * @post $none
     */
    public void setResourceParameter(final int resourceID, final double costPerCPU,
	    final double costPerBw) {
	setResourceParameter(resourceID, costPerCPU);
	this.costPerBw = costPerBw;
	this.accumulatedBwCost = costPerBw * getCloudletFileSize();

    }

    /**
     * Gets the total cost of processing or executing this Cloudlet
     * <tt>Processing Cost = input data transfer + processing cost + output transfer cost</tt>
     * .
     * 
     * @return the total cost of processing Cloudlet
     * 
     * @pre $none
     * @post $result >= 0.0
     */
    public double getProcessingCost() {
	// cloudlet cost: execution cost...
	// double cost = getProcessingCost();
	double cost = 0;
	// ...plus input data transfer cost...
	cost += this.accumulatedBwCost;
	// ...plus output cost
	cost += this.costPerBw * getCloudletOutputSize();
	return cost;
    }

    // Data cloudlet

    /**
     * Gets the required files.
     * 
     * @return the required files
     */
    public List<String> getRequiredFiles() {
	return this.requiredFiles;
    }

    /**
     * Sets the required files.
     * 
     * @param requiredFiles
     *        the new required files
     */
    protected void setRequiredFiles(final List<String> requiredFiles) {
	this.requiredFiles = requiredFiles;
    }

    /**
     * Adds the required filename to the list.
     * 
     * @param fileName
     *        the required filename
     * 
     * @return <tt>true</tt> if succesful, <tt>false</tt> otherwise
     */
    public boolean addRequiredFile(final String fileName) {
	// if the list is empty
	if (getRequiredFiles() == null) {
	    setRequiredFiles(new LinkedList<String>());
	}

	// then check whether filename already exists or not
	boolean result = false;
	for (int i = 0; i < getRequiredFiles().size(); i++) {
	    final String temp = getRequiredFiles().get(i);
	    if (temp.equals(fileName)) {
		result = true;
		break;
	    }
	}

	if (!result) {
	    getRequiredFiles().add(fileName);
	}

	return result;
    }

    /**
     * Deletes the given filename from the list.
     * 
     * @param filename
     *        the given filename to be deleted
     * 
     * @return <tt>true</tt> if succesful, <tt>false</tt> otherwise
     */
    public boolean deleteRequiredFile(final String filename) {
	boolean result = false;
	if (getRequiredFiles() == null) {
	    return result;
	}

	for (int i = 0; i < getRequiredFiles().size(); i++) {
	    final String temp = getRequiredFiles().get(i);

	    if (temp.equals(filename)) {
		getRequiredFiles().remove(i);
		result = true;

		break;
	    }
	}

	return result;
    }

    /**
     * Checks whether this cloudlet requires any files or not.
     * 
     * @return <tt>true</tt> if required, <tt>false</tt> otherwise
     */
    public boolean requiresFiles() {
	boolean result = false;
	if (getRequiredFiles() != null && getRequiredFiles().size() > 0) {
	    result = true;
	}

	return result;
    }

    /**
     * Gets the utilization model cpu.
     * 
     * @return the utilization model cpu
     */
    public UtilizationModel getUtilizationModelCpu() {
	return this.utilizationModelCpu;
    }

    /**
     * Sets the utilization model cpu.
     * 
     * @param utilizationModelCpu
     *        the new utilization model cpu
     */
    public void setUtilizationModelCpu(final UtilizationModel utilizationModelCpu) {
	this.utilizationModelCpu = utilizationModelCpu;
    }

    /**
     * Gets the utilization model ram.
     * 
     * @return the utilization model ram
     */
    public UtilizationModel getUtilizationModelRam() {
	return this.utilizationModelRam;
    }

    /**
     * Sets the utilization model ram.
     * 
     * @param utilizationModelRam
     *        the new utilization model ram
     */
    public void setUtilizationModelRam(final UtilizationModel utilizationModelRam) {
	this.utilizationModelRam = utilizationModelRam;
    }

    /**
     * Gets the utilization model bw.
     * 
     * @return the utilization model bw
     */
    public UtilizationModel getUtilizationModelBw() {
	return this.utilizationModelBw;
    }

    /**
     * Sets the utilization model bw.
     * 
     * @param utilizationModelBw
     *        the new utilization model bw
     */
    public void setUtilizationModelBw(final UtilizationModel utilizationModelBw) {
	this.utilizationModelBw = utilizationModelBw;
    }

    /**
     * Gets the total utilization of cpu.
     * 
     * @param time
     *        the time
     * 
     * @return the utilization of cpu
     */
    public double getUtilizationOfCpu(final long time) {
	return getUtilizationModelCpu().getUtilization(time);
    }

    /**
     * Gets the utilization of memory.
     * 
     * @param time
     *        the time
     * 
     * @return the utilization of memory
     */
    public double getUtilizationOfRam(final long time) {
	return getUtilizationModelRam().getUtilization(time);
    }

    /**
     * Gets the utilization of bw.
     * 
     * @param time
     *        the time
     * 
     * @return the utilization of bw
     */
    public double getUtilizationOfBw(final long time) {
	return getUtilizationModelBw().getUtilization(time);
    }

    public void setID(int newID) {
	this.cloudletId = newID;
    }

    public void setLostComputation(long lostComputation) {
	this.lostComputation = lostComputation;
    }

    public long getLostComputation() {
	return this.lostComputation;
    }
}
