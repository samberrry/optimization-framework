package org.cloudbus.spotsim.pricing.db;

import static org.cloudbus.spotsim.main.Constants.*;

import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;

import org.cloudbus.spotsim.enums.AZ;
import org.cloudbus.spotsim.enums.InstanceType;
import org.cloudbus.spotsim.enums.OS;
import org.cloudbus.spotsim.enums.Region;
import org.cloudbus.spotsim.main.config.Config;
import org.cloudbus.spotsim.main.config.SimProperties;
import org.cloudbus.spotsim.spothistory.SpotPriceRecord;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeSpotPriceHistoryRequest;
import com.amazonaws.services.ec2.model.DescribeSpotPriceHistoryResult;
import com.amazonaws.services.ec2.model.SpotPrice;

/**
 * 
 * Da Grabba
 * 
 * Reads price history from Amazon EC2 via its Java API
 * 
 * @see AmazonEC2Client
 * 
 * @author William Voorsluys - williamvoor@gmail.com
 * 
 */
public class TheGrabber {

    public static class ConcurrentPriceGrabber implements Runnable {

	private final boolean append;

	private final InstanceType type;

	private final OS os;

	private final Region region;

	private final Calendar from;

	private final Calendar to;

	private final AZ availabilityZone;

	private final HistoryPersistenceManager h;

	private static final Map<String, ReentrantLock> keyLocks = new HashMap<String, ReentrantLock>();

	public ConcurrentPriceGrabber(final boolean append, final InstanceType type, final OS os,
		final Region region, final AZ availabilityZone, final Calendar from,
		final Calendar to, HistoryPersistenceManager hpm) {
	    super();
	    this.append = append;
	    this.type = type;
	    this.os = os;
	    this.region = region;
	    this.from = from;
	    this.to = to;
	    this.availabilityZone = availabilityZone;
	    this.h = hpm;
	}

	@Override
	public void run() {
	    final String key = HistoryPersistenceManager.key(this.region, this.availabilityZone,
		this.type, this.os);

	    SortedSet<SpotPriceRecord> newPrices = null;
	    try {
		newPrices = TheGrabber.fetchSpotHistory(this.region, this.availabilityZone,
		    this.type, this.os, this.from, this.to);
	    } catch (final Exception e) {
		System.err.println("Error fetching prices for "
			+ this.region.getAmazonName()
			+ this.availabilityZone
			+ "-"
			+ this.type
			+ "-"
			+ this.os
			+ ", Ex:"
			+ e.getMessage());
		return;
	    }

	    if (newPrices != null) {
		ReentrantLock keyLock;
		synchronized (keyLocks) {
		    if (keyLocks.containsKey(key)) {
			keyLock = keyLocks.get(key);
		    } else {
			keyLock = new ReentrantLock();
			keyLocks.put(key, keyLock);
		    }
		}

		keyLock.lock();
		if (this.append
			&& this.h.contains(this.region, this.availabilityZone, this.type, this.os)) {
		    final SortedSet<SpotPriceRecord> list = this.h.getFromDb(this.region,
			this.availabilityZone, this.type, this.os);
		    System.out.println("Returned prices for: "
			    + this.region.getAmazonName()
			    + this.availabilityZone
			    + "-"
			    + this.type
			    + "-"
			    + this.os
			    + " from "
			    + Config.formatDate(newPrices.first().getDate())
			    + " to "
			    + Config.formatDate(newPrices.last().getDate())
			    + " Existing: from "
			    + Config.formatDate(list.first().getDate())
			    + " to "
			    + Config.formatDate(list.last().getDate()));
		    list.addAll(newPrices);
		    this.h.addToDb(this.region, this.availabilityZone, this.type, this.os, list);
		} else {
		    System.out.println("Returned prices for: "
			    + this.region.getAmazonName()
			    + this.availabilityZone
			    + "-"
			    + this.type
			    + "-"
			    + this.os
			    + " from "
			    + Config.formatDate(newPrices.first().getDate())
			    + " to "
			    + Config.formatDate(newPrices.last().getDate()));
		    this.h.addToDb(this.region, this.availabilityZone, this.type, this.os,
			newPrices);
		}
		keyLock.unlock();
	    }
	}
    }

