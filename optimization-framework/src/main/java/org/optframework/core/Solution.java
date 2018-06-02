package org.optframework.core;

import org.cloudbus.cloudsim.util.workload.Job;
import org.cloudbus.cloudsim.util.workload.Workflow;
import org.optframework.config.StaticProperties;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class Solution implements StaticProperties {
    int id;
    /**
     * Cost of the solution
     * */
    int cost;
    /**
     *  Integer array X is used to represent the assignment of tasks to instances.
     * The value of the ith element of this array specifies the index of instance to which this task is assigned.`
     * The length of X is equal to the number of tasks in the workflow, and it is equal to n
     */
    public int xArray[];

    /**
     * Represents the type of instances used in assignment X where only spot-instances are employed to run the workflow
     */
    public int yArray[];

    /**
     * Y prime represents the type of instances used in assignment X where only on-demand instances are utilized to run the workflow
     */
    public int yPrimeArray[];

    /**
     * M, is the total elapsed time required to execute the entire workflow when only SIs are employed
     */
    public int makespan;

    public int numberOfUsedInstances;

    public int visited;

    /**
     * M prime, is the worst case makespan of the given workflow happening when all the spot-instances fail and we switch all of them to the on-demand instances
     */
    public int makespanPrime;

    public Solution(int numberOfTasks, int numberOFInstances) {
        xArray = new int[numberOfTasks];
        yArray = new int[numberOFInstances];
        yPrimeArray = new int[numberOFInstances];
    }

    void generateRandomSolution(Workflow workflow){
        List<Job> jobList = workflow.getJobList();

        /**
         * Generates random xArray
         * */
        Random r = new Random();
        int bound = 0;

//        It always assigns task 0 (first task) to instance 0 (first instance)
        xArray[jobList.get(0).getIntId()] = bound;

        bound++;
        for (int i = 1; i < jobList.size(); i++) {
            Job job = jobList.get(i);
            int random = r.nextInt(bound + 1);

            xArray[job.getIntId()] = random;

            if (bound == random && bound < M_NUMBER){
                bound++;
            }
        }

        /**
         * Generate random yArray
         * */
        numberOfUsedInstances = bound;

        for (int i = 0; i < numberOfUsedInstances; i++) {
            int random = r.nextInt(N_TYPES);
            yArray[i] = random;
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Solution solution = (Solution) o;
        return id == solution.id &&
                Arrays.equals(xArray, solution.xArray) &&
                Arrays.equals(yArray, solution.yArray);
    }

    @Override
    public int hashCode() {

        int result = Objects.hash(id);
        result = 31 * result + Arrays.hashCode(xArray);
        result = 31 * result + Arrays.hashCode(yArray);
        return result;
    }
}
