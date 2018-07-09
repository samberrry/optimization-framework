package org.optframework.core.heft;

import com.rits.cloning.Cloner;
import org.cloudbus.cloudsim.util.workload.WorkflowDAG;
import org.optframework.config.Config;
import org.optframework.core.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HEFTExampleAlgorithm {

    Workflow workflow;

    List<Job> orderedJobList;

    List<Job> originalJobList;

    Cloner cloner = new Cloner();

    int xArray[];

    public HEFTExampleAlgorithm(Workflow workflow) {
        this.workflow = workflow;
    }

    public void runAlgorithm() {
        originalJobList = workflow.getJobList();
        orderedJobList = cloner.deepClone(originalJobList);

        Collections.sort(orderedJobList, Job.rankComparator);
        WorkflowDAG dag = workflow.getWfDAG();

        double instanceTimeLine[] = new double[3];
        boolean instanceUsed[] = new boolean[3];

        xArray = new int[orderedJobList.size()];

        Instance instanceList[] = new Instance[3];
        for (int i = 0; i < 3; i++) {
            instanceList[i] = new Instance();
        }

        Job firstJob = orderedJobList.get(0);
        Job originalVersion = originalJobList.get(firstJob.getIntId());
        double temp = firstJob.getExeTime()[0];
        int tempInstance = 0;

        //for the first task
        for (int i = 1; i < 3; i++) {
            double exeTime = firstJob.getExeTime()[i];
            if (exeTime < temp){
                temp = exeTime;
                tempInstance = i;
            }
        }

        xArray[firstJob.getIntId()] = tempInstance;
        instanceUsed[tempInstance] = true;
        instanceTimeLine[tempInstance] = temp;

        originalVersion.setFinishTime(instanceTimeLine[tempInstance]);

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
            for (int j = 0; j < 3; j++) {
                int maxParentId = getJobWithMaxParentFinishTimeWithCij(parentJobs, job.getIntId(), j);
                Job maxParentJob;
                double latestParentFinishTime = 0.0;

                if (parentJobs.size() != 0){
                    maxParentJob = originalJobList.get(maxParentId);
                    latestParentFinishTime = maxParentJob.getFinishTime();
                }

                if (instanceList[j].gapList.size() > 0){
                    Collections.sort(instanceList[j].gapList , Gap.gapComparator);
                }

                if (instanceList[j].gapList.size() >= 1 && parentJobs.size() != 0){
                    int k =0;
                    for (Gap gap: instanceList[j].gapList){
                        if (latestParentFinishTime < gap.endTime){
                            double tempEdge = originalJobList.get(maxParentId).getEdge(job.getIntId());
                            double tempCIJ = tempEdge / (double)Config.global.bandwidth;
                            double taskExeTime;

                            if (j == xArray[maxParentId]){
                                taskExeTime = job.getExeTime()[j];
                            }else {
                                taskExeTime = job.getExeTime()[j] + tempCIJ;
                            }

                            double availableGapTime = gap.endTime - latestParentFinishTime;

                            if (availableGapTime >= taskExeTime){
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
                                    gapOccurred = false;
                                    gapIsUsed = true;
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
                //this is for the tasks in the first layer
                if (parentJobs.size() == 0){
                    double currentFinishTime = instanceTimeLine[j] + job.getExeTime()[j];

                    if (currentFinishTime < tempTaskFinishTime){
                        gapIsUsed = false;
                        tempTaskFinishTime = currentFinishTime;
                        tempInstanceId = j;
                    }
                }else {
                    //check minimum task finish time for all of the current instances
                    double waitingTime = originalJobList.get(maxParentId).getFinishTime() - instanceTimeLine[j];

                    if (waitingTime > 0 ){
                        double currentTime = instanceTimeLine[j] + waitingTime;
                        double edge = originalJobList.get(maxParentId).getEdge(job.getIntId());
                        double cij = edge / (double)Config.global.bandwidth;
                        double currentFinishTime;

                        if (j == xArray[maxParentId]){
                            currentFinishTime = currentTime + job.getExeTime()[j];
                        }else {
                            double timeToSendData = currentTime - originalJobList.get(maxParentId).getFinishTime();

                            if (timeToSendData >= cij){
                                currentFinishTime = currentTime + job.getExeTime()[j];
                            }else {
                                currentFinishTime = currentTime + (cij - timeToSendData) + job.getExeTime()[j];
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
                        double edge = originalJobList.get(maxParentId).getEdge(job.getIntId());
                        double cij = edge / (double)Config.global.bandwidth;
                        double currentFinishTime;

                        if (j == xArray[maxParentId]){
                            currentFinishTime = instanceTimeLine[j] + job.getExeTime()[j];
                        }else {
                            double timeToSendData = instanceTimeLine[j] - originalJobList.get(maxParentId).getFinishTime();

                            if (timeToSendData >= cij){
                                currentFinishTime = instanceTimeLine[j] + job.getExeTime()[j];
                            }else {
                                currentFinishTime = instanceTimeLine[j] + (cij - timeToSendData) + job.getExeTime()[j];
                            }
                        }

                        if (currentFinishTime < tempTaskFinishTime){
                            tempTaskFinishTime = currentFinishTime;
                            tempInstanceId = j;
                            gapIsUsed = false;
                            gapOccurred = false;
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
            originalJobList.get(job.getIntId()).setFinishTime(tempTaskFinishTime);
            xArray[job.getIntId()] = tempInstanceId;
        }

        double maxTime = instanceTimeLine[0];
        for (double time : instanceTimeLine){
            if (time > maxTime){
                maxTime = time;
            }
        }
        Log.logger.info("Makespan from HEFT: "+ (int)maxTime);

        String xStr = "";
        for (int val: xArray){
            xStr += " " + val;
        }
        System.out.println(xStr);
    }

    int getJobWithMaxParentFinishTimeWithCij(ArrayList<Integer> parentJobs, int jobId, int assignedInstanceId){
        double tempValue = -1;
        int tempId = -1;

        for (int parentId : parentJobs){
            double tempEdge = originalJobList.get(parentId).getEdge(jobId);
            double tempCIJ = tempEdge / (double)Config.global.bandwidth;
            double maxJobStartTime;
            if (assignedInstanceId == xArray[parentId]){
                maxJobStartTime = originalJobList.get(parentId).getFinishTime();
            }else {
                maxJobStartTime = originalJobList.get(parentId).getFinishTime() + tempCIJ;
            }

            if (tempValue < maxJobStartTime){
                tempValue = maxJobStartTime;
                tempId = originalJobList.get(parentId).getIntId();
            }
        }
        return tempId;
    }

}
