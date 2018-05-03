package org.cloudbus.cloudsim.workflow;

import org.cloudbus.cloudsim.util.workload.Job;
import org.cloudbus.cloudsim.util.workload.Workflow;


public class WorkflowSimpleExample {

	public static int taskID = 0;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		Workflow simpleWorkflow = new Workflow(6, 1000, 1000);
		/**
		 * Defining a simple workflow A->B, A->C, B->D, B->E, D->F, E->F, C->F
		 * */
		
		int groupID = 1;
		int userID = 1;
		long submitTime = 0 ;
		long len = 100;
		int numProc = 2;
		long reqRunTime = 5000;
		
		Job wfA = new Job(taskID, submitTime, reqRunTime , userID, groupID, len, numProc);
		simpleWorkflow.createTask(wfA);
		taskID++;
		
		Job wfB = new Job(taskID, submitTime, reqRunTime , userID, groupID, len, numProc);
		simpleWorkflow.createTask(wfB);
		taskID++;
		
		Job wfC = new Job(taskID, submitTime, reqRunTime , userID, groupID, len, numProc);
		simpleWorkflow.createTask(wfC);
		taskID++;
		
		Job wfD = new Job(taskID, submitTime, reqRunTime , userID, groupID, len, numProc);
		simpleWorkflow.createTask(wfD);
		taskID++;
		
		Job wfE = new Job(taskID, submitTime, reqRunTime , userID, groupID, len, numProc);
		simpleWorkflow.createTask(wfE);
		taskID++;
		
		Job wfF = new Job(taskID, submitTime, reqRunTime , userID, groupID, len, numProc);
		simpleWorkflow.createTask(wfF);
		taskID++;
		
		simpleWorkflow.addEdge(wfA, wfB, 10);
		simpleWorkflow.addEdge(wfA, wfC, 10);
		simpleWorkflow.addEdge(wfB, wfD, 10);
		simpleWorkflow.addEdge(wfB, wfE, 10);
		simpleWorkflow.addEdge(wfD, wfF, 10);
		simpleWorkflow.addEdge(wfE, wfF, 10);
		simpleWorkflow.addEdge(wfC, wfF, 10);
		
		simpleWorkflow.getWfDAG().printDAG();

	}

}
