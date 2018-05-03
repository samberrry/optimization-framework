/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2010, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.lists;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 2.0
 */
public class PeListTest {

    private static final double MIPS = 1000;

    private List<Pe> peList;

    @Before
    public void setUp() throws Exception {
	this.peList = new ArrayList<Pe>();

	this.peList.add(new Pe(0, new PeProvisionerSimple(MIPS)));
	this.peList.add(new Pe(1, new PeProvisionerSimple(MIPS)));
    }

    @Test
    public void testGetMips() {
	assertEquals(MIPS, PeList.getMips(this.peList, 0), 0);
	assertEquals(MIPS, PeList.getMips(this.peList, 1), 0);
	assertEquals(-1, PeList.getMips(this.peList, 2), 0);
    }

    @Test
    public void testGetTotalMips() {
	assertEquals(MIPS * this.peList.size(), PeList.getTotalMips(this.peList), 0);
    }

    @Test
    public void testSetPeStatus() {
	assertEquals(2, PeList.getFreePesNumber(this.peList));
	assertEquals(0, PeList.getBusyPesNumber(this.peList));
	assertTrue(PeList.setPeStatus(this.peList, 0, Pe.BUSY));
	assertEquals(Pe.BUSY, PeList.getById(this.peList, 0).getStatus());
	assertEquals(1, PeList.getFreePesNumber(this.peList));
	assertEquals(1, PeList.getBusyPesNumber(this.peList));
	assertTrue(PeList.setPeStatus(this.peList, 1, Pe.BUSY));
	assertEquals(Pe.BUSY, PeList.getById(this.peList, 1).getStatus());
	assertEquals(0, PeList.getFreePesNumber(this.peList));
	assertEquals(2, PeList.getBusyPesNumber(this.peList));
	assertFalse(PeList.setPeStatus(this.peList, 2, Pe.BUSY));
	assertEquals(0, PeList.getFreePesNumber(this.peList));
	assertEquals(2, PeList.getBusyPesNumber(this.peList));
    }

    @Test
    public void testSetStatusFailed() {
	assertEquals(Pe.FREE, PeList.getById(this.peList, 0).getStatus());
	assertEquals(Pe.FREE, PeList.getById(this.peList, 1).getStatus());
	PeList.setStatusFailed(this.peList, true);
	assertEquals(Pe.FAILED, PeList.getById(this.peList, 0).getStatus());
	assertEquals(Pe.FAILED, PeList.getById(this.peList, 1).getStatus());
	PeList.setStatusFailed(this.peList, false);
	assertEquals(Pe.FREE, PeList.getById(this.peList, 0).getStatus());
	assertEquals(Pe.FREE, PeList.getById(this.peList, 1).getStatus());

	PeList.setStatusFailed(this.peList, "test", 0, true);
	assertEquals(Pe.FAILED, PeList.getById(this.peList, 0).getStatus());
	assertEquals(Pe.FAILED, PeList.getById(this.peList, 1).getStatus());
	PeList.setStatusFailed(this.peList, "test", 0, false);
	assertEquals(Pe.FREE, PeList.getById(this.peList, 0).getStatus());
	assertEquals(Pe.FREE, PeList.getById(this.peList, 1).getStatus());
    }

    @Test
    public void testFreePe() {
	assertSame(this.peList.get(0), PeList.getFreePe(this.peList));
	PeList.setPeStatus(this.peList, 0, Pe.BUSY);
	assertSame(this.peList.get(1), PeList.getFreePe(this.peList));
	PeList.setPeStatus(this.peList, 1, Pe.BUSY);
	assertNull(PeList.getFreePe(this.peList));
    }

    @Test
    public void testGetMaxUtilization() {
	Vm vm0 = new Vm(0, 0, MIPS / 2, 1, 0, 0, 0, "", null);
	Vm vm1 = new Vm(1, 0, MIPS / 2, 1, 0, 0, 0, "", null);

	assertTrue(this.peList.get(0).getPeProvisioner().allocateMipsForVm(vm0, MIPS / 3));
	assertTrue(this.peList.get(1).getPeProvisioner().allocateMipsForVm(vm1, MIPS / 5));

	assertEquals(MIPS / 3 / MIPS, PeList.getMaxUtilization(this.peList), 0.001);
    }

    @Test
    public void testGetMaxUtilizationAmongVmsPes() {
	Vm vm0 = new Vm(0, 0, MIPS / 2, 1, 0, 0, 0, "", null);
	Vm vm1 = new Vm(1, 0, MIPS / 2, 1, 0, 0, 0, "", null);

	assertTrue(this.peList.get(0).getPeProvisioner().allocateMipsForVm(vm0, MIPS / 3));
	assertTrue(this.peList.get(1).getPeProvisioner().allocateMipsForVm(vm1, MIPS / 5));

	assertEquals(MIPS / 3 / MIPS, PeList.getMaxUtilizationAmongVmsPes(this.peList, vm0), 0.001);
	assertEquals(MIPS / 5 / MIPS, PeList.getMaxUtilizationAmongVmsPes(this.peList, vm1), 0.001);
    }

}
