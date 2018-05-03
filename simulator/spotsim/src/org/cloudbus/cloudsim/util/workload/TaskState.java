package org.cloudbus.cloudsim.util.workload;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

public enum TaskState {

    COMPLETED,
    CANCELED,
    RUNNING,
    SCHEDULED,
    FAILED,
    POSTPONED,
    READY;

    private static final Map<TaskState, EnumSet<TaskState>> stateMachine = new EnumMap<TaskState, EnumSet<TaskState>>(
	TaskState.class);

    static {
	stateMachine.put(RUNNING, EnumSet.of(COMPLETED, FAILED, CANCELED));
	stateMachine.put(SCHEDULED, EnumSet.of(READY, RUNNING, CANCELED));
	stateMachine.put(FAILED, EnumSet.of(SCHEDULED));
	stateMachine.put(POSTPONED, EnumSet.of(SCHEDULED));
	stateMachine.put(READY, EnumSet.of(POSTPONED, SCHEDULED));
    }

    public boolean finished() {
	return this == CANCELED || this == COMPLETED || this == FAILED;
    }

    public EnumSet<TaskState> getAllowedTransitions() {
	return stateMachine.get(this);
    }

    public boolean hasStarted() {
	return this == RUNNING || this.finished();
    }

    public boolean isTransitionAllowed(final TaskState newStatus) {

	if (stateMachine.containsKey(this)) {
	    return stateMachine.get(this).contains(newStatus);
	}
	return false;
    }
}
