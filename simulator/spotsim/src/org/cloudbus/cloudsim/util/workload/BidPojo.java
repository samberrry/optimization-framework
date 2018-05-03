package org.cloudbus.cloudsim.util.workload;

public class BidPojo {

	private long LTO;
	
	private double bid;
	
	private double spotPirce;

	public BidPojo(long lTO, double bid, double spotPrice) {
		super();
		this.LTO = lTO;
		this.bid = bid;
		this.spotPirce = spotPrice;
	}
	
	public BidPojo() {
		super();
	}

	public long getLTO() {
		return LTO;
	}

	public void setLTO(long lTO) {
		LTO = lTO;
	}

	public double getBid() {
		return bid;
	}

	public void setBid(double bid) {
		this.bid = bid;
	}

	public double getSpotPirce() {
		return spotPirce;
	}

	public void setSpotPirce(double spotPirce) {
		this.spotPirce = spotPirce;
	}
	
	
	
}
