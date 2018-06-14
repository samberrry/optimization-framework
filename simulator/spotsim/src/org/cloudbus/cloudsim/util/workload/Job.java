/*
 * Title:        GridSim Toolkit
 * Description:  GridSim (Grid Simulation) Toolkit for Modeling and Simulation
 *               of Parallel and Distributed Systems such as Clusters and Grids
 * License:      GPL - http://www.gnu.org/copyleft/gpl.html
 */

package org.cloudbus.cloudsim.util.workload;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

import org.cloudbus.spotsim.broker.resources.Resource;
import org.cloudbus.spotsim.enums.AZ;
import org.cloudbus.spotsim.enums.InstanceType;
import org.cloudbus.spotsim.enums.PriceModel;

/**
 * This class represents job read from a a workload trace file.
 * 
 * @author Marcos Dias de Assuncao
 * @since 5.0
 * 
 */
public class Job implements Serializable {

    private static final long serialVersionUID = 9088429638005084161L;

    public enum JobStatus {
	COMPLETED,
	NEW,
	RUNNING,
	TIME_LAPSED;
    }

    private static Comparator<Job> submitTimeCompare = new Comparator<Job>() {

	@Override
	public int compare(final Job o1, final Job o2) {
	    return new Long(o1.getSubmitTime()).compareTo(o2.getSubmitTime());
	}
    };

    private double a;

    private double budget;

    private long completionTime;

    private long deadline;

    private long estimatedLength = -1;

    private final int groupID;

    private long id;

    private long length;

    private int nextTaskID = 0;

    private int numProc;

    private long reqRunTime;

    private double sigma;

    private long startTime;

    private JobStatus status;

    private long submitTime;

    private final List<Task> tasks;

    private long timeTaken;

    private final int userID;
    
    private List<WFEdge> edgeInfo;
    
    private long criticalPathWeight;
    
    private long latestStartTime = 0;
    
    private long latestFinishTime = 0;
    
    private boolean flagLST = false;
    
    private long parentEdgeWeight;

    /**
     * Creates a new object.
     * 
     * @param id
     *        TODO
     * @param subTime
     *        the time at which the job should be submitted
     * @param reqRunTime
     * @param userID
     * @param groupID
     * @param length
     *        TODO
     * @param numProc
     *        TODO
     */
    public Job(final long id, final long subTime, final long reqRunTime, final int userID,
	    final int groupID, final long length, final int numProc) {
	this.submitTime = subTime;
	this.reqRunTime = reqRunTime;
	this.userID = userID;
	this.groupID = groupID;
	setLength(length);
	this.id = id;
	setNumProc(numProc);
	this.tasks = new ArrayList<Task>();
	this.edgeInfo = new ArrayList<>();
    }

    public static Comparator<? super Job> submitTimeCompare() {
	return submitTimeCompare;
    }

    public void checkSanity() {
    }

    public long getTransferTime(int taskID){
    	long transferTime = 0;
    	
    	for(WFEdge e : edgeInfo){
    		if(e.getToTaskID() == taskID){
    			transferTime = e.getTransferTime();
    		}
    	}
    	return transferTime;
    }
    
    @Override
    public boolean equals(final Object obj) {
	if (this == obj) {
	    return true;
	}
	if (obj == null) {
	    return false;
	}
	if (this.getClass() != obj.getClass()) {
	    return false;
	}
	final Job other = (Job) obj;
	if (this.id != other.id) {
	    return false;
	}
	return true;
    }

    public double getA() {
	return this.a;
    }

    public double getBudget() {
	return this.budget;
    }

    public long getCompletionTime() {
	return this.completionTime;
    }

    public long getDeadline() {
	return this.deadline;
    }

    public final long getEstimatedLength() {
	return this.estimatedLength;
    }

    public int getGroupID() {
	return this.groupID;
    }

    public long getId() {
    	return this.id;
    }
    
    public int getIntId() {
    	return (int)this.id;
    }

    public EnumSet<AZ> getAZInUse() {

	final EnumSet<AZ> ret = EnumSet.noneOf(AZ.class);
	for (final Task t : this.tasks) {
	    final Resource resource = t.getResource();
	    if (!t.getState().finished() && resource != null) {
		ret.add(resource.getAz());
	    }
	}
	return ret;
    }

    public EnumSet<InstanceType> getInstanceTypesUsed() {

	final EnumSet<InstanceType> ret = EnumSet.noneOf(InstanceType.class);
	for (final Task t : this.tasks) {
	    final Resource resource = t.getResource();
	    if (!t.getState().finished() && resource != null) {
		ret.add(resource.getType());
	    }
	}
	return ret;
    }

    public long getLength() {
	return this.length;
    }

    public int getNumberOfActiveTasks() {
	int ret = 0;
	for (final Task t : this.tasks) {
	    if (!t.getState().finished()) {
		ret++;
	    }
	}
	return ret;
    }
    
