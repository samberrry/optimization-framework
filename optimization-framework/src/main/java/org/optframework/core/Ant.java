package org.optframework.core;

import org.optframework.core.parameters.Solution;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;

public class Ant extends RecursiveTask<List<Solution>>{

    int level;

    public Ant(int level) {
        this.level = level;
    }

    @Override
    protected List<Solution> compute() {
        int newLevel = level /2;
        List<Solution> solutionList = new ArrayList<>();

        if (level == 1){
            Log.logger.info("An ant is started");
            //Simulated Annealing portion
            SimulatedAnnealingAlgorithm saAlgorithm = new SimulatedAnnealingAlgorithm(2);
            solutionList.add(saAlgorithm.runSA());
            return solutionList;
        }else{
            //Use of divide and conquer strategy
            Ant childAnt1 = new Ant(newLevel);
            Ant childAnt2 = new Ant(newLevel);

            childAnt1.fork();
            childAnt2.fork();
            //waits to complete the SA algorithm
            List<Solution> childSolution1 = childAnt1.join();
            List<Solution> childSolution2 = childAnt2.join();

            if (childSolution1 != null && childSolution2 != null){
                solutionList.addAll(childSolution1);
                solutionList.addAll(childSolution2);
                return solutionList;
            }else {
                Log.logger.warning("Exception occurred because of empty solution");
                throw  new RuntimeException("Empty Solution");
            }
        }
    }
}
