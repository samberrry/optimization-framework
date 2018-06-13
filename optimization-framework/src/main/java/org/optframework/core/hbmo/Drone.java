package org.optframework.core.hbmo;

import org.cloudbus.cloudsim.util.workload.Workflow;
import org.optframework.core.InstanceInfo;

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
