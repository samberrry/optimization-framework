/**
 * 
 */
package org.cloudbus.spotsim.broker;

public class DowneyParams {

    private final double a;

    private final double sigma;

    public DowneyParams(final double a, final double sigma) {
	super();
	this.a = a;
	this.sigma = sigma;
    }

    public double getA() {
	return this.a;
    }

    public double getSigma() {
	return this.sigma;
    }
}