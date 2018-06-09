package org.optframework.core;

import com.rits.cloning.Cloner;
import org.cloudbus.cloudsim.util.workload.Workflow;
import org.cloudbus.spotsim.enums.InstanceType;

/**
 * This algorithm search the entire search space of the problem. This method is suitable only
 * */

public class CompleteAlgorithm implements OptimizationAlgorithm{

    InstanceInfo instanceInfo[];

    Workflow workflow;

    Solution globalSolution;

    Cloner cloner = new Cloner();

    public CompleteAlgorithm(InstanceInfo[] instanceInfo, Workflow workflow) {
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

        for (int i = 0; i < workflow.getJobList().size()+1; i++) {
            int data[] = new int[workflow.getJobList().size()];
            recursiveCombinationForXArray(data , -1 , i, workflow.getJobList().size(), false);
            System.out.println("==========================" + i);
            printSolution(globalSolution, instanceInfo);
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
//            yCounter =0;

//            System.out.println("***********************************************");
//            String str= "";
//            for (int a: data){
//                str += a;
//            }
//            if (visited){
//                System.out.println("X Array: " + str);
//            }
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

            solution.fitness();

//            String str= "";
//            String str2= "";
//            for (int a: data){
//                str += a;
//            }
//            for (int b: solution.xArray){
//                str2 +=b;
//            }
//            Log.logger.info("X Array: "+ str2+ "    Y Array: " + str + " current cost: " + solution.cost+ " global: " + globalSolution.cost);

            if (globalSolution.cost > solution.cost){
                globalSolution = cloner.deepClone(solution);
            }

//            if (globalCost > solution.cost){
//                globalCost = solution.cost;
//            }

//            yCounter++;
        }
    }

    private void printSolution(Solution solution, InstanceInfo instanceInfo[]){
        Log.logger.info("Total Cost: " + solution.getCost());
        Log.logger.info("Number of used Instances: " + solution.numberOfUsedInstances);

        for (int i = 0; i < solution.instanceTimes.length; i++) {
            Log.logger.info("Requested time for instance " + instanceInfo[i].getType().getName() + " : " + solution.instanceTimes[i]);
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
    }
}
