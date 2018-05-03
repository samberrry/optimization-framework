package org.cloudbus.spotsim.broker.resources;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

public enum ResourceState {
    PENDING,
    IDLE,
    ACTIVE,
    TERMINATION_REQUESTED,
    OUT_OF_BID,
    RES_FAILED,
    DESTROYED;

    private static final Map<ResourceState, EnumSet<ResourceState>> stateMachine = new EnumMap<ResourceState, EnumSet<ResourceState>>(
	ResourceState.class);

    static {
	stateMachine.put(PENDING, EnumSet.of(IDLE, ACTIVE, TERMINATION_REQUESTED,RES_FAILED));
	stateMachine.put(IDLE, EnumSet.of(TERMINATION_REQUESTED, ACTIVE, OUT_OF_BID, RES_FAILED));
	stateMachine.put(ACTIVE, EnumSet.of(IDLE, OUT_OF_BID, RES_FAILED));
	stateMachine.put(TERMINATION_REQUESTED, EnumSet.of(IDLE, DESTROYED));
	stateMachine.put(OUT_OF_BID, EnumSet.of(DESTROYED));
	stateMachine.put(RES_FAILED, EnumSet.of(DESTROYED));
    }

    public EnumSet<ResourceState> getAllowedTransitions() {
	return stateMachine.get(this);
    }

    public boolean isUsable() {
	return this == PENDING || this == IDLE || this == ACTIVE;
    }

    public boolean transitionAllowed(final ResourceState newState) {

	if (stateMachine.containsKey(this)) {
	    return stateMachine.get(this).contains(newState);
	}
	return false;

    }
}