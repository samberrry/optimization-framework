/**
 * 
 */
package org.cloudbus.spotsim.simrecords;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This is a profiler. A very simple one. I mean, very very simple!
 * 
 * @author William Voorsluys - williamvoor@gmail.com
 * 
 */
public class Profiler {

    public enum Metric {
	SIM,
	SCHED_TIME,
	PRICE_SEARCH_TIME,
	SCHED_RECYCLE,
	SCHED_NEW,
	FORECASTING;

	long tempTime = -1;
    }

    private final Map<Metric, AtomicLong> times;

    private Map<Metric, Double> percentages;

    public Profiler() {
	this.times = new HashMap<Metric, AtomicLong>();
	for (final Metric metric : Metric.values()) {
	    this.times.put(metric, new AtomicLong());
	}
    }

    public void computeTotals() {
	long total = 0L;
	for (final AtomicLong metricVal : this.times.values()) {
	    total += metricVal.get();
	}
	this.percentages = new HashMap<Metric, Double>();
	for (final Metric metric : Metric.values()) {
	    this.percentages.put(metric, (double) (this.times.get(metric).get() / total));
	}
    }

    public void endPeriod(final Metric m) {
	this.times.get(m).addAndGet(System.currentTimeMillis() - m.tempTime);
    }

    public Map<Metric, AtomicLong> getTimes() {
	return this.times;
    }

    public long soFar(final Metric m) {
	return System.currentTimeMillis() - m.tempTime;
    }

    public void startPeriod(final Metric m) {
	m.tempTime = System.currentTimeMillis();
    }
}