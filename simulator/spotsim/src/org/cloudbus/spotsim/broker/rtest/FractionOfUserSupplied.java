package org.cloudbus.spotsim.broker.rtest;

import org.cloudbus.cloudsim.util.workload.Job;

public class FractionOfUserSupplied implements RuntimeEstimator {

    private static final double FRACTION = 0.5;

    public FractionOfUserSupplied() {
	super();
    }

    @Override
    public long estimateJobLength(final Job job) {
	return (long) Math.ceil(job.getReqRunTime() * FRACTION);
    }

    @Override
    public void recordRuntime(final int userId, long timeTaken) {

    }
}
