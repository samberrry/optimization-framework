package org.optframework.core.utils;

import org.cloudbus.cloudsim.util.workload.Job;
import org.cloudbus.cloudsim.util.workload.Workflow;
import org.cloudbus.cloudsim.workflow.Models.DAX.Dax2Workflow;
import org.cloudbus.spotsim.main.config.SimProperties;
import org.optframework.core.Log;

public class PopulateWorkflow {
    public static Workflow populateWorkflowFromDax(double budget, long deadline) {
        Log.logger.info("Populates the workflow from Dax file");

        Dax2Workflow dax = new Dax2Workflow();
        dax.processDagFile(SimProperties.WORKFLOW_FILE_DAG.asString()
                , 1, 100,0);

        Workflow workflow = dax.workflow;
        workflow.initBudget(budget);
        workflow.setDeadline(deadline);

        return workflow;
    }

    public static Workflow populateWorkflowWithId(double budget, long deadline, int workflow_id) {
        Log.logger.info("Populates the workflow from Dax file");

        String workflowPath = "";
        Workflow workflow = null;

        if (workflow_id < 100){
            switch (workflow_id){
                case 1:
                    workflowPath = "resources/input/inputDAGfiles/Inspiral_1000.xml";
                    break;
                case 2:
                    workflowPath = "resources/input/inputDAGfiles/Inspiral_100.xml";
                    break;
                case 3:
                    workflowPath = "resources/input/inputDAGfiles/Inspiral_50.xml";
                    break;
                case 4:
                    workflowPath = "resources/input/inputDAGfiles/Inspiral_30.xml";
                    break;
            }

            if (workflowPath == ""){
                throw new RuntimeException("Invalid workflow Id. Possible values: 1 for 1000 nodes, 2 for 100 nodes , 3 for 30 nodes");
            }

            Dax2Workflow dax = new Dax2Workflow();
            dax.processDagFile(workflowPath
                    , 1, 100,0);

            workflow = dax.workflow;
            workflow.initBudget(budget);
            workflow.setDeadline(deadline);
        }else {
            switch (workflow_id){
                case 101:
                    return populateSimpleWorkflow(budget, deadline);
                case 102:
                    return populateSimpleWorkflow2(budget, deadline);
                case 103:
                    return populateSimpleWorkflow3(budget, deadline);
                case 104:
                    return populateSimpleWorkflow4(budget, deadline);
                case 105:
                    return populateSimpleWorkflow5(budget, deadline);
                case 106:
                    return populateSimpleWorkflow6(budget, deadline);
                case 107:
                    return populateSimpleWorkflow7(budget, deadline);
                case 108:
                    return populateSimpleWorkflow8(budget, deadline);
                case 200:
                    return populateHEFTExample(budget, deadline);
            }
        }

        return workflow;
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

        Job wfA = new Job(taskID, submitTime, 3600 , userID, groupID, 3600, numProc);
        simpleWorkflow.createTask(wfA);
        taskID++;

        Job wfB = new Job(taskID, submitTime, 1200 , userID, groupID, 1200, numProc);
        simpleWorkflow.createTask(wfB);
        taskID++;

        Job wfC = new Job(taskID, submitTime, 22000 , userID, groupID, 22000, numProc);
        simpleWorkflow.createTask(wfC);
        taskID++;

        Job wfD = new Job(taskID, submitTime, 8600 , userID, groupID, 8600, numProc);
        simpleWorkflow.createTask(wfD);
        taskID++;

        Job wfE = new Job(taskID, submitTime, 2200 , userID, groupID, 2200, numProc);
        simpleWorkflow.createTask(wfE);
        taskID++;

        Job wfF = new Job(taskID, submitTime, 11000, userID, groupID, 11000, numProc);
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

    public static Workflow populateSimpleWorkflow2(double budget, long deadline){
        Log.logger.info("Populates the workflow from the simple workflow");

        Workflow simpleWorkflow = new Workflow(5, 1000, 2);

        simpleWorkflow.initBudget(budget);
        simpleWorkflow.setDeadline(deadline);

        /**
         * Defining a simple workflow A->B B->D, B->E, D->F, E->F
         * */

        int taskID = 0;

        int groupID = 1;
        int userID = 1;
        long submitTime = 0 ;
        int numProc = 1;

        Job wfA = new Job(taskID, submitTime, 300 , userID, groupID, 300, numProc);
        simpleWorkflow.createTask(wfA);
        taskID++;

        Job wfB = new Job(taskID, submitTime, 10 , userID, groupID, 10, numProc);
        simpleWorkflow.createTask(wfB);
        taskID++;

        Job wfD = new Job(taskID, submitTime, 50 , userID, groupID, 50, numProc);
        simpleWorkflow.createTask(wfD);
        taskID++;

        Job wfE = new Job(taskID, submitTime, 100 , userID, groupID, 100, numProc);
        simpleWorkflow.createTask(wfE);
        taskID++;

        Job wfF = new Job(taskID, submitTime, 40 , userID, groupID, 40, numProc);
        simpleWorkflow.createTask(wfF);
        taskID++;

        simpleWorkflow.addEdge(wfA, wfB, 0);
        simpleWorkflow.addEdge(wfB, wfD, 0);
        simpleWorkflow.addEdge(wfB, wfE, 0);
        simpleWorkflow.addEdge(wfD, wfF, 0);
        simpleWorkflow.addEdge(wfE, wfF, 0);

        return simpleWorkflow;
    }

    public static Workflow populateSimpleWorkflow3(double budget, long deadline){
        Log.logger.info("Populates the workflow from the simple workflow");

        Workflow simpleWorkflow = new Workflow(5, deadline, budget);

        simpleWorkflow.initBudget(budget);
        simpleWorkflow.setDeadline(deadline);

        /**
         * Defining a simple workflow A->B B->D, B->E, D->F, E->F
         * */

        int taskID = 0;

        int groupID = 1;
        int userID = 1;
        long submitTime = 0 ;
        int numProc = 1;

        Job wfA = new Job(taskID, submitTime, 1200 , userID, groupID, 1200, numProc);
        simpleWorkflow.createTask(wfA);
        taskID++;

        Job wfB = new Job(taskID, submitTime, 2100 , userID, groupID, 2100, numProc);
        simpleWorkflow.createTask(wfB);
        taskID++;

        Job wfD = new Job(taskID, submitTime, 5600 , userID, groupID, 5600, numProc);
        simpleWorkflow.createTask(wfD);
        taskID++;

        Job wfE = new Job(taskID, submitTime, 5500 , userID, groupID, 5500, numProc);
        simpleWorkflow.createTask(wfE);
        taskID++;

        Job wfF = new Job(taskID, submitTime, 2300 , userID, groupID, 2300, numProc);
        simpleWorkflow.createTask(wfF);
        taskID++;

        simpleWorkflow.addEdge(wfA, wfB, 0);
        simpleWorkflow.addEdge(wfB, wfD, 0);
        simpleWorkflow.addEdge(wfB, wfE, 0);
        simpleWorkflow.addEdge(wfD, wfF, 0);
        simpleWorkflow.addEdge(wfE, wfF, 0);

        return simpleWorkflow;
    }

    public static Workflow populateSimpleWorkflow4(double budget, long deadline){
        Log.logger.info("Populates the workflow from the simple workflow");

        Workflow simpleWorkflow = new Workflow(5, 1000, 1000);

        simpleWorkflow.initBudget(budget);
        simpleWorkflow.setDeadline(deadline);

        /**
         * Defining a simple workflow A->D A->C, B->D, C->E, D->E
         * */

        int taskID = 0;

        int groupID = 1;
        int userID = 1;
        long submitTime = 0 ;
        int numProc = 1;

        Job wfA = new Job(taskID, submitTime, 50 , userID, groupID, 50, numProc);
        simpleWorkflow.createTask(wfA);
        taskID++;

        Job wfB = new Job(taskID, submitTime, 100 , userID, groupID, 100, numProc);
        simpleWorkflow.createTask(wfB);
        taskID++;

        Job wfC = new Job(taskID, submitTime, 30 , userID, groupID, 30, numProc);
        simpleWorkflow.createTask(wfC);
        taskID++;

        Job wfD = new Job(taskID, submitTime, 40 , userID, groupID, 40, numProc);
        simpleWorkflow.createTask(wfD);
        taskID++;

        Job wfE = new Job(taskID, submitTime, 10 , userID, groupID, 10, numProc);
        simpleWorkflow.createTask(wfE);
        taskID++;

        simpleWorkflow.addEdge(wfA, wfC, 0);
        simpleWorkflow.addEdge(wfA, wfD, 0);
        simpleWorkflow.addEdge(wfB, wfD, 0);
        simpleWorkflow.addEdge(wfC, wfE, 0);
        simpleWorkflow.addEdge(wfD, wfE, 0);

        return simpleWorkflow;
    }

    public static Workflow populateSimpleWorkflow5(double budget, long deadline){
        Log.logger.info("Populates the workflow from the simple workflow");

        Workflow simpleWorkflow = new Workflow(3, 1000, 1000);

        simpleWorkflow.initBudget(budget);
        simpleWorkflow.setDeadline(deadline);

        /**
         * Defining a simple workflow A->C B->C
         * */

        int taskID = 0;

        int groupID = 1;
        int userID = 1;
        long submitTime = 0 ;
        int numProc = 1;

        Job wfA = new Job(taskID, submitTime, 3000 , userID, groupID, 3000, numProc);
        simpleWorkflow.createTask(wfA);
        taskID++;

        Job wfB = new Job(taskID, submitTime, 10000 , userID, groupID, 10000, numProc);
        simpleWorkflow.createTask(wfB);
        taskID++;

        Job wfC = new Job(taskID, submitTime, 50000 , userID, groupID, 50000, numProc);
        simpleWorkflow.createTask(wfC);
        taskID++;

        simpleWorkflow.addEdge(wfA, wfC, 0);
        simpleWorkflow.addEdge(wfB, wfC, 0);

        return simpleWorkflow;
    }

    public static Workflow populateSimpleWorkflow6(double budget, long deadline){
        Log.logger.info("Populates the workflow from the simple workflow");

        Workflow simpleWorkflow = new Workflow(10, 1000, 1000);

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

        Job wfA = new Job(taskID, submitTime, 3600 , userID, groupID, 3600, numProc);
        simpleWorkflow.createTask(wfA);
        taskID++;

        Job wfB = new Job(taskID, submitTime, 1200 , userID, groupID, 1200, numProc);
        simpleWorkflow.createTask(wfB);
        taskID++;

        Job wfC = new Job(taskID, submitTime, 22000 , userID, groupID, 22000, numProc);
        simpleWorkflow.createTask(wfC);
        taskID++;

        Job wfD = new Job(taskID, submitTime, 8600 , userID, groupID, 8600, numProc);
        simpleWorkflow.createTask(wfD);
        taskID++;

        Job wfE = new Job(taskID, submitTime, 2200 , userID, groupID, 2200, numProc);
        simpleWorkflow.createTask(wfE);
        taskID++;

        Job wfF = new Job(taskID, submitTime, 11000, userID, groupID, 11000, numProc);
        simpleWorkflow.createTask(wfF);
        taskID++;

        Job wfG = new Job(taskID, submitTime, 8000, userID, groupID, 8000, numProc);
        simpleWorkflow.createTask(wfG);
        taskID++;

        Job wfH = new Job(taskID, submitTime, 5600, userID, groupID, 5600, numProc);
        simpleWorkflow.createTask(wfH);
        taskID++;

        Job wfI = new Job(taskID, submitTime, 17000, userID, groupID, 17000, numProc);
        simpleWorkflow.createTask(wfI);
        taskID++;

        Job wfJ = new Job(taskID, submitTime, 1200, userID, groupID, 1200, numProc);
        simpleWorkflow.createTask(wfJ);
        taskID++;

        simpleWorkflow.addEdge(wfA, wfB, 0);
        simpleWorkflow.addEdge(wfA, wfC, 0);
        simpleWorkflow.addEdge(wfB, wfD, 0);
        simpleWorkflow.addEdge(wfB, wfE, 0);
        simpleWorkflow.addEdge(wfC, wfF, 0);
        simpleWorkflow.addEdge(wfF, wfH, 0);
        simpleWorkflow.addEdge(wfF, wfI, 0);
        simpleWorkflow.addEdge(wfD, wfG, 0);
        simpleWorkflow.addEdge(wfE, wfG, 0);
        simpleWorkflow.addEdge(wfG, wfJ, 0);
        simpleWorkflow.addEdge(wfH, wfJ, 0);
        simpleWorkflow.addEdge(wfI, wfJ, 0);

        return simpleWorkflow;
    }

    public static Workflow populateSimpleWorkflow7(double budget, long deadline){
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

        Job wfA = new Job(taskID, submitTime, 300 , userID, groupID, 300, numProc);
        simpleWorkflow.createTask(wfA);
        taskID++;

        Job wfB = new Job(taskID, submitTime, 10 , userID, groupID, 10, numProc);
        simpleWorkflow.createTask(wfB);
        taskID++;

        Job wfC = new Job(taskID, submitTime, 200 , userID, groupID, 200, numProc);
        simpleWorkflow.createTask(wfC);
        taskID++;

        Job wfD = new Job(taskID, submitTime, 80 , userID, groupID, 80, numProc);
        simpleWorkflow.createTask(wfD);
        taskID++;

        Job wfE = new Job(taskID, submitTime, 30 , userID, groupID, 30, numProc);
        simpleWorkflow.createTask(wfE);
        taskID++;

        Job wfF = new Job(taskID, submitTime, 100, userID, groupID, 100, numProc);
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

    public static Workflow populateSimpleWorkflow8(double budget, long deadline){
        Log.logger.info("Populates the workflow from the simple workflow");

        Workflow simpleWorkflow = new Workflow(4, 1000, 1000);

        simpleWorkflow.initBudget(budget);
        simpleWorkflow.setDeadline(deadline);

        /**
         * Defining a simple workflow A->C B->D C->D
         * */

        int taskID = 0;

        int groupID = 1;
        int userID = 1;
        long submitTime = 0 ;
        int numProc = 1;

        Job wfA = new Job(taskID, submitTime, 1000 , userID, groupID, 1000, numProc);
        simpleWorkflow.createTask(wfA);
        taskID++;

        Job wfB = new Job(taskID, submitTime, 1500 , userID, groupID, 1500, numProc);
        simpleWorkflow.createTask(wfB);
        taskID++;

        Job wfC = new Job(taskID, submitTime, 3000 , userID, groupID, 3000, numProc);
        simpleWorkflow.createTask(wfC);
        taskID++;

        Job wfD = new Job(taskID, submitTime, 200 , userID, groupID, 200, numProc);
        simpleWorkflow.createTask(wfD);
        taskID++;

        simpleWorkflow.addEdge(wfA, wfC, 0);
        simpleWorkflow.addEdge(wfB, wfD, 0);
        simpleWorkflow.addEdge(wfC, wfD, 0);

        return simpleWorkflow;
    }

    public static Workflow populateHEFTExample(double budget, long deadline){
        Log.logger.info("Populates the workflow from the simple workflow");

        Workflow simpleWorkflow = new Workflow(10, 1000, 1000);

        simpleWorkflow.initBudget(budget);
        simpleWorkflow.setDeadline(deadline);

        int taskID = 0;

        int groupID = 1;
        int userID = 1;
        long submitTime = 0 ;
        int numProc = 1;

        Job wfA = new Job(taskID, submitTime, 0 , userID, groupID, 0, numProc);
        simpleWorkflow.createTask(wfA);
        taskID++;

        Job wfB = new Job(taskID, submitTime, 0 , userID, groupID, 0, numProc);
        simpleWorkflow.createTask(wfB);
        taskID++;

        Job wfC = new Job(taskID, submitTime, 0 , userID, groupID, 0, numProc);
        simpleWorkflow.createTask(wfC);
        taskID++;

        Job wfD = new Job(taskID, submitTime, 0 , userID, groupID, 0, numProc);
        simpleWorkflow.createTask(wfD);
        taskID++;

        Job wfE = new Job(taskID, submitTime, 0 , userID, groupID, 0, numProc);
        simpleWorkflow.createTask(wfE);
        taskID++;

        Job wfF = new Job(taskID, submitTime, 0, userID, groupID, 0, numProc);
        simpleWorkflow.createTask(wfF);
        taskID++;

        Job wfG = new Job(taskID, submitTime, 0, userID, groupID, 0, numProc);
        simpleWorkflow.createTask(wfG);
        taskID++;

        Job wfH = new Job(taskID, submitTime, 0, userID, groupID, 0, numProc);
        simpleWorkflow.createTask(wfH);
        taskID++;

        Job wfI = new Job(taskID, submitTime, 0, userID, groupID, 0, numProc);
        simpleWorkflow.createTask(wfI);
        taskID++;

        Job wfJ = new Job(taskID, submitTime, 0, userID, groupID, 0, numProc);
        simpleWorkflow.createTask(wfJ);
        taskID++;

        simpleWorkflow.addEdge(wfA, wfB, 18);
        simpleWorkflow.addEdge(wfA, wfC, 12);
        simpleWorkflow.addEdge(wfA, wfD, 9);
        simpleWorkflow.addEdge(wfA, wfE, 11);
        simpleWorkflow.addEdge(wfA, wfF, 14);
        simpleWorkflow.addEdge(wfB, wfH, 19);
        simpleWorkflow.addEdge(wfB, wfI, 16);
        simpleWorkflow.addEdge(wfC, wfG, 23);
        simpleWorkflow.addEdge(wfD, wfH, 27);
        simpleWorkflow.addEdge(wfD, wfI, 23);
        simpleWorkflow.addEdge(wfE, wfI, 13);
        simpleWorkflow.addEdge(wfF, wfH, 15);
        simpleWorkflow.addEdge(wfG, wfJ, 17);
        simpleWorkflow.addEdge(wfH, wfJ, 11);
        simpleWorkflow.addEdge(wfI, wfJ, 13);

        return simpleWorkflow;
    }

}
