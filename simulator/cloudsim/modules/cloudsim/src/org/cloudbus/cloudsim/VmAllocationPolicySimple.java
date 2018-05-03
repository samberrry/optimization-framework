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
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.cloudbus.cloudsim.core.CloudSim;

/**
 * VmAllocationPolicySimple is an VmAllocationPolicy that chooses, as the host
 * for a VM, the host with less PEs in use.
 * 
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 1.0
 */
public class VmAllocationPolicySimple extends VmAllocationPolicy {

    /** The vm table. */
    private final Map<Integer, Host> vmTable;

    /** The used pes. */
    private final Map<Integer, Integer> usedPes;

    /** The free pes. */
    private final List<Integer> freePes;

    /**
     * Creates the new VmAllocationPolicySimple object.
     * 
     * @param list
     *        the list
     * 
     * @pre $none
     * @post $none
     */
    public VmAllocationPolicySimple(List<? extends Host> list) {
	super(list);

	this.freePes = new ArrayList<Integer>();
	for (Host host : getHostList()) {
	    getFreePes().add(host.getPesNumber());

	}

	this.vmTable = new HashMap<Integer, Host>();
	this.usedPes = new HashMap<Integer, Integer>();
    }

    /**
     * Allocates a host for a given VM.
     * 
     * @param vm
     *        VM specification
     * 
     * @return $true if the host could be allocated; $false otherwise
     * 
     * @pre $none
     * @post $none
     */
    @Override
    public boolean allocateHostForVm(Vm vm) {
	int requiredPes = vm.getPesNumber();
	boolean success = false;
	int tries = 0;
	List<Integer> freePesTmp = new ArrayList<Integer>();
	for (Integer freePe : getFreePes()) {
	    freePesTmp.add(freePe);
	}

	if (!this.vmTable.containsKey(vm.getId())) { // if this vm was not
						     // created
	    do {// we still trying until we find a host or until we try all of
		// them
		int moreFree = Integer.MIN_VALUE;
		int idx = -1;

		// we want the host with less pes in use
		for (int i = 0; i < freePesTmp.size(); i++) {
		    if (freePesTmp.get(i) > moreFree) {
			moreFree = freePesTmp.get(i);
			idx = i;
		    }
		}

		Host host = getHostList().get(idx);
		success = host.vmCreate(vm);

		if (success) { // if vm were succesfully created in the host
		    if (Log.logger.isLoggable(Level.FINE)) {
			Log.logger.fine(Log.clock()
				+ "VmAllocationPolicy: VM '"
				+ vm.getId()
				+ "', Chosen host: #"
				+ host.getId()
				+ " idx:"
				+ idx);
		    }
		    this.vmTable.put(vm.getId(), host);
		    this.usedPes.put(vm.getId(), requiredPes);
		    getFreePes().set(idx, getFreePes().get(idx) - requiredPes);
		    return true;
		}
		freePesTmp.set(idx, Integer.MIN_VALUE);
		tries++;
	    } while (!success && tries < getFreePes().size());

	}

	return false;
    }

    /**
     * Releases the host used by a VM.
     * 
     * @param vm
     *        the vm
     * 
     * @pre $none
     * @post none
     */
    @Override
    public void deallocateHostForVm(Vm vm) {

	Host host = this.vmTable.remove(vm.getId());
	int idx = getHostList().indexOf(host);
	int pes = this.usedPes.remove(vm.getId());
	if (host != null) {
	    host.vmDestroy(vm);
	    getFreePes().set(idx, getFreePes().get(idx) + pes);
	}
    }

    /**
     * Gets the host that is executing the given VM belonging to the given user.
     * 
     * @param vm
     *        the vm
     * 
     * @return the Host with the given vmID and userID; $null if not found
     * 
     * @pre $none
     * @post $none
     */
    @Override
    public Host getHost(Vm vm) {
	Log.logger.finer("["
		+ Double.toString(CloudSim.clock())
		+ "] "
		+ "VmAllocationPolicy: Searching host for VM '"
		+ vm.getId()
		+ "'");
	return this.vmTable.get(vm.getId());
    }

    /**
     * Gets the host that is executing the given VM belonging to the given user.
     * 
     * @param vmId
     *        the vm id
     * @return the Host with the given vmID and userID; $null if not found
     * 
     * @pre $none
     * @post $none
     */
    @Override
    public Host getHost(int vmId) {
	final Host host = this.vmTable.get(vmId);
	if (host == null) {
	    throw new IllegalArgumentException("There are no hosts associated to VM: " + vmId);
	}
	return host;
    }

    /**
     * Gets the free pes.
     * 
     * @return the free pes
     */
    protected List<Integer> getFreePes() {
	return this.freePes;
    }

    /*
     * (non-Javadoc)
     * 
     * @see cloudsim.VmAllocationPolicy#optimizeAllocation(double,
     * cloudsim.VmList, double)
     */
    @Override
    public List<Map<String, Object>> optimizeAllocation(List<? extends Vm> vmList) {
	// TODO Auto-generated method stub
	return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * VmAllocationPolicy#allocateHostForVm(org.cloudbus
     * .cloudsim.Vm, Host)
     */
    @Override
    public boolean allocateHostForVm(Vm vm, Host host) {
	if (host.vmCreate(vm)) { // if vm has been succesfully created in the
				 // host
	    this.vmTable.put(vm.getId(), host);
	    Log.formatLine(
		"%.2f: VM #" + vm.getId() + " has been allocated to the host #" + host.getId(),
		CloudSim.clock());
	    return true;
	}

	return false;
    }
}
