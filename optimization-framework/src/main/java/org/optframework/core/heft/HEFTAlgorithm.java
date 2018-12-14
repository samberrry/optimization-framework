package org.optframework.core.heft;

import com.rits.cloning.Cloner;
import org.cloudbus.cloudsim.util.workload.WorkflowDAG;
import org.cloudbus.spotsim.enums.InstanceType;
import org.optframework.GlobalAccess;
import org.optframework.config.Config;
import org.optframework.core.*;
import org.optframework.core.utils.TaskUtility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HEFTAlgorithm implements OptimizationAlgorithm {

    Workflow workflow;

    InstanceInfo instanceInfo[];

    List<Job> orderedJobList;

    List<Job> originalJobList;

    Cloner cloner = new Cloner();

    int xArray[];

    double taskFinishTimes[];

    //this array is the y array from the hbmo algorithm with specified size
    int availableInstances[];

    int maxNumberOfUsedInstancesForIntegrationWithOtherSolutions;

    public HEFTAlgorithm(Workflow workflow, InstanceInfo[] instanceInfo, int availableInstances[]) {
        this.workflow = workflow;
        this.instanceInfo = instanceInfo;
        this.availableInstances = availableInstances;
    }

    public HEFTAlgorithm(Workflow workflow, InstanceInfo[] instanceInfo) {
        this.workflow = workflow;
        this.instanceInfo = instanceInfo;
    }

    public HEFTAlgorithm(Workflow workflow, InstanceInfo[] instanceInfo, int[] availableInstances, int maxNumberOfUsedInstancesForIntegrationWithOtherSolutions) {
        this.workflow = workflow;
        this.instanceInfo = instanceInfo;
        this.availableInstances = availableInstances;
        this.maxNumberOfUsedInstancesForIntegrationWithOtherSolutions = maxNumberOfUsedInstancesForIntegrationWithOtherSolutions;
    }

    @Override
    public Solution runAlgorithm() {
        taskFinishTimes = new double[workflow.getJobList().size()];

        originalJobList = workflow.getJobList();
        orderedJobList = GlobalAccess.orderedJobList;
        WorkflowDAG dag = workflow.getWfDAG();

        double instanceTimeLine[] = new double[availableInstances.length];
        boolean instanceUsed[] = new boolean[availableInstances.length];

        xArray = new int[orderedJobList.size()];
        int yArray[] = new int[availableInstances.length];
        for (int i = 0; i < availableInstances.length; i++) {
            yArray[i] = -1;
        }

        Instance instanceList[] = new Instance[availableInstances.length];
        for (int i = 0; i < availableInstances.length; i++) {
            instanceList[i] = new Instance();
        }

        Job firstJob = orderedJobList.get(0);
        Job originalVersion = originalJobList.get(firstJob.getIntId());
        double temp = TaskUtility.executionTimeOnTypeWithCustomJob(firstJob, instanceInfo[availableInstances[0]].getType());
        int tempInstance = 0;

        //for the first task
        for (int i = 1; i < availableInstances.length; i++) {
            double exeTime = TaskUtility.executionTimeOnTypeWithCustomJob(firstJob, instanceInfo[availableInstances[i]].getType());
            if (exeTime < temp){
                temp = exeTime;
                tempInstance = i;
            }
        }

        xArray[firstJob.getIntId()] = tempInstance;
        yArray[tempInstance] = availableInstances[tempInstance];
        instanceTimeLine[tempInstance] = temp;
        instanceUsed[tempInstance] = true;

        taskFinishTimes[originalVersion.getIntId()] = instanceTimeLine[tempInstance];

        //for the rest of tasks
        for (int i = 1; i < orderedJobList.size(); i++) {
            Job job = orderedJobList.get(i);
            double tempTaskFinishTime = 999999999999999999.0;
            int tempInstanceId = -1;
            boolean gapOccurred = false;
            double endOfInstanceWaitTime = -99999999999999999.9;

            //Info about the gap and the instance
            boolean gapIsUsed = false;
            int instanceGapId = -1;
            int gapId = -1;
            //

            ArrayList<Integer> parentJobs = dag.getParents(job.getIntId());

            //it is possible to have multiple start tasks without dependencies
            for (int j = 0; j < availableInstances.length; j++) {
                int maxParentId = getJobWithMaxParentFinishTimeWithCij(parentJobs, job.getIntId(), j);
                Job maxParentJob;
                double latestParentFinishTime = 0.0;

                if (parentJobs.size() != 0){
                    maxParentJob = originalJobList.get(maxParentId);
                    latestParentFinishTime = taskFinishTimes[maxParentJob.getIntId()];
                }

                if (instanceList[j].gapList.size() > 0){
                    Collections.sort(instanceList[j].gapList , Gap.gapComparator);
                }

                if (instanceList[j].gapList.size() >= 1 && parentJobs.size() != 0){
                    int k =0;
                    for (Gap gap: instanceList[j].gapList){
                        if (latestParentFinishTime < gap.endTime){
                            double tempEdge = Math.abs(originalJobList.get(maxParentId).getEdge(job.getIntId()));
                            double tempCIJ = tempEdge / (double)Config.global.bandwidth;
                            double taskExeTime;

                            if (j == xArray[maxParentId]){
                                taskExeTime = TaskUtility.executionTimeOnTypeWithCustomJob(job, instanceInfo[availableInstances[j]].getType());
                            }else {
                                taskExeTime = TaskUtility.executionTimeOnTypeWithCustomJob(job, instanceInfo[availableInstances[j]].getType()) + tempCIJ;
                            }

                            double availableGapTime = gap.endTime - latestParentFinishTime;
                            double gapTest = availableGapTime;
                            if (availableGapTime > gap.duration){
                                gapTest = gap.duration;
                            }

                            if (gapTest >= taskExeTime){
                                double remainingTimeToStartGap = gap.startTime - latestParentFinishTime;
                                double gapTaskFinishTime;

                                if (remainingTimeToStartGap >= 0){
                                    double timeToSendData = gap.startTime - latestParentFinishTime;
                                    if (timeToSendData >= tempCIJ){
                                        gapTaskFinishTime = gap.startTime + (taskExeTime - tempCIJ);
                                    }else {
                                        gapTaskFinishTime = gap.startTime + (taskExeTime - timeToSendData);
                                    }
                                }else {
                                    gapTaskFinishTime = latestParentFinishTime + taskExeTime;
                                }

                                if (gapTaskFinishTime < tempTaskFinishTime){
                                    tempTaskFinishTime = gapTaskFinishTime;
                                    tempInstanceId = j;
                                    gapIsUsed = true;
                                    gapOccurred = false;
                                    instanceGapId = j;
                                    gapId = k;
                                }
                                break;
                            }
                            k++;
                        }else {
                            break;
                        }
                    }
                }
                if (parentJobs.size() == 0){
                    double currentFinishTime = instanceTimeLine[j] + TaskUtility.executionTimeOnTypeWithCustomJob(job, instanceInfo[availableInstances[j]].getType());

                    if (currentFinishTime < tempTaskFinishTime){
                        gapIsUsed = false;
                        tempTaskFinishTime = currentFinishTime;
                        tempInstanceId = j;
                    }
                }else {
                    //check minimum task finish time for all of the current instances
                    double waitingTime = taskFinishTimes[maxParentId] - instanceTimeLine[j];

                    if (waitingTime > 0 ){
                        double currentTime = instanceTimeLine[j] + waitingTime;
                        double cij = 0.0;
                        double currentFinishTime;

                        if (j == xArray[maxParentId]){
                            currentFinishTime = currentTime + TaskUtility.executionTimeOnTypeWithCustomJob(job, instanceInfo[availableInstances[j]].getType());
                        }else {
                            double edge = Math.abs(originalJobList.get(maxParentId).getEdge(job.getIntId()));
                            cij = edge / (double)Config.global.bandwidth;
                            double timeToSendData = currentTime - taskFinishTimes[maxParentId];
                            if (timeToSendData >= cij){
                                currentFinishTime = currentTime + TaskUtility.executionTimeOnTypeWithCustomJob(job, instanceInfo[availableInstances[j]].getType());
                            }else {
                                currentFinishTime = currentTime + (cij - timeToSendData) + TaskUtility.executionTimeOnTypeWithCustomJob(job, instanceInfo[availableInstances[j]].getType());
                            }
                        }

                        if (currentFinishTime < tempTaskFinishTime){
                            gapOccurred = true;
                            endOfInstanceWaitTime = currentTime;
                            tempTaskFinishTime = currentFinishTime;
                            tempInstanceId = j;
                            gapIsUsed = false;
                        }
                    }else {
                        double edge = Math.abs(originalJobList.get(maxParentId).getEdge(job.getIntId()));
                        double cij = edge / (double)Config.global.bandwidth;
                        double currentFinishTime;

                        if (j == xArray[maxParentId]){
                            currentFinishTime = instanceTimeLine[j] + TaskUtility.executionTimeOnTypeWithCustomJob(job, instanceInfo[availableInstances[j]].getType());
                        }else {
                            double timeToSendData = instanceTimeLine[j] - taskFinishTimes[maxParentId];

                            if (timeToSendData >= cij){
                                currentFinishTime = instanceTimeLine[j] + TaskUtility.executionTimeOnTypeWithCustomJob(job, instanceInfo[availableInstances[j]].getType());
                            }else {
                                currentFinishTime = instanceTimeLine[j] + (cij - timeToSendData) + TaskUtility.executionTimeOnTypeWithCustomJob(job, instanceInfo[availableInstances[j]].getType());
                            }
                        }

                        if (currentFinishTime < tempTaskFinishTime){
                            tempTaskFinishTime = currentFinishTime;
                            tempInstanceId = j;
                            gapOccurred = false;
                            gapIsUsed = false;
                        }
                    }
                }
            }
            if (gapOccurred && instanceUsed[tempInstanceId]){
                instanceList[tempInstanceId].hasGap = true;
                Gap gap = new Gap(instanceTimeLine[tempInstanceId], endOfInstanceWaitTime);
                instanceList[tempInstanceId].gapList.add(gap);
            }
            instanceUsed[tempInstanceId] = true;

            if (gapIsUsed){
                Gap gap = instanceList[instanceGapId].getGapList().get(gapId);
                gap.startTime = tempTaskFinishTime;
                if (gap.startTime >= gap.endTime){
                    instanceList[instanceGapId].gapList.remove(gapId);
                    Collections.sort(instanceList[instanceGapId].gapList , Gap.gapComparator);
                }else {
                    gap.duration = gap.endTime - gap.startTime;
                }
            }else {
                instanceTimeLine[tempInstanceId] = tempTaskFinishTime;
            }
            taskFinishTimes[job.getIntId()] = tempTaskFinishTime;
            xArray[job.getIntId()] = tempInstanceId;
            yArray[tempInstanceId] = availableInstances[tempInstanceId];
        }

        double maxTime = instanceTimeLine[0];
        for (double time : instanceTimeLine){
            if (time > maxTime){
                maxTime = time;
            }
        }
        Log.logger.info("Makespan from HEFT: "+ (int)maxTime);

        int numberOfUsedInstances =0;
        for (int i = 0; i < yArray.length; i++) {
            if (yArray[i] != -1){
                numberOfUsedInstances++;
            }
        }

        Solution solution;

        if (maxNumberOfUsedInstancesForIntegrationWithOtherSolutions > 0){
            solution = new Solution(workflow, instanceInfo, maxNumberOfUsedInstancesForIntegrationWithOtherSolutions);
            solution.numberOfUsedInstances = availableInstances.length;
            solution.xArray = xArray;

            int newYArray[] = new int[maxNumberOfUsedInstancesForIntegrationWithOtherSolutions];
            for (int i = 0; i < maxNumberOfUsedInstancesForIntegrationWithOtherSolutions; i++) {
                newYArray[i] = -1;
            }
            for (int i = 0; i < availableInstances.length; i++) {
                newYArray[i] = availableInstances[i];
            }
            solution.yArray = newYArray;

            solution.heftFitness();
        }else {
            solution = new Solution(workflow, instanceInfo, availableInstances.length);
            solution.numberOfUsedInstances = availableInstances.length;
            solution.xArray = xArray;
            solution.yArray = availableInstances;
            solution.heftFitness();
        }
        solution.origin = "heft";

        return solution;
    }



    int getJobWithMaxParentFinishTimeWithCij(ArrayList<Integer> parentJobs, int jobId, int assignedInstanceId){
        double tempValue = -1;
        int tempId = -1;

        for (int parentId : parentJobs){
            double tempEdge = Math.abs(originalJobList.get(parentId).getEdge(jobId));
            double tempCIJ = tempEdge / (double)Config.global.bandwidth;
            double maxJobStartTime;
            if (assignedInstanceId == xArray[parentId]){
                maxJobStartTime = taskFinishTimes[parentId];
            }else {
                maxJobStartTime = taskFinishTimes[parentId] + tempCIJ;
            }

            if (tempValue < maxJobStartTime){
                tempValue = maxJobStartTime;
                tempId = originalJobList.get(parentId).getIntId();
            }
        }
        return tempId;
    }

    public static int[] getTotalInstancesForHEFT(int numberOfInstances){
        int totalInstances[] = new int[numberOfInstances];
        for (int i = 0; i < numberOfInstances;) {
            for (InstanceType type : InstanceType.values()){
                totalInstances[i] = type.getId();
                i++;
                if (i >= numberOfInstances)
                    break;
            }
        }

        return totalInstances;
    }

    public static int[] getTotalInstancesForHEFTMostPowerful(int numberOfInstances){
        int maxECUId = -1;
        double maxECU = 0.0;

        for (InstanceType type : InstanceType.values()){
            if (type.getEcu() > maxECU){
                maxECUId = type.getId();
                maxECU = type.getEcu();
            }
        }

        int totalInstances[] = new int[numberOfInstances];
        for (int i = 0; i < numberOfInstances;i++) {
            totalInstances[i] = maxECUId;
        }

        return totalInstances;
    }




    public Solution runAlgorithm_With_Hour_limitation() {
        taskFinishTimes = new double[workflow.getJobList().size()];

        originalJobList = workflow.getJobList();
        orderedJobList = GlobalAccess.orderedJobList;
        WorkflowDAG dag = workflow.getWfDAG();

        double instanceTimeLine[] = new double[availableInstances.length];
        boolean instanceUsed[] = new boolean[availableInstances.length];

        xArray = new int[orderedJobList.size()];
        int yArray[] = new int[availableInstances.length];
        for (int i = 0; i < availableInstances.length; i++) {
            yArray[i] = -1;
        }

        Instance instanceList[] = new Instance[availableInstances.length];
        for (int i = 0; i < availableInstances.length; i++) {
            instanceList[i] = new Instance();
        }

        Job firstJob = orderedJobList.get(0);
        Job originalVersion = originalJobList.get(firstJob.getIntId());
        double temp = TaskUtility.executionTimeOnTypeWithCustomJob(firstJob, instanceInfo[availableInstances[0]].getType());
        int tempInstance = 0;

        //for the first task
        for (int i = 1; i < availableInstances.length; i++) {
            double exeTime = TaskUtility.executionTimeOnTypeWithCustomJob(firstJob, instanceInfo[availableInstances[i]].getType());
            if (exeTime < temp){
                temp = exeTime;
                tempInstance = i;
            }
        }

        xArray[firstJob.getIntId()] = tempInstance;
        yArray[tempInstance] = availableInstances[tempInstance];
        instanceTimeLine[tempInstance] = temp;
        instanceUsed[tempInstance] = true;

        taskFinishTimes[originalVersion.getIntId()] = instanceTimeLine[tempInstance];

        //for the rest of tasks
        for (int i = 1; i < orderedJobList.size(); i++) {
            Job job = orderedJobList.get(i);
            double tempTaskFinishTime = 999999999999999999.0;
            int tempInstanceId = -1;
            boolean gapOccurred = false;
            double endOfInstanceWaitTime = -99999999999999999.9;

            //Info about the gap and the instance
            boolean gapIsUsed = false;
            int instanceGapId = -1;
            int gapId = -1;
            //

            ArrayList<Integer> parentJobs = dag.getParents(job.getIntId());

            //it is possible to have multiple start tasks without dependencies
            for (int j = 0; j < availableInstances.length; j++) {

                int maxParentId = getJobWithMaxParentFinishTimeWithCij(parentJobs, job.getIntId(), j);
                Job maxParentJob;
                double latestParentFinishTime = 0.0;

                if (parentJobs.size() != 0){
                    maxParentJob = originalJobList.get(maxParentId);
                    latestParentFinishTime = taskFinishTimes[maxParentJob.getIntId()];
                }

                if (instanceList[j].gapList.size() > 0){
                    Collections.sort(instanceList[j].gapList , Gap.gapComparator);
                }

                if (instanceList[j].gapList.size() >= 1 && parentJobs.size() != 0){
                    int k =0;
                    for (Gap gap: instanceList[j].gapList){
                        if (latestParentFinishTime < gap.endTime){
                            double tempEdge = Math.abs(originalJobList.get(maxParentId).getEdge(job.getIntId()));
                            double tempCIJ = tempEdge / (double)Config.global.bandwidth;
                            double taskExeTime;

                            if (j == xArray[maxParentId]){
                                taskExeTime = TaskUtility.executionTimeOnTypeWithCustomJob(job, instanceInfo[availableInstances[j]].getType());
                            }else {
                                taskExeTime = TaskUtility.executionTimeOnTypeWithCustomJob(job, instanceInfo[availableInstances[j]].getType()) + tempCIJ;
                            }

                            double availableGapTime = gap.endTime - latestParentFinishTime;
                            double gapTest = availableGapTime;
                            if (availableGapTime > gap.duration){
                                gapTest = gap.duration;
                            }

                            if (gapTest >= taskExeTime){
                                double remainingTimeToStartGap = gap.startTime - latestParentFinishTime;
                                double gapTaskFinishTime;

                                if (remainingTimeToStartGap >= 0){
                                    double timeToSendData = gap.startTime - latestParentFinishTime;
                                    if (timeToSendData >= tempCIJ){
                                        gapTaskFinishTime = gap.startTime + (taskExeTime - tempCIJ);
                                    }else {
                                        gapTaskFinishTime = gap.startTime + (taskExeTime - timeToSendData);
                                    }
                                }else {
                                    gapTaskFinishTime = latestParentFinishTime + taskExeTime;
                                }

                                if (gapTaskFinishTime < tempTaskFinishTime){
                                    tempTaskFinishTime = gapTaskFinishTime;
                                    tempInstanceId = j;
                                    gapIsUsed = true;
                                    gapOccurred = false;
                                    instanceGapId = j;
                                    gapId = k;
                                }
                                break;
                            }
                            k++;
                        }else {
                            break;
                        }
                    }
                }
                if (parentJobs.size() == 0){
                    double currentFinishTime = instanceTimeLine[j] + TaskUtility.executionTimeOnTypeWithCustomJob(job, instanceInfo[availableInstances[j]].getType());

                    if (currentFinishTime < tempTaskFinishTime && currentFinishTime < 3600){
                        gapIsUsed = false;
                        tempTaskFinishTime = currentFinishTime;
                        tempInstanceId = j;
                    }
                }else {
                    //check minimum task finish time for all of the current instances
                    double waitingTime = taskFinishTimes[maxParentId] - instanceTimeLine[j];

                    if (waitingTime > 0 ){
                        double currentTime = instanceTimeLine[j] + waitingTime;
                        double cij = 0.0;
                        double currentFinishTime;

                        if (j == xArray[maxParentId]){
                            currentFinishTime = currentTime + TaskUtility.executionTimeOnTypeWithCustomJob(job, instanceInfo[availableInstances[j]].getType());
                        }else {
                            double edge = Math.abs(originalJobList.get(maxParentId).getEdge(job.getIntId()));
                            cij = edge / (double)Config.global.bandwidth;
                            double timeToSendData = currentTime - taskFinishTimes[maxParentId];
                            if (timeToSendData >= cij){
                                currentFinishTime = currentTime + TaskUtility.executionTimeOnTypeWithCustomJob(job, instanceInfo[availableInstances[j]].getType());
                            }else {
                                currentFinishTime = currentTime + (cij - timeToSendData) + TaskUtility.executionTimeOnTypeWithCustomJob(job, instanceInfo[availableInstances[j]].getType());
                            }
                        }

                        if (currentFinishTime < tempTaskFinishTime && currentFinishTime < 3600){
                            gapOccurred = true;
                            endOfInstanceWaitTime = currentTime;
                            tempTaskFinishTime = currentFinishTime;
                            tempInstanceId = j;
                            gapIsUsed = false;
                        }
                    }else {
                        double edge = Math.abs(originalJobList.get(maxParentId).getEdge(job.getIntId()));
                        double cij = edge / (double)Config.global.bandwidth;
                        double currentFinishTime;

                        if (j == xArray[maxParentId]){
                            currentFinishTime = instanceTimeLine[j] + TaskUtility.executionTimeOnTypeWithCustomJob(job, instanceInfo[availableInstances[j]].getType());
                        }else {
                            double timeToSendData = instanceTimeLine[j] - taskFinishTimes[maxParentId];

                            if (timeToSendData >= cij){
                                currentFinishTime = instanceTimeLine[j] + TaskUtility.executionTimeOnTypeWithCustomJob(job, instanceInfo[availableInstances[j]].getType());
                            }else {
                                currentFinishTime = instanceTimeLine[j] + (cij - timeToSendData) + TaskUtility.executionTimeOnTypeWithCustomJob(job, instanceInfo[availableInstances[j]].getType());
                            }
                        }

                        if (currentFinishTime < tempTaskFinishTime && currentFinishTime < 3600){
                            tempTaskFinishTime = currentFinishTime;
                            tempInstanceId = j;
                            gapOccurred = false;
                            gapIsUsed = false;
                        }
                    }
                }
            }
            if (gapOccurred && instanceUsed[tempInstanceId]){
                instanceList[tempInstanceId].hasGap = true;
                Gap gap = new Gap(instanceTimeLine[tempInstanceId], endOfInstanceWaitTime);
                instanceList[tempInstanceId].gapList.add(gap);
            }
            instanceUsed[tempInstanceId] = true;

            if (gapIsUsed){
                Gap gap = instanceList[instanceGapId].getGapList().get(gapId);
                gap.startTime = tempTaskFinishTime;
                if (gap.startTime >= gap.endTime){
                    instanceList[instanceGapId].gapList.remove(gapId);
                    Collections.sort(instanceList[instanceGapId].gapList , Gap.gapComparator);
                }else {
                    gap.duration = gap.endTime - gap.startTime;
                }
            }else {
                instanceTimeLine[tempInstanceId] = tempTaskFinishTime;
            }
            taskFinishTimes[job.getIntId()] = tempTaskFinishTime;
            xArray[job.getIntId()] = tempInstanceId;
            yArray[tempInstanceId] = availableInstances[tempInstanceId];
        }

        double maxTime = instanceTimeLine[0];
        for (double time : instanceTimeLine){
            if (time > maxTime){
                maxTime = time;
            }
        }
        Log.logger.info("Makespan from HEFT: "+ (int)maxTime);

        int numberOfUsedInstances =0;
        for (int i = 0; i < yArray.length; i++) {
            if (yArray[i] != -1){
                numberOfUsedInstances++;
            }
        }

        Solution solution;

        if (maxNumberOfUsedInstancesForIntegrationWithOtherSolutions > 0){
            solution = new Solution(workflow, instanceInfo, maxNumberOfUsedInstancesForIntegrationWithOtherSolutions);
            solution.numberOfUsedInstances = availableInstances.length;
            solution.xArray = xArray;

            int newYArray[] = new int[maxNumberOfUsedInstancesForIntegrationWithOtherSolutions];
            for (int i = 0; i < maxNumberOfUsedInstancesForIntegrationWithOtherSolutions; i++) {
                newYArray[i] = -1;
            }
            for (int i = 0; i < availableInstances.length; i++) {
                newYArray[i] = availableInstances[i];
            }
            solution.yArray = newYArray;

            solution.heftFitness();
        }else {
            solution = new Solution(workflow, instanceInfo, availableInstances.length);
            solution.numberOfUsedInstances = availableInstances.length;
            solution.xArray = xArray;
            solution.yArray = availableInstances;
            solution.heftFitness();
        }
        solution.origin = "heft";

        return solution;
    }
}
