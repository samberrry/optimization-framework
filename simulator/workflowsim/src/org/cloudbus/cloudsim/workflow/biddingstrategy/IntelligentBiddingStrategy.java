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

public class IntelligentBiddingStrategy  implements WorkflowBiddingStrategy{
	
	private static HashMap<InstanceType,Double> previousBidPrice;
	private WorkflowBroker broker;
	private static double prevBidPr;
	
	public IntelligentBiddingStrategy(){
		previousBidPrice = new HashMap<>();
	}

	public IntelligentBiddingStrategy(WorkflowBroker broker) {
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
		double failureProb =1- priceFailureEst.computeFailureProbability(prevBidPr, region, az, type, os,0L, currentTime);
		double priceOD = PriceDB.getOnDemandPrice(region, type, os);
		double priceSpot = this.broker.priceQuery(type, os);
		
		Double temp = Math.abs(((LTO-currentTime)/failureProb));
		if(temp == Double.NEGATIVE_INFINITY || temp.isNaN()){
			bid = priceOD;
		}else{
			bid = Math.exp(-alpha* temp)*priceOD+ (1 - Math.exp(-alpha*temp))*(beta*priceOD + (1-beta)*priceSpot);
		}
		if(prevBidPr !=0 && bid < prevBidPr ){
			bid = prevBidPr;
		}
		previousBidPrice.put(type, bid);
		if(bid <= 0){
			bid = priceSpot;
		}
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