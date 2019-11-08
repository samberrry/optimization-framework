package org.optframework.core;

import org.cloudbus.spotsim.enums.AZ;
import org.cloudbus.spotsim.enums.InstanceType;
import org.cloudbus.spotsim.enums.OS;
import org.cloudbus.spotsim.enums.Region;
import org.cloudbus.spotsim.pricing.PriceRecord;
import org.cloudbus.spotsim.pricing.SpotPriceHistory;
import org.cloudbus.spotsim.pricing.db.PriceDB;

public class InstanceInfo {
    InstanceType type;
    double spotPrice;

    public InstanceType getType() {
        return type;
    }

    public void setType(InstanceType type) {
        this.type = type;
    }

    public double getSpotPrice() {
        return spotPrice;
    }

    public void setSpotPrice(double spotPrice) {
        this.spotPrice = spotPrice;
    }

    /**
     * mowsc
     * */
    public static InstanceInfo[] populateInstancePrices(Region region , AZ az, OS os){
        Log.logger.info("Loads spot prices history");
//        SpotPriceHistory priceTraces = PriceDB.getPriceTrace(region , az);
        InstanceInfo info[] = new InstanceInfo[InstanceType.values().length];

//        for (InstanceType type: InstanceType.values()){
//            PriceRecord priceRecord = priceTraces.getNextPriceChange(type,os);
//            InstanceInfo instanceInfo = new InstanceInfo();
//            instanceInfo.setSpotPrice(priceRecord.getPrice());
//            instanceInfo.setType(type);
//
//            info[type.getId()] = instanceInfo;
//        }

        InstanceInfo instanceInfo1 = new InstanceInfo();
        instanceInfo1.setSpotPrice(0.06);
        instanceInfo1.setType(InstanceType.M1SMALL);
        info[0] = instanceInfo1;

        InstanceInfo instanceInfo2 = new InstanceInfo();
        instanceInfo2.setSpotPrice(0.12);
        instanceInfo2.setType(InstanceType.M1MEDIUM);
        info[1] = instanceInfo2;

        InstanceInfo instanceInfo3 = new InstanceInfo();
        instanceInfo3.setSpotPrice(0.113);
        instanceInfo3.setType(InstanceType.M3MEDIUM);
        info[2] = instanceInfo3;

        InstanceInfo instanceInfo4 = new InstanceInfo();
        instanceInfo4.setSpotPrice(0.24);
        instanceInfo4.setType(InstanceType.M1LARGE);
        info[3] = instanceInfo4;

        InstanceInfo instanceInfo5 = new InstanceInfo();
        instanceInfo5.setSpotPrice(0.225);
        instanceInfo5.setType(InstanceType.M3LARGE);
        info[4] = instanceInfo5;

        InstanceInfo instanceInfo6 = new InstanceInfo();
        instanceInfo6.setSpotPrice(0.48);
        instanceInfo6.setType(InstanceType.M1XLARGE);
        info[5] = instanceInfo6;

        InstanceInfo instanceInfo7 = new InstanceInfo();
        instanceInfo7.setSpotPrice(0.45);
        instanceInfo7.setType(InstanceType.M3XLARGE);
        info[6] = instanceInfo7;

        InstanceInfo instanceInfo8 = new InstanceInfo();
        instanceInfo8.setSpotPrice(0.9);
        instanceInfo8.setType(InstanceType.M32XLARGE);
        info[7] = instanceInfo8;

        return info;
    }
}