    /*
     * Number of concurrent threads to use when sending price requests to
     * Amazon. Too many threads may cause requests to be throttled. Too few
     * threads makes the process slow
     */
    private static final int THREADS = 5;

    private static HistoryPersistenceManager hpm = new HistoryPersistenceManager(
	SimProperties.PRICING_HISTORY_MAP_DIR.asString());

    /**
     * This method will contact Amazon EC2 to obtain up-to-date prices for all
     * instances
     * 
     * @param from
     *        TODO
     * @param to
     *        TODO
     * @param regions
     *        TODO
     * 
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public static void fetchAllTypes(final boolean append, final Calendar from, final Calendar to,
	    EnumSet<Region> regions) throws InterruptedException, ExecutionException {

	if (to.before(from)) {
	    throw new IllegalArgumentException();
	}

	final ExecutorService exec = Executors.newFixedThreadPool(THREADS);
	final List<Future<?>> futures = new LinkedList<Future<?>>();

	for (final InstanceType type : InstanceType.values()) {
	    for (final OS os : OS.values()) {
		for (final Region region : regions) {
		    for (final AZ az : region.getAvailabilityZones()) {
			for (final Calendar cal = (Calendar) from.clone(); cal.before(to); cal.add(
			    Calendar.DAY_OF_MONTH, 10)) {
			    final Calendar from2 = (Calendar) cal.clone();
			    final Calendar to2 = (Calendar) from2.clone();
			    to2.add(Calendar.DAY_OF_MONTH, 10);
			    final Future<?> future = exec.submit(new ConcurrentPriceGrabber(append,
				type, os, region, az, from2, to2, hpm));
			    futures.add(future);
			}
		    }
		}
	    }
	}

	for (final Future<?> future : futures) {
	    future.get();
	}
    }

    public static SortedSet<SpotPriceRecord> fetchSpotHistory(final Region region, final AZ az,
	    final InstanceType type, final OS os) {

	return fetchSpotHistory(region, az, type, os, START_TIME, END_TIME);
    }

    public static SortedSet<SpotPriceRecord> fetchSpotHistory(final Region region, final AZ az,
	    final InstanceType type, final OS os, final Calendar from, final Calendar to) {

	final AWSCredentials awsCredentials = new BasicAWSCredentials(ACCESS_KEY, SECRET_KEY);

	final String types = type.getName();
	final AmazonEC2Client amazonEC2Client = new AmazonEC2Client(awsCredentials);
	amazonEC2Client.setEndpoint("ec2." + region.getAmazonName() + ".amazonaws.com");
	final DescribeSpotPriceHistoryRequest request = new DescribeSpotPriceHistoryRequest()
	    .withStartTime(from.getTime()).withEndTime(to.getTime()).withInstanceTypes(types)
	    .withProductDescriptions(os.getAmazonName())
	    .withAvailabilityZone(region.getAmazonName() + az);
	final DescribeSpotPriceHistoryResult history = amazonEC2Client
	    .describeSpotPriceHistory(request);

	final List<SpotPrice> returnedPriceList = history.getSpotPriceHistory();

	if (!returnedPriceList.isEmpty()) {

	    final SortedSet<SpotPriceRecord> priceSet = new TreeSet<SpotPriceRecord>();

	    for (final SpotPrice spotPrice : returnedPriceList) {

		final Date timestamp = spotPrice.getTimestamp();
		priceSet.add(new SpotPriceRecord(timestamp, Double.parseDouble(spotPrice
		    .getSpotPrice())));
	    }

	    return priceSet;
	}
	return null;
    }

    public static SortedSet<SpotPriceRecord> fetchSpotHistory(Region region, AZ azId,
	    InstanceType type, OS os, long from, long to) {
	Calendar fromDate = SimProperties.SIM_START_TIME.asDate();
	fromDate.add(Calendar.SECOND, (int) from);

	Calendar toDate = SimProperties.SIM_END_TIME.asDate();
	toDate.add(Calendar.SECOND, (int) to);

	return fetchSpotHistory(region, azId, type, os, fromDate, toDate);
    }
}
