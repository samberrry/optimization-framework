package org.cloudbus.spotsim.cloudprovider;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.util.workload.Task;
import org.cloudbus.spotsim.ComputeCloudTags;
import org.cloudbus.spotsim.enums.AZ;
import org.cloudbus.spotsim.enums.InstanceType;
import org.cloudbus.spotsim.enums.OS;
import org.cloudbus.spotsim.enums.PriceModel;
import org.cloudbus.spotsim.enums.Region;
import org.cloudbus.spotsim.payloads.ChangeBidRequest;
import org.cloudbus.spotsim.payloads.CkptInstanceRequest;
import org.cloudbus.spotsim.payloads.CloudRequest;
import org.cloudbus.spotsim.payloads.RunInstancesRequest;
import org.cloudbus.spotsim.payloads.RunTaskRequest;

public class ComputeCloudStub implements ComputeCloud {

    private final ComputeCloudImpl impl;

    public ComputeCloudStub(final ComputeCloudImpl impl) {
	super();
	this.impl = impl;
    }
    
    @Override
    public void cancelTask(final int senderID, final Task task) {
	CloudSim.send(senderID, getServerId(), 0, ComputeCloudTags.CANCEL_TASK, task);
    }

    @Override
    public void changeBid(final long token, final int senderID, final double newBid) {
	final ChangeBidRequest changeBidRequest = new ChangeBidRequest(token, token, newBid);
	CloudSim.send(senderID, getServerId(), 0, ComputeCloudTags.CHANGE_BID, changeBidRequest);
    }

    @Override
    public void checkpointInstance(final long token, final int senderID, final long frequency) {
	CloudSim.send(senderID, getServerId(), frequency, ComputeCloudTags.CHECKPOINT_INSTANCE,
	    new CkptInstanceRequest(token, frequency));
    }

    public int getServerId() {
	return this.impl.getEntityID();
    }

    @Override
    public double priceQuery(final InstanceType type, final OS os) {
	return this.impl.priceQuery(type, os);
    }

    @Override
    public double priceQuery(final InstanceType type, final OS os, final AZ az) {
	return this.impl.priceQuery(type, os, az);
    }

    @Override
    public long runInstance(final int senderID, final InstanceType type, final OS os,
	    final PriceModel priceModel, final double bid, final long delay) {
	return this.runInstance(senderID, 1, 1, type, os, priceModel, bid, delay, AZ.ANY);
    }

    @Override
    public long runInstance(final int senderID, final int minCount, final int maxCount,
	    final InstanceType type, final OS os, final PriceModel priceModel, final double bid,
	    final long delay, final AZ az) {
	final CloudRequest req = new RunInstancesRequest(minCount, maxCount, type, os, priceModel,
	    bid, az);
	CloudSim.send(senderID, getServerId(), delay, ComputeCloudTags.RUN_INSTANCE, req);

	return req.getToken();
    }

    @Override
    public void runTask(final long token, final int senderID, final Task t) {
	final RunTaskRequest request = new RunTaskRequest(token, t);
	CloudSim.send(senderID, getServerId(), 0, ComputeCloudTags.RUN_TASK, request);
    }

    @Override
    public void terminateInstance(final int senderID, final long toTerminate) {
	final ArrayList<Long> list = new ArrayList<Long>();
	list.add(toTerminate);
	CloudSim.send(senderID, getServerId(), 0, ComputeCloudTags.TERMINATE_INSTANCES, list);
    }

    @Override
    public void terminateInstances(final int senderID, final List<Long> toTerminate) {
	CloudSim
	    .send(senderID, getServerId(), 0, ComputeCloudTags.TERMINATE_INSTANCES, toTerminate);
    }

    @Override
    public void disconnect(int senderID) {
	CloudSim.send(senderID, getServerId(), 0, ComputeCloudTags.CLIENT_DISCONNECT, null);
    }

    @Override
    public Region getRegion() {
	return this.impl.getRegion();
    }

	@Override
	public void failInstance(int senderID, long token) {
		CloudSim.send(senderID, getServerId(), 0, ComputeCloudTags.FAIL_INSTANCE, token);		
	}
}
