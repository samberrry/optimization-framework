package org.cloudbus.spotsim.cloudprovider;

import static junit.framework.Assert.assertEquals;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.spotsim.PriceChangeEvent;
import org.cloudbus.spotsim.cloudprovider.instance.Instance;
import org.cloudbus.spotsim.cloudprovider.instance.InstanceState;
import org.cloudbus.spotsim.enums.AZ;
import org.cloudbus.spotsim.enums.InstanceType;
import org.cloudbus.spotsim.enums.OS;
import org.cloudbus.spotsim.enums.PriceModel;
import org.cloudbus.spotsim.enums.Region;
import org.cloudbus.spotsim.payloads.RunInstancesRequest;
import org.cloudbus.spotsim.pricing.Accounting;
import org.cloudbus.spotsim.pricing.PriceRecord;
import org.cloudbus.spotsim.pricing.SpotPriceHistory;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ComputeCloudImplTest {

    private static ComputeCloudImpl cloud;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

	CloudSim.init(1, null, true);

	cloud = new ComputeCloudImpl(10, Region.US_EAST);
	final Accounting acc = EasyMock.createNiceMock(Accounting.class);
	final SpotPriceHistory hist = EasyMock.createNiceMock(SpotPriceHistory.class);
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void outOfBid() throws Exception {

	final RunInstancesRequest req = new RunInstancesRequest(1, 1, InstanceType.M1SMALL,
	    OS.LINUX, PriceModel.SPOT, 0.03, AZ.A);
	final Instance instance1 = cloud.runInstance(1, req);
	assertEquals(InstanceState.STARTING, instance1.getState());
	final PriceChangeEvent ev = new PriceChangeEvent(InstanceType.M1SMALL, OS.LINUX,
	    new PriceRecord(0L, 0.04), AZ.A);
	cloud.changePrice(ev);
	cloud.terminateInstances(1, instance1.getRequestToken());
	assertEquals(InstanceState.OUT_OF_BID, instance1.getState());
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }
}
