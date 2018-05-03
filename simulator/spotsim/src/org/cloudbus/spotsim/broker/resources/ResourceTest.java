package org.cloudbus.spotsim.broker.resources;

import static junit.framework.Assert.assertEquals;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.util.workload.Job;
import org.cloudbus.cloudsim.util.workload.Task;
import org.cloudbus.spotsim.enums.AZ;
import org.cloudbus.spotsim.enums.InstanceType;
import org.cloudbus.spotsim.enums.OS;
import org.cloudbus.spotsim.enums.Region;
import org.cloudbus.spotsim.main.config.Config;
import org.cloudbus.spotsim.main.config.SimProperties;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ResourceTest {

    private static final int TASK1_LENGTH = 5000;

    private static ResourceFactory resourceFactory;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

	CloudSim.setClock(0);
	Log.init(null);
	Config.load(null);
	resourceFactory = new ResourceFactory();
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void idleTimes() throws Exception {

	Region region = Region.getDefault();
	OS os = OS.getDefault();
	final Resource r1 = resourceFactory.newResource(region, AZ.A, InstanceType.M1SMALL, os,
	    0.04);
	final Job j1 = new Job(1, 0, 1000, 1, 1, TASK1_LENGTH, 1);
	final Task t1 = j1.newTask();
	t1.setEstimatedRunTime(TASK1_LENGTH);
	r1.requested(1, 0);
	assertEquals(SimProperties.DC_VM_INIT_TIME.asInt(), r1.getTimeThatItWillBecomeIdle());
	assertEquals(SimProperties.DC_VM_INIT_TIME.asInt() + 3600,
	    r1.getNextFullHourAfterBecomingIdle());
	assertEquals(SimProperties.DC_VM_INIT_TIME.asInt() + 3600, r1.getNextFullHour());
	r1.scheduleTask(t1);
	assertEquals(SimProperties.DC_VM_INIT_TIME.asInt() + TASK1_LENGTH,
	    r1.getTimeThatItWillBecomeIdle());
	assertEquals(SimProperties.DC_VM_INIT_TIME.asInt() + 7200,
	    r1.getNextFullHourAfterBecomingIdle());

	CloudSim.setClock(CloudSim.clock() + SimProperties.DC_VM_INIT_TIME.asInt());
	r1.received();
	assertEquals(SimProperties.DC_VM_INIT_TIME.asInt() + TASK1_LENGTH,
	    r1.getTimeThatItWillBecomeIdle());
	assertEquals(SimProperties.DC_VM_INIT_TIME.asInt() + 7200,
	    r1.getNextFullHourAfterBecomingIdle());

	CloudSim.setClock(CloudSim.clock() + SimProperties.DC_VM_INIT_TIME.asInt());
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }
}
