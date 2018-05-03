package org.cloudbus.spotsim.broker.rtest;

import org.cloudbus.cloudsim.util.workload.Job;
import org.cloudbus.spotsim.broker.ModelParameters;

public class RandomEstimation implements RuntimeEstimator {

    public RandomEstimation() {
	super();
    }

    @Override
    public long estimateJobLength(final Job job) {
	return (long) ModelParameters.randomInInterval(1, job.getReqRunTime());
    }

    @Override
    public void recordRuntime(final int userId, long timeTaken) {

    }
}
