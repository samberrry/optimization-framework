package org.cloudbus.cloudsim.workflow;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
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
import org.cloudbus.cloudsim.util.workload.Workflow;
import org.cloudbus.cloudsim.workflow.Models.DAX.Dax2Workflow;
import org.cloudbus.cloudsim.workflow.broker.WorkflowBroker;
import org.cloudbus.cloudsim.workflow.broker.WorkflowFailureBroker;
import org.cloudbus.cloudsim.workflow.broker.WorkflowSchedulingPolicy;
import org.cloudbus.cloudsim.workflow.failure.FailureBroker;
import org.cloudbus.cloudsim.workflow.failure.NewWeibullFailureModel;
import org.cloudbus.cloudsim.workflow.failure.NoFailureModel;
import org.cloudbus.cloudsim.workflow.failure.RandomTaskFailureModel;
import org.cloudbus.cloudsim.workflow.failure.TaskFailureGenerator;
import org.cloudbus.cloudsim.workflow.failure.WeibullFailureModel;
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
import org.cloudbus.spotsim.main.Results;
import org.cloudbus.spotsim.main.config.Config;
import org.cloudbus.spotsim.main.config.SimProperties;
import org.cloudbus.spotsim.simrecords.SimulationData;

public class WorkflowFailureMain {
	
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
    public static Workflow readWorkflowJobs(String dagFile) {
    	
    	
    	Dax2Workflow dax = new Dax2Workflow();
    	dax.processDagFile(dagFile, 1, 100,0);		
    //	dax.workflow.getWfDAG().printDAG();

		return dax.workflow;
    }

    private static void runReplicaSim() throws Exception {
    	
   /*	String dagFile = "leadadas.xml";
    //String dagFile = "C:\\Documents and Settings\\deepakc\\My Documents\\cloudsim-workflows.tar\\cloudsim-workflows\\CyberShake_1000.xml";*/
   // Workflow simpleWorkflow = new Workflow(6, 130, 50);
	/**
	 * Defining a simple workflow A->B, B->C, B->D, B->E, E ->F
	 * */
	/*int taskID =0;
	int groupID = 1;
	int userID = 1;
	long submitTime = 0 ;
	long len = 100;
	int numProc = 1;
	long reqRunTime = 5000;
	
		Job wfA = new Job(taskID, submitTime, reqRunTime , userID, groupID, 100, numProc);
		simpleWorkflow.createTask(wfA);
		taskID++;
		
		Job wfB = new Job(taskID, submitTime, reqRunTime , userID, groupID, 200, numProc);
		simpleWorkflow.createTask(wfB);
		taskID++;
		
		Job wfC = new Job(taskID, submitTime, reqRunTime , userID, groupID, 100, numProc);
		simpleWorkflow.createTask(wfC);
		taskID++;
		
		Job wfD = new Job(taskID, submitTime, reqRunTime , userID, groupID, 300, numProc);
		simpleWorkflow.createTask(wfD);
		taskID++;
		
		Job wfE = new Job(taskID, submitTime, reqRunTime , userID, groupID, 500, numProc);
		simpleWorkflow.createTask(wfE);
		taskID++;
		
		Job wfF = new Job(taskID, submitTime, reqRunTime , userID, groupID, 200, numProc);
		simpleWorkflow.createTask(wfF);
		taskID++;
		
			
		simpleWorkflow.addEdge(wfA, wfB, 10);
		simpleWorkflow.addEdge(wfB, wfC, 10);
		simpleWorkflow.addEdge(wfB, wfD, 10);
		simpleWorkflow.addEdge(wfB, wfE, 10);
		simpleWorkflow.addEdge(wfE, wfF, 10);*/
		
	
	//String dagFile = "C:\\Documents and Settings\\deepakc\\My Documents\\cloudsim-workflows.tar\\cloudsim-workflows\\CyberShake_30.xml";
	Workflow simpleWorkflow = readWorkflowJobs(SimProperties.WORKFLOW_FILE_DAG.asString());
    simpleWorkflow.initBudget(SimProperties.WORKFLOW_BUDGET.asDouble());
    simpleWorkflow.setDeadline(SimProperties.WORKFLOW_DEADLINE.asLong());
    //simpleWorkflow.getWfDAG().printDAG();
	List<Job> allJobs = simpleWorkflow.getJobList();
	
	List<Job> sublist = allJobs;
	
	GregorianCalendar startDate = SimProperties.SIM_START_TIME.asDate();
	System.out.println(" Start Date " + startDate.getTime() + " Random num "+ 26160);
	startDate.add(Calendar.MINUTE, 26160);
	SimProperties.SIM_START_TIME.set(startDate);
		
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
	final Class<? extends WorkflowSchedulingPolicy> c = (Class<? extends WorkflowSchedulingPolicy>) Class
	    .forName(SimProperties.SCHED_POLICY_CLASS.asString());
	final Constructor<? extends WorkflowSchedulingPolicy> constructor = c.getConstructor(new Class<?>[] {});
	final WorkflowSchedulingPolicy policy = constructor.newInstance();

	Log.logger.warning(Log.clock() + "Loading policy " + SimProperties.SCHED_POLICY_CLASS);

	// Create a client
	final WorkflowFailureBroker broker = new WorkflowFailureBroker("client1", serverStub, policy, simpleWorkflow);
	ModelParameters.generateRandomParams(sublist);
	broker.addJobs(sublist);
	
	final TaskFailureGenerator taskFailureGen = new NoFailureModel(broker);
	final FailureBroker failureBroker = new FailureBroker(taskFailureGen, broker);
	broker.setFailureBroker(failureBroker);
	
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