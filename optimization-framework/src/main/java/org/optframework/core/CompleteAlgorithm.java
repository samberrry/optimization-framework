package org.optframework.core;

import org.cloudbus.cloudsim.util.workload.Workflow;
import org.cloudbus.spotsim.enums.InstanceType;

/**
 * This algorithm search the entire search space of the problem. This method is suitable only
 * */

public class CompleteAlgorithm implements OptimizationAlgorithm{

    InstanceInfo instanceInfo[];

    Workflow workflow;

    Solution globalSolution;

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

        globalSolution = initialSolution;

        for (int i = 0; i < workflow.getJobList().size()+1; i++) {
            int data[] = new int[workflow.getJobList().size()];
            System.out.println("==========================" + i);
            recursiveCombinationForXArray(data , -1 , i, workflow.getJobList().size(), false);
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

            recursiveCombinationForYArray(yData, -1, InstanceType.values().length, instanceNumber , solution);

//            String str= "";
//            for (int a: data){
//                str += a;
//            }
//            if (visited){
//                System.out.println(str);
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

            if (globalSolution.cost > solution.cost){
                globalSolution = solution;
            }

//            String str= "";
//            for (int a: data){
//                str += a;
//            }
//            System.out.println(str);
        }
    }
}
