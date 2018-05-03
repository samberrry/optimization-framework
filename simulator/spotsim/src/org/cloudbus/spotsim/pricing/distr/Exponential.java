package org.cloudbus.spotsim.pricing.distr;

import org.apache.commons.math.random.RandomGenerator;

public class Exponential implements RandomDistribution {

    private final double mean;

    private final RandomGenerator rng;

    public Exponential(double mean, RandomGenerator rng) {
	this(mean, 1D, rng);
    }

    public Exponential(double mean, double muMult, RandomGenerator rng) {
	this.rng = rng;
	this.mean = mean * muMult;
    }

    @Override
    public double nextDouble() {
	return -this.mean * Math.log(this.rng.nextDouble());
    }

    public double getMean() {
	return this.mean;
    }
}
