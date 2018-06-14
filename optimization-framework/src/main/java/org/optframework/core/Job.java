package org.optframework.core;

import org.cloudbus.cloudsim.util.workload.WFEdge;

import java.io.Serializable;
import java.util.List;

/**
 * @author Hessam Modaberi
 * @since 2018
 * */

public class Job implements Serializable {

    private static final long serialVersionUID = 4123274113987701274L;

    private long id;

    private double rank;

    private double finishTime;

    private double exeTime[];

    private double avgExeTime;

    private List<WFEdge> edgeInfo;

    public Job(long id, double rank, double finishTime, double[] exeTime, double avgExeTime, List<WFEdge> edgeInfo) {
        this.id = id;
        this.rank = rank;
        this.finishTime = finishTime;
        this.exeTime = exeTime;
        this.avgExeTime = avgExeTime;
        this.edgeInfo = edgeInfo;
    }

    public int getIntId() {
        return (int)this.id;
    }

    public long getEdge(int taskID){
        for(WFEdge edge : edgeInfo){
            if(taskID == edge.getToTaskID()){
                return edge.getTransferTime();
            }
        }
        return 0;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        final Job other = (Job) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getRank() {
        return rank;
    }

    public void setRank(double rank) {
        this.rank = rank;
    }

    public double getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(double finishTime) {
        this.finishTime = finishTime;
    }

    public double getAvgExeTime() {
        return avgExeTime;
    }

    public void setAvgExeTime(double avgExeTime) {
        this.avgExeTime = avgExeTime;
    }
}
