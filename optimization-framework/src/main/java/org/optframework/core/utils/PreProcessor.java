package org.optframework.core.utils;

import org.cloudbus.cloudsim.util.workload.WorkflowDAG;
import org.cloudbus.spotsim.enums.InstanceType;
import org.optframework.core.Job;
import org.optframework.core.Workflow;

import java.util.ArrayList;
import java.util.List;

public class PreProcessor {
    public static Workflow doPreProcessing(org.cloudbus.cloudsim.util.workload.Workflow workflow){
        List<Job> jobList = new ArrayList<>();

        for (org.cloudbus.cloudsim.util.workload.Job job : workflow.getJobList()){
            double exeTime[] = new double[InstanceType.values().length];
            double total = 0.0;

            for (InstanceType type: InstanceType.values()){
                exeTime[type.getId()] = TaskUtility.executionTimeOnType(job,type);
                total += exeTime[type.getId()];
            }
            jobList.add(job.getIntId(), new Job(job.getIntId(),
                    0,
                    0,
                    exeTime,
                    (total/InstanceType.values().length),
                    job.getEdgeInfo()));
        }

        WorkflowDAG dag = workflow.getWfDAG();
        ArrayList<Integer> level = dag.getLastLevel();

        for (int jobId: level){
            Job job = jobList.get(jobId);
            job.setRank(job.getAvgExeTime());
        }

        level = dag.getParents(level);

        while (level.size() != 0){


            level = dag.getParents(level);
        }

        return new Workflow(workflow.getWfDAG(),
                jobList,
                workflow.getJobList().size(),
                workflow.getDeadline(),
                workflow.getBudget(),
                0.0);
    }
}
