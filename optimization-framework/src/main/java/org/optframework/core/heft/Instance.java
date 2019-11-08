package org.optframework.core.heft;

import java.util.ArrayList;

/**
 *  PURPOSE: is used to store gap information
 * */

public class Instance {
    /**
     * gapList contains the gap list of the instance
     * */
    public ArrayList<Gap> gapList = new ArrayList<>();
    public boolean hasGap;
    /**
     * During runtime this property gets negative values
     * */
    public int lastGapId = 0;
    /**
     * taskGapList contains the list of tasks and gaps together in order. Negative values are gaps
     * */
    public ArrayList<Integer> taskGapList = new ArrayList<>();

    public ArrayList<Gap> getGapList() {
        return gapList;
    }

}
