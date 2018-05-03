package org.cloudbus.cloudsim.workflow;

import static org.cloudbus.spotsim.main.config.SimProperties.RNG_SEED;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.math.random.Well512a;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.util.workload.DeadlineBudgetBean;
import org.cloudbus.cloudsim.util.workload.Job;
import org.cloudbus.cloudsim.util.workload.Workflow;
import org.cloudbus.cloudsim.workflow.broker.WorkflowBroker;
import org.cloudbus.cloudsim.workflow.broker.WorkflowSchedulingPolicy;
import org.cloudbus.spotsim.broker.Broker;
import org.cloudbus.spotsim.broker.ModelParameters;
import org.cloudbus.spotsim.broker.SchedPolicy;
import org.cloudbus.spotsim.broker.resources.Resource;
import org.cloudbus.spotsim.broker.resources.ResourceState;
import org.cloudbus.spotsim.broker.rtest.FractionOfUserSupplied;
import org.cloudbus.spotsim.broker.rtest.Optimal;
import org.cloudbus.spotsim.broker.rtest.OptimalWithError;
import org.cloudbus.spotsim.broker.rtest.RandomEstimation;
import org.cloudbus.spotsim.broker.rtest.RecentAverage;
import org.cloudbus.spotsim.broker.rtest.UserSupplied;
import org.cloudbus.spotsim.cloudprovider.ComputeCloudImpl;
import org.cloudbus.spotsim.cloudprovider.ComputeCloudStub;
import org.cloudbus.spotsim.enums.InstanceType;
import org.cloudbus.spotsim.enums.Region;
import org.cloudbus.spotsim.enums.RuntimeEstimationMethod;
import org.cloudbus.spotsim.enums.WorkflowPolicies;
import org.cloudbus.spotsim.main.Results;
import org.cloudbus.spotsim.main.config.Config;
import org.cloudbus.spotsim.main.config.SimProperties;
import org.cloudbus.spotsim.pricing.PriceTraceGen;
import org.cloudbus.spotsim.pricing.db.PriceDB;
import org.cloudbus.spotsim.pricing.distr.RandomDistribution;
import org.cloudbus.spotsim.pricing.distr.RandomDistributionManager;
import org.cloudbus.spotsim.simrecords.SimulationData;

import org.cloudbus.cloudsim.workflow.Models.DAX.Dax2Workflow;

public class StartWorkflowMain {
	
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
	Log.init("cloudsim.log");
	System.out.println("Simulation starting, properties: " + propertiesFile);
	Config.load(propertiesFile);
	SimulationData.singleton().setProps(Config.getProps());
	
	FileWriter fw = new FileWriter(getOutputFileName());
	PrintWriter out = new PrintWriter(fw);

