package org.optframework.core.hbmo;

import org.optframework.core.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;

public class Mating extends RecursiveTask<List<Chromosome>> {

    int level;

    public Mating(int level) {
        this.level = level;
    }

    @Override
    protected List<Chromosome> compute() {
        int newLevel = level/2;
        List<Chromosome> chromosomeList = new ArrayList<>();

        if (level == 1){
            Log.logger.info("A Mating is started");

            // SIMULATED ANNEALING

            return chromosomeList;
        }else {
            Mating childMating1 = new Mating(newLevel);
            Mating childMating2 = new Mating(newLevel);

            childMating1.fork();
            childMating2.fork();

            List<Chromosome> childChromosome1 = childMating1.join();
            List<Chromosome> childChromosome2 = childMating2.join();

            if (childChromosome1 != null && childChromosome2 != null){
                chromosomeList.addAll(childChromosome1);
                chromosomeList.addAll(childChromosome2);
                return chromosomeList;
            }else {
                Log.logger.warning("Exception occurred because of empty solution");
                throw  new RuntimeException("Empty Solution");
            }

        }
    }
}
