package org.optframework.core.heft;

import org.cloudbus.spotsim.enums.InstanceType;
import org.optframework.config.Config;
import org.optframework.core.*;
import org.optframework.core.utils.PopulateWorkflow;
import org.optframework.core.utils.PreProcessor;

public class HEFTService {
    public static Solution getHEFT(InstanceInfo instanceInfo[]){
        Log.logger.info("<<<<<<<<<<  HEFT Algorithm is started  >>>>>>>>>>>");

        int maxECUId = -1;
        double maxECU = 0.0;

        for (InstanceType type : InstanceType.values()){
            if (type.getEcu() > maxECU){
                maxECUId = type.getId();
                maxECU = type.getEcu();
            }
        }

        /**
         * Initializes available instances for the HEFT algorithm with the max number of instances and sets them to the most powerful instance type (that is 6)
         * */
        int totalInstances[] = new int[Config.global.m_number];
        for (int i = 0; i < Config.global.m_number; i++) {
            totalInstances[i] = maxECUId;
        }

        Workflow heftWorkflow = PreProcessor.doPreProcessingForHEFT(PopulateWorkflow.populateWorkflowWithId(Config.global.budget, 0, Config.global.workflow_id), Config.global.bandwidth, totalInstances, instanceInfo);

        heftWorkflow.setBeta(Beta.computeBetaValue(heftWorkflow, instanceInfo, Config.global.m_number));

        HEFTAlgorithm heftAlgorithm = new HEFTAlgorithm(heftWorkflow, instanceInfo, totalInstances);
        Solution heftSolution = heftAlgorithm.runAlgorithm();
        heftSolution.fitness();

        return heftSolution;
    }

    public static Solution getCostEfficientHEFT(InstanceInfo instanceInfo[]){
        Log.logger.info("<<<<<<<<<<  HEFT Algorithm is started  >>>>>>>>>>>");

        int minECUId = -1;
        double minECU = 99999999999.9;

        for (InstanceType type : InstanceType.values()){
            if (type.getEcu() < minECU){
                minECUId = type.getId();
                minECU = type.getEcu();
            }
        }

        /**
         * Initializes available instances for the HEFT algorithm with the max number of instances and sets them to the most powerful instance type (that is 6)
         * */
        int totalInstances[] = new int[Config.global.m_number];
        for (int i = 0; i < Config.global.m_number; i++) {
            totalInstances[i] = minECUId;
        }

        Workflow heftWorkflow = PreProcessor.doPreProcessingForHEFT(PopulateWorkflow.populateWorkflowWithId(Config.global.budget, 0, Config.global.workflow_id), Config.global.bandwidth, totalInstances, instanceInfo);

        heftWorkflow.setBeta(Beta.computeBetaValue(heftWorkflow, instanceInfo, Config.global.m_number));

        HEFTAlgorithm heftAlgorithm = new HEFTAlgorithm(heftWorkflow, instanceInfo, totalInstances);
        Solution heftSolution = heftAlgorithm.runAlgorithm();
        heftSolution.fitness();

        return heftSolution;
    }
}
