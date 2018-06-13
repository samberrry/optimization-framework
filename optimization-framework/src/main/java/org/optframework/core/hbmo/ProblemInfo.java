package org.optframework.core.hbmo;

import org.cloudbus.cloudsim.util.workload.Workflow;
import org.optframework.core.InstanceInfo;

public class ProblemInfo {
    InstanceInfo instanceInfo[];
    Workflow workflow;
    int numberOfInstances;

    public ProblemInfo(InstanceInfo[] instanceInfo, Workflow workflow, int numberOfInstances) {
        this.instanceInfo = instanceInfo;
        this.workflow = workflow;
        this.numberOfInstances = numberOfInstances;
    }
}
