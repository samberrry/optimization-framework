package org.cloudbus.spotsim.pricing;

import java.util.HashMap;
import java.util.Map;

import org.cloudbus.spotsim.broker.forecasting.MinMaxMean;
import org.cloudbus.spotsim.broker.forecasting.PriceForecastKey;
import org.cloudbus.spotsim.enums.InstanceType;
import org.cloudbus.spotsim.enums.OS;
import org.cloudbus.utils.data.enummaps.DoubleEnumMap;

public class Caches {

    /*
     * A series of caches to avoid repetitive queries to the price history
     * records. They make this simulator much faster. Seriously!
     */
    private final DoubleEnumMap<InstanceType, OS, Double> minCache;

    private Map<PriceForecastKey, MinMaxMean> minMaxCache;

    private Map<Long, Long> from2firstCache;

    private Map<Long, Long> to2lastCache;

    private Map<PriceForecastKey, Double> priceCache;

    public Caches() {
	this.minCache = new DoubleEnumMap<InstanceType, OS, Double>();
	this.minMaxCache = new HashMap<PriceForecastKey, MinMaxMean>();
	this.from2firstCache = new HashMap<Long, Long>();
	this.to2lastCache = new HashMap<Long, Long>();
	this.priceCache = new HashMap<PriceForecastKey, Double>();
    }

    public MinMaxMean getMinMax(long from, long to, InstanceType type, OS os) {
	if (!this.from2firstCache.containsKey(from) || !this.to2lastCache.containsKey(to)) {
	    return null;
	}
	long first = this.from2firstCache.get(from);
	long last = this.to2lastCache.get(to);
	final PriceForecastKey key = PriceForecastKey.newInstance(first, last, type, os);
	final MinMaxMean ret = this.minMaxCache.get(key);
	PriceForecastKey.reclaimInstance(key);
	return ret;
    }

    public void cacheMinMax(long from, long to, long first, long last, InstanceType type, OS os,
	    MinMaxMean minMax) {
	this.from2firstCache.put(from, first);
	this.to2lastCache.put(to, last);
	final PriceForecastKey key = PriceForecastKey.newInstance(first, last, type, os);
	this.minMaxCache.put(key, minMax);
    }

    public void cacheMin(InstanceType type, OS os, double price) {
	this.minCache.put(type, os, price);
    }

    public Double getPrice(long from, long to, InstanceType type, OS os) {
	PriceForecastKey key = PriceForecastKey.newInstance(from, to, type, os);
	final Double ret = this.priceCache.get(key);
	PriceForecastKey.reclaimInstance(key);
	return ret;
    }
}