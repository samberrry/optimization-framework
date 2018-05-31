package org.optframework.core;

import org.cloudbus.cloudsim.util.workload.Workflow;
import org.optframework.config.StaticProperties;

import java.util.HashSet;
import java.util.Set;

public class SimulatedAnnealingAlgorithm implements StaticProperties {

    double temp;

    Set visited_solutions = new HashSet<Solution>();

    Solution best;

    int globalCounter = 0;

    Workflow workflow;

    public SimulatedAnnealingAlgorithm() {
    }

    public SimulatedAnnealingAlgorithm(Workflow workflow) {
        this.workflow = workflow;
    }

    public Solution runSA(){
        Log.logger.info("Starts SA Algorithm");
        Log.logger.info("Simulated Annealing parameters Initial temp: "+ START_TEMP+ " Final temp: " + FINAL_TEMP + " Cooling Factor: " + COOLING_FACTOR + " Equilibrium point: " + SA_EQUILIBRIUM_COUNT);

//        INPUTS: OK

//        GENERATION of the initial solution = X Y arrays

//        LOOP

    //        LOOP at a fixed temperature:
    //        GENERATE random neighbors
    //        IF checks to accept the neighbor solution
    //        ELSE accept with probability
    //        UNTIL equilibrium

//        TEMPERATURE update

//        UNTIL stopping criteria T < T min

//        OUTPUT: best solution found
        return null;
    }
}
