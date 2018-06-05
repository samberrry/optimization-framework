package org.optframework.core;

public class ParentTask {
    int parentId;
    double parentFinishTime;
//    CR is computed: edge transfer time/ instance time bandwidth
    double cr;

    public ParentTask(int parentId, double parentFinishTime, double cr) {
        this.parentId = parentId;
        this.parentFinishTime = parentFinishTime;
        this.cr = cr;
    }
}
