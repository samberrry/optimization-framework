package org.optframework.core.hbmo;

import com.rits.cloning.Cloner;
import org.optframework.config.Config;
import org.optframework.config.StaticProperties;

import java.util.Random;

public class Mating implements Runnable, StaticProperties {

    int id;

    Thread thread;

    ProblemInfo problemInfo;

    Queen queen;

    public Mating(int id, String name, ProblemInfo problemInfo, Queen queen) {
        this.id = id;
        this.queen = queen;
        this.problemInfo = problemInfo;
        thread = new Thread(this, name);
        thread.start();
    }

    @Override
    public void run() {
        Cloner cloner = new Cloner();
        Random r = new Random();

        //This constructor also generates the random solution
        Drone drone = new Drone(problemInfo.workflow, problemInfo.instanceInfo, problemInfo.numberOfInstances);

        int threadSpmSize = SPERMATHECA_SIZE / NUMBER_OF_HBMO_THREADS;

        double queenSpeed = Config.honeybee_algorithm.getMax_speed();

        while (queenSpeed > Config.honeybee_algorithm.getMin_speed() && HBMOAlgorithm.spermathecaList.get(id).chromosomeList.size() < threadSpmSize){
            if (probability(queen.chromosome.fitnessValue, drone.chromosome.fitnessValue, queenSpeed) > r.nextDouble()){
                HBMOAlgorithm.spermathecaList.get(id).chromosomeList.add(cloner.deepClone(drone.chromosome));
            }
            queenSpeed = 0.999 * queenSpeed;

            drone = new Drone(problemInfo.workflow, problemInfo.instanceInfo, problemInfo.numberOfInstances);

            HBMOAlgorithm.globalCounter++;
        }
    }

    double probability(double queenFitness, double droneFitness, double queenSpeed){

        if(droneFitness > queenFitness)
            return 1;
        else
            return Math.exp((droneFitness - queenFitness) / queenSpeed);
    }
}
