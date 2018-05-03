package org.cloudbus.cloudsim.util.workload;

public class DeadlineBudgetBean {
	
	private long deadline;
	private double budget;
		
	public DeadlineBudgetBean(long deadline, double budget) {
		super();
		this.deadline = deadline;
		this.budget = budget;
	}
	
	@Override
	public String toString() {
		return "DeadlineBudgetBean [deadline=" + deadline + ", budget="
				+ budget + "]";
	}

	public long getDeadline() {
		return deadline;
	}
	public void setDeadline(long deadline) {
		this.deadline = deadline;
	}
	public double getBudget() {
		return budget;
	}
	public void setBudget(double budget) {
		this.budget = budget;
	}

}
