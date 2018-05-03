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
import java.util.Map.Entry;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.lists.PeList;
import org.cloudbus.cloudsim.provisioners.PeProvisioner;

/**
 * VmSchedulerTimeShared is a VMM allocation policy that allocates one or more
 * Pe to a VM, and allows sharing of PEs by multiple VMs. This class also
 * implements 10% performance degration due to VM migration.
 * 
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 1.0
 */
public class VmSchedulerTimeShared extends VmScheduler {

    /** The mips map requested. */
    private Map<Integer, List<Double>> mipsMapRequested;

    /** The pes in use. */
    private int pesInUse;

    /** The vms in migration. */
    private List<Integer> vmsMigratingOut;

    /**
     * Instantiates a new vm scheduler time shared.
     * 
     * @param pelist
     *        the pelist
     */
    public VmSchedulerTimeShared(List<? extends Pe> pelist) {
	super(pelist);
	this.mipsMapRequested = new HashMap<Integer, List<Double>>();
	this.vmsMigratingOut = new ArrayList<Integer>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see cloudsim.VmScheduler#allocatePesForVm(cloudsim.Vm, java.util.List)
     */
    @Override
    public boolean allocatePesForVm(Vm vm, List<Double> mipsShareRequested) {
	/**
	 * TODO: add the same to RAM and BW provisioners
	 */
	if (vm.isInMigration()) {
	    if (!this.vmsMigratingOut.contains(vm.getId())) {
		this.vmsMigratingOut.add(vm.getId());
	    }
	} else {
	    if (this.vmsMigratingOut.contains(vm.getId())) {
		this.vmsMigratingOut.remove(vm.getId());
	    }
	}
	boolean result = allocatePesForVm(vm.getId(), mipsShareRequested);
	updatePeProvisioning();
	return result;
    }

    /**
     * Allocate pes for vm.
     * 
     * @param vmUid
     *        the vm uid
     * @param mipsShareRequested
     *        the mips share requested
     * 
     * @return true, if successful
     */
    protected boolean allocatePesForVm(int vmUid, List<Double> mipsShareRequested) {
	this.mipsMapRequested.put(vmUid, mipsShareRequested);
	setPesInUse(getPesInUse() + mipsShareRequested.size());

	double totalRequestedMips = 0;
	double peMips = getPeCapacity();
	for (Double mips : mipsShareRequested) {
	    if (mips > peMips) { // each virtual PE of a VM must require not
				 // more than the capacity of a physical PE
		return false;
	    }
	    totalRequestedMips += mips;
	}

	List<Double> mipsShareAllocated = new ArrayList<Double>();
	for (Double mipsRequested : mipsShareRequested) {
	    if (this.vmsMigratingOut.contains(vmUid)) {
		mipsRequested *= 0.9; // performance degradation due to
				      // migration = 10% MIPS
	    }
	    mipsShareAllocated.add(mipsRequested);
	}

	Log.logger.info("["
		+ Double.toString(CloudSim.clock())
		+ "] "
		+ "Available MIPS in this Host "
		+ getAvailableMips());

	if (getAvailableMips() >= totalRequestedMips) {
	    this.mipsMap.put(vmUid, mipsShareAllocated);
	    setAvailableMips(getAvailableMips() - totalRequestedMips);
	    Log.logger.info("["
		    + Double.toString(CloudSim.clock())
		    + "] "
		    + this.getClass().getCanonicalName()
		    + ", Allocated "
		    + totalRequestedMips
		    + " for VM "
		    + vmUid
		    + ", available: "
		    + getAvailableMips());
	} else {
	    int pesSkipped = 0;
	    for (List<Double> mipsMap : this.mipsMap.values()) {
		for (int i = 0; i < mipsMap.size(); i++) {
		    if (mipsMap.get(i) == 0) {
			pesSkipped++;
			continue;
		    }
		}
	    }

	    double shortage = (totalRequestedMips - getAvailableMips())
		    / (getPesInUse() - pesSkipped);

	    this.mipsMap.put(vmUid, mipsShareAllocated);
	    setAvailableMips(0);

	    double additionalShortage = 0;
	    do {
		additionalShortage = 0;
		for (List<Double> mipsMap : this.mipsMap.values()) {
		    for (int i = 0; i < mipsMap.size(); i++) {
			if (mipsMap.get(i) == 0) {
			    continue;
			}
			if (mipsMap.get(i) >= shortage) {
			    mipsMap.set(i, mipsMap.get(i) - shortage);
			} else {
			    additionalShortage += shortage - mipsMap.get(i);
			    mipsMap.set(i, 0.0);
			}
			if (mipsMap.get(i) == 0) {
			    pesSkipped++;
			}
		    }
		}
		shortage = additionalShortage / (getPesInUse() - pesSkipped);
	    } while (additionalShortage > 0);
	}

	return true;
    }

    /**
     * Update allocation of VMs on PEs.
     */
    protected void updatePeProvisioning() {
	Iterator<Pe> peIterator = getPeList().iterator();
	Pe pe = peIterator.next();
	PeProvisioner peProvisioner = pe.getPeProvisioner();
	peProvisioner.deallocateMipsForAllVms();
	double availableMips = peProvisioner.getAvailableMips();
	for (Entry<Integer, List<Double>> entry : this.mipsMap.entrySet()) {
	    int vmUid = entry.getKey();
	    for (double mips : entry.getValue()) {
		if (availableMips >= mips) {
		    peProvisioner.allocateMipsForVm(vmUid, mips);
		    availableMips -= mips;
		} else {
		    while (mips >= 0) {
			peProvisioner.allocateMipsForVm(vmUid, availableMips);
			mips -= availableMips;
			if (mips <= 0.1) {
			    mips = 0;
			    break;
			}
			if (!peIterator.hasNext()) {
			    Log.logger.info("["
				    + Double.toString(CloudSim.clock())
				    + "] "
				    + "There is no enough MIPS ("
				    + mips
				    + ") to accommodate VM "
				    + vmUid);
			}
			pe = peIterator.next();
			peProvisioner = pe.getPeProvisioner();
			peProvisioner.deallocateMipsForAllVms();
			availableMips = peProvisioner.getAvailableMips();
		    }
		}
	    }
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see cloudsim.VmScheduler#deallocatePesForVm(cloudsim.Vm)
     */
    @Override
    public void deallocatePesForVm(Vm vm) {
	this.mipsMapRequested.remove(vm.getId());
	setPesInUse(0);
	this.mipsMap.clear();
	setAvailableMips(PeList.getTotalMips(getPeList()));

	for (Pe pe : getPeList()) {
	    pe.getPeProvisioner().deallocateMipsForVm(vm);
	}

	for (Entry<Integer, List<Double>> entry : this.mipsMapRequested.entrySet()) {
	    allocatePesForVm(entry.getKey(), entry.getValue());
	}

	updatePeProvisioning();
    }

    /**
     * Releases PEs allocated to all the VMs.
     * 
     * @param vm
     *        the vm
     * 
     * @pre $none
     * @post $none
     */
    @Override
    public void deallocatePesForAllVms() {
	super.deallocatePesForAllVms();
	this.mipsMapRequested.clear();
	setPesInUse(0);
    }

    /**
     * Returns maximum available MIPS among all the PEs. For the time shared
     * policy it is just all the avaiable MIPS.
     * 
     * @return max mips
     */
    @Override
    public double getMaxAvailableMips() {
	return getAvailableMips();
    }

    /**
     * Sets the pes in use.
     * 
     * @param pesInUse
     *        the new pes in use
     */
    protected void setPesInUse(int pesInUse) {
	this.pesInUse = pesInUse;
    }

    /**
     * Gets the pes in use.
     * 
     * @return the pes in use
     */
    protected int getPesInUse() {
	return this.pesInUse;
    }
}
