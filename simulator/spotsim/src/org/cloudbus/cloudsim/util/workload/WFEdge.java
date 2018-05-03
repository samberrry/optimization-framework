package org.cloudbus.cloudsim.util.workload;

public class WFEdge {
	
	private int toTaskID;
	private long transferTime;
	
	public WFEdge(int toTaskID, long transferTime) {
		super();
		this.toTaskID = toTaskID;
		this.transferTime = transferTime;
	}
	
	public int getToTaskID() {
		return toTaskID;
	}
	public void setToTaskID(int toTaskID) {
		this.toTaskID = toTaskID;
	}
	public long getTransferTime() {
		return transferTime;
	}
	public void setTransferTime(long transferTime) {
		this.transferTime = transferTime;
	}
	public void incrTaskId(){
		this.toTaskID++;
	}
	public void decrTaskId(){
		this.toTaskID--;
	}
}
