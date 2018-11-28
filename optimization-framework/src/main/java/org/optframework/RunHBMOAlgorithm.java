package org.optframework;

import com.rits.cloning.Cloner;
import org.cloudbus.spotsim.enums.AZ;
import org.cloudbus.spotsim.enums.InstanceType;
import org.cloudbus.spotsim.enums.OS;
import org.cloudbus.spotsim.enums.Region;
import org.optframework.config.Config;
import org.optframework.core.*;
import org.optframework.core.hbmo.HBMOAlgorithm;
import org.optframework.core.hbmo.HBMOAlgorithmWithFullMutation;
import org.optframework.core.utils.PopulateWorkflow;
import org.optframework.core.utils.PreProcessor;
import org.optframework.core.utils.Printer;

import java.util.Collections;

/**
 * @author Hessam Modabberi
 * @version 1.0.0
 */

public class RunHBMOAlgorithm {

    public static final int M_NUMBER = Config.global.m_number;

    public static void runHBMO(){
        Log.logger.info("<<<<<<<<< HBMO Algorithm is started >>>>>>>>>");

        Workflow workflow = PreProcessor.doPreProcessing(PopulateWorkflow.populateWorkflowWithId(Config.global.budget, 0, Config.global.workflow_id));

        Cloner cloner = new Cloner();
        GlobalAccess.orderedJobList = cloner.deepClone(workflow.getJobList());
        Collections.sort(GlobalAccess.orderedJobList, Job.rankComparator);

        honeyBeePreProcessing(workflow);

        Log.logger.info("Maximum number of instances: " + M_NUMBER + " Number of different types of instances: " + InstanceType.values().length + " Number of tasks: "+ workflow.getJobList().size());
        Printer.printHoneBeeInfo();

        /**
         * Assumptions:
         * Region: europe
         * Availability Zone: A
         * OS type: Linux System
         * */
        InstanceInfo instanceInfo[] = InstanceInfo.populateInstancePrices(Region.EUROPE , AZ.A, OS.LINUX);

        workflow.setBeta(Beta.computeBetaValue(workflow, instanceInfo, M_NUMBER));

        OptimizationAlgorithm optimizationAlgorithm;

        if (Config.honeybee_algorithm.getFull_mutation()){
            optimizationAlgorithm = new HBMOAlgorithmWithFullMutation(false, workflow, instanceInfo, Config.honeybee_algorithm.getGeneration_number());
        }else {
            optimizationAlgorithm = new HBMOAlgorithm(false, workflow, instanceInfo, Config.honeybee_algorithm.getGeneration_number());
        }

        double fitnessValueList[] = new double[Config.honeybee_algorithm.getNumber_of_runs()];
        double costValueList[] = new double[Config.honeybee_algorithm.getNumber_of_runs()];

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
        Log.logger.info("Average Fitness value: " + sum / Config.honeybee_algorithm.getNumber_of_runs());
        Log.logger.info("Average Cost value: " + costSum / Config.honeybee_algorithm.getNumber_of_runs());

        Log.logger.info("Max fitness: " + max + " Min fitness: "+ min);
        Printer.printHoneBeeInfo();
    }

    static void honeyBeePreProcessing(Workflow workflow){
        if (workflow.getJobList().size() >= 100){
            double kRandom = workflow.getJobList().size() * Config.honeybee_algorithm.getNeighborhood_ratio();
            Config.honeybee_algorithm.kRandom =  (int) kRandom;
        }else {
            Config.honeybee_algorithm.kRandom = workflow.getJobList().size()/3;
        }
    }
}
