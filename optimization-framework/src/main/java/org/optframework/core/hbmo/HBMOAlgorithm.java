package org.optframework.core.hbmo;

import com.rits.cloning.Cloner;
import org.cloudbus.cloudsim.util.workload.Workflow;
import org.optframework.config.StaticProperties;
import org.optframework.core.InstanceInfo;
import org.optframework.core.OptimizationAlgorithm;
import org.optframework.core.Solution;

import java.util.concurrent.ForkJoinPool;

/**
 *
 * Implementation for the Honey Bee Mating Optimization Algorithm
 *
 * @author Hessam hessam.modaberi@gmail.com
 * @since 2018
 * */

public class HBMOAlgorithm implements OptimizationAlgorithm, StaticProperties {

    Queen queen;

    Workflow workflow;

    InstanceInfo instanceInfo[];

    int generationNumber;

    Cloner cloner;

    public HBMOAlgorithm(Workflow workflow, InstanceInfo[] instanceInfo, int generationNumber) {
        this.workflow = workflow;
        this.instanceInfo = instanceInfo;
        this.generationNumber = generationNumber;
    }

    @Override
    public Solution runAlgorithm() {

        // Queen generation
        queen = new Queen(cloner.deepClone(workflow), instanceInfo, M_NUMBER);

        queen.localSearch();

        for (int i = 0; i < generationNumber; i++) {

            // mating flight
            matingFlight();
            generateBrood();
            queen.localSearch();
        }

        return null;
    }

    void matingFlight(){
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        

    }

    void generateBrood(){

    }
}
