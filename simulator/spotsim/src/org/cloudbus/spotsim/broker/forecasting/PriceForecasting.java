package org.cloudbus.spotsim.broker.forecasting;

import java.util.EnumSet;
import java.util.NavigableSet;
import java.util.SortedSet;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.spotsim.enums.InstanceType;
import org.cloudbus.spotsim.enums.OS;
import org.cloudbus.spotsim.enums.PriceForecastingMethod;
import org.cloudbus.spotsim.main.config.SimProperties;
import org.cloudbus.spotsim.pricing.Accounting;
import org.cloudbus.spotsim.pricing.Caches;
import org.cloudbus.spotsim.pricing.PriceRecord;
import org.cloudbus.spotsim.pricing.SpotPriceHistory;
import org.cloudbus.utils.data.enummaps.DoubleEnumMap;

public class PriceForecasting {

    public final SpotPriceHistory spotPriceHistory;

    private final Caches cache = new Caches();

    private DoubleEnumMap<InstanceType, OS, NavigableSet<PriceRecord>> randomPredictions;

    public PriceForecasting(final SpotPriceHistory spotPriceHistory) {
	this.spotPriceHistory = spotPriceHistory;
	if (SimProperties.PRICING_COST_FORECASTING_METHOD.asEnum() == PriceForecastingMethod.MM
		|| SimProperties.PRICING_MINMAX_FORECASTING_METHOD.asEnum() == PriceForecastingMethod.MM) {
	    generateRandomPredictions();
	}
    }

    private void generateRandomPredictions() {
	this.randomPredictions = new DoubleEnumMap<InstanceType, OS, NavigableSet<PriceRecord>>();

	EnumSet<InstanceType> types = EnumSet.allOf(InstanceType.class);
	EnumSet<OS> oss = EnumSet.allOf(OS.class);

	for (OS os : oss) {
	    for (InstanceType t : types) {
		this.randomPredictions.put(t, os, this.spotPriceHistory.randomPricePeriod(t, os, 0,
		    SimProperties.SIM_DAYS_TO_LOAD.asInt() * 24 * 3600));
	    }
	}
    }

    public double forecastFuturePrice(final InstanceType type, final OS os, final long from,
	    final long to) {

	Double cached = this.cache.getPrice(from, to, type, os);

	if (cached != null) {
	    return cached;
	}

	final int fullHours = (int) Math.ceil((to - from) / 3600D);

	double cost = -1D;
	final PriceForecastingMethod forecastingMethod = (PriceForecastingMethod) SimProperties.PRICING_COST_FORECASTING_METHOD
	    .asEnum();
	switch (forecastingMethod) {
	case OPTIMAL:
	    cost = Accounting.computeCost(from, to, false,
		this.spotPriceHistory.getIteratorAtTime(type, os, from));
	    break;
	case ON_DEMAND:
	    cost = this.spotPriceHistory.getOnDemandPrice(type, os) * fullHours;
	    break;
	case PAST_N_DAYS_MEAN:
	    final long from2 = Math.min(from, CloudSim.clock());
	    final long sevenDaysB4 = from2
		    - SimProperties.SCHED_N_DAYS_FORECASTING.asInt()
		    * 24
		    * 3600;
	    final MinMaxMean minMax = this.spotPriceHistory
		.minMaxMean(type, os, sevenDaysB4, from2);
	    cost = minMax.getMean() * fullHours;
	    break;
	case CURRENT:
	    cost = this.spotPriceHistory.getPriceAtTime(type, os, from) * fullHours;
	    break;
	case MM:
	    NavigableSet<PriceRecord> pred = this.randomPredictions.get(type, os);
	    cost = Accounting.computeCost(from, to, false,
		SpotPriceHistory.getIteratorAtTime(type, os, from, pred));
	    break;
	default:
	    throw new IllegalArgumentException("Price forecasting option "
		    + forecastingMethod
		    + " is not yet implemented");
	}
	if (cost <= 0) {
	    throw new IllegalStateException("Price forecasting option "
		    + forecastingMethod
		    + " produced an invalid cost: "
		    + cost);
	}
	return cost;
    }

    public MinMaxMean forecastMinMaxMean(final InstanceType type, final OS os, final long from,
	    final long to, final long current) {

	final PriceForecastingMethod forecastingMethod = (PriceForecastingMethod) SimProperties.PRICING_MINMAX_FORECASTING_METHOD
	    .asEnum();
	MinMaxMean minMax = null;
	switch (forecastingMethod) {
	case OPTIMAL:
	    minMax = this.spotPriceHistory.minMaxMean(type, os, from, to);
	    break;
	case PAST_N_DAYS_MEAN:
	    final long nDaysB4 = current
		    - SimProperties.SCHED_N_DAYS_FORECASTING.asInt()
		    * 24
		    * 3600;
	    minMax = this.spotPriceHistory.minMaxMean(type, os, nDaysB4, current);
	    break;
	case ON_DEMAND:
	    final double price = this.spotPriceHistory.getOnDemandPrice(type, os);
	    return new MinMaxMean(price, price, price);
	case CURRENT:
	    final double currentPrice = this.spotPriceHistory.getPriceAtTime(type, os, current);
	    return new MinMaxMean(currentPrice, currentPrice, currentPrice);
	case MM:
	    MinMaxMean cached = this.cache.getMinMax(from, to, type, os);
	    if (cached != null) {
		return cached;
	    }
	    SortedSet<PriceRecord> subset = SpotPriceHistory.getPriceSubset(from, to,
		this.randomPredictions.get(type, os));
	    minMax = SpotPriceHistory.minMaxMean(subset);
	    long first = subset.first().getTime();
	    long last = subset.last().getTime();
	    this.cache.cacheMinMax(from, to, first, last, type, os, minMax);
	    break;
	default:
	    throw new IllegalArgumentException("Price forecasting option "
		    + forecastingMethod
		    + " is not yet implemented");
	}

	if (minMax == null) {
	    new IllegalStateException("MinMax forecasting option "
		    + forecastingMethod
		    + " produced no forecast");
	}

	return minMax;
    }
}