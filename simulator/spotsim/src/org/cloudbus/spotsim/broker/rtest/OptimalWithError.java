package org.cloudbus.spotsim.broker.rtest;

import org.cloudbus.cloudsim.util.workload.Job;
import org.cloudbus.spotsim.broker.ModelParameters;

public class OptimalWithError implements RuntimeEstimator {

    public OptimalWithError() {
	super();
    }

    @Override
    public long estimateJobLength(final Job job) {
	return (long) Math.ceil(job.getLength()
		+ job.getLength()
		* ModelParameters.randomInInterval(-0.1, 0.1));
    }

    @Override
    public void recordRuntime(final int userId, long timeTaken) {

    }
}
