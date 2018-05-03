package org.cloudbus.cloudsim.workflow.failure;

import java.util.NavigableSet;
import java.util.SortedSet;

import org.cloudbus.spotsim.enums.AZ;
import org.cloudbus.spotsim.enums.InstanceType;
import org.cloudbus.spotsim.enums.OS;
import org.cloudbus.spotsim.enums.Region;
import org.cloudbus.spotsim.main.config.SimProperties;
import org.cloudbus.spotsim.pricing.PriceRecord;
import org.cloudbus.spotsim.pricing.SpotPriceHistory;
import org.cloudbus.spotsim.pricing.db.PriceDB;

public class PriceFailureEstimator {

	public static SpotPriceHistory spotPriceHistory;
	 /*private static SoftReference<HistoryPersistenceManager> hpm = new SoftReference<HistoryPersistenceManager>(
			 new HistoryPersistenceManager(SimProperties.PRICING_HISTORY_MAP_DIR.asString()));*/
    
	public PriceFailureEstimator(SpotPriceHistory spotPriceHistory) {
		super();
		this.spotPriceHistory = spotPriceHistory;
	}
	
	public PriceFailureEstimator() {
		super();
	}

	/**
	 * This function check the history and return the failure probability for the Bid Price
	 * @param bidPrice
	 * @return the failure probability of the bid price value between 0 to 1
	 */
	public double computeFailureProbability(double bidPrice,Region region, AZ az, InstanceType instanceType, OS os, long from, long to){
		
		//SimProperties.PRICING_TRACE_GEN.set(PriceTraceGen.HISTORY);

		int highPriceOverall = 0;
		long timeHighOveral = 0;
		long diff = 0;
		
		SpotPriceHistory spotHist = PriceDB.getPriceTrace(region, az);
		if (spotHist.areTherePricesForType(instanceType, os)) {
			final NavigableSet<PriceRecord> fullList = spotHist.getPricesForType(
					instanceType, os);
			SortedSet<PriceRecord> priceSubset = spotHist.getPriceSubset(from, to, fullList);
			timeHighOveral = getOutOfBidTime(bidPrice, priceSubset);
			final long first = priceSubset.first().getDate();
			final long last = priceSubset.last().getDate();
			diff = last - first;
		} else {
			System.out.println("No prices for " + region.getAmazonName() + az
					+ "-" + instanceType + "-" + os);
		}
		
		if(diff == 0){
			if(timeHighOveral <= 0){
				return 0;
			}else{
				return 1;
			}
		}

		return (double)timeHighOveral/diff;
	}
	
	/**
	 * This function check the history and return the failure probability for the Bid Price
	 * @param bidPrice
	 * @return the failure probability of the bid price value between 0 to 1
	 */
	public double computeFailureProbability(double bidPrice,Region region, AZ az, InstanceType instanceType, OS os){
		
		//SimProperties.PRICING_TRACE_GEN.set(PriceTraceGen.HISTORY);
		long timeHighOveral = 0;
		long diff = 0;
		
		
		SpotPriceHistory spotHist = this.spotPriceHistory;
		if(spotHist == null){
			this.spotPriceHistory = PriceDB.getPriceTrace(region, az);
			spotHist = this.spotPriceHistory;
		}
		if (spotHist.areTherePricesForType(instanceType, os)) {
			final NavigableSet<PriceRecord> fullList = spotHist.getPricesForType(
					instanceType, os);
			final int simDaysToLoad = SimProperties.SIM_DAYS_TO_LOAD.asInt();
			SortedSet<PriceRecord> priceSubset = spotHist.getPriceSubset((simDaysToLoad-1) * 24 * 3600, (simDaysToLoad) * 24 * 3600, fullList);
			timeHighOveral = getOutOfBidTime(bidPrice, priceSubset);
			final long first = priceSubset.first().getDate();
			final long last = priceSubset.last().getDate();
			diff = last - first;

		} else {
			System.out.println("No prices for " + region.getAmazonName() + az
					+ "-" + instanceType + "-" + os);
		}

		if(diff >0 ){
			return (double)timeHighOveral/diff;
		}else{
			return 0;
		}
	}
	
	
/*	
	public static HistoryPersistenceManager getHPM() {
		HistoryPersistenceManager hp = hpm.get();
		if (hp == null) {
		    hp = new HistoryPersistenceManager(SimProperties.PRICING_HISTORY_MAP_DIR.asString());
		    hpm = new SoftReference<HistoryPersistenceManager>(hp);
		}
		return hp;
	}*/
	
	/**
	 * This function check the history and return the failure probability for the Bid Price
	 * @param bidPrice
	 * @return the failure probability of the bid price value between 0 to 1
	 */
	public double computeFailureProbability(double bidPrice, InstanceType instanceType, OS os){
		
		//SimProperties.PRICING_TRACE_GEN.set(PriceTraceGen.HISTORY);

		long timeHighOveral = 0;
		long diff = 0;

		SpotPriceHistory spotHist = this.spotPriceHistory;
		if (spotHist.areTherePricesForType(instanceType, os)) {
			final NavigableSet<PriceRecord> fullList = spotHist.getPricesForType(instanceType, os);
			timeHighOveral = getOutOfBidTime(bidPrice, fullList);
			final long first = fullList.first().getDate();
			final long last = fullList.last().getDate();
			diff = last - first;
		} else {
			System.out.println("No prices are available for instance type "+ instanceType);
		}

		return (double)timeHighOveral/diff;
	}

	private long getOutOfBidTime(double bidPrice, final SortedSet<PriceRecord> priceSubset) {

		int highPriceOverall =0;
		long timeHighOveral = 0;
		if (!priceSubset.isEmpty()) {
			double max = Double.MIN_VALUE;
			double min = Double.MAX_VALUE;
			int highprice = 0;
			long timeHigh = 0;
			long timeHighMax = Long.MIN_VALUE;
			long prev = priceSubset.first().getTime();
			for (PriceRecord rec : priceSubset) {
				double price = rec.getPrice();
				long date = rec.getDate();
				if (price > max) {
					max = price;
				} else if (price < min) {
					min = price;
				}
				if (price > bidPrice) {
					highprice++;
					final long diff2 = date - prev;
					timeHigh += diff2;
					if (diff2 > timeHighMax) {
						timeHighMax = diff2;
					}
				}
				prev = date;
			}

			highPriceOverall += highprice;
			timeHighOveral += timeHigh;
		}
		return timeHighOveral;
	}
		
}
