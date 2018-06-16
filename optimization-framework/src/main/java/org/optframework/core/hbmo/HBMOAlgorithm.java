package org.optframework.core.hbmo;

import com.rits.cloning.Cloner;
import org.cloudbus.spotsim.enums.InstanceType;
import org.optframework.config.Config;
import org.optframework.config.StaticProperties;
import org.optframework.core.*;
import org.optframework.core.utils.Printer;

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

    static Workflow workflow;

    static InstanceInfo instanceInfo[];

    int generationNumber;

    public static int globalCounter = 0;

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
        queen = new Queen(workflow, instanceInfo, M_NUMBER);

        queen.chromosome = localSearch(queen.chromosome);

        for (int i = 0; i < generationNumber; i++) {
            Log.logger.info("=========================Iteration :" + i);
            long start = System.currentTimeMillis();

            matingFlight();
            generateBrood();
            queen.chromosome = localSearch(queen.chromosome);

            long stop = System.currentTimeMillis();
            Printer.printSolution(queen.chromosome,instanceInfo,stop-start);
            spermathecaList.clear();
        }

        return queen.chromosome;
    }

    void matingFlight(){
        List<Mating> matingList = new ArrayList<>();
        for (int i = 0; i < Config.honeybee_algorithm.getNumber_of_threads(); i++) {
            ProblemInfo problemInfo = new ProblemInfo(instanceInfo, workflow, M_NUMBER);
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
            for (Chromosome brood : spermatheca.chromosomeList){

                if (brood.fitnessValue < queen.chromosome.fitnessValue){
                    queen.chromosome = cloner.deepClone(brood);
                }
            }
        }
    }

    static Chromosome crossOver(Chromosome queenChr, Chromosome childChr){
        int mask[] = new int[workflow.getJobList().size()];
        Random r = new Random();
        int newXArray[] = new int[workflow.getJobList().size()];
        int newYArray[] = new int[M_NUMBER];
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
        Chromosome chromosome = new Chromosome(workflow, instanceInfo, M_NUMBER);
        chromosome.numberOfUsedInstances = numberOfInstancesUsed;
        chromosome.xArray = newXArray;
        chromosome.yArray = newYArray;
        chromosome.fitness();

        chromosome.solutionMapping();

        return chromosome;
    }

    static Chromosome localSearch(Chromosome mainChr){
        Cloner cloner = new Cloner();
        Chromosome currentBestChr = cloner.deepClone(mainChr);

        //all neighbors without new instance with only X array change
        for (int i = 0; i < mainChr.workflow.getJobList().size(); i++) {
            int newXArray [] = new int[mainChr.workflow.getJobList().size()];
            System.arraycopy(mainChr.xArray , 0, newXArray, 0, mainChr.xArray.length);
            for (int j = 0; j < mainChr.numberOfUsedInstances; j++) {
                newXArray[i] =j;
                Chromosome newChromosome = new Chromosome(workflow, mainChr.instanceInfo, M_NUMBER);
                newChromosome.xArray = newXArray;
                newChromosome.yArray = mainChr.yArray;
                newChromosome.numberOfUsedInstances = mainChr.numberOfUsedInstances;
                newChromosome.fitness();

                if (newChromosome.fitnessValue < currentBestChr.fitnessValue){
                    currentBestChr = cloner.deepClone(newChromosome);
                }
            }
        }

        //all neighbors without new instance with only Y array change
        for (int i = 0; i < mainChr.numberOfUsedInstances; i++) {
            for (int j = 0; j < InstanceType.values().length; j++) {
                int []newYArray = new int[M_NUMBER];
                System.arraycopy(mainChr.yArray, 0, newYArray,0, mainChr.yArray.length);
                newYArray[i] = j;
                Chromosome newChromosome = new Chromosome(workflow, mainChr.instanceInfo, M_NUMBER);
                newChromosome.xArray = mainChr.xArray;
                newChromosome.yArray = newYArray;
                newChromosome.numberOfUsedInstances = mainChr.numberOfUsedInstances;
                newChromosome.fitness();

                if (newChromosome.fitnessValue < currentBestChr.fitnessValue){
                    currentBestChr = cloner.deepClone(newChromosome);
                }
            }
        }

        //all neighbors with new instance selected with only X array change
        if (mainChr.numberOfUsedInstances != M_NUMBER){
            for (int i = 0; i < mainChr.xArray.length; i++) {
                int newXArray [] = new int[mainChr.workflow.getJobList().size()];
                System.arraycopy(mainChr.xArray , 0, newXArray, 0, mainChr.xArray.length);

                int newInstanceId = mainChr.numberOfUsedInstances;
                newXArray[i] = newInstanceId;

                Chromosome newChromosome = new Chromosome(workflow, mainChr.instanceInfo, M_NUMBER);
                newChromosome.xArray = newXArray;
                newChromosome.yArray = mainChr.yArray;
                newChromosome.numberOfUsedInstances = mainChr.numberOfUsedInstances+1;
                newChromosome.fitness();

                if (newChromosome.fitnessValue < currentBestChr.fitnessValue){
                    currentBestChr = cloner.deepClone(newChromosome);
                }
            }

            for (int i = 0; i < InstanceType.values().length; i++) {
                int []newYArray = new int[M_NUMBER];
                System.arraycopy(mainChr.yArray, 0, newYArray,0, mainChr.yArray.length);

                newYArray[mainChr.numberOfUsedInstances] = i;

                Chromosome newChromosome = new Chromosome(workflow, mainChr.instanceInfo, M_NUMBER);
                newChromosome.xArray = mainChr.xArray;
                newChromosome.yArray = newYArray;
                newChromosome.numberOfUsedInstances = mainChr.numberOfUsedInstances+1;
                newChromosome.fitness();

                if (newChromosome.fitnessValue < currentBestChr.fitnessValue){
                    currentBestChr = cloner.deepClone(newChromosome);
                }
            }
        }

        return currentBestChr;
    }
}
