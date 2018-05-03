package org.cloudbus.cloudsim.workflow.failure;

import java.util.Random;
import java.util.Set;
import java.util.logging.Level;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.util.workload.Job;
import org.cloudbus.cloudsim.util.workload.Task;
import org.cloudbus.cloudsim.util.workload.TaskState;
import org.cloudbus.cloudsim.workflow.broker.WorkflowFailureBroker;
import org.cloudbus.spotsim.broker.resources.Resource;

public class RandomTaskFailureModel extends TaskFailureGenerator {

	int i =0;
	public RandomTaskFailureModel(WorkflowFailureBroker broker) {
		super(broker);
		
	}
	
	private void randomFailure(){
		Set<Job> jobList = this.getBroker().getRunningJobs();
		Random rand = new Random();
		for(Job job : jobList){
			double nextGaussian = rand.nextGaussian();
			if(nextGaussian > 0 && nextGaussian < 0.2){
				Task firstRunningTask = getFirstRunningTask(job);
				if(firstRunningTask != null){
					failTask(firstRunningTask, 1000L);
				}
			}
		}
	}

	private Task getFirstRunningTask(Job job){
		for(Task task: job.getTasks()){
			if(task.getState() == TaskState.RUNNING){
				return task;
			}
		}
		return null;
	}

	@Override
	public void generateFailure(Task task) {
		Random rand = new Random(10L);
		double nextGaussian = rand.nextGaussian();
		if(nextGaussian > 0 && nextGaussian < 0.9){
			if(task != null && i <=2){
				failTask(task, 5L);
				i++;
				if (Log.logger.isLoggable(Level.WARNING)) {
					Log.logger.warning(Log.clock()
						+ " Failing Task: "
						+ task.getId()
						);
				}
			}
		}
	}

	@Override
	public void generateResourceFailure(Resource resource) {
		// TODO Auto-generated method stub
		
	}
}
