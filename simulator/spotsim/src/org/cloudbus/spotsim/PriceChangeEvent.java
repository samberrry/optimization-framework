package org.cloudbus.spotsim;

import org.cloudbus.spotsim.enums.AZ;
import org.cloudbus.spotsim.enums.InstanceType;
import org.cloudbus.spotsim.enums.OS;
import org.cloudbus.spotsim.pricing.PriceRecord;

public class PriceChangeEvent {

    private final InstanceType type;

    private final OS os;

    private final PriceRecord priceRecord;

    private final AZ az;

    public PriceChangeEvent(final InstanceType type, final OS os, final PriceRecord priceRecord,
	    final AZ az) {
	super();
	this.type = type;
	this.priceRecord = priceRecord;
	this.os = os;
	this.az = az;
    }

    public AZ getAz() {
	return this.az;
    }

    public OS getOs() {
	return this.os;
    }

    public PriceRecord getPriceRecord() {
	return this.priceRecord;
    }

    public InstanceType getType() {
	return this.type;
    }
}