package org.optframework.core.hbmo;

import org.optframework.core.InstanceInfo;
import org.optframework.core.Workflow;

public class Queen{
    Chromosome chromosome;

    public Queen(Workflow workflow, InstanceInfo []instanceInfo, int numberOfInstances) {
        chromosome = new Chromosome(workflow, instanceInfo, numberOfInstances);
        chromosome.generateRandomSolution(workflow);
        chromosome.fitness();
    }
}
