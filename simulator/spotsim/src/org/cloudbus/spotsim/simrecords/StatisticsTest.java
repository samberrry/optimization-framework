package org.cloudbus.spotsim.simrecords;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class StatisticsTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void readableTime() throws Exception {

	assertEquals("2h1m40s", Statistics.getTime(7300));

	assertEquals("1h", Statistics.getTime(3600));
	assertEquals("1h1m", Statistics.getTime(3660));
	assertEquals("1h1m1s", Statistics.getTime(3661));
	assertEquals("1h1s", Statistics.getTime(3601));

	assertEquals("1m", Statistics.getTime(60));
	assertEquals("1m1s", Statistics.getTime(61));
	assertEquals("1m10s", Statistics.getTime(70));

	assertEquals("30s", Statistics.getTime(30));
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }
}
