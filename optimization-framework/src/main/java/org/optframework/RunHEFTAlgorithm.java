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
import org.optframework.core.heft.HEFTAlgorithm;
import org.optframework.core.utils.PopulateWorkflow;
import org.optframework.core.utils.PreProcessor;
import org.optframework.core.utils.Printer;

public class RunHEFTAlgorithm implements StaticProperties {

    public static void main(String[] args) throws Exception{
        Log.init();

        Log.logger.info("<<<<<<<<< HEFT Algorithm is started >>>>>>>>>");

        /**
         * Initializes Cloudsim Logger
         * */
        org.cloudbus.cloudsim.Log.init("cloudsim.log");

        Log.logger.info("Loads configs");
        org.cloudbus.spotsim.main.config.Config.load(null);

        Config.initConfig();

        Workflow workflow = PreProcessor.doPreProcessing(PopulateWorkflow.populateSimpleWorkflow8(Config.global.budget, 0), Config.global.bandwidth);

        Log.logger.info("Maximum number of instances: " + M_NUMBER + " Number of different types of instances: " + N_TYPES + " Number of tasks: "+ workflow.getJobList().size());

        Log.logger.info("Config File ---------- "+" itr: "+ Config.honeybee_algorithm.getGeneration_number()+ " sp size: "+ Config.honeybee_algorithm.getSpermatheca_size()+ " nbh ratio: "+ Config.honeybee_algorithm.getNeighborhood_ratio()+ " force speed: "+ Config.honeybee_algorithm.getForce_speed());

        /**
         * Assumptions:
         * Region: europe
         * Availability Zone: A
         * OS type: Linux System
         * */
        InstanceInfo instanceInfo[] = populateInstancePrices(Region.EUROPE , AZ.A, OS.LINUX);

        HEFTAlgorithm heftAlgorithm = new HEFTAlgorithm(workflow, instanceInfo);

        long start = System.currentTimeMillis();

        Solution solution = heftAlgorithm.runAlgorithm();

        long stop = System.currentTimeMillis();

        Printer.printSolution(solution, instanceInfo, stop-start);
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

}
