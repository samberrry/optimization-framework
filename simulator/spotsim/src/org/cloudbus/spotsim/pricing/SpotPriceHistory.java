package org.cloudbus.spotsim.pricing;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableSet;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math.util.MathUtils;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.spotsim.broker.forecasting.MinMaxMean;
import org.cloudbus.spotsim.enums.AZ;
import org.cloudbus.spotsim.enums.InstanceType;
import org.cloudbus.spotsim.enums.OS;
import org.cloudbus.spotsim.enums.Region;
import org.cloudbus.spotsim.main.config.Config;
import org.cloudbus.spotsim.main.config.SimProperties;
import org.cloudbus.spotsim.pricing.db.PriceDB;
import org.cloudbus.spotsim.pricing.distr.RandomDistribution;
import org.cloudbus.spotsim.pricing.distr.RandomDistributionManager;
import org.cloudbus.utils.data.enummaps.DoubleEnumMap;

/**
 * Manages the trace of spot prices
 * 
 * @author William Voorsluys
 * 
 * @see SpotPriceHistoryTest
 * 
 */
public class SpotPriceHistory implements SpotPriceHistoryQuery {

    public final String DATE = "date";

    private final Region region;

    private final AZ az;

    private final DoubleEnumMap<InstanceType, OS, NavigableSet<PriceRecord>> inMemoryPrices;

    private Map<InstanceType, PriorityQueue<PriceRecord>> priceQueue;

    private final Caches caches;

    public static int cacheHits = 0;

    public SpotPriceHistory() {
	this(SimProperties.DC_DEFAULT_REGION.asEnum(Region.class), AZ.A);
    }

    public SpotPriceHistory(final Region region, final AZ az) {
	this(region, az, null);
    }

    public SpotPriceHistory(final Region region, final AZ az,
	    DoubleEnumMap<InstanceType, OS, NavigableSet<PriceRecord>> priceHistory) {
	super();
	this.region = region;
	this.az = az;
	this.caches = new Caches();
	this.inMemoryPrices = new DoubleEnumMap<InstanceType, OS, NavigableSet<PriceRecord>>();
	TimeZone.setDefault(TimeZone.getTimeZone("GMT"));

	if (SimProperties.PRICING_TRACE_GEN.asEnum(PriceTraceGen.class) == PriceTraceGen.HISTORY) {
	    if (priceHistory == null) {
		throw new IllegalArgumentException("Requires price history");
	    }
	    loadEC2Traces(priceHistory);
	} else {
	    loadRandomTraces();
	}

	for (final InstanceType type : InstanceType.values()) {
	    for (final OS os : OS.values()) {
		if (this.inMemoryPrices.containsKeys(type, os)) {		
			NavigableSet<PriceRecord> navigableSet = this.inMemoryPrices.get(type, os);
			PriceRecord min = Collections.min(navigableSet);
			double price = min.getPrice();
		    this.caches.cacheMin(type, os,price);
		}
	    }
	}
    }

    public NavigableSet<PriceRecord> getEvenlyDistributedPriceList2(final InstanceType type,
	    final OS os, final long start, final long end) {

	long movingDate = start;

	NavigableSet<PriceRecord> ret = new TreeSet<PriceRecord>();

	while (movingDate <= end) {
	    ret.add(new PriceRecord(movingDate, getPriceAtTime(type, os, movingDate)));
	    movingDate += 3600;
	}

	return ret;

    }

    public NavigableSet<PriceRecord> getEvenlyDistributedPriceList(final InstanceType type,
	    final OS os, final long start, final long end) {

	final Iterator<PriceRecord> iterator = this.getIteratorAtTime(type, os, start);
	long movingDate = start;
	final NavigableSet<PriceRecord> ret = new TreeSet<PriceRecord>();
	while (iterator.hasNext()) {
	    final PriceRecord next = iterator.next();
	    PriceRecord next2 = null;
	    if (iterator.hasNext()) {
		next2 = iterator.next();
	    }

	    if (next.getTime() > end) {
		break;
	    }

	    ret.add(new PriceRecord(movingDate, next.getPrice()));

	    if (next2 != null && next2.getTime() <= end) {
		movingDate += 3600;
		while (movingDate < next2.getTime()) {
		    final PriceRecord ad2 = new PriceRecord(movingDate, next.getPrice());
		    ret.add(ad2);
		    movingDate += 3600;
		}
	    }
	}
	return ret;
    }

    public Iterator<PriceRecord> getIteratorAtTime(final InstanceType type, final OS os,
	    final long start) {

	final NavigableSet<PriceRecord> priceList = this.inMemoryPrices.get(type, os);

	return getIteratorAtTime(type, os, start, priceList);
    }

