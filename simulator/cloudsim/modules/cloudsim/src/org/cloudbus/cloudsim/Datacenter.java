/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2010, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;

/**
 * Datacenter class is a CloudResource whose hostList are virtualized. It deals
 * with processing of VM queries (i.e., handling of VMs) instead of processing
 * Cloudlet-related queries. So, even though an AllocPolicy will be instantiated
 * (in the init() method of the superclass, it will not be used, as processing
 * of cloudlets are handled by the CloudletScheduler and processing of
 * VirtualMachines are handled by the VmAllocationPolicy.
 * 
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 1.0
 */
public class Datacenter extends SimEntity {

    /** The characteristics. */
    private DatacenterCharacteristics characteristics;

    /** The regional cis name. */
    private String regionalCisName;

    /** The vm provisioner. */
    private VmAllocationPolicy vmAllocationPolicy;

    /** The last process time. */
    private double lastProcessTime;

    /** The debts. */
    private Map<Integer, Double> debts;

    /** The storage list. */
    private List<Storage> storageList;

    /** The scheduling interval. */
    private double schedulingInterval;

    private int timesCalled = 0;

    /**
     * Allocates a new PowerDatacenter object.
     * 
     * @param name
     *        the name to be associated with this entity (as required by
     *        Sim_entity class from simjava package)
     * @param characteristics
     *        an object of DatacenterCharacteristics
     * @param storageList
     *        a LinkedList of storage elements, for data simulation
     * @param vmAllocationPolicy
     *        the vmAllocationPolicy
     * 
     * @throws Exception
     *         This happens when one of the following scenarios occur:
     *         <ul>
     *         <li>creating this entity before initializing CloudSim package
     *         <li>this entity name is <tt>null</tt> or empty
     *         <li>this entity has <tt>zero</tt> number of PEs (Processing
     *         Elements). <br>
     *         No PEs mean the Cloudlets can't be processed. A CloudResource
     *         must contain one or more Machines. A Machine must contain one or
     *         more PEs.
     *         </ul>
     * 
     * @pre name != null
     * @pre resource != null
     * @post $none
     */
    public Datacenter(String name, DatacenterCharacteristics characteristics,
	    VmAllocationPolicy vmAllocationPolicy, List<Storage> storageList,
	    double schedulingInterval) {
	super(name);

	setCharacteristics(characteristics);
	setVmAllocationPolicy(vmAllocationPolicy);
	setLastProcessTime(0.0);
	setDebts(new HashMap<Integer, Double>());
	setStorageList(storageList);
	setSchedulingInterval(schedulingInterval);

	// If this resource doesn't have any PEs then no useful at all
	if (getCharacteristics().getPesNumber() == 0) {
	    throw new RuntimeException(super.getName()
		    + " : Error - this entity has no PEs. Therefore, can't process any Cloudlets.");
	}

	// stores id of this class
	getCharacteristics().setId(super.getId());
    }

