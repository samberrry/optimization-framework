package org.optframework.core.utils;

import org.cloudbus.spotsim.enums.InstanceType;
import org.optframework.core.Job;

public class TaskUtility {
    public static double executionTimeOnType(Job job, InstanceType type){
        return (Math.abs(job.getLength()) * 8D) / type.getEcu();
    }

    public static double executionTimeOnTypeWithCustomJob(Job job, InstanceType type){
        return (Math.abs(job.getLength()) * 8D) / type.getEcu();
    }
}
