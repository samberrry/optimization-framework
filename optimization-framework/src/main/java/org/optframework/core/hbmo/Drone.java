package org.optframework.core.hbmo;

import org.optframework.core.InstanceInfo;
import org.optframework.core.Workflow;

public class Drone {
    Chromosome chromosome;

    public Drone(Workflow workflow, InstanceInfo[]instanceInfo, int numberOfInstances) {
        chromosome = new Chromosome(workflow, instanceInfo, numberOfInstances);
        chromosome.generateRandomSolution(workflow);
        chromosome.fitness();
    }

    public Chromosome getChromosome() {
        return chromosome;
    }

    public void setChromosome(Chromosome chromosome) {
        this.chromosome = chromosome;
    }
}
