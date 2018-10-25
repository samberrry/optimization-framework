package org.optframework.core.pacsa;

import org.optframework.config.Config;
import org.optframework.core.InstanceInfo;
import org.optframework.core.Solution;
import org.optframework.core.Workflow;
import org.optframework.core.utils.Printer;

/**
 * This PACSA versions does the algorithm based on iteration number specified in the config file
 * */

public class PACSAIterationNumber extends PACSAOptimization{

    public PACSAIterationNumber(double pheromoneInitialSeed, Workflow workflow, InstanceInfo[] instanceInfo) {
        super(pheromoneInitialSeed, workflow, instanceInfo);
    }

    @Override
    public Solution runAlgorithm() {
        Printer.printSAInfo();

        //This generates the random initial solutions for the PACSA algorithm
        generateRandomInitialSolutionList();

        //here is the only difference
        for (int p = 0; p < Config.pacsa_algorithm.iteration_number; p++){
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
}
