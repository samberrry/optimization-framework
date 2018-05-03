package org.cloudbus.spotsim.main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math.util.FastMath;
import org.apache.commons.math.util.MathUtils;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.util.workload.Job;
import org.cloudbus.spotsim.broker.ModelParameters;
import org.cloudbus.spotsim.enums.BiddingStrategy;
import org.cloudbus.spotsim.enums.FaultToleranceMethod;
import org.cloudbus.spotsim.enums.InstanceType;
import org.cloudbus.spotsim.enums.OS;
import org.cloudbus.spotsim.enums.PriceForecastingMethod;
import org.cloudbus.spotsim.enums.Region;
import org.cloudbus.spotsim.enums.RuntimeEstimationMethod;
import org.cloudbus.spotsim.main.config.Config;
import org.cloudbus.spotsim.main.config.SimProperties;
import org.cloudbus.spotsim.simrecords.JobSummary;
import org.cloudbus.spotsim.simrecords.SimulationData;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.StreamException;
import com.thoughtworks.xstream.persistence.FilePersistenceStrategy;
import com.thoughtworks.xstream.persistence.XmlMap;

public class Results {

    private static final double CONFIDENCE = 0.95;

    public static final String RESULTS_DIR = "resources/output/simulationResults";

    private static Map<String, SimulationData> simDB;

    private static File dir = new File(RESULTS_DIR);

    private static boolean loaded = false;

    public static void main(final String[] args) throws Exception {

	Log.init();
	Config.load();
	// printJobSummariesCSV();
	// printInstanceTrace();
	computeBaseline();
	// dataPlots();
    }

    public static void persistResults(final String key) throws IOException {

	load();
	simDB.put(key, SimulationData.singleton());
	if (SimProperties.REPORT_DETAILED.asBoolean()) {
	    SimulationData.singleton().writeDBs();
	}
    }

    public static boolean alreadyExists(String uniqueKey) {

	load();
	if (simDB.containsKey(uniqueKey)) {
	    try {
		SimulationData simulationData = simDB.get(uniqueKey);
		return simulationData.getStats() != null;
	    } catch (StreamException se) {
		// input contained no data
		Set<Entry<String, SimulationData>> set = simDB.entrySet();
		for (Iterator<Entry<String, SimulationData>> it = set.iterator(); it.hasNext();) {
		    Entry<String, SimulationData> entry = it.next();
		    if (entry.getKey().equals(uniqueKey)) {
			it.remove();
		    }
		}
	    }
	}
	return false;
    }

