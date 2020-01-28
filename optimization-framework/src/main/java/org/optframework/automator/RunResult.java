package org.optframework.automator;

import org.optframework.core.Solution;
import java.util.ArrayList;

/**
 * Holds results from a complete run in one automator iteration
 * */

public class RunResult {
    public ArrayList<Solution> solutionArrayListToCSV = new ArrayList<>();
    public ArrayList<Long> timeInMilliSecArrayList = new ArrayList<>();
}
