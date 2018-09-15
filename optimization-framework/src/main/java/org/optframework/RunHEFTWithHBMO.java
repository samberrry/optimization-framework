package org.optframework;

import org.cloudbus.spotsim.enums.AZ;
import org.cloudbus.spotsim.enums.OS;
import org.cloudbus.spotsim.enums.Region;
import org.optframework.config.Config;
import org.optframework.core.*;
import org.optframework.core.hbmo.HBMOAlgorithm;
import org.optframework.core.heft.HEFTAlgorithm;
import org.optframework.core.utils.PopulateWorkflow;
import org.optframework.core.utils.PreProcessor;
import org.optframework.core.utils.Printer;

public class RunHEFTWithHBMO {
    public static final int M_NUMBER = Config.global.m_number;

    public static void runHEFTWithHBMO(){

        /**
         * Assumptions:
         * Region: europe
         * Availability Zone: A
         * OS type: Linux System
         * */
        InstanceInfo instanceInfo[] = InstanceInfo.populateInstancePrices(Region.EUROPE , AZ.A, OS.LINUX);

        Log.logger.info("<<<<<<<<<<  HBMO Algorithm is started  >>>>>>>>>>");

        Workflow hbmoWorkflow = PreProcessor.doPreProcessing(PopulateWorkflow.populateWorkflowWithId(Config.global.budget, 0, Config.global.workflow_id));

        hbmoWorkflow.setBeta(Beta.computeBetaValue(hbmoWorkflow, instanceInfo, M_NUMBER));

        HBMOAlgorithm hbmoAlgorithm = new HBMOAlgorithm(false, hbmoWorkflow, instanceInfo, Config.honeybee_algorithm.getGeneration_number());

        long start = System.currentTimeMillis();
        Solution hbmoSolution = hbmoAlgorithm.runAlgorithm();
        long stop = System.currentTimeMillis();

//        Printer.printSolution(hbmoSolution, instanceInfo, stop-start);

        int totalInstances[] = new int[hbmoSolution.numberOfUsedInstances];
        for (int i = 0; i < hbmoSolution.numberOfUsedInstances; i++) {
            totalInstances[i] = hbmoSolution.yArray[i];
        }

        Log.logger.info("----------  HBMO Algorithm is finished  ----------");

        Workflow workflow = PreProcessor.doPreProcessingForHEFT(PopulateWorkflow.populateWorkflowWithId(Config.global.budget, 0, Config.global.workflow_id), Config.global.bandwidth, totalInstances, instanceInfo);

        HEFTAlgorithm heftAlgorithm = new HEFTAlgorithm(workflow, instanceInfo, totalInstances);

        start = System.currentTimeMillis();
        Solution heftSolution = heftAlgorithm.runAlgorithm();
        stop = System.currentTimeMillis();

//        Log.logger.info("HBMO Makespan: " + hbmoSolution.makespan + "   HEFT Makespan: " + heftSolution.makespan);

        Printer.printTime(stop-start);
    }
}