    private static void computeBaseline() throws IOException {

	final DecimalFormat format = new DecimalFormat("#.##");
	final List<Job> jobList = Main.readWorkloadJobs().subList(0,
	    SimProperties.WORKLOAD_JOBS.asInt());
	ModelParameters.generateRandomParams(jobList);
	long totalTime = 0L;

	for (final Job job : jobList) {
	    totalTime += job.getLength();
	}

	System.out.println("Total jobs: " + jobList.size());
	System.out.println("Total hours: " + format.format(totalTime / 3600D));
	System.out.println("Single instance types");
	final InstanceType[] values = InstanceType.values();

	for (final InstanceType type : values) {
	    double totalTimePar = 0D;
	    for (final Job job : jobList) {
		final long length = job.getLength();
		totalTimePar += ModelParameters.execTimeParallel(job.getA(), job.getSigma(),
		    type.getEc2units(), length);
	    }
	    final double totalHours = totalTimePar / 3600D;
	    final double cost = totalHours
		    * type.getMinimumSpotPossible(Region.US_EAST, OS.LINUX)
		    / 2.833333333D;
	    System.out.println(type.name()
		    + " Cost for "
		    + format.format(totalHours)
		    + " hours: "
		    + format.format(cost));
	}

	System.out.println("Multiple instance types. spot");
	final Map<InstanceType, AtomicLong> hoursPerInstance = new HashMap<InstanceType, AtomicLong>();
	final Map<InstanceType, AtomicLong> numOfInstances = new HashMap<InstanceType, AtomicLong>();

	for (final Job job : jobList) {
	    final long length = job.getLength();
	    double bestDps = Double.MAX_VALUE;
	    final long bestTime = Long.MAX_VALUE;
	    InstanceType bestType = InstanceType.M1SMALL;
	    double timeOnInstance = 0;
	    for (final InstanceType type : values) {
		final double time = ModelParameters.execTimeParallel(job.getA(), job.getSigma(),
		    type.getEc2units(), length);
		final double dps = type.getMinimumSpotPossible(Region.US_EAST, OS.LINUX)
			/ 3600D
			* time;
		if (dps < bestDps || dps == bestDps && time < bestTime) {
		    bestDps = dps;
		    bestType = type;
		    timeOnInstance = time;
		}
	    }
	    AtomicLong t1 = hoursPerInstance.get(bestType);
	    AtomicLong t2 = numOfInstances.get(bestType);
	    if (t1 == null) {
		t1 = new AtomicLong(0L);
		hoursPerInstance.put(bestType, t1);
	    }
	    if (t2 == null) {
		t2 = new AtomicLong(0L);
		numOfInstances.put(bestType, t2);
	    }
	    t1.getAndAdd((long) Math.ceil(timeOnInstance));
	    t2.getAndIncrement();
	}

	double totalCostSpot = 0D;
	double totalOnDemand = 0D;
	for (final InstanceType type : values) {
	    final AtomicLong instanceHours = hoursPerInstance.get(type);
	    final AtomicLong num = numOfInstances.get(type);
	    if (instanceHours != null && num != null) {
		final double h = instanceHours.longValue() / 3600D;
		final double ondemand = h * type.getOnDemandPrice(Region.US_EAST, OS.LINUX);
		final double spot = h * type.getMinimumSpotPossible(Region.US_EAST, OS.LINUX);
		totalCostSpot += spot;
		totalOnDemand += ondemand;
		System.out.println(type.name()
			+ " "
			+ num
			+ " jobs. Cost for "
			+ format.format(h)
			+ " hours: on demand: "
			+ format.format(ondemand)
			+ ", spot: "
			+ format.format(spot));
	    }
	}
	System.out.println("Total cost: ondemand: "
		+ format.format(totalOnDemand)
		+ ", spot "
		+ format.format(totalCostSpot));
    }

    private static void speedUpOfAllJobs() throws IOException {
	final List<Job> allJobs = Main.readWorkloadJobs();
	final List<Job> jobList = allJobs.subList(0, SimProperties.WORKLOAD_JOBS.asInt());
	ModelParameters.generateRandomParams(jobList);
	for (final Job job : jobList) {
	    final double speedUp = ModelParameters.speedUp(job.getA(), job.getSigma(), 5);
	    final double execTimePar = job.getLength() / speedUp;

	    System.out.println("Job "
		    + job.getId()
		    + ", A: "
		    + job.getA()
		    + ", sigma: "
		    + job.getSigma()
		    + ", speedup: "
		    + speedUp
		    + ", exec par: "
		    + execTimePar);
	}
    }

    private static void append(final PrintWriter fcost, final int xaxis,
	    final FaultToleranceMethod _ft, final double mean, final double stddev,
	    final double confidence, int pess, BiddingStrategy bidd, int multi) {
	final String cstr = '"'
		+ _ft.shortName()
		+ '-'
		+ bidd
		+ '-'
		+ pess
		+ '-'
		+ multi
		+ "\" "
		+ xaxis
		+ ' '
		+ mean
		+ ' '
		+ stddev
		+ ' '
		+ confidence;
	System.out.println(cstr);
	fcost.println(cstr);
    }

    private static void appendInstanceTypes(final PrintWriter fninstances,
	    final RuntimeEstimationMethod _rt, final PriceForecastingMethod _fc,
	    final Map<InstanceType, DescriptiveStatistics> typesCount) {
	final StringBuilder b = new StringBuilder();

	b.append(_rt.shortName()).append('-').append(_fc.shortName()).append(' ');
	for (final InstanceType type : InstanceType.values()) {
	    b.append(typesCount.get(type).getMean()).append(' ');
	}
	System.out.println(b);
	fninstances.println(b.toString());
    }

