package org.optframework.core.dfs;

import com.rits.cloning.Cloner;
import org.cloudbus.spotsim.enums.InstanceType;
import org.optframework.core.*;

/**
 * This algorithm search the entire search space of the problem. This method is suitable only
 * */

public class DFSAlgorithm implements OptimizationAlgorithm {

    InstanceInfo instanceInfo[];

    Workflow workflow;

    Solution globalSolution;

    Cloner cloner = new Cloner();

    public DFSAlgorithm(InstanceInfo[] instanceInfo, Workflow workflow) {
        this.instanceInfo = instanceInfo;
        this.workflow = workflow;
    }

    @Override
    public Solution runAlgorithm() {
        Solution initialSolution = new Solution(workflow, instanceInfo, 1);
        initialSolution.xArray = new int[workflow.getJobList().size()];
        initialSolution.numberOfUsedInstances = 1;
        initialSolution.yArray = new int[1];

        initialSolution.fitness();

        globalSolution = cloner.deepClone(initialSolution);

        //until workflow size is not true
//        for (int i = 1; i < workflow.getJobList().size(); i++) {
//            int data[] = new int[workflow.getJobList().size()];
//            recursiveCombinationForXArray(data , -1 , i, workflow.getJobList().size(), false);
//            System.out.println("*****************");
////            printSolution(globalSolution, instanceInfo, i);
//        }

        int xArray[] = new int[workflow.getJobList().size()];
        int yArray[] = new int[workflow.getJobList().size()];

        backTrack(xArray, yArray, -1, -1);

        return globalSolution;
    }

    /**
     * This is for X Array
     * */
    void recursiveCombinationForXArray(int data[], int level, int instanceNumber, int threshold, boolean visited){
        if (level < threshold -1){
            level++;
            for (int i = 0; i < instanceNumber; i++) {
                data[level] = i;
                if (i == instanceNumber-1){
                    visited = true;
                }
                recursiveCombinationForXArray(data, level, instanceNumber, threshold, visited);
            }
        }else {
            Solution solution = new Solution(workflow, instanceInfo, instanceNumber);
            solution.xArray = data;
            solution.numberOfUsedInstances = instanceNumber;

            int []yData = new int[instanceNumber];


//            System.out.println(yCounter);

//            System.out.println("***********************************************");

            String str= "";
            for (int a: data){
                str += a;
            }

            if (visited){
                boolean used[] = new boolean[instanceNumber];
                boolean doNotComputeY = false;
                for (int i = 0; i < data.length; i++) {
                    used[data[i]] = true;
                }
                for (int i = 0; i < used.length; i++) {
                    if (used[i] == false){
                        doNotComputeY = true;
                        break;
                    }
                }
                if (!doNotComputeY){
                    recursiveCombinationForYArray(yData, -1, InstanceType.values().length, instanceNumber, solution);
                    System.out.println("X Array: " + str);
                }
            }
        }
    }

    /**
     * This is for Y Array
     * */
    void recursiveCombinationForYArray(int data[], int level, int instanceNumber, int threshold, Solution solution){
        if (level < threshold -1){
            level++;
            for (int i = 0; i < instanceNumber; i++) {
                data[level] = i;
                recursiveCombinationForYArray(data, level, instanceNumber, threshold, solution);
            }
        }else {
            solution.yArray = data;

//            solution.fitness();

            String str= "";
            String str2= "";
            for (int a: data){
                str += a;
            }
            for (int b: solution.xArray){
                str2 +=b;
            }
            System.out.println("X Array: " + str2 + " Y Array: "+ str);


            if (globalSolution.fitnessValue > solution.fitnessValue){
                globalSolution = cloner.deepClone(solution);
            }
        }
    }

    public void printSolution(Solution solution, InstanceInfo instanceInfo[],int k){
        Log.logger.info("============================[Level] Number of used instances: " + k);

        for (int i = 0; i < solution.instanceTimes.length; i++) {
            Log.logger.info("Requested time for instance " + instanceInfo[i].getType().getName() + " : " + solution.instanceTimes[i]);
        }

        for (int i = 0; i < solution.instanceTimes.length; i++) {
            Log.logger.info("Timeline for instance " + instanceInfo[i].getType().getName() + " : " + solution.instanceTimelines[i]);
        }

        String xArray = "";
        for (int val : solution.xArray){
            xArray += " " + String.valueOf(val);
        }
        Log.logger.info("Value of the X Array: "+ xArray);

        String yArray = "";
        for (int i = 0; i < solution.numberOfUsedInstances; i++) {
            yArray += " " + String.valueOf(solution.yArray[i]);
        }
        Log.logger.info("Value of the Y Array: "+ yArray);

        Log.logger.info("Total Cost: " + solution.cost);
        Log.logger.info("Makespan: " + solution.makespan);
        Log.logger.info("(Best Solution so far) Fitness Value: "+ solution.fitnessValue);
    }

    public void backTrack(int xArray[], int yArray[], int i, int k){
        int numberOfUsedInstances = 0;
        boolean used[] = new boolean[workflow.getJobList().size()];

        for (int j = 0; j < xArray.length; j++) {
            used[xArray[j]] = true;
        }

        for (int j = 0; j < used.length; j++) {
            if (used[j]){
                numberOfUsedInstances++;
            }
        }

        if (i == workflow.getJobList().size()-1){
            //compute Y
            String xstr = "";
            for (int a : xArray){
                xstr += a;
            }
            System.out.println( "X Array" + xstr + " " + numberOfUsedInstances);
        }else{
            for (int j = 0; j <= numberOfUsedInstances; j++) {
                xArray[i+1] = j;
                backTrack(xArray, yArray, i+1, k);
            }
        }
    }
}

//            if (k == numberOfUsedInstances-1){
//                //end
//                String str1 = "";
//                String str2 = "";
//                //print x and y
//                for (int j = 0; j < xArray.length; j++) {
//                    str1 += xArray[j];
//                }
//                for (int j = 0; j < yArray.length; j++) {
//                    str2 +=yArray[j];
//                }
//                System.out.println("X Array: "+ str1 + " Y Array: "+ str2);
//
//            }else {
//                for (int j = 0; j <= numberOfUsedInstances; j++) {
//                    yArray[k+1] = j;
//                    backTrack(xArray, yArray,i , k+1);
//                }
//            }