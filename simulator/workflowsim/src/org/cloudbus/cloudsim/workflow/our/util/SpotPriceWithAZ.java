package org.cloudbus.cloudsim.workflow.our.util;

import org.cloudbus.spotsim.enums.AZ;
import org.cloudbus.spotsim.spothistory.SpotPriceRecord;

import java.util.SortedSet;

public class SpotPriceWithAZ {
    private SortedSet<SpotPriceRecord> spotPriceRecords;
    private AZ az;

    public SpotPriceWithAZ() {
    }

    public SpotPriceWithAZ(SortedSet<SpotPriceRecord> spotPriceRecords, AZ az) {
        this.spotPriceRecords = spotPriceRecords;
        this.az = az;
    }

    public SortedSet<SpotPriceRecord> getSpotPriceRecords() {
        return spotPriceRecords;
    }

    public void setSpotPriceRecords(SortedSet<SpotPriceRecord> spotPriceRecords) {
        this.spotPriceRecords = spotPriceRecords;
    }

    public AZ getAz() {
        return az;
    }

    public void setAz(AZ az) {
        this.az = az;
    }
}
