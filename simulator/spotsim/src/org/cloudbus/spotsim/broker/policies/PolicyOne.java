package org.cloudbus.spotsim.broker.policies;

import static org.cloudbus.cloudsim.util.workload.TaskState.POSTPONED;

import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.apache.commons.math.util.MathUtils;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.util.workload.Task;
import org.cloudbus.spotsim.broker.Broker;
import org.cloudbus.spotsim.broker.ModelParameters;
import org.cloudbus.spotsim.broker.SchedPolicy;
import org.cloudbus.spotsim.broker.SchedulingDecision;
import org.cloudbus.spotsim.broker.forecasting.MinMaxMean;
import org.cloudbus.spotsim.broker.resources.Resource;
import org.cloudbus.spotsim.broker.rtest.RuntimeEstimator;
import org.cloudbus.spotsim.enums.AZ;
import org.cloudbus.spotsim.enums.BiddingStrategy;
import org.cloudbus.spotsim.enums.InstanceType;
import org.cloudbus.spotsim.enums.OS;
import org.cloudbus.spotsim.enums.PriceForecastingMethod;
import org.cloudbus.spotsim.enums.Region;
import org.cloudbus.spotsim.main.config.SimProperties;
import org.cloudbus.spotsim.pricing.db.PriceDB;
import org.cloudbus.spotsim.simrecords.Profiler.Metric;
import org.cloudbus.spotsim.simrecords.SimulationData;

public class PolicyOne implements SchedPolicy {

    private static final String NAME = "ONE";

    private Broker broker;

    protected RuntimeEstimator estimator;

    public PolicyOne() {
    }

    @Override
    public SchedulingDecision fitsOnIdle(final Task task, final Collection<Resource> idle) {

	for (final Resource idleResource : idle) {
	    final long estimatedParallelTime = ModelParameters.execTimeParallel(task.getJob()
		.getA(), task.getJob().getSigma(), idleResource.getType().getEc2units(), task
		.getJob().getEstimatedLength());
	    final long gratisSeconds = idleResource.getSecondsToNextFullHour();

	    if (gratisSeconds < estimatedParallelTime) {
		return new SchedulingDecision(task, idleResource, estimatedParallelTime);
	    }
	}
	return null;
    }

    @Override
    public RuntimeEstimator getRuntimeEstimator() {
	return this.estimator;
    }

    @Override
    public String policyName() {
	return NAME;
    }

