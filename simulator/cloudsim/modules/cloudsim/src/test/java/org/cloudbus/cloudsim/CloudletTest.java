/*
 * Title:        CloudSim Toolkiimport static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;
c) 2009-2010, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 2.0
 */
public class CloudletTest {

    private static final long CLOUDLET_LENGTH = 1000;
    private static final long CLOUDLET_FILE_SIZE = 300;
    private static final long CLOUDLET_OUTPUT_SIZE = 300;

    private static final int PES_NUMBER = 2;

    private Cloudlet cloudlet;
    private UtilizationModel utilizationModelCpu;
    private UtilizationModel utilizationModelRam;
    private UtilizationModel utilizationModelBw;

    @Before
    public void setUp() throws Exception {
	this.utilizationModelCpu = new UtilizationModelStochastic();
	this.utilizationModelRam = new UtilizationModelStochastic();
	this.utilizationModelBw = new UtilizationModelStochastic();
	this.cloudlet = new Cloudlet(0, CLOUDLET_LENGTH, PES_NUMBER, CLOUDLET_FILE_SIZE,
	    CLOUDLET_OUTPUT_SIZE, this.utilizationModelCpu, this.utilizationModelRam,
	    this.utilizationModelBw);
    }

    @Test
    public void testCloudlet() {
	assertEquals(CLOUDLET_LENGTH, this.cloudlet.getLength(), 0);
	assertEquals(CLOUDLET_FILE_SIZE, this.cloudlet.getCloudletFileSize());
	assertEquals(CLOUDLET_OUTPUT_SIZE, this.cloudlet.getCloudletOutputSize());
	assertEquals(PES_NUMBER, this.cloudlet.getPesNumber());
	assertSame(this.utilizationModelCpu, this.cloudlet.getUtilizationModelCpu());
	assertSame(this.utilizationModelRam, this.cloudlet.getUtilizationModelRam());
	assertSame(this.utilizationModelBw, this.cloudlet.getUtilizationModelBw());
    }

    @Test
    public void testGetUtilizationOfCpu() {
	assertEquals(this.utilizationModelCpu.getUtilization(0),
	    this.cloudlet.getUtilizationOfCpu(0), 0);
    }

    @Test
    public void testGetUtilizationOfRam() {
	assertEquals(this.utilizationModelRam.getUtilization(0),
	    this.cloudlet.getUtilizationOfRam(0), 0);
    }

    @Test
    public void testGetUtilizationOfBw() {
	assertEquals(this.utilizationModelBw.getUtilization(0),
	    this.cloudlet.getUtilizationOfBw(0), 0);
    }

    @Test
    public void testCloudletAlternativeConstructor1() {
	this.cloudlet = new Cloudlet(0, CLOUDLET_LENGTH, PES_NUMBER, CLOUDLET_FILE_SIZE,
	    CLOUDLET_OUTPUT_SIZE, this.utilizationModelCpu, this.utilizationModelRam,
	    this.utilizationModelBw, true, new LinkedList<String>());
	testCloudlet();
	testGetUtilizationOfCpu();
	testGetUtilizationOfRam();
	testGetUtilizationOfBw();
    }

    @Test
    public void testCloudletAlternativeConstructor2() {
	this.cloudlet = new Cloudlet(0, CLOUDLET_LENGTH, PES_NUMBER, CLOUDLET_FILE_SIZE,
	    CLOUDLET_OUTPUT_SIZE, this.utilizationModelCpu, this.utilizationModelRam,
	    this.utilizationModelBw, new LinkedList<String>());
	testCloudlet();
	testGetUtilizationOfCpu();
	testGetUtilizationOfRam();
	testGetUtilizationOfBw();
    }

}
