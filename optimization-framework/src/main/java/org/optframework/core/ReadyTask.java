package org.optframework.core;

import java.util.Comparator;

public class ReadyTask implements Comparable<ReadyTask>{
    int jobId;
    double exeTime;
    double maxParentFinishTime;
    double maxParentWeight;
    double weight;
    double cr;

    public ReadyTask(int jobId, double exeTime) {
        this.jobId = jobId;
        this.exeTime = exeTime;
    }

    public ReadyTask(int jobId, double exeTime, double maxParentFinishTime, double maxParentWeight,  double cr) {
        this.jobId = jobId;
        this.exeTime = exeTime;
        this.maxParentFinishTime = maxParentFinishTime;
        this.maxParentWeight = maxParentWeight;
        this.cr = cr;
        this.weight = maxParentWeight + exeTime + cr;
    }

    @Override
    public int compareTo(ReadyTask o) {
        return (int)o.exeTime - (int)exeTime ;
    }

    public static Comparator<ReadyTask> weightComparator = new Comparator<ReadyTask>() {
        @Override
        public int compare(ReadyTask o1, ReadyTask o2) {
            return (int)o2.weight - (int)o1.weight;
        }
    };
}
