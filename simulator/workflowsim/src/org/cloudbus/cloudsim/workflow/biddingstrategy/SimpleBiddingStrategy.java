package org.cloudbus.cloudsim.workflow.biddingstrategy;

import java.util.HashMap;

import org.cloudbus.cloudsim.workflow.broker.WorkflowBroker;
import org.cloudbus.cloudsim.workflow.failure.PriceFailureEstimator;
import org.cloudbus.spotsim.enums.AZ;
import org.cloudbus.spotsim.enums.InstanceType;
import org.cloudbus.spotsim.enums.OS;
import org.cloudbus.spotsim.enums.Region;
import org.cloudbus.spotsim.main.config.SimProperties;
import org.cloudbus.spotsim.pricing.db.PriceDB;

public class SimpleBiddingStrategy  implements WorkflowBiddingStrategy{
	
	private static HashMap<InstanceType,Double> previousBidPrice;
	private WorkflowBroker broker;
	private static double prevBidPr;
	
	public SimpleBiddingStrategy(){
		previousBidPrice = new HashMap<>();
	}

	public SimpleBiddingStrategy(WorkflowBroker broker) {
		this.broker = broker;
		previousBidPrice = new HashMap<>();
	}

	@Override
	public double bid(long LTO, long currentTime, InstanceType type, OS os, Region region, AZ az) {
		double bid = 0;
		double alpha = SimProperties.RISK_FACTOR_ALPHA.asDouble();
		double beta = SimProperties.BUFFER_FACTOR_BETA.asDouble();
		
		prevBidPr = 0;
		if (previousBidPrice.containsKey(type)) {
			prevBidPr = previousBidPrice.get(type);
		}
		
		PriceFailureEstimator priceFailureEst = new PriceFailureEstimator(PriceDB.getPriceTrace(region, az));
		double failureProb =1- priceFailureEst.computeFailureProbability(prevBidPr, region, az, type, os);
		double priceOD = PriceDB.getOnDemandPrice(region, type, os);
		double priceSpot = this.broker.priceQuery(type, os);
		
		Double temp = ((LTO-currentTime)/failureProb);
		if(temp == Double.NEGATIVE_INFINITY || temp.isNaN()){
			bid = priceOD;
		}else{
			bid = Math.exp(-alpha* temp)*priceOD+ (1 - Math.exp(-alpha*temp))*(beta*priceOD + (1-beta)*priceSpot);
		}
		//System.out.println(bid+"\t"+LTO+"\t"+currentTime+"\t"+failureProb+"\t"+priceSpot+"\t"+alpha+"\t"+beta);
		previousBidPrice.put(type, bid);
		return bid;
	}

	@Override
	public void updateBidPrice(InstanceType type, double bidPrice) {
		previousBidPrice.put(type, bidPrice);		
	}
	
	@Override
	public void setBroker(WorkflowBroker broker){
		this.broker = broker;
	}
}