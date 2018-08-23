package org.optframework.core.utils;

import org.cloudbus.cloudsim.util.workload.Job;
import org.cloudbus.spotsim.enums.InstanceType;

public class TaskUtility {
    public static double executionTimeOnType(Job job, InstanceType type){
        return (double) job.getLength() / type.getEcu();
    }

    public static double executionTimeOnTypeWithCustomJob(org.optframework.core.Job job, InstanceType type){
        return job.getLength() / type.getEcu();
    }
}