    /**
     * Does the scheduling. Firstly: tries to fit the job in an existing idle
     * instance. Secondly: tries to queue the job in an instance that will be
     * idle soon. Finally, allocates a brand new instance for this job
     */
    @Override
    public SchedulingDecision sched(final Task task, final Collection<Resource> myInstances,
	    boolean reachedMax, Region region) {

	EnumSet<AZ> usableAZsForTask = region.getAvailabilityZones();
	final EnumSet<AZ> usedOnes = task.getJob().getAZInUse();
	if (usedOnes.size() < usableAZsForTask.size()) {
	    usableAZsForTask.removeAll(usedOnes);
	}

	if (task.getJob().getEstimatedLength() < 0) {
	    final long estimateJobLength = this.estimator.estimateJobLength(task.getJob());
	    task.getJob().setEstimatedLength(estimateJobLength);
	}

	final Map<InstanceType, Long> estimates = new EnumMap<InstanceType, Long>(
	    InstanceType.class);

	// pre-compute task running times on all instance types
	for (final InstanceType instanceType : InstanceType.values()) {
	    final long execTimeParallel = ModelParameters.execTimeParallel(task.getJob().getA(),
		task.getJob().getSigma(), instanceType.getEc2units(), task.getJob()
		    .getEstimatedLength());
	    if (execTimeParallel <= 0) {
		throw new IllegalStateException("Task runtime estimate must be greater than 0");
	    }
	    estimates.put(instanceType, execTimeParallel);
	}

	final long longestEstimatedRunTime = estimates.get(InstanceType.M1SMALL);

	final long clock = CloudSim.clock();
	final long maxWaitTime = Math.max(
	    0L,
	    task.getJob().getDeadline()
		    - (SimProperties.SCHED_PESSIMIST_FACTOR.asInt()
			    * longestEstimatedRunTime
			    + clock
			    + SimProperties.DC_VM_INIT_TIME.asInt() + SimProperties.SCHED_INTERVAL
			.asInt()));

	final long maxStartTime = clock + maxWaitTime;

	SimulationData.singleton().getProfiler().startPeriod(Metric.SCHED_RECYCLE);
	// search for resources among active instances
	if (Log.logger.isLoggable(Level.INFO)) {
	    Log.logger.info(Log.clock()
		    + " SCHEDULING JOB: "
		    + task.getId()
		    + ", length: "
		    + task.getJob().getLength()
		    + ", deadline: "
		    + task.getJob().getDeadline()
		    + ", maximum wait time: "
		    + maxWaitTime
		    + ", maximum start time "
		    + maxStartTime
		    + ", longest estimated runtime "
		    + longestEstimatedRunTime
		    + ", Status: "
		    + task.getState()
		    + ", total: "
		    + myInstances.size());
	}

	double recyclePrice = Double.MAX_VALUE;
	double earliestCompletionTime = Double.MAX_VALUE;
	Resource suitableResource = null;
	for (final Resource res : myInstances) {
	    if (usableAZsForTask.contains(res.getAz())) {
		final long estimatedTime = estimates.get(res.getType());
		final long expectedIdleTime = res.getTimeThatItWillBecomeIdle();
		final long nextFullHour = res.getNextFullHourAfterBecomingIdle();
		final long gratisSeconds = nextFullHour - expectedIdleTime;

		if (expectedIdleTime <= maxStartTime || reachedMax) {
		    if (estimatedTime <= gratisSeconds) {
			/*
			 * Job can be fit in less than one hour that has already
			 * been paid (execution is thus free, gratis)
			 */
			recyclePrice = 0;
			final double complTime = expectedIdleTime + estimatedTime;
			if (complTime < earliestCompletionTime) {
			    earliestCompletionTime = complTime;
			    suitableResource = res;
			}
		    } else if (recyclePrice > 0) {
			final double p = forecastFuturePrice(region, res.getAz(), res.getType(),
			    res.getOs(), nextFullHour,
			    (long) Math.ceil(nextFullHour + estimatedTime - gratisSeconds));
			if (p < recyclePrice) {
			    recyclePrice = p;
			    suitableResource = res;
			}
		    }
		}
	    }
	}

	SimulationData.singleton().getProfiler().endPeriod(Metric.SCHED_RECYCLE);

	// found a suitable existing instance, will recycle it.
	if (suitableResource != null && (recyclePrice == 0 || reachedMax)) {
	    final long startTime = suitableResource.getTimeThatItWillBecomeIdle();
	    if (Log.logger.isLoggable(Level.FINE)) {
		Log.logger.fine(Log.clock()
			+ "Reusing instance "
			+ suitableResource.getId()
			+ " for job "
			+ task.getId()
			+ ", idle time: "
			+ startTime
			+ ", status: "
			+ suitableResource.getState());
	    }
	    final long estimatedRunTime = estimates.get(suitableResource.getType());
	    return new SchedulingDecision(task, false, Region.getDefault(),
		suitableResource.getAz(), suitableResource.getType(), OS.getDefault(),
		suitableResource, false, startTime, estimatedRunTime, recyclePrice, 0D, maxWaitTime);
	}

	/* Decides whether to postpone */
	final boolean postpone = toPostponeOrNotToPostpone(task, longestEstimatedRunTime,
	    maxWaitTime, reachedMax);

	if (postpone) {
	    // postpone job scheduling, because it's not urgent and there are no
	    // idle instances, or the maximum number of instances has been
	    // reached
	    return new SchedulingDecision(task, maxWaitTime);
	}

	double minCost = recyclePrice;
	long startTime = -1L;

	// check if it's worth starting a brand new instance. It still might be
	// cheaper than the previously computed recycle price
	InstanceType chosenInstanceType = null;
	AZ chosenAz = null;
	OS preferedOS = OS.LINUX;
	Region preferedRegion = Region.US_EAST;

	SimulationData.singleton().getProfiler().startPeriod(Metric.SCHED_NEW);
	final long increment = 1800;
	for (AZ az : usableAZsForTask) {
	    Set<InstanceType> usableTypes = PriceDB.typesAvailableList(preferedRegion, az,
		preferedOS);
	    for (final InstanceType instanceType : usableTypes) {
		final long estimatedTime = estimates.get(instanceType);
		// swipes future prices to find a good spot to fit the job
		for (long i = clock; i <= maxStartTime; i += increment) {
		    SimulationData.singleton().getProfiler().startPeriod(Metric.FORECASTING);

		    final double costForANew = forecastFuturePrice(region, az, instanceType,
			preferedOS, i, i + estimatedTime);
		    SimulationData.singleton().getProfiler().endPeriod(Metric.FORECASTING);

		    if (costForANew < minCost) {
			minCost = costForANew;
			chosenInstanceType = instanceType;
			chosenAz = az;
			startTime = i;
		    }
		    if (!((PriceForecastingMethod) SimProperties.PRICING_COST_FORECASTING_METHOD
			.asEnum()).isFuturistic()) {
			break;
		    }
		}
	    }
	}

	SimulationData.singleton().getProfiler().endPeriod(Metric.SCHED_NEW);

	if (suitableResource != null && recyclePrice <= minCost) {
	    startTime = suitableResource.getTimeThatItWillBecomeIdle();
	    if (Log.logger.isLoggable(Level.FINE)) {
		Log.logger.fine(Log.clock()
			+ " Reusing instance "
			+ suitableResource.getId()
			+ " for job "
			+ task.getId()
			+ ", idle time: "
			+ startTime
			+ ", status: "
			+ suitableResource.getState());
	    }

	    final long estimatedRunTime = estimates.get(suitableResource.getType());
	    return new SchedulingDecision(task, false, Region.getDefault(),
		suitableResource.getAz(), suitableResource.getType(), OS.getDefault(),
		suitableResource, false, startTime, estimatedRunTime, recyclePrice, 0D, maxWaitTime);
	}

	final long estimatedRunTime = estimates.get(chosenInstanceType);
	double bidPrice = bid(preferedRegion, chosenAz, chosenInstanceType, preferedOS, startTime,
	    estimatedRunTime);
	return new SchedulingDecision(task, false, Region.getDefault(), chosenAz,
	    chosenInstanceType, OS.getDefault(), null, true, startTime, estimatedRunTime, minCost,
	    bidPrice, maxWaitTime);
    }

