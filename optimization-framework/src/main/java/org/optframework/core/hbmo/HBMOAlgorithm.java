package org.optframework.core.hbmo;

import com.rits.cloning.Cloner;
import org.cloudbus.cloudsim.util.workload.Workflow;
import org.optframework.config.StaticProperties;
import org.optframework.core.InstanceInfo;
import org.optframework.core.OptimizationAlgorithm;
import org.optframework.core.Solution;

import java.util.ArrayList;
import java.util.List;

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

    public static List<Spermatheca> spermathecaList = new ArrayList<>();

    Cloner cloner = new Cloner();

    public HBMOAlgorithm(Workflow workflow, InstanceInfo[] instanceInfo, int generationNumber) {
        this.workflow = workflow;
        this.instanceInfo = instanceInfo;
        this.generationNumber = generationNumber;
    }

    @Override
    public Solution runAlgorithm() {

        // Queen generation
        queen = new Queen(cloner.deepClone(workflow), instanceInfo, M_NUMBER);

//        queen.localSearch(cloner.deepClone(workflow), M_NUMBER);

        for (int i = 0; i < generationNumber; i++) {

            // mating flight
            matingFlight();
            generateBrood();
//            queen.localSearch(cloner.deepClone(workflow), M_NUMBER);
        }

        return null;
    }

    void matingFlight(){
        List<Mating> matingList = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_HBMO_THREADS; i++) {
            ProblemInfo problemInfo = new ProblemInfo(instanceInfo, workflow, M_NUMBER);
            Mating mating = new Mating(i, String.valueOf(i), problemInfo, queen);
            Spermatheca spermatheca = new Spermatheca();
            spermathecaList.add(i , spermatheca);
            matingList.add(i, mating);
        }

        for (Mating mating : matingList){
            try {
                mating.thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    void generateBrood(){

    }
}
