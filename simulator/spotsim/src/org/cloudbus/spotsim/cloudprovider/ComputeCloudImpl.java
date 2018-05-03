package org.cloudbus.spotsim.cloudprovider;

import static org.cloudbus.spotsim.cloudprovider.instance.InstanceState.*;

import java.io.File;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.EventTag;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.util.workload.PerformanceVariatorEnum;
import org.cloudbus.cloudsim.util.workload.Task;
import org.cloudbus.cloudsim.util.workload.UniformPerformanceVariator;
import org.cloudbus.spotsim.ComputeCloudTags;
import org.cloudbus.spotsim.PriceChangeEvent;
import org.cloudbus.spotsim.broker.ModelParameters;
import org.cloudbus.spotsim.cloudprovider.instance.DatacenterManager;
import org.cloudbus.spotsim.cloudprovider.instance.Instance;
import org.cloudbus.spotsim.enums.AZ;
import org.cloudbus.spotsim.enums.FaultToleranceMethod;
import org.cloudbus.spotsim.enums.InstanceType;
import org.cloudbus.spotsim.enums.OS;
import org.cloudbus.spotsim.enums.PriceModel;
import org.cloudbus.spotsim.enums.Region;
import org.cloudbus.spotsim.main.config.SimProperties;
import org.cloudbus.spotsim.payloads.ChangeBidRequest;
import org.cloudbus.spotsim.payloads.CkptInstanceRequest;
import org.cloudbus.spotsim.payloads.CloudRequest;
import org.cloudbus.spotsim.payloads.InstanceCreatedNotification;
import org.cloudbus.spotsim.payloads.InstanceTerminatedNotification;
import org.cloudbus.spotsim.payloads.RunInstancesRequest;
import org.cloudbus.spotsim.payloads.RunTaskRequest;
import org.cloudbus.spotsim.pricing.Accounting;
import org.cloudbus.spotsim.pricing.PriceRecord;
import org.cloudbus.spotsim.pricing.db.PriceDB;
import org.cloudbus.spotsim.simrecords.SimulationData;

/**
 * @author William Voorsluys - williamvoor@gmail.com
 * 
 *         Simulates a compute cloud, much like Amazon's EC2. Main methods:
 *         {@link #runInstance(int, RunInstancesRequest)} (creates an instance)
 *         {@link #terminateInstances(int, Long...) (terminates one or more
 *         instances)
 *         
 *         @see ComputeCloudImplTest
 */
public class ComputeCloudImpl extends SimEntity {

    private final Map<Integer, Task> cloudlet2task;

    private final Map<AZ, DatacenterManager> datacenters;

    private int nextCloudletID = 0;

    private final Map<Long, Integer> request2instanceId;

    private final Map<Integer, AZ> instanceId2datacenterId;

    private final Region region;

    private final Set<Integer> connectedClients;
    
    /*FileWriter fw;
 	PrintWriter out;*/

    public ComputeCloudImpl(final int hostsPerDatacenter, final Region region) {
	super("Cloud");
	this.region = region;
	this.datacenters = new EnumMap<AZ, DatacenterManager>(AZ.class);
	createDataCenters(hostsPerDatacenter);
	this.cloudlet2task = new HashMap<Integer, Task>();
	this.request2instanceId = new HashMap<Long, Integer>();
	this.instanceId2datacenterId = new HashMap<Integer, AZ>();
	this.connectedClients = new HashSet<Integer>();
	
    /*try {
		fw = new FileWriter(getOutputFileName());
		out = new PrintWriter(fw);
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}*/
    }

    public static long calcNewLength(final Task t, final InstanceType type, final long length) {
	final int processors = type.getEc2units();
	double taskLength = Math.ceil(ModelParameters.execTimeParallel(t.getJob().getA(), t.getJob()
		    .getSigma(), processors, length));
		if (SimProperties.PERFORMANCE_VARIATION_FLAG.asBoolean()) {
			switch ((PerformanceVariatorEnum) SimProperties.PERFORMANCE_VARIATOR.asEnum()) {
			case UNIFORM_PERF_VAR:
				return (long) (new UniformPerformanceVariator().generationRuntimeVariation(taskLength) * processors);
			}
		}
	return (long) (taskLength * processors);
    }

    public int getEntityID() {
	return getId();
    }

