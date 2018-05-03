package org.cloudbus.spotsim.pricing;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.spotsim.enums.AZ;
import org.cloudbus.spotsim.enums.InstanceType;
import org.cloudbus.spotsim.enums.OS;
import org.cloudbus.spotsim.enums.Region;
import org.cloudbus.spotsim.main.VolatilityCombo;
import org.cloudbus.spotsim.main.config.Config;
import org.cloudbus.spotsim.main.config.SimProperties;
import org.cloudbus.spotsim.pricing.db.HistoryPersistenceManager;
import org.cloudbus.spotsim.pricing.db.PriceDB;
import org.cloudbus.spotsim.pricing.db.TheGrabber;
import org.cloudbus.spotsim.pricing.distr.RandomDistributionManager;
import org.cloudbus.spotsim.spothistory.SpotPriceRecord;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.thoughtworks.xstream.XStream;

/**
 * @see SpotPriceHistory
 */
public class SpotPriceHistoryTest {

    private HistoryPersistenceManager historyPersistenceManager;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
	TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
	Log.init(null);
	Config.load();
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void countDays() throws Exception {

	SimProperties.PRICING_TRACE_GEN.set(PriceTraceGen.HISTORY);

	PriceDB.flushPriceDB();
	//RandomDistributionManager.flush();
	
	int highPriceOverall = 0;
	long timeHighOveral = 0;
//	HESSAM COMMENTED FOR ANY AZ
//	final EnumSet<Region> regions = EnumSet.of(Region.DEEPAK_TEST);
	final EnumSet<Region> regions = null;
	for (final Region region : regions) {
	    for (final AZ azId : region.getAvailabilityZones()) {
		SpotPriceHistory spotHist = PriceDB.getPriceTrace(region, azId);
		for (final InstanceType type : InstanceType.values()) {
		    for (final OS os : OS.values()) {
			if (spotHist.areTherePricesForType(type, os)) {
			    final NavigableSet<PriceRecord> list = spotHist.getPricesForType(type,
				os);
			    if (!list.isEmpty()) {
				double max = Double.MIN_VALUE;
				double min = Double.MAX_VALUE;
				int highprice = 0;
				long timeHigh = 0;
				long timeHighMax = Long.MIN_VALUE;
				long prev = list.first().getTime();
				for (PriceRecord rec : list) {
				    double price = rec.getPrice();
				    long date = rec.getDate();
				    if (price > max) {
					max = price;
				    } else if (price < min) {
					min = price;
				    }
				    if (price > type.getOnDemandPrice(region, os)) {
					highprice++;
					final long diff = date - prev;
					timeHigh += diff;
					if (diff > timeHighMax) {
					    timeHighMax = diff;
					}
				    }
				    prev = date;
				}

				highPriceOverall += highprice;
				timeHighOveral += timeHigh;
				double timeHighAv = highprice > 0 ? timeHigh / highprice : 0;
				final long first = list.first().getDate();
				final long last = list.last().getDate();
				final long diff = last - first;
				System.out.println(region.getAmazonName()
					+ azId
					+ "-"
					+ type
					+ "-"
					+ os
					+ " from "
					+ Config.formatDate(first)
					+ " to "
					+ Config.formatDate(last)
					+ " ("
					+ DateUtils.getSecsInDays(diff)
					+ ") max "
					+ max
					+ ", min "
					+ min
					+ ", high prices "
					+ highprice
					+ " time high "
					+ timeHigh / 3600D
					+ " avg "
					+ timeHighAv
					+ " timeHighMax "
					+ timeHighMax);

			    }
			} else {
			    System.out.println("No prices for "
				    + region.getAmazonName()
				    + azId
				    + "-"
				    + type
				    + "-"
				    + os);
			}
		    }
		}
	    }
	}
	double inHours = timeHighOveral / 3600D;
	System.out.println("high prices overall " + highPriceOverall + " time high " + inHours);
    }

    @Test
    public void evenlyDistr() throws Exception {

	SpotPriceHistory spotPriceHistory = PriceDB.getPriceTrace(Region.US_EAST, AZ.A);

	final InstanceType[] values = InstanceType.values();
	for (final InstanceType instanceType : values) {

	    final PrintStream stream = new PrintStream(new BufferedOutputStream(
		new FileOutputStream("spot-history-" + instanceType.getName())));

	    final NavigableSet<PriceRecord> list = spotPriceHistory.getEvenlyDistributedPriceList(
		instanceType, OS.LINUX, 0, SimProperties.SIM_DURATION_SECONDS.asLong());
	    for (final PriceRecord spotPriceRecord : list) {
		stream.println(spotPriceRecord);
		System.out.println(spotPriceRecord);
	    }
	    stream.close();
	}
    }

