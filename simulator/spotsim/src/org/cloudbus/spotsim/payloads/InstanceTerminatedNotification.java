package org.cloudbus.spotsim.payloads;

public class InstanceTerminatedNotification extends CloudRequest {

    private final boolean outOfBid;

    private final double cost;

    private final int hoursCharged;

    public InstanceTerminatedNotification(final long requestToken, final boolean finalStatus,
	    final double cost, int hoursCharged) {
	super(requestToken);
	this.outOfBid = finalStatus;
	this.cost = cost;
	this.hoursCharged = hoursCharged;
    }

    public double getCost() {
	return this.cost;
    }

    public boolean getFinalStatus() {
	return this.outOfBid;
    }

    public int getHoursCharged() {
	return hoursCharged;
    }
}
