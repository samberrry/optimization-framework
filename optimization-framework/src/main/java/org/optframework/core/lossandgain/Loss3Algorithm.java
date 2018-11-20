package org.optframework.core.lossandgain;

import org.optframework.core.*;

public class Loss3Algorithm implements OptimizationAlgorithm {

    Solution heftSolution;
    int totalInstances[];
    Workflow workflow;
    InstanceInfo instanceInfo[];

    public Loss3Algorithm(Solution heftSolution, int[] totalInstances, Workflow workflow, InstanceInfo[] instanceInfo) {
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

        /**
         * This part is for calculating matrix elements
         * */
        Solution solutionTemp = null;
        Solution solution = null;
        try {
            solution = heftSolution.clone();
        } catch (Exception e) {
            Log.logger.info("Cloning Exception");
        }

        //int newXArray[] = new int[heftSolution.xArray.length];
        boolean furtherImprovement = true;
   /*     while (solution.cost > workflow.getBudget() && furtherImprovement) {

            for (int i = 0; i < workflow.getJobList().size(); i++) {
                for (int j = 0; j < totalInstances.length; j++) {
                    try {
                        solutionTemp = solution.clone();
                    } catch (Exception e) {
                        Log.logger.info("Cloning Exception");
                    }

                    double oldMakespan = solution.makespan;

                    // double tempOld1 = workflow.getJobList().get(i).getExeTime()[solutionTemp.yArray[solutionTemp.xArray[i]]];
                    // double tempOld2 = instanceInfo[solutionTemp.yArray[solution.xArray[i]]].getSpotPrice();
                    // double oldCost = tempOld1 * tempOld2 / 3600.0;
                    double oldCost = solution.cost;

                    solutionTemp.xArray[i] = j;

                    solutionTemp.heftFitness();

                    double newMakespan = solutionTemp.makespan;
                    //  double newCost = solution.cost;
                    double temp1 = workflow.getJobList().get(i).getExeTime()[solutionTemp.yArray[solutionTemp.xArray[i]]];
                    double temp2 = instanceInfo[solutionTemp.yArray[solutionTemp.xArray[i]]].getSpotPrice();
                    double newCost = temp1 * temp2 / 3600.0;

                    double costDiff = oldCost - newCost;
                    double makespanDiff = newMakespan - oldMakespan;

                    if (costDiff <= 0) {
                        matrix[i][j] = 0;
                    } else {
                        matrix[i][j] = (makespanDiff) / (costDiff);
                        if (matrix[i][j] < 0)
                            Log.logger.info("<<<<<<<<<<<    NEW RUN " + i + "     >>>>>>>>>>>\n");
                    }
                }
            }

            // Generating loss solution from the weight matrix


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

        }*/
        int test = 0;

        heftSolution.xArray = solution.xArray;

        heftSolution.heftFitness();

        return heftSolution;
    }

    public Solution runAlgorithm2() {
        double matrix[][] = new double[workflow.getJobList().size()][totalInstances.length];


         // This part is for calculating matrix elements

        Solution solutionTemp = null;
        Solution solution = null;
        try {
            solution = heftSolution.clone();
        } catch (Exception e) {
            Log.logger.info("Cloning Exception");
        }

        double sumExeTaks = 0;

      //  for(int k = 0; k < totalInstances.length; k++)
       // solution.yArray[0] = 0; // set it to the chaepest and the most efficient instance
       // solution.yArray[1] = 4; // set it to the chaepest and the most efficient instance
       // solution.yArray[2] = 4; // set it to the chaepest and the most efficient instance

        int value = 7;
        for (int i = 0; i < workflow.getJobList().size(); i++) {

            solution.xArray[i] = value;
           // value += 8;
            sumExeTaks += workflow.getJobList().get(i).getExeTime()[solution.yArray[solution.xArray[i]]];
        }
      /*  solution.xArray[0] = 2;
        solution.xArray[1] = 2;
        solution.xArray[2] = 0;
        solution.xArray[3] = 0;

        solution.xArray[4] = 0;
        solution.xArray[5] = 0;
        solution.xArray[6] = 2;
        solution.xArray[7] = 2;

        solution.xArray[8] = 1;
        solution.xArray[9] = 2;
        solution.xArray[10] = 2;
        solution.xArray[11] = 1;

        solution.xArray[12] = 1;
        solution.xArray[13] = 2;
        solution.xArray[14] = 0;
        solution.xArray[15] = 2;

        solution.xArray[16] = 0;
        solution.xArray[17] = 2;
        solution.xArray[18] = 0;
        solution.xArray[19] = 2;

        solution.xArray[20] = 2;
        solution.xArray[21] = 2;
        solution.xArray[22] = 1;
        solution.xArray[23] = 1;

        solution.xArray[24] = 0;
        solution.xArray[25] = 1;
        solution.xArray[26] = 2;
        solution.xArray[27] = 2;

        solution.xArray[28] = 1;
        solution.xArray[29] = 0;

        for (int z = 0; z < workflow.getJobList().size(); z++)
          solution.zArray[z] = z;

        solution.fitness();

        //int newXArray[] = new int[heftSolution.xArray.length];
      /*  boolean furtherImprovement = true;
        while (solution.cost > workflow.getBudget() && furtherImprovement) {

            for (int i = 0; i < workflow.getJobList().size(); i++) {
                for (int j = 0; j < totalInstances.length; j++) {
                    try {
                        solutionTemp = solution.clone();
                    } catch (Exception e) {
                        Log.logger.info("Cloning Exception");
                    }


                    double oldMakespan = solution.makespan;


                    //double tempOld1 = workflow.getJobList().get(i).getExeTime()[solution.yArray[solution.xArray[i]]];
                    //double tempOld2 = instanceInfo[solution.yArray[solution.xArray[i]]].getSpotPrice();
                    //double oldCost = tempOld1 * tempOld2 / 3600.0;
                    double oldCost = solution.cost;

                    solutionTemp.xArray[i] = j;

                    solutionTemp.heftFitness();

                    double newMakespan = solutionTemp.makespan;

                    double newCost = solutionTemp.cost;
                    //double temp1 = workflow.getJobList().get(i).getExeTime()[solutionTemp.yArray[solutionTemp.xArray[i]]];
                    //double temp2 = instanceInfo[solutionTemp.yArray[solutionTemp.xArray[i]]].getSpotPrice();
                    //double newCost = temp1 * temp2 / 3600.0;

                    double costDiff = oldCost - newCost;
                    double makespanDiff = newMakespan - oldMakespan;

                    if (costDiff <= 0) {
                        matrix[i][j] = 0;
                    } else {
                        matrix[i][j] = (makespanDiff) / (costDiff);
                        if (makespanDiff < 0)
                            matrix[i][j] *= -10; // to give a lower priority to better makespan VS better cost
                    }
                }
            }


             // Generating loss solution from the weight matrix


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
        int test = 0;*/

        heftSolution.xArray = solution.xArray;
        heftSolution.yArray = solution.yArray;

        heftSolution.heftFitness();

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

    public MatrixElement FindMinNonZeroElement(double matrix[][]) {

        double minValue = 99999999999.999;
        int minTaskId = -1;
        int minInstanceId = -1;
        for (int i = 0; i < workflow.getJobList().size(); i++) {
            for (int j = 0; j < totalInstances.length; j++)
                if (matrix[i][j] != 0 && matrix[i][j] < minValue) {
                    minValue = matrix[i][j];
                    minInstanceId = j;
                    minTaskId = i;

                }

        }

        MatrixElement NewElement = new MatrixElement(minTaskId,minInstanceId);
        return NewElement;
    }
}
