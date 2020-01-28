package org.optframework.automator;

import org.optframework.core.Solution;
import java.util.ArrayList;

/**
 * Holds results from a complete run in one automator iteration
 * */

public class RunResult {
    public ArrayList<Solution> solutionArrayListToCSV;
    public ArrayList<Long> timeInMilliSecArrayList;

    public RunResult(ArrayList<Solution> solutionArrayListToCSV, ArrayList<Long> timeInMilliSecArrayList) {
        this.solutionArrayListToCSV = solutionArrayListToCSV;
        this.timeInMilliSecArrayList = timeInMilliSecArrayList;
    }
}
