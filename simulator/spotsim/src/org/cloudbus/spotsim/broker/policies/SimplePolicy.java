package org.cloudbus.spotsim.broker.policies;

import java.util.Collection;

import org.cloudbus.cloudsim.util.workload.Task;
import org.cloudbus.spotsim.broker.Broker;
import org.cloudbus.spotsim.broker.SchedPolicy;
import org.cloudbus.spotsim.broker.SchedulingDecision;
import org.cloudbus.spotsim.broker.resources.Resource;
import org.cloudbus.spotsim.broker.rtest.RuntimeEstimator;
import org.cloudbus.spotsim.enums.InstanceType;
import org.cloudbus.spotsim.enums.OS;
import org.cloudbus.spotsim.enums.Region;

public class SimplePolicy implements SchedPolicy {

    @Override
    public SchedulingDecision fitsOnIdle(final Task task, final Collection<Resource> idle) {
	return new SchedulingDecision(task, idle.iterator().next(), 1);
    }

    @Override
    public RuntimeEstimator getRuntimeEstimator() {
	return null;
    }

    @Override
    public String policyName() {
	return "Simple";
    }

    @Override
    public SchedulingDecision sched(final Task task, final Collection<Resource> myInstances,
	    boolean reachedMax, Region region) {
	return new SchedulingDecision(task, InstanceType.M1SMALL, OS.getDefault(), region);
    }

    @Override
    public void setBroker(final Broker broker) {
    }

    @Override
    public void setRuntimeEstimator(final RuntimeEstimator runtimeEstimator) {
    }

    @Override
    public boolean usesEstimation() {
	return false;
    }
}
