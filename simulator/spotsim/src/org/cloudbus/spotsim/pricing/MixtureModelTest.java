package org.cloudbus.spotsim.pricing;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

import org.cloudbus.spotsim.pricing.distr.MixtureModel;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class MixtureModelTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void arrayConstructor() throws Exception {

	final MixtureModel mm = new MixtureModel(3, new String[] { "0.1", "0.8", "20", "30", "40",
		"1", "2", "3" });
	assertEquals(3, mm.getK());
	assertArrayEquals(mm.getP(), new BigDecimal[] { new BigDecimal("0.1"),
		new BigDecimal("0.8"), new BigDecimal("0.1") });
	assertArrayEquals(mm.getMu(), new BigDecimal[] { new BigDecimal("20"),
		new BigDecimal("30"), new BigDecimal("40") });
	assertArrayEquals(mm.getSigma(), new BigDecimal[] { new BigDecimal("1"),
		new BigDecimal("2"), new BigDecimal("3") });
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }
}