	ArrayList<DeadlineBudgetBean> readDeadlineBudget = null;
    if( SimProperties.WF_INPUT_FILE_MODE.asBoolean()){
    	readDeadlineBudget = readDeadlineBudget();
	}else{
		readDeadlineBudget = new ArrayList<DeadlineBudgetBean>();
		DeadlineBudgetBean element = new DeadlineBudgetBean(SimProperties.WORKFLOW_DEADLINE.asLong(), SimProperties.WORKFLOW_BUDGET.asDouble());
		readDeadlineBudget.add(element);
	}
    printHeadersforOutputFile(out);
   WorkflowPolicies[] values = {WorkflowPolicies.NEW_LIBERAL, WorkflowPolicies.NEW_CONSERVATIVE, WorkflowPolicies.NEW_BASELINE, WorkflowPolicies.NEW_BASELINE_SPOT, WorkflowPolicies.CONSERVATIVE_NAIVE, WorkflowPolicies.LIBERAL_NAIVE};
  // WorkflowPolicies[] values = {WorkflowPolicies.NEW_LIBERAL};
    for(WorkflowPolicies wfPol : values){
			for (DeadlineBudgetBean oneEntry : readDeadlineBudget) {
				for (int i = 0; i < SimProperties.WF_NUMBER_OF_EXECUTIONS
						.asInt(); i++) {
					
					CloudSim.init(1, null, true);

					// Set the Config seed based on the iteration count
					SimProperties.RNG_SEED.set((long)i);

					System.out.println("Simulation starting, properties: " + propertiesFile);
					Config.load(propertiesFile);
					
					SimulationData.singleton().setProps(Config.getProps());

					if (!SimProperties.REPORT_REDO_EXISTING_RESULTS.asBoolean()
							&& Results.alreadyExists(Config.uniqueKey())) {
						Log.logger.warning("Exiting. Simulation " + Config.uniqueKey()+ " has already been done");
						System.exit(1);
					}

					try {
						runReplicaSim(oneEntry, wfPol.getClassName());
						outputResults(out, oneEntry, wfPol.getPolicyName(), i, false);
					} catch (NullPointerException e) {
						System.out.println(" Did i can the exception");
						outputResults(out, oneEntry, wfPol.getPolicyName(), i, true);
						e.printStackTrace();
					}
					
					SimulationData.resetStats();
					PriceDB.flushPriceDB();
					if(SimProperties.PRICING_TRACE_GEN.asEnum() == PriceTraceGen.RANDOM){
					RandomDistributionManager.flush();
					}
					//Resetting resource nextID, so that resources id start from 0
					Resource.setNextID(0);
				}
			}
    }
    closeFile(fw,out);
    }

    private static void printHeadersforOutputFile(PrintWriter out) {
    	//Workflow, policy, roundNumber, deadline, budget
    	out.print("WORKFLOW");
    	out.print(",");
		out.print("POLICY");
		out.print(",");
		out.print("ROUND");
		out.print(",");
		out.print("DEADLINE");
		out.print(",");
		out.print("BUDGET");
		out.print(",");
		//cost, runtime, numb of ondemand, on-demand details, number of spot, spot details, bidding history
		out.print("COST");
		out.print(",");
		out.print("MAKESPAN");
		out.print(",");
		out.print("FAILURES");
		out.print(",");
		out.print("OD NUM");
		out.print(",");
		out.print("OD DETAILS");
		out.print(",");
		out.print("SPOT");
		out.print(",");
		out.print("SPOT DETAILS");
		out.print(",");
		out.println("SPOT BIDS");
		
	}

	private static void outputResults(PrintWriter out, DeadlineBudgetBean oneEntry, String policy, int round, boolean exceptionFlag) {
    	
    	String daxFile = new File(SimProperties.WORKFLOW_FILE_DAG.toString()).getName();
    	int indexval = daxFile.lastIndexOf(".");
    	indexval = daxFile.lastIndexOf(".");
    	//Standard outputs
    	//Workflow, policy, roundNumber, deadline, budget
    	out.print(daxFile.substring(0,indexval));
    	out.print(",");
		out.print(policy);
		out.print(",");
		out.print(round);
		out.print(",");
		out.print(oneEntry.getDeadline());
		out.print(",");
		out.print(oneEntry.getBudget());
		out.print(",");
		if(!exceptionFlag){
		//cost, runtime, numb of ondemand, on-demand details, number of spot, spot details, bidding history
		out.print(SimulationData.singleton().getStats().getActualCost());
		out.print(",");
		out.print(SimulationData.singleton().getStats().getFinishTime());
		out.print(",");
		out.print(SimulationData.singleton().getStats().getJobsFailed());
		out.print(",");
		out.print(getNumInstance(SimulationData.singleton().getStats().getODInstanceTypesUsed()));
		out.print(",");
		out.print(getInstanceDetails(SimulationData.singleton().getStats().getODInstanceTypesUsed()));
		out.print(",");
		out.print(getNumInstance(SimulationData.singleton().getStats().getspotInstanceTypesUsed()));
		out.print(",");
		out.print(getInstanceDetails(SimulationData.singleton().getStats().getspotInstanceTypesUsed()));
		out.print(",");
		out.println(SimulationData.singleton().getStats().getSpotPriceHist());
		}
		else{
			out.print("NS");
			out.print(",");
			out.print("NS");
			out.println(",");
		}
	}

	private static String getInstanceDetails(
			Map<InstanceType, AtomicInteger> odInstanceTypesUsed) {
		StringBuilder instanceDetails = new StringBuilder();
		for(InstanceType type : odInstanceTypesUsed.keySet()){
			int numInst = odInstanceTypesUsed.get(type).intValue();
			if(numInst > 0){
				instanceDetails.append(type.toString());
				instanceDetails.append("=");
				instanceDetails.append(numInst);
				instanceDetails.append(":");
			}
		}
		return instanceDetails.toString();
	}

	private static String getNumInstance(
			Map<InstanceType, AtomicInteger> odInstanceTypesUsed) {
		Integer totalNumInstances = 0;
		for(InstanceType type : odInstanceTypesUsed.keySet()){
			totalNumInstances += odInstanceTypesUsed.get(type).intValue();
		}
		return totalNumInstances.toString();
	}

	public static void closeFile(FileWriter fw, PrintWriter out){
		try {
			//Flush the output to the file
		   out.flush();
		       
		   //Close the Print Writer
		   out.close();
		       
		   //Close the File Writer
		  
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}       
	}
    
    private static String getOutputFileName() {
    	String outputFileName = new String();
    	String daxFile = new File(SimProperties.WORKFLOW_FILE_DAG.toString()).getName();
    	int indexval = daxFile.lastIndexOf(".");
    	indexval = daxFile.lastIndexOf(".");
    	outputFileName += SimProperties.WORKFLOW_OUTPUT_DIRECTORY.asString();
		outputFileName += daxFile.substring(0,indexval);
		outputFileName += "_";
		outputFileName += "alpha";
		outputFileName += SimProperties.RISK_FACTOR_ALPHA.asString();
		outputFileName += "_";
		outputFileName += "beta";
		outputFileName += SimProperties.BUFFER_FACTOR_BETA.asString();
		outputFileName += "_";
		outputFileName += "thresold";
		outputFileName += SimProperties.FAILURE_THRESHOLD.asString();
		outputFileName += "_";
		outputFileName += "ChrgPrd";
		outputFileName += SimProperties.PRICING_CHARGEABLE_PERIOD.asString();
		outputFileName += "_";
		outputFileName += "VMINIT";
		outputFileName += SimProperties.DC_VM_INIT_TIME.asString();
		outputFileName += ".csv";
		//outputFileName += Config.formatDate(Config.getSimPeriodStart());
		
		System.out.println("Output File name " + outputFileName );
		
		return outputFileName;
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
    
    
    public static ArrayList<DeadlineBudgetBean> readDeadlineBudget(){

		ArrayList<DeadlineBudgetBean> deadlineBudgetList = new ArrayList<DeadlineBudgetBean>();
		//File file = new File("C:\\newInputFile.csv");
		//File file = new File("C:\\inputFile.csv");

		deadlineBudgetList.add(new DeadlineBudgetBean(700 , 1000));

//		File file = new File(SimProperties.WORKFLOW_INPUT_FILE.asString());
		 /**
		try {
			BufferedReader bufRdr  = new BufferedReader(new FileReader(file));
			String line = null;	
			 
			//read each line of text file
			while((line = bufRdr.readLine()) != null)
			{
				StringTokenizer st = new StringTokenizer(line,",");
				while (st.hasMoreTokens())
				{
					//get next token and store it in the array
					String nextToken = st.nextToken();
					long deadline = Long.parseLong(nextToken);
					
					nextToken = st.nextToken();
					double budget = Double.parseDouble(nextToken);
					
					DeadlineBudgetBean element = new DeadlineBudgetBean(deadline, budget);
					
					deadlineBudgetList.add(element);
				}
			}

		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		return deadlineBudgetList;
		
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

    private static void runReplicaSim(DeadlineBudgetBean oneEntry, String policyName) throws Exception {
    	
//    Workflow simpleWorkflow = readWorkflowJobs(SimProperties.WORKFLOW_FILE_DAG.asString());
//    simpleWorkflow.initBudget(oneEntry.getBudget());
//    simpleWorkflow.setDeadline(oneEntry.getDeadline());

		Workflow simpleWorkflow = new Workflow(6, 1000, 1000);
		/**
		 * Defining a simple workflow A->B, A->C, B->D, B->E, D->F, E->F, C->F
		 * */

		int groupID = 1;
		int userID = 1;
		long submitTime = 0 ;
		long len = 100;
		int numProc = 2;
		long reqRunTime = 5000;

		int taskID = 0;

		Job wfA = new Job(taskID, submitTime, reqRunTime , userID, groupID, len, numProc);
		simpleWorkflow.createTask(wfA);
		taskID++;

		Job wfB = new Job(taskID, submitTime, reqRunTime , userID, groupID, len, numProc);
		simpleWorkflow.createTask(wfB);
		taskID++;

		Job wfC = new Job(taskID, submitTime, reqRunTime , userID, groupID, len, numProc);
		simpleWorkflow.createTask(wfC);
		taskID++;

		Job wfD = new Job(taskID, submitTime, reqRunTime , userID, groupID, len, numProc);
		simpleWorkflow.createTask(wfD);
		taskID++;

		Job wfE = new Job(taskID, submitTime, reqRunTime , userID, groupID, len, numProc);
		simpleWorkflow.createTask(wfE);
		taskID++;

		Job wfF = new Job(taskID, submitTime, reqRunTime , userID, groupID, len, numProc);
		simpleWorkflow.createTask(wfF);
		taskID++;

		simpleWorkflow.addEdge(wfA, wfB, 10);
		simpleWorkflow.addEdge(wfA, wfC, 10);
		simpleWorkflow.addEdge(wfB, wfD, 10);
		simpleWorkflow.addEdge(wfB, wfE, 10);
		simpleWorkflow.addEdge(wfD, wfF, 10);
		simpleWorkflow.addEdge(wfE, wfF, 10);
		simpleWorkflow.addEdge(wfC, wfF, 10);




		System.out.println("Deadline " + oneEntry.getDeadline() + " Budget " + oneEntry.getBudget());
	
	List<Job> sublist = simpleWorkflow.getJobList();

	final String key = Config.uniqueKey();
	System.out.println("Starting SPOTSIM: " + key);

	final ComputeCloudImpl cloud = new ComputeCloudImpl(5000,
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
	    .forName(policyName);
	final Constructor<? extends WorkflowSchedulingPolicy> constructor = c.getConstructor(new Class<?>[] {});
	final WorkflowSchedulingPolicy policy = constructor.newInstance();

	Log.logger.info(Log.clock() + "Loading policy " + policyName);

	// Create a client
	final WorkflowBroker broker = new WorkflowBroker("client1", serverStub, policy, simpleWorkflow);
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
	//SimulationData.singleton().getProfiler().computeTotals();
	Results.persistResults(key);
    }

    private static void runSim() {
	CloudSim.startSimulation();
	CloudSim.stopSimulation();
    }
	

}
