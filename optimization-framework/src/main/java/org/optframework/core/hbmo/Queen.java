package org.optframework.core.hbmo;

import com.rits.cloning.Cloner;
import org.cloudbus.cloudsim.util.workload.Workflow;
import org.cloudbus.spotsim.enums.InstanceType;
import org.optframework.core.InstanceInfo;

public class Queen{
    Chromosome chromosome;

    Chromosome currentBestChr;

    public Queen(Workflow workflow, InstanceInfo []instanceInfo, int numberOfInstances) {
        chromosome = new Chromosome(workflow, instanceInfo, numberOfInstances);
        chromosome.generateRandomSolution(workflow);
        chromosome.fitness();
    }

    void localSearch(Workflow workflow, int numberOfInstances){
        Cloner cloner = new Cloner();

        currentBestChr = cloner.deepClone(chromosome);

        //all neighbors without new instance
        for (int i = 0; i < chromosome.workflow.getJobList().size(); i++) {
            int newXArray [] = new int[chromosome.workflow.getJobList().size()];
            System.arraycopy(chromosome.xArray , 0, newXArray, 0, chromosome.xArray.length);
            for (int j = 0; j < chromosome.numberOfUsedInstances; j++) {
                newXArray[i] =j;
                for (int k = 0; k < chromosome.numberOfUsedInstances; k++) {
                    for (int l = 0; l < InstanceType.values().length; l++) {
                        int []newYArray = new int[numberOfInstances];
                        System.arraycopy(chromosome.yArray, 0, newYArray,0, chromosome.yArray.length);

                        newYArray[k] = l;
                        Chromosome newChromosome = new Chromosome(workflow, chromosome.instanceInfo, numberOfInstances);
                        newChromosome.xArray = newXArray;
                        newChromosome.yArray = newYArray;
                        newChromosome.numberOfUsedInstances = chromosome.numberOfUsedInstances;
                        newChromosome.fitness();

                        if (newChromosome.fitnessValue < currentBestChr.fitnessValue){
                            currentBestChr = cloner.deepClone(newChromosome);
                        }
                    }
                }
            }
        }

        //all neighbors with new instance selected
        if (chromosome.numberOfUsedInstances != numberOfInstances){
            for (int i = 0; i < chromosome.xArray.length; i++) {
                int newXArray [] = new int[chromosome.workflow.getJobList().size()];
                System.arraycopy(chromosome.xArray , 0, newXArray, 0, chromosome.xArray.length);

                int newInstanceId = chromosome.numberOfUsedInstances;
                newXArray[i] = newInstanceId;

                int []newYArray = new int[numberOfInstances];
                System.arraycopy(chromosome.yArray, 0, newYArray,0, chromosome.yArray.length);

                for (int j = 0; j < InstanceType.values().length; j++) {
                    newYArray[newInstanceId] = j;
                    Chromosome newChromosome = new Chromosome(workflow, chromosome.instanceInfo, numberOfInstances);
                    newChromosome.xArray = newXArray;
                    newChromosome.yArray = newYArray;
                    newChromosome.numberOfUsedInstances = chromosome.numberOfUsedInstances+1;

                    newChromosome.fitness();

                    if (newChromosome.fitnessValue < currentBestChr.fitnessValue){
                        currentBestChr = cloner.deepClone(newChromosome);
                    }
                }
            }
        }

        chromosome = cloner.deepClone(currentBestChr);
    }
}