    public HashMap<InstanceType, Double> getOutOfBidTasksInfo() {
    	HashMap<InstanceType, Double> outOfBidTaskInfo = new HashMap<>();
    	for (final Task t : this.tasks) {
    	    Resource resource = t.getResource();
			if ( resource.getPriceModel() == PriceModel.SPOT && t.getState() == TaskState.FAILED) {
				if(!outOfBidTaskInfo.containsKey(resource.getType())){
					outOfBidTaskInfo.put(resource.getType(), resource.getBid());
				} else {
					Double existingBid = outOfBidTaskInfo.get(resource.getType());
					if(existingBid < resource.getBid()){
						outOfBidTaskInfo.put(resource.getType(), resource.getBid());
					}
				}
    	    }
    	}
    	return outOfBidTaskInfo;
    }

    public int getNumberOfTasks() {
	return this.tasks.size();
    }

    public int getNumProc() {
	return this.numProc;
    }

    public long getReqRunTime() {
	return this.reqRunTime;
    }

    public double getSigma() {
	return this.sigma;
    }

    public long getStartTime() {
	return this.startTime;
    }

    public JobStatus getStatus() {
	return this.status;
    }

    public long getSubmitTime() {
	return this.submitTime;
    }

    public List<Task> getTasks() {
	return this.tasks;
    }

	public long getTimeTaken() {
		return timeTaken;
	}

	public int getUserID() {
	return this.userID;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + (int) (this.id ^ this.id >>> 32);
	return result;
    }

    public Task newTask() {
	final Task newTask = new Task(this, getNextTaskID());
	this.tasks.add(newTask);
	return newTask;
    }

    public void resetTasks() {
	final int runningTasks = getNumberOfActiveTasks();
	if (runningTasks > 0) {
	    throw new RuntimeException("Cannot reset job "
		    + getId()
		    + " "
		    + runningTasks
		    + " tasks are still running");
	}
	this.tasks.clear();
	this.nextTaskID = 0;
    }

    public void setA(final double a) {
	this.a = a;
    }

    public void setBudget(final double budget) {
	this.budget = budget;
    }

    public void setCompletionTime(final long completionTime) {
	this.completionTime = completionTime;
    }

    public void setDeadline(final long deadline) {
	this.deadline = deadline;
    }

    public void setEstimatedLength(final long estimatedLength) {
	this.estimatedLength = estimatedLength;
    }

    public void setLength(final long length) {
	this.length = length;
    }

    public void setNumProc(final int numProc) {
	this.numProc = numProc;
    }

    public void setReqRunTime(final long reqRunTime) {
	this.reqRunTime = reqRunTime;
    }

    public void setSigma(final double sigma) {
	this.sigma = sigma;
    }

    public void setStartTime(final long startTime) {
	this.startTime = startTime;
    }

    public void setStatus(final JobStatus status) {
	this.status = status;
    }
    
    public boolean isAssigned(){
    	if(this.status == JobStatus.COMPLETED || this.status == JobStatus.RUNNING){
    		return true;
    	}else{
    		return false;
    	}
    }

	public void setTimeTaken(long timeTaken) {
		this.timeTaken = timeTaken;
	}

	public long getEdge(int taskID){
		for(WFEdge edge : edgeInfo){
			if(taskID == edge.getToTaskID()){
				return edge.getTransferTime();
			}
		}
		return 0;
	}
    
    public void setEdge(long transferTime){
		for(WFEdge edge : edgeInfo){
				edge.setTransferTime(transferTime);
		}
	}
	
	public void addEdge(int taskID, long transTime){
		WFEdge edge = new WFEdge(taskID, transTime);
		edgeInfo.add(edge);
	}

    @Override
    public String toString() {
	final StringBuilder builder = new StringBuilder();
	builder.append("Job ").append(this.id).append(", submitTime=").append(this.submitTime)
	    .append(", scheduledStartTime=").append(", deadline=").append(this.deadline)
	    .append("]");
	return builder.toString();
    }

    private int getNextTaskID() {
	return this.nextTaskID++;
    }

	public void setSubmitTime(long submitTime) {
		this.submitTime = submitTime;
	}

	public long getCriticalPathWeight() {
		return criticalPathWeight;
	}

	public void setCriticalPathWeight(long criticalPathWeight) {
		//System.out.println(" Job ID " + this.getIntId() + " CPW " + criticalPathWeight);
		this.criticalPathWeight = criticalPathWeight;
	}

	public long getLatestStartTime() {
		return latestStartTime;
	}

	public void setLatestStartTime(long latestStartTime) {
		this.latestStartTime = latestStartTime;
	}
	
	public void incrTaskIDonEdges(){
		for(WFEdge edge : edgeInfo){
			edge.incrTaskId();
		}
	}
	public void decrTaskIDonEdges(){
		for(WFEdge edge : edgeInfo){
			edge.decrTaskId();
		}
	}
	public void incrId(){
		this.id++;
	}
	public void decrId(){
		this.id--;
	}

	public boolean isFlagLST() {
		return flagLST;
	}

	public void setFlagLST(boolean flagLST) {
		this.flagLST = flagLST;
	}

	public long getLatestFinishTime() {
		return latestFinishTime;
	}

	public void setLatestFinishTime(long latestFinishTime) {
		this.latestFinishTime = latestFinishTime;
	}

	public long getParentEdgeWeight() {
		return parentEdgeWeight;
	}

	public void setParentEdgeWeight(long parentEdgeWeight) {
		this.parentEdgeWeight = parentEdgeWeight;
	}

//	HESSAM ADDED
	public List<WFEdge> getEdgeInfo() {
		return edgeInfo;
	}
}