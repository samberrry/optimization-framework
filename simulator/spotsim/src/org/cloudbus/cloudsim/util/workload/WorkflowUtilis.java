package org.cloudbus.cloudsim.util.workload;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.util.workload.Job.JobStatus;
import org.cloudbus.spotsim.broker.ModelParameters;
import org.cloudbus.spotsim.enums.InstanceType;
import org.cloudbus.spotsim.enums.OS;
import org.cloudbus.spotsim.enums.Region;
import org.cloudbus.spotsim.main.config.SimProperties;
import org.cloudbus.spotsim.pricing.db.PriceDB;


public class WorkflowUtilis {

	Workflow workflow;
	long LTO;
	double priceOnDemand;
	boolean LTOSwitch;
	

	public WorkflowUtilis(Workflow workflow) {
		super();
		this.workflow = workflow;
		this.priceOnDemand = 10;
		this.LTOSwitch = true;
	}
	
	public long computeLTO(int taskId){
		computeCriticalPath(taskId);
		setLTO(workflow.getDeadline() - getCriticalPath());
		return getLTO();
	}
	
	public long computeLTO(int taskId, InstanceType type){
		computeCriticalPath(taskId, type);
		setLTO(workflow.getDeadline() - getCriticalPath());
		return getLTO();
	}
	
	public long reComputeLTO(){
		setLTO(workflow.getDeadline() - getCriticalPath());
		return getLTO();
	}
	
	public double computeBudgetLTO(int taskId){
		return (getCriticalPath()/SimProperties.PRICING_CHARGEABLE_PERIOD.asLong())*PriceDB.getOnDemandPrice(Region.DEEPAK_TEST, InstanceType.M1SMALL, OS.LINUX);	
	}
	
	public boolean isCriticalNode(Integer node){
		WorkflowDAG dag = workflow.getWfDAG();
		Job criticalElement = null;
		for(Integer n : dag.getLastLevel()){
			criticalElement = findCriticalElement(dag, criticalElement, n);
		}
		if(criticalElement.getIntId() == node){
			return true;
		}
		while(criticalElement!= null && criticalElement.getIntId() != node){
			int cpEID = criticalElement.getIntId();
			ArrayList<Integer> parents = dag.getParents(cpEID);
			if(!parents.isEmpty()){
				for(Integer p : parents){
					criticalElement = findCriticalElement(dag, criticalElement, p);
				}
				if(criticalElement.getIntId() == node){
					return true;
				}
			}else{
				criticalElement = null;
			}
		}
		return false;
	}

	private Job findCriticalElement(WorkflowDAG dag, Job criticalElement,
			Integer n) {
		long criticalPath = 0;
		Job job = dag.getNode(n);
		long cpw = job.getCriticalPathWeight();
		if(criticalPath <= cpw){
			criticalPath = cpw;
			criticalElement = job;
		}
		return criticalElement;
	}
	
	public ArrayList<Job> getCriticalPathJobs(InstanceType type){
		return this.workflow.criticalPathJobMap.get(type);
	}
	
	public long getCriticalPathTime(InstanceType type){
		long criticalPathTime =0;
		Job prevJob = null;
		long edgeTime = 0L;
		ArrayList<Job> criticalPathJobs = this.workflow.criticalPathJobMap.get(type);
		for(Job job : criticalPathJobs){
			if(null != job){
				if(prevJob != null){
					edgeTime = job.getEdge(prevJob.getIntId());
					edgeTime = (edgeTime > 0) ? edgeTime : prevJob.getEdge(job.getIntId());
				}
	    		criticalPathTime += ModelParameters.execTimeParallel(job.getA(), job.getSigma(), 
	    				type.getEc2units(), job.getLength()) + edgeTime;
	    		prevJob = job;
			}
    	}
		return criticalPathTime;
	}
	
