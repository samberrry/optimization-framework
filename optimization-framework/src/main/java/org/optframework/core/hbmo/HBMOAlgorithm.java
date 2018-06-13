package org.optframework.core.hbmo;

import com.rits.cloning.Cloner;
import org.cloudbus.cloudsim.util.workload.Workflow;
import org.cloudbus.spotsim.enums.InstanceType;
import org.optframework.config.StaticProperties;
import org.optframework.core.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * Implementation for the Honey Bee Mating Optimization Algorithm
 *
 * @author Hessam hessam.modaberi@gmail.com
 * @since 2018
 * */

public class HBMOAlgorithm implements OptimizationAlgorithm, StaticProperties {

    Queen queen;

    Workflow workflow;

    InstanceInfo instanceInfo[];

    int generationNumber;

    public static List<Spermatheca> spermathecaList = new ArrayList<>();

    Cloner cloner = new Cloner();

    public HBMOAlgorithm(Workflow workflow, InstanceInfo[] instanceInfo, int generationNumber) {
        this.workflow = workflow;
        this.instanceInfo = instanceInfo;
        this.generationNumber = generationNumber;
    }

    @Override
    public Solution runAlgorithm() {

        // Queen generation
        queen = new Queen(cloner.deepClone(workflow), instanceInfo, M_NUMBER);

        queen.chromosome = localSearch(queen.chromosome);

        for (int i = 0; i < generationNumber; i++) {
            Log.logger.info("=========================Iteration :" + i);
            long start = System.currentTimeMillis();

            matingFlight();
            generateBrood();
            queen.chromosome = localSearch(queen.chromosome);

            long stop = System.currentTimeMillis();
            Printer.printSolution(queen.chromosome,instanceInfo,stop-start);
        }

        return queen.chromosome;
    }

    void matingFlight(){
        List<Mating> matingList = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_HBMO_THREADS; i++) {
            ProblemInfo problemInfo = new ProblemInfo(instanceInfo, cloner.deepClone(workflow), M_NUMBER);
            spermathecaList.add(i , new Spermatheca());
            Mating mating = new Mating(i, String.valueOf(i), problemInfo, queen);
            matingList.add(i, mating);
        }