    public static Iterator<PriceRecord> getIteratorAtTime(final InstanceType type, final OS os,
	    final long start, final NavigableSet<PriceRecord> priceList) {
	if (priceList == null) {
	    throw new RuntimeException("There are no prices for " + type + " " + os);
	}

	return new ReadOnlyIterator<PriceRecord>(getPriceSubset(start, priceList.last().getDate(),
	    priceList).iterator());
    }

    /**
     * @param os
     *        TODO
     * @return a record of when will be the next price change and how much. Null
     *         if there are no price changes anymore
     */
    public PriceRecord getNextPriceChange(final InstanceType type, final OS os) {

	if (this.priceQueue == null) {
	    this.priceQueue = new EnumMap<InstanceType, PriorityQueue<PriceRecord>>(
		InstanceType.class);
	}
	PriorityQueue<PriceRecord> queue = this.priceQueue.get(type);
	if (queue == null) {
	    queue = getPriceQueue(type, os, SimProperties.SIM_DAYS_OFFSET_TO_LOAD.asInt()* 24 * 3600 , SimProperties.SIM_DAYS_TO_LOAD.asInt() * 24 * 3600);
	    if (queue.isEmpty()) {
		return null;
	    }
	    this.priceQueue.put(type, queue);
	}
	return queue.poll();
    }

    public double getPriceAtTime(final InstanceType type, final OS os, final long timestamp) {

	final PriceRecord spotPriceRecord = getPriceRecordAtTime(type, os, timestamp);
	final double price = spotPriceRecord.getPrice();

	if (price <= 0D) {
	    throw new IllegalStateException("Bug: Price at "
		    + Config.formatDate(timestamp)
		    + " is invalid "
		    + price);
	}

	return price;
    }

    public PriorityQueue<PriceRecord> getPriceQueue(final InstanceType type, final OS os,
	    final long from, final long to) {
	final PriorityQueue<PriceRecord> queue = new PriorityQueue<PriceRecord>();
	queue.addAll(getPriceSubset(from, to, getPricesForType(type, os)));
	return queue;
    }

    public NavigableSet<PriceRecord> getPricesForType(final InstanceType type, final OS os) {
	return this.inMemoryPrices.get(type, os);
    }

    public boolean areTherePricesForType(final InstanceType type, final OS os) {
	return this.inMemoryPrices.containsKeys(type, os);
    }

    public static SortedSet<PriceRecord> getPriceSubset(final long from, final long to,
	    NavigableSet<PriceRecord> priceList) {

	final PriceRecord first = priceList.floor(new PriceRecord(from, 0));
	if (first == null) {
	    throw new RuntimeException("There are no prices for from time "
		    + from
		    + ", first available is "
		    + priceList.first().getDate());
	}

	PriceRecord last = priceList.higher(new PriceRecord(to, 0));
	if (last == null) {
	    PriceRecord lastAvailable = priceList.last();
	    if (lastAvailable.getDate() == to) {
		last = lastAvailable;
	    }
	}

	if (last == null) {
	    throw new RuntimeException("Subset: there are no prices for up to time "
		    + to
		    + ", last available is "
		    + priceList.last().getDate());
	}

	return priceList.subSet(first, last);
    }

    private void loadEC2Traces(
	    DoubleEnumMap<InstanceType, OS, NavigableSet<PriceRecord>> priceHistory) {

	for (final InstanceType type : InstanceType.values()) {
	    for (final OS os : OS.values()) {
		NavigableSet<PriceRecord> hist = priceHistory.get(type, os);
		if (hist != null) {
		    Log.logger.warning("Loaded prices for " + this.region + this.az + type + os);
		    hist = rectifyPriceRecord(hist);
		    if(SimProperties.PRICING_TRACE_HISTORY_MOD.asBoolean()){
				hist = changeInterArrivalTime(hist);
			}
		    this.inMemoryPrices.put(type, os, hist);
		} else {
		    Log.logger.warning("Missing prices for " + this.region + this.az + type + os);
		}
	    }
	}
    }

    private NavigableSet<PriceRecord> rectifyPriceRecord(
			NavigableSet<PriceRecord> hist) {
		PriceRecord first = hist.first();
		if(first.getTime() != 0){
			PriceRecord  newRec = new PriceRecord(0, first.getPrice());
			hist.add(newRec);
		}
		
		return hist;
	}

	private NavigableSet<PriceRecord> changeInterArrivalTime(
			NavigableSet<PriceRecord> hist) {
    	NavigableSet<PriceRecord> historyPrices = new TreeSet<>();
    	
    	for(PriceRecord record : hist){
    		long time = record.getTime();
    		time = (long) (time * SimProperties.PRICING_TRACE_HISTORY_MOD_FACTOR.asDouble());
    		PriceRecord prRec = new PriceRecord(time, record.getPrice());
    		historyPrices.add(prRec);
    	}
		return historyPrices;
	}

