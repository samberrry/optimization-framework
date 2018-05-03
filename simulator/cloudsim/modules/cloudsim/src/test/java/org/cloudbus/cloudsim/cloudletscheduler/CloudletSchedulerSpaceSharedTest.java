package org.cloudbus.cloudsim.cloudletscheduler;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletStatus;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 
 * @author williamv
 * 
 */
public class CloudletSchedulerSpaceSharedTest {

    private static final int CLOUDLET_ID_1 = 1;
    private static final int CLOUDLET_ID_2 = 2;
    private static final int CLOUDLET_ID_3 = 3;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
	Log.init(null);
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    private CloudletSchedulerSpaceShared sched;
    private Cloudlet c1;
    private ArrayList<Double> processors;
    private Cloudlet c2;
    private Cloudlet c3;

    @Before
    public void setUp() throws Exception {

	this.sched = new CloudletSchedulerSpaceShared();
	this.c1 = new Cloudlet(CLOUDLET_ID_1, 10, 1);
	this.c1.setResourceParameter(1, 0, 0);
	this.c2 = new Cloudlet(CLOUDLET_ID_2, 10, 2);
	this.c2.setResourceParameter(1, 0, 0);
	this.c3 = new Cloudlet(CLOUDLET_ID_3, 10, 1);
	this.c3.setResourceParameter(1, 0, 0);
	this.processors = new ArrayList<Double>();
	this.processors.add(1D);
	this.processors.add(1D);
	this.sched.mipsChanged(this.processors);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void cloudletStates() throws Exception {

	this.sched.updateVmProcessing();

	assertEquals(CloudletStatus.CREATED, this.c1.getStatus());
	assertEquals(CloudletStatus.CREATED, this.c2.getStatus());

	assertEquals(2, this.sched.freeCPUs());

	this.sched.cloudletSubmit(this.c1);
	assertEquals(1, this.sched.freeCPUs());
	this.sched.cloudletSubmit(this.c2);
	assertEquals(1, this.sched.freeCPUs());
	this.sched.cloudletSubmit(this.c3);
	assertEquals(0, this.sched.freeCPUs());

	assertEquals(CloudletStatus.INEXEC, this.sched.getCloudletStatus(CLOUDLET_ID_1));
	assertEquals(CloudletStatus.QUEUED, this.sched.getCloudletStatus(CLOUDLET_ID_2));
	assertEquals(CloudletStatus.INEXEC, this.sched.getCloudletStatus(CLOUDLET_ID_3));

	this.sched.updateVmProcessing();

	this.sched.pauseAllCloudlets();

	assertEquals(2, this.sched.freeCPUs());

	assertEquals(CloudletStatus.PAUSED, this.sched.getCloudletStatus(CLOUDLET_ID_1));
	assertEquals(CloudletStatus.PAUSED, this.sched.getCloudletStatus(CLOUDLET_ID_2));
	assertEquals(CloudletStatus.PAUSED, this.sched.getCloudletStatus(CLOUDLET_ID_3));

	this.sched.updateVmProcessing();

	assertEquals(CloudletStatus.PAUSED, this.sched.getCloudletStatus(CLOUDLET_ID_1));
	assertEquals(CloudletStatus.PAUSED, this.sched.getCloudletStatus(CLOUDLET_ID_2));
	assertEquals(CloudletStatus.PAUSED, this.sched.getCloudletStatus(CLOUDLET_ID_3));

	this.sched.cloudletCancel(CLOUDLET_ID_1);
	this.sched.cloudletResume(CLOUDLET_ID_2);

	assertEquals(0, this.sched.freeCPUs());

	this.sched.cloudletResume(CLOUDLET_ID_3);

	assertEquals(CloudletStatus.INEXEC, this.sched.getCloudletStatus(CLOUDLET_ID_2));
	assertEquals(CloudletStatus.QUEUED, this.sched.getCloudletStatus(CLOUDLET_ID_3));
	assertEquals(CloudletStatus.CANCELED, this.c1.getStatus());
	assertEquals(CloudletStatus.UNKNOWN, this.sched.getCloudletStatus(CLOUDLET_ID_1));
    }

    @Test
    public void testProgress() throws Exception {

	CloudSim.setClock(0);
	this.sched.updateVmProcessing();
	this.sched.cloudletSubmit(this.c1);
	assertEquals(0, this.c1.getTotalProgress());
	CloudSim.setClock(1);
	this.sched.updateVmProcessing();
	assertEquals(0, this.c1.getTotalProgress());
	CloudSim.setClock(2);
	this.sched.updateVmProcessing();
	this.sched.cloudletCancel(CLOUDLET_ID_1);
	assertEquals(0, this.c1.getTotalProgress());
    }

    @Test
    public void testPauseCancel() throws Exception {

	CloudSim.setClock(0);
	this.sched.updateVmProcessing();
	this.sched.cloudletSubmit(this.c1);
	assertEquals(0, this.c1.getTotalProgress());
	CloudSim.setClock(1);
	this.sched.updateVmProcessing();
	assertEquals(0, this.c1.getTotalProgress());
	CloudSim.setClock(2);
	this.sched.updateVmProcessing();
	this.sched.cloudletPause(CLOUDLET_ID_1);
	assertEquals(0, this.c1.getTotalProgress());
	CloudSim.setClock(5);
	this.sched.cloudletResume(CLOUDLET_ID_1);
	assertEquals(2, this.c1.getTotalProgress());
	this.sched.updateVmProcessing();
	CloudSim.setClock(9);
	this.sched.updateVmProcessing();
	this.sched.cloudletCancel(CLOUDLET_ID_1);
	assertEquals(2, this.c1.getTotalProgress());
    }

    @Test
    public void testSuspend() throws Exception {

	CloudSim.setClock(0);
	this.sched.updateVmProcessing();
	this.sched.cloudletSubmit(this.c1);
	CloudSim.setClock(2);
	this.sched.updateVmProcessing();
	this.sched.cloudletPause(CLOUDLET_ID_1);
	assertEquals(0, this.c1.getTotalProgress());
	CloudSim.setClock(5);
	this.sched.updateVmProcessing();
	this.sched.cloudletResume(CLOUDLET_ID_1);
	assertEquals(2, this.c1.getTotalProgress());
	CloudSim.setClock(8);
	this.sched.updateVmProcessing();
	this.sched.cloudletSuspend(CLOUDLET_ID_1);
	assertEquals(5, this.c1.getTotalProgress());
    }

    @Test
    public void testPauseResume() throws Exception {

	CloudSim.setClock(0);
	this.sched.updateVmProcessing();
	this.sched.cloudletSubmit(this.c1);
	assertEquals(0, this.c1.getTotalProgress());
	CloudSim.setClock(1);
	this.sched.updateVmProcessing();
	assertEquals(0, this.c1.getTotalProgress());
	CloudSim.setClock(2);
	this.sched.updateVmProcessing();
	this.sched.cloudletPause(CLOUDLET_ID_1);
	assertEquals(0, this.c1.getTotalProgress());
	CloudSim.setClock(5);
	this.sched.updateVmProcessing();
	this.sched.cloudletResume(CLOUDLET_ID_1);
	assertEquals(2, this.c1.getTotalProgress());
	this.sched.updateVmProcessing();
	CloudSim.setClock(10);
	this.sched.updateVmProcessing();
	assertEquals(2, this.c1.getTotalProgress());
	CloudSim.setClock(13);
	this.sched.updateVmProcessing();
	assertEquals(10, this.c1.getTotalProgress());
    }
}