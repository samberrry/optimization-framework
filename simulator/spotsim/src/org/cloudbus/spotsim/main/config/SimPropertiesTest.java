package org.cloudbus.spotsim.main.config;

import static org.junit.Assert.*;

import org.cloudbus.spotsim.broker.forecasting.PriceForecastKey;
import org.cloudbus.spotsim.enums.InstanceType;
import org.cloudbus.spotsim.enums.OS;
import org.cloudbus.spotsim.enums.Region;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class SimPropertiesTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void asEnum() {

	SimProperties.DC_DEFAULT_REGION.set(Region.US_EAST);
	assertEquals(Region.US_EAST, SimProperties.DC_DEFAULT_REGION.asEnum());
	assertEquals(Region.US_EAST, SimProperties.DC_DEFAULT_REGION.asEnum(Region.class));

	try {
	    SimProperties.DC_DEFAULT_REGION.asEnum(InstanceType.class);
	    SimProperties.DC_DEFAULT_REGION.asLong();
	    fail();
	} catch (ClassCastException e) {
	}

	SimProperties.DC_DEFAULT_REGION.read("US_EAST");
	assertEquals(Region.US_EAST, SimProperties.DC_DEFAULT_REGION.asEnum(Region.class));
	assertEquals("US_EAST", SimProperties.DC_DEFAULT_REGION.asString());
    }

    @Test
    public void wrongType() {

	try {
	    SimProperties.DC_DEFAULT_REGION.read("bla");
	    fail();
	} catch (IllegalArgumentException e) {
	}

	try {
	    SimProperties.RNG_SEED.set(Region.APAC_JAPAN);
	    SimProperties.DC_DEFAULT_REGION.asBoolean();
	    SimProperties.DC_DEFAULT_REGION.asLong();
	    SimProperties.DC_DEFAULT_REGION.asInt();
	    SimProperties.DC_DEFAULT_REGION.asDouble();
	    SimProperties.RNG_SEED.asEnum(Region.class);
	    fail();
	} catch (ClassCastException e) {
	}

	try {
	    SimProperties.RNG_SEED.read("oops");
	    fail();
	} catch (NumberFormatException e) {
	}
    }

    @Test
    public void numbers() throws Exception {
	SimProperties.RNG_SEED.set(10L);
	SimProperties.WORKLOAD_JOBS.set(1);
	SimProperties.PRICING_PRICE_MU_MULT.set(2.5);

	assertEquals(10L, SimProperties.RNG_SEED.asLong());
	assertEquals(10, SimProperties.RNG_SEED.asInt());
	assertEquals(10D, SimProperties.RNG_SEED.asDouble(), 0D);

	assertEquals(1L, SimProperties.WORKLOAD_JOBS.asLong());
	assertEquals(1, SimProperties.WORKLOAD_JOBS.asInt());
	assertEquals(1D, SimProperties.WORKLOAD_JOBS.asDouble(), 0D);

	assertEquals(2L, SimProperties.PRICING_PRICE_MU_MULT.asLong());
	assertEquals(2, SimProperties.PRICING_PRICE_MU_MULT.asInt());
	assertEquals(2.5D, SimProperties.PRICING_PRICE_MU_MULT.asDouble(), 0D);
    }

    @Test
    public void bool() throws Exception {
	try {
	    SimProperties.REPORT_REDO_EXISTING_RESULTS.read("2xxxx");
	    fail();
	} catch (ClassCastException e) {
	}

	SimProperties.REPORT_REDO_EXISTING_RESULTS.read("false");
	assertFalse(SimProperties.REPORT_REDO_EXISTING_RESULTS.asBoolean());

	SimProperties.REPORT_REDO_EXISTING_RESULTS.read("FALSE");
	assertFalse(SimProperties.REPORT_REDO_EXISTING_RESULTS.asBoolean());

	SimProperties.REPORT_REDO_EXISTING_RESULTS.set(true);
	assertTrue(SimProperties.REPORT_REDO_EXISTING_RESULTS.asBoolean());
    }

    @Test
    public void string() throws Exception {

	SimProperties.DUMMY.read("true");
	assertTrue(SimProperties.DUMMY.asBoolean());

	SimProperties.DUMMY.set(10);
	assertEquals("10", SimProperties.DUMMY.asString());
    }

    @Test
    public void arbitraryType() throws Exception {

	try {
	    SimProperties.DUMMY2.asType(PriceForecastKey.class);
	    fail();
	} catch (NullPointerException e) {
	}

	final PriceForecastKey val = new PriceForecastKey(0, 0, InstanceType.M1SMALL, OS.LINUX);
	SimProperties.DUMMY2.set(val);

	try {
	    SimProperties.DUMMY2.asType(Integer.class);
	    SimProperties.DUMMY2.asInt();
	    fail();
	} catch (ClassCastException e) {
	}

	PriceForecastKey asType = SimProperties.DUMMY2.asType(PriceForecastKey.class);
	PriceForecastKey asType2 = (PriceForecastKey) SimProperties.DUMMY2.asObject();
	assertEquals(val, asType);
	assertEquals(val, asType2);
    }
}
