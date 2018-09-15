package org.optframework.core.utils;

import org.cloudbus.cloudsim.util.workload.Job;
import org.cloudbus.spotsim.enums.InstanceType;
import org.optframework.config.Config;

public class TaskUtility {
    public static double executionTimeOnType(Job job, InstanceType type){
        return (double) job.getLength() * Config.global.task_length_coefficient / type.getEc2units();
    }

    public static double executionTimeOnTypeWithCustomJob(org.optframework.core.Job job, InstanceType type){
        return job.getLength() * Config.global.task_length_coefficient / type.getEc2units();
    }
}