    public double priceQuery(final InstanceType type, final OS os) {
	return chooseBestDataCenter(AZ.ANY, type, os).getCurrentPrice(type, os);
    }

    public double priceQuery(final InstanceType type, final OS os, final AZ az) {
	return chooseBestDataCenter(az, type, os).getCurrentPrice(type, os);
    }

    @Override
    public void processEvent(final SimEvent ev) {

	final EventTag eventTag = ev.getEventTag();

	if (eventTag instanceof ComputeCloudTags) {
	    final ComputeCloudTags tag = (ComputeCloudTags) eventTag;
	    // Handles ComputeCloud specific events
	    switch (tag) {
	    case RUN_INSTANCE:
		if (!this.connectedClients.contains(ev.getSource())) {
		    this.connectedClients.add(ev.getSource());
		}
		final RunInstancesRequest req = (RunInstancesRequest) ev.getData();
		runInstance(ev.getSource(), req);
		break;
	    case TERMINATE_INSTANCES:
		final List<Long> tTerminate = (List<Long>) ev.getData();
		this.terminateInstances(ev.getSource(), tTerminate);
		break;
	    case CHECKPOINT_INSTANCE:
		final CkptInstanceRequest ckptRequest = (CkptInstanceRequest) ev.getData();
		checkpointInstance(ckptRequest);
		break;
	    case RUN_TASK:
		final RunTaskRequest rtReq = (RunTaskRequest) ev.getData();
		runTask(ev.getSource(), rtReq);
		break;
	    case CHANGE_INSTANCE_PRICE:
		/*
		 * updates price of a spot instance type and schedules the next
		 * price change
		 */
		final PriceChangeEvent changeEvent = (PriceChangeEvent) ev.getData();
		changePrice(changeEvent);
		break;
	    case CANCEL_TASK:
		final Task cancelRequest = (Task) ev.getData();
		cancelTask(ev.getSource(), cancelRequest);
		break;
	    case FAIL_INSTANCE:
			final long failResourceToken = (long) ev.getData();
			failInstance(ev.getSource(), failResourceToken);
			break;
	    case CHANGE_BID:
		final ChangeBidRequest changeBidReq = (ChangeBidRequest) ev.getData();
		changeBid(changeBidReq);
		break;
	    case FIRE_UP_INSTANCE:
		fireUpInstance((Instance) ev.getData());
		break;
	    case RESUME_INSTANCE:
		resumeInstance((Instance) ev.getData());
		break;
	    case CLIENT_DISCONNECT:
		Log.logger.warning(Log.clock() + "Client " + ev.getSource() + " disconnected");
		this.connectedClients.remove(ev.getSource());
		if (this.connectedClients.isEmpty()) {
		    CloudSim.abruptallyTerminate();
		}
		break;
	    default:
		throw new RuntimeException("Event " + tag + " cannot be processed by the server");
	    }
	} else if (eventTag instanceof CloudSimTags) {
	    final CloudSimTags tag = (CloudSimTags) eventTag;
	    // Handles original CloudSim tags
	    switch (tag) {
	    case VM_CREATE_ACK:
		// VM was successfully created
		int[] data = (int[]) ev.getData();
		final int datacenterId = data[0];
		final int vmId = data[1];
		final int result = data[2];
		vmCreated(datacenterId, vmId, result == 1);
		break;
	    case CLOUDLET_RETURN:
		final Cloudlet c = (Cloudlet) ev.getData();
		cloudletReturned(c);
		break;
	    case VM_DESTROY_ACK:
		data = (int[]) ev.getData();
		final int vmID = data[1];
		vmTerminated(vmID);
		break;
	    case VM_PAUSE_ACK:
		data = (int[]) ev.getData();
		final int vmID3 = data[1];
		vmPaused(vmID3);
		break;
	    default:
		throw new RuntimeException("Event " + tag + " cannot be processed by the server");
	    }
	} else {
	    throw new RuntimeException("Event " + eventTag + " cannot be processed by the server");
	}

    }

