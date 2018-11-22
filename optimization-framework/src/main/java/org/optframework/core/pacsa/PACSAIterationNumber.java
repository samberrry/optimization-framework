package org.optframework.core.pacsa;

import org.cloudbus.cloudsim.Log;
import org.optframework.RunPACSAAlgorithm;
import org.optframework.config.Config;
import org.optframework.core.InstanceInfo;
import org.optframework.core.Solution;
import org.optframework.core.Workflow;
import org.optframework.core.utils.Printer;

import java.util.List;

/**
 * This PACSA versions does the algorithm based on iteration number specified in the config file
 * */

public class PACSAIterationNumber extends PACSAOptimization{

    public PACSAIterationNumber(List<Solution> initialSolutionList, double pheromoneInitialSeed, Workflow workflow, InstanceInfo[] instanceInfo) {
        super(initialSolutionList, pheromoneInitialSeed, workflow, instanceInfo);
    }

    @Override
    public Solution runAlgorithm() {
        Printer.printSAInfo();


        Printer.printSAInfo();

        int iteration_counter = 0;

        //This generates the random initial solutions for the PACSA algorithm
        generateRandomInitialSolutionList();

        while (iteration_counter <= Config.pacsa_algorithm.iteration_number) {
            iteration_counter++;
            Solution[] antSolutionList = runAnts();
            Solution bestCurrentSolution = null;
            try {
                bestCurrentSolution = antSolutionList[0].clone();
            }
            catch (Exception e)
            {
                org.optframework.core.Log.logger.info("Cloning Exception");
            }

            //Update the best solution
            for (Solution solution: antSolutionList){
                if (solution.fitnessValue < bestCurrentSolution.fitnessValue){
                    bestCurrentSolution = solution;
                }
            }
            if (globalBestSolution == null){
                try {
                    globalBestSolution = bestCurrentSolution.clone();
                }
                catch (Exception e)
                {
                    org.optframework.core.Log.logger.info("Cloning Exception");
                }
                RunPACSAAlgorithm.Best_Iteration = iteration_counter;
            }else {
                if (bestCurrentSolution.fitnessValue < globalBestSolution.fitnessValue){
                    try {
                        globalBestSolution = bestCurrentSolution.clone();
                    }
                    catch (Exception e)
                    {
                        org.optframework.core.Log.logger.info("Cloning Exception");
                    }
                    RunPACSAAlgorithm.Best_Iteration = iteration_counter;
                }
            }

            Solution solutionToUpdate;
            if (Config.pacsa_algorithm.global_based){
                solutionToUpdate = globalBestSolution;
            }else {
                solutionToUpdate = bestCurrentSolution;
            }

            for (int instanceId : solutionToUpdate.xArray){
                if (!instanceVisited[instanceId]){
                    usedInstances.add(instanceId);
                    instanceVisited[instanceId] = true;
                }
            }

            //updated by Hamid/////////////

            //updates x pheromone trail
            for (int i = 0; i < Config.global.m_number; i++) {
                for (int j = 0; j < workflow.getNumberTasks(); j++) {
                    pheromoneTrailForX[i][j] *= Config.pacsa_algorithm.evaporation_factor;
                    if(solutionToUpdate.xArray[j] == i){
                        pheromoneTrailForX[i][j] += 1 / solutionToUpdate.fitnessValue;
                    }
                }
            }
            // for (int i = 0; i < workflow.getNumberTasks(); i++) {

            //pheromoneTrailForX[solutionToUpdate.xArray[i]][i] += 1 / solutionToUpdate.fitnessValue;//(pheromoneTrailForX[solutionToUpdate.xArray[i]][i] * Config.pacsa_algorithm.evaporation_factor) + 1 / solutionToUpdate.fitnessValue;
            //}

            //updates y pheromone trail
            for (int i = 0; i < instanceInfo.length; i++) {
                for (int j = 0; j < Config.global.m_number; j++) {
                    pheromoneTrailForY[i][j] *= Config.pacsa_algorithm.evaporation_factor;
                    if(solutionToUpdate.yArray[j] == i){
                        pheromoneTrailForY[i][j] += 1 / solutionToUpdate.fitnessValue;
                    }
                }
            }

            //   for (Integer instanceId: usedInstances){
            //       pheromoneTrailForY[solutionToUpdate.yArray[instanceId]][instanceId] += 1 / solutionToUpdate.fitnessValue;//(pheromoneTrailForY[solutionToUpdate.yArray[instanceId]][instanceId] * Config.pacsa_algorithm.evaporation_factor) + 1 / solutionToUpdate.fitnessValue;
            //   }

            //updates z pheromone trail
            for (int k = 0; k < workflow.getJobList().size(); k++) {
                for (int j = 0; j < workflow.getJobList().size(); j++) {
                    pheromoneTrailForZ[j][k] *= Config.pacsa_algorithm.evaporation_factor;
                    if (j == solutionToUpdate.zArray[k]) {
                        pheromoneTrailForZ[j][k] += 1 / solutionToUpdate.fitnessValue;
                    }


                }
            }

            //update current base seed
            currentBasePheromoneValue *= Config.pacsa_algorithm.evaporation_factor;

            //prepares probability matrix for solution generation from pheromone trail
            createProbabilityMatrix();

            for (int i = 0; i < Config.pacsa_algorithm.number_of_ants; i++) {
                initialSolutionList.add(i, generateInitialSolutionFromPheromone());
            }
        }
     //   Log.logger.info("Pacsa Iterations="+iteration_counter);
        Log.logger.info("The best solution in PACSA founded in iteration:"+RunPACSAAlgorithm.Best_Iteration+"\n");
        return globalBestSolution;

/*
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

            if(!Config.pacsa_algorithm.iteration_number_based) {
                //Update cooling factor
                Config.sa_algorithm.cooling_factor *= Config.pacsa_algorithm.cf_increase_ratio;
                //Update initial temperature
                Config.sa_algorithm.start_temperature *= Config.pacsa_algorithm.temp_decrease_ratio;

                for (int i = 0; i < Config.pacsa_algorithm.number_of_ants; i++) {
                    initialSolutionList.add(i, generateInitialSolutionFromPheromone());
                }
            }
        }

        return globalBestSolution;*/

    }
}
