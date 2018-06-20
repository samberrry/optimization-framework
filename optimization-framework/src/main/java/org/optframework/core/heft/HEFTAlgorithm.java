package org.optframework.core.heft;

import com.rits.cloning.Cloner;
import org.cloudbus.cloudsim.util.workload.WorkflowDAG;
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

    //this array is the y array from the hbmo algorithm with specified size
    int usedInstances[];

    public HEFTAlgorithm(Workflow workflow, InstanceInfo[] instanceInfo, int usedInstances[]) {
        this.workflow = workflow;
        this.instanceInfo = instanceInfo;
        this.usedInstances = usedInstances;
    }

    public HEFTAlgorithm(Workflow workflow, InstanceInfo[] instanceInfo) {
        this.workflow = workflow;
        this.instanceInfo = instanceInfo;
    }

    @Override
    public Solution runAlgorithm() {
        originalJobList = workflow.getJobList();
        orderedJobList = cloner.deepClone(originalJobList);

        Collections.sort(orderedJobList, Job.rankComparator);
        WorkflowDAG dag = workflow.getWfDAG();

        double instanceTimeLine[] = new double[usedInstances.length];

        int xArray[] = new int[orderedJobList.size()];
        int yArray[] = new int[usedInstances.length];

        Job firstJob = orderedJobList.get(0);
        Job originalVersion = originalJobList.get(firstJob.getIntId());
        double temp = TaskUtility.executionTimeOnTypeWithCustomJob(firstJob, instanceInfo[usedInstances[0]].getType());
        int tempInstance = 0;

        //for the first task
        for (int i = 1; i < usedInstances.length; i++) {
            double exeTime = TaskUtility.executionTimeOnTypeWithCustomJob(firstJob, instanceInfo[usedInstances[i]].getType());
            if (exeTime < temp){
                temp = exeTime;
                tempInstance = i;
            }
        }

        xArray[firstJob.getIntId()] = tempInstance;
        //error
        yArray[tempInstance] = usedInstances[tempInstance];
        instanceTimeLine[tempInstance] = temp;

        originalVersion.setFinishTime(instanceTimeLine[tempInstance]);

        //for the rest of tasks
        for (int i = 1; i < orderedJobList.size(); i++) {
            Job job = orderedJobList.get(i);
            double tempTaskFinishTime = 999999999999999999.0;
            int tempInstanceId = -1;

            ArrayList<Integer> parentJobs = dag.getParents(job.getIntId());

            //it is possible to have multiple start tasks without dependencies
            if (parentJobs.size() == 0){
                for (int j = 0; j < usedInstances.length; j++) {
                    double currentFinishTime = instanceTimeLine[j] + TaskUtility.executionTimeOnTypeWithCustomJob(job, instanceInfo[usedInstances[j]].getType());

                    if (currentFinishTime < tempTaskFinishTime){
                        tempTaskFinishTime = currentFinishTime;
                        tempInstanceId = j;
                    }
                }
            }else {
                //check minimum task finish time for all of the current instances
                for (int j = 0; j < usedInstances.length; j++) {
                    int maxParentId = getJobWithMaxParentFinishTime(parentJobs);

                    double waitingTime = originalJobList.get(maxParentId).getFinishTime() - instanceTimeLine[j];

                    if (waitingTime > 0 ){
                        double currentTime = instanceTimeLine[j] + waitingTime;
                        double edge = originalJobList.get(maxParentId).getEdge(job.getIntId());
                        double cij = edge / (double)Config.global.bandwidth;

                        double currentFinishTime = currentTime + cij + TaskUtility.executionTimeOnTypeWithCustomJob(job, instanceInfo[usedInstances[j]].getType());

                        if (currentFinishTime < tempTaskFinishTime){
                            tempTaskFinishTime = currentFinishTime;
                            tempInstanceId = j;
                        }
                    }else {
                        double edge = originalJobList.get(maxParentId).getEdge(job.getIntId());
                        double cij = edge / (double)Config.global.bandwidth;

                        double currentFinishTime = instanceTimeLine[j] + cij + TaskUtility.executionTimeOnTypeWithCustomJob(job, instanceInfo[usedInstances[j]].getType());

                        if (currentFinishTime < tempTaskFinishTime){
                            tempTaskFinishTime = currentFinishTime;
                            tempInstanceId = j;
                        }
                    }
                }
            }

            instanceTimeLine[tempInstanceId] = tempTaskFinishTime;
            originalJobList.get(job.getIntId()).setFinishTime(tempTaskFinishTime);
            xArray[job.getIntId()] = tempInstanceId;
            yArray[tempInstanceId] = usedInstances[tempInstanceId];
        }

        for (int i = 0; i < instanceTimeLine.length; i++) {
            Log.logger.info("Timeline for instance " + instanceInfo[yArray[i]].getType().getName() + " : " + instanceTimeLine[i]);
        }

        String xArrayStr = "";
        for (int val : xArray){
            xArrayStr += " " + String.valueOf(val);
        }
        Log.logger.info("Value of the X Array: "+ xArrayStr);

        String yArrayStr = "";
        for (int i = 0; i < usedInstances.length; i++) {
            yArrayStr += " " + String.valueOf(yArray[i]);
        }
        Log.logger.info("Value of the Y Array: "+ yArrayStr);

        double maxTime = instanceTimeLine[0];
        for (double time : instanceTimeLine){
            if (time > maxTime){
                maxTime = time;
            }
        }
        Log.logger.info("Makespan from HEFT: "+ (int)maxTime);

        Solution solution = new Solution(workflow, instanceInfo, Config.global.m_number);
//        solution.numberOfUsedInstances = usedInstances.length;
//        solution.xArray = xArray;
//        solution.yArray = yArray;
//
//        solution.fitness();

        return solution;
    }

    int getJobWithMaxParentFinishTime(ArrayList<Integer> parentJobs){
        double tempValue = originalJobList.get(parentJobs.get(0)).getFinishTime();
        int tempId = originalJobList.get(parentJobs.get(0)).getIntId();

        for (int parentId : parentJobs){
            if (tempValue < originalJobList.get(parentId).getFinishTime()){
                tempId = originalJobList.get(parentId).getIntId();
            }
        }
        return tempId;
    }
}