    /**
     * Processes events or services that are available for this PowerDatacenter.
     * 
     * @param ev
     *        a Sim_event object
     * 
     * @pre ev != null
     * @post $none
     */
    @Override
    public void processEvent(SimEvent ev) {
	int srcId = -1;

	switch ((CloudSimTags) ev.getTag()) {
	// Resource characteristics inquiry
	case RESOURCE_CHARACTERISTICS:
	    srcId = ((Integer) ev.getData()).intValue();
	    sendNow(srcId, ev.getTag(), getCharacteristics());
	    break;

	// Resource dynamic info inquiry
	case RESOURCE_DYNAMICS:
	    srcId = ((Integer) ev.getData()).intValue();
	    sendNow(srcId, ev.getTag(), 0);
	    break;

	case RESOURCE_NUM_PE:
	    srcId = ((Integer) ev.getData()).intValue();
	    int numPE = getCharacteristics().getPesNumber();
	    sendNow(srcId, ev.getTag(), numPE);
	    break;

	case RESOURCE_NUM_FREE_PE:
	    srcId = ((Integer) ev.getData()).intValue();
	    int freePesNumber = getCharacteristics().getFreePesNumber();
	    sendNow(srcId, ev.getTag(), freePesNumber);
	    break;

	// New Cloudlet arrives
	case CLOUDLET_SUBMIT:
	    processCloudletSubmit(ev, false);
	    break;

	// New Cloudlet arrives, but the sender asks for an ack
	case CLOUDLET_SUBMIT_ACK:
	    processCloudletSubmit(ev, true);
	    break;

	// Cancels a previously submitted Cloudlet
	case CLOUDLET_CANCEL:
	    processCloudlet(ev, CloudSimTags.CLOUDLET_CANCEL);
	    break;

	// Pauses a previously submitted Cloudlet
	case CLOUDLET_PAUSE:
	    processCloudlet(ev, CloudSimTags.CLOUDLET_PAUSE);
	    break;

	// Pauses a previously submitted Cloudlet, but the sender
	// asks for an acknowledgement
	case CLOUDLET_PAUSE_ACK:
	    processCloudlet(ev, CloudSimTags.CLOUDLET_PAUSE_ACK);
	    break;

	// Resumes a previously submitted Cloudlet
	case CLOUDLET_RESUME:
	    processCloudlet(ev, CloudSimTags.CLOUDLET_RESUME);
	    break;

	// Resumes a previously submitted Cloudlet, but the sender
	// asks for an acknowledgement
	case CLOUDLET_RESUME_ACK:
	    processCloudlet(ev, CloudSimTags.CLOUDLET_RESUME_ACK);
	    break;

	// Moves a previously submitted Cloudlet to a different resource
	case CLOUDLET_MOVE:
	    processCloudletMove((int[]) ev.getData(), CloudSimTags.CLOUDLET_MOVE);
	    break;

	// Moves a previously submitted Cloudlet to a different resource
	case CLOUDLET_MOVE_ACK:
	    processCloudletMove((int[]) ev.getData(), CloudSimTags.CLOUDLET_MOVE_ACK);
	    break;

	// Checks the status of a Cloudlet
	case CLOUDLET_STATUS:
	    processCloudletStatus(ev);
	    break;

	case VM_CREATE:
	    processVmCreate(ev, false);
	    break;

	case VM_CREATE_ACK:
	    processVmCreate(ev, true);
	    break;

	case VM_DESTROY:
	    processVmDestroy(ev, false);
	    break;

	case VM_DESTROY_ACK:
	    processVmDestroy(ev, true);
	    break;

	case VM_MIGRATE:
	    processVmMigrate(ev, false);
	    break;

	case VM_PAUSE:
	    processVmPause(ev, false);
	    break;

	case VM_PAUSE_ACK:
	    processVmPause(ev, true);
	    break;

	case VM_RESUME:
	    processVmResume(ev, false);
	    break;

	case VM_RESUME_ACK:
	    processVmResume(ev, true);
	    break;

	case VM_MIGRATE_ACK:
	    processVmMigrate(ev, true);
	    break;

	case VM_DATA_ADD:
	    processDataAdd(ev, false);
	    break;

	case VM_DATA_ADD_ACK:
	    processDataAdd(ev, true);
	    break;

	case VM_DATA_DEL:
	    processDataDelete(ev, false);
	    break;

	case VM_DATA_DEL_ACK:
	    processDataDelete(ev, true);
	    break;

	case VM_DATACENTER_EVENT:
	    this.timesCalled++;
	    final Vm vm = (Vm) ev.getData();
	    updateCloudletProcessing(vm);
	    checkCloudletCompletion(vm);
	    setLastProcessTime(CloudSim.clock());
	    break;
	// other unknown tags are processed by this method
	default:
	    processOtherEvent(ev);
	    break;
	}
    }

    /**
     * Process data del.
     * 
     * @param ev
     *        the ev
     * @param ack
     *        the ack
     */
    protected void processDataDelete(SimEvent ev, boolean ack) {
	if (ev == null) {
	    return;
	}

	Object[] data = (Object[]) ev.getData();
	if (data == null) {
	    return;
	}

	String filename = (String) data[0];
	int req_source = ((Integer) data[1]).intValue();
	DataCloudTags tag;

	// check if this file can be deleted (do not delete is right now)
	DataCloudTags msg = deleteFileFromStorage(filename);
	if (msg == DataCloudTags.FILE_DELETE_SUCCESSFUL) {
	    tag = DataCloudTags.CTLG_DELETE_MASTER;
	} else { // if an error occured, notify user
	    tag = DataCloudTags.FILE_DELETE_MASTER_RESULT;
	}

	if (ack) {
	    // send back to sender
	    Object pack[] = new Object[2];
	    pack[0] = filename;
	    pack[1] = msg;

	    sendNow(req_source, tag, pack);
	}
    }

