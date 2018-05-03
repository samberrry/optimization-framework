package org.cloudbus.spotsim.cloudprovider.instance;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;

import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.spotsim.enums.AZ;
import org.cloudbus.spotsim.enums.InstanceType;
import org.cloudbus.spotsim.enums.OS;
import org.cloudbus.spotsim.enums.PriceModel;
import org.cloudbus.spotsim.enums.Region;
import org.cloudbus.spotsim.pricing.PriceRecord;
import org.cloudbus.spotsim.pricing.SpotPriceHistory;
import org.cloudbus.utils.data.enummaps.DoubleEnumMap;

/**
 * This is an Instance factory. Creates virtual machine instances and keeps them
 * organised. Several collections are kept to make access to specific types of
 * instances faster.
 * 
 * @see Instance
 * 
 * @author William Voorsluys - williamvoor@gmail.com
 * 
 */
public class DatacenterManager {

    private static Comparator<Instance> bidComparator = new Comparator<Instance>() {
	@Override
	public int compare(final Instance o1, final Instance o2) {
	    final int bidCompare = Double.compare(o1.getBidPrice(), o2.getBidPrice());
	    if (bidCompare == 0) {
		return new Integer(o1.getId()).compareTo(o2.getId());
	    }
	    return bidCompare;
	}
    };

    /* All instances */
    private final Map<Integer, Instance> instancesById;

    /* Only spot instances */
    private final DoubleEnumMap<PriceModel, InstanceType, SortedSet<Instance>> byPriceModelAndType;

    private final Datacenter dc;

    private final SpotPriceHistory priceTrace;

    private final DoubleEnumMap<InstanceType, OS, Double> currentPrices;

    private final AZ az;

    private final Region region;

    public DatacenterManager(final SpotPriceHistory priceTrace, final AZ az, Region region,
	    int hosts) {
	this.region = region;
	this.az = az;
	this.priceTrace = priceTrace;
	this.dc = createDatacenter(hosts);
	this.instancesById = new HashMap<Integer, Instance>();
	this.byPriceModelAndType = new DoubleEnumMap<PriceModel, InstanceType, SortedSet<Instance>>();
	this.currentPrices = new DoubleEnumMap<InstanceType, OS, Double>();
    }

    private Datacenter createDatacenter(int hosts) {
	final List<Host> hostList = new ArrayList<Host>();

	final double computePower = InstanceType.maxCPUUnitsInstance.getComputePower();
	final int ram = 200 * InstanceType.maxMemoryInstance.getMem();
	final int cores = InstanceType.maxCoresUnitsInstance.getCores();

	for (int j = 0; j < hosts; j++) {
	    hostList.add(new Host(j, cores, ram, computePower, false));
	}

	final String arch = "x86";
	final String os = "Linux";
	final String vmm = "Xen";
	final double time_zone = 0;
	final double cost = 0.0;
	final double costPerMem = 0.0;
	final double costPerStorage = 0.0;
	final double costPerBw = 0.0;

	final DatacenterCharacteristics characteristics = new DatacenterCharacteristics(arch, os,
	    vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);
	return new Datacenter("dc-" + this.region.getAmazonName() + this.az, characteristics,
	    new VmAllocationPolicySimple(hostList), null, 10);
    }

    public void bidChanged(final int id, final double newBid) {
	final Instance instance = getInstanceById(id);

	final SortedSet<Instance> spotInstances = this.byPriceModelAndType.get(PriceModel.SPOT,
	    instance.getType());
	spotInstances.remove(instance);
	instance.setBidPrice(newBid);
	spotInstances.add(instance);
    }

    public void destroyInstance(final int instanceID) {
	final Instance toRemove = this.instancesById.remove(instanceID);
	if (toRemove == null) {
	    throw new IllegalStateException("Cannot destroy instance "
		    + instanceID
		    + ", it does not exist");
	}
	this.byPriceModelAndType.remove(toRemove.getPricing(), toRemove.getType());
	if (Log.logger.isLoggable(Level.FINE)) {
	    Log.logger.info(Log.clock() + "Instance " + instanceID + " destroyed");
	}
    }

    public AZ getAz() {
	return this.az;
    }

    public double getCurrentPrice(final InstanceType type, final OS os) {
	return this.currentPrices.get(type, os);
    }

    public int getDcEntityId() {
	return this.dc.getId();
    }

    public Instance getInstanceById(final int id) {
	return this.instancesById.get(id);
    }

    public PriceRecord getNextPriceChange(final InstanceType type, final OS os) {
	if (!this.priceTrace.areTherePricesForType(type, os)) {
	    return null;
	}
	return this.priceTrace.getNextPriceChange(type, os);
    }

    /**
     * Returns a set containing spot instances that have a bid price less then
     * or equal to <code>price</code>
     * 
     * @param os
     *        TODO
     * @param price
     * 
     * @return
     */
    public List<Instance> getOutOfBidSpotInstances(final InstanceType type, final OS os,
	    final double price) {
	final List<Instance> ret = new ArrayList<Instance>();

	if (this.byPriceModelAndType.containsKeys(PriceModel.SPOT, type)) {
	    final SortedSet<Instance> all = this.byPriceModelAndType.get(PriceModel.SPOT, type);
	    final SortedSet<Instance> headSet = all.headSet(new Instance(price + 0.00001D));
	    for (final Instance instance : headSet) {
		if (instance.getOs() == os && instance.getState().isRunning()) {
		    ret.add(instance);
		}
	    }
	}

	return ret;
    }

    public Set<Instance> getPendingSpotRequests(final InstanceType type, final OS os) {

	final Set<Instance> ret = new HashSet<Instance>();

	if (this.byPriceModelAndType.containsKeys(PriceModel.SPOT, type)) {

	    for (final Instance instance : this.byPriceModelAndType.get(PriceModel.SPOT, type)) {
		if (instance.getOs() == os && instance.getState() == InstanceState.PENDING) {
		    ret.add(instance);
		}
	    }
	}

	return ret;
    }

    public boolean instanceExists(final int id) {
	return this.instancesById.containsKey(id);
    }

    public Instance newInstance(final InstanceType instanceType, final OS os,
	    final PriceModel priceModel, final int cloudId, final double bid) {
	final Instance instance = new Instance(instanceType, os, priceModel, cloudId, bid, this);

	this.instancesById.put(instance.getId(), instance);

	SortedSet<Instance> set = this.byPriceModelAndType.get(priceModel, instanceType);
	if (set == null) {
	    set = new TreeSet<Instance>(bidComparator);
	    this.byPriceModelAndType.put(priceModel, instanceType, set);
	}
	set.add(instance);

	if (Log.logger.isLoggable(Level.FINE)) {
	    Log.logger.fine(Log.clock()
		    + "Creating Instance "
		    + instance.getId()
		    + ", "
		    + priceModel
		    + ", "
		    + instanceType
		    + ", "
		    + os);
	}

	return instance;
    }

    public void updatePrice(final InstanceType type, final OS os, final double newPrice) {
	this.currentPrices.put(type, os, newPrice);
    }
}