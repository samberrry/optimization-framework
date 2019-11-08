package org.optframework.core.pso;

import com.rits.cloning.Cloner;
import org.cloudbus.spotsim.enums.InstanceType;
import org.optframework.config.Config;
import org.optframework.core.InstanceInfo;
import org.optframework.core.OptimizationAlgorithm;
import org.optframework.core.Solution;
import org.optframework.core.Workflow;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PSOOptimization implements OptimizationAlgorithm {
    Workflow workflow;
    InstanceInfo instanceInfo[];

    Particle globalBestParticle;
    List<Particle> particleList = new ArrayList<>();
    Cloner cloner;

    Particle initialSolution;

    public PSOOptimization(Particle initialSolution, Workflow workflow, InstanceInfo[] instanceInfo) {
        this.workflow = workflow;
        this.instanceInfo = instanceInfo;
        this.globalBestParticle = new Particle(workflow, instanceInfo, Config.global.m_number);
        this.globalBestParticle.generateRandomSolution(workflow);
        this.globalBestParticle.heftFitness();
        this.cloner = new Cloner();
        this.initialSolution = initialSolution;
    }

    @Override
    public Solution runAlgorithm() {

        //Initialize particles and their velocities
        generateRandomInitialParticleList();
        for (Particle particle: particleList){
            particle.heftFitness();

            //If the fitness value is better than the previous best pbest,
            // set the current fitness value as the new pbest.
            if (particle.fitnessValue < particle.bestFitnessValueSoFar){
                particle.bestFitnessValueSoFar = particle.fitnessValue;
                particle.bestXArraySoFar = particle.xArray;
                particle.bestYArraySoFar = particle.yArray;
            }

            //check for the best particle as gbest
            if (particle.fitnessValue < globalBestParticle.fitnessValue){
                globalBestParticle = particle;
            }
        }

        for (int i = 0; i < Config.pso_algorithm.maximum_iteration; i++) {
            for (Particle particle: particleList){
                //calculate velocityX and update their positions
                calculateVelocity(particle);
                updatePosition(particle);

                //For each particle, calculate its fitness value
                particle.heftFitness();

                //If the fitness value is better than the previous best pbest,
                // set the current fitness value as the new pbest.
                if (particle.fitnessValue < particle.bestFitnessValueSoFar){
                    particle.bestFitnessValueSoFar = particle.fitnessValue;
                    particle.bestXArraySoFar = particle.xArray;
                    particle.bestYArraySoFar = particle.yArray;
                }

                //check for the best particle as gbest
                if (particle.fitnessValue < globalBestParticle.fitnessValue){
                    globalBestParticle = cloner.deepClone(particle);
                }
            }
        }
        return globalBestParticle;
    }

    void generateRandomInitialParticleList(){
        if (initialSolution != null){
            for (int i = 0; i < Config.pso_algorithm.number_of_particles; i++) {
                Particle particle = cloner.deepClone(initialSolution);
                particle.generateRandomVelocities();
                particleList.add(particle);
            }
        }else {
            for (int i = 0; i < Config.pso_algorithm.number_of_particles; i++) {
                Particle solution = new Particle(workflow, instanceInfo, Config.global.m_number);
                solution.generateRandomSolution(workflow);
                solution.generateRandomVelocities();
                particleList.add(i , solution);
            }
        }
    }

    void calculateVelocity(Particle particle){
        Random random = new Random();
        for (int i = 0; i < particle.velocityX.length; i++) {
            particle.velocityX[i] = (Config.pso_algorithm.weight * particle.velocityX[i]) +
                    (Config.pso_algorithm.acceleration_coefficient1 * random.nextDouble())*
                            (particle.bestXArraySoFar[i] - particle.xArray[i]) +
                    (Config.pso_algorithm.acceleration_coefficient2 * random.nextDouble())*
                            (globalBestParticle.xArray[i] - particle.xArray[i]);

            if (particle.velocityX[i] >= Config.global.m_number || particle.velocityX[i] <= - Config.global.m_number){
                particle.velocityX[i] %= Config.global.m_number;
            }
        }

        for (int i = 0; i < particle.numberOfUsedInstances; i++) {
            particle.velocityY[i] = (Config.pso_algorithm.weight * particle.velocityY[i]) +
                    (Config.pso_algorithm.acceleration_coefficient1 * random.nextDouble())*
                            (particle.bestYArraySoFar[i] - particle.yArray[i]) +
                    (Config.pso_algorithm.acceleration_coefficient2 * random.nextDouble())*
                            (globalBestParticle.yArray[i] - particle.yArray[i]);
            if (particle.velocityY[i] >= InstanceType.values().length || particle.velocityY[i] <= -InstanceType.values().length){
                particle.velocityY[i] %= InstanceType.values().length;
            }
        }
    }

    void updatePosition(Particle particle){
        for (int i = 0; i < particle.xArray.length; i++) {
            particle.xArray[i] = particle.xArray[i] + (int)particle.velocityX[i];

            if (particle.xArray[i] >= Config.global.m_number){
                particle.xArray[i] %= Config.global.m_number;
            }else if (particle.xArray[i] <= -Config.global.m_number){
                particle.xArray[i] %= Config.global.m_number;
                particle.xArray[i] = - particle.xArray[i];
            }else if (particle.xArray[i] < 0){
                particle.xArray[i] = - particle.xArray[i];
            }

            if (particle.xArray[i]+1 > particle.numberOfUsedInstances){
                particle.numberOfUsedInstances = particle.xArray[i]+1;
            }
        }
        for (int i = 0; i < particle.numberOfUsedInstances; i++) {
            particle.yArray[i] = particle.yArray[i] + (int)particle.velocityY[i];

            if (particle.yArray[i] >= InstanceType.values().length){
                particle.yArray[i] %= InstanceType.values().length;
            }else if (particle.yArray[i] <= - InstanceType.values().length){
                particle.yArray[i] %= InstanceType.values().length;
                particle.yArray[i] = - particle.yArray[i];
            }else if (particle.yArray[i] < 0){
                particle.yArray[i] = - particle.yArray[i];
            }
        }
    }
}
