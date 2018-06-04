package org.optframework.core;

import java.util.Comparator;

public class ReadyTask implements Comparable<ReadyTask>{
    int jobId;
    double exeTime;
    double maxParentFinishTime;
    double weight;
    double cr;

    public ReadyTask(int jobId, double exeTime) {
        this.jobId = jobId;
        this.exeTime = exeTime;
    }

    public ReadyTask(int jobId, double exeTime, double maxParentFinishTime, double cr) {
        this.jobId = jobId;
        this.exeTime = exeTime;
        this.maxParentFinishTime = maxParentFinishTime;
        this.cr = cr;
    }

    @Override
    public int compareTo(ReadyTask o) {
        return (int)o.exeTime - (int)exeTime ;
    }

    public static Comparator<ReadyTask> weightComparator = new Comparator<ReadyTask>() {
        @Override
        public int compare(ReadyTask o1, ReadyTask o2) {
            return (int)o1.weight - (int)o2.weight;
        }
    };
}
