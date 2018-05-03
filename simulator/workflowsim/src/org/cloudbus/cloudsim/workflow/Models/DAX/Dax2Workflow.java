package org.cloudbus.cloudsim.workflow.Models.DAX;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Random;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.cloudbus.cloudsim.util.workload.Job;
import org.cloudbus.cloudsim.util.workload.Workflow;
import org.cloudbus.spotsim.main.config.SimProperties;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class Dax2Workflow extends DefaultHandler{

	protected int ownerId;
	private long baseMIPS;
	protected Random random;
	
	/*Data structures filled during XML parsing*/
	public ArrayList<Job> jobs;
	
	public Workflow workflow;
	
	/**
	 * Reads the file specified as input, and processes the corresponding DAG, generating
	 * internal representation of provisioning and scheduling decision. WorkflowEngine
	 * queries for such an information to process the DAG.
	 *   
	 * @param dagFile Name of the DAG file.
	 */
	public void processDagFile(String dagFile, int ownerId, long baseMIPS, long seed){
		this.ownerId = ownerId;
		this.baseMIPS = baseMIPS;
		this.random = new Random(seed);
		
		this.jobs = new ArrayList<Job>();
		
		SAXParserFactory spf = SAXParserFactory.newInstance();
		try {
			SAXParser sp = spf.newSAXParser();
			sp.parse(dagFile, this);		
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	/********************************** SAX-related methods ****************************************/
	public static Job currentJob;
	public static int jobCount;
	public static int dataItemCont;
	public static Hashtable<String,Job> jobMap;
	public static Hashtable<Integer, Long> outputData;
	public static int numJobs;
	
	public void startDocument(){

		currentJob=null;
		jobCount=0;
		dataItemCont=0;
		jobMap = new Hashtable<String,Job>();
		outputData = new Hashtable<>();
		
	}
	
	public void startElement(String uri, String localName, String qName, Attributes attributes){
		/*
		 * Elements can be one of: 'adag' 'job' 'uses' 'child' 'parent'
		 */
		int groupID = 1;
		int userID = 1;
		long submitTime = 0 ;
		int numProc = 1;
		
		if(qName.equalsIgnoreCase("adag")){//nothing to be done
			numJobs = Integer.parseInt(attributes.getValue("jobCount"));
			
			//Initiate the workflow with Jobcount number of jobs
			this.workflow = new Workflow(numJobs);
			
		} else if(qName.equalsIgnoreCase("job")){//a new task is being declared
			String id = attributes.getValue("id");
			String runtime = attributes.getValue("runtime");
					
			//Length of a job is like Million Instructions and it will not change for a cloudlet, depending on the Instance type
			//running times will change
			double jobRunTime = Double.parseDouble(runtime);
			// Make sure the DAG time are in seconds or milliseconds .. but mainly not in double
			long jobRunTimeSec = (long) Math.ceil(jobRunTime);
	
			//Jobs less than a second is converted in a onesec
			if(jobRunTimeSec <= 0){
				jobRunTimeSec = 1;
			}
			//running in seconds only
			long jobRunTimeLength = jobRunTimeSec * SimProperties.WORKFLOW_LENGTH_MULT.asLong();
				
			Job job = new Job(jobCount, submitTime, jobRunTimeLength, userID, groupID, jobRunTimeLength, numProc);
			workflow.createTask(job);

			jobMap.put(id, job);
			jobs.add(job);
			
			currentJob = job;
			
			jobCount++;
		} else if(qName.equalsIgnoreCase("uses")){//a file dependency from the current task
			String link = attributes.getValue("link");
			String size = attributes.getValue("size");
			
			
			if(link.equalsIgnoreCase("output")){
				long sizeInBytes = Long.parseLong(size);
				long sizeInKb = sizeInBytes/1024;
				if(outputData.containsKey(currentJob.getIntId())){
					//As the DAX does not clear mention which output data is for which child task
					//We sum all the output data and assign the total output data to all child tasks as transfer time
					Long oldData = outputData.get(currentJob.getIntId());
					outputData.put(currentJob.getIntId(), sizeInKb+oldData);
				}else{
					outputData.put(currentJob.getIntId(), sizeInKb);
				}
			}
			
		} else if(qName.equalsIgnoreCase("child")){//a task that depends on other(s)
			String ref = attributes.getValue("ref");
			currentJob = jobMap.get(ref);
		} else if(qName.equalsIgnoreCase("parent")){//a task that others depend on
			String ref = attributes.getValue("ref");
			Job parentJob = jobMap.get(ref);
			Long outputDataSize = outputData.get(parentJob.getIntId());
			
			//System.out.println("Adding Edge from "+parentJob.getIntId() + " to " + currentJob.getIntId() + " DTT " + outputDataSize);
			workflow.addEdge(parentJob,currentJob, outputDataSize/SimProperties.DATACENTER_BANDWIDTH.asLong());

		} else {
			System.out.println("WARNING: Unknown XML element:"+qName);
		}
	}
		
	public void endDocument(){
		//parsing is completed. Cleanup auxiliary data structures and run the actual DAG provisioning/scheduling
		jobMap.clear();
			
	}
}
