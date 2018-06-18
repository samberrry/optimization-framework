package org.optframework.core.heft;

import com.rits.cloning.Cloner;
import org.cloudbus.cloudsim.util.workload.WorkflowDAG;
import org.cloudbus.spotsim.enums.InstanceType;
import org.optframework.config.Config;
import org.optframework.core.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HEFTAlgorithm implements OptimizationAlgorithm {

    Workflow workflow;

    InstanceInfo instanceInfo[];

    List<Job> orderedJobList;

    List<Job> originalJobList;

    Cloner cloner = new Cloner();

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

        double instancesTimes[] = new double[orderedJobList.size()];

        double instanceTimeLine[] = new double[orderedJobList.size()];

        int numberOfUsedInstances = 0;

        int xArray[] = new int[orderedJobList.size()];
        int yArray[] = new int[orderedJobList.size()];

        Job firstJob = orderedJobList.get(0);
        Job originalVersion = originalJobList.get(firstJob.getIntId());
        double temp = firstJob.getExeTime()[InstanceType.values()[0].getId()];
        InstanceType tempType = InstanceType.values()[0];

        //for the first task
        for (InstanceType type : InstanceType.values()){
            double exeTime = firstJob.getExeTime()[type.getId()];
            if (exeTime < temp){
                temp = exeTime;
                tempType = type;
            }
        }
        xArray[firstJob.getIntId()] = 0;
        yArray[0] = tempType.getId();
        instanceTimeLine[0] = temp;
        instancesTimes[0] = temp;

        originalVersion.setFinishTime(instanceTimeLine[0]);

        numberOfUsedInstances++;

        //for the rest of tasks
        for (int i = 1; i < orderedJobList.size(); i++) {
            Job job = orderedJobList.get(i);
            double tempTaskFinishTime = 999999999999999999.0;
            int tempInstanceId = -1;

            ArrayList<Integer> parentJobs = dag.getParents(job.getIntId());

            //check minimum task finish time for all of the current instances
            for (int j = 0; j < numberOfUsedInstances; j++) {
                int maxParentId = getJobWithMaxParentFinishTime(parentJobs);

                double waitingTime = originalJobList.get(maxParentId).getFinishTime() - instanceTimeLine[j];

                if (waitingTime > 0 ){
                    double currentTime = instanceTimeLine[j] + waitingTime;
                    double edge = originalJobList.get(maxParentId).getEdge(i);
                    double cij = edge / (double)Config.global.bandwidth;

                    double currentFinishTime = currentTime + cij + originalJobList.get(i).getExeTime()[yArray[j]];

                    if (currentFinishTime < tempTaskFinishTime){
                        tempTaskFinishTime = currentFinishTime;
                        tempInstanceId = j;
                    }
                }else {
                    double edge = originalJobList.get(maxParentId).getEdge(i);
                    double cij = edge / (double)Config.global.bandwidth;

                    double currentFinishTime = instanceTimeLine[j] + cij + originalJobList.get(i).getExeTime()[yArray[j]];

                    if (currentFinishTime < tempTaskFinishTime){
                        tempTaskFinishTime = currentFinishTime;
                        tempInstanceId = j;
                    }
                }
            }
            boolean newInstanceIsUsed = false;
            int newType = -1;
            for (InstanceType type: InstanceType.values()){
                int maxParentId = getJobWithMaxParentFinishTime(parentJobs);
                double edge = originalJobList.get(maxParentId).getEdge(i);
                double cij = edge / (double)Config.global.bandwidth;

                double currentFinishTimeForNew = originalJobList.get(maxParentId).getFinishTime() + cij + originalJobList.get(i).getExeTime()[type.getId()];

                if (currentFinishTimeForNew < tempTaskFinishTime) {
                    newInstanceIsUsed = true;
                    tempTaskFinishTime = currentFinishTimeForNew;
                    tempInstanceId = numberOfUsedInstances;
                    newType = type.getId();
                }
            }

            originalJobList.get(i).setFinishTime(tempTaskFinishTime);
            xArray[i] = tempInstanceId;

            if (newInstanceIsUsed){
                yArray[tempInstanceId] = newType;
                numberOfUsedInstances++;
            }
        }

        String xArrayStr = "";
        for (int val : xArray){
            xArrayStr += " " + String.valueOf(val);
        }
        Log.logger.info("Value of the X Array: "+ xArrayStr);

        String yArrayStr = "";
        for (int i = 0; i < numberOfUsedInstances; i++) {
            yArrayStr += " " + String.valueOf(yArray[i]);
        }
        Log.logger.info("Value of the Y Array: "+ yArrayStr);


        Solution solution = new Solution(workflow, instanceInfo, Config.global.m_number);
        solution.numberOfUsedInstances = numberOfUsedInstances;
        solution.xArray = xArray;
        solution.yArray = yArray;

        solution.fitness();

        return solution;
    }

    int getJobWithMaxParentFinishTime(ArrayList<Integer> parentJobs){
        double tempValue = originalJobList.get(parentJobs.get(0)).getFinishTime();
        int tempId = originalJobList.get(parentJobs.get(0)).getIntId();

        for (int parentId : parentJobs){
            if (tempValue > originalJobList.get(parentId).getFinishTime()){
                tempId = originalJobList.get(parentId).getIntId();
            }
        }
        return tempId;
    }
}
