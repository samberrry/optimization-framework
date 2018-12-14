package org.optframework.core.lossandgain;

import org.cloudbus.spotsim.enums.InstanceType;
import org.optframework.config.Config;
import org.optframework.core.*;
import org.optframework.core.utils.Printer;

public class Loss2Algorithm implements OptimizationAlgorithm {

    Solution heftSolution;
    int totalInstances[];
    Workflow workflow;
    InstanceInfo instanceInfo[];

    public Loss2Algorithm(Solution heftSolution, int[] totalInstances, Workflow workflow, InstanceInfo[] instanceInfo) {
        try {
            this.heftSolution = heftSolution.clone();
        } catch (Exception e) {
            Log.logger.info("Clone Exception");
        }
        this.totalInstances = totalInstances;
        this.workflow = workflow;
        this.instanceInfo = instanceInfo;
    }

    @Override
    public Solution runAlgorithm() {
        double matrix[][] = new double[workflow.getJobList().size()][totalInstances.length];

        double sumExeTaks = 0;

        //  for(int k = 0; k < totalInstances.length; k++)
        // solution.yArray[0] = 0; // set it to the chaepest and the most efficient instance
        // solution.yArray[1] = 4; // set it to the chaepest and the most efficient instance
        // solution.yArray[2] = 4; // set it to the chaepest and the most efficient instance

        Solution solutionTemp = null;
        Solution solution = null;
        try {
            solution = heftSolution.clone();
        } catch (Exception e) {
            Log.logger.info("Cloning Exception");
        }

      // This finds the possible solution only using one instance
      /*  if(!Config.global.deadline_based) {
            int best_instances = -1;
            double best_fintenss_value = 9999999999.99;
            for (int j = 0; j < instanceInfo.length; j++) {
                for (int i = 0; i < workflow.getJobList().size(); i++) {

                    solution.xArray[i] = j;

                }
                solution.heftFitness();
                if (solution.fitnessValue < best_fintenss_value) {
                    best_instances = j;
                    best_fintenss_value = solution.fitnessValue;
                }
            }

            for (int i = 0; i < workflow.getJobList().size(); i++) {

                solution.xArray[i] = best_instances;

            }
            heftSolution.xArray = solution.xArray;
        }*/





         // This part is for calculating matrix elements

    /*    Solution solution = null;
        for (int i = 0; i < workflow.getJobList().size(); i++) {
            for (int j = 0; j < totalInstances.length; j++) {
                try {
                    solution = heftSolution.clone();
                } catch (Exception e) {
                    Log.logger.info("Cloning Exception");
                }

                double oldMakespan = solution.makespan;
                double oldCost = solution.cost;

                solution.xArray[i] = j;

                solution.heftFitness();

                double newMakespan = solution.makespan;
                //  double newCost = solution.cost;
                double temp1 = workflow.getJobList().get(i).getExeTime()[solution.yArray[solution.xArray[i]]];
                double temp2 = instanceInfo[solution.yArray[solution.xArray[i]]].getSpotPrice();
                double newCost = temp1 * temp2 / 3600.0;

                double costDiff = oldCost - newCost;
                double makespanDiff = newMakespan - oldMakespan;

                if (costDiff <= 0) {
                    matrix[i][j] = 0;
                } else {
                    matrix[i][j] = (makespanDiff) / (costDiff);
                }
            }
        }


         // Generating loss solution from the weight matrix

        try {
            solution = heftSolution.clone();
        } catch (Exception e) {
            Log.logger.info("Cloning Exception");
        }

        //int newXArray[] = new int[heftSolution.xArray.length];
        boolean furtherImprovement = true;
        while (solution.cost > workflow.getBudget() && furtherImprovement) {
            //int oldInstanceId = heftSolution.xArray[i];
            // int minInstanceId,minTaskId;
            MatrixElement MinElement = FindMinPositiveElement(matrix);
            if (MinElement.j == -1)
                furtherImprovement = false;
            else {
                solution.xArray[MinElement.i] = MinElement.j;
                matrix[MinElement.i][MinElement.j] = 0;
                solution.heftFitness();
            }

        }
        int test = 0;
*/


        heftSolution.heftFitness();
        heftSolution.origin = "Loss2";
        Log.logger.info("Loss2 Solution Fitness is: "+ heftSolution.fitnessValue + ", Cost=" + heftSolution.cost);

        return heftSolution;
    }

    public MatrixElement FindMinPositiveElement(double matrix[][]) {

        double minValue = 99999999999.999;
        int minTaskId = -1;
        int minInstanceId = -1;
        for (int i = 0; i < workflow.getJobList().size(); i++) {
            for (int j = 0; j < totalInstances.length; j++)
                if (matrix[i][j] > 0 && matrix[i][j] < minValue) {
                    minValue = matrix[i][j];
                    minInstanceId = j;
                    minTaskId = i;

                }

        }

        MatrixElement NewElement = new MatrixElement(minTaskId,minInstanceId);
        return NewElement;
    }
}
