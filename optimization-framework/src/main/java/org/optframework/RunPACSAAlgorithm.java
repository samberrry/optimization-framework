package org.optframework;

import org.cloudbus.cloudsim.util.workload.Job;
import org.cloudbus.cloudsim.util.workload.Workflow;
import org.cloudbus.cloudsim.workflow.Models.DAX.Dax2Workflow;
import org.cloudbus.spotsim.main.config.SimProperties;
import org.optframework.core.Log;
import org.optframework.core.pacsa.PACSAOptimization;

/**
 * @author Hessam Modabberi hessam.modaberi@gmail.com
 * @version 1.0.0
 */

public class RunPACSAAlgorithm {

    public static void runPACSA()
    {
        PACSAOptimization pacsaOptimization = new PACSAOptimization(populateSimpleWorkflow(1000, 0));

//        PACSAOptimization saAlgorithm = new PACSAOptimization(populateWorkflowFromDax(1000, 0));

        pacsaOptimization.runAlgorithm();
    }

    private static Workflow populateSimpleWorkflow(double budget, long deadline){
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
        simpleWorkflow.addEdge(wfB, wfD, 2);
        simpleWorkflow.addEdge(wfB, wfE, 2);
        simpleWorkflow.addEdge(wfD, wfF, 1);
        simpleWorkflow.addEdge(wfE, wfF, 0);
        simpleWorkflow.addEdge(wfC, wfF, 2);

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
}
