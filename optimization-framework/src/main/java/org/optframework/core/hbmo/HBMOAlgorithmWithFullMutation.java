package org.optframework.core.hbmo;

import com.rits.cloning.Cloner;
import org.cloudbus.spotsim.enums.InstanceType;
import org.optframework.config.Config;
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

public class HBMOAlgorithmWithFullMutation implements OptimizationAlgorithm {

    Queen queen;

    static Workflow workflow;

    static InstanceInfo instanceInfo[];

    int generationNumber;

    public static int globalCounter = 0;

    public List<Spermatheca> spermathecaList = new ArrayList<>();

    public static final int M_NUMBER = Config.global.m_number;

    Cloner cloner = new Cloner();

    boolean hasInitialSolution;

   public Solution initialSolution;

    public HBMOAlgorithmWithFullMutation(boolean hasInitialSolution, Workflow workflow, InstanceInfo[] instanceInfo, int generationNumber) {
        this.hasInitialSolution = hasInitialSolution;
        this.workflow = workflow;
        this.instanceInfo = instanceInfo;
        this.generationNumber = generationNumber;
    }

    public HBMOAlgorithmWithFullMutation(boolean hasInitialSolution, Workflow workflow, InstanceInfo[] instanceInfo, int generationNumber, Solution initialSolution) {
        this.hasInitialSolution = hasInitialSolution;
        this.workflow = workflow;
        this.instanceInfo = instanceInfo;
        this.generationNumber = generationNumber;
        this.initialSolution = initialSolution;
    }

    @Override
    public Solution runAlgorithm() {
        // Queen generation
        if (hasInitialSolution){
            queen = new Queen(workflow, instanceInfo, M_NUMBER);
            queen.chromosome.yArray = initialSolution.yArray;
            queen.chromosome.xArray = initialSolution.xArray;
            queen.chromosome.numberOfUsedInstances = initialSolution.numberOfUsedInstances;
            queen.chromosome.fitness();
        }else {
            queen = new Queen(workflow, instanceInfo, M_NUMBER);
        }

        long start2 = System.currentTimeMillis();
        queen.chromosome = localSearch(queen.chromosome);
        long stop2 = System.currentTimeMillis();
        Log.logger.info("Queen local search time: "+ (stop2 - start2));

        for (int i = 0; i < generationNumber; i++) {
            Log.logger.info("=========================Iteration :" + i);
            long start = System.currentTimeMillis();

            matingFlight();
            generateBrood();

            long stop = System.currentTimeMillis();
            Printer.lightPrintSolutionForHBMOItr(queen.chromosome,instanceInfo);

            spermathecaList.clear();
        }

        return queen.chromosome;
    }

