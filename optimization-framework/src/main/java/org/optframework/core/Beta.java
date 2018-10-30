package org.optframework.core;

import org.cloudbus.spotsim.enums.InstanceType;

public class Beta {
    public static double computeBetaValue(Workflow workflow, InstanceInfo instanceInfo[], int numberOfInstances){
        //solution with best makespan
        int bestCaseTasks[] = new int[workflow.getJobList().size()];
        int bestCaseInstances[] = new int[workflow.getJobList().size()];

        for (int i = 0; i < workflow.getJobList().size(); i++) {
            bestCaseTasks[i] = i;
        }

        int bestId = findFastestInstanceId();

        for (int i = 0; i < workflow.getJobList().size(); i++) {
            bestCaseInstances[i] = bestId;
        }

        Solution solution = new Solution(workflow, instanceInfo, numberOfInstances);
        solution.numberOfUsedInstances = workflow.getJobList().size();
        solution.xArray = bestCaseTasks;
        solution.yArray = bestCaseInstances;

        solution.heftFitness();

        //solution with worst makespan
        int worstCaseTasks[] = new int[workflow.getJobList().size()];
        int worstCaseInstances[] = new int[1];

        worstCaseInstances[0] = findSlowestInstanceId();

        Solution solution2 = new Solution(workflow, instanceInfo, numberOfInstances);
        solution2.numberOfUsedInstances = 1;
        solution2.xArray = worstCaseTasks;
        solution2.yArray = worstCaseInstances;

        solution2.heftFitness();

        return 100 * (solution2.makespan - solution.makespan);
    }

    public static int findFastestInstanceId(){
        InstanceType temp = InstanceType.M1SMALL;

        for (InstanceType type: InstanceType.values()){
            if (type.getEcu() > temp.getEcu()){
                temp = type;
            }
        }
        return temp.getId();
    }

    static int findSlowestInstanceId(){
        InstanceType temp = InstanceType.M1SMALL;

        for (InstanceType type: InstanceType.values()){
            if (type.getEcu() < temp.getEcu()){
                temp = type;
            }
        }
        return temp.getId();
    }
}
