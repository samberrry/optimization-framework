package org.cloudbus.spotsim.broker.resources;

import static org.junit.Assert.*;

import java.util.Set;

import org.cloudbus.spotsim.enums.AZ;
import org.cloudbus.spotsim.enums.InstanceType;
import org.cloudbus.spotsim.enums.OS;
import org.cloudbus.spotsim.enums.Region;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ResourceFactoryTest {

    private ResourceFactory fact;

    private Resource r1;

    private Resource r2;

    private Resource r3;

    private Resource r4;

    private Resource r5;

    private Resource r6;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {

	this.fact = new ResourceFactory();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void test1() {
	Region region = Region.getDefault();
	OS os = OS.getDefault();
	this.r1 = this.fact.newResource(region, AZ.A, InstanceType.M1SMALL, os, 0.05);
	this.r2 = this.fact.newResource(region, AZ.A, InstanceType.M1SMALL, os, 0.05);
	this.r3 = this.fact.newResource(region, AZ.A, InstanceType.M1SMALL, os, 0.05);
	this.r4 = this.fact.newResource(region, AZ.A, InstanceType.M1SMALL, os, 0.05);
	this.r5 = this.fact.newResource(region, AZ.A, InstanceType.M1SMALL, os, 0.05);
	this.r6 = this.fact.newResource(region, AZ.A, InstanceType.M1SMALL, os, 0.05);

	assertEquals(6, this.fact.getTotalNumberOfResources());
	assertEquals(ResourceState.PENDING, this.r1.getState());

	assertContainsAll();
	this.fact.updateResourceState(this.r1, ResourceState.ACTIVE);
	assertContainsAll();
	this.fact.updateResourceState(this.r1, ResourceState.IDLE);
	this.fact.updateResourceState(this.r2, ResourceState.ACTIVE);
	assertContainsAll();

	Set<Resource> resourcesByState = this.fact.getResourcesByState(ResourceState.IDLE);
	assertTrue(resourcesByState.contains(this.r1));

	this.fact.updateResourceState(this.r1, ResourceState.OUT_OF_BID);
	this.fact.updateResourceState(this.r1, ResourceState.DESTROYED);
	assertFalse(this.fact.getAllUsableResources().contains(this.r1));
    }

    private void assertContainsAll() {
	Set<Resource> allUsableResources = this.fact.getAllUsableResources();
	assertTrue(allUsableResources.contains(this.r1));
	assertTrue(allUsableResources.contains(this.r2));
	assertTrue(allUsableResources.contains(this.r3));
	assertTrue(allUsableResources.contains(this.r4));
	assertTrue(allUsableResources.contains(this.r5));
	assertTrue(allUsableResources.contains(this.r6));
    }
}
