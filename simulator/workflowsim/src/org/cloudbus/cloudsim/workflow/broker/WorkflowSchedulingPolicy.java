package org.cloudbus.cloudsim.workflow.broker;

import java.util.ArrayList;
import java.util.Collection;

import org.cloudbus.cloudsim.util.workload.Task;
import org.cloudbus.spotsim.broker.SchedulingDecision;
import org.cloudbus.spotsim.broker.resources.Resource;
import org.cloudbus.spotsim.broker.rtest.RuntimeEstimator;
import org.cloudbus.spotsim.enums.Region;

public interface WorkflowSchedulingPolicy {

    SchedulingDecision fitsOnIdle(Task task, Collection<Resource> idle);

    RuntimeEstimator getRuntimeEstimator();

    String policyName();

    SchedulingDecision sched(Task task, Collection<Resource> myInstances, boolean reachedMax,
    	    Region region);
    
    ArrayList<SchedulingDecision> schedule(Task task, Collection<Resource> myInstances, boolean reachedMax,
    	    Region region);
    
    void setBroker(WorkflowBroker broker);

    void setRuntimeEstimator(RuntimeEstimator runtimeEstimator);

    boolean usesEstimation();
}
