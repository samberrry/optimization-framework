package org.optframework.core;

import com.rits.cloning.Cloner;
import org.cloudbus.cloudsim.util.workload.Workflow;
import org.optframework.config.StaticProperties;

import java.util.*;

/**
 * @author Hessam Modabberi hessam.modaberi@gmail.com
 * @version 1.0.0
 */
public class SimulatedAnnealingAlgorithm implements OptimizationAlgorithm, StaticProperties {

    double temp;

    Set<Solution> visited_solutions = new HashSet<>();

    Solution bestCurrent;

    Solution globalBest;

    Workflow workflow;

    InstanceInfo instanceInfo[];

    Cloner cloner = new Cloner();

    public SimulatedAnnealingAlgorithm() {
    }

    public SimulatedAnnealingAlgorithm(Workflow workflow, InstanceInfo[] instanceInfo) {
        this.workflow = workflow;
        this.instanceInfo = instanceInfo;
    }

    @Override
    public Solution runAlgorithm(){
        Log.logger.info("Starts SA Algorithm");
        Log.logger.info("Simulated Annealing parameters Initial temp: "+ START_TEMP+ " Final temp: " + FINAL_TEMP + " Cooling Factor: " + COOLING_FACTOR + " Equilibrium point: " + SA_EQUILIBRIUM_COUNT);

        Solution initialSolution = new Solution(cloner.deepClone(workflow), instanceInfo, M_NUMBER);

        //Initializes the initial solution with random values
        initialSolution.generateRandomSolution(workflow);

        temp = START_TEMP;
        bestCurrent = initialSolution;
        globalBest = bestCurrent;
        bestCurrent.fitness();

        //LOOP at a fixed temperature:
        while (temp >= FINAL_TEMP){
            for (int i = 0; i < SA_EQUILIBRIUM_COUNT; i++) {
                //GENERATES random neighbor
                Solution randomNeighbor = new Solution(cloner.deepClone(workflow), instanceInfo, M_NUMBER);
                randomNeighbor.generateRandomSolution(workflow);
                if (!visited_solutions.contains(randomNeighbor)){
                    visited_solutions.add(randomNeighbor);

                    randomNeighbor.fitness();

                    double delta = randomNeighbor.cost - bestCurrent.cost;
                    if (delta <= 0){
                        bestCurrent = randomNeighbor;
                        if((randomNeighbor.cost - globalBest.cost) <= 0){
                            globalBest = randomNeighbor;
                        }
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
            temp = temp * COOLING_FACTOR;
        }
        if(bestCurrent.cost - globalBest.cost <= 0){
            globalBest = bestCurrent;
            return globalBest;
        }else {
            return bestCurrent;
        }
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
}
