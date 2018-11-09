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
                double oldMakespan = heftSolution.makespan;
                double oldCost = heftSolution.cost;
                int tempX = heftSolution.xArray[i];

                heftSolution.xArray[i] = j;

                heftSolution.fitness();


                double newMakespan = heftSolution.makespan;
                double newCost = heftSolution.cost;

                matrix[j][i] = (newMakespan - oldMakespan) / (oldCost - newCost);

                heftSolution.xArray[i] = tempX;
            }
        }

        /**
         * Generating loss solution from matrix
         * */
        int newXArray[] = new int[heftSolution.xArray.length];

        for (int i = 0; i < workflow.getJobList().size(); i++) {
            double minValue = 99999999999.999;
            int minInstanceId = -1;
            for (int j = 0; j < totalInstances.length; j++) {
                if (matrix[j][i] < minValue){
                    minValue = matrix[j][i];
                    minInstanceId = j;
                }
            }
            newXArray[i] = minInstanceId;
        }

        heftSolution.xArray = newXArray;

        heftSolution.fitness();

        return heftSolution;
    }
}
