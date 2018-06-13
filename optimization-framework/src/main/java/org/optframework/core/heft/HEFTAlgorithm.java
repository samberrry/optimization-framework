package org.optframework.core.heft;

import org.cloudbus.cloudsim.util.workload.Job;
import org.cloudbus.cloudsim.util.workload.Workflow;
import org.cloudbus.cloudsim.util.workload.WorkflowDAG;
import org.cloudbus.spotsim.enums.InstanceType;
import org.optframework.core.*;

import java.util.*;

/**
 * This is implementation for the Heterogeneous Earliest Finish Time algorithm
 * */

public class HEFTAlgorithm implements OptimizationAlgorithm {

    Workflow workflow;

    InstanceInfo instanceInfo[];

    int numberOfInstances;

    public HEFTAlgorithm(Workflow workflow, InstanceInfo[] instanceInfo, int numberOfInstances) {
        this.workflow = workflow;
        this.instanceInfo = instanceInfo;
        this.numberOfInstances = numberOfInstances;
    }

    @Override
    public Solution runAlgorithm() {

        return null;
    }

//    List<Job> getSortedJobsBasedOnWeight(Workflow workflow){
//        if (workflow == null){
//            Log.logger.warning("Invalid input");
//            return null;
//        }
//
//        double totalCost = 0D;
//
//        WorkflowDAG dag = workflow.getWfDAG();
//        ArrayList<Integer> level = dag.getFirstLevel();
//
//        ArrayList<Job> jobList = (ArrayList<Job>) workflow.getJobList();
//
//        Map<Integer, ArrayList<ReadyTask>> instanceList = new HashMap();
//
////      Do this for the first level - First level may contain several tasks
//        for (int jobId: level){
//            int type = Beta.findFastestInstanceId();
//            double exeTime = TaskUtility.executionTimeOnType(jobList.get(jobId), instanceInfo[type].getType());
//
//            if (!instanceList.containsKey(instanceId)){
//                instanceList.put(instanceId, new ArrayList<ReadyTask>());
//            }
//            ArrayList<ReadyTask> readyTaskList = instanceList.get(instanceId);
//            readyTaskList.add(new ReadyTask(jobId, exeTime));
//        }
//
////      Computes maximum task's length and updates the instance times
//        for (Integer instance : instanceList.keySet()){
//            ArrayList<ReadyTask> readyTaskList = instanceList.get(instance);
//            Collections.sort(readyTaskList);
//
//            for (ReadyTask readyTask : readyTaskList){
//                instancesTimes[instance] += readyTask.exeTime;
//                instanceTimeLine[instance] += readyTask.exeTime;
//                instanceUsed[instance] = true;
//                Job job = jobList.get(readyTask.jobId);
//
//                job.setExeTime(readyTask.exeTime);
//                job.setWeight(readyTask.exeTime);
//                job.setFinishTime(instanceTimeLine[instance]);
//            }
//        }
//
//        //Go to the second level
//        level = dag.getNextLevel(level);
//
//        //Do this for the levels after the initial level
//        while (level.size() != 0){
//            instanceList = new HashMap<>();
//
//            /**
//             * This 'for' does the following:
//             * - finds max parent's finish time for every task in a level
//             * - assigns all of them to an instance
//             * - computes weights for all of the tasks in a level and make them ready to run on instance
//             * */
//            for (int jobId: level){
//                int instanceId = this.xArray[jobId];
//                int typeId = this.yArray[instanceId];
//
//                InstanceType instanceType = instanceInfo[typeId].getType();
//                double exeTime = TaskUtility.executionTimeOnType(jobList.get(jobId), instanceType);
//
//                ArrayList<Integer> parentList = dag.getParents(jobId);
//
//                ArrayList<ParentTask> parentTaskList = new ArrayList<>();
//                for (Integer parentId : parentList){
//                    parentTaskList.add(new ParentTask(parentId , jobList.get(parentId).getFinishTime() , jobList.get(parentId).getEdge(jobId)/ instanceType.getBandwidth()));
//                }
//                ParentTask maxParent = findMaxParentFinishTimeWithCR(parentTaskList);
//
//                if (!instanceList.containsKey(instanceId)){
//                    instanceList.put(instanceId, new ArrayList<ReadyTask>());
//                }
//                ArrayList<ReadyTask> readyTaskList = instanceList.get(instanceId);
//
//                readyTaskList.add(new ReadyTask(jobId,exeTime, maxParent.parentFinishTime, jobList.get(maxParent.parentId).getWeight() , maxParent.cr));
//            }
//
////            It is time to compute the time for every instance
//            for (Integer instance : instanceList.keySet()){
//                ArrayList<ReadyTask> readyTaskList = instanceList.get(instance);
//                Collections.sort(readyTaskList, ReadyTask.weightComparator);
//
//                for (ReadyTask readyTask : readyTaskList){
//                    Job job = jobList.get(readyTask.jobId);
//
//                    if (!instanceUsed[instance]){
//                        instanceUsed[instance] = true;
//                        instancesTimes[instance] = readyTask.exeTime + readyTask.cr;
//                        instanceTimeLine[instance] = readyTask.maxParentFinishTime;
//                        instanceTimeLine[instance] += readyTask.exeTime + readyTask.cr;
//                    }else if (readyTask.maxParentFinishTime > instanceTimeLine[instance]){
//                        double timeToWait = readyTask.maxParentFinishTime - instanceTimeLine[instance];
//                        instancesTimes[instance] += (timeToWait + readyTask.exeTime + readyTask.cr);
//
//                        instanceTimeLine[instance] = readyTask.maxParentFinishTime + readyTask.exeTime + readyTask.cr;
//                    }else{
//                        instancesTimes[instance] += readyTask.exeTime + readyTask.cr;
//                        instanceTimeLine[instance] += readyTask.exeTime + readyTask.cr;
//                    }
//
//                    job.setExeTime(readyTask.exeTime);
//                    job.setFinishTime(instanceTimeLine[instance]);
//                    job.setWeight( readyTask.weight);
//                }
//            }
//
//            level = dag.getNextLevel(level);
//        }
//
//        return null;
//    }
}