    void matingFlight(){
        Thread threadList[] = new Thread[Config.honeybee_algorithm.getNumber_of_threads()];
        List<Spermatheca> threadSpermatheca = new ArrayList<>();
        int threadSpmSize = Config.honeybee_algorithm.getSpermatheca_size() / Config.honeybee_algorithm.getNumber_of_threads();


        for (int i = 0; i < Config.honeybee_algorithm.getNumber_of_threads(); i++) {
            int itr = i;
            threadSpermatheca.add(itr, new Spermatheca());

            threadList[i] = new Thread(() -> {
                Cloner cloner = new Cloner();
                Random r = new Random();

                //This constructor also generates the random solution
                Drone drone = new Drone(workflow, instanceInfo, M_NUMBER);

                double SMax;
                double Smin;

                if (Config.honeybee_algorithm.getForce_speed()){
                    SMax = Config.honeybee_algorithm.getMax_speed();
                    Smin = Config.honeybee_algorithm.getMin_speed();
                }else {
                    final double beta = 0.6 + 0.3 * r.nextDouble();
                    SMax = Math.abs((queen.chromosome.fitnessValue - drone.chromosome.fitnessValue) / Math.log(beta));

                    Smin = Math.abs((queen.chromosome.fitnessValue - drone.chromosome.fitnessValue) / Math.log(0.05));

                    SMax /= Config.honeybee_algorithm.getsMax_division();
                    Smin /= Config.honeybee_algorithm.getsMin_division();
                }

                double queenSpeed = SMax;

                while (queenSpeed > Smin && threadSpermatheca.get(itr).chromosomeList.size() < threadSpmSize){
                    if (probability(queen.chromosome.fitnessValue, drone.chromosome.fitnessValue, queenSpeed) > r.nextDouble()){
                        Chromosome brood = HBMOAlgorithmWithFullMutation.crossOver(queen.chromosome, drone.chromosome);

//                long start = System.currentTimeMillis();
                        brood = lightLocalSearch(brood,Config.honeybee_algorithm.kRandom);
//                long stop = System.currentTimeMillis();
//                Log.logger.info("brood local search: "+ (stop - start));

                        threadSpermatheca.get(itr).chromosomeList.add(cloner.deepClone(brood));
                    }
                    queenSpeed = Config.honeybee_algorithm.getCooling_factor() * queenSpeed;

                    drone = new Drone(workflow, instanceInfo, M_NUMBER);
                }
            });
        }

        for (int i = 0; i < Config.honeybee_algorithm.getNumber_of_threads(); i++) {
            threadList[i].start();
        }

        for (int i = 0; i < Config.honeybee_algorithm.getNumber_of_threads(); i++) {
            try {
                threadList[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        spermathecaList.clear();

        spermathecaList = threadSpermatheca;
    }

    double probability(double queenFitness, double droneFitness, double queenSpeed){

        if(droneFitness > queenFitness)
            return 1;
        else
            return Math.exp((droneFitness - queenFitness) / queenSpeed);
    }

    void generateBrood(){

        for (Spermatheca spermatheca : spermathecaList){
            for (Chromosome brood : spermatheca.chromosomeList){

                //Do mutation for every brood
                mutation(brood);

                if (brood.fitnessValue < queen.chromosome.fitnessValue){
                    queen.chromosome = cloner.deepClone(brood);
                }
            }
        }
    }

    void mutation(Chromosome forMutation){
        Random r = new Random();
        int xOrY = r.nextInt(3);

        switch (xOrY){
            case 0:
                int taskIdToDoMutation = r.nextInt(forMutation.workflow.getJobList().size());
                boolean bl = true;
                int newInstanceId = -1;

                while (bl){
                    newInstanceId = r.nextInt(forMutation.numberOfUsedInstances+1);
                    if (forMutation.xArray[taskIdToDoMutation] != newInstanceId && newInstanceId < Config.global.m_number){
                        bl = false;
                    }
                }

                if (newInstanceId == forMutation.numberOfUsedInstances){
                    forMutation.numberOfUsedInstances++;
                    int newYValue = r.nextInt(InstanceType.values().length);
                    forMutation.xArray[taskIdToDoMutation] = newInstanceId;
                    forMutation.yArray[newInstanceId] = newYValue;
                }else {
                    forMutation.xArray[taskIdToDoMutation] = newInstanceId;
                }
                break;
            case 1:
                int randomInstance1 = r.nextInt(forMutation.numberOfUsedInstances);
                int randomInstance2 = r.nextInt(forMutation.numberOfUsedInstances);

                int yVal1 = forMutation.yArray[randomInstance2];

                forMutation.yArray[randomInstance2] = forMutation.yArray[randomInstance1];
                forMutation.yArray[randomInstance1] = yVal1;
                break;
            case 2:
                //todo: z array
                break;
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
        //todo: z array crossover

        Chromosome chromosome = new Chromosome(workflow, instanceInfo, M_NUMBER);
        chromosome.numberOfUsedInstances = numberOfInstancesUsed;
        chromosome.xArray = newXArray;
        chromosome.yArray = newYArray;
        chromosome.solutionMapping();

        chromosome.fitness();

        return chromosome;
    }

     Chromosome localSearch(Chromosome mainChr){
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

        //all neighbors with new instance selected
        if (mainChr.numberOfUsedInstances != M_NUMBER){
            for (int i = 0; i < mainChr.xArray.length; i++) {
                int newXArray [] = new int[mainChr.workflow.getJobList().size()];
                System.arraycopy(mainChr.xArray , 0, newXArray, 0, mainChr.xArray.length);

                int newInstanceId = mainChr.numberOfUsedInstances;
                newXArray[i] = newInstanceId;

                for (int j = 0; j < InstanceType.values().length; j++) {
                    int []newYArray = new int[M_NUMBER];
                    System.arraycopy(mainChr.yArray, 0, newYArray,0, mainChr.yArray.length);

                    newYArray[newInstanceId] = j;

                    Chromosome newChromosome = new Chromosome(workflow, mainChr.instanceInfo, M_NUMBER);
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

    Chromosome lightLocalSearch(Chromosome mainChr, int kRandomTasks){
        Cloner cloner = new Cloner();
        Chromosome currentBestChr = cloner.deepClone(mainChr);

        //local search on all of the instances with only one assigned task
        for (int i = 0; i < mainChr.xArray.length; i++) {
            if (mainChr.instanceUsages[mainChr.xArray[i]] == 1){
                int taskId = i;
                for (int j = 0; j < mainChr.numberOfUsedInstances; j++) {
                    if (mainChr.xArray[taskId] != j){
                        int newXArray [] = new int[mainChr.workflow.getJobList().size()];
                        System.arraycopy(mainChr.xArray , 0, newXArray, 0, mainChr.xArray.length);
                        newXArray[taskId] = j;

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

                //for assigning to new instance
                int newXArray [] = new int[mainChr.workflow.getJobList().size()];
                System.arraycopy(mainChr.xArray , 0, newXArray, 0, mainChr.xArray.length);
                newXArray[taskId] = mainChr.numberOfUsedInstances;
                for (InstanceType type : InstanceType.values()){
                    int []newYArray = new int[M_NUMBER];
                    System.arraycopy(mainChr.yArray, 0, newYArray,0, mainChr.yArray.length);
                    newYArray[mainChr.numberOfUsedInstances] = type.getId();

                    Chromosome newChromosome = new Chromosome(workflow, mainChr.instanceInfo, M_NUMBER);
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

        //change only Y array
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

        boolean visitedTasks[] = new boolean[mainChr.xArray.length];
        int iteration = kRandomTasks;
        Random r = new Random();

        //randomly do local search on a specified number of tasks
        while (iteration != 0){
            int randomTask = r.nextInt(mainChr.xArray.length);
            if (!visitedTasks[randomTask]){
                visitedTasks[randomTask] = true;
                for (int j = 0; j < mainChr.numberOfUsedInstances; j++) {
                    if (mainChr.xArray[randomTask] != j){
                        int newXArray [] = new int[mainChr.xArray.length];
                        System.arraycopy(mainChr.xArray , 0, newXArray, 0, mainChr.xArray.length);
                        newXArray[randomTask] = j;

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

                //all neighbors with new instance
                for (int i = 0; i < InstanceType.values().length; i++) {
                    int []newYArray = new int[M_NUMBER];
                    System.arraycopy(mainChr.yArray, 0, newYArray,0, mainChr.yArray.length);
                    int newXArray [] = new int[mainChr.xArray.length];
                    System.arraycopy(mainChr.xArray , 0, newXArray, 0, mainChr.xArray.length);

                    newXArray[randomTask] = mainChr.numberOfUsedInstances;

                    newYArray[mainChr.numberOfUsedInstances] = i;

                    Chromosome newChromosome = new Chromosome(workflow, mainChr.instanceInfo, M_NUMBER);
                    newChromosome.xArray = newXArray;
                    newChromosome.yArray = newYArray;
                    newChromosome.numberOfUsedInstances = mainChr.numberOfUsedInstances+1;
                    newChromosome.fitness();

                    if (newChromosome.fitnessValue < currentBestChr.fitnessValue){
                        currentBestChr = cloner.deepClone(newChromosome);
                    }
                }
                iteration--;
            }
        }

        return currentBestChr;
    }
}
