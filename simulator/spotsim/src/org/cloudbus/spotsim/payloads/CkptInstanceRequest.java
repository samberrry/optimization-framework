package org.cloudbus.spotsim.payloads;

public class CkptInstanceRequest extends CloudRequest {

    private final long frequency;

    public CkptInstanceRequest(final long token, final long frequency) {
	super(token);
	this.frequency = frequency;
    }

    public long getFrequency() {
	return this.frequency;
    }
}