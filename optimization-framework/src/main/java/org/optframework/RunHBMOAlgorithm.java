package org.optframework;

import org.cloudbus.cloudsim.util.workload.Workflow;
import org.cloudbus.spotsim.enums.AZ;
import org.cloudbus.spotsim.enums.InstanceType;
import org.cloudbus.spotsim.enums.OS;
import org.cloudbus.spotsim.enums.Region;
import org.cloudbus.spotsim.main.config.Config;
import org.cloudbus.spotsim.pricing.PriceRecord;
import org.cloudbus.spotsim.pricing.SpotPriceHistory;
import org.cloudbus.spotsim.pricing.db.PriceDB;
import org.optframework.config.StaticProperties;
import org.optframework.core.*;
import org.optframework.core.hbmo.HBMOAlgorithm;

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
        Config.load(null);

        Workflow workflow = PopulateWorkflow.populateWorkflowFromDax(1000, 0);
        Log.logger.info("Maximum number of instances: " + M_NUMBER + " Number of different types of instances: " + N_TYPES + " Number of tasks: "+ workflow.getJobList().size());

        /**
         * Assumptions:
         * Region: europe
         * Availability Zone: A
         * OS type: Linux System
         * */
        InstanceInfo instanceInfo[] = populateInstancePrices(Region.EUROPE , AZ.A, OS.LINUX);

        workflow.setBeta(Beta.computerBetaValue(workflow, instanceInfo, M_NUMBER));

        HBMOAlgorithm hbmoAlgorithm = new HBMOAlgorithm(workflow, instanceInfo, 45);

        Solution solution = hbmoAlgorithm.runAlgorithm();

        printSolution(solution, instanceInfo);
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

    private static void printSolution(Solution solution, InstanceInfo instanceInfo[]){
        Log.logger.info("Number of used Instances: " + solution.numberOfUsedInstances);

        for (int i = 0; i < solution.instanceTimes.length; i++) {
            Log.logger.info("Requested time for instance " + instanceInfo[solution.yArray[i]].getType().getName() + " : " + solution.instanceTimes[i]);
        }

        for (int i = 0; i < solution.instanceTimes.length; i++) {
            Log.logger.info("Timeline for instance " + instanceInfo[solution.yArray[i]].getType().getName() + " : " + solution.instanceTimelines[i]);
        }

        String xArray = "";
        for (int val : solution.xArray){
            xArray += " " + String.valueOf(val);
        }
        Log.logger.info("Value of the X Array: "+ xArray);

        String yArray = "";
        for (int i = 0; i < solution.numberOfUsedInstances; i++) {
            yArray += " " + String.valueOf(solution.yArray[i]);
        }
        Log.logger.info("Value of the Y Array: "+ yArray);

        Log.logger.info("Total Cost: " + solution.cost);
        Log.logger.info("Makespan: " + solution.makespan);
        Log.logger.info("Fitness Value: "+ solution.fitnessValue);
    }

}
