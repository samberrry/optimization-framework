package org.optframework.core.lossandgain;

import org.optframework.core.*;

public class Loss2Algorithm implements OptimizationAlgorithm {

    Solution heftSolution;
    int totalInstances[];
    Workflow workflow;
    InstanceInfo instanceInfo[];

    public Loss2Algorithm(Solution heftSolution, int[] totalInstances, Workflow workflow, InstanceInfo[] instanceInfo) {
        try {
            this.heftSolution = heftSolution.clone();
        }catch (Exception e){
            Log.logger.info("Clone Exception");
        }
        this.totalInstances = totalInstances;
        this.workflow = workflow;
        this.instanceInfo = instanceInfo;
    }

    @Override
    public Solution runAlgorithm() {
        double matrix[][] = new double[totalInstances.length][workflow.getJobList().size()];

        /**
         * This part is for calculating matrix elements
         * */
        for (int i = 0; i < workflow.getJobList().size(); i++) {
            for (int j = 0; j < totalInstances.length; j++) {
                Solution solution = null;
                try {
                    solution = heftSolution.clone();
                }catch (Exception e){
                    Log.logger.info("Cloning Exception");
                }

                double oldMakespan = solution.makespan;
                double oldCost = solution.cost;

                solution.xArray[i] = j;

                solution.heftFitness();

                double newMakespan = solution.makespan;
                double newCost = solution.cost;

                double costDiff = oldCost-newCost;
                double makespanDiff = newMakespan - oldMakespan;

                if (costDiff <= 0){
                    matrix[j][i] = 0;
                }else{
                    matrix[j][i] = (makespanDiff) / (costDiff);
                }
            }
        }

        /**
         * Generating loss solution from matrix
         * */
        int newXArray[] = new int[heftSolution.xArray.length];

        for (int i = 0; i < workflow.getJobList().size(); i++) {
            int oldInstanceId = heftSolution.xArray[i];
            double minValue = 99999999999.999;
            int minInstanceId = -1;
            for (int j = 0; j < totalInstances.length; j++) {
                if (matrix[j][i] < minValue){
                    if (matrix[j][i] != 0){
                        minValue = matrix[j][i];
                        minInstanceId = j;
                    }
                }
            }
            if (minInstanceId != -1){
                newXArray[i] = minInstanceId;
            }else {
                newXArray[i] = oldInstanceId;
            }
        }

        heftSolution.xArray = newXArray;

        heftSolution.heftFitness();

        return heftSolution;
    }
}
