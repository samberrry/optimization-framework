package org.cloudbus.spotsim.spothistory.distr;

import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.ExponentialDistributionImpl;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.spotsim.main.config.Config;
import org.cloudbus.spotsim.pricing.distr.Exponential;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ExponentialTest {

    private Exponential distr;

    private ExponentialDistributionImpl distr2;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
	Log.init();
	Config.load();

	this.distr = new Exponential(5, Config.RNG);
	this.distr2 = new ExponentialDistributionImpl(5);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void exp1() throws MathException {
	for (int i = 0; i < 100; i++) {
	    System.out.println("1: " + this.distr.nextDouble() + ", 2: " + this.distr2.sample());
	}
    }
}
