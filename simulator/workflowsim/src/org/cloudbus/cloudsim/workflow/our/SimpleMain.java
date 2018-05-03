package org.cloudbus.cloudsim.workflow.our;

import org.cloudbus.spotsim.spothistory.SpotPriceRecord;

import java.util.Map;
import java.util.SortedSet;

public class SimpleMain {
    public static void main(String[] args) {
         Map<String, SortedSet<SpotPriceRecord>> pricesDB = HistoryPriceManagement.populatePriceDB();

        System.out.println("The size of sorted set: " + pricesDB.size());

    }
}
