package org.optframework;

import org.cloudbus.spotsim.enums.AZ;
import org.cloudbus.spotsim.enums.InstanceType;
import org.cloudbus.spotsim.enums.OS;
import org.cloudbus.spotsim.enums.Region;
import org.cloudbus.spotsim.main.config.Config;
import org.cloudbus.spotsim.pricing.PriceRecord;
import org.cloudbus.spotsim.pricing.SpotPriceHistory;
import org.cloudbus.spotsim.pricing.db.PriceDB;
import org.optframework.core.*;
import org.optframework.core.dfs.DFSAlgorithm;
import org.optframework.core.utils.PopulateWorkflow;
import org.optframework.core.utils.PreProcessor;
import org.optframework.core.utils.Printer;

public class RunDFSAlgorithm {
    public static final int M_NUMBER = org.optframework.config.Config.global.m_number;

    public static void main(String[] args) throws Exception {
        Log.init();

        /**
         * Initializes Cloudsim Logger
         * */
        org.cloudbus.cloudsim.Log.init("cloudsim.log");

        Log.logger.info("Loads configs");
        Config.load(null);

        org.optframework.config.Config.initConfig();

        Workflow workflow = PreProcessor.doPreProcessing(PopulateWorkflow.populateSimpleWorkflow5(1000, 0));

        Log.logger.info("Maximum number of instances: " + M_NUMBER + " Number of different types of instances: " + InstanceType.values().length + " Number of tasks: "+ workflow.getJobList().size());

        /**
         * Assumptions:
         * Region: europe
         * Availability Zone: A
         * OS type: Linux System
         * */
        InstanceInfo instanceInfo[] = populateInstancePrices(Region.EUROPE , AZ.A, OS.LINUX);

        workflow.setBeta(Beta.computeBetaValue(workflow, instanceInfo, M_NUMBER));

        long start = System.currentTimeMillis();

        DFSAlgorithm dfsAlgorithm = new DFSAlgorithm(instanceInfo, workflow);

        long stop = System.currentTimeMillis();

        Solution solution = dfsAlgorithm.runAlgorithm();

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
