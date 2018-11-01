package org.optframework.core.pacsa;


import com.rits.cloning.Cloner;
import org.apache.commons.collections4.list.SetUniqueList;
import org.cloudbus.cloudsim.util.workload.WorkflowDAG;
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

    protected List<Solution> outInitialSolution;
    protected Solution globalBestSolution;
    protected List<Solution> initialSolutionList = new ArrayList<>();

    protected double pheromoneTrailForX[][];
    protected double pheromoneTrailForY[][];
    protected double pheromoneTrailForZ[][];

    protected Workflow workflow;
    protected WorkflowDAG dag;

    protected InstanceInfo instanceInfo[];
//    int numberOfCurrentUsedInstances;

    public PACSAOptimization(List<Solution> outInitialSolution, double pheromoneInitialSeed, Workflow workflow, InstanceInfo instanceInfo[]) {
        this.workflow = workflow;
        this.dag = workflow.getWfDAG();
        this.instanceInfo = instanceInfo;
        this.outInitialSolution = outInitialSolution;

        /**
         * Pheromone trail structure:
         * rows = tasks
         * columns = number of instances + number of different types of instances
         * */
        pheromoneTrailForX = new double[Config.global.m_number][this.workflow.getNumberTasks()];
        for (int i = 0; i < Config.global.m_number; i++) {
            for (int j = 0; j < workflow.getNumberTasks(); j++) {
                pheromoneTrailForX[i][j] = pheromoneInitialSeed;
            }
        }

        pheromoneTrailForY = new double[instanceInfo.length][Config.global.m_number];
        for (int i = 0; i < instanceInfo.length; i++) {
            for (int j = 0; j < Config.global.m_number; j++) {
                pheromoneTrailForY[i][j] = pheromoneInitialSeed;
            }
        }

        pheromoneTrailForZ = new double[this.workflow.getJobList().size()][this.workflow.getJobList().size()];
        for (int i = 0; i < workflow.getJobList().size(); i++) {
            for (int j = 0; j < workflow.getJobList().size(); j++) {
                pheromoneTrailForZ[i][j] = pheromoneInitialSeed;
            }
        }
    }

    @Override
    public Solution runAlgorithm() {
        Printer.printSAInfo();

        //This generates the random initial solutions for the PACSA algorithm
        generateRandomInitialSolutionList();

        while (Config.sa_algorithm.cooling_factor < Config.pacsa_algorithm.equilibrium_point) {
            Solution[] antSolutionList = runAnts();
            Solution bestCurrentSolution = antSolutionList[0];

            //Update the best solution
            for (Solution solution: antSolutionList){
                if (solution.fitnessValue < bestCurrentSolution.fitnessValue){
                    bestCurrentSolution = solution;
                }
            }
            if (globalBestSolution == null){
                globalBestSolution = bestCurrentSolution;
            }else {
                if (bestCurrentSolution.fitnessValue < globalBestSolution.fitnessValue){
                    globalBestSolution = bestCurrentSolution;
                }
            }

            Solution solutionToUpdate;
            if (Config.pacsa_algorithm.global_based){
                solutionToUpdate = globalBestSolution;
            }else {
                solutionToUpdate = bestCurrentSolution;
            }

            //The best current solution (found in this iteration) updates the pheromone trail
            for (int k = 0; k < workflow.getNumberTasks(); k++) {
                for (int j = 0; j < Config.global.m_number; j++) {
                    if (j == solutionToUpdate.xArray[k]){
                        pheromoneTrailForX[j][k] = (pheromoneTrailForX[j][k] * Config.pacsa_algorithm.evaporation_factor) + 1 / solutionToUpdate.fitnessValue;
                    }else {
                        pheromoneTrailForX[j][k] *= Config.pacsa_algorithm.evaporation_factor;
                    }
                }
            }

            for (int k = 0; k < solutionToUpdate.numberOfUsedInstances; k++) {
                for (int j = 0; j < instanceInfo.length; j++) {
                    if (j == solutionToUpdate.xArray[k]){
                        pheromoneTrailForY[j][k] = (pheromoneTrailForY[j][k] * Config.pacsa_algorithm.evaporation_factor) + 1 / solutionToUpdate.fitnessValue;
                    }else {
                        pheromoneTrailForY[j][k] *= Config.pacsa_algorithm.evaporation_factor;
                    }
                }
            }

            for (int k = 0; k < workflow.getJobList().size(); k++) {
                for (int j = 0; j < workflow.getJobList().size(); j++) {
                    if (j == solutionToUpdate.zArray[k]){
                        pheromoneTrailForZ[j][k] = (pheromoneTrailForZ[j][k] * Config.pacsa_algorithm.evaporation_factor) + 1 / solutionToUpdate.fitnessValue;
                    }else {
                        pheromoneTrailForZ[j][k] *= Config.pacsa_algorithm.evaporation_factor;
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

    Solution[] runAnts(){
        Thread threadList[] = new Thread[Config.pacsa_algorithm.getNumber_of_ants()];
        Solution[] solutionList = new Solution[Config.pacsa_algorithm.getNumber_of_ants()];

        for (int i = 0; i < Config.pacsa_algorithm.getNumber_of_ants(); i++) {
            int itr = i;
            threadList[i] = new Thread(() -> {
                SimulatedAnnealingAlgorithm sa = new SimulatedAnnealingAlgorithm(initialSolutionList.get(itr), workflow, instanceInfo);

                Solution solution = sa.runAlgorithm();
                solutionList[itr] = solution;
            });
        }

        for (int i = 0; i < Config.pacsa_algorithm.number_of_ants; i++) {
            threadList[i].start();
        }

        for (int i = 0; i < Config.pacsa_algorithm.number_of_ants; i++) {
            try {
                threadList[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return solutionList;
    }

    protected void generateRandomInitialSolutionList(){
        if (outInitialSolution.size() != 0){
            for (Solution solution: outInitialSolution){
                initialSolutionList.add(solution);
            }
            for (int i = 0; i < Config.pacsa_algorithm.number_of_ants - outInitialSolution.size(); i++) {
                Solution solution = new Solution(workflow, instanceInfo, Config.global.m_number);
                solution.generateRandomSolution(workflow);
                initialSolutionList.add(i , solution);
            }
        }else {
            for (int i = 0; i < Config.pacsa_algorithm.number_of_ants; i++) {
                Solution solution = new Solution(workflow, instanceInfo, Config.global.m_number);
                solution.generateRandomSolution(workflow);
                initialSolutionList.add(i , solution);
            }
        }
    }

    protected Solution generateInitialSolutionFromPheromone(){
        int generatedXArray[] = new int[workflow.getNumberTasks()];
        int generatedYArray[] = new int[Config.global.m_number];
        Integer generatedZArray[] = new Integer[workflow.getNumberTasks()];
        Random rand = new Random();
        int maxInstances = -1;

        for (int k = 0; k < workflow.getNumberTasks(); k++) {
            double xProbability[] = new double[Config.global.m_number];
            for (int j = 0; j < Config.global.m_number; j++) {
                double pheromoneSum = 0;
                for (int i = 0; i < Config.global.m_number; i++) {
                    pheromoneSum += pheromoneTrailForX[i][k];
                }
                //computes probability for a total column
                xProbability[j] = (pheromoneTrailForX[j][k] / pheromoneSum);
            }
            double randomX = rand.nextDouble();
            double probabilitySumTemp = 0;
            int selectedInstance = -1;
            for (int i = 0; i < Config.global.m_number; i++) {
                probabilitySumTemp += xProbability[i];
                if (probabilitySumTemp > randomX){
                    selectedInstance = i;
                    break;
                }
            }
            generatedXArray[k] = selectedInstance;
            if (selectedInstance > maxInstances){
                maxInstances = selectedInstance;
            }
        }

        for (int instanceId: generatedXArray){
            double yProbability[] = new double[instanceInfo.length];
            for (int j = 0; j < instanceInfo.length; j++) {
                double pheromoneSum = 0;
                for (int i = 0; i < instanceInfo.length; i++) {
                    pheromoneSum += pheromoneTrailForY[i][instanceId];
                }
                yProbability[j] = (pheromoneTrailForY[j][instanceId] / pheromoneSum);
            }
            double randomY = rand.nextDouble();
            double probabilitySumTemp = 0;
            int selectedInstance = -1;
            for (int i = 0; i < instanceInfo.length; i++) {
                probabilitySumTemp += yProbability[i];
                if (probabilitySumTemp > randomY){
                    selectedInstance = i;
                    break;
                }
            }
            generatedYArray[instanceId] = selectedInstance;
        }

        boolean repeatIt = true;
        ArrayList<Integer> readyTasksToOrder = dag.getFirstLevel();

        for (int k = 0; k < workflow.getNumberTasks(); k++) {
            double zProbability[] = new double[workflow.getJobList().size()];
            for (Integer taskIdI: readyTasksToOrder){
                double pheromoneSum = 0;
                for (Integer taskId : readyTasksToOrder){
                    pheromoneSum += pheromoneTrailForZ[taskId][k];
                }
                zProbability[taskIdI] = (pheromoneTrailForZ[taskIdI][k] / pheromoneSum);
            }
            int newSelectedTaskToOrder = -1;
            int idInReadyList = -1;
            while (repeatIt){
                double randomX = rand.nextDouble();
                double probabilitySumTemp = 0;
                for (int i = 0; i < readyTasksToOrder.size(); i++) {
                    probabilitySumTemp += zProbability[readyTasksToOrder.get(i)];
                    if (probabilitySumTemp > randomX){
                        newSelectedTaskToOrder = readyTasksToOrder.get(i);
                        idInReadyList = i;
                        break;
                    }
                }

                ArrayList<Integer> parentList = dag.getParents(newSelectedTaskToOrder);
                int isSeen = 0;
                for (Integer parentId: parentList){
                    for (int j = 0; j < k; j++) {
                        if (parentId == generatedZArray[j]){
                            isSeen++;
                        }
                    }
                }
                if (isSeen == parentList.size()){
                    repeatIt = false;
                }
            }
            readyTasksToOrder.remove(idInReadyList);
            readyTasksToOrder.addAll(dag.getChildren(newSelectedTaskToOrder));
            SetUniqueList.setUniqueList(readyTasksToOrder);
            repeatIt = true;
            generatedZArray[k] = newSelectedTaskToOrder;
        }

        Solution solution = new Solution(workflow, instanceInfo, maxInstances + 1);
        solution.numberOfUsedInstances = maxInstances + 1;
        solution.xArray = generatedXArray;
        solution.yArray = generatedYArray;
        solution.zArray = generatedZArray;
        solution.fitness();

        return solution;
    }
}
