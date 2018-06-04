package org.optframework.core;

public class ParentTask {
    int parentId;
    double parentFinishTime;
    double cr;

    public ParentTask(int parentId, long parentFinishTime, long cr) {
        this.parentId = parentId;
        this.parentFinishTime = parentFinishTime;
        this.cr = cr;
    }
}
