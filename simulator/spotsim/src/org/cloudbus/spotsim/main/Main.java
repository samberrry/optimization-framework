package org.cloudbus.spotsim.main;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.util.workload.Job;
import org.cloudbus.cloudsim.util.workload.WorkloadFileReader;
import org.cloudbus.spotsim.broker.Broker;
import org.cloudbus.spotsim.broker.ModelParameters;
import org.cloudbus.spotsim.broker.SchedPolicy;
import org.cloudbus.spotsim.broker.rtest.FractionOfUserSupplied;
import org.cloudbus.spotsim.broker.rtest.Optimal;
import org.cloudbus.spotsim.broker.rtest.OptimalWithError;
import org.cloudbus.spotsim.broker.rtest.RandomEstimation;
import org.cloudbus.spotsim.broker.rtest.RecentAverage;
import org.cloudbus.spotsim.broker.rtest.UserSupplied;
import org.cloudbus.spotsim.cloudprovider.ComputeCloudImpl;
import org.cloudbus.spotsim.cloudprovider.ComputeCloudStub;
import org.cloudbus.spotsim.enums.Region;
import org.cloudbus.spotsim.enums.RuntimeEstimationMethod;
import org.cloudbus.spotsim.main.config.Config;
import org.cloudbus.spotsim.main.config.SimProperties;
import org.cloudbus.spotsim.simrecords.SimulationData;

public class Main {

    private static final String PROP_OPTION = "properties";

    private static String propertiesFile;

    private static Options options;

    public static void main(final String[] args) throws Exception {

	TimeZone.setDefault(TimeZone.getTimeZone("GMT"));

	try {
	    parseCLI(args);
	} catch (final ParseException pe) {
	    System.err.println("Invalid command arguments: " + pe.getMessage());
	    final HelpFormatter formatter = new HelpFormatter();
	    formatter.printHelp("spotsim", options);
	    System.exit(1);
	}

	CloudSim.init(1, null, true);

	System.out.println("Simulation starting, properties: " + propertiesFile);

	Config.load(propertiesFile);
	SimulationData.singleton().setProps(Config.getProps());

	if (!SimProperties.REPORT_REDO_EXISTING_RESULTS.asBoolean()
		&& Results.alreadyExists(Config.uniqueKey())) {
	    Log.logger.warning("Exiting. Simulation "
		    + Config.uniqueKey()
		    + " has already been done");
	    System.exit(1);
	}

	// computeBaseline();

	// speedUpOfAllJobs();

	runReplicaSim();

	// Results.persistResults(SimProperties.uniqueKey());
    }

    private static void parseCLI(final String[] args) throws ParseException {
	OptionBuilder.withArgName("file");
	OptionBuilder.hasArg();
	OptionBuilder.withDescription("Properties file containing simulation parameters");
	final Option option = OptionBuilder.create(PROP_OPTION);
	options = new Options();
	options.addOption(option);

	final CommandLineParser parser = new GnuParser();
	final CommandLine line = parser.parse(options, args);

	propertiesFile = line.getOptionValue(PROP_OPTION);
    }

    /*
     * Reads a workload trace in the SWF format
     */
    public static List<Job> readWorkloadJobs() {
	final WorkloadFileReader reader = new WorkloadFileReader(
	    SimProperties.WORKLOAD_FILE.asString(), 1);

	reader.setComment(";");

	final List<Job> generatedWorkload = reader.generateWorkload();

	return generatedWorkload;
    }

    private static void runReplicaSim() throws Exception {

	List<Job> allJobs = readWorkloadJobs();
	final List<Job> sublist = new ArrayList<Job>(allJobs.subList(0,
	    SimProperties.WORKLOAD_JOBS.asInt()));
	allJobs = null;
	// List<Job> sublist = allJobs;

	final String key = Config.uniqueKey();
	System.out.println("Starting SPOTSIM: " + key);

	final ComputeCloudImpl cloud = new ComputeCloudImpl(500,
	    (Region) SimProperties.DC_DEFAULT_REGION.asEnum());
	final ComputeCloudStub serverStub = new ComputeCloudStub(cloud);

	final Iterator<Job> iterator = sublist.iterator();
	while (iterator.hasNext()) {
	    final Job j = iterator.next();
	    if (j.getLength() <= 0) {
		iterator.remove();
	    }
	}

	// Instantiate policy
	@SuppressWarnings("unchecked")
	final Class<? extends SchedPolicy> c = (Class<? extends SchedPolicy>) Class
	    .forName(SimProperties.SCHED_POLICY_CLASS.asString());
	final Constructor<? extends SchedPolicy> constructor = c.getConstructor(new Class<?>[] {});
	final SchedPolicy policy = constructor.newInstance();

	Log.logger.info(Log.clock() + "Loading policy " + SimProperties.SCHED_POLICY_CLASS);

	// Create a client
	final Broker broker = new Broker("client1", serverStub, policy);
	ModelParameters.generateRandomParams(sublist);
	broker.addJobs(sublist);

	switch ((RuntimeEstimationMethod) SimProperties.SCHED_RUNTIME_ESTIMATION.asEnum()) {
	case OPTIMAL:
	    policy.setRuntimeEstimator(new Optimal());
	    break;
	case ALMOST_OPTIMAL:
	    policy.setRuntimeEstimator(new OptimalWithError());
	    break;
	case RECENT_AVERAGE:
	    policy.setRuntimeEstimator(new RecentAverage());
	    break;
	case USER_SUPPLIED:
	    policy.setRuntimeEstimator(new UserSupplied());
	    break;
	case USER_SUPPLIED_FRACTION:
	    policy.setRuntimeEstimator(new FractionOfUserSupplied());
	    break;
	case RANDOM:
	    policy.setRuntimeEstimator(new RandomEstimation());
	    break;
	}

	policy.setBroker(broker);

	// Go go go
	runSim();

	System.out.println("Summary: " + SimulationData.singleton().getStats());

	// Grab simulation statistics
	System.out.println("Ran for date: " + Config.formatDate(Config.getSimPeriodStart()));
	SimulationData.singleton().getProfiler().computeTotals();
	Results.persistResults(key);
    }

    private static void runSim() {
	CloudSim.startSimulation();
	CloudSim.stopSimulation();
    }
}