    private double bid(Region chosenRegion, AZ chosenAz, InstanceType chosenInstanceType,
	    OS chosenOs, long startTime, final long estimatedRunTime) {
	final MinMaxMean minMax = forecastMinMaxMean(startTime, startTime + estimatedRunTime,
	    CloudSim.clock(), chosenInstanceType, chosenOs, chosenAz, chosenRegion);
	double bid;
	switch ((BiddingStrategy) SimProperties.SCHED_BIDDING_STRAT.asEnum()) {
	case MIN:
	    bid = minMax.getMin() + 0.001;
	    break;
	case MAX:
	    bid = minMax.getMax() + 0.001;
	    break;
	case MEAN:
	    bid = minMax.getMean();
	    break;
	case HIGH:
	    bid = 1000D;
	    break;
	case ONDEMAND:
	    bid = PriceDB.getOnDemandPrice(chosenRegion, chosenInstanceType, chosenOs);
	    break;
	case CURRENT:
	    bid = this.broker.priceQuery(chosenInstanceType, chosenOs) + 0.001;
	    break;
	default:
	    bid = 1000D;
	    break;
	}

	return MathUtils.round(bid, 3);
    }

    private MinMaxMean forecastMinMaxMean(long from, long to, long current, InstanceType type,
	    OS os, AZ az, Region region) {
	return PriceDB.getForecaster(region, az).forecastMinMaxMean(type, os, from, to, current);
    }

    private double forecastFuturePrice(Region region, AZ az, InstanceType type, OS os, long from,
	    long to) {
	return PriceDB.getForecaster(region, az).forecastFuturePrice(type, os, from, to);
    }

    @Override
    public void setBroker(final Broker broker) {
	this.broker = broker;
    }

    @Override
    public void setRuntimeEstimator(final RuntimeEstimator runtimeEstimator) {
	this.estimator = runtimeEstimator;
    }

    @Override
    public boolean usesEstimation() {
	return true;
    }

    protected boolean toPostponeOrNotToPostpone(final Task task,
	    final long longestEstimatedRunTime, final long maxWaitTime, final boolean reachedMax) {
	return // postpone if
	       // maximum number of instances reached, OR
	reachedMax
	// this job already has a replica, OR
	// || task.getJob().getNumberOfActiveTasks() > 2
	// jobs is short, can wait, and hasn't been
	// postponed earlier
		|| longestEstimatedRunTime < 3600
		&& maxWaitTime > 0
		&& !(task.getState() == POSTPONED);
    }
}
