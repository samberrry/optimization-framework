/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2010, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * VmSchedulerSpaceShared is a VMM allocation policy that allocates one or more
 * Pe to a VM, and doesn't allow sharing of PEs. If there is no free PEs to the
 * VM, allocation fails. Free PEs are not allocated to VMs
 * 
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 1.0
 */
public class VmSchedulerSpaceShared extends VmScheduler {

    /** Map containing VM ID and a vector of PEs allocated to this VM. */
    private Map<Integer, List<Pe>> peAllocationMap;

    /** The free pes vector. */
    private List<Pe> freePes;

    /**
     * Instantiates a new vm scheduler space shared.
     * 
     * @param pelist
     *        the pelist
     */
    public VmSchedulerSpaceShared(List<? extends Pe> pelist) {
	super(pelist);
	this.peAllocationMap = new HashMap<Integer, List<Pe>>();
	this.freePes = new ArrayList<Pe>();
	getFreePes().addAll(pelist);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * VmScheduler#allocatePesForVm(org.cloudbus.cloudsim
     * .Vm, java.util.List)
     */
    @Override
    public boolean allocatePesForVm(Vm vm, List<Double> mipsShare) {
	// if there is no enough free PEs, fails
	if (getFreePes().size() < mipsShare.size()) {
	    return false;
	}

	List<Pe> selectedPes = new ArrayList<Pe>();
	Iterator<Pe> peIterator = getFreePes().iterator();
	Pe pe = peIterator.next();
	double totalMips = 0;
	for (Double mips : mipsShare) {
	    if (mips <= pe.getMips()) {
		selectedPes.add(pe);
		if (!peIterator.hasNext()) {
		    break;
		}
		pe = peIterator.next();
		totalMips += mips;
	    }
	}
	if (mipsShare.size() > selectedPes.size()) {
	    return false;
	}

	getFreePes().removeAll(selectedPes);

	this.peAllocationMap.put(vm.getId(), selectedPes);
	this.mipsMap.put(vm.getId(), mipsShare);
	setAvailableMips(getAvailableMips() - totalMips);

	return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * VmScheduler#deallocatePesForVm(org.cloudbus.cloudsim
     * .Vm)
     */
    @Override
    public void deallocatePesForVm(Vm vm) {
	getFreePes().addAll(this.peAllocationMap.get(vm.getId()));
	this.peAllocationMap.remove(vm.getId());

	double totalMips = 0;
	for (double mips : this.mipsMap.get(vm.getId())) {
	    totalMips += mips;
	}
	setAvailableMips(getAvailableMips() + totalMips);

	this.mipsMap.remove(vm.getId());
    }

    /**
     * Gets the free pes vector.
     * 
     * @return the free pes vector
     */
    protected List<Pe> getFreePes() {
	return this.freePes;
    }

}
