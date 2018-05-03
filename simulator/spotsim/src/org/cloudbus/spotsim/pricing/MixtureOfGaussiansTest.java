package org.cloudbus.spotsim.pricing;

import static org.cloudbus.spotsim.main.config.SimProperties.RNG_SEED;

import java.util.Map;

import org.apache.commons.math.random.Well512a;
import org.apache.commons.math.util.MathUtils;
import org.cloudbus.spotsim.enums.AZ;
import org.cloudbus.spotsim.enums.InstanceType;
import org.cloudbus.spotsim.enums.Region;
import org.cloudbus.spotsim.pricing.distr.RandomDistribution;
import org.cloudbus.spotsim.pricing.distr.RandomDistributionManager;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class MixtureOfGaussiansTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
	RandomDistributionManager.load(new Well512a(RNG_SEED.asLong()));
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testRandomness() {

	Map<InstanceType, RandomDistribution> priceMM = RandomDistributionManager.getPriceDistr(
	    Region.DEEPAK_TEST, AZ.ANY, new Well512a(RNG_SEED.asLong()));

	Map<InstanceType, RandomDistribution> priceIntrMM = RandomDistributionManager.getInterPriceDistr(
		    Region.DEEPAK_TEST, AZ.ANY, new Well512a(RNG_SEED.asLong()));

	//Well512a rnd = new Well512a(1);
	RandomDistribution gaussians = priceMM.get(InstanceType.M1SMALL);
	RandomDistribution interPriceDistr = priceIntrMM.get(InstanceType.M1SMALL);

	long sumTime =0;
	for (int i = 0; i < 5; i++) {
	    double nextGaussian = MathUtils.round(gaussians.nextDouble() / 100D, 3);
	    int nextGaussianIntr = (int) (interPriceDistr.nextDouble() * 3600);
	    sumTime += nextGaussianIntr;
	    System.out.println(nextGaussian + "\t" + sumTime);
	}
	System.out.println("========================================================");
	RandomDistributionManager.load(new Well512a(RNG_SEED.asLong()));
   priceMM = RandomDistributionManager.getPriceDistr(
    	    Region.DEEPAK_TEST, AZ.ANY, new Well512a(RNG_SEED.asLong()));

    	priceIntrMM = RandomDistributionManager.getInterPriceDistr(
    		    Region.DEEPAK_TEST, AZ.ANY, new Well512a(RNG_SEED.asLong()));

    	//Well512a rnd = new Well512a(1);
    	gaussians = priceMM.get(InstanceType.M1SMALL);
    	interPriceDistr = priceIntrMM.get(InstanceType.M1SMALL);

    	sumTime =0;
    	for (int i = 0; i < 5; i++) {
    	    double nextGaussian = MathUtils.round(gaussians.nextDouble() / 100D, 3);
    	    int nextGaussianIntr = (int) (interPriceDistr.nextDouble() * 3600);
    	    sumTime += nextGaussianIntr;
    	    System.out.println(nextGaussian + "\t" + sumTime);
    	}
        }


}
