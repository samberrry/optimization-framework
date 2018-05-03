/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2010, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.lists.PeList;

/**
 * VmScheduler is an abstract class that represents the policy used by a VMM to
 * share processing power among VMs running in a host.
 * 
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 1.0
 */
public abstract class VmScheduler {

    /** The peList. */
    private List<? extends Pe> peList;

    /** The MIPS that are currently allocated to the VMs. */
    protected Map<Integer, List<Double>> mipsMap;

    /** The total available mips. */
    private double availableMips;

    /**
     * Creates a new HostAllocationPolicy.
     * 
     * @param pelist
     *        the pelist
     * 
     * @pre peList != $null
     * @post $none
     */
    public VmScheduler(List<? extends Pe> pelist) {
	setPeList(pelist);
	this.mipsMap = new HashMap<Integer, List<Double>>();
	setAvailableMips(PeList.getTotalMips(getPeList()));
	Log.logger.finer("["
		+ Double.toString(CloudSim.clock())
		+ "] "
		+ "Initial available MIPS: "
		+ getAvailableMips());
    }

    /**
     * Allocates PEs for a VM.
     * 
     * @param vm
     *        the vm
     * @param mipsShare
     *        the mips share
     * 
     * @return $true if this policy allows a new VM in the host, $false
     *         otherwise
     * 
     * @pre $none
     * @post $none
     */
    public abstract boolean allocatePesForVm(Vm vm, List<Double> mipsShare);

    /**
     * Releases PEs allocated to a VM.
     * 
     * @param vm
     *        the vm
     * 
     * @pre $none
     * @post $none
     */
    public abstract void deallocatePesForVm(Vm vm);

    /**
     * Releases PEs allocated to all the VMs.
     * 
     * @param vm
     *        the vm
     * 
     * @pre $none
     * @post $none
     */
    public void deallocatePesForAllVms() {
	this.mipsMap.clear();
	setAvailableMips(PeList.getTotalMips(getPeList()));
	for (Pe pe : getPeList()) {
	    pe.getPeProvisioner().deallocateMipsForAllVms();
	}
    }

    /**
     * Returns the MIPS share of each Pe that is allocated to a given VM.
     * 
     * @param vm
     *        the vm
     * 
     * @return an array containing the amount of MIPS of each pe that is
     *         available to the VM
     * 
     * @pre $none
     * @post $none
     */
    public List<Double> getAllocatedMipsForVm(Vm vm) {
	return this.mipsMap.get(vm.getId());
    }

    /**
     * Gets the total allocated MIPS for a VM over all the PEs.
     * 
     * @param vm
     *        the vm
     * 
     * @return the allocated mips for vm
     */
    public double getTotalAllocatedMipsForVm(Vm vm) {
	double allocated = 0;
	List<Double> mipsMap = getAllocatedMipsForVm(vm);
	if (mipsMap != null) {
	    for (double mips : mipsMap) {
		allocated += mips;
	    }
	}
	return allocated;
    }

    /**
     * Returns maximum available MIPS among all the PEs.
     * 
     * @return max mips
     */
    public double getMaxAvailableMips() {
	if (getPeList() == null) {
	    Log.logger.info("[" + Double.toString(CloudSim.clock()) + "] " + "Pe list is empty");
	    return 0;
	}

	double max = 0.0;
	for (Pe pe : getPeList()) {
	    double tmp = pe.getPeProvisioner().getAvailableMips();
	    if (tmp > max) {
		max = tmp;
	    }
	}

	return max;
    }

    /**
     * Returns PE capacity in MIPS.
     * 
     * @return mips
     */
    public double getPeCapacity() {
	if (getPeList() == null) {
	    Log.logger.info("[" + Double.toString(CloudSim.clock()) + "] " + "Pe list is empty");
	    return 0;
	}
	return getPeList().get(0).getMips();
    }

    /**
     * Gets the vm list.
     * 
     * @return the vm list
     */
    @SuppressWarnings("unchecked")
    public <T extends Pe> List<T> getPeList() {
	return (List<T>) this.peList;
    }

    /**
     * Sets the vm list.
     * 
     * @param peList
     *        the pe list
     */
    protected <T extends Pe> void setPeList(List<T> peList) {
	this.peList = peList;
    }

    /**
     * Gets the free mips.
     * 
     * @return the free mips
     */
    public double getAvailableMips() {
	return this.availableMips;
    }

    /**
     * Sets the free mips.
     * 
     * @param availableMips
     *        the new free mips
     */
    protected void setAvailableMips(double availableMips) {
	this.availableMips = availableMips;
    }

}
