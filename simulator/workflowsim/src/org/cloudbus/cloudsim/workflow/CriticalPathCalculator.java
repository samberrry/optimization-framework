package org.cloudbus.cloudsim.workflow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import org.cloudbus.cloudsim.util.workload.Job;
import org.cloudbus.cloudsim.util.workload.Workflow;
import org.cloudbus.cloudsim.util.workload.WorkflowUtilis;
import org.cloudbus.cloudsim.workflow.Models.DAX.Dax2Workflow;
import org.cloudbus.spotsim.broker.ModelParameters;
import org.cloudbus.spotsim.broker.rtest.RecentAverage;
import org.cloudbus.spotsim.enums.InstanceType;

public class CriticalPathCalculator {

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		
		
		String dagFile = "C:\\Documents and Settings\\deepakc\\My Documents\\cloudsim-workflows.tar\\cloudsim-workflows\\Sipht_30.xml";
		WorkflowUtilis wfUtil = createWorkflow(dagFile);
		long conservativeCriticalPath = getConservativeCriticalPath(wfUtil);
    	System.out.println(" Normal Critical Path of small CyberShake " + conservativeCriticalPath);
    	//long liberalCriticalPath = getLiberalCriticalPath(wfUtil);
    	//System.out.println(" Liberal Critical Path of small CyberShake  " + liberalCriticalPath);
    	
    /*	dagFile = "C:\\Documents and Settings\\deepakc\\My Documents\\cloudsim-workflows.tar\\cloudsim-workflows\\Montage_1000.xml";
		wfUtil = createWorkflow(dagFile);
		conservativeCriticalPath = getConservativeCriticalPath(wfUtil);
    	System.out.println(" Normal Critical Path of CyberShake " + conservativeCriticalPath);
    	//liberalCriticalPath = getLiberalCriticalPath(wfUtil);
    	//System.out.println(" Liberal Critical Path of CyberShake " + liberalCriticalPath);
    	
    	dagFile = "C:\\Documents and Settings\\deepakc\\My Documents\\cloudsim-workflows.tar\\cloudsim-workflows\\CyberShake_1000.xml";
		wfUtil = createWorkflow(dagFile);
		conservativeCriticalPath = getConservativeCriticalPath(wfUtil);
    	System.out.println(" Normal Critical Path of CyberShake " + conservativeCriticalPath);
    	//liberalCriticalPath = getLiberalCriticalPath(wfUtil);
    	//System.out.println(" Liberal Critical Path of CyberShake " + liberalCriticalPath);
    	
    	dagFile = "C:\\Documents and Settings\\deepakc\\My Documents\\cloudsim-workflows.tar\\cloudsim-workflows\\Inspiral_1000.xml";
		wfUtil = createWorkflow(dagFile);
		conservativeCriticalPath = getConservativeCriticalPath(wfUtil);
    	System.out.println(" Normal Critical Path of LIGO " + conservativeCriticalPath);
    	//liberalCriticalPath = getLiberalCriticalPath(wfUtil);
    	//System.out.println(" Liberal Critical Path of LIGO " + liberalCriticalPath);
    	
    	dagFile = "C:\\Documents and Settings\\deepakc\\My Documents\\cloudsim-workflows.tar\\cloudsim-workflows\\Epigenomics_997.xml";
		wfUtil = createWorkflow(dagFile);
		conservativeCriticalPath = getConservativeCriticalPath(wfUtil);
    	System.out.println(" Normal Critical Path of Epigenomics " + conservativeCriticalPath);
    	//liberalCriticalPath = getLiberalCriticalPath(wfUtil);
    	//System.out.println(" Liberal Critical Path of Epigenomics " + liberalCriticalPath);
    	
    	dagFile = "C:\\Documents and Settings\\deepakc\\My Documents\\cloudsim-workflows.tar\\cloudsim-workflows\\Sipht_1000.xml";
		wfUtil = createWorkflow(dagFile);
		conservativeCriticalPath = getConservativeCriticalPath(wfUtil);
    	System.out.println(" Normal Critical Path of SIPHT " + conservativeCriticalPath);
    	//liberalCriticalPath = getLiberalCriticalPath(wfUtil);
    	//System.out.println(" Liberal Critical Path of SIPHT " + liberalCriticalPath);
*/
	}
	private static WorkflowUtilis createWorkflow(String dagFile){
		Workflow simpleWorkflow;
		Dax2Workflow dax = new Dax2Workflow();
    	dax.processDagFile(dagFile, 1, 100,0);		
	    simpleWorkflow = dax.workflow;
	    simpleWorkflow.setDeadline(Long.MAX_VALUE);
	    simpleWorkflow.setBudget(Double.MAX_VALUE);
	    WorkflowUtilis wfUtil = new WorkflowUtilis(simpleWorkflow);
	    
	    return wfUtil;
	}

	private static long getConservativeCriticalPath(WorkflowUtilis wfUtil) {
		
    	for(final Integer node : wfUtil.getWorkflow().getWfDAG().getFirstLevel()){
    		wfUtil.computeCriticalPath(node);
    	}
		return wfUtil.getCriticalPath();
	}

//	private static long getLiberalCriticalPath(WorkflowUtilis wfUtil){
//		return computeLiberalCP(wfUtil.getCriticalPathJobs(), InstanceType.C1XLARGE);
//
//	}
	 private static long computeLiberalCP(ArrayList<Job> criticalPathJobs, InstanceType type) {
	    	
		RecentAverage recentAverage = new RecentAverage();
		Collections.reverse(criticalPathJobs);
		long criticalPathTime = 0;
		Job prevJob = null;
		for (Job job : criticalPathJobs) {
			if(prevJob != null){
				criticalPathTime += prevJob.getEdge(job.getIntId());
			}
			System.out.println(" Job lenth " + job.getLength());
			criticalPathTime += ModelParameters.execTimeParallel(job.getA(), job.getSigma(), type.getEc2units(),
					recentAverage.estimateJobLength(job));
			prevJob = job;
			System.out.println(" Job id "+ job.getIntId() + " Critical Path " + criticalPathTime  );
		}

		return criticalPathTime;
	}
	
}