	public boolean isCriticalPathJob(Job job){
		Iterator it = this.workflow.criticalPathJobMap.entrySet().iterator();
		if(null == job){
			return false;
		}
	    while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        ArrayList<Job> jobList = (ArrayList<Job>)pairs.getValue();
	        if(null != jobList && !jobList.isEmpty()){
				for (Job j : jobList) {
					if ( null != j && j.getId() == job.getId()) {
						return true;
					}
				}
	        }
	    }
	    return false;
	}
	
	public void removeCritcalPathFromMap(Job job){
		Iterator it = this.workflow.criticalPathJobMap.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        ArrayList<Job> jobList = (ArrayList<Job>)pairs.getValue();
	        if(null != jobList || !jobList.isEmpty()){
	        	jobList.remove(job);
	        }
	    }
	}
	
	public void recomputeCritcalPathFromMap(Job job){
		Iterator it = this.workflow.criticalPathJobMap.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        ArrayList<Job> jobList = (ArrayList<Job>)pairs.getValue();
	        if(null != jobList || !jobList.isEmpty()){
	        	if(jobList.contains(job)){
	        		jobList=computeCriticalPathJobs((InstanceType)pairs.getKey());
	        		this.workflow.criticalPathJobMap.put((InstanceType)pairs.getKey(), jobList);
	        	}
	        }
	    }
	}
	public void setCriticalPathJobs(InstanceType type, ArrayList<Job> criticalPathJobs){
		this.workflow.criticalPathJobMap.put(type, criticalPathJobs);
	}
	
	public ArrayList<Job> computeCriticalPathJobs(InstanceType type){
		ArrayList<Job> criticalPath = new ArrayList<>();
		WorkflowDAG dag = workflow.getWfDAG();
		long cp = 0;
		Job firstCP=null;
		computeCriticalPath(dag.getFirstLevel().get(0), type);
		for(Integer node : dag.getLastLevel()){
			Job job = dag.getNode(node);
			long cpw = job.getCriticalPathWeight();
			if(cp < cpw){
				cp = cpw;
				firstCP = job;
			}
		}
		criticalPath.add(firstCP);
		
		if(null != firstCP){
			while (!dag.getParents(firstCP.getIntId()).isEmpty()) {
				cp = 0;
				final Job counter = firstCP;
				for (Integer node : dag.getParents(firstCP.getIntId())) {
					Job job = dag.getNode(node);
					long cpw = job.getCriticalPathWeight();
					if (cp < cpw) {
						cp = cpw;
						firstCP = job;
					}
				}
				if(counter == firstCP){
					break;
				}
				criticalPath.add(firstCP);
			}
		}
		return criticalPath;
	}
	
	
	
	public ArrayList<Job> getCriticalPathJobs(){
		ArrayList<Job> criticalPath = new ArrayList<>();
		WorkflowDAG dag = workflow.getWfDAG();
		long cp = 0;
		Job firstCP=null;
		for(Integer node : dag.getLastLevel()){
			Job job = dag.getNode(node);
			long cpw = job.getCriticalPathWeight();
			if(cp < cpw){
				cp = cpw;
				firstCP = job;
			}
		}
		criticalPath.add(firstCP);
		
		if(null != firstCP){
			while (!dag.getParents(firstCP.getIntId()).isEmpty()) {
				cp = 0;
				final Job counter = firstCP;
				for (Integer node : dag.getParents(firstCP.getIntId())) {
					Job job = dag.getNode(node);
					long cpw = job.getCriticalPathWeight();
					if (cp < cpw) {
						cp = cpw;
						firstCP = job;
					}
				}
				if(counter == firstCP){
					break;
				}
				criticalPath.add(firstCP);
			}
		}
		return criticalPath;
	}

	public long getCriticalPath(){
		WorkflowDAG dag = workflow.getWfDAG();
		long criticalPath = 0;
		for(Integer node : dag.getLastLevel()){
			Job job = dag.getNode(node);
			long cpw = job.getCriticalPathWeight();
			if(criticalPath < cpw){
				criticalPath = cpw;
			}
		}
		return criticalPath;
	}

	public void computeCriticalPath(Integer node){
		WorkflowDAG dag = workflow.getWfDAG();
		if(dag.getFirstLevel().contains(node)){
			for(Integer i : dag.getFirstLevel()){
			Job job = dag.getNode(i);
			if(job.getStatus()!= JobStatus.COMPLETED){
				job.setCriticalPathWeight(job.getLength());
			}
			}
		}
		if(dag.getChildren(node).size()==0){
			Job job = dag.getNode(node);
			return;
		}else{
			for(Integer child: dag.getChildren(node)){
				long cpWeight = setCriticalPathWeight(dag, child);
				computeCriticalPath(child);
			}
		}
	}
	
	public long executionTimeonType(Job job, InstanceType type){
		return ModelParameters.execTimeParallel(job.getA(),
				job.getSigma(), type.getEc2units(), job.getLength());
	}
	
	public void computeCriticalPath(Integer node, InstanceType type){
		WorkflowDAG dag = workflow.getWfDAG();
		if(dag.getFirstLevel().contains(node)){
			Job job = dag.getNode(node);
			if(job.getStatus()!= JobStatus.COMPLETED){
				job.setCriticalPathWeight(executionTimeonType(job, type));
			}
		}
		if(dag.getChildren(node).size()==0){
			Job job = dag.getNode(node);
			return;
		}else{
			for(Integer child: dag.getChildren(node)){
				long cpWeight = setCriticalPathWeight(dag, child, type);
				computeCriticalPath(child, type);
			}
		}
	}
	
	public void computeLFT(long deadline){
		WorkflowDAG dag = workflow.getWfDAG();
				
		for(Integer lastNode : dag.getLastLevel()){
			dag.getNode(lastNode).setLatestFinishTime(deadline);
			computeNodeLFT(lastNode, dag, deadline);
		}
		
	}
	
	public void computeNodeLFT(Integer node, WorkflowDAG dag, long deadline){
		ArrayList<Integer> parents = dag.getParents(node);
		//long jobLength = dag.getNode(node).getLength();
		
		if(parents.isEmpty()){
			return;
		}else{
			for(Integer parent : parents){
				dag.computeLFT(parent, deadline);
				computeNodeLFT(parent, dag, deadline);
			}
		}
	}
	
	public void computeLST(Integer node){
		WorkflowDAG dag = workflow.getWfDAG();
		/*if(dag.getNode(node).isFlagLST()){
			return;
		}*/
		if(dag.getParents(node).isEmpty()){
			
			Job nodeJob = dag.getNode(node);
			long nodeLST = dag.getMaxCriticalPathWeightFirstLevel() - nodeJob.getCriticalPathWeight();
			nodeJob.setLatestStartTime(nodeLST);
			return;
		}else{
			Integer parentId=0;
			long cp=0;
			for(Integer parent : dag.getParents(node)){
				final Job parentJob = dag.getNode(parent);
				if(cp < parentJob.getCriticalPathWeight()){
					cp = parentJob.getCriticalPathWeight();
					parentId = parent;
				}
			}
			Job maxWeight = dag.getNode(parentId);
			for(Integer parent : dag.getParents(node)){
				final Job parentJob = dag.getNode(parent);
			//	if(!parentJob.isFlagLST()){
				long maxWght = maxWeight.getCriticalPathWeight() - parentJob.getCriticalPathWeight();
				parentJob.setLatestStartTime(maxWght);
				parentJob.setFlagLST(true);
				computeLST(parent);
			//	}
			}
			
		}
	}
	
	public ArrayList<Job> getJobsLongestPath(Integer node){
		ArrayList<Job> longestPathJobs = new ArrayList<Job>();
		WorkflowDAG dag = workflow.getWfDAG();
		Job sinkNode = dag.getNode(node);
		longestPathJobs.add(sinkNode);
		ArrayList<Integer> childList = new ArrayList<Integer>();
		childList.add(node);
		ArrayList<Integer> allChildren = dag.getAllChildren(childList);
		if(allChildren.isEmpty()){
			return longestPathJobs;
		}
		ArrayList<Integer> lastLevel = dag.getLastLevel();
		lastLevel.retainAll(allChildren);
		long criticalPathweight = 0;
		Job sourceNode = null;
		for(Integer i : lastLevel){
			Job node2 = dag.getNode(i);
			if(criticalPathweight < node2.getCriticalPathWeight()){
				criticalPathweight = node2.getCriticalPathWeight();
				sourceNode = node2;
			}
		}
		ArrayList<Integer> parentList = new ArrayList<Integer>(sourceNode.getIntId());
		parentList.add(sourceNode.getIntId());
		ArrayList<Integer> allParents = dag.getAllParents(parentList);
		allChildren.retainAll(allParents);
		allChildren.add(sourceNode.getIntId());
		Integer iterator = node;
		while(!dag.getChildren(iterator).isEmpty()){
			Job criticalChild = dag.getCriticalChild(iterator, allChildren);
			if(criticalChild != null){
				longestPathJobs.add(criticalChild);
				iterator = criticalChild.getIntId();
			}
		}
		return longestPathJobs;
	}
	
	public void recomputeCriticalPath(Integer node, long weight){
		WorkflowDAG dag = workflow.getWfDAG();
		ArrayList<Integer> childrenNodes = dag.getChildren(node);
		if(childrenNodes.isEmpty()){
			return;
		}
		for(Integer child : childrenNodes){
			Job childJob = dag.getNode(child);
			Job criticalParent = dag.getCriticalParent(child);
			long cpParentWeight = criticalParent.getCriticalPathWeight();
			if(criticalParent.getStatus() == JobStatus.RUNNING){
				cpParentWeight = criticalParent.getDeadline() - CloudSim.clock();
			}
			long cpw = cpParentWeight + criticalParent.getEdge(child)+ childJob.getLength();
			if(cpw < 0 ){
				cpw = 0;
			}
			childJob.setCriticalPathWeight(cpw);
			recomputeCriticalPath(child, weight);
		}
	}
	
	public void recomputeCriticalPath(Integer node, long weight, InstanceType type){
		WorkflowDAG dag = workflow.getWfDAG();
		ArrayList<Integer> childrenNodes = dag.getChildren(node);
		if(childrenNodes.isEmpty()){
			return;
		}
		for(Integer child : childrenNodes){
			Job childJob = dag.getNode(child);
			Job criticalParent = dag.getCriticalParent(child);
			long cpParentWeight = criticalParent.getCriticalPathWeight();
			if(criticalParent.getStatus() == JobStatus.RUNNING){
				cpParentWeight = criticalParent.getDeadline() - CloudSim.clock();
			}
			long cpw = cpParentWeight + criticalParent.getEdge(child)+ executionTimeonType(childJob, type);
			if(cpw < 0 ){
				cpw = 0;
			}
			childJob.setCriticalPathWeight(cpw);
			recomputeCriticalPath(child, weight, type);
		}
	}
	
	private long setCriticalPathWeight(WorkflowDAG dag, Integer child) {
		long tempMax = 0;
		Job childJob = dag.getNode(child);
		for(Integer parent : dag.getParents(child)){
			long time = 0;
			Job parentJob = dag.getNode(parent);
			time = parentJob.getEdge(child) + parentJob.getCriticalPathWeight();
			if (tempMax < time) {
				tempMax = time;
			}
		}
		if(childJob.getStatus() != JobStatus.COMPLETED){
			childJob.setCriticalPathWeight(tempMax+childJob.getLength());
		}
		return childJob.getCriticalPathWeight();
	}
	
	private long setCriticalPathWeight(WorkflowDAG dag, Integer child, InstanceType type) {
		long tempMax = 0;
		Job childJob = dag.getNode(child);
		for(Integer parent : dag.getParents(child)){
			long time = 0;
			Job parentJob = dag.getNode(parent);
			time = parentJob.getEdge(child) + parentJob.getCriticalPathWeight();
			if (tempMax < time) {
				tempMax = time;
			}
		}
		if(childJob.getStatus() != JobStatus.COMPLETED){
			childJob.setCriticalPathWeight(tempMax+executionTimeonType(childJob, type));
		}
		return childJob.getCriticalPathWeight();
	}

	public Workflow getWorkflow() {
		return workflow;
	}

	public void setWorkflow(Workflow workflow) {
		this.workflow = workflow;
	}

	public long getLTO() {
		return LTO;
	}

	public void setLTO(long lTO) {
		LTO = lTO;
	}

	public double getPriceOnDemand() {
		return priceOnDemand;
	}

	public void setPriceOnDemand(double priceOnDemand) {
		this.priceOnDemand = priceOnDemand;
	}

	public boolean isLTOSwitch() {
		return LTOSwitch;
	}

	public void setLTOSwitch(boolean lTOSwitch) {
		LTOSwitch = lTOSwitch;
	}
	
}
