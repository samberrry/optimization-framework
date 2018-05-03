package org.cloudbus.spotsim.pricing;

import org.cloudbus.spotsim.spothistory.SpotPriceRecord;

public class PriceRecord implements Comparable<PriceRecord> {

    private final long time;

    private final double price;

    public PriceRecord(long time, double price) {
	this.time = time;
	this.price = price;
    }

    public PriceRecord(SpotPriceRecord spotPriceRecord) {
	this.time = spotPriceRecord.millisSinceStart() / 1000L;
	this.price = spotPriceRecord.getPrice();
    }

    public long getTime() {
	return this.time;
    }

    public double getPrice() {
	return this.price;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	long temp;
	temp = Double.doubleToLongBits(this.price);
	result = prime * result + (int) (temp ^ temp >>> 32);
	result = prime * result + (int) (this.time ^ this.time >>> 32);
	return result;
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj) {
	    return true;
	}
	if (obj == null) {
	    return false;
	}
	if (getClass() != obj.getClass()) {
	    return false;
	}
	PriceRecord other = (PriceRecord) obj;
	if (Double.doubleToLongBits(this.price) != Double.doubleToLongBits(other.price)) {
	    return false;
	}
	if (this.time != other.time) {
	    return false;
	}
	return true;
    }

    @Override
    public int compareTo(PriceRecord o) {
	return Long.compare(this.time, o.time);
    }

    public long getDate() {
	return getTime();
    }

    @Override
    public String toString() {
	return "PriceRecord [time=" + this.time + ", price=" + this.price + "]";
    }
}
