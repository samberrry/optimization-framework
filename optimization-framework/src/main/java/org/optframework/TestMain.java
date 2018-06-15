package org.optframework;

import org.cloudbus.cloudsim.util.workload.Job;
import org.cloudbus.cloudsim.util.workload.Workflow;
import org.cloudbus.cloudsim.util.workload.WorkflowDAG;
import org.optframework.config.Config;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TestMain {
    public static void main(String[] args) {

        Workflow simpleWorkflow = new Workflow(6, 1000, 1000);

        simpleWorkflow.initBudget(1000);
        simpleWorkflow.setDeadline(5000);

        /**
         * Defining a simple workflow A->B, A->C, B->D, B->E, D->F, E->F, C->F
         * */

        int taskID = 0;

        int groupID = 1;
        int userID = 1;
        long submitTime = 0 ;
        int numProc = 1;
//
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

        WorkflowDAG dag = simpleWorkflow.getWfDAG();

        Yaml yaml = new Yaml();

        InputStream in = null;
        try {
            in = Files.newInputStream( Paths.get(Config.configPath) );
        } catch (IOException e) {
            e.printStackTrace();
        }

        Config config = null;
        try {
            config = yaml.loadAs( in, Config.class );
        } catch (Exception e) {
            System.out.println("An exception occurred");
        }

    }
}
