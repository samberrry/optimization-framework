package org.cloudbus.spotsim.main.config;

import static org.cloudbus.spotsim.main.config.SimProperties.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Properties;
import java.util.logging.Level;

import org.apache.commons.math.random.RandomGenerator;
import org.apache.commons.math.random.Well512a;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.spotsim.enums.InstanceType;
import org.cloudbus.spotsim.enums.OS;
import org.cloudbus.spotsim.enums.Region;

public class Config {

    // Constants
    public static final GregorianCalendar START_TIME = new GregorianCalendar(2011, Calendar.JULY, 5);

    public static final GregorianCalendar END_TIME = new GregorianCalendar(2011,
	Calendar.SEPTEMBER, 10);

    public static final String ACCESS_KEY = "0AR2GGRFRVFNTJ43BG02";

    public static final String SECRET_KEY = "jjcScXKzEKt9qgBqY2cxe+pqsQ1n2SsXcnu0ey6w";

    private static GregorianCalendar simPeriodStart;

    private static Properties props;

    private static String uniqueKey;

    public static String POLICY_CLASS;

    public static String POLICY_NAME;

    public static RandomGenerator RNG;

    public static Properties getProps() {
	return props;
    }

    public static Calendar getSimPeriodStart() {
	return simPeriodStart;
    }

    public static void load() throws FileNotFoundException, IOException {
	load(null);
    }

    public static void load(final String propertiesFile) throws FileNotFoundException, IOException {

	props = new Properties();
	if (propertiesFile != null) {
	    props.load(new BufferedReader(new FileReader(propertiesFile)));
	}

	SimProperties.load(props);

	Log.logger.setLevel(Level.INFO);
	Log.logger.info("Loading properties: " + props.toString());

	final String logLevel = LOG_LEVEL.asString();
	final Level lvl = Level.parse(logLevel);
	Log.logger.info("Logger new level is: " + lvl);
	Log.logger.setLevel(lvl);

	RNG = new Well512a(RNG_SEED.asLong());

	// computes a unique ID for this simulation run. All properties values,
	// including defaults, must be set in 'props'
	uniqueKey = SimProperties.getDigest();
	// random start dates
	simPeriodStart = generateRandomDate(SIM_START_TIME.asDate(), SIM_DAYS_TO_LOAD.asInt());
    }

    public static String uniqueKey() {
	return uniqueKey;
    }

    private static GregorianCalendar generateRandomDate(final Calendar startTime, int days) {

	final long millis = days * 24 * 3600;
	final GregorianCalendar date = new GregorianCalendar();
	date.setTimeInMillis((long) (startTime.getTimeInMillis() + RNG.nextDouble() * millis));
	return date;
    }

    public static String formatDate(final long date) {
	GregorianCalendar c = (GregorianCalendar) getSimPeriodStart().clone();
	c.add(Calendar.SECOND, (int) date);
	return formatDate(c);
    }

    public static String formatDate(final Date date) {
	return new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(date);
    }

    public static String formatDate(final Calendar date) {
	return formatDate(date.getTime());
    }

    public static double getOnDemandPrice(Region r, InstanceType t, OS os) {

	String name = new StringBuilder("price.ondemand.").append(r.toString().toLowerCase())
	    .append('.').append(t.toString().toLowerCase()).append('.')
	    .append(os.toString().toLowerCase()).toString();

	if (!props.containsKey(name)) {
	    return t.getOnDemandPrice(r, os);
	}

	return Double.parseDouble(props.getProperty(name));
    }
}