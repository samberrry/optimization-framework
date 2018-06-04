package org.optframework;

import com.rits.cloning.Cloner;
import org.cloudbus.cloudsim.util.workload.Job;
import org.cloudbus.cloudsim.util.workload.Workflow;
import org.cloudbus.cloudsim.util.workload.WorkflowDAG;
import org.cloudbus.cloudsim.workflow.Models.DAX.Dax2Workflow;
import org.cloudbus.spotsim.main.config.SimProperties;
import org.optframework.core.ReadyTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TestMain {
    public static void main(String[] args) {
//        SimProperties s = SimProperties.PRICING_HISTORY_MAP_DIR;
//
//        System.out.println(SimProperties.PRICING_HISTORY_MAP_DIR.getValue());
//        System.out.println();
//
//        Dax2Workflow dax = new Dax2Workflow();
//        dax.processDagFile(SimProperties.WORKFLOW_FILE_DAG.asString()
//                , 1, 100,0);
//
//        Workflow workflow = dax.workflow;
//        workflow.initBudget(1000);
//        workflow.setDeadline(4000);


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
//
//
        WorkflowDAG dag = simpleWorkflow.getWfDAG();
//        WorkflowDAG dag = workflow.getWfDAG();
//
//        ArrayList<Integer> arrayList = dag.getFirstLevel();
//
//        ArrayList<Integer> nextArray = dag.getNextLevel(arrayList);
//
//        ArrayList<Integer> nextArray2 = dag.getNextLevel(nextArray);
//
//        ArrayList<Integer> nextArray3 = dag.getNextLevel(nextArray2);
//
//        ArrayList<Integer> nexetArray4 = dag.getNextLevel(nextArray3);

//        dag.getFirstLevel()
//        dag.getChildren();
//        dag.getLastLevel();
//        dag.getNode();
//        dag.printDAG();

        ArrayList<Job> jobArrayList = (ArrayList<Job>) simpleWorkflow.getJobList();
//        Job job2 = jobArrayList.get(10);
//        job2.setLength(550000);
//        job2.setReqRunTime(550000);
//
//        Job job = dag.getCriticalParent(14);
//        ArrayList<Integer> arrayList = dag.getParents(14);
//

        ArrayList<Integer> arrayList = dag.getFirstLevel();

        ArrayList<Integer> parents = dag.getParents(arrayList.get(0));

        int a =1;

        Cloner cloner = new Cloner();
        Workflow workflow = cloner.deepClone(simpleWorkflow);

        workflow.setDeadline(123123123);
        workflow.setBudget(987654);

        ArrayList<Job> jobArrayList2 = (ArrayList<Job>) workflow.getJobList();
        Job job2 = jobArrayList2.get(1);
        job2.setCriticalPathWeight(23456);
        job2.setLength(987654);



//        ==============

        ArrayList<ReadyTask> readyTasks = new ArrayList<>();
        readyTasks.add(new ReadyTask(123312,343));
        readyTasks.add(new ReadyTask(1212,3434));
        readyTasks.add(new ReadyTask(2,134));
        readyTasks.add(new ReadyTask(2,1));
        readyTasks.add(new ReadyTask(1,234));
        readyTasks.add(new ReadyTask(1,876567));

        Collections.sort(readyTasks);
        int array[] = {12,2,3,4,5,6,32,45,6,6,78,89,89};
        List intArray = Arrays.asList(array);
        Collections.sort(intArray);

        int a2=3;

    }
}
