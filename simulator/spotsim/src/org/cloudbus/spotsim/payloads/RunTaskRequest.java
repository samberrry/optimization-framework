package org.cloudbus.spotsim.payloads;

import org.cloudbus.cloudsim.util.workload.Task;

public class RunTaskRequest extends CloudRequest {

    private final Task task;

    public RunTaskRequest(final long token, final Task task) {
	super(token);
	this.task = task;
    }

    public Task getTask() {
	return this.task;
    }
}
