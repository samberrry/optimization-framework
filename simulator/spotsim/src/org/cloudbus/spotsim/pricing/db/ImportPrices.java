package org.cloudbus.spotsim.pricing.db;

import org.cloudbus.spotsim.enums.AZ;
import org.cloudbus.spotsim.enums.InstanceType;
import org.cloudbus.spotsim.enums.OS;
import org.cloudbus.spotsim.enums.Region;

public class ImportPrices {

    private static HistoryPersistenceManager source = new HistoryPersistenceManager(
	"pricesMapRegions");

    private static HistoryPersistenceManager dest = new HistoryPersistenceManager("pricesMap");

    public static void main(String... args) {

	for (Region region : Region.values()) {
	    for (OS os : OS.values()) {
		for (InstanceType type : InstanceType.values()) {
		    if (source.contains(region, null, type, os)) {
			System.out.println("Adding: " + region + "-" + type + "-" + os);
			dest.addToDb(region, AZ.A, type, os,
			    source.getFromDb(region, null, type, os));
		    }
		}
	    }
	}
    }
}