    /**
     * Process data add.
     * 
     * @param ev
     *        the ev
     * @param ack
     *        the ack
     */
    protected void processDataAdd(SimEvent ev, boolean ack) {
	if (ev == null) {
	    return;
	}

	Object[] pack = (Object[]) ev.getData();
	if (pack == null) {
	    return;
	}

	File file = (File) pack[0]; // get the file
	file.setMasterCopy(true); // set the file into a master copy
	int sentFrom = ((Integer) pack[1]).intValue(); // get sender ID

	/******
	 * // DEBUG Log.printLine(super.get_name() + ".addMasterFile(): " +
	 * file.getName() + " from " + CloudSim.getEntityName(sentFrom));
	 *******/

	Object[] data = new Object[3];
	data[0] = file.getName();

	DataCloudTags msg = addFile(file); // add the file

	double debit;
	if (getDebts().containsKey(sentFrom)) {
	    debit = getDebts().get(sentFrom);
	} else {
	    debit = 0.0;
	}

	debit += getCharacteristics().getCostPerBw() * file.getSize();

	getDebts().put(sentFrom, debit);

	if (ack) {
	    data[1] = Integer.valueOf(-1); // no sender id
	    data[2] = msg; // the result of adding a master
	    // file
	    sendNow(sentFrom, DataCloudTags.FILE_ADD_MASTER_RESULT, data);
	}
    }

    /**
     * Process the event for an User/Broker who wants to know the status of a
     * Cloudlet. This PowerDatacenter will then send the status back to the
     * User/Broker.
     * 
     * @param ev
     *        a Sim_event object
     * 
     * @pre ev != null
     * @post $none
     */
    protected void processCloudletStatus(SimEvent ev) {
	int cloudletId = 0;
	int userId = 0;
	int vmId = 0;
	CloudletStatus status = null;

	try {
	    // if a sender using cloudletXXX() methods
	    int data[] = (int[]) ev.getData();
	    cloudletId = data[0];
	    userId = data[1];
	    vmId = data[2];

	    status = getVmAllocationPolicy().getHost(vmId).getVm(userId)
		.getCloudletStatus(cloudletId);
	}

	// if a sender using normal send() methods
	catch (ClassCastException c) {
	    try {
		Cloudlet cl = (Cloudlet) ev.getData();
		cloudletId = cl.getCloudletId();
		userId = cl.getUserId();

		status = getVmAllocationPolicy().getHost(vmId).getVm(userId)
		    .getCloudletStatus(cloudletId);
	    } catch (Exception e) {
		Log.logger.info("["
			+ Double.toString(CloudSim.clock())
			+ "] "
			+ getName()
			+ ": Error in processing CloudSimTags.CLOUDLET_STATUS");
		Log.logger.info("[" + Double.toString(CloudSim.clock()) + "] " + e.getMessage());
		return;
	    }
	} catch (Exception e) {
	    Log.logger.info("["
		    + Double.toString(CloudSim.clock())
		    + "] "
		    + getName()
		    + ": Error in processing CloudSimTags.CLOUDLET_STATUS");
	    Log.logger.info("[" + Double.toString(CloudSim.clock()) + "] " + e.getMessage());
	    return;
	}

	int[] array = new int[3];
	array[0] = getId();
	array[1] = cloudletId;
	array[2] = status.ordinal();

	sendNow(userId, CloudSimTags.CLOUDLET_STATUS, array);
    }

    /**
     * Here all the method related to VM requests will be received and forwarded
     * to the related method.
     * 
     * @param ev
     *        the received event
     * 
     * @pre $none
     * @post $none
     */
    protected void processOtherEvent(SimEvent ev) {
	throw new IllegalArgumentException("Datacenter: Unexpected event "
		+ (ev == null ? "null" : ev.getTag()));
    }

    /**
     * Process the event for an User/Broker who wants to create a VM in this
     * PowerDatacenter. This PowerDatacenter will then send the status back to
     * the User/Broker.
     * 
     * @param ev
     *        a Sim_event object
     * @param ack
     *        the ack
     * 
     * @pre ev != null
     * @post $none
     */
    protected void processVmCreate(SimEvent ev, boolean ack) {

	Vm vm = (Vm) ev.getData();

	boolean result = getVmAllocationPolicy().allocateHostForVm(vm);

	if (Log.logger.isLoggable(Level.FINE)) {
	    Log.logger.fine(Log.clock()
		    + " Datacenter creating VM "
		    + vm.getId()
		    + ", user:"
		    + vm.getUserId()
		    + ", host: "
		    + vm.getHost()
		    + ", success: "
		    + result);
	}

	if (ack) {
	    int[] data = new int[3];
	    data[0] = getId();
	    data[1] = vm.getId();

	    if (result) {
		data[2] = 1;
	    } else {
		data[2] = 0;
	    }
	    sendNow(vm.getUserId(), CloudSimTags.VM_CREATE_ACK, data);
	}

	if (result) {
	    double amount = 0.0;
	    if (getDebts().containsKey(vm.getUserId())) {
		amount = getDebts().get(vm.getUserId());
	    }
	    amount += getCharacteristics().getCostPerMem() * vm.getRam();
	    amount += getCharacteristics().getCostPerStorage() * vm.getSize();

	    getDebts().put(vm.getUserId(), amount);
	}
    }

