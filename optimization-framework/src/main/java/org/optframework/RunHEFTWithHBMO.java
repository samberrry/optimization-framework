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
import org.optframework.core.heft.HEFTAlgorithm;
import org.optframework.core.utils.PopulateWorkflow;
import org.optframework.core.utils.PreProcessor;
import org.optframework.core.utils.Printer;

public class RunHEFTWithHBMO implements StaticProperties {
    public static void main(String[] args) throws Exception{
        Log.init();

        Log.logger.info("<<<<<<<<< HEFT Algorithm with HBMO is started >>>>>>>>>");

        /**
         * Initializes Cloudsim Logger
         * */
        org.cloudbus.cloudsim.Log.init("cloudsim.log");

        Log.logger.info("Loads configs");
        org.cloudbus.spotsim.main.config.Config.load(null);

        Config.initConfig();

        /**
         * Assumptions:
         * Region: europe
         * Availability Zone: A
         * OS type: Linux System
         * */
        InstanceInfo instanceInfo[] = populateInstancePrices(Region.EUROPE , AZ.A, OS.LINUX);

        Log.logger.info("<<<<<<<<<<  HBMO Algorithm is started  >>>>>>>>>>");

        Workflow hbmoWorkflow = PreProcessor.doPreProcessing(PopulateWorkflow.populateWorkflowFromDaxWithId(Config.global.budget, 0, Config.global.workflow_id), Config.global.bandwidth);

        hbmoWorkflow.setBeta(Beta.computeBetaValue(hbmoWorkflow, instanceInfo, M_NUMBER));

        HBMOAlgorithm hbmoAlgorithm = new HBMOAlgorithm(hbmoWorkflow, instanceInfo, Config.honeybee_algorithm.getGeneration_number());

        long start = System.currentTimeMillis();
        Solution hbmoSolution = hbmoAlgorithm.runAlgorithm();
        long stop = System.currentTimeMillis();

        Printer.printSolution(hbmoSolution, instanceInfo, stop-start);

        int totalInstances[] = new int[hbmoSolution.numberOfUsedInstances];
        for (int i = 0; i < hbmoSolution.numberOfUsedInstances; i++) {
            totalInstances[i] = hbmoSolution.yArray[i];
        }

        Log.logger.info("----------  HBMO Algorithm is finished  ----------");

        Workflow workflow = PreProcessor.doPreProcessingForHEFT(PopulateWorkflow.populateWorkflowFromDaxWithId(Config.global.budget, 0, Config.global.workflow_id), Config.global.bandwidth, totalInstances, instanceInfo);

        HEFTAlgorithm heftAlgorithm = new HEFTAlgorithm(workflow, instanceInfo, totalInstances);

        Solution solution = heftAlgorithm.runAlgorithm();

        Printer.printSolutionWithoutTime(solution, instanceInfo);
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
