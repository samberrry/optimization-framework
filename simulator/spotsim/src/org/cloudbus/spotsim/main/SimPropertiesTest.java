package org.cloudbus.spotsim.main;

import org.apache.commons.math.random.Well512a;
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
    public void weirdRNG() {
	Well512a rng1 = new Well512a(10L);
	Well512a rng2 = new Well512a(10L);

	for (int i = 0; i < 20; i++) {
	    System.out.println("1 " + rng1.nextDouble());
	    System.out.println("2 " + rng2.nextDouble());
	}
    }
}
