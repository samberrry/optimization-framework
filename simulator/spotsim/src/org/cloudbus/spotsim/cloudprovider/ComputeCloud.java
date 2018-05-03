package org.cloudbus.spotsim.cloudprovider;

import java.util.List;

import org.cloudbus.cloudsim.util.workload.Task;
import org.cloudbus.spotsim.enums.AZ;
import org.cloudbus.spotsim.enums.InstanceType;
import org.cloudbus.spotsim.enums.OS;
import org.cloudbus.spotsim.enums.PriceModel;
import org.cloudbus.spotsim.enums.Region;

public interface ComputeCloud {

	void failInstance(int senderID, long token);
	
    void cancelTask(int senderID, Task task);

    void changeBid(long token, int senderID, double newBid);

    void checkpointInstance(long token, int senderID, long frequency);

    double priceQuery(InstanceType type, OS os);

    double priceQuery(InstanceType type, OS os, AZ az);

    long runInstance(int senderID, InstanceType type, OS os, PriceModel priceModel, double bid,
	    long delay);

    long runInstance(int senderID, int minCount, int maxCount, InstanceType type, OS os,
	    PriceModel priceModel, double bid, long delay, AZ az);

    void runTask(long token, int senderID, Task t);

    void terminateInstance(int senderID, long toTerminate);

    void terminateInstances(int senderID, List<Long> toTerminate);

    void disconnect(int senderID);

    Region getRegion();
}