        for (Mating mating : matingList){
            try {
                mating.thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    void generateBrood(){
        for (Spermatheca spermatheca : spermathecaList){
            for (Chromosome childChr : spermatheca.chromosomeList){
                Chromosome brood = crossOver(queen.chromosome, childChr);

                brood = localSearch(brood);

                if (brood.fitnessValue < queen.chromosome.fitnessValue){
                    queen.chromosome = cloner.deepClone(brood);
                }
            }
        }

    }

    Chromosome crossOver(Chromosome queenChr, Chromosome childChr){
        int mask[] = new int[workflow.getJobList().size()];
        Random r = new Random();
        int newXArray[] = new int[workflow.getJobList().size()];
        int newYArray[];
        int numberOfInstancesUsed;

        for (int i = 0; i < mask.length; i++) {
            mask[i] = r.nextInt(2);
        }

        for (int i = 0; i < workflow.getJobList().size(); i++) {
            switch (mask[i]){
                case 0:
                    newXArray[i] = queenChr.xArray[i];
                    break;
                case 1:
                    newXArray[i] = childChr.xArray[i];
                    break;
            }
        }

        if (queenChr.numberOfUsedInstances == childChr.numberOfUsedInstances){
            newYArray = new int[queenChr.yArray.length];
            numberOfInstancesUsed = queenChr.numberOfUsedInstances;
            for (int i = 0; i < queenChr.numberOfUsedInstances; i++) {
                int toggle = r.nextInt(2);
                switch (toggle){
                    case 0:
                        newYArray[i] = queenChr.yArray[i];
                        break;
                    case 1:
                        newYArray[i] = childChr.yArray[i];
                        break;
                }
            }
        }else {
            int maxNumber , minNumber;
            boolean queenHasMaxLength = false;
            if (queenChr.numberOfUsedInstances > childChr.numberOfUsedInstances){
                queenHasMaxLength = true;
                maxNumber = queenChr.numberOfUsedInstances;
                numberOfInstancesUsed = queenChr.numberOfUsedInstances;
                minNumber = childChr.numberOfUsedInstances;
            }else {
                maxNumber = childChr.numberOfUsedInstances;
                numberOfInstancesUsed = childChr.numberOfUsedInstances;
                minNumber = queenChr.numberOfUsedInstances;
            }
            newYArray = new int[maxNumber];

            for (int i = 0; i < minNumber; i++) {
                int toggle = r.nextInt(2);
                switch (toggle){
                    case 0:
                        newYArray[i] = queenChr.yArray[i];
                        break;
                    case 1:
                        newYArray[i] = childChr.yArray[i];
                        break;
                }
            }

            for (int i = minNumber; i < maxNumber; i++) {
                int toggle = r.nextInt(2);
                switch (toggle){
                    case 0:
                        if (queenHasMaxLength){
                            newYArray[i] = queenChr.yArray[i];
                        }else {
                            newYArray[i] = r.nextInt(InstanceType.values().length);
                        }
                        break;
                    case 1:
                        if (queenHasMaxLength){
                            newYArray[i] = r.nextInt(InstanceType.values().length);
                        }else {
                            newYArray[i] = childChr.yArray[i];
                        }
                        break;
                }
            }
        }
        Chromosome chromosome = new Chromosome(cloner.deepClone(workflow), instanceInfo, M_NUMBER);
        chromosome.numberOfUsedInstances = numberOfInstancesUsed;
        chromosome.xArray = newXArray;
        chromosome.yArray = newYArray;
        chromosome.fitness();

        chromosome.solutionMapping();

        return chromosome;
    }

    Chromosome localSearch(Chromosome mainChr){
        Cloner cloner = new Cloner();
        Chromosome currentBestChr = cloner.deepClone(mainChr);

        //all neighbors without new instance
        for (int i = 0; i < mainChr.workflow.getJobList().size(); i++) {
            int newXArray [] = new int[mainChr.workflow.getJobList().size()];
            System.arraycopy(mainChr.xArray , 0, newXArray, 0, mainChr.xArray.length);
            for (int j = 0; j < mainChr.numberOfUsedInstances; j++) {
                newXArray[i] =j;
                for (int k = 0; k < mainChr.numberOfUsedInstances; k++) {
                    for (int l = 0; l < InstanceType.values().length; l++) {
                        int []newYArray = new int[M_NUMBER];
                        System.arraycopy(mainChr.yArray, 0, newYArray,0, mainChr.yArray.length);

                        newYArray[k] = l;
                        Chromosome newChromosome = new Chromosome(cloner.deepClone(workflow), mainChr.instanceInfo, M_NUMBER);
                        newChromosome.xArray = newXArray;
                        newChromosome.yArray = newYArray;
                        newChromosome.numberOfUsedInstances = mainChr.numberOfUsedInstances;
                        newChromosome.fitness();

                        if (newChromosome.fitnessValue < currentBestChr.fitnessValue){
                            currentBestChr = cloner.deepClone(newChromosome);
                        }
                    }
                }
            }
        }

        //all neighbors with new instance selected
        if (mainChr.numberOfUsedInstances != M_NUMBER){
            for (int i = 0; i < mainChr.xArray.length; i++) {
                int newXArray [] = new int[mainChr.workflow.getJobList().size()];
                System.arraycopy(mainChr.xArray , 0, newXArray, 0, mainChr.xArray.length);

                int newInstanceId = mainChr.numberOfUsedInstances;
                newXArray[i] = newInstanceId;

                int []newYArray = new int[M_NUMBER];
                System.arraycopy(mainChr.yArray, 0, newYArray,0, mainChr.yArray.length);

                for (int j = 0; j < InstanceType.values().length; j++) {
                    newYArray[newInstanceId] = j;
                    Chromosome newChromosome = new Chromosome(cloner.deepClone(workflow), mainChr.instanceInfo, M_NUMBER);
                    newChromosome.xArray = newXArray;
                    newChromosome.yArray = newYArray;
                    newChromosome.numberOfUsedInstances = mainChr.numberOfUsedInstances+1;

                    newChromosome.fitness();

                    if (newChromosome.fitnessValue < currentBestChr.fitnessValue){
                        currentBestChr = cloner.deepClone(newChromosome);
                    }
                }
            }
        }

        return currentBestChr;
    }
}
