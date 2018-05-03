package org.cloudbus.cloudsim.workflow.our;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.cloudbus.cloudsim.workflow.our.util.SpotPriceWithAZ;
import org.cloudbus.spotsim.enums.AZ;
import org.cloudbus.spotsim.enums.InstanceType;
import org.cloudbus.spotsim.enums.OS;
import org.cloudbus.spotsim.enums.Region;
import org.cloudbus.spotsim.main.config.SimProperties;
import org.cloudbus.spotsim.pricing.db.HistoryPersistenceManager;
import org.cloudbus.spotsim.spothistory.SpotPriceRecord;

import java.io.*;
import java.util.*;

/**
 * This class is responsible for reading history price list from JSON files retrieved from the awscli
 *
 * @author Hessam
 * @since 2018
 * */

public abstract class HistoryPriceManagement {

    public static List<SpotPriceWithAZ> get(InstanceType type){
        Gson gson = new Gson();
        SpotPriceHistory priceHistory = null;
        List<SpotPriceWithAZ> spotPriceWithAZList = new ArrayList<>();

        String fileName = null;
        switch (type){
            case M1SMALL:
                fileName = "m1_small";
                break;
            case M24XLARGE:
                fileName = "m2_4xlarge";
                break;
//            case C1MEDIUM:
//                fileName = "c_medium";
//                break;
//            case C1XLARGE:
//                fileName = "c1_xlarge";
//                break;
            case M1LARGE:
                fileName = "m1_large";
                break;
            case M1MEDIUM:
                fileName = "m1_medium";
                break;
            case M1XLARGE:
                fileName = "m1_xlarge";
                break;
            case M2XLARGE:
                fileName = "m2_xlarge";
                break;
            case M3XLARGE:
                fileName = "m3_xlarge";
                break;
            case M22XLARGE:
                fileName = "m2_2xlarge";
                break;
            case M32XLARGE:
                fileName = "m3_2xlarge";
                break;
        }

        try {
            priceHistory = new ObjectMapper().readValue(new File(SimProperties.PRICING_HISTORY_MAP_DIR.getValue() + fileName +".json"),SpotPriceHistory.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!priceHistory.getPriceList().isEmpty()){
            final SortedSet<SpotPriceRecord> priceSetForA = new TreeSet<>();
            final SortedSet<SpotPriceRecord> priceSetForB = new TreeSet<>();
            final SortedSet<SpotPriceRecord> priceSetForC = new TreeSet<>();

            for (final SpotPriceItem spotPrice : priceHistory.getPriceList()) {
                switch (spotPrice.getAvailabilityZone().substring(spotPrice.getAvailabilityZone().length()-1))
                {
                    case "a":
                        priceSetForA.add(new SpotPriceRecord(spotPrice.getTimestamp(), spotPrice.getSpotPrice()));
                        break;
                    case "b":
                        priceSetForB.add(new SpotPriceRecord(spotPrice.getTimestamp(), spotPrice.getSpotPrice()));
                        break;
                    case "c":
                        priceSetForC.add(new SpotPriceRecord(spotPrice.getTimestamp(), spotPrice.getSpotPrice()));
                        break;
                }
            }

            System.out.println("DONE with: " + priceHistory.getPriceList().size() + " number of items");

            if (!priceSetForA.isEmpty()){
                spotPriceWithAZList.add(new SpotPriceWithAZ(priceSetForA , AZ.A));
            }
            if (!priceSetForB.isEmpty()){
                spotPriceWithAZList.add(new SpotPriceWithAZ(priceSetForB, AZ.B));
            }
            if (!priceSetForC.isEmpty()){
                spotPriceWithAZList.add(new SpotPriceWithAZ(priceSetForC, AZ.C));
            }

            return spotPriceWithAZList;
        }else {
            System.out.println("empty history price list");
            return null;
        }
    }

    public static Map<String, SortedSet<SpotPriceRecord>> populatePriceDB(){
        Map<String, SortedSet<SpotPriceRecord>> populatedDB = new HashMap<>();

        for (final InstanceType type : InstanceType.values()){
            List<SpotPriceWithAZ> spotPriceWithAZList = get(type);

            for (SpotPriceWithAZ spotPriceWithAZ : spotPriceWithAZList){
                SortedSet<SpotPriceRecord> tempPrices = spotPriceWithAZ.getSpotPriceRecords();

                if (tempPrices != null){
                    populatedDB.put(HistoryPersistenceManager.key(Region.EUROPE, spotPriceWithAZ.getAz(), type , OS.LINUX), tempPrices);
                }
            }
        }

        return populatedDB;
    }
}
