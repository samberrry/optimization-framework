package org.optframework.core.hbmo;

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
        Drone drone = new Drone(problemInfo.workflow, problemInfo.instanceInfo, problemInfo.numberOfInstances);

        Random r = new Random();
        final double beta = 0.6 + 0.3 * r.nextDouble();
        int threadSpmSize = SPERMATHECA_SIZE / NUMBER_OF_HBMO_THREADS;

        double SMax = Math.abs((queen.chromosome.fitnessValue - drone.chromosome.fitnessValue) / Math.log(beta));

        double Smin = Math.abs((queen.chromosome.fitnessValue - drone.chromosome.fitnessValue) / Math.log(0.05));

        SMax /= 10;
        Smin /= 10;

        double queenSpeed = SMax;

        while (queenSpeed > Smin && HBMOAlgorithm.spermathecaList.get(id).chromosomeList.size() < threadSpmSize){
            if (probability(queen.chromosome.fitnessValue, drone.chromosome.fitnessValue, queenSpeed) > r.nextDouble()){
                HBMOAlgorithm.spermathecaList.get(id).chromosomeList.add(drone.chromosome);
            }
            queenSpeed = 0.999 * queenSpeed;
            drone.chromosome.generateRandomSolution(problemInfo.workflow);
        }
    }

    double probability(double queenFitness, double droneFitness, double queenSpeed){

        if(droneFitness > queenFitness)
            return 1;
        else
            return Math.exp((droneFitness - queenFitness) / queenSpeed);
    }
}
