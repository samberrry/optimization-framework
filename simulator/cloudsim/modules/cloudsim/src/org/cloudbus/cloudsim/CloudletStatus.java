package org.cloudbus.cloudsim;

import java.util.EnumSet;

public enum CloudletStatus {
    CREATED,
    READY,
    QUEUED,
    INEXEC,
    SUCCESS,
    PAUSED,
    FAILED,
    CANCELED,
    SUSPENDED,
    FAILED_RESOURCE_UNAVAILABLE,
    UNKNOWN,
    INCONSISTENT;

    private static final EnumSet<CloudletStatus> activeStates = EnumSet.of(CREATED, READY, QUEUED,
	INEXEC, PAUSED);

    public boolean isActive() {
	return activeStates.contains(this);
    }

    public static EnumSet<CloudletStatus> getActivestates() {
	return activeStates;
    }
}
