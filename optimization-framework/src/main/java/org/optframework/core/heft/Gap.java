package org.optframework.core.heft;

import java.util.Comparator;

public class Gap {
    int gapId;
    public double startTime;
    public double endTime;
    public double duration;

    public Gap(double startTime, double endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
        duration = endTime - startTime;
    }

    public static Comparator<Gap> gapComparator = new Comparator<Gap>() {
        @Override
        public int compare(Gap o1, Gap o2) {
            return (int)o1.startTime - (int)o2.startTime;
        }
    };
}