    /**
     * Process the event for an User/Broker who wants to destroy a VM previously
     * created in this PowerDatacenter. This PowerDatacenter may send, upon
     * request, the status back to the User/Broker.
     * 
     * @param ev
     *        a Sim_event object
     * @param ack
     *        the ack
     * 
     * @pre ev != null
     * @post $none
     */
    protected void processVmDestroy(SimEvent ev, boolean ack) {
	Vm vm = (Vm) ev.getData();
	vm.cancelAllCloudlets();
	getVmAllocationPolicy().deallocateHostForVm(vm);

	if (ack) {
	    int[] data = new int[3];
	    data[0] = getId();
	    data[1] = vm.getId();
	    data[2] = 1;

	    sendNow(vm.getUserId(), CloudSimTags.VM_DESTROY_ACK, data);
	}
    }

    protected void processVmPause(SimEvent ev, boolean ack) {

	Vm vm = (Vm) ev.getData();
	checkCloudletCompletion(vm);
	vm.pause();
	if (ack) {
	    int[] data = new int[3];
	    data[0] = getId();
	    data[1] = vm.getId();
	    data[2] = 1;
	    send(vm.getUserId(), 1, CloudSimTags.VM_PAUSE_ACK, data);
	}
    }

    protected void processVmResume(SimEvent ev, boolean ack) {

	Vm vm = (Vm) ev.getData();
	vm.resume();
	if (ack) {
	    int[] data = new int[3];
	    data[0] = getId();
	    data[1] = vm.getId();
	    data[2] = 1;
	    send(vm.getUserId(), 1, CloudSimTags.VM_RESUME_ACK, data);
	}
    }

    protected void processVmMigrate(SimEvent ev, boolean ack) {
	Object tmp = ev.getData();
	if (!(tmp instanceof Map<?, ?>)) {
	    throw new ClassCastException("The data object must be Map<String, Object>");
	}

	@SuppressWarnings("unchecked")
	Map<String, Object> migrate = (HashMap<String, Object>) tmp;

	Vm vm = (Vm) migrate.get("vm");
	Host host = (Host) migrate.get("host");

	getVmAllocationPolicy().deallocateHostForVm(vm);
	host.removeMigratingInVm(vm);
	boolean result = getVmAllocationPolicy().allocateHostForVm(vm, host);
	if (!result) {
	    if (Log.logger.isLoggable(Level.FINE)) {
		Log.logger.fine(Log.clock() + "Allocation failed");
	    }
	}

	if (ack) {
	    int[] data = new int[3];
	    data[0] = getId();
	    data[1] = vm.getId();

	    if (result) {
		data[2] = 1;
	    } else {
		data[2] = 0;
	    }
	    sendNow(ev.getSource(), CloudSimTags.VM_CREATE_ACK, data);
	}

	double amount = 0.0;
	if (this.debts.containsKey(vm.getUserId())) {
	    amount = this.debts.get(vm.getUserId());
	}

	amount += getCharacteristics().getCostPerMem() * vm.getRam();
	amount += getCharacteristics().getCostPerStorage() * vm.getSize();

	this.debts.put(vm.getUserId(), amount);

	Log.formatLine("%.2f: Migration of VM #%d to Host #%d is completed", CloudSim.clock(),
	    vm.getId(), host.getId());
	vm.setInMigration(false);
    }

    /**
     * Processes a Cloudlet based on the event type.
     * 
     * @param ev
     *        a Sim_event object
     * @param type
     *        event type
     * 
     * @pre ev != null
     * @pre type > 0
     * @post $none
     */
    protected void processCloudlet(SimEvent ev, CloudSimTags type) {
	int cloudletId = 0;
	int userId = 0;
	int vmId = 0;

	try { // if the sender using cloudletXXX() methods
	    int data[] = (int[]) ev.getData();
	    cloudletId = data[0];
	    userId = data[1];
	    vmId = data[2];
	}

	// if the sender using normal send() methods
	catch (ClassCastException c) {
	    try {
		Cloudlet cl = (Cloudlet) ev.getData();
		cloudletId = cl.getCloudletId();
		userId = cl.getUserId();
		vmId = cl.getVmId();
	    } catch (Exception e) {
		Log.logger.info("["
			+ Double.toString(CloudSim.clock())
			+ "] "
			+ super.getName()
			+ ": Error in processing Cloudlet");
		Log.logger.info("[" + Double.toString(CloudSim.clock()) + "] " + e.getMessage());
		return;
	    }
	} catch (Exception e) {
	    Log.logger.info("["
		    + Double.toString(CloudSim.clock())
		    + "] "
		    + super.getName()
		    + ": Error in processing a Cloudlet.");
	    Log.logger.info("[" + Double.toString(CloudSim.clock()) + "] " + e.getMessage());
	    return;
	}

	// begins executing ....
	switch (type) {
	case CLOUDLET_CANCEL:
	    processCloudletCancel(cloudletId, userId, vmId);
	    break;

	case CLOUDLET_PAUSE:
	    processCloudletPause(cloudletId, userId, vmId, false);
	    break;

	case CLOUDLET_PAUSE_ACK:
	    processCloudletPause(cloudletId, userId, vmId, true);
	    break;

	case CLOUDLET_RESUME:
	    processCloudletResume(cloudletId, userId, vmId, false);
	    break;

	case CLOUDLET_RESUME_ACK:
	    processCloudletResume(cloudletId, userId, vmId, true);
	    break;
	default:
	    break;
	}

    }

