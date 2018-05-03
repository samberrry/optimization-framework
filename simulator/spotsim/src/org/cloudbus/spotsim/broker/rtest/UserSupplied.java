package org.cloudbus.spotsim.broker.rtest;

import org.cloudbus.cloudsim.util.workload.Job;

public class UserSupplied implements RuntimeEstimator {

    public UserSupplied() {
	super();
    }

    @Override
    public long estimateJobLength(final Job job) {
	return job.getReqRunTime();
    }

    @Override
    public void recordRuntime(final int userId, long timeTaken) {

    }
}