    @Test
    public void volatility() throws Exception {

	SimProperties.PRICING_TRACE_GEN.set(PriceTraceGen.RANDOM);

	NavigableSet<VolatilityCombo> volatSet = new TreeSet<>();
	double volat = volat(1D, 1D, 1D);
	volatSet.add(new VolatilityCombo(volat, 1D, 1D, 1D));

	for (double j = 1D; j < 3D; j += 0.1D) {
	    for (double k = 2D; k >= 0.1D; k -= 0.1D) {
		VolatilityCombo e = new VolatilityCombo(volat(1D, j, k), 1D, j, k);
		volatSet.add(e);
	    }
	}

	for (VolatilityCombo volatPOJO : volatSet) {
	    System.out.println(volatPOJO);
	}
    }

    private double volat(double multPriceMu, double multPriceSigma, double multTimeMu) {

	SimProperties.PRICING_PRICE_MU_MULT.set(multPriceMu);
	SimProperties.PRICING_PRICE_SIGMA_MULT.set(multPriceSigma);
	SimProperties.PRICING_TIME_MU_MULT.set(multTimeMu);
	RandomDistributionManager.load(Config.RNG);

	final int N = 30;
	DescriptiveStatistics d = new DescriptiveStatistics();
	for (int i = 0; i < N; i++) {
//		HESSAM COMMENTED FOR ANY AZ
//	    SpotPriceHistory hist = new SpotPriceHistory(Region.DEEPAK_TEST, AZ.ANY);
//	    d.addValue(hist.computeVolatility(InstanceType.M1SMALL, OS.LINUX));
	}

	return d.getMean();
    }

    @Test
    public void volatTrace() {
	SpotPriceHistory hist = PriceDB.getPriceTrace(Region.US_EAST, AZ.A);
	System.out.println(hist.computeVolatility(InstanceType.M1SMALL, OS.LINUX));
    }

    @Test
    public void fetchAll() throws Exception {
	TheGrabber.fetchAllTypes(true, SimProperties.SIM_START_TIME.asDate(),
	    SimProperties.SIM_END_TIME.asDate(), EnumSet.of(Region.SA_EAST));
    }

	//		HESSAM COMMENTED FOR ANY AZ
//    @Test
//    public void fetchOneType() throws Exception {
//	final SortedSet<SpotPriceRecord> newPrices = TheGrabber.fetchSpotHistory(Region.US_WEST,
//	    AZ.A, InstanceType.C1MEDIUM, OS.LINUX, SimProperties.SIM_START_TIME.asDate(),
//	    SimProperties.SIM_END_TIME.asDate());
//
//	System.out.println("Returned prices for: "
//		+ Region.US_EAST.getAmazonName()
//		+ "a"
//		+ "-"
//		+ InstanceType.C1MEDIUM
//		+ "-"
//		+ OS.LINUX
//		+ " from "
//		+ Config.formatDate(newPrices.first().getDate())
//		+ " to "
//		+ Config.formatDate(newPrices.last().getDate()));
//    }

    @Test
    public void fillGaps() throws Exception {

	int totalGaps = 0;

	SimProperties.PRICING_TRACE_GEN.set(PriceTraceGen.HISTORY);

	for (final Region region : Region.values()) {
	    for (final AZ azId : region.getAvailabilityZones()) {
		final SpotPriceHistory db = PriceDB.getPriceTrace(region, azId);
		for (final InstanceType type : InstanceType.values()) {
		    for (final OS os : OS.values()) {
			final SortedSet<PriceRecord> list = db.getPricesForType(type, os);
			if (list != null && !list.isEmpty()) {
			    long previousDate = list.first().getDate();
			    for (final PriceRecord spotPriceRecord : list) {
				final long currentDate = spotPriceRecord.getDate();
				if (currentDate - previousDate > 3 * 24 * 3600) {
				    final SortedSet<SpotPriceRecord> newPriceHist = TheGrabber
					.fetchSpotHistory(region, azId, type, os, previousDate,
					    currentDate);
				    PriceDB.addToDb(region, azId, type, os, newPriceHist);
				    System.err.println("Gap greater than 3 days from "
					    + Config.formatDate(previousDate)
					    + " to "
					    + Config.formatDate(currentDate)
					    + ". records returned: "
					    + newPriceHist.size());
				    totalGaps++;
				}
				previousDate = currentDate;
			    }
			}
		    }
		}
	    }
	}
	System.out.println("TOTAL GAPS " + totalGaps);
    }

    @Test
    public void nextPriceChange() throws Exception {

	SpotPriceHistory spotPriceHistory = PriceDB.getPriceTrace(Region.US_EAST, AZ.A);

	for (final InstanceType type : InstanceType.values()) {
	    for (final OS os : OS.values()) {
		PriceRecord price = new PriceRecord(0, 0.0);
		spotPriceHistory.getNextPriceChange(type, os);
		PriceRecord newPrice = spotPriceHistory.getNextPriceChange(type, os);
		while (newPrice != null) {
		    final String genKey = HistoryPersistenceManager.key(Region.US_EAST,
			Region.US_EAST.getAvailabilityZones().iterator().next(), type, OS.LINUX);
		    System.out
			.println("Current ( " + genKey + ") " + price + ", next: " + newPrice);
		    assertThat(newPrice, not(equalTo(price)));
		    price = newPrice;
		    newPrice = spotPriceHistory.getNextPriceChange(type, os);
		}
	    }
	}
    }

