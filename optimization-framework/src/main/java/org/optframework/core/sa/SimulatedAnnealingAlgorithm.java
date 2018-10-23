package org.optframework.core.sa;

import com.rits.cloning.Cloner;
import org.optframework.config.Config;
import org.optframework.core.*;

import java.util.*;

/**
 * @author Hessam Modabberi hessam.modaberi@gmail.com
 * @version 1.0.0
 */
public class SimulatedAnnealingAlgorithm implements OptimizationAlgorithm {

    Solution initialSolution;

    double temp;

    Set<Solution> visited_solutions = new HashSet<>();

    Solution bestCurrent;

    Solution globalBest;

    Workflow workflow;

    InstanceInfo instanceInfo[];

    Cloner cloner = new Cloner();

    long counter = 0;

    public SimulatedAnnealingAlgorithm(Solution initialSolution, Workflow workflow, InstanceInfo[] instanceInfo) {
        this.initialSolution = initialSolution;
        this.workflow = workflow;
        this.instanceInfo = instanceInfo;
    }

    @Override
    public Solution runAlgorithm(){
        if (initialSolution == null){
            initialSolution = new Solution(workflow, instanceInfo, Config.global.m_number);
            //Initializes the initial solution with random values
            initialSolution.generateRandomSolution(workflow);
        }
        initialSolution.fitness();

        temp = Config.sa_algorithm.start_temperature;
        bestCurrent = initialSolution;
        globalBest = cloner.deepClone(bestCurrent);

        //LOOPs at a fixed temperature:
        while (temp >= Config.sa_algorithm.final_temperature){
            for (int i = 0; i < Config.sa_algorithm.equilibrium_point; i++) {
                counter++;
                //GENERATES random neighbor
                Solution randomNeighbor = cloner.deepClone(bestCurrent);

                //Generates a random neighbor solution
                randomNeighbor.generateRandomNeighborSolution(workflow);

                randomNeighbor.fitness();

                double delta = randomNeighbor.fitnessValue - bestCurrent.fitnessValue;
                if (delta <= 0){
                    bestCurrent = randomNeighbor;
                    if((randomNeighbor.fitnessValue - globalBest.fitnessValue) <= 0){
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
            }
            temp = temp * Config.sa_algorithm.cooling_factor;
        }
        if(bestCurrent.fitnessValue - globalBest.fitnessValue <= 0){
            globalBest = bestCurrent;
            return globalBest;
        }else {
            return bestCurrent;
        }
    }

    public Solution runAlgorithWithRandomSolution(){
        Log.logger.info("Starts SA Algorithm");
        Log.logger.info("Simulated Annealing parameters Initial temp: "+ Config.sa_algorithm.start_temperature+ " Final temp: " + Config.sa_algorithm.final_temperature + " Cooling Factor: " + Config.sa_algorithm.cooling_factor + " Equilibrium point: " + Config.sa_algorithm.equilibrium_point);

        Solution initialSolution = new Solution(workflow, instanceInfo, Config.global.m_number);

        //Initializes the initial solution with random values
        initialSolution.generateRandomSolution(workflow);

        temp = Config.sa_algorithm.start_temperature;
        bestCurrent = initialSolution;
        globalBest = bestCurrent;
        bestCurrent.fitness();

        //LOOPs at a fixed temperature:
        while (temp >= Config.sa_algorithm.final_temperature){
            for (int i = 0; i < Config.sa_algorithm.equilibrium_point; i++) {
                //GENERATES random neighbor
                Solution randomNeighbor = new Solution(workflow, instanceInfo, Config.global.m_number);
                randomNeighbor.generateRandomSolution(workflow);
                randomNeighbor.fitness();

                double delta = randomNeighbor.fitnessValue - bestCurrent.fitnessValue;
                if (delta <= 0){
                    bestCurrent = randomNeighbor;
                    if((randomNeighbor.fitnessValue - globalBest.fitnessValue) <= 0){
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
            }
            temp = temp * Config.sa_algorithm.cooling_factor;
        }
        if(bestCurrent.fitnessValue - globalBest.fitnessValue <= 0){
            globalBest = bestCurrent;
            return globalBest;
        }else {
            return bestCurrent;
        }
    }

    double bolzmanDist(double delta, double temp){
        return Math.exp(-(Math.abs(delta))/temp);
    }

    public Workflow getWorkflow() {
        return workflow;
    }

    public void setWorkflow(Workflow workflow) {
        this.workflow = workflow;
    }
}
