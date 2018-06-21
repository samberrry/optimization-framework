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

    int yCounter=0;

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
        for (int i = 1; i < workflow.getJobList().size()+1; i++) {
            int data[] = new int[workflow.getJobList().size()];
            recursiveCombinationForXArray(data , -1 , i, workflow.getJobList().size(), false);
            System.out.println("*****************");
//            printSolution(globalSolution, instanceInfo, i);
        }

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

            recursiveCombinationForYArray(yData, -1, InstanceType.values().length, instanceNumber, solution);

//            System.out.println(yCounter);
            yCounter++;

//            System.out.println("***********************************************");
            String str= "";
            for (int a: data){
                str += a;
            }
            if (visited){
                System.out.println("X Array: " + str);
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

//            String str= "";
//            String str2= "";
//            for (int a: data){
//                str += a;
//            }
//            for (int b: solution.xArray){
//                str2 +=b;
//            }
//            Log.logger.info("X Array: "+ str2+ "    Y Array: " + str + " current cost: " + solution.cost+ " global: " + globalSolution.cost);

            if (globalSolution.fitnessValue > solution.fitnessValue){
                globalSolution = cloner.deepClone(solution);
            }

//            if (globalCost > solution.cost){
//                globalCost = solution.cost;
//            }

//            yCounter++;
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

    void backTrack(int xArray[], int yArray[], int i, int k){
        int numberOfUsedInstances = 0;
        for (int j = 0; j < i; j++) {
            if (xArray[i] > numberOfUsedInstances){
                numberOfUsedInstances = xArray[i];
            }
        }

        if (i != xArray.length){
            for (int j = 0; j < xArray.length; j++) {



                backTrack(xArray,yArray, i+1, k);
            }
        }else {
            if (k != numberOfUsedInstances){
                for (int j = 0; j < numberOfUsedInstances; j++) {

                }
            }
            // computer fitness
        }
    }
}