    @Override
    public void shutdownEntity() {
    	/*try {
			fw.close();
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
    	
    }

    @Override
    public void startEntity() {
	// kicks off price variation according to price history logs or random
	// model, for each datacenter
	final Collection<DatacenterManager> values = this.datacenters.values();

	for (final DatacenterManager dc : values) {
	    for (final InstanceType type : InstanceType.values()) {
		for (final OS os : OS.values()) {
		    final PriceRecord nextPriceChange = dc.getNextPriceChange(type, os);
		    if (nextPriceChange != null) {
			Log.logger.info(Log.clock()
				+ "Scheduling price change for type "
				+ type
				+ ": "
				+ nextPriceChange);
			this.sendNow(getId(), ComputeCloudTags.CHANGE_INSTANCE_PRICE,
			    new PriceChangeEvent(type, os, nextPriceChange, dc.getAz()));
		    }
		}
	    }
	}
    }
    
    protected void failInstance(final int senderID, final long token) {

    	final Instance instance = getInstanceByToken(token);
    	if (instance != null && instance.getState().isRunning()) {
    		this.sendNow(instance.getParentDatacenter().getDcEntityId(), CloudSimTags.VM_DESTROY,
    				instance.getVm());
    		instance.setState(FAILED);
    		instanceFailed(instance);
    	}
   }
    
    
    protected void cancelTask(final int senderID, final Task task) {

	final Instance instance = getInstanceByToken(task.getResource().getToken());

	if (instance != null && instance.getState().isRunning()) {

	    final Cloudlet cloudlet = task.getCloudlet();

	    if (cloudlet.getStatus().isActive()) {
		if (Log.logger.isLoggable(Level.FINE)) {
		    Log.logger.fine(Log.clock()
			    + "Canceling cloudlet "
			    + cloudlet.getCloudletId()
			    + " on VM "
			    + instance.getId());
		}
		this.sendNow(instance.getParentDatacenter().getDcEntityId(),
		    CloudSimTags.CLOUDLET_CANCEL, cloudlet);
		if(instance.getState()!= IDLE){
			instance.setState(IDLE);
		}
		this.send(senderID, 1L, ComputeCloudTags.TASK_CANCELED, task);
	    }
	}
    }

    protected void changeBid(final ChangeBidRequest request) {

	final Instance instance = getInstanceFromRequest(request);

	if (instance.getState() == PENDING) {
	    instance.getParentDatacenter().bidChanged(instance.getId(), request.getNewBid());
	    if (Log.logger.isLoggable(Level.INFO)) {
		Log.logger.info(Log.clock()
			+ "New bid submitted for instance: "
			+ instance.getId()
			+ ", bid: "
			+ instance.getBidPrice()
			+ ", current price: "
			+ instance.getParentDatacenter().getCurrentPrice(instance.getType(),
			    instance.getOs()));
	    }

	    if (isInBid(instance)) {
		scheduleInstanceStart(instance);
	    }
	}
    }

    protected void changePrice(final PriceChangeEvent changeEvent) {
	final InstanceType type = changeEvent.getType();
	final OS os = changeEvent.getOs();
	final AZ az = changeEvent.getAz();
	final PriceRecord priceRecord = changeEvent.getPriceRecord();
	final double newPrice = priceRecord.getPrice();
	final DatacenterManager dc = this.datacenters.get(az);
	dc.updatePrice(type, os, newPrice);
	
	for (final Instance instance : dc.getOutOfBidSpotInstances(type, os, newPrice)) {
	    instanceOutOfBid(instance);
	}
	checkPendingSpotRequests(dc.getPendingSpotRequests(type, os), newPrice);
	scheduleNextChange(type, os, priceRecord.getTime(), dc);
    }

   	protected void checkpointInstance(final CkptInstanceRequest request) {
	if (isRequestActive(request.getToken())) {
	    final Instance instance = getInstanceFromRequest(request);
	    saveInstanceState(instance);
	    final long frequency = request.getFrequency();
	    if (frequency > 0 && !instance.getState().isTerminated()) {
		this.send(getId(), frequency, ComputeCloudTags.CHECKPOINT_INSTANCE,
		    new CkptInstanceRequest(request.getToken(), frequency));
	    }
	}
    }
    
    private static String getOutputFileName() {
    	String outputFileName = new String();
    	Random rand = new Random();
    	String daxFile = new File(SimProperties.WORKFLOW_FILE_DAG.toString()).getName();
    	int indexval = daxFile.lastIndexOf(".");
    	indexval = daxFile.lastIndexOf(".");
    	outputFileName += SimProperties.WORKFLOW_OUTPUT_DIRECTORY.asString();
		outputFileName += daxFile.substring(0,indexval);
		outputFileName += "_";
		outputFileName += "alpha";
		outputFileName += SimProperties.RISK_FACTOR_ALPHA.asString();
		outputFileName += "_";
		outputFileName += "beta";
		outputFileName += SimProperties.BUFFER_FACTOR_BETA.asString();
		outputFileName += "_";
		outputFileName += "thresold";
		outputFileName += SimProperties.FAILURE_THRESHOLD.asString();
		outputFileName += "_";
		outputFileName += "ChrgPrd";
		outputFileName += SimProperties.PRICING_CHARGEABLE_PERIOD.asString();
		outputFileName += "_";
		outputFileName += "VMINIT";
		outputFileName += SimProperties.DC_VM_INIT_TIME.asString();
		outputFileName += "_";
		outputFileName += "SPOTPriceHist";
		outputFileName += "_";
		outputFileName += rand.nextInt(100);
		outputFileName += ".csv";
		//outputFileName += Config.formatDate(Config.getSimPeriodStart());
		
		System.out.println("Output File name " + outputFileName );
		
		return outputFileName;
	}

    protected void cloudletReturned(final Cloudlet c) {
	final Task task = this.cloudlet2task.get(c.getCloudletId());
	final Instance instance = getInstance(c.getVmId());
	if (!instance.getState().isTerminated()) {
	    if (instance.getState() == PAUSING) {
		this.sendNow(instance.getParentDatacenter().getDcEntityId(),
		    CloudSimTags.VM_RESUME, instance.getVm());
		instance.setLatestCkpt(CloudSim.clock());
	    }
	    instance.setState(IDLE);
	    this.sendNow(task.getUserID(), ComputeCloudTags.TASK_FINISHED, task);
	}
    }

    protected void resumeInstance(final Instance instance) {
	if (instance.getState() == PAUSED) {
	    instance.setState(RUNNING_BUSY);
	    this.sendNow(instance.getParentDatacenter().getDcEntityId(), CloudSimTags.VM_RESUME,
		instance.getVm());
	    instance.setLatestCkpt(CloudSim.clock());
	}
    }

    protected Instance runInstance(final int senderId, final RunInstancesRequest request) {
	final InstanceType type = request.getType();
	final OS os = request.getOs();
	final double bid = request.getBid();
	final AZ az = request.getAz();

	final DatacenterManager dc = chooseBestDataCenter(az, type, os);

	if (Log.logger.isLoggable(Level.INFO)) {
	    Log.logger.info(Log.clock()
		    + "New instance requested: "
		    + type
		    + ", bid: "
		    + bid
		    + ", current price: "
		    + dc.getCurrentPrice(type, os));
	}

	final Instance newInstance = dc
	    .newInstance(type, os, request.getPriceModel(), getId(), bid);
	this.request2instanceId.put(request.getToken(), newInstance.getId());
	this.instanceId2datacenterId.put(newInstance.getId(), dc.getAz());
	newInstance.setRequestToken(request.getToken());
	newInstance.setBrokerID(senderId);
	if (isInBid(newInstance)) {
	    scheduleInstanceStart(newInstance);
	}

	return newInstance;
    }

    protected void runTask(final int senderID, final RunTaskRequest rtReq) {

	final Instance instance = getInstanceFromRequest(rtReq);

	if (instance.getState() == OUT_OF_BID) {
	    return;
	}

	final Task task = rtReq.getTask();

	if (instance.getState() == RUNNING_BUSY) {
	    throw new IllegalStateException("Cannot run task "
		    + task
		    + " on instance "
		    + instance.getId()
		    + " , already running "
		    + instance.getTask());
	}

	final Cloudlet oldCloudlet = task.getCloudlet();
	final long length = oldCloudlet == null ? task.getJob().getLength() : oldCloudlet
	    .getRemainingProgress();
	final long newLength = calcNewLength(task, instance.getType(), length);
	final int cloudletId = nextCloudletID();
	final Cloudlet cloudlet = new Cloudlet(cloudletId, newLength, 1);
	cloudlet.setUserId(getId());
	cloudlet.setVmId(instance.getId());
	task.setCloudlet(cloudlet);
	this.cloudlet2task.put(cloudletId, task);
	instance.setState(RUNNING_BUSY);
	instance.setTask(task);
	this.sendNow(instance.getParentDatacenter().getDcEntityId(), CloudSimTags.CLOUDLET_SUBMIT,
	    cloudlet);
    }

    protected void terminateInstances(final int senderID, final List<Long> tTerminate) {
	this.terminateInstances(senderID, tTerminate.toArray(new Long[tTerminate.size()]));
    }

    protected void terminateInstances(final int senderID, final Long... tTerminate) {
	for (final Long i : tTerminate) {
	    final Integer instId = this.request2instanceId.get(i);
	    final Instance instance = getInstance(instId);
	    if (instance.getState() == PENDING || instance.getState() == IDLE) {
		instance.setState(TERMINATED_BY_USER);
		instanceTerminated(instance);
	    }else if(instance.getState() == FAILED){
	    	return;
	    }else {
		if (instance.getState() != OUT_OF_BID) {
		    this.sendNow(instance.getParentDatacenter().getDcEntityId(),
			CloudSimTags.VM_DESTROY_ACK, instance.getVm());
		}if(instance.getState()!= TERMINATED_BY_USER){
			instance.setState(TERMINATED_BY_USER);
		}
	    }
	    /*//changes by deepak 18 aug 2014 to remove looping in checpointing for instances not beeing terminated
	    final DatacenterManager dc = this.datacenters.get(this.instanceId2datacenterId
	    	    .get(instId));
	    dc.destroyInstance(instId);*/
	}
    }

    private double billInstance(final Instance instance, boolean outOfBid) {
	final long accStart = instance.getAccStart();
	if (accStart < 0) {
	    return 0.0;
	}
	return Accounting.computeCost(accStart, instance.getAccEnd(), outOfBid,
	    PriceDB.getPriceTrace(this.region, instance.getParentDatacenter().getAz())
		.getIteratorAtTime(instance.getType(), instance.getOs(), accStart));
    }

    protected void vmCreated(final int datacenterId, final int vmId, final boolean result) {
	final Instance instance = getInstance(vmId);
	if (result) {
	    if (Log.logger.isLoggable(Level.FINE)) {
		Log.logger.fine(Log.clock()
			+ ": "
			+ getName()
			+ ": VM #"
			+ vmId
			+ " has been created in Datacenter #"
			+ datacenterId
			+ ", Host #"
			+ instance.getVm().getHost().getId());
	    }
	} else {
	    throw new IllegalStateException(Log.clock()
		    + ": "
		    + getName()
		    + ": VM #"
		    + vmId
		    + " has failed on #"
		    + datacenterId);
	}
    }

    protected void vmPaused(final int instanceId) {

	final Instance instance = getInstance(instanceId);
	if (instance.getState() == PAUSING) {
	    instance.setState(PAUSED);
	    final long overhead = instance.getType().getSuspendOverhead();
	    this.send(getId(), overhead, ComputeCloudTags.RESUME_INSTANCE, instance);
	    SimulationData.singleton().getStats().incrFTOverhead(overhead);
	}
    }

    protected void vmTerminated(final int vmID) {
	final Instance instance = getInstance(vmID);
	instanceTerminated(instance);
    }

    private void checkPendingSpotRequests(final Collection<Instance> pendingSpotRequests,
	    final double price) {

	for (final Instance instance : pendingSpotRequests) {
	    if (instance.getBidPrice() > price) {
		scheduleInstanceStart(instance);
	    }
	}
    }

    private DatacenterManager chooseBestDataCenter(final AZ az, final InstanceType type, final OS os) {
	if (az == AZ.ANY) {
	    double min = Double.MAX_VALUE;
	    DatacenterManager chosenDc = null;
	    for (final DatacenterManager dc : this.datacenters.values()) {
		final double currentPrice = dc.getCurrentPrice(type, os);
		if (currentPrice < min) {
		    min = currentPrice;
		    chosenDc = dc;
		}
	    }
	    return chosenDc;
	}
	return this.datacenters.get(az);
    }

    private long computeBootDelay(final InstanceType type, final OS os) {
	return SimProperties.DC_VM_INIT_TIME.asLong();
    }

    private void createDataCenters(final int hostsPerDatacenter) {

	for (final AZ az : this.region.getAvailabilityZones()) {
	    this.datacenters.put(az, new DatacenterManager(PriceDB.getPriceTrace(this.region, az),
		az, this.region, hostsPerDatacenter));
	}
    }

    private void fireUpInstance(final Instance instance) {
	if (isInBid(instance)) {
	    instance.startAccounting();
	    instance.setState(IDLE);
	    this.sendNow(instance.getBrokerID(), ComputeCloudTags.INSTANCE_CREATED,
		new InstanceCreatedNotification(instance.getRequestToken(), instance
		    .getParentDatacenter().getAz()));
	} else {
	   this.sendNow(instance.getParentDatacenter().getDcEntityId(), CloudSimTags.VM_DESTROY,
		instance.getVm());
	    instance.setState(PENDING);
	  
	   // instanceOutOfBid(instance);
	    /*   this.sendNow(
	    	    instance.getBrokerID(),
	    	    ComputeCloudTags.INSTANCE_TERMINATED,
	    	    new InstanceTerminatedNotification(instance.getRequestToken(), true, 0, instance
	    		.chargeablePeriods()));
	    instance.setState(OUT_OF_BID);*/
	}
    }

    private Instance getInstance(final Integer instanceId) {
	final DatacenterManager dc = this.datacenters.get(this.instanceId2datacenterId
	    .get(instanceId));
	final Instance instanceById = dc.getInstanceById(instanceId);
	return instanceById;
    }

    private Instance getInstanceByToken(final long token) {

	final Integer instanceId = this.request2instanceId.get(token);
	final Instance instanceById = getInstance(instanceId);
	if (instanceById == null) {
	    throw new IllegalArgumentException("There is no instance associated to request "
		    + token);
	}
	return instanceById;
    }

    private Instance getInstanceFromRequest(final CloudRequest request) {
	return getInstanceByToken(request.getToken());
    }

    private void instanceOutOfBid(final Instance instance) {

	final double bid = instance.getBidPrice();
	final double currentPrice = instance.getParentDatacenter().getCurrentPrice(
	    instance.getType(), instance.getOs());
	if (Log.logger.isLoggable(Level.INFO)) {
	    Log.logger.info(Log.clock()
		    + " Instance "
		    + instance.getId()
		    + " ("
		    + instance.getType()
		    + ' '
		    + instance.getOs()
		    + ") "
		    + " is out of bid. Bid: "
		    + bid
		    + ", price: "
		    + currentPrice);
	}

	if ((FaultToleranceMethod) SimProperties.FT_METHOD.asEnum() == FaultToleranceMethod.CHKPT_PERFECT) {
	    // special case to test a perfect, but unreal, form of checkpointing
	    this.sendNow(instance.getParentDatacenter().getDcEntityId(), CloudSimTags.VM_PAUSE,
		instance.getVm());
	    this.sendNow(instance.getParentDatacenter().getDcEntityId(), CloudSimTags.VM_RESUME,
		instance.getVm());
	}
	instance.setState(OUT_OF_BID);
	this.sendNow(instance.getParentDatacenter().getDcEntityId(), CloudSimTags.VM_DESTROY_ACK,
	    instance.getVm());
    }
    
    private void instanceFailed(final Instance instance) {
    	instance.stopAccounting();
    	final boolean failed = true;
    	// final double cost = billInstance(instance, false);
    	
    	double cost = 0;
    	if(instance.getPricing() == PriceModel.SPOT){
    		cost = billInstance(instance, failed);
    	}else if(instance.getPricing() == PriceModel.ON_DEMAND){
    		OS os = instance.getOs();
    		long from = instance.getAccStart();
    		long to = instance.getAccEnd();
    		final int minChargeablePeriod = SimProperties.PRICING_CHARGEABLE_PERIOD.asInt();
    		//if it failed first hr is 0
    		if (to - from <= minChargeablePeriod) {
    			cost = 0;
    		}else{
    			//last hr is not charged
    			final double hours = Math.ceil((to - from) / minChargeablePeriod);
    			cost = (hours-1) * instance.getType().getOnDemandPrice(region, os);
    		}
    		
    	}else{
    		throw new IllegalArgumentException("Undefined Pricing Model");
    	}
    	
    	instance.setCost(cost);
    	this.sendNow(
    	    instance.getBrokerID(),
    	    ComputeCloudTags.INSTANCE_FAILED,
    	    new InstanceTerminatedNotification(instance.getRequestToken(), false, cost, instance
    		.chargeablePeriods()-1));
    	
    	/*//Deepak 20th aug .. trying to remove instance form the list
    	final int instanceID = instance.getId();
    	final DatacenterManager dc = this.datacenters.get(this.instanceId2datacenterId
    		    .get(instanceID));
    	dc.destroyInstance(instanceID);*/
        }
    

    private void instanceTerminated(final Instance instance) {
	instance.stopAccounting();
	final boolean outOfBid = instance.getState() == OUT_OF_BID;
	// final double cost = billInstance(instance, false);
	
	double cost = 0;
	if(instance.getPricing() == PriceModel.SPOT){
		cost = billInstance(instance, outOfBid);
	}else if(instance.getPricing() == PriceModel.ON_DEMAND){
		OS os = instance.getOs();
		long from = instance.getAccStart();
		long to = instance.getAccEnd();
		final int minChargeablePeriod = SimProperties.PRICING_CHARGEABLE_PERIOD.asInt();
		if (to - from <= minChargeablePeriod) {
			cost = instance.getType().getOnDemandPrice(region, os);
		}else{
			final double hours = Math.ceil((to - from) / minChargeablePeriod);
			cost = hours * instance.getType().getOnDemandPrice(region, os);
		}
		
	}else{
		throw new IllegalArgumentException("Undefined Pricing Model");
	}
	
	instance.setCost(cost);
	this.sendNow(
	    instance.getBrokerID(),
	    ComputeCloudTags.INSTANCE_TERMINATED,
	    new InstanceTerminatedNotification(instance.getRequestToken(), outOfBid, cost, instance
		.chargeablePeriods()));
	/*//Deepak 20th aug .. trying to remove instance form the list
	final int instanceID = instance.getId();
	final DatacenterManager dc = this.datacenters.get(this.instanceId2datacenterId
		    .get(instanceID));
	dc.destroyInstance(instanceID);*/
    }

    private boolean isInBid(final Instance instance) {
	if (!instance.isSpot()) {
	    return true;
	}
	double currentPrice = instance.getParentDatacenter().getCurrentPrice(
	    instance.getType(), instance.getOs());
	return instance.getBidPrice() > currentPrice;
    }

    private boolean isRequestActive(final long requestId) {
	if (!this.request2instanceId.containsKey(requestId)) {
	    return false;
	}
	final Integer instId = this.request2instanceId.get(requestId);
	final DatacenterManager dc = this.datacenters.get(this.instanceId2datacenterId.get(instId));
	return dc.instanceExists(instId);
    }

    private int nextCloudletID() {
	return this.nextCloudletID++;
    }

    private void saveInstanceState(final Instance instance) {

	if (instance.getState() == RUNNING_BUSY) {
	    /*
	     * only save the state if there's something running; skip for all
	     * other states (e.g. BOOTING, IDLE)
	     */
	    instance.setState(PAUSING);
	    this.sendNow(instance.getParentDatacenter().getDcEntityId(), CloudSimTags.VM_PAUSE_ACK,
		instance.getVm());
	}
    }

    private void scheduleInstanceStart(final Instance instance) {
	instance.setState(STARTING);
	this.sendNow(instance.getParentDatacenter().getDcEntityId(), CloudSimTags.VM_CREATE_ACK,
	    instance.getVm());
	this.send(getId(), computeBootDelay(instance.getType(), instance.getOs()),
	    ComputeCloudTags.FIRE_UP_INSTANCE, instance);
    }

    private void scheduleNextChange(final InstanceType type, final OS os,
	    final long timeStampOfLastChange, final DatacenterManager datacenter) {
	final PriceRecord nextPriceChange = datacenter.getNextPriceChange(type, os);
	if (nextPriceChange != null
		&& nextPriceChange.getTime() < SimProperties.SIM_DAYS_TO_LOAD.asInt() * 24 * 3600) {
	    final PriceChangeEvent nextChangeEvent = new PriceChangeEvent(type, os,
		nextPriceChange, datacenter.getAz());
	    this.send(getId(), nextPriceChange.getTime() - timeStampOfLastChange,
		ComputeCloudTags.CHANGE_INSTANCE_PRICE, nextChangeEvent);
	}
    }

    public Region getRegion() {
	return this.region;
    }
}