    /**
     * Process the event for an User/Broker who wants to move a Cloudlet.
     * 
     * @param receivedData
     *        information about the migration
     * @param cloudletMove
     *        event tag
     * 
     * @pre receivedData != null
     * @pre type > 0
     * @post $none
     */
    protected void processCloudletMove(int[] receivedData, CloudSimTags type) {

	int[] array = receivedData;
	int cloudletId = array[0];
	int userId = array[1];
	int vmId = array[2];
	int vmDestId = array[3];
	int destId = array[4];

	// get the cloudlet
	final Vm sourceVm = getVmAllocationPolicy().getHost(vmId).getVm(vmId);
	Cloudlet cl = sourceVm.cloudletCancel(cloudletId);

	boolean failed = false;
	if (cl == null) {// cloudlet doesn't exist
	    failed = true;
	} else {
	    // has the cloudlet already finished?
	    if (cl.getStatus() == CloudletStatus.SUCCESS) {// if yes, send it
		// back to user
		int[] data = new int[3];
		data[0] = getId();
		data[1] = cloudletId;
		data[2] = 0;
		sendNow(cl.getUserId(), CloudSimTags.CLOUDLET_SUBMIT_ACK, data);
		sendNow(cl.getUserId(), CloudSimTags.CLOUDLET_RETURN, cl);
	    }

	    // prepare cloudlet for migration
	    cl.setVmId(vmDestId);

	    // the cloudlet will migrate from one vm to another does the
	    // destination VM exist?
	    if (destId == getId()) { // same datacenter
		Vm vm = getVmAllocationPolicy().getHost(vmDestId).getVm(userId);
		if (vm == null) {
		    failed = true;
		} else {
		    long fileTransferTime = predictFileTransferTime(cl.getRequiredFiles()); // time
		    // to
		    // transfer
		    // the
		    // files
		    vm.cloudletSubmit(cl);
		}
	    } else {// the cloudlet will migrate from one resource to another
		CloudSimTags tag = type.equals(CloudSimTags.CLOUDLET_MOVE_ACK) ? CloudSimTags.CLOUDLET_SUBMIT_ACK
			: CloudSimTags.CLOUDLET_SUBMIT;
		sendNow(destId, tag, cl);
	    }
	}

	if (type == CloudSimTags.CLOUDLET_MOVE_ACK) {// send ACK if
						     // requested
	    int[] data = new int[3];
	    data[0] = getId();
	    data[1] = cloudletId;
	    if (failed) {
		data[2] = 0;
	    } else {
		data[2] = 1;
	    }
	    sendNow(cl.getUserId(), CloudSimTags.CLOUDLET_SUBMIT_ACK, data);
	}
    }

