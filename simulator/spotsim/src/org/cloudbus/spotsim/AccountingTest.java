package org.cloudbus.spotsim;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.spotsim.cloudprovider.instance.DatacenterManager;
import org.cloudbus.spotsim.cloudprovider.instance.Instance;
import org.cloudbus.spotsim.enums.InstanceType;
import org.cloudbus.spotsim.enums.OS;
import org.cloudbus.spotsim.enums.PriceModel;
import org.cloudbus.spotsim.main.config.Config;
import org.cloudbus.spotsim.pricing.PriceRecord;
import org.cloudbus.spotsim.pricing.SpotPriceHistory;
import org.cloudbus.spotsim.spothistory.SpotPriceRecord;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class AccountingTest {

    private static GregorianCalendar SECOND_MARCH;

    private static GregorianCalendar SEVENTH_MARCH;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

	TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
	SECOND_MARCH = new GregorianCalendar(2010, Calendar.MARCH, 2);
	SEVENTH_MARCH = new GregorianCalendar(2010, Calendar.MARCH, 7);
	Log.init(null);
	Config.load(null);
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
    public void testBill() throws Exception {

	final InstanceType t = InstanceType.M1SMALL;

	final SpotPriceRecord s = new SpotPriceRecord(SECOND_MARCH, 0);
	final SpotPriceRecord e = new SpotPriceRecord(SEVENTH_MARCH, 0);

	final SpotPriceHistory spotPriceHistory = new SpotPriceHistory();
	PriceRecord nextPriceChange = spotPriceHistory.getNextPriceChange(t, OS.LINUX);
	Log.logger.fine("Starting at: " + nextPriceChange);
	PriceRecord previous;
	while (nextPriceChange != null) {
	    previous = nextPriceChange;
	    nextPriceChange = spotPriceHistory.getNextPriceChange(t, OS.LINUX);
	    if (nextPriceChange != null) {
		final double diff = (nextPriceChange.getDate() - previous.getDate()) / 3600;
		Log.logger.fine("Change at: " + nextPriceChange + ", Hours passed: " + diff);
	    }
	}

	final DatacenterManager datacenterManager = new DatacenterManager(spotPriceHistory, null,
	    null, 1000);
	final Instance instance = datacenterManager.newInstance(t, OS.LINUX, PriceModel.SPOT, 1,
	    0.03);
	instance.setAccStart(s.secondsSinceStart());
	instance.setAccEnd(e.secondsSinceStart());
    }
}
