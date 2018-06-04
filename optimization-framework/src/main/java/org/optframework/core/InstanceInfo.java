package org.optframework.core;

import org.cloudbus.spotsim.enums.InstanceType;

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
}