    /**
     * Processes a Cloudlet submission.
     * 
     * @param ev
     *        a SimEvent object
     * @param ack
     *        an acknowledgement
     * 
     * @pre ev != null
     * @post $none
     */
    protected void processCloudletSubmit(SimEvent ev, boolean ack) {

	try {
	    // gets the Cloudlet object
	    Cloudlet cl = (Cloudlet) ev.getData();

	    // checks whether this Cloudlet has finished or not
	    if (cl.isFinished()) {
		String name = CloudSim.getEntityName(cl.getUserId());
		Log.logger
		    .warning(Log.clock()
			    + getName()
			    + ": Warning - Cloudlet #"
			    + cl.getCloudletId()
			    + " owned by "
			    + name
			    + " is already completed/finished therefore, it is not being executed again");

		// NOTE: If a Cloudlet has finished, then it won't be processed.
		// So, if ack is required, this method sends back a result.
		// If ack is not required, this method don't send back a result.
		// Hence, this might cause CloudSim to be hanged since waiting
		// for this Cloudlet back.
		if (ack) {
		    int[] data = new int[3];
		    data[0] = getId();
		    data[1] = cl.getCloudletId();
		    data[2] = 0;

		    // unique tag = operation tag
		    sendNow(cl.getUserId(), CloudSimTags.CLOUDLET_SUBMIT_ACK, data);
		}

		sendNow(cl.getUserId(), CloudSimTags.CLOUDLET_RETURN, cl);

		return;
	    }

	    // process this Cloudlet to this CloudResource
	    cl.setResourceParameter(getId(), getCharacteristics().getCostPerSecond(),
		getCharacteristics().getCostPerBw());

	    int vmId = cl.getVmId();

	    Host host = getVmAllocationPolicy().getHost(vmId);
	    Vm vm = host.getVm(vmId);
	    long estimatedRunTime = vm.cloudletSubmit(cl);
	    
	    if (Log.logger.isLoggable(Level.FINE)) {
		Log.logger.fine(Log.clock()
			+ "Running cloudlet: "
			+ cl.getCloudletId()
			+ " on VM "
			+ vmId
			+ ". Length: "
			+ cl.getLength()
			+ ", estimated runtime: "
			+ estimatedRunTime);
	    }
	    if (estimatedRunTime > 0L) { // if this cloudlet is in the exec
		nextInternalEvent(estimatedRunTime, vm);
	    }

	    if (ack) {
		int[] data = new int[3];
		data[0] = getId();
		data[1] = cl.getCloudletId();
		data[2] = 1;

		// unique tag = operation tag
		sendNow(cl.getUserId(), CloudSimTags.CLOUDLET_SUBMIT_ACK, data);
	    }
	} catch (ClassCastException c) {
	    Log.logger.info("["
		    + Double.toString(CloudSim.clock())
		    + "] "
		    + getName()
		    + ".processCloudletSubmit(): "
		    + "ClassCastException error.");
	    c.printStackTrace();
	}
    }

    /**
     * Predict file transfer time.
     * 
     * @param requiredFiles
     *        the required files
     * 
     * @return the double
     */
    protected long predictFileTransferTime(List<String> requiredFiles) {
	long time = 0L;

	Iterator<String> iter = requiredFiles.iterator();
	while (iter.hasNext()) {
	    String fileName = iter.next();
	    for (int i = 0; i < getStorageList().size(); i++) {
		Storage tempStorage = getStorageList().get(i);
		File tempFile = tempStorage.getFile(fileName);
		if (tempFile != null) {
		    time += (long) (tempFile.getSize() / tempStorage.getMaxTransferRate());
		    break;
		}
	    }
	}
	return time;
    }

    /**
     * Processes a Cloudlet resume request.
     * 
     * @param cloudletId
     *        resuming cloudlet ID
     * @param userId
     *        ID of the cloudlet's owner
     * @param ack
     *        $true if an ack is requested after operation
     * @param vmId
     *        the vm id
     * 
     * @pre $none
     * @post $none
     */
    protected void processCloudletResume(int cloudletId, int userId, int vmId, boolean ack) {

	final Vm vm = getVmAllocationPolicy().getHost(vmId).getVm(vmId);
	long estRuntime = vm.cloudletResume(cloudletId);

	boolean status = false;
	if (estRuntime > 0) { // if this cloudlet is in the exec queue
	    status = true;
	    if (estRuntime > CloudSim.clock()) {
		nextInternalEvent(estRuntime, vm);
	    }
	}

	if (ack) {
	    int[] data = new int[3];
	    data[0] = getId();
	    data[1] = cloudletId;
	    if (status) {
		data[2] = 1;
	    } else {
		data[2] = 0;
	    }
	    sendNow(userId, CloudSimTags.CLOUDLET_RESUME_ACK, data);
	}
    }

    private void nextInternalEvent(long delay, Vm vm) {
	schedule(getId(), delay, CloudSimTags.VM_DATACENTER_EVENT, vm);
    }

    /**
     * Processes a Cloudlet pause request.
     * 
     * @param cloudletId
     *        resuming cloudlet ID
     * @param userId
     *        ID of the cloudlet's owner
     * @param ack
     *        $true if an ack is requested after operation
     * @param vmId
     *        the vm id
     * 
     * @pre $none
     * @post $none
     */
    protected void processCloudletPause(int cloudletId, int userId, int vmId, boolean ack) {
	final Vm vm = getVmAllocationPolicy().getHost(vmId).getVm(vmId);
	boolean status = vm.cloudletPause(cloudletId);

	if (ack) {
	    int[] data = new int[3];
	    data[0] = getId();
	    data[1] = cloudletId;
	    if (status) {
		data[2] = 1;
	    } else {
		data[2] = 0;
	    }
	    sendNow(userId, CloudSimTags.CLOUDLET_PAUSE_ACK, data);
	}
    }

    /**
     * Processes a Cloudlet cancel request.
     * 
     * @param cloudletId
     *        resuming cloudlet ID
     * @param userId
     *        ID of the cloudlet's owner
     * @param vmId
     *        the vm id
     * 
     * @pre $none
     * @post $none
     */
    protected void processCloudletCancel(int cloudletId, int userId, int vmId) {
	getVmAllocationPolicy().getHost(vmId).getVm(vmId).cloudletCancel(cloudletId);
    }

    /**
     * Updates processing of each cloudlet running in this PowerDatacenter. It
     * is necessary because Hosts and VirtualMachines are simple objects, not
     * entities. So, they don't receive events and updating cloudlets inside
     * them must be called from the outside.
     * 
     * @pre $none
     * @post $none
     * @param vm
     *        TODO
     */
    protected void updateCloudletProcessing(Vm vm) {

	long nextEvent = vm.updateProcessing();
	if (nextEvent > 0) {
	    nextInternalEvent(nextEvent - CloudSim.clock(), vm);
	}
    }

    private void checkCloudletCompletion(Vm vm) {
	if (vm.areThereFinishedCloudlets()) {
	    List<Cloudlet> finishedCloudlets = vm.getAndRemoveFinishedCloutlets();
	    for (Cloudlet cl : finishedCloudlets) {
		sendNow(cl.getUserId(), CloudSimTags.CLOUDLET_RETURN, cl);
	    }
	}
    }

    /**
     * Adds a file into the resource's storage before the experiment starts. If
     * the file is a master file, then it will be registered to the RC when the
     * experiment begins.
     * 
     * @param file
     *        a DataCloud file
     * 
     * @return a tag number denoting whether this operation is a success or not
     * 
     * @see CloudSim.datagrid.DataCloudTags#FILE_ADD_SUCCESSFUL
     * @see CloudSim.datagrid.DataCloudTags#FILE_ADD_ERROR_EMPTY
     */
    public DataCloudTags addFile(File file) {
	if (file == null) {
	    return DataCloudTags.FILE_ADD_ERROR_EMPTY;
	}

	if (contains(file.getName())) {
	    return DataCloudTags.FILE_ADD_ERROR_EXIST_READ_ONLY;
	}

	// check storage space first
	if (getStorageList().size() <= 0) {
	    return DataCloudTags.FILE_ADD_ERROR_STORAGE_FULL;
	}

	Storage tempStorage = null;
	DataCloudTags msg = DataCloudTags.FILE_ADD_ERROR_STORAGE_FULL;

	for (int i = 0; i < getStorageList().size(); i++) {
	    tempStorage = getStorageList().get(i);
	    if (tempStorage.getAvailableSpace() >= file.getSize()) {
		tempStorage.addFile(file);
		msg = DataCloudTags.FILE_ADD_SUCCESSFUL;
		break;
	    }
	}

	return msg;
    }

    /**
     * Checks whether the resource has the given file.
     * 
     * @param file
     *        a file to be searched
     * 
     * @return <tt>true</tt> if successful, <tt>false</tt> otherwise
     */
    protected boolean contains(File file) {
	if (file == null) {
	    return false;
	}
	return contains(file.getName());
    }

    /**
     * Checks whether the resource has the given file.
     * 
     * @param fileName
     *        a file name to be searched
     * 
     * @return <tt>true</tt> if successful, <tt>false</tt> otherwise
     */
    protected boolean contains(String fileName) {
	if (fileName == null || fileName.length() == 0) {
	    return false;
	}

	Iterator<Storage> it = getStorageList().iterator();
	Storage storage = null;
	boolean result = false;

	while (it.hasNext()) {
	    storage = it.next();
	    if (storage.contains(fileName)) {
		result = true;
		break;
	    }
	}

	return result;
    }

    /**
     * Deletes the file from the storage. Also, check whether it is possible to
     * delete the file from the storage.
     * 
     * @param fileName
     *        the name of the file to be deleted
     * 
     * @return the error message as defined in
     *         {@link CloudSim.datagrid.DataCloudTags}
     * 
     * @see CloudSim.datagrid.DataCloudTags#FILE_DELETE_SUCCESSFUL
     * @see CloudSim.datagrid.DataCloudTags#FILE_DELETE_ERROR_ACCESS_DENIED
     * @see CloudSim.datagrid.DataCloudTags#FILE_DELETE_ERROR
     */
    private DataCloudTags deleteFileFromStorage(String fileName) {
	Storage tempStorage = null;
	File tempFile = null;
	DataCloudTags msg = DataCloudTags.FILE_DELETE_ERROR;

	for (int i = 0; i < getStorageList().size(); i++) {
	    tempStorage = getStorageList().get(i);
	    tempFile = tempStorage.getFile(fileName);
	    tempStorage.deleteFile(fileName, tempFile);
	    msg = DataCloudTags.FILE_DELETE_SUCCESSFUL;
	} // end for

	return msg;
    }

