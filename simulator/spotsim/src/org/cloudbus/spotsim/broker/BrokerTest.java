package org.cloudbus.spotsim.broker;

import static org.easymock.EasyMock.eq;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.util.workload.Job;
import org.cloudbus.cloudsim.util.workload.Job.JobStatus;
import org.cloudbus.cloudsim.util.workload.Task;
import org.cloudbus.cloudsim.util.workload.TaskState;
import org.cloudbus.spotsim.broker.policies.SimplePolicy;
import org.cloudbus.spotsim.cloudprovider.ComputeCloud;
import org.cloudbus.spotsim.enums.AZ;
import org.cloudbus.spotsim.enums.InstanceType;
import org.cloudbus.spotsim.enums.OS;
import org.cloudbus.spotsim.enums.PriceModel;
import org.cloudbus.spotsim.main.config.Config;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class BrokerTest {

    private static final int NJOBS = 100;

    private static List<Job> jobs;

    private static List<SimEvent> events;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

	jobs = new ArrayList<Job>();
	CloudSim.init(1, null, true);
	Config.load(null);

	for (int i = 0; i < NJOBS; i++) {
	    final Job j = new Job(i, i, 10, i, i, 100, 1);
	    jobs.add(j);
	}
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
    public void testExecutionWithOutOfBid() throws Exception {

    }

    @SuppressWarnings("boxing")
    @Test
    public void testNormalExecutionWithReplication() throws Exception {

	final ComputeCloud server = EasyMock.createMock(ComputeCloud.class);
	final SchedPolicy policy = new SimplePolicy();
	final Broker broker = new Broker("WillBrokerzzz", server, policy);
	policy.setBroker(broker);

	EasyMock.expect(server.priceQuery(InstanceType.M1SMALL, OS.LINUX)).andReturn(1D).anyTimes();
	for (final Job j : jobs) {
	    broker.newJobArrived(j);
	    EasyMock.expect(
		server.runInstance(eq(broker.getId()), eq(InstanceType.M1SMALL), eq(OS.LINUX),
		    eq(PriceModel.SPOT), EasyMock.anyDouble(), eq(0))).andReturn(
		new Long(j.getId()));
	    EasyMock.expect(
		server.runInstance(eq(broker.getId()), eq(InstanceType.M1SMALL), eq(OS.LINUX),
		    eq(PriceModel.SPOT), EasyMock.anyDouble(), eq(0))).andReturn(
		new Long(NJOBS + j.getId()));
	}
	for (final Job j : jobs) {
	    server.runTask(eq(j.getId()), eq(broker.getId()), EasyMock.anyObject(Task.class));
	    server.runTask(eq(NJOBS + j.getId()), eq(broker.getId()),
		EasyMock.anyObject(Task.class));
	}

	for (final Job j : jobs) {
	    server.cancelTask(eq(broker.getId()), EasyMock.anyObject(Task.class));
	}

	for (final Job j : jobs) {
	    server.terminateInstance(eq(broker.getId()), EasyMock.eq(j.getId()));
	    server.terminateInstance(eq(broker.getId()), EasyMock.eq(NJOBS + j.getId()));
	}

	EasyMock.replay(server);

	broker.schedule();

	for (final Job j : jobs) {
	    broker.instanceCreated(j.getId(), AZ.A);
	    broker.instanceCreated(NJOBS + j.getId(), AZ.A);
	}

	for (final Job j : jobs) {
	    assertEquals(TaskState.RUNNING, j.getTasks().get(0).getState());
	    assertEquals(TaskState.RUNNING, j.getTasks().get(1).getState());
	}

	for (final Job j : jobs) {
	    broker.taskFinished(j.getTasks().get(0));
	}

	for (final Job j : jobs) {
	    assertEquals(TaskState.COMPLETED, j.getTasks().get(0).getState());
	    assertEquals(TaskState.CANCELED, j.getTasks().get(1).getState());
	    assertEquals(JobStatus.COMPLETED, j.getStatus());
	}

	for (final Job j : jobs) {
	}

	EasyMock.verify(server);
    }
}
