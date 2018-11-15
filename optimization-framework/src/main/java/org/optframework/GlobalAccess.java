package org.optframework;

import org.optframework.core.Job;

import java.util.List;

/**
 * all of the static properties should be initialized
 * */

public class GlobalAccess {
    /**
     * ordered job list based on ranks
     * */
    public static List<Job> orderedJobList;
    /**
     * e.g: the value of the index 3 denotes that how many parents the task with Id=3 has
     * */
    public static int numberOfParentsList[];

    /**
     * maximum level of the workflow
     * */
    public static int maxLevel;
}
