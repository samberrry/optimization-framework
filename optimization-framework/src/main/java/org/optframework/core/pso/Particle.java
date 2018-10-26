package org.optframework.core.pso;

import org.cloudbus.spotsim.enums.InstanceType;
import org.optframework.config.Config;
import org.optframework.core.InstanceInfo;
import org.optframework.core.Solution;
import org.optframework.core.Workflow;

public class Particle extends Solution {
    public double velocityX[];
    public double velocityY[];
    public double bestFitnessValueSoFar = 9999999999.9;
    public int bestXArraySoFar[];
    public int bestYArraySoFar[];

    public Particle(Workflow workflow, InstanceInfo[] instanceInfo, int numberOfInstances) {
        super(workflow, instanceInfo, numberOfInstances);
        velocityX = new double[workflow.getJobList().size()];
        velocityY = new double[Config.global.m_number];
    }
}
