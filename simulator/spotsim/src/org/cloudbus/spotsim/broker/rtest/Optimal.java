package org.cloudbus.spotsim.broker.rtest;

import org.cloudbus.cloudsim.util.workload.Job;

public class Optimal implements RuntimeEstimator {

    public Optimal() {
	super();
    }

    @Override
    public long estimateJobLength(final Job job) {
	return job.getLength();
    }

    @Override
    public void recordRuntime(final int userId, long timeTaken) {

    }
}
