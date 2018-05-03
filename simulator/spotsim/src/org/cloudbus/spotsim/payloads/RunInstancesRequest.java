package org.cloudbus.spotsim.payloads;

import org.cloudbus.spotsim.enums.AZ;
import org.cloudbus.spotsim.enums.InstanceType;
import org.cloudbus.spotsim.enums.OS;
import org.cloudbus.spotsim.enums.PriceModel;

public class RunInstancesRequest extends CloudRequest {

    private final int minCount;

    private final int maxCount;

    private final InstanceType type;

    private final OS os;

    private final PriceModel priceModel;

    private final double bid;

    private final AZ az;

    public RunInstancesRequest(final int minCount, final int maxCount, final InstanceType type,
	    final OS os, final PriceModel priceModel, final double bid, final AZ az) {
	super();
	this.minCount = minCount;
	this.maxCount = maxCount;
	this.type = type;
	this.os = os;
	this.priceModel = priceModel;
	this.bid = bid;
	this.az = az;
    }

    public AZ getAz() {
	return this.az;
    }

    public double getBid() {
	return this.bid;
    }

    public int getMaxCount() {
	return this.maxCount;
    }

    public int getMinCount() {
	return this.minCount;
    }

    public OS getOs() {
	return this.os;
    }

    public PriceModel getPriceModel() {
	return this.priceModel;
    }

    public int getQtty() {
	return this.minCount;
    }

    public InstanceType getType() {
	return this.type;
    }
}
