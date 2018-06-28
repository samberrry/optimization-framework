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

public class RunHEFTBeforeHBMO {
    public static final int M_NUMBER = Config.global.m_number;

    public static void runHEFTBeforeHBMO(){

        /**
         * Assumptions:
         * Region: europe
         * Availability Zone: A
         * OS type: Linux System
         * */
        InstanceInfo instanceInfo[] = InstanceInfo.populateInstancePrices(Region.EUROPE , AZ.A, OS.LINUX);

        int totalInstances[] = new int[Config.global.m_number];
        for (int i = 0; i < Config.global.m_number; i++) {
            totalInstances[i] = 6;
        }

        Log.logger.info("<<<<<<<<<<  HEFT Algorithm is started  >>>>>>>>>>>");

        Workflow workflow = PreProcessor.doPreProcessingForHEFT(PopulateWorkflow.populateWorkflowWithId(Config.global.budget, 0, Config.global.workflow_id), Config.global.bandwidth, totalInstances, instanceInfo);

        workflow.setBeta(Beta.computeBetaValue(workflow, instanceInfo, M_NUMBER));

        HEFTAlgorithm heftAlgorithm = new HEFTAlgorithm(workflow, instanceInfo, totalInstances);

        Solution heftSolution = heftAlgorithm.runAlgorithm();

        Log.logger.info("<<<<<<<<<<  HBMO Algorithm is started  >>>>>>>>>>>");

        Workflow hbmoWorkflow = PreProcessor.doPreProcessing(PopulateWorkflow.populateWorkflowWithId(Config.global.budget, 0, Config.global.workflow_id));

        hbmoWorkflow.setBeta(Beta.computeBetaValue(hbmoWorkflow, instanceInfo, M_NUMBER));

        HBMOAlgorithm hbmoAlgorithm = new HBMOAlgorithm(true, hbmoWorkflow, instanceInfo, Config.honeybee_algorithm.getGeneration_number());

        hbmoAlgorithm.initialSolution = heftSolution;

        long start = System.currentTimeMillis();
        Solution hbmoSolution = hbmoAlgorithm.runAlgorithm();
        long stop = System.currentTimeMillis();

        Log.logger.info("HEFT Fitness: "+ heftSolution.fitnessValue + " HBMO Fitness: " + hbmoSolution.fitnessValue);

        Printer.printTime(stop-start);
    }
}