	public MinMaxMean minMaxMean(final InstanceType type, final OS os, final long from,
	    final long to) {

	MinMaxMean cached = this.caches.getMinMax(from, to, type, os);

	if (cached != null) {
	    return cached;
	}

	SortedSet<PriceRecord> subset = getPriceSubset(from, to, getPricesForType(type, os));
	final MinMaxMean minMax = minMaxMean(subset);

	long first = subset.first().getTime();
	long last = subset.last().getTime();
	this.caches.cacheMinMax(from, to, first, last, type, os, minMax);

	return minMax;
    }

    public static MinMaxMean minMaxMean(Collection<PriceRecord> subset) {
	final Iterator<PriceRecord> it = subset.iterator();
	double current = it.next().getPrice();

	double min = current;
	double max = current;
	double sum = current;
	int c = 1;

	while (it.hasNext()) {
	    current = it.next().getPrice();
	    sum += current;
	    c++;
	    if (current < min) {
		min = current;
	    } else if (current > max) {
		max = current;
	    }
	}

	return new MinMaxMean(min, max, sum / c);
    }

    public NavigableSet<PriceRecord> randomPricePeriod(InstanceType type, OS os, final long from,
	    final long to) {
	final double minSpotPossible = type.getMinimumSpotPossible(this.region, os);
	final double maxSpotPossible = type.getMaximumSpotPossible(this.region, os);

	final NavigableSet<PriceRecord> l = new TreeSet<PriceRecord>();
	final RandomDistribution typePriceDistr = RandomDistributionManager.getPriceDistr(
	    this.region, this.az, type, Config.RNG);
	final RandomDistribution interPriceDistr = RandomDistributionManager.getInterPriceDistr(
	    this.region, this.az, type, Config.RNG);
	double previousPrice = MathUtils.round(typePriceDistr.nextDouble() / 100D, 3);

	long movingDate = from;
	l.add(new PriceRecord(movingDate, previousPrice));

	while (movingDate <= to) {
	    double newPrice = MathUtils.round(typePriceDistr.nextDouble() / 100D, 3);
	    int maxTries = 0;
	    while (maxTries <= 20
		    && newPrice == previousPrice
		    || newPrice < minSpotPossible
		    || newPrice > maxSpotPossible) {
		newPrice = MathUtils.round(typePriceDistr.nextDouble() / 100D, 3);
		maxTries++;
	    }
	    l.add(new PriceRecord(movingDate, newPrice));
	    previousPrice = newPrice;
	    movingDate += (int) (interPriceDistr.nextDouble() * 3600);
	}

	l.add(new PriceRecord(to, previousPrice));
	return l;
    }

    private PriceRecord getPriceRecordAtTime(final InstanceType type, final OS os,
	    final long timestamp) {
	final NavigableSet<PriceRecord> priceList = getPricesForType(type, os);

	return priceList.floor(new PriceRecord(timestamp, 0));
    }

    private void loadRandomTraces() {

	for (final InstanceType type : InstanceType.values()) {
	    for (OS os : OS.values()) {
		long start = 0L;
		// long start = -1L * 15 * 24 * 3600;
		final double minSpotPossible = type.getMinimumSpotPossible(this.region, os);
		final double maxSpotPossible = type.getMaximumSpotPossible(this.region, os);
		if (minSpotPossible >= maxSpotPossible) {
		    throw new IllegalArgumentException("Min spot price "
			    + minSpotPossible
			    + " must be lower than max "
			    + maxSpotPossible);
		}
		NavigableSet<PriceRecord> l = randomPricePeriod(type, os, start,
		    SimProperties.SIM_DAYS_TO_LOAD.asInt() * 24 * 3600);
		this.inMemoryPrices.put(type, os, l);
	    }
	}
    }

    public Set<InstanceType> getTypesAvailable(OS os) {
	return this.inMemoryPrices.keySet1(os);
    }

    public Set<OS> getOSsAvailable(InstanceType type) {
	return this.inMemoryPrices.keySet2(type);
    }

    public double getOnDemandPrice(InstanceType type, OS os) {
	return PriceDB.getOnDemandPrice(this.region, type, os);
    }

    public double computeVolatility(InstanceType type, OS os) {

	NavigableSet<PriceRecord> l = getEvenlyDistributedPriceList2(type, os, 0,
	    SimProperties.SIM_DAYS_TO_LOAD.asLong() * 24 * 3600);

	DescriptiveStatistics stat = new DescriptiveStatistics();

	for (Iterator<PriceRecord> iterator = l.iterator(); iterator.hasNext();) {
	    PriceRecord rec = iterator.next();
	    PriceRecord rec2 = null;
	    if (iterator.hasNext()) {
		rec2 = iterator.next();
	    }

	    if (rec2 != null) {
		double ln = Math.log(rec2.getPrice() / rec.getPrice());
		stat.addValue(ln);
	    }
	}
	return stat.getStandardDeviation() * Math.sqrt(l.size() - 1);
    }
}
