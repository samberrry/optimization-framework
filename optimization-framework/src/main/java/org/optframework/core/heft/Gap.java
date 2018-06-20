package org.optframework.core.heft;

import java.util.Comparator;

public class Gap {
    double startTime;
    double endTime;
    double duration;

    public Gap(double startTime, double endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
        duration = endTime - startTime;
    }

    public double getStartTime() {
        return startTime;
    }

    public void setStartTime(double startTime) {
        this.startTime = startTime;
    }

    public double getEndTime() {
        return endTime;
    }

    public void setEndTime(double endTime) {
        this.endTime = endTime;
    }

    public static Comparator<Gap> gapComparator = new Comparator<Gap>() {
        @Override
        public int compare(Gap o1, Gap o2) {
            return (int)o1.startTime - (int)o2.startTime;
        }
    };
}
