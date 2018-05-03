package org.cloudbus.spotsim.payloads;

public class ChangeBidRequest extends CloudRequest {

    private final long requestId;

    private final double newBid;

    public ChangeBidRequest(final long token, final long requestId, final double newBid) {
	super(token);
	this.requestId = requestId;
	this.newBid = newBid;
    }

    public double getNewBid() {
	return this.newBid;
    }

    public long getRequestId() {
	return this.requestId;
    }
}
