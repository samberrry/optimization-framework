package org.optframework.core;

import org.cloudbus.cloudsim.util.workload.Job;
import org.cloudbus.spotsim.enums.InstanceType;

public class TaskUtility {
    public static long executionTimeOnType(Job job, InstanceType type){
        return job.getLength() / type.getEc2units();
    }
}
