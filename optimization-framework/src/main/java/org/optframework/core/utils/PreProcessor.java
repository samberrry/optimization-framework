package org.optframework.core.utils;

import org.cloudbus.cloudsim.util.workload.WorkflowDAG;
import org.cloudbus.spotsim.enums.InstanceType;
import org.optframework.GlobalAccess;
import org.optframework.config.Config;
import org.optframework.core.InstanceInfo;
import org.optframework.core.Job;
import org.optframework.core.Workflow;

import java.util.ArrayList;
import java.util.List;

/**
 * The Preprocessor computes:
 * - Ranks
 * - Execution times of tasks on every different instance type
 * - Number of parents for each task
 * - Maximum level of the workflow
 * */

public class PreProcessor {
    static List<Job> jobList;
    static org.cloudbus.cloudsim.util.workload.Workflow workflow;

    public static Workflow doPreProcessing(org.cloudbus.cloudsim.util.workload.Workflow workflow){
        WorkflowDAG dag = workflow.getWfDAG();

        int parents[] = new int[workflow.getJobList().size()];
        for (org.cloudbus.cloudsim.util.workload.Job job: workflow.getJobList()){
            parents[job.getIntId()] = dag.getParents(job.getIntId()).size();
        }
        GlobalAccess.numberOfParentsList = parents;

        ArrayList<Integer> nextLevel = dag.getFirstLevel();
        int temp = nextLevel.size();

        while (nextLevel.size() != 0){
            if (nextLevel.size() > temp){
                temp = nextLevel.size();
            }
            nextLevel = dag.getNextLevel(nextLevel);
        }
        GlobalAccess.maxLevel = temp;

        jobList = new ArrayList<>();
        PreProcessor.workflow = workflow;
        List<Job> jobListWithDoubleTaskLength = PopulateWorkflow.jobListWithDoubleTaskLength;

        for (org.cloudbus.cloudsim.util.workload.Job job : workflow.getJobList()){
            double exeTime[] = new double[InstanceType.values().length];
            double total = 0.0;
            Job doubleLengthJob = jobListWithDoubleTaskLength.get(job.getIntId());

            for (InstanceType type: InstanceType.values()){
                exeTime[type.getId()] = TaskUtility.executionTimeOnType(doubleLengthJob,type);
                total += exeTime[type.getId()];
            }
            Job newJob = new Job(job.getIntId(),
                    exeTime,
                    (total/InstanceType.values().length),
                    job.getEdgeInfo());

            //this is used only in heft algorithm
            newJob.setLength(doubleLengthJob.getLength());

            jobList.add(job.getIntId(), newJob);
        }

        return computeRank();
    }

    public static Workflow doPreProcessingForHEFTExample(org.cloudbus.cloudsim.util.workload.Workflow workflow){
        PreProcessor.workflow = workflow;

        double taskTimes[][] = {
                {14,16,9},
                {13,19,18},
                {11,13,19},
                {13,8,17},
                {12,13,10},
                {13,16,9},
                {7,15,11},
                {5,11,14},
                {18,12,20},
                {21,7,16}};

        jobList = new ArrayList<>();

        for (org.cloudbus.cloudsim.util.workload.Job job : workflow.getJobList()){
            double total = 0.0;
            for (double temp : taskTimes[job.getIntId()]){
                total += temp;
            }
            jobList.add(job.getIntId(), new Job(job.getIntId(),
                    taskTimes[job.getIntId()],
                    (total/taskTimes[job.getIntId()].length),
                    job.getEdgeInfo()));
        }

        return computeRank();
    }

    static Workflow computeRank(){
        WorkflowDAG dag = workflow.getWfDAG();
        ArrayList<Integer> level = dag.getLastLevel();

        for (int jobId: level){
            Job job = jobList.get(jobId);
            job.setRank(job.getAvgExeTime());
        }

        level = dag.getParents(level);

        while (level.size() != 0){
            for (int jobId : level){
                ArrayList<Integer> children = dag.getChildren(jobId);
                Job job = jobList.get(jobId);
                int maxChildId = getMaxChildRank(jobId, children);

                job.setRank(job.getAvgExeTime() + jobList.get(maxChildId).getRank() + job.getEdge(maxChildId)/Config.global.bandwidth);
            }
            level = dag.getParents(level);
        }

        return new Workflow(workflow.getWfDAG(),
                jobList,
                workflow.getJobList().size(),
                workflow.getDeadline(),
                workflow.getBudget(),
                0.0);
    }

    static int getMaxChildRank(int parent, ArrayList<Integer> children){
        int maxChild = -1;
        double maxVal = -1;

        for (int child : children){
            double newVal = jobList.get(child).getRank() + jobList.get(parent).getEdge(child)/Config.global.bandwidth;
            if (newVal > maxVal){
                maxVal = newVal;
                maxChild = child;
            }
        }
        return maxChild;
    }

    public static Workflow doPreProcessingForHEFT(org.cloudbus.cloudsim.util.workload.Workflow workflow, double bw, int totalInstances[], InstanceInfo instanceInfo[]){
        jobList = new ArrayList<>();
        List<Job> jobListWithDoubleTaskLength = PopulateWorkflow.jobListWithDoubleTaskLength;

        for (org.cloudbus.cloudsim.util.workload.Job job : workflow.getJobList()){
            double total = 0.0;
            double exeTime[] = new double[InstanceType.values().length];
            Job doubleLengthJob = jobListWithDoubleTaskLength.get(job.getIntId());

            for (int typeId: totalInstances){
                double taskExeTime = doubleLengthJob.getLength() / instanceInfo[typeId].getType().getEcu();
                total += taskExeTime;
            }

            for (InstanceType type: InstanceType.values()){
                exeTime[type.getId()] = TaskUtility.executionTimeOnType(doubleLengthJob,type);
            }

            Job newJob = new Job(job.getIntId(),
                    exeTime,
                    (total/totalInstances.length),
                    job.getEdgeInfo());

            newJob.setLength(doubleLengthJob.getLength());

            jobList.add(job.getIntId(), newJob);
        }

        return computeRank();
    }
}
