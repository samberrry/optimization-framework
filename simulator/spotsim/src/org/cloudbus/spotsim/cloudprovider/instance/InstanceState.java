package org.cloudbus.spotsim.cloudprovider.instance;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

public enum InstanceState {

    PENDING,
    STARTING,
    IDLE,
    RUNNING_BUSY,
    PAUSED,
    PAUSING,
    TERMINATED_BY_USER,
    OUT_OF_BID,
    FAILED;

    private static final Map<InstanceState, EnumSet<InstanceState>> stateMachine = new EnumMap<InstanceState, EnumSet<InstanceState>>(
	InstanceState.class);

    static {
	stateMachine.put(PENDING, EnumSet.of(STARTING, TERMINATED_BY_USER,FAILED));
	stateMachine.put(STARTING, EnumSet.of(PENDING, FAILED, IDLE));
	stateMachine.put(IDLE, EnumSet.of(RUNNING_BUSY, OUT_OF_BID, TERMINATED_BY_USER, FAILED));
	stateMachine.put(RUNNING_BUSY, EnumSet.of(IDLE, OUT_OF_BID, TERMINATED_BY_USER, PAUSING, FAILED));
	stateMachine.put(PAUSED, EnumSet.of(OUT_OF_BID, TERMINATED_BY_USER, RUNNING_BUSY, FAILED));
	stateMachine.put(PAUSING, EnumSet.of(PAUSED, IDLE, OUT_OF_BID, TERMINATED_BY_USER, FAILED));
	stateMachine.put(OUT_OF_BID, EnumSet.of(TERMINATED_BY_USER));
    }

    public EnumSet<InstanceState> getAllowedTransitions() {
	return stateMachine.get(this);
    }

    public boolean isRunning() {
	return this == RUNNING_BUSY || this == IDLE || this == PAUSED || this == PAUSING;
    }

    public boolean isStarting() {
	return equals(PENDING) || equals(STARTING);
    }

    public boolean isTerminated() {
	return equals(FAILED) || equals(OUT_OF_BID) || equals(TERMINATED_BY_USER);
    }

    public boolean transitionAllowed(final InstanceState newState) {
	if (stateMachine.containsKey(this)) {
	    return stateMachine.get(this).contains(newState);
	}
	return false;
    }
}