    private static void dataPlots() throws IOException {

	if (simDB == null) {
	    load();
	}

	for (final Integer _jobs : Sweep.jobs) {
	    final PrintWriter fcost = new PrintWriter(new BufferedWriter(new FileWriter(new File(
		dir, "cost" + _jobs + ".dat"), false)));
	    final PrintWriter fddl = new PrintWriter(new BufferedWriter(new FileWriter(new File(
		dir, "deadline" + _jobs + ".dat"), false)));
	    final PrintWriter futil = new PrintWriter(new BufferedWriter(new FileWriter(new File(
		dir, "util" + _jobs + ".dat"), false)));
	    final PrintWriter fninstances = new PrintWriter(new BufferedWriter(new FileWriter(
		new File(dir, "ninstances" + _jobs + ".dat"), false)));
	    int xaxis = 1;
	    for (Integer _mult : Sweep.multipliers) {
		for (PriceForecastingMethod _fc : Sweep.fc) {
		    for (RuntimeEstimationMethod _rt : Sweep.rt) {
			for (final FaultToleranceMethod _ft : Sweep.ft) {
			    for (final BiddingStrategy _bidd : Sweep.bidd) {
				for (final Integer _pess : Sweep.pess) {
				    final DescriptiveStatistics cost = new DescriptiveStatistics();
				    final DescriptiveStatistics ddlBr = new DescriptiveStatistics();
				    final DescriptiveStatistics utilz = new DescriptiveStatistics();
				    for (final Long _seed : Sweep.seeds) {
					SimProperties.WORKLOAD_LENGTH_MULTIPLIER.set(_mult);
					SimProperties.SCHED_RUNTIME_ESTIMATION.set(_rt);
					SimProperties.PRICING_COST_FORECASTING_METHOD.set(_fc);
					SimProperties.FT_METHOD.set(_ft);
					SimProperties.SCHED_BIDDING_STRAT.set(_bidd);
					SimProperties.SCHED_PESSIMIST_FACTOR.set(_pess);
					SimProperties.RNG_SEED.set(_seed);

					final String sim = Config.uniqueKey();
					if (simDB.containsKey(sim)) {
					    final SimulationData simData = simDB.get(sim);
					    // System.out.println(simData.getStats());
					    cost.addValue(simData.getStats().getActualCost());
					    ddlBr
						.addValue(simData.getStats().getDeadlineBreaches());
					    utilz.addValue(simData.getStats().getUtilization());
					}
				    }

				    if (cost.getValues().length > 0) {
					System.out.println("COST");
					final double costConf = getConfidenceIntervalWidth(cost,
					    CONFIDENCE);
					append(fcost, xaxis, _ft,
					    MathUtils.round(cost.getMean(), 2),
					    MathUtils.round(cost.getStandardDeviation(), 2),
					    costConf, _pess, _bidd, _mult);
					System.out.println("DEADLINE");
					final double ddlConf = getConfidenceIntervalWidth(ddlBr,
					    CONFIDENCE);
					append(fddl, xaxis, _ft,
					    MathUtils.round(ddlBr.getMean(), 2),
					    MathUtils.round(ddlBr.getStandardDeviation(), 2),
					    ddlConf, _pess, _bidd, _mult);

					System.out.println("UTIL");
					final double utilzConf = getConfidenceIntervalWidth(utilz,
					    CONFIDENCE);
					append(futil, xaxis++, _ft,
					    MathUtils.round(utilz.getMean(), 2),
					    MathUtils.round(utilz.getStandardDeviation(), 2),
					    utilzConf, _pess, _bidd, _mult);
				    }
				}
			    }
			}
		    }
		}
	    }
	    final StringBuilder bu = new StringBuilder();
	    for (final InstanceType type : InstanceType.values()) {
		bu.append(type.getName()).append(' ');
	    }
	    // System.out.println(bu);
	    // fninstances.println(bu.toString());
	    // for (RuntimeEstimationMethod _rt : rt) {
	    // for (PriceForecastingMethod _fc : fc) {
	    // Map<InstanceType, DescriptiveStatistics> typesCount = new
	    // HashMap<InstanceType, DescriptiveStatistics>();
	    // for (String _seed : seeds) {
	    // String sim = SimProperties.key(_seed, _jobs, _rt.toString(),
	    // _fc.toString(), "false", "ONE");
	    // if (!simDB.containsKey(sim)) {
	    // System.err.println("Results for simulation : " + sim +
	    // " do not exist");
	    // break;
	    // }
	    // SimulationData simData = simDB.get(sim);
	    // Map<InstanceType, AtomicInteger> instanceTypesUsed =
	    // simData.getStats()
	    // .getInstanceTypesUsed();
	    // for (Entry<InstanceType, AtomicInteger> ins :
	    // instanceTypesUsed.entrySet()) {
	    // DescriptiveStatistics tstat = typesCount.get(ins.getKey());
	    // if (tstat == null) {
	    // tstat = new DescriptiveStatistics();
	    // typesCount.put(ins.getKey(), tstat);
	    // }
	    // tstat.addValue(ins.getValue().get());
	    // }
	    // }
	    // appendInstanceTypes(fninstances, _rt, _fc, typesCount);
	    // }
	    // }
	    fcost.close();
	    futil.close();
	    fddl.close();
	    fninstances.close();
	}

	System.out.println("DATAPLOTS DONE");
    }

    // private static void printInstanceTrace() throws IOException {
    //
    // StringBuilder comments = new StringBuilder();
    // comments.append("#Fields").append("\n");
    // comments.append("#1 Instance ID").append("\n");
    // comments.append("#2 Pricing scheme (SPOT, ON_DEMAND, RESERVED)").append("\n");
    // comments.append("#3 Type").append("\n");
    // comments.append("#4 Bid price").append("\n");
    // comments.append("#5 Launched at time").append("\n");
    // comments.append("#6 Terminated at time").append("\n");
    // comments.append("#7 Jobs run").append("\n");
    // comments.append("#8 Utilization").append("\n");
    // comments.append("#9 Idle periods: [from time:to time]").append("\n");
    //
    // load();
    //
    // Set<String> entries = simDB.keySet();
    //
    // for (String entry : entries) {
    // File f = new File(dir, entry + "-instance-trace.csv");
    // final Map<Integer, Instance> instances = simDB.get(entry).getInstances();
    // if (instances != null) {
    // System.out.println("Printing: " + f.getCanonicalPath());
    // Set<Integer> instanceKeys = instances.keySet();
    // PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(f)));
    // out.println(comments.toString());
    // for (Integer in : instanceKeys) {
    // Instance i = instances.get(in);
    // StringBuilder b = new StringBuilder();
    // b.append(in).append(',').append(i.getPricing()).append(',').append(i.getType())
    // .append(',').append(i.getBidPrice()).append(',').append(i.getAccStart())
    // .append(',').append(i.getAccEnd()).append(',').append(i.getJobsRun())
    // .append(',').append(i.utilization()).append(',');
    // List<IdlePeriod> idlePeriods = i.getIdlePeriods();
    // for (IdlePeriod ip : idlePeriods) {
    // b.append(ip);
    // }
    // final String csString = b.toString();
    // out.println(csString);
    // System.out.println(csString);
    // }
    // out.close();
    // } else {
    // System.out.println("Skipping: " + f.getCanonicalPath());
    // }
    // }
    // }

    private static double getConfidenceIntervalWidth(final DescriptiveStatistics stats,
	    final double significance) {
	final double k = 1.96;
	return k + stats.getStandardDeviation() / FastMath.sqrt(30);
    }

    @SuppressWarnings("unchecked")
    private static void load() {
	if (!loaded) {
	    if (!dir.exists()) {
		dir.mkdirs();
	    }
	    final XStream xstream = new XStream();
	    xstream.processAnnotations(SimulationData.class);

	    simDB = new XmlMap(new FilePersistenceStrategy(dir, xstream));
	    loaded = true;
	}
    }

    // XML 2 CSV
    private static void printJobSummariesCSV() throws IOException {

	final Set<String> entries = simDB.keySet();

	for (final String entry : entries) {

	    final File f = new File(dir, entry + ".csv");

	    if (!f.exists()) {

		final SimulationData simulationData = simDB.get(entry);

		System.out.println("Printing: " + entry);

		final PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(f)));

		final Map<Long, JobSummary> jobs = simulationData.getActiveJobs();
		for (final Long j : jobs.keySet()) {
		    final JobSummary jobSummary = jobs.get(j);
		    out.println(jobSummary.toString());
		}
	    }
	}
    }

    private static void printSimulationDataCSV() throws IOException {
	load();

	final Set<String> entries = simDB.keySet();

	for (final String entry : entries) {

	    final File f = new File(dir, entry + "-summary.csv");

	    if (!f.exists()) {

		final SimulationData simulationData = simDB.get(entry);
		System.out.println("Printing: " + entry);
		final PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(f)));
		out.println(simulationData.toString());
		out.close();
	    }
	}
    }
}
