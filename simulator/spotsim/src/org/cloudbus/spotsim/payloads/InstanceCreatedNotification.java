package org.cloudbus.spotsim.payloads;

import org.cloudbus.spotsim.enums.AZ;

public class InstanceCreatedNotification extends CloudRequest {

    private final AZ availabilityZone;

    public InstanceCreatedNotification(final long token, final AZ az) {
	super(token);
	this.availabilityZone = az;
    }

    public AZ getAvailabilityZone() {
	return this.availabilityZone;
    }
}
