package org.optframework;

import org.cloudbus.spotsim.enums.AZ;
import org.cloudbus.spotsim.enums.InstanceType;
import org.cloudbus.spotsim.enums.OS;
import org.cloudbus.spotsim.enums.Region;
import org.optframework.config.Config;
import org.optframework.core.*;
import org.optframework.core.heft.HEFTAlgorithm;
import org.optframework.core.pacsa.PACSAOptimization;
import org.optframework.core.utils.PopulateWorkflow;
import org.optframework.core.utils.PreProcessor;
import org.optframework.core.utils.Printer;

/**
 * @author Hessam Modabberi hessam.modaberi@gmail.com
 * @version 1.0.0
 */

public class RunPACSAAlgorithm {
    public static final int M_NUMBER = Config.global.m_number;

    public static void runPACSA()
    {
        Log.logger.info("<<<<<<<<< PACSA Algorithm is started >>>>>>>>>");

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
                maxECU = type.getEcu();
            }
        }

        /**
         * Initializes available instances for the HEFT algorithm with the max number of instances and sets them to the most powerful instance type (that is 6)
         * */
        int totalInstances[] = new int[M_NUMBER];
        for (int i = 0; i < M_NUMBER; i++) {
            totalInstances[i] = maxECUId;
        }

        Log.logger.info("<<<<<<<<<<  HEFT Algorithm is started  >>>>>>>>>>>");

        Workflow heftWorkflow = PreProcessor.doPreProcessingForHEFT(PopulateWorkflow.populateWorkflowWithId(Config.global.budget, 0, Config.global.workflow_id), Config.global.bandwidth, totalInstances, instanceInfo);

        heftWorkflow.setBeta(Beta.computeBetaValue(heftWorkflow, instanceInfo, M_NUMBER));

        HEFTAlgorithm heftAlgorithm = new HEFTAlgorithm(heftWorkflow, instanceInfo, totalInstances);
        Solution heftSolution = heftAlgorithm.runAlgorithm();
        heftSolution.fitness();

        if (Config.pacsa_algorithm.m_number_from_heft){
            Config.global.m_number = heftSolution.numberOfUsedInstances;
        }

        Workflow workflow = PreProcessor.doPreProcessing(PopulateWorkflow.populateWorkflowWithId(Config.global.budget, 0, Config.global.workflow_id));

        computeCoolingFactorForSA(workflow.getJobList().size());

        Log.logger.info("Maximum number of instances: " + M_NUMBER + " Number of different types of instances: " + InstanceType.values().length + " Number of tasks: "+ workflow.getJobList().size());

        workflow.setBeta(Beta.computeBetaValue(workflow, instanceInfo, M_NUMBER));

        OptimizationAlgorithm optimizationAlgorithm;

        double fitnessValueList[] = new double[Config.pacsa_algorithm.getNumber_of_runs()];
        double costValueList[] = new double[Config.pacsa_algorithm.getNumber_of_runs()];

        optimizationAlgorithm = new PACSAOptimization((1/(double)heftSolution.makespan),workflow, instanceInfo);

        for (int i = 0; i < Config.honeybee_algorithm.getNumber_of_runs(); i++) {
            Printer.printSplitter();
            Log.logger.info("<<<<<<<<<<<    NEW RUN "+ i +"     >>>>>>>>>>>\n");

            long start = System.currentTimeMillis();

            Solution solution = optimizationAlgorithm.runAlgorithm();

            fitnessValueList[i] = solution.fitnessValue;
            costValueList[i] = solution.cost;

            long stop = System.currentTimeMillis();

            Printer.lightPrintSolution(solution,stop-start);
        }

        double sum = 0.0, costSum = 0.0;
        double max = 0.0, costMax = 0.0;
        double min = 999999999999.9, costMin = 99999999.9;
        for (double value : fitnessValueList){
            sum += value;
            if (value > max){
                max = value;
            }
            if (value < min){
                min = value;
            }
        }

        for (double value : costValueList){
            costSum += value;
            if (value > costMax){
                costMax = value;
            }
            if (value < costMin){
                costMin = value;
            }
        }

        Printer.printSplitter();
        Log.logger.info("Average Fitness value: " + sum / Config.pacsa_algorithm.getNumber_of_runs());
        Log.logger.info("Average Cost value: " + costSum / Config.pacsa_algorithm.getNumber_of_runs());

        Log.logger.info("Max fitness: " + max + " Min fitness: "+ min);
    }

    static void computeCoolingFactorForSA(int numberOfTasks){
        if (!Config.sa_algorithm.force_cooling){
            if (numberOfTasks >= 10){
                Config.sa_algorithm.cooling_factor = 1 - 1 / (double)numberOfTasks;
            }else {
                Config.sa_algorithm.cooling_factor = 0.9;
            }
        }
    }
}
