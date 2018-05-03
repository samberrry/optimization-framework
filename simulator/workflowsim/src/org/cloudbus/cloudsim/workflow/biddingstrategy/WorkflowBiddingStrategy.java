package org.cloudbus.cloudsim.workflow.biddingstrategy;

import org.cloudbus.cloudsim.workflow.broker.WorkflowBroker;
import org.cloudbus.spotsim.enums.AZ;
import org.cloudbus.spotsim.enums.InstanceType;
import org.cloudbus.spotsim.enums.OS;
import org.cloudbus.spotsim.enums.Region;

public interface WorkflowBiddingStrategy {

	public abstract double bid(long LTO, long currentTime, InstanceType type, OS os, Region region, AZ az);
	
	public abstract void updateBidPrice( InstanceType type, double bidPrice);
	
	public abstract void setBroker(WorkflowBroker broker);
}