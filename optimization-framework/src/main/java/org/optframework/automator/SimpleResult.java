package org.optframework.automator;

/**
 * This class holds necessary data for a single result
 * */

public class SimpleResult {
    public double budget;
    public double cost;
    public int makespan;
    public double fitness;
    public long milli;

    public SimpleResult(double budget, double cost, int makespan, double fitness, long milli) {
        this.budget = budget;
        this.cost = cost;
        this.makespan = makespan;
        this.fitness = fitness;
        this.milli = milli;
    }
}
