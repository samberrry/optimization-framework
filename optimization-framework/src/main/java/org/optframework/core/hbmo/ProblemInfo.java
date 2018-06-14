package org.optframework.core.hbmo;

import org.optframework.core.InstanceInfo;
import org.optframework.core.Workflow;

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
