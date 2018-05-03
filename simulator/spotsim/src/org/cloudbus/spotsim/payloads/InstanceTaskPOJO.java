package org.cloudbus.spotsim.payloads;

import org.cloudbus.cloudsim.util.workload.Task;
import org.cloudbus.spotsim.cloudprovider.instance.Instance;

public class InstanceTaskPOJO {

    private final Instance instance;

    private final Task task;

    public InstanceTaskPOJO(final Instance instance, final Task replica) {
	super();
	this.instance = instance;
	this.task = replica;
    }

    public Instance getInstance() {
	return this.instance;
    }

    public Task getTask() {
	return this.task;
    }
}