package org.cloudbus.spotsim.broker.rtest;

import org.cloudbus.cloudsim.util.workload.Job;

public interface RuntimeEstimator {

    long estimateJobLength(Job job);

    void recordRuntime(int userId, long timeTaken);
}