package org.optframework.core.hbmo;

import com.rits.cloning.Cloner;
import org.optframework.config.Config;
import org.optframework.config.StaticProperties;
import org.optframework.core.Log;

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

        double SMax;
        double Smin;

        if (Config.honeybee_algorithm.getForce_speed()){
            SMax = Config.honeybee_algorithm.getMax_speed();
            Smin = Config.honeybee_algorithm.getMin_speed();
        }else {
            final double beta = 0.6 + 0.3 * r.nextDouble();
            SMax = Math.abs((queen.chromosome.fitnessValue - drone.chromosome.fitnessValue) / Math.log(beta));

            Smin = Math.abs((queen.chromosome.fitnessValue - drone.chromosome.fitnessValue) / Math.log(0.05));

            SMax /= 10;
            Smin /= 10;
        }

        int threadSpmSize = Config.honeybee_algorithm.getSpermatheca_size() / Config.honeybee_algorithm.getNumber_of_threads();

        double queenSpeed = SMax;

        while (queenSpeed > Smin && HBMOAlgorithm.spermathecaList.get(id).chromosomeList.size() < threadSpmSize){
            if (probability(queen.chromosome.fitnessValue, drone.chromosome.fitnessValue, queenSpeed) > r.nextDouble()){
                Chromosome brood = HBMOAlgorithm.crossOver(queen.chromosome, drone.chromosome);

                long start = System.currentTimeMillis();
                brood = HBMOAlgorithm.lightLocalSearch(brood,100);
                long stop = System.currentTimeMillis();
                Log.logger.info("brood local search: "+ (stop - start));

                HBMOAlgorithm.spermathecaList.get(id).chromosomeList.add(cloner.deepClone(brood));
            }
            queenSpeed = Config.honeybee_algorithm.getCooling_factor() * queenSpeed;

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
