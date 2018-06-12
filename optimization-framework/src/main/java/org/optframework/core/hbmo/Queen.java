package org.optframework.core.hbmo;

import org.cloudbus.cloudsim.util.workload.Workflow;
import org.optframework.core.InstanceInfo;

public class Queen{
    Chromosome chromosome;

    public Queen(Workflow workflow, InstanceInfo []instanceInfo, int numberOfInstances) {
        chromosome = new Chromosome(workflow, instanceInfo, numberOfInstances);
        chromosome.generateRandomSolution(workflow);
    }

    void localSearch(){

    }
}
