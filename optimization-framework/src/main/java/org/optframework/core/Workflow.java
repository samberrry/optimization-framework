package org.optframework.core;

import org.cloudbus.cloudsim.util.workload.WorkflowDAG;

import java.util.List;

/**
 * @author Hessam Modaberi
 * @since 2018
 * */

public class Workflow {
    WorkflowDAG wfDAG;

    /** Contains all jobs */
    private List<Job>jobList;

    private int numberTasks;
    private long deadline;
    private double budget;
    private double beta;

    public Workflow(WorkflowDAG wfDAG, List<Job> jobList, int numberTasks, long deadline, double budget, double beta) {
        this.wfDAG = wfDAG;
        this.jobList = jobList;
        this.numberTasks = numberTasks;
        this.deadline = deadline;
        this.budget = budget;
        this.beta = beta;
    }

    public void initBudget(double budget){
        setBudget(budget);
    }

    public WorkflowDAG getWfDAG() {
        return wfDAG;
    }

    public void setWfDAG(WorkflowDAG wfDAG) {
        this.wfDAG = wfDAG;
    }

    public double getBudget() {
        return budget;
    }

    public void setBudget(double budget) {
        this.budget = budget;
    }

    public int getNumberTasks() {
        return numberTasks;
    }

    public void setNumberTasks(int numberTasks) {
        this.numberTasks = numberTasks;
    }

    public long getDeadline() {
        return deadline;
    }

    public void setDeadline(long deadline) {
        this.deadline = deadline;
    }

    public double getBeta() {
        return beta;
    }

    public void setBeta(double beta) {
        this.beta = beta;
    }

    public List<Job> getJobList() {
        return jobList;
    }

    public void setJobList(List<Job> jobList) {
        this.jobList = jobList;
    }
}
