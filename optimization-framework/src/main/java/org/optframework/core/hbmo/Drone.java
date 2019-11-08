package org.optframework.core.hbmo;

import org.optframework.core.InstanceInfo;
import org.optframework.core.Workflow;

import java.util.Objects;

public class Drone implements Cloneable{
    Chromosome chromosome;

    public Drone() {
    }

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

    @Override
    public Drone clone() throws CloneNotSupportedException {
        Drone drone = (Drone) super.clone();

        drone.chromosome = (Chromosome) drone.chromosome.clone();

        return drone;
    }

}
