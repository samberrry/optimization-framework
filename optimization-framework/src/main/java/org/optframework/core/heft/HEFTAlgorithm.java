package org.optframework.core.heft;

import org.cloudbus.spotsim.enums.InstanceType;
import org.optframework.core.*;

import java.util.Collections;
import java.util.List;

public class HEFTAlgorithm implements OptimizationAlgorithm {

    Workflow workflow;

    InstanceInfo instanceInfo[];

    public HEFTAlgorithm(Workflow workflow, InstanceInfo[] instanceInfo) {
        this.workflow = workflow;
        this.instanceInfo = instanceInfo;
    }

    @Override
    public Solution runAlgorithm() {
        List<Job> jobList = workflow.getJobList();
        Collections.sort(jobList, Job.rankComparator);

        for (Job job: jobList){
            for (InstanceType type : InstanceType.values()){

            }
        }

        return null;
    }
}
