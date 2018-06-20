package org.optframework.core.heft;

import java.util.ArrayList;

public class Instance {
    ArrayList<Gap> gapList = new ArrayList<>();
    boolean hasGap;

    public ArrayList<Gap> getGapList() {
        return gapList;
    }

    public void setGapList(ArrayList<Gap> gapList) {
        this.gapList = gapList;
    }

    public boolean isHasGap() {
        return hasGap;
    }

    public void setHasGap(boolean hasGap) {
        this.hasGap = hasGap;
    }
}
