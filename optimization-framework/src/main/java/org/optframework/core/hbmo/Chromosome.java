package org.optframework.core.hbmo;

import org.optframework.core.InstanceInfo;
import org.optframework.core.Solution;
import org.optframework.core.Workflow;

public class Chromosome extends Solution {
    public Chromosome(Workflow workflow, InstanceInfo[] instanceInfo, int numberOfInstances) {
        super(workflow, instanceInfo, numberOfInstances);
    }
}
