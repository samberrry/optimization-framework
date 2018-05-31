package org.optframework.core;


import org.cloudbus.cloudsim.util.workload.Workflow;
import org.optframework.config.StaticProperties;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

/**
 * Implementation for Parallel Ant Colony leveraged by Simulated Annealing
 * optimization algorithm
 *
 *
 * @author Hessam - hessam.modaberi@gmail.com
 * @since 2018
 * */

public class PACSAOptimization implements OptimizationAlgorithm ,StaticProperties {

    Solution solution;

    double pheromoneTrail[][];

    static Workflow workflow;

    public PACSAOptimization(Workflow workflow) {
        this.workflow = workflow;
    }

    @Override
    public void runAlgorithm() {
        Log.logger.info("PACSA Optimization Algorithm is started");
        ForkJoinPool pool = new ForkJoinPool();

        //Initializes the size of the pheromone trail
        Log.logger.info("Initializes Pheromone Trail with the size of: ["+workflow.getJobList().size()+"]["+M_NUMBER+"] Number of workflow tasks: "+workflow.getJobList().size()+ " Maximum number of instances: "+ M_NUMBER);

        pheromoneTrail = new double[workflow.getJobList().size()][M_NUMBER];

        for (int i = 0; i < 10; i++) {
            Log.logger.info("Iteration " + i + 1 + " is started with "+ NUMBER_OF_ANTS + " ants");
            Ant ant = new Ant(NUMBER_OF_ANTS);
            ant.workflow = workflow;
            List<Solution> solutionList  =  pool.invoke(ant);

            // Print results
            //Getting feedback and update pheromone trail

            Log.logger.info("End of iteration "+ i + 1);
        }

        Log.logger.info("End of PACSA Algorithm");
    }

}
