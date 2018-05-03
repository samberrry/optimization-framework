package org.cloudbus.spotsim.pricing.distr;

import java.math.BigDecimal;
import java.util.Arrays;

public class MixtureModel {

    private final int k;

    private final BigDecimal[] p;

    private final BigDecimal[] mu;

    private final BigDecimal[] sigma;

    public MixtureModel(final int k, final double[] p, final double[] mu, final double[] sigma) {
	if (k < 1) {
	    throw new IllegalArgumentException("K must be greater than 0");
	}
	this.k = k;
	this.p = new BigDecimal[k];
	this.mu = new BigDecimal[k];
	this.sigma = new BigDecimal[k];
	for (int i = 0; i < k; i++) {
	    this.p[i] = new BigDecimal("" + p[i]);
	    this.mu[i] = new BigDecimal("" + mu[i]);
	    this.sigma[i] = new BigDecimal("" + sigma[i]);
	}
	this.checkSanity();
    }

    public MixtureModel(final int k, final String[] params) {
	this.k = k;
	final int minParam = k * 3 - 1;
	if (params.length < minParam) {
	    throw new IllegalArgumentException("A mixture model of k="
		    + k
		    + " should have "
		    + minParam
		    + " parameters, but only has "
		    + params.length);
	}

	this.p = new BigDecimal[k];
	this.mu = new BigDecimal[k];
	this.sigma = new BigDecimal[k];

	BigDecimal psum = BigDecimal.ZERO;
	for (int i = 0; i < k - 1; i++) {
	    this.p[i] = new BigDecimal(params[i]);
	    psum = psum.add(this.p[i]);
	}

	this.p[k - 1] = BigDecimal.ONE.subtract(psum);
	for (int i = k - 1, j = 0; i < 2 * k - 1; i++, j++) {
	    this.mu[j] = new BigDecimal(params[i]);
	}
	for (int i = 2 * k - 1, j = 0; i < 3 * k - 1; i++, j++) {
	    this.sigma[j] = new BigDecimal(params[i]);
	}
	this.checkSanity();
    }

    public int getK() {
	return this.k;
    }

    public BigDecimal[] getMu() {
	return this.mu;
    }

    public BigDecimal[] getP() {
	return this.p;
    }

    public BigDecimal[] getSigma() {
	return this.sigma;
    }

    @Override
    public String toString() {
	final StringBuilder builder = new StringBuilder();
	builder.append("MixtureModel [k=").append(this.k).append(", p=")
	    .append(Arrays.toString(this.p)).append(", mu=").append(Arrays.toString(this.mu))
	    .append(", sigma=").append(Arrays.toString(this.sigma)).append("]");
	return builder.toString();
    }

    private void checkSanity() {

	if (this.p.length != this.k) {
	    throw new RuntimeException("Not enough p values: " + this.p.length);
	}
	if (this.mu.length != this.k) {
	    throw new RuntimeException("Not enough mu values" + this.mu.length);
	}
	if (this.sigma.length != this.k) {
	    throw new RuntimeException("Not enough sigma values" + this.sigma.length);
	}

	BigDecimal sum = BigDecimal.ZERO;
	String ps = "";
	for (final BigDecimal pp : this.p) {
	    ps += pp + " + ";
	    sum = sum.add(pp);
	}
	if (!(sum.compareTo(BigDecimal.ONE) == 0)) {
	    throw new RuntimeException("P values don't add up to 1: " + ps + "==" + sum);
	}
    }
}