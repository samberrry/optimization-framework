package org.optframework.core;

import org.cloudbus.cloudsim.util.workload.Workflow;
import org.optframework.config.StaticProperties;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class SimulatedAnnealingAlgorithm implements StaticProperties {

    double temp;

    Set<Solution> visited_solutions = new HashSet<>();

    Solution bestCurrent;

    int globalCounter = 0;

    Workflow workflow;

    double prices[];

    public SimulatedAnnealingAlgorithm() {
    }

    public SimulatedAnnealingAlgorithm(Workflow workflow, double[] prices) {
        this.workflow = workflow;
        this.prices = prices;
    }

    public Solution runSA(){
        Log.logger.info("Starts SA Algorithm");
        Log.logger.info("Simulated Annealing parameters Initial temp: "+ START_TEMP+ " Final temp: " + FINAL_TEMP + " Cooling Factor: " + COOLING_FACTOR + " Equilibrium point: " + SA_EQUILIBRIUM_COUNT);

        Solution initialSolution = new Solution(workflow.getJobList().size(), M_NUMBER);

        //Initializes the initial solution with random values
        initialSolution.generateRandomSolution(workflow);

        temp = START_TEMP;
        bestCurrent = initialSolution;

        //LOOP at a fixed temperature:
        while (temp >= FINAL_TEMP){
            for (int i = 0; i < SA_EQUILIBRIUM_COUNT; i++) {
                //GENERATES random neighbor
                Solution randomNeighbor = new Solution(workflow.getJobList().size(), M_NUMBER);
                if (!visited_solutions.contains(randomNeighbor)){
                    visited_solutions.add(randomNeighbor);

                    double delta = fitness(randomNeighbor) - fitness(bestCurrent);
                    if (delta <= 0){
                        bestCurrent = randomNeighbor;
                    }else {
                        //Generate a uniform random value x in the range (0,1)
                        Random r = new Random();
                        double random = r.nextDouble();

                        if (random < bolzmanDist(delta, temp)){
                            bestCurrent = randomNeighbor;
                        }
                    }
                }else{
                    updateVisitedField(randomNeighbor);
                }
            }
            temp -= COOLING_FACTOR;
        }
        return bestCurrent;
    }

    void updateVisitedField(Solution solution){
        for (Solution temp: visited_solutions){
            if (temp.equals(solution)){
                temp.visited ++;
            }
        }
    }

    double bolzmanDist(double delta, double temp){
        return Math.exp(-(Math.abs(delta))/temp);
    }

    double fitness(Solution solution){


        return 0;
    }
}
