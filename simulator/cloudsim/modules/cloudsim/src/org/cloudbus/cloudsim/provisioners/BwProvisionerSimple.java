/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2010, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.provisioners;

import java.util.HashMap;
import java.util.Map;

import org.cloudbus.cloudsim.Vm;

/**
 * BwProvisionerSimple is a class that implements a simple best effort
 * allocation policy: if there is bw available to request, it allocates;
 * otherwise, it fails.
 * 
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 1.0
 */
public class BwProvisionerSimple extends BwProvisioner {

    /** The bw table. */
    private Map<Integer, Long> bwTable;

    /**
     * Instantiates a new bw provisioner simple.
     * 
     * @param bw
     *        the bw
     */
    public BwProvisionerSimple(long bw) {
	super(bw);
	this.bwTable = new HashMap<Integer, Long>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see cloudsim.provisioners.BwProvisioner#allocateBwForVm(cloudsim.Vm,
     * long)
     */
    @Override
    public boolean allocateBwForVm(Vm vm, long bw) {
	deallocateBwForVm(vm);

	if (getAvailableBw() >= bw) {
	    setAvailableBw(getAvailableBw() - bw);
	    this.bwTable.put(vm.getId(), bw);
	    vm.setCurrentAllocatedBw(getAllocatedBwForVm(vm));
	    return true;
	}

	vm.setCurrentAllocatedBw(getAllocatedBwForVm(vm));
	return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see cloudsim.provisioners.BwProvisioner#getAllocatedBwForVm(cloudsim.Vm)
     */
    @Override
    public long getAllocatedBwForVm(Vm vm) {
	if (this.bwTable.containsKey(vm.getId())) {
	    return this.bwTable.get(vm.getId());
	}
	return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see cloudsim.provisioners.BwProvisioner#deallocateBwForVm(cloudsim.Vm)
     */
    @Override
    public void deallocateBwForVm(Vm vm) {
	if (this.bwTable.containsKey(vm.getId())) {
	    long amountFreed = this.bwTable.remove(vm.getId());
	    setAvailableBw(getAvailableBw() + amountFreed);
	    vm.setCurrentAllocatedBw(0);
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see cloudsim.provisioners.BwProvisioner#deallocateBwForVm(cloudsim.Vm)
     */
    @Override
    public void deallocateBwForAllVms() {
	super.deallocateBwForAllVms();
	this.bwTable.clear();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * gridsim.virtualization.power.provisioners.BWProvisioner#isSuitableForVm
     * (gridsim.virtualization.power.VM, long)
     */
    @Override
    public boolean isSuitableForVm(Vm vm, long bw) {
	long allocatedBw = getAllocatedBwForVm(vm);
	boolean result = allocateBwForVm(vm, bw);
	deallocateBwForVm(vm);
	if (allocatedBw > 0) {
	    allocateBwForVm(vm, allocatedBw);
	}
	return result;
    }
}
