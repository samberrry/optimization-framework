package org.cloudbus.spotsim.pricing.db;

import java.lang.ref.SoftReference;
import java.util.EnumSet;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedSet;

import org.cloudbus.spotsim.broker.forecasting.PriceForecasting;
import org.cloudbus.spotsim.enums.AZ;
import org.cloudbus.spotsim.enums.InstanceType;
import org.cloudbus.spotsim.enums.OS;
import org.cloudbus.spotsim.enums.Region;
import org.cloudbus.spotsim.main.config.Config;
import org.cloudbus.spotsim.main.config.SimProperties;
import org.cloudbus.spotsim.pricing.PriceRecord;
import org.cloudbus.spotsim.pricing.PriceTraceGen;
import org.cloudbus.spotsim.pricing.SpotPriceHistory;
import org.cloudbus.spotsim.spothistory.SpotPriceRecord;
import org.cloudbus.utils.data.enummaps.DoubleEnumMap;
import org.cloudbus.utils.data.enummaps.TripleEnumMap;

public class PriceDB {

    private static DoubleEnumMap<Region, AZ, SpotPriceHistory> historyDB = new DoubleEnumMap<Region, AZ, SpotPriceHistory>();

    private static DoubleEnumMap<Region, AZ, PriceForecasting> forecasters = new DoubleEnumMap<Region, AZ, PriceForecasting>();

    private static TripleEnumMap<Region, InstanceType, OS, Double> onDemandPrices;

    private static SoftReference<HistoryPersistenceManager> hpm = new SoftReference<HistoryPersistenceManager>(
	new HistoryPersistenceManager(SimProperties.PRICING_HISTORY_MAP_DIR.asString()));

    public static Set<InstanceType> typesAvailableList(final Region r, final AZ az, OS os) {
	if (!historyDB.containsKeys(r, az)) {
	    return EnumSet.noneOf(InstanceType.class);
	}
	return historyDB.get(r, az).getTypesAvailable(os);
    }

    public static boolean isSpotAvailable(final Region r, final AZ az, final InstanceType type,
	    OS os) {
	if (!historyDB.containsKeys(r, az)) {
	    return false;
	}
	return historyDB.get(r, az).areTherePricesForType(type, os);
    }

    public static SpotPriceHistory getPriceTrace(final Region r, final AZ az) {

	if (!historyDB.containsKeys(r, az)) {
	    SpotPriceHistory newHist;
	    if (SimProperties.PRICING_TRACE_GEN.asEnum(PriceTraceGen.class) == PriceTraceGen.HISTORY) {
		HistoryPersistenceManager hp = getHPM();

		final DoubleEnumMap<InstanceType, OS, NavigableSet<PriceRecord>> fromDb = hp
		    .getFromDb(r, az);
		if (fromDb.isEmpty()) {
		    return null;
		}
		newHist = new SpotPriceHistory(r, az, fromDb);
	    } else {
		newHist = new SpotPriceHistory(r, az);
	    }
	    historyDB.put(r, az, newHist);
	    return newHist;
	}
	return historyDB.get(r, az);
    }

    public static HistoryPersistenceManager getHPM() {
	HistoryPersistenceManager hp = hpm.get();
	if (hp == null) {
	    hp = new HistoryPersistenceManager(SimProperties.PRICING_HISTORY_MAP_DIR.asString());
	    hpm = new SoftReference<HistoryPersistenceManager>(hp);
	}
	return hp;
    }

    public static PriceForecasting getForecaster(final Region r, final AZ az) {

	if (!forecasters.containsKeys(r, az)) {
	    forecasters.put(r, az, new PriceForecasting(getPriceTrace(r, az)));
	}
	return forecasters.get(r, az);
    }

    private static void loadOnDemandPrices() {

	onDemandPrices = new TripleEnumMap<Region, InstanceType, OS, Double>();

	for (Region region : Region.values()) {
	    for (InstanceType type : InstanceType.values()) {
		for (OS os : OS.values()) {
		    onDemandPrices.put(region, type, os, Config.getOnDemandPrice(region, type, os));
		}
	    }
	}
    }

    public static double getOnDemandPrice(final Region r, final InstanceType type, final OS os) {

	if (onDemandPrices == null) {
	    loadOnDemandPrices();
	}

	if (!onDemandPrices.containsKeys(r, type, os)) {
	    throw new IllegalArgumentException("On demand price for "
		    + r
		    + '-'
		    + type
		    + '-'
		    + os
		    + " does not exist");
	}

	return onDemandPrices.get(r, type, os);
    }

    public static void addToDb(Region region, AZ azId, InstanceType type, OS os,
	    SortedSet<SpotPriceRecord> newPriceHist) {
	getHPM().addToDb(region, azId, type, os, newPriceHist);
    }
    
    public static void flushPriceDB(){
    	historyDB.clear();
    	//onDemandPrices.clear();
    	hpm.clear();
    }
}