package org.optframework.core;

public class Beta {
    public static double computeBetaValue(Workflow workflow, InstanceInfo instanceInfo[], int numberOfInstances){
        //solution with best makespan
        int bestCaseTasks[] = new int[workflow.getJobList().size()];
        int bestCaseInstances[] = new int[workflow.getJobList().size()];

        for (int i = 0; i < workflow.getJobList().size(); i++) {
            bestCaseTasks[i] = i;
        }

        int bestId = findFastestInstanceId(instanceInfo);

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

        worstCaseInstances[0] = findSlowestInstanceId(instanceInfo);

        Solution solution2 = new Solution(workflow, instanceInfo, numberOfInstances);
        solution2.numberOfUsedInstances = 1;
        solution2.xArray = worstCaseTasks;
        solution2.yArray = worstCaseInstances;

        solution2.heftFitness();

        return 100 * (solution2.makespan - solution.makespan);
    }

    public static int findFastestInstanceId(InstanceInfo instanceInfo[]){
        InstanceInfo temp = instanceInfo[0];
        for (InstanceInfo info: instanceInfo){
            if (info.type.getEcu() > temp.type.getEcu()){
                temp = info;
            }
        }
        return  temp.type.getId();
    }

    static int findSlowestInstanceId(InstanceInfo instanceInfo[]){
        InstanceInfo temp = instanceInfo[0];
        for (InstanceInfo info: instanceInfo){
            if (info.type.getEcu() < temp.type.getEcu()){
                temp = info;
            }
        }
        return  temp.type.getId();
    }
}
