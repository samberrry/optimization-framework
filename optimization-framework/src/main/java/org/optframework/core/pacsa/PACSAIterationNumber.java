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
   // private static double test_general_best = 999999999.9;

    int maxNumberOfInstances;

    public PACSAIterationNumber(List<Solution> initialSolutionList, double pheromoneInitialSeed, Workflow workflow, InstanceInfo[] instanceInfo, int maxNumberOfInstances) {
        super(initialSolutionList, pheromoneInitialSeed, workflow, instanceInfo, maxNumberOfInstances);
        this.maxNumberOfInstances = maxNumberOfInstances;
    }

    @Override
    public Solution runAlgorithm() {
        Printer.printSAInfo();


        Printer.printSAInfo();

        int iteration_counter = 0;

        double oroginal_cooling_schedule = Config.sa_algorithm.cooling_factor;

        //This generates the random initial solutions for the PACSA algorithm
        generateRandomInitialSolutionList();

        int global_best_updated = 2;// this is used to speedup the algorithm in the two next iterations after every general best updating

        while (iteration_counter <= Config.pacsa_algorithm.iteration_number) {

            Solution[] antSolutionList;
            if(iteration_counter == 0) {
                antSolutionList = runAnts(true);
            }else
            {
                antSolutionList = runAnts(false);
            }

            iteration_counter++;
            Solution bestCurrentSolution = null;
            try {
                bestCurrentSolution = antSolutionList[0].clone();
            }
            catch (Exception e)
            {
                org.optframework.core.Log.logger.info("Cloning Exception");
            }

            boolean converge_flag = true;
            double previous_solution_fitness = antSolutionList[0].fitnessValue;
            String list_ants_fintess = "";
            //Update the best solution
            for (Solution solution: antSolutionList){
                list_ants_fintess += Double.toString(solution.fitnessValue) + ", ";
                if (solution.fitnessValue < bestCurrentSolution.fitnessValue){
                    bestCurrentSolution = solution;
                }
                if(previous_solution_fitness != solution.fitnessValue)
                {
                    converge_flag = false;
                }
            }
            Log.logger.info("List of ants' fitness:"+list_ants_fintess);

            Log.logger.info("BestCurrentFitness:"+bestCurrentSolution.fitnessValue+" found in iter:"+iteration_counter);
            if (globalBestSolution == null){
                try {
                    globalBestSolution = bestCurrentSolution.clone();
                    Log.logger.info("GeneralBestFitness:"+globalBestSolution.fitnessValue+" found in iter:"+iteration_counter);
                    global_best_updated = 2; // means that two next iterations should work with a very low colling factor to speedup the algorithm
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
                        Log.logger.info("GeneralBestFitness:"+globalBestSolution.fitnessValue+" found in iter:"+iteration_counter);
                        global_best_updated = 2; // means that two next iterations should work with a very low colling factor to speedup the algorithm
                    }
                    catch (Exception e)
                    {
                        org.optframework.core.Log.logger.info("Cloning Exception");
                    }
                    RunPACSAAlgorithm.Best_Iteration = iteration_counter;
                }

            }






            Solution solutionToUpdate;
            if (Config.pacsa_algorithm.global_based) {
                solutionToUpdate = globalBestSolution;
                // }else {
                //     solutionToUpdate = bestCurrentSolution;
                // }

                for (int instanceId : solutionToUpdate.xArray){
                    if (!instanceVisited[instanceId]){
                        usedInstances.add(instanceId);
                        instanceVisited[instanceId] = true;

                        for (int j = 0; j < workflow.getNumberTasks(); j++) {
                            pheromoneTrailForX[instanceId][j] = currentBasePheromoneValue;
                        }

                        for (int i = 0; i < instanceInfo.length; i++) {
                            pheromoneTrailForY[i][instanceId] = currentBasePheromoneValue;
                        }
                    }
                }

                //updated by Hamid/////////////

                //updates x pheromone trail
                for (int i = 0; i < maxNumberOfInstances; i++) {
                    for (int j = 0; j < workflow.getNumberTasks(); j++) {
                        pheromoneTrailForX[i][j] *= Config.pacsa_algorithm.evaporation_factor;
                        //       if(global_best_updated % 2 == 0) {
                        if (solutionToUpdate.xArray[j] == i) {
                            pheromoneTrailForX[i][j] += 1 / solutionToUpdate.fitnessValue;
                        }
                        //       }
                    }
                }
                // for (int i = 0; i < workflow.getNumberTasks(); i++) {

                //pheromoneTrailForX[solutionToUpdate.xArray[i]][i] += 1 / solutionToUpdate.fitnessValue;//(pheromoneTrailForX[solutionToUpdate.xArray[i]][i] * Config.pacsa_algorithm.evaporation_factor) + 1 / solutionToUpdate.fitnessValue;
                //}

                //updates y pheromone trail
                for (int i = 0; i < instanceInfo.length; i++) {
                    for (int j = 0; j < maxNumberOfInstances; j++) {
                        pheromoneTrailForY[i][j] *= Config.pacsa_algorithm.evaporation_factor;
                        //        if(global_best_updated % 2 == 0) {
                        if (solutionToUpdate.yArray[j] == i) {
                            pheromoneTrailForY[i][j] += 1 / solutionToUpdate.fitnessValue;
                        }
                        //        }
                    }
                }

                //   for (Integer instanceId: usedInstances){
                //       pheromoneTrailForY[solutionToUpdate.yArray[instanceId]][instanceId] += 1 / solutionToUpdate.fitnessValue;//(pheromoneTrailForY[solutionToUpdate.yArray[instanceId]][instanceId] * Config.pacsa_algorithm.evaporation_factor) + 1 / solutionToUpdate.fitnessValue;
                //   }

                //updates z pheromone trail
                for (int k = 0; k < workflow.getJobList().size(); k++) {
                    for (int j = 0; j < workflow.getJobList().size(); j++) {
                        pheromoneTrailForZ[j][k] *= Config.pacsa_algorithm.evaporation_factor;
                    //    if(global_best_updated % 2 == 0) {
                        if (j == solutionToUpdate.zArray[k]) {
                            pheromoneTrailForZ[j][k] += 1 / solutionToUpdate.fitnessValue;
                        }
                    //    }


                    }
                }
            }
            else //It is normal ACO not MAX-Min ant system
            {
                 if(iteration_counter == 1) {
                     for (int i = 0; i < maxNumberOfInstances; i++) {   //To avoid overhead in ACO, we assume all instances are visited
                         instanceVisited[i] = true;
                         usedInstances.add(i);
                     }
                 }


                //updated by Hamid/////////////

                //updates x pheromone trail
                for (int i = 0; i < maxNumberOfInstances; i++) {
                    for (int j = 0; j < workflow.getNumberTasks(); j++) {
                        pheromoneTrailForX[i][j] *= Config.pacsa_algorithm.evaporation_factor;
                        for(Solution solution: antSolutionList) {
                            if (solution.xArray[j] == i) {
                                pheromoneTrailForX[i][j] += 1 / solution.fitnessValue;
                            }
                        }
                    }
                }
                // for (int i = 0; i < workflow.getNumberTasks(); i++) {

                //pheromoneTrailForX[solutionToUpdate.xArray[i]][i] += 1 / solutionToUpdate.fitnessValue;//(pheromoneTrailForX[solutionToUpdate.xArray[i]][i] * Config.pacsa_algorithm.evaporation_factor) + 1 / solutionToUpdate.fitnessValue;
                //}

                //updates y pheromone trail
                for (int i = 0; i < instanceInfo.length; i++) {
                    for (int j = 0; j < maxNumberOfInstances; j++) {
                        pheromoneTrailForY[i][j] *= Config.pacsa_algorithm.evaporation_factor;
                        for(Solution solution: antSolutionList) {
                            if (solution.yArray[j] == i) {
                                pheromoneTrailForY[i][j] += 1 / solution.fitnessValue;
                            }
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
                        for(Solution solution: antSolutionList) {
                            if (j == solution.zArray[k]) {
                                pheromoneTrailForZ[j][k] += 1 / solution.fitnessValue;
                            }
                        }


                    }
                }
            }

            //update current base seed
            currentBasePheromoneValue *= Config.pacsa_algorithm.evaporation_factor;

            //prepares probability matrix for solution generation from pheromone trail
            createProbabilityMatrix();

            String List_new_born_ants = "";
            for (int i = 0; i < Config.pacsa_algorithm.number_of_ants; i++) {
                Solution tempSolution = generateInitialSolutionFromPheromone();
                initialSolutionList.add(i, tempSolution);
                List_new_born_ants += Double.toString(tempSolution.fitnessValue) + ", ";


            }

            Log.logger.info("List of newborn ants' fitness:"+List_new_born_ants);

            Log.logger.info("------------------End of iteration "+iteration_counter+" --------------------");


            /*
            Only for test we initialize with a heavy SA and then we use a light version of SA
             */
            if(global_best_updated > 0) {
                Config.sa_algorithm.cooling_factor = 0.9;//*= Config.pacsa_algorithm.cf_increase_ratio;
                global_best_updated--;
            }
            else
            {
               // if(workflow.getJobList().size() >= 900)
                    Config.sa_algorithm.cooling_factor = oroginal_cooling_schedule;//
              //  else
              //      Config.sa_algorithm.cooling_factor = oroginal_cooling_schedule;//*= Config.pacsa_algorithm.cf_increase_ratio;
            }


            Config.sa_algorithm.start_temperature = 1.0;//*= Config.pacsa_algorithm.temp_decrease_ratio;

            if (converge_flag) {

             //   revolution(initialSeed);
             //   currentBasePheromoneValue = initialSeed;
             //   converge_flag = false;

              //  Log.logger.info("_________________________-Revolution!_______________________________");


                  break;

                //   Config.sa_algorithm.cooling_factor = 0.999;
                //   Config.sa_algorithm.start_temperature = 85000.0;
            }

        }

        Config.sa_algorithm.cooling_factor = oroginal_cooling_schedule;// to make sure that the cooling schedule SA has not been changed

     //   Log.logger.info("Pacsa Iterations="+iteration_counter);
        Log.logger.info("The best solution in PACSA founded in iteration:"+RunPACSAAlgorithm.Best_Iteration+"\n");
        return globalBestSolution;



    }
}
