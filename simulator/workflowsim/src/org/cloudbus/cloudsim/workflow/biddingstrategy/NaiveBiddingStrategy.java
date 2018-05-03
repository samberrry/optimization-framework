package org.cloudbus.cloudsim.workflow.biddingstrategy;

import org.cloudbus.cloudsim.workflow.broker.WorkflowBroker;
import org.cloudbus.spotsim.enums.AZ;
import org.cloudbus.spotsim.enums.InstanceType;
import org.cloudbus.spotsim.enums.OS;
import org.cloudbus.spotsim.enums.Region;

public class NaiveBiddingStrategy implements WorkflowBiddingStrategy {

	private WorkflowBroker broker;
	
	@Override
	public double bid(long LTO, long currentTime, InstanceType type, OS os,
			Region region, AZ az) {
		return this.broker.priceQuery(type, os);
	}

	@Override
	public void updateBidPrice(InstanceType type, double bidPrice) {
		// Nothing to update

	}

	@Override
	public void setBroker(WorkflowBroker broker) {
		this.broker = broker;

	}

}
