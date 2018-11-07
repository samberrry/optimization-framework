package org.optframework.core;

import org.cloudbus.cloudsim.util.workload.WFEdge;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

/**
 * @author Hessam Modaberi
 * @since 2018
 * */

public class Job implements Serializable{

    private static final long serialVersionUID = 4123274113987701274L;

    private long id;

    private double rank;

    private double finishTime;

    private double length;

    private double exeTime[];

    private double avgExeTime;

    private List<WFEdge> edgeInfo;

    public Job(long id, double[] exeTime, double avgExeTime, List<WFEdge> edgeInfo) {
        this.id = id;
        this.exeTime = exeTime;
        this.avgExeTime = avgExeTime;
        this.edgeInfo = edgeInfo;
    }

    public Job(long id, double length){
        this.id = id;
        this.length = length;
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

    public double[] getExeTime() {
        return exeTime;
    }

    public void setExeTime(double[] exeTime) {
        this.exeTime = exeTime;
    }

    public static Comparator<Job> rankComparator = new Comparator<Job>() {
        @Override
        public int compare(Job o1, Job o2) {
            return (int)o2.rank - (int)o1.rank;
        }
    };

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

}
