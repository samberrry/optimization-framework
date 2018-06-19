package org.optframework;

import org.cloudbus.spotsim.enums.AZ;
import org.cloudbus.spotsim.enums.InstanceType;
import org.cloudbus.spotsim.enums.OS;
import org.cloudbus.spotsim.enums.Region;
import org.cloudbus.spotsim.pricing.PriceRecord;
import org.cloudbus.spotsim.pricing.SpotPriceHistory;
import org.cloudbus.spotsim.pricing.db.PriceDB;
import org.optframework.config.Config;
import org.optframework.config.StaticProperties;
import org.optframework.core.*;
import org.optframework.core.hbmo.HBMOAlgorithm;
import org.optframework.core.utils.PopulateWorkflow;
import org.optframework.core.utils.PreProcessor;
import org.optframework.core.utils.Printer;

/**
 * @author Hessam Modabberi
 * @version 1.0.0
 */

public class RunHBMOAlgorithm implements StaticProperties {

    public static void main(String[] args) throws Exception{
        Log.init();

        Log.logger.info("<<<<<<<<< HBMO Algorithm is started >>>>>>>>>");

        /**
         * Initializes Cloudsim Logger
         * */
        org.cloudbus.cloudsim.Log.init("cloudsim.log");

        Log.logger.info("Loads configs");
        org.cloudbus.spotsim.main.config.Config.load(null);

        Config.initConfig();

        Workflow workflow = PreProcessor.doPreProcessing(PopulateWorkflow.populateWorkflowFromDaxWithId(Config.global.budget, 0, Config.global.workflow_id), Config.global.bandwidth);

        honeyBeePreProcessing(workflow);

        Log.logger.info("Maximum number of instances: " + M_NUMBER + " Number of different types of instances: " + N_TYPES + " Number of tasks: "+ workflow.getJobList().size());
        Printer.printHoneBeeInfo();

        /**
         * Assumptions:
         * Region: europe
         * Availability Zone: A
         * OS type: Linux System
         * */
        InstanceInfo instanceInfo[] = populateInstancePrices(Region.EUROPE , AZ.A, OS.LINUX);

        workflow.setBeta(Beta.computeBetaValue(workflow, instanceInfo, M_NUMBER));

        HBMOAlgorithm hbmoAlgorithm = new HBMOAlgorithm(workflow, instanceInfo, Config.honeybee_algorithm.getGeneration_number());

        double fitnessValueList[] = new double[Config.honeybee_algorithm.getNumber_of_runs()];

        for (int i = 0; i < Config.honeybee_algorithm.getNumber_of_runs(); i++) {

            long start = System.currentTimeMillis();

            Solution solution = hbmoAlgorithm.runAlgorithm();

            fitnessValueList[i] = solution.fitnessValue;

            long stop = System.currentTimeMillis();

            Log.logger.info("Global Counter: " + HBMOAlgorithm.globalCounter);
            Printer.printSolution(solution, instanceInfo, stop-start);
        }

        double sum = 0.0;
        double max = 0.0;
        double min = 999999999999.9;
        for (double value : fitnessValueList){
            sum += value;
            if (value > max){
                max = value;
            }
            if (value < min){
                min = value;
            }
        }
        Printer.printSplitter();
        Log.logger.info("Average fitness value: " + sum / Config.honeybee_algorithm.getNumber_of_runs());

        Log.logger.info("Max fitness: " + max + " Min fitness: "+ min);
        Printer.printHoneBeeInfo();
    }

    private static InstanceInfo[] populateInstancePrices(Region region , AZ az, OS os){
        Log.logger.info("Loads spot prices history");
        SpotPriceHistory priceTraces = PriceDB.getPriceTrace(region , az);
        InstanceInfo info[] = new InstanceInfo[InstanceType.values().length];

        for (InstanceType type: InstanceType.values()){
            PriceRecord priceRecord = priceTraces.getNextPriceChange(type,os);
            InstanceInfo instanceInfo = new InstanceInfo();
            instanceInfo.setSpotPrice(priceRecord.getPrice());
            instanceInfo.setType(type);

            info[type.getId()] = instanceInfo;
        }
        return info;
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
