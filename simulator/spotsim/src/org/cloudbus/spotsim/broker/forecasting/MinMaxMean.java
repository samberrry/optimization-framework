package org.cloudbus.spotsim.broker.forecasting;

public class MinMaxMean {

    private final double min, max, mean;

    public MinMaxMean(final double min, final double max, final double mean) {
	if (min <= 0 || max <= 0 || mean <= 0) {
	    throw new IllegalArgumentException("Values must be greater than 0 ("
		    + min
		    + ','
		    + max
		    + ','
		    + mean
		    + ')');
	}
	this.min = min;
	this.max = max;
	this.mean = mean;
    }

    public double getMax() {
	return this.max;
    }

    public double getMean() {
	return this.mean;
    }

    public double getMin() {
	return this.min;
    }
}