package org.optframework;

import org.cloudbus.spotsim.enums.AZ;
import org.cloudbus.spotsim.enums.InstanceType;
import org.cloudbus.spotsim.enums.OS;
import org.cloudbus.spotsim.enums.Region;
import org.optframework.config.Config;
import org.optframework.core.*;
import org.optframework.core.heft.HEFTAlgorithm;
import org.optframework.core.utils.PopulateWorkflow;
import org.optframework.core.utils.PreProcessor;
import org.optframework.core.utils.Printer;

public class RunHEFTAlgorithm {

    public static final int M_NUMBER = Config.global.m_number;

    public static void runSingleHEFT(){

        /**
         * Assumptions:
         * Region: europe
         * Availability Zone: A
         * OS type: Linux System
         * */
        InstanceInfo instanceInfo[] = InstanceInfo.populateInstancePrices(Region.EUROPE , AZ.A, OS.LINUX);

        int maxECUId = -1;
        double maxECU = 0.0;

        for (InstanceType type : InstanceType.values()){
            if (type.getEcu() > maxECU){
                maxECUId = type.getId();
            }
        }

        /**
         * Initializes available instances for the HEFT algorithm with the max number of instances and sets them to the most powerful instance type (that is 6)
         * */
        int totalInstances[] = new int[M_NUMBER];
        for (int i = 0; i < M_NUMBER; i++) {
            totalInstances[i] = maxECUId;
        }

//        int totalInstances[] = new int[M_NUMBER * 9];
//        int k = 0;
//        for (int i = 0; i < M_NUMBER; i++) {
//            for (int j = 0; j < 9; j++) {
//                totalInstances[k] = j;
//                        k++;
//            }
//        }

        Workflow workflow = PreProcessor.doPreProcessingForHEFT(PopulateWorkflow.populateWorkflowWithId(Config.global.budget, 0, Config.global.workflow_id), Config.global.bandwidth, totalInstances, instanceInfo);

        HEFTAlgorithm heftAlgorithm = new HEFTAlgorithm(workflow, instanceInfo, totalInstances);

        long start = System.currentTimeMillis();
        Solution heftSolution = heftAlgorithm.runAlgorithm();
        long stop = System.currentTimeMillis();

        Printer.printSolution(heftSolution, instanceInfo, stop-start);
    }
}
