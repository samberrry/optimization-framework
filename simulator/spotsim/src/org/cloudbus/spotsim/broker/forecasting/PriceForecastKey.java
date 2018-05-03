package org.cloudbus.spotsim.broker.forecasting;

import org.cloudbus.cloudsim.util.ArraySmallObjectCache;
import org.cloudbus.cloudsim.util.SmallObjectCache;
import org.cloudbus.spotsim.enums.InstanceType;
import org.cloudbus.spotsim.enums.OS;
import org.cloudbus.spotsim.main.config.Config;

public class PriceForecastKey {

    @Override
    public String toString() {

	return "PriceForecastKey [from="
		+ Config.formatDate(this.from)
		+ ", to="
		+ this.to
		+ ", type="
		+ this.type
		+ ", os="
		+ this.os
		+ "]";
    }

    private long from;

    private long to;

    private InstanceType type;

    private OS os;

    private int hashcode = -1;

    private static SmallObjectCache<PriceForecastKey> cache = new ArraySmallObjectCache<PriceForecastKey>();

    public static final PriceForecastKey newInstance(final long from, final long to,
	    final InstanceType type, final OS os) {
	if (cache.isEmpty()) {
	    return new PriceForecastKey(from, to, type, os);
	}
	final PriceForecastKey reclaimed = cache.obtain();
	reclaimed.from = from;
	reclaimed.to = to;
	reclaimed.type = type;
	reclaimed.os = os;
	reclaimed.hash();
	return reclaimed;
    }

    public static void reclaimInstance(PriceForecastKey reclaimed) {
	cache.release(reclaimed);
    }

    public PriceForecastKey(final long from, final long to, final InstanceType type, final OS os) {
	super();
	this.from = from;
	this.to = to;
	if (type == null) {
	    throw new NullPointerException("Type cannot be null");
	}
	if (os == null) {
	    throw new NullPointerException("OS cannot be null");
	}
	this.os = os;
	this.type = type;
	hash();
    }

    @Override
    public boolean equals(final Object obj) {
	if (this == obj) {
	    return true;
	}
	final PriceForecastKey other = (PriceForecastKey) obj;
	if (this.from != other.from) {
	    return false;
	}

	if (this.to != other.to) {
	    return false;
	}

	if (!this.type.equals(other.type)) {
	    return false;
	}

	if (!this.os.equals(other.os)) {
	    return false;
	}

	return true;
    }

    @Override
    public int hashCode() {
	return this.hashcode;
    }

    // pre-computes hash code
    private void hash() {
	final int prime = 31;
	this.hashcode = 1;
	this.hashcode = prime * this.hashcode + (int) (this.from ^ this.from >>> 32);
	this.hashcode = prime * this.hashcode + (int) (this.to ^ this.to >>> 32);
	this.hashcode = prime * this.hashcode + this.type.hashCode();
	this.hashcode = prime * this.hashcode + this.os.hashCode();
    }
}