package org.cloudbus.spotsim.pricing.distr;

import java.math.BigDecimal;

import org.apache.commons.math.random.RandomGenerator;
import org.cloudbus.spotsim.main.config.SimProperties;

import umontreal.iro.lecuyer.randvar.NormalGen;
import umontreal.iro.lecuyer.randvarmulti.MultinormalCholeskyGen;
import umontreal.iro.lecuyer.rng.LFSR258;

/*************************************************************************
 * Generate random variables with mixture of gaussians distribution. The
 * approach is composition method. This class needs SSJ library. To download
 * visit http://www.iro.umontreal.ca/~simardr/ssj/
 * 
 * @author Bahman Javadi
 * @author William Voorsluys
 * 
 * 
 *************************************************************************/

public class MixtureOfGaussians implements RandomDistribution {

    private final NormalGen ngen;

    private final MixtureModel mixtureModel;

    private final double[] sigma;

    private final double[] mu;

    private final int k;

    private final BigDecimal[] p;

    private final RandomGenerator rng;

    public MixtureOfGaussians(final MixtureModel mixtureModel, RandomGenerator rng) {
	this(mixtureModel, 1D, 1D, rng);
    }

    public MixtureOfGaussians(final MixtureModel mixtureModel, final double multMu,
	    final double multSigma, RandomGenerator rng) {
	super();
	this.mixtureModel = mixtureModel;
	this.rng = rng;
	final long s = SimProperties.RNG_SEED.asLong();
	LFSR258.setPackageSeed(new long[] { 2 + s, 512 + s, 4096 + s, 131072 + s, 8388608 + s });
	this.ngen = new NormalGen(new LFSR258());
	this.k = this.mixtureModel.getK();
	final BigDecimal[] muB = this.mixtureModel.getMu();
	final BigDecimal[] sigmaB = this.mixtureModel.getSigma();
	this.p = this.mixtureModel.getP();
	this.mu = new double[muB.length];
	this.sigma = new double[sigmaB.length];

	for (int i = 0; i < sigmaB.length; i++) {
	    this.sigma[i] = sigmaB[i].multiply(new BigDecimal(multSigma)).doubleValue();
	    this.mu[i] = muB[i].multiply(new BigDecimal(multMu)).doubleValue();
	}
    }

    @Override
    public double nextDouble() {

	BigDecimal rnd;
	int i;
	BigDecimal pr;

	rnd = new BigDecimal(this.rng.nextDouble());
	i = 0;
	pr = this.p[i];
	while (i <= this.k) {
	    if (rnd.compareTo(pr) <= 0) {
		break;
	    }
	    i++;
	    pr = pr.add(this.p[i]);
	}

	final double[] value = new double[1];
	final double[] mui = { this.mu[i] };
	final double[][] sigma2 = { { this.sigma[i] } };

	do {

	    MultinormalCholeskyGen.nextPoint(this.ngen, mui, sigma2, value);

	} while (value[0] < 0);
	return value[0];
    }
}