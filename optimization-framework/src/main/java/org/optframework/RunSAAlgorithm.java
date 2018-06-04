package org.optframework;

import org.cloudbus.cloudsim.util.workload.Job;
import org.cloudbus.cloudsim.util.workload.Workflow;
import org.cloudbus.cloudsim.workflow.Models.DAX.Dax2Workflow;
import org.cloudbus.spotsim.enums.AZ;
import org.cloudbus.spotsim.enums.InstanceType;
import org.cloudbus.spotsim.enums.OS;
import org.cloudbus.spotsim.enums.Region;
import org.cloudbus.spotsim.main.config.Config;
import org.cloudbus.spotsim.main.config.SimProperties;
import org.cloudbus.spotsim.pricing.PriceRecord;
import org.cloudbus.spotsim.pricing.SpotPriceHistory;
import org.cloudbus.spotsim.pricing.db.PriceDB;
import org.optframework.config.StaticProperties;
import org.optframework.core.InstanceInfo;
import org.optframework.core.Log;
import org.optframework.core.SimulatedAnnealingAlgorithm;
import org.optframework.core.Solution;

/**
 * @author Hessam Modabberi hessam.modaberi@gmail.com
 * @version 1.0.0
 */

public class RunSAAlgorithm implements StaticProperties {

    public static void main( String[] args ) throws Exception
    {
        Log.init();

        /**
         * Initializes Cloudsim Logger
         * */
        org.cloudbus.cloudsim.Log.init("cloudsim.log");

        Log.logger.info("Loads configs");
        Config.load(null);

        Workflow workflow = populateSimpleWorkflow(1000, 0);
        Log.logger.info("Maximum number of instances: " + M_NUMBER + " Number of different types of instances: " + N_TYPES + " Number of tasks: "+ workflow.getJobList().size());

        /**
         * Assumptions:
         * Region: europe
         * Availability Zone: A
         * OS type: Linux System
         * */
        SimulatedAnnealingAlgorithm saAlgorithm = new SimulatedAnnealingAlgorithm(workflow, populateInstancePrices(Region.EUROPE , AZ.A, OS.LINUX));

//      SimulatedAnnealingAlgorithm saAlgorithm = new SimulatedAnnealingAlgorithm(populateWorkflowFromDax(1000, 0));
        Solution solution = saAlgorithm.runSA();
        Log.logger.info("Total Cost: " + solution.getCost());
        System.out.println("Total Cost: " + solution.getCost());
    }

    public static Workflow populateSimpleWorkflow(double budget, long deadline){
        Log.logger.info("Populates the workflow from the simple workflow");

        Workflow simpleWorkflow = new Workflow(6, 1000, 1000);

        simpleWorkflow.initBudget(budget);
        simpleWorkflow.setDeadline(deadline);

        /**
         * Defining a simple workflow A->B, A->C, B->D, B->E, D->F, E->F, C->F
         * */

        int taskID = 0;

        int groupID = 1;
        int userID = 1;
        long submitTime = 0 ;
        int numProc = 1;

        Job wfA = new Job(taskID, submitTime, 360 , userID, groupID, 360, numProc);
        simpleWorkflow.createTask(wfA);
        taskID++;

        Job wfB = new Job(taskID, submitTime, 1080 , userID, groupID, 1080, numProc);
        simpleWorkflow.createTask(wfB);
        taskID++;

        Job wfC = new Job(taskID, submitTime, 15360 , userID, groupID, 15360, numProc);
        simpleWorkflow.createTask(wfC);
        taskID++;

        Job wfD = new Job(taskID, submitTime, 1080 , userID, groupID, 1080, numProc);
        simpleWorkflow.createTask(wfD);
        taskID++;

        Job wfE = new Job(taskID, submitTime, 25140 , userID, groupID, 25140, numProc);
        simpleWorkflow.createTask(wfE);
        taskID++;

        Job wfF = new Job(taskID, submitTime, 15360 , userID, groupID, 15360, numProc);
        simpleWorkflow.createTask(wfF);
        taskID++;

        simpleWorkflow.addEdge(wfA, wfB, 0);
        simpleWorkflow.addEdge(wfA, wfC, 0);
        simpleWorkflow.addEdge(wfB, wfD, 0);
        simpleWorkflow.addEdge(wfB, wfE, 0);
        simpleWorkflow.addEdge(wfD, wfF, 0);
        simpleWorkflow.addEdge(wfE, wfF, 0);
        simpleWorkflow.addEdge(wfC, wfF, 0);

        return simpleWorkflow;
    }

    private static Workflow populateWorkflowFromDax(double budget, long deadline) {
        Log.logger.info("Populates the workflow from Dax file");

        Dax2Workflow dax = new Dax2Workflow();
        dax.processDagFile(SimProperties.WORKFLOW_FILE_DAG.asString()
                , 1, 100,0);

        Workflow workflow = dax.workflow;
        workflow.initBudget(budget);
        workflow.setDeadline(deadline);

        return workflow;
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
