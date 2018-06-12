package org.optframework.core.hbmo;

import org.cloudbus.cloudsim.util.workload.Workflow;
import org.optframework.core.InstanceInfo;
import org.optframework.core.Solution;

public class Chromosome extends Solution {
    public Chromosome(Workflow workflow, InstanceInfo[] instanceInfo, int numberOfInstances) {
        super(workflow, instanceInfo, numberOfInstances);
    }
}
