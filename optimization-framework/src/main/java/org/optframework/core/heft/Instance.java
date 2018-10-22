package org.optframework.core.heft;

import java.util.ArrayList;

/**
 *  PURPOSE: is used to store gap information
 * */

public class Instance {
    public ArrayList<Gap> gapList = new ArrayList<>();
    public boolean hasGap;

    public ArrayList<Gap> getGapList() {
        return gapList;
    }

}
