package org.cloudbus.spotsim.pricing;

import java.util.Iterator;

import org.cloudbus.spotsim.main.config.SimProperties;

/**
 * 
 * Manages usage accounting and billing of virtual machine instances
 * 
 * @author William Voorsluys - williamvoor@gmail.com
 * 
 */
public class Accounting {

    /**
     * 
     * @param from
     *        period start (simulation time)
     * @param to
     *        period end (simulation time)
     * @param outOfBid
     * @param l
     * @return
     */
    public static double computeCost(final long from, final long to, final boolean outOfBid,
	    final Iterator<PriceRecord> it) {

	if (to < from) {
	    throw new RuntimeException("End date: " + to + " must be after Start date: " + from);
	}

	PriceRecord currentPrice = it.next();

	final int minChargeablePeriod = SimProperties.PRICING_CHARGEABLE_PERIOD.asInt();
	if (to - from < minChargeablePeriod) {
	    // less than one minimum billing period (usually one hour), return
	    // current price if its not out of bid
		if(outOfBid){
			return 0.0;
		}
	    return currentPrice.getPrice();
	}

	PriceRecord nextChange = null;
	if (it.hasNext()) {
	    nextChange = it.next();
	}

	if (nextChange == null || nextChange.getTime() > to) {
	    final double hours = Math.ceil((to - from) / minChargeablePeriod);
	    return hours * currentPrice.getPrice();
	}

	long nextTimestamp = nextChange.getTime();

	double cost = 0.0;
	long prev = from;
	while (true) {
	    prev += minChargeablePeriod;
	    if (prev >= to) {
		if (!outOfBid) {
		    cost += currentPrice.getPrice();
		}
		break;
	    }
	    cost += currentPrice.getPrice();

	    while (prev > nextTimestamp) {
		currentPrice = nextChange;
		if (!it.hasNext()) {
		    throw new RuntimeException("There no price records beyond "
			    + nextTimestamp
			    + " yet, the simulation has extended to: "
			    + prev);
		}
		nextChange = it.next();
		nextTimestamp = nextChange.getTime();
	    }
	}
	return cost;
    }
}