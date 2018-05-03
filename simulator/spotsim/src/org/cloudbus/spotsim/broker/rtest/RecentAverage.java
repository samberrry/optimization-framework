package org.cloudbus.spotsim.broker.rtest;

import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.util.workload.Job;
import org.cloudbus.spotsim.broker.BoundedNonBlockingDeque;
import org.cloudbus.spotsim.main.config.SimProperties;

public class RecentAverage implements RuntimeEstimator {

    private static final int MULTIPLIER = 1;

    private final Map<Integer, Deque<Long>> latestJobRuntimes;

    public RecentAverage() {
	super();
	this.latestJobRuntimes = new HashMap<Integer, Deque<Long>>();
    }

    @Override
    public long estimateJobLength(final Job job) {
	double ret;
	final Deque<Long> userJobs = this.latestJobRuntimes.get(job.getUserID());
	if (userJobs == null || userJobs.size() < SimProperties.SCHED_PAST_RUNTIMES.asInt()) {
	    return job.getReqRunTime();
	}
	ret = avg(userJobs) * MULTIPLIER;
	if (Log.logger.isLoggable(Level.FINE)) {
	    Log.logger.fine(Log.clock()
		    + "Estimated length of job "
		    + job.getId()
		    + " of length "
		    + job.getLength()
		    + " is "
		    + ret);
	}
	return (long) Math.ceil(ret);
    }

    @Override
    public void recordRuntime(final int userId, long serialLength) {
	Deque<Long> list = this.latestJobRuntimes.get(userId);
	if (list == null) {
	    list = new BoundedNonBlockingDeque<Long>(SimProperties.SCHED_PAST_RUNTIMES.asInt());
	    this.latestJobRuntimes.put(userId, list);
	}

	if (serialLength <= 0) {
	    throw new IllegalStateException("Time taken by job must be greater than 0, but it is "
		    + serialLength);
	}

	list.offerFirst(serialLength);
    }

    private double avg(final Collection<Long> list) {
	double sum = 0D;
	for (final Long n : list) {
	    sum += n;
	}
	return sum / list.size();
    }
}
