package org.optframework;

import org.cloudbus.spotsim.enums.AZ;
import org.cloudbus.spotsim.enums.InstanceType;
import org.cloudbus.spotsim.enums.OS;
import org.cloudbus.spotsim.enums.Region;
import org.optframework.config.Config;
import org.optframework.core.*;
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

        Workflow workflow = PreProcessor.doPreProcessing(PopulateWorkflow.populateWorkflowWithId(Config.global.budget, 0, Config.global.workflow_id));

        Log.logger.info("Maximum number of instances: " + M_NUMBER + " Number of different types of instances: " + InstanceType.values().length + " Number of tasks: "+ workflow.getJobList().size());

        /**
         * Assumptions:
         * Region: europe
         * Availability Zone: A
         * OS type: Linux System
         * */
        InstanceInfo instanceInfo[] = InstanceInfo.populateInstancePrices(Region.EUROPE , AZ.A, OS.LINUX);

        workflow.setBeta(Beta.computeBetaValue(workflow, instanceInfo, M_NUMBER));

        OptimizationAlgorithm optimizationAlgorithm;

        double fitnessValueList[] = new double[Config.pacsa_algorithm.getNumber_of_runs()];
        double costValueList[] = new double[Config.pacsa_algorithm.getNumber_of_runs()];

        optimizationAlgorithm = new PACSAOptimization(workflow, instanceInfo);

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
}
