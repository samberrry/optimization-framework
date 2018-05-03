package org.cloudbus.spotsim.cloudprovider;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.spotsim.cloudprovider.instance.DatacenterManager;
import org.cloudbus.spotsim.cloudprovider.instance.Instance;
import org.cloudbus.spotsim.cloudprovider.instance.InstanceState;
import org.cloudbus.spotsim.enums.AZ;
import org.cloudbus.spotsim.enums.InstanceType;
import org.cloudbus.spotsim.enums.OS;
import org.cloudbus.spotsim.enums.PriceModel;
import org.cloudbus.spotsim.enums.Region;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ComputeCloudInstancesTest {

    private static final Region region = Region.US_EAST;

    private final int cloudId = 1;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

	Log.init(null);
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
    public void testHeadSet() throws Exception {
	final DatacenterManager instances = new DatacenterManager(null, AZ.A, Region.US_EAST, 100);
	final Instance instance1 = instances.newInstance(InstanceType.M1SMALL, OS.LINUX,
	    PriceModel.SPOT, this.cloudId, 0.04);
	final Instance instance2 = instances.newInstance(InstanceType.M1SMALL, OS.LINUX,
	    PriceModel.SPOT, this.cloudId, 0.05);
	final Instance instance21 = instances.newInstance(InstanceType.M1SMALL, OS.LINUX,
	    PriceModel.SPOT, this.cloudId, 0.05);
	final Instance instance3 = instances.newInstance(InstanceType.M1SMALL, OS.LINUX,
	    PriceModel.SPOT, this.cloudId, 0.06);
	final Instance instance4 = instances.newInstance(InstanceType.M1SMALL, OS.LINUX,
	    PriceModel.SPOT, this.cloudId, 0.06);
	final Instance instance5 = instances.newInstance(InstanceType.M1SMALL, OS.LINUX,
	    PriceModel.SPOT, this.cloudId, 0.08);

	instance1.setState(InstanceState.STARTING);
	instance2.setState(InstanceState.STARTING);
	instance21.setState(InstanceState.STARTING);
	instance3.setState(InstanceState.STARTING);
	instance4.setState(InstanceState.STARTING);
	instance5.setState(InstanceState.STARTING);

	List<Instance> instancesHeadSet = instances.getOutOfBidSpotInstances(InstanceType.M1SMALL,
	    OS.LINUX, 0.03);
	assertTrue(instancesHeadSet.isEmpty());

	instancesHeadSet = instances
	    .getOutOfBidSpotInstances(InstanceType.M1SMALL, OS.LINUX, 0.065);
	assertNotNull(instancesHeadSet);
	assertFalse(instancesHeadSet.isEmpty());
	assertTrue(instancesHeadSet.contains(instance1));
	assertTrue(instancesHeadSet.contains(instance2));
	assertTrue(instancesHeadSet.contains(instance21));
	assertTrue(instancesHeadSet.contains(instance3));
	assertTrue(instancesHeadSet.contains(instance4));
	assertFalse(instancesHeadSet.contains(instance5));

	instancesHeadSet = instances.getOutOfBidSpotInstances(InstanceType.M1SMALL, OS.LINUX, 0.09);
	assertTrue(instancesHeadSet.contains(instance1));
	assertTrue(instancesHeadSet.contains(instance2));
	assertTrue(instancesHeadSet.contains(instance3));
	assertTrue(instancesHeadSet.contains(instance4));
	assertTrue(instancesHeadSet.contains(instance5));
    }
}