    @Test
    public void printToFilePerRegion() throws Exception {

	for (final Region region : EnumSet.of(Region.US_WEST)) {
	    for (final InstanceType type : InstanceType.values()) {
		for (final OS os : EnumSet.of(OS.LINUX)) {
		    SortedSet<PriceRecord> list = new TreeSet<PriceRecord>();
		    for (final AZ azId : region.getAvailabilityZones()) {
			SpotPriceHistory priceTrace = PriceDB.getPriceTrace(region, azId);
			final NavigableSet<PriceRecord> pricesForType = priceTrace
			    .getPricesForType(type, os);
			if (pricesForType != null) {
			    list.addAll(pricesForType);
			    System.out.println("az "
				    + azId
				    + " "
				    + pricesForType.size()
				    + " prices");
			}
		    }

		    System.out.println(type.toString() + os.toString() + ", total " + list.size());
		    if (!list.isEmpty()) {
			Iterator<PriceRecord> iterator = list.iterator();
			PriceRecord prev = iterator.next();
			while (iterator.hasNext()) {
			    PriceRecord next = iterator.next();
			    if (next.getPrice() == prev.getPrice()) {
				iterator.remove();
			    }
			    prev = next;
			}

			final String fileName = region.getAmazonName()
				+ '.'
				+ os.getNameForFile()
				+ '.'
				+ type.getName()
				+ ".csv";
			File dir = new File("prices_csv_per_region");
			dir.mkdirs();
			printPriceList(list, new File(dir, fileName));
		    }
		}
	    }
	}
    }

    @Test
    public void printToFile() throws Exception {

	for (final Region region : Region.values()) {
	    for (final AZ azId : region.getAvailabilityZones()) {
		SpotPriceHistory hist = new SpotPriceHistory(region, azId,
		    this.historyPersistenceManager.getFromDb(region, azId));
		for (final InstanceType type : InstanceType.values()) {
		    for (final OS os : OS.values()) {
			final SortedSet<PriceRecord> list = hist.getPricesForType(type, os);
			if (list != null && !list.isEmpty()) {
			    final String fileName = region.getAmazonName()
				    + azId
				    + '.'
				    + os.getNameForFile()
				    + '.'
				    + type.getName()
				    + ".csv";
			    File dir = new File("prices_csv_per_az");
			    dir.mkdirs();
			    printPriceList(list, new File(dir, fileName));
			}
		    }
		}
	    }
	}
    }

    private void printPriceList(final SortedSet<PriceRecord> list, final File file)
	    throws FileNotFoundException {
	final PrintStream stream = new PrintStream(new BufferedOutputStream(new FileOutputStream(
	    file)), true);

	System.out.println(file
		+ " from "
		+ Config.formatDate(list.first().getDate())
		+ " to "
		+ Config.formatDate(list.last().getDate()));
	for (final PriceRecord spotPriceRecord : list) {
	    stream.println(spotPriceRecord);
	}
	stream.close();
    }

    @Ignore
    @Test
    public void testTZ() throws Exception {

	final SortedSet<SpotPriceRecord> fetch = TheGrabber.fetchSpotHistory(Region.US_EAST, AZ.A,
	    InstanceType.M1SMALL, OS.LINUX, 0, 72000);
	TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
	for (final SpotPriceRecord spotPriceRecord : fetch) {
	    System.out.println(spotPriceRecord);
	}
    }

    @Test
    public void randomness() throws Exception {

	SpotPriceHistory sph2 = new SpotPriceHistory(Region.US_EAST, AZ.A);

	PriceRecord price1 = sph2.getNextPriceChange(InstanceType.M1SMALL, OS.LINUX);
	do {
	    PriceRecord price2 = sph2.getNextPriceChange(InstanceType.M1SMALL, OS.LINUX);
	    System.out.println("1 " + price1 + ", 2 " + price2);
	    assertFalse(price1.getPrice() == price2.getPrice());
	    price1 = price2;
	    price1 = sph2.getNextPriceChange(InstanceType.M1SMALL, OS.LINUX);
	} while (price1 != null);
    }

    @Test
    public void xml() throws Exception {

	TreeSet<SpotPriceRecord> s = new TreeSet<>();

	s.add(new SpotPriceRecord((GregorianCalendar) Calendar.getInstance(), 0.01));

	XStream x = new XStream();
	x.processAnnotations(SpotPriceRecord.class);

	String xml = x.toXML(s);
	System.out.println(xml);
    }
}
