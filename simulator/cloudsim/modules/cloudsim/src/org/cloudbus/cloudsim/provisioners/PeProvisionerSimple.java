/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2010, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.provisioners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Vm;

/**
 * The Class PeProvisionerSimple.
 * 
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 2.0
 */
public class PeProvisionerSimple extends PeProvisioner {

    /** The pe table. */
    private Map<Integer, List<Double>> peTable;

    /**
     * Creates the PeProvisionerSimple object.
     * 
     * @param availableMips
     *        the available mips
     * 
     * @pre $none
     * @post $none
     */
    public PeProvisionerSimple(double availableMips) {
	super(availableMips);
	this.peTable = new HashMap<Integer, List<Double>>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * cloudsim.provisioners.PeProvisioner#allocateMipsForVM(cloudsim.power.VM,
     * int)
     */
    @Override
    public boolean allocateMipsForVm(Vm vm, double mips) {
	return allocateMipsForVm(vm.getId(), mips);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * cloudsim.provisioners.PeProvisioner#allocateMipsForVm(java.lang.String,
     * double)
     */
    @Override
    public boolean allocateMipsForVm(int vmUid, double mips) {
	if (getAvailableMips() < mips) {
	    return false;
	}

	List<Double> allocatedMips;

	if (this.peTable.containsKey(vmUid)) {
	    allocatedMips = this.peTable.get(vmUid);
	} else {
	    allocatedMips = new ArrayList<Double>();
	}

	allocatedMips.add(mips);

	setAvailableMips(getAvailableMips() - mips);
	this.peTable.put(vmUid, allocatedMips);

	return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * cloudsim.provisioners.PeProvisioner#allocateMipsForVM(cloudsim.power.VM,
     * java.util.ArrayList)
     */
    @Override
    public boolean allocateMipsForVm(Vm vm, List<Double> mips) {
	int totalMipsToAllocate = 0;
	for (double _mips : mips) {
	    totalMipsToAllocate += _mips;
	}

	if (getAvailableMips() + getTotalAllocatedMipsForVm(vm) < totalMipsToAllocate) {
	    return false;
	}

	setAvailableMips(getAvailableMips() + getTotalAllocatedMipsForVm(vm) - totalMipsToAllocate);
	this.peTable.put(vm.getId(), mips);

	return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see cloudsim.provisioners.PeProvisioner#deallocateMipsForAllVms()
     */
    @Override
    public void deallocateMipsForAllVms() {
	super.deallocateMipsForAllVms();
	this.peTable.clear();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * cloudsim.provisioners.PeProvisioner#getAllocatedMipsForVMByVirtualPeId
     * (cloudsim.power.VM, int)
     */
    @Override
    public double getAllocatedMipsForVmByVirtualPeId(Vm vm, int peId) {
	if (this.peTable.containsKey(vm.getId())) {
	    try {
		return this.peTable.get(vm.getId()).get(peId);
	    } catch (Exception e) {
	    }
	}
	return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * cloudsim.provisioners.PeProvisioner#getAllocatedMipsForVM(cloudsim.power
     * .VM)
     */
    @Override
    public List<Double> getAllocatedMipsForVm(Vm vm) {
	if (this.peTable.containsKey(vm.getId())) {
	    return this.peTable.get(vm.getId());
	}
	return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * cloudsim.provisioners.PeProvisioner#getTotalAllocatedMipsForVM(cloudsim
     * .power.VM)
     */
    @Override
    public double getTotalAllocatedMipsForVm(Vm vm) {
	if (this.peTable.containsKey(vm.getId())) {
	    double totalAllocatedMips = 0.0;
	    for (double mips : this.peTable.get(vm.getId())) {
		totalAllocatedMips += mips;
	    }
	    return totalAllocatedMips;
	}
	return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * cloudsim.provisioners.PeProvisioner#deallocateMipsForVM(cloudsim.power
     * .VM)
     */
    @Override
    public void deallocateMipsForVm(Vm vm) {
	if (this.peTable.containsKey(vm.getId())) {
	    for (double mips : this.peTable.get(vm.getId())) {
		setAvailableMips(getAvailableMips() + mips);
	    }
	    this.peTable.remove(vm.getId());
	}
    }
}
