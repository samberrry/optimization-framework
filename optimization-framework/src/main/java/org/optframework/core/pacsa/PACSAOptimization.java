package org.optframework.core.pacsa;


import org.optframework.config.Config;
import org.optframework.core.*;
import org.optframework.core.sa.SimulatedAnnealingAlgorithm;
import org.optframework.core.utils.Printer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Implementation for Parallel Ant Colony leveraged by Simulated Annealing
 * optimization algorithm
 *
 *
 * @author Hessam - hessam.modaberi@gmail.com
 * @since 2018
 * */

public class PACSAOptimization implements OptimizationAlgorithm {

    Solution globalBestSolution;
    List<Solution> initialSolutionList = new ArrayList<>();

    double pheromoneTrailForX[][];
    double pheromoneTrailForY[][];

    private Workflow workflow;

    private InstanceInfo instanceInfo[];
    int maxNumberOfUsedInstances;

    public PACSAOptimization(Workflow workflow, InstanceInfo instanceInfo[]) {
        this.workflow = workflow;
        this.instanceInfo = instanceInfo;
        /**
         * Pheromone trail structure:
         * rows = tasks
         * columns = number of instances + number of different types of instances
         * */
        pheromoneTrailForX = new double[Config.global.m_number][this.workflow.getNumberTasks()];
        pheromoneTrailForY = new double[instanceInfo.length][this.workflow.getNumberTasks()];
    }

    @Override
    public Solution runAlgorithm() {
        Printer.printSAInfo();

        //This generates the random initial solutions for the PACSA algorithm
        generateRandomInitialSolutionList();

        while (Config.sa_algorithm.cooling_factor < Config.pacsa_algorithm.equilibrium_point) {
            List<Solution> antSolutionList = runAnts();
            Solution bestCurrentSolution = antSolutionList.get(0);

            //Update the best solution
            for (Solution solution: antSolutionList){
                if (solution.fitnessValue < bestCurrentSolution.fitnessValue){
                    bestCurrentSolution = solution;
                }
            }
            if (bestCurrentSolution.fitnessValue < globalBestSolution.fitnessValue){
                globalBestSolution = bestCurrentSolution;
            }

            //Updates the pheromone trail
            for (int k = 0; k < workflow.getNumberTasks(); k++) {
                for (int j = 0; j < Config.global.m_number; j++) {
                    if (pheromoneTrailForX[j][k] == bestCurrentSolution.xArray[k]){
                        pheromoneTrailForX[j][k] = (pheromoneTrailForX[j][k] * Config.pacsa_algorithm.evaporation_factor) + 1 / bestCurrentSolution.fitnessValue;
                    }else {
                        pheromoneTrailForX[j][k] *= Config.pacsa_algorithm.evaporation_factor;
                    }
                }
            }

            for (int k = 0; k < workflow.getNumberTasks(); k++) {
                for (int j = 0; j < instanceInfo.length; j++) {
                    if (pheromoneTrailForY[j][k] == bestCurrentSolution.xArray[k]){
                        pheromoneTrailForY[j][k] = (pheromoneTrailForY[j][k] * Config.pacsa_algorithm.evaporation_factor) + 1 / bestCurrentSolution.fitnessValue;
                    }else {
                        pheromoneTrailForY[j][k] *= Config.pacsa_algorithm.evaporation_factor;
                    }
                }
            }

            //Update cooling factor
            Config.sa_algorithm.cooling_factor *= Config.pacsa_algorithm.cf_increase_ratio;
            //Update initial temperature
            Config.sa_algorithm.start_temperature *= Config.pacsa_algorithm.temp_decrease_ratio;

            for (int i = 0; i < Config.pacsa_algorithm.number_of_ants; i++) {
                initialSolutionList.add(i, generateInitialSolutionFromPheromone());
            }
        }

        return globalBestSolution;
    }

    List<Solution> runAnts(){
        Thread threadList[] = new Thread[Config.pacsa_algorithm.getNumber_of_ants()];
        List<Solution> solutionList = new ArrayList<>();

        for (int i = 0; i < Config.pacsa_algorithm.getNumber_of_ants(); i++) {
            solutionList.add(i, new Solution(workflow, instanceInfo, Config.global.m_number));
            int itr = i;
            threadList[i] = new Thread(() -> {
                SimulatedAnnealingAlgorithm sa = new SimulatedAnnealingAlgorithm(initialSolutionList.get(itr), workflow, instanceInfo);

                Solution solution = sa.runAlgorithm();
                solutionList.add(itr, solution);
            });
        }

        for (int i = 0; i < Config.pacsa_algorithm.number_of_ants; i++) {
            threadList[i].start();
        }

        for (int i = 0; i < Config.honeybee_algorithm.getNumber_of_threads(); i++) {
            try {
                threadList[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return solutionList;
    }

    private void generateRandomInitialSolutionList(){
        for (int i = 0; i < Config.pacsa_algorithm.number_of_ants; i++) {
            Solution solution = new Solution(workflow, instanceInfo, Config.global.m_number);
            solution.generateRandomSolution(workflow);
            initialSolutionList.add(i , solution);
        }
    }

    private Solution generateInitialSolutionFromPheromone(){
        int numberOfUsedInstances = 0;

        for (int k = 0; k < workflow.getNumberTasks(); k++) {
            double xProbability[] = new double[Config.global.m_number];
            for (int j = 0; j < Config.global.m_number; j++) {
                double pheromoneSum = 0;
                for (int i = 0; i < Config.global.m_number; i++) {
                    if (j != i){
                        pheromoneSum += pheromoneTrailForX[i][k];
                    }
                }
                xProbability[j] = (pheromoneTrailForX[k][j] / pheromoneSum);
            }
            Random rand = new Random();


        }

        for (int k = 0; k < workflow.getNumberTasks(); k++) {
            for (int j = 0; j < instanceInfo.length; j++) {

            }
        }

        Solution solution = new Solution(workflow, instanceInfo, numberOfUsedInstances);
        solution.fitness();
        return solution;
    }
}
