package org.optframework.core.parameters;

import java.util.ArrayList;
import java.util.List;

public class Solution {
    /**
     *  Integer array X is used to represent the assignment of tasks to instances.
     * The value of the ith element of this array specifies the index of instance to which this task is assigned.`
     * The length of X is equal to the number of tasks in the workflow, and it is equal to n
     */
    public List<Integer> xArray = new ArrayList<>();

    /**
     * Represents the type of instances used in assignment X where only spot-instances are employed to run the workflow
     */
    public List<Integer> yArray = new ArrayList<>();

    /**
     * Y prime represents the type of instances used in assignment X where only on-demand instances are utilized to run the workflow
     */
    public List<Integer> yPrimeArray = new ArrayList<>();

    /**
     * M, is the total elapsed time required to execute the entire workflow when only SIs are employed
     */
    public int makespan;

    /**
     * M prime, is the worst case makespan of the given workflow happening when all the spot-instances fail and we switch all of them to the on-demand instances
     */
    public int makespanPrime;

}