    /**
     * Prints the debts.
     */
    public void printDebts() {
	Log.logger.info("["
		+ Double.toString(CloudSim.clock())
		+ "] "
		+ "*****PowerDatacenter: "
		+ getName()
		+ "*****");
	Log.logger.info("[" + Double.toString(CloudSim.clock()) + "] " + "User id\t\tDebt");

	Set<Integer> keys = getDebts().keySet();
	Iterator<Integer> iter = keys.iterator();
	DecimalFormat df = new DecimalFormat("#.##");
	while (iter.hasNext()) {
	    int key = iter.next();
	    double value = getDebts().get(key);
	    Log.logger.info("["
		    + Double.toString(CloudSim.clock())
		    + "] "
		    + key
		    + "\t\t"
		    + df.format(value));
	}
	Log.logger.info("["
		+ Double.toString(CloudSim.clock())
		+ "] "
		+ "**********************************");
    }

    /*
     * (non-Javadoc)
     * 
     * @see cloudsim.core.SimEntity#shutdownEntity()
     */
    @Override
    public void shutdownEntity() {
	Log.logger.warning(Log.clock()
		+ getName()
		+ " is shutting down... Times called "
		+ this.timesCalled);
    }

    /*
     * (non-Javadoc)
     * 
     * @see cloudsim.core.SimEntity#startEntity()
     */
    @Override
    public void startEntity() {
	// this resource should register to regional GIS.
	// However, if not specified, then register to system GIS (the
	// default CloudInformationService) entity.
	int gisID = CloudSim.getEntityId(this.regionalCisName);
	if (gisID == -1) {
	    gisID = CloudSim.getCloudInfoServiceEntityId();
	}

	// send the registration to GIS
	sendNow(gisID, CloudSimTags.REGISTER_RESOURCE, getId());
    }

    /**
     * Gets the host list.
     * 
     * @return the host list
     */
    @SuppressWarnings("unchecked")
    public <T extends Host> List<T> getHostList() {
	return (List<T>) getCharacteristics().getHostList();
    }

    /**
     * Gets the characteristics.
     * 
     * @return the characteristics
     */
    protected DatacenterCharacteristics getCharacteristics() {
	return this.characteristics;
    }

    /**
     * Sets the characteristics.
     * 
     * @param characteristics
     *        the new characteristics
     */
    protected void setCharacteristics(DatacenterCharacteristics characteristics) {
	this.characteristics = characteristics;
    }

    /**
     * Gets the regional cis name.
     * 
     * @return the regional cis name
     */
    protected String getRegionalCisName() {
	return this.regionalCisName;
    }

    /**
     * Sets the regional cis name.
     * 
     * @param regionalCisName
     *        the new regional cis name
     */
    protected void setRegionalCisName(String regionalCisName) {
	this.regionalCisName = regionalCisName;
    }

    /**
     * Gets the vm allocation policy.
     * 
     * @return the vm allocation policy
     */
    public VmAllocationPolicy getVmAllocationPolicy() {
	return this.vmAllocationPolicy;
    }

    /**
     * Sets the vm allocation policy.
     * 
     * @param vmAllocationPolicy
     *        the new vm allocation policy
     */
    protected void setVmAllocationPolicy(VmAllocationPolicy vmAllocationPolicy) {
	this.vmAllocationPolicy = vmAllocationPolicy;
    }

    /**
     * Gets the last process time.
     * 
     * @return the last process time
     */
    protected double getLastProcessTime() {
	return this.lastProcessTime;
    }

    /**
     * Sets the last process time.
     * 
     * @param lastProcessTime
     *        the new last process time
     */
    protected void setLastProcessTime(double lastProcessTime) {
	this.lastProcessTime = lastProcessTime;
    }

    /**
     * Gets the debts.
     * 
     * @return the debts
     */
    protected Map<Integer, Double> getDebts() {
	return this.debts;
    }

    /**
     * Sets the debts.
     * 
     * @param debts
     *        the debts
     */
    protected void setDebts(Map<Integer, Double> debts) {
	this.debts = debts;
    }

    /**
     * Gets the storage list.
     * 
     * @return the storage list
     */
    protected List<Storage> getStorageList() {
	return this.storageList;
    }

    /**
     * Sets the storage list.
     * 
     * @param storageList
     *        the new storage list
     */
    protected void setStorageList(List<Storage> storageList) {
	this.storageList = storageList;
    }

    /**
     * Gets the scheduling interval.
     * 
     * @return the scheduling interval
     */
    protected double getSchedulingInterval() {
	return this.schedulingInterval;
    }

    /**
     * Sets the scheduling interval.
     * 
     * @param schedulingInterval
     *        the new scheduling interval
     */
    protected void setSchedulingInterval(double schedulingInterval) {
	this.schedulingInterval = schedulingInterval;
    }

}
