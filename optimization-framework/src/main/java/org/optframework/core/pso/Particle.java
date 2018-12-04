package org.optframework.core.pso;

import org.optframework.config.Config;
import org.optframework.core.InstanceInfo;
import org.optframework.core.Solution;
import org.optframework.core.Workflow;

import java.util.Random;

public class Particle extends Solution {
    public double velocityX[];
    public double velocityY[];
    public double velocityZ[];
    public double bestFitnessValueSoFar = Math.pow(10,100);
    public int bestXArraySoFar[];
    public int bestYArraySoFar[];
    public Integer bestZArraySoFar[];

    public Particle(Workflow workflow, InstanceInfo[] instanceInfo, int numberOfInstances) {
        super(workflow, instanceInfo, numberOfInstances);
        velocityX = new double[workflow.getJobList().size()];
        velocityY = new double[Config.global.m_number];
        velocityZ = new double[workflow.getJobList().size()];
    }

    public void generateRandomVelocities(){
        Random random = new Random();

        for (int i = 0; i < workflow.getJobList().size(); i++) {
            int max = workflow.getJobList().size();
            int min = -workflow.getJobList().size();
            int number = random.nextInt(max + 1 -min) + min;

            velocityX[i] = number;
        }

        for (int i = 0; i < Config.global.m_number; i++) {
            int max = Config.global.m_number;
            int min = -Config.global.m_number;
            int number = random.nextInt(max + 1 -min) + min;

            velocityY[i] = number;
        }

        for (int i = 0; i < workflow.getJobList().size(); i++) {
            int max = workflow.getJobList().size();
            int min = -workflow.getJobList().size();
            int number = random.nextInt(max + 1 -min) + min;

            velocityZ[i] = number;
        }
    }
}
