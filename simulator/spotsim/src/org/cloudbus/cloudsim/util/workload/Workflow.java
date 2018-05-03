package org.cloudbus.cloudsim.util.workload;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.spotsim.enums.InstanceType;

public class Workflow {
	
	WorkflowDAG wfDAG;
	public Map<Integer, Integer> taskToCloudletID;
	
	/** Contains all jobs */
    private List<Job> jobList;
	
	private int numberTasks;
	private long deadline;
	private double budget;
	private double remainingBudget;
	final Map<InstanceType, ArrayList<Job>> criticalPathJobMap;
	
	public Workflow(Map<Integer, Integer> taskToCloudletID, long deadline, double budget){
		this.taskToCloudletID = taskToCloudletID;
		this.deadline = deadline;
		this.budget = budget;
		this.jobList = new ArrayList<>();
		setWfDAG(new WorkflowDAG(numberTasks));
		this.remainingBudget = budget;
		this.criticalPathJobMap = new HashMap<InstanceType, ArrayList<Job>>();
	}
	
	public Workflow(long deadline, double budget){
		this.deadline = deadline;
		this.budget = budget;
		this.jobList = new ArrayList<>();
		setWfDAG(new WorkflowDAG(numberTasks));
		this.remainingBudget = budget;
		this.criticalPathJobMap = new HashMap<InstanceType, ArrayList<Job>>();
	}
	
	public Workflow(int numTasks, long deadline, double budget){
		this.numberTasks = numTasks;
		this.deadline = deadline;
		this.budget = budget;
		this.jobList = new ArrayList<>();
		setWfDAG(new WorkflowDAG(numberTasks));
		this.remainingBudget = budget;
		this.criticalPathJobMap = new HashMap<InstanceType, ArrayList<Job>>();
	}
	
	public Workflow(int numJobs) {
		this.numberTasks = numJobs;
		this.jobList = new ArrayList<>();
		setWfDAG(new WorkflowDAG(numberTasks));
		this.criticalPathJobMap = new HashMap<InstanceType, ArrayList<Job>>();
	}

	public void addEdge(Job wfTaskA, Job wfTaskB, long transferTime) {
		
		this.wfDAG.addEdge(wfTaskA, wfTaskB);
		
		wfDAG.adj(wfTaskA.getIntId()).getNodeInfo().addEdge(wfTaskB.getIntId(), transferTime);
	}

	public void createTask(Job wftInfo){
		
		//create tasknode in workflow digraph // get Node will return null if no edge is created
		getWfDAG().adj(wftInfo.getIntId()).setNodeInfo(wftInfo);
		
		jobList.add(wftInfo);
		
	}
	
	public void initBudget(double budget){
		setBudget(budget);
		setRemainingBudget(budget);
	}
	
	public void computeRemainingBudget(double cost){
		setRemainingBudget(this.remainingBudget - cost);
	}

	public WorkflowDAG getWfDAG() {
		return wfDAG;
	}

	public void setWfDAG(WorkflowDAG wfDAG) {
		this.wfDAG = wfDAG;
	}
	
	public Map<Integer, Integer> getTaskToCloudletID() {
		return taskToCloudletID;
	}
	public void setTaskToCloudletID(Map<Integer, Integer> taskToCloudletID) {
		this.taskToCloudletID = taskToCloudletID;
	}
	public long getDeadline() {
		return deadline;
	}
	
	public double getBudget() {
		return budget;
	}
	

	public List<Job> getJobList() {
		return jobList;
	}

	public void setJobList(List<Job> jobList) {
		this.jobList = jobList;
	}

	public double getRemainingBudget() {
		return remainingBudget;
	}

	public void setRemainingBudget(double remainingBudget) {
		this.remainingBudget = remainingBudget;
	}

	public void setDeadline(long deadline) {
		this.deadline = deadline;
	}

	public void setBudget(double budget) {
		this.budget = budget;
	}
	
}
