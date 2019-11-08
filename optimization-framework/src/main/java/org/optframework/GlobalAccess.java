package org.optframework;

import org.optframework.core.Job;
import org.optframework.core.Solution;
import java.util.ArrayList;
import java.util.List;

/**
 * all of the static properties should be initialized
 * */

public class GlobalAccess {
    /**
     * Ordered job list based on ranks
     * */
    public static List<Job> orderedJobList;
    /**
     * E.g: the value of the index 3 denotes that how many parents the task with Id=3 has
     * */
    public static int numberOfParentsList[];

    /**
     * Maximum level of the workflow
     * */
    public static int maxLevel;

    /**
     * Generated solutions during running an algorithm is stored here
     * */
    public static ArrayList<Solution> solutionRepository;

    /**
     * Contains the latest generated solution from the latest running algorithm
     * */
    public static Solution latestSolution;

    /**
     * These variables are used in budget automator
     * Array of solutions which should be printed to csv file
     */
    public static ArrayList<Solution> solutionArrayListToCSV;
    public static ArrayList<Long> timeInMilliSecArrayList;

    public static String workflowName;
}
