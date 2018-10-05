package org.optframework.core.pacsa;


import org.optframework.config.Config;
import org.optframework.core.*;
import org.optframework.core.utils.Printer;

import java.util.ArrayList;
import java.util.List;

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

    double pheromoneTrail[][];

    private Workflow workflow;

    private InstanceInfo instanceInfo[];

    private List<Solution> antSolutions = new ArrayList<>();

    public PACSAOptimization(Workflow workflow, InstanceInfo instanceInfo[]) {
        this.workflow = workflow;
        this.instanceInfo = instanceInfo;
        /**
         * Pheromone trail structure:
         * rows = tasks
         * columns = number of instances + number of different types of instances
         * */
        pheromoneTrail = new double[workflow.getNumberTasks()][Config.global.m_number + instanceInfo.length];
    }

    @Override
    public Solution runAlgorithm() {
        Printer.printSAInfo();

        for (int i = 0; i < 1; i++) {
            runAnts();
            //TODO: update the best solution
            //TODO: increment iteration counter
            //TODO: update pheromone trail
            //TODO: update cooling factor
            //TODO: update initial temperature

        }



        return globalBestSolution;
    }

    void runAnts(){
        Thread threadList[] = new Thread[Config.pacsa_algorithm.getNumber_of_ants()];
        List<Solution> solutionList = new ArrayList<>();

        for (int itr = 0; itr < Config.pacsa_algorithm.getNumber_of_ants(); itr++) {
            solutionList.add(itr, new Solution(workflow, instanceInfo, Config.global.m_number));
            threadList[itr] = new Thread(() -> {

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
    }

}
