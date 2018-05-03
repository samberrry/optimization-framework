package org.cloudbus.cloudsim.workflow.failure;

import java.util.logging.Level;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.util.workload.Task;
import org.cloudbus.cloudsim.workflow.broker.WorkflowBroker;
import org.cloudbus.cloudsim.workflow.broker.WorkflowFailureBroker;
import org.cloudbus.spotsim.ComputeCloudTags;
import org.cloudbus.spotsim.broker.resources.Resource;
import org.cloudbus.spotsim.cloudprovider.ComputeCloudImpl;

public abstract class TaskFailureGenerator {

	WorkflowFailureBroker broker;
	ComputeCloudImpl cloudImpl;

	public TaskFailureGenerator(WorkflowFailureBroker broker) {
		super();
		this.broker = broker;
	}
	
	public WorkflowBroker getBroker(){
		return this.broker;
	}
	
	public void closeFiles() {
		
	}
	
	public void failTask(final Task task, long delay) {
		
		CloudSim.send(broker.getId(), broker.getId(), delay, ComputeCloudTags.TASK_FAILED, task);
		if (Log.logger.isLoggable(Level.INFO)) {
			Log.logger.info(Log.clock()
				+ " Failing Task: "
				+ task.getId()
				+ ", after a delay of: "
				+ delay);
		}
    }
	
	public void delayResourceFailure(final Resource resource, long delay) {
		
		CloudSim.send(broker.getId(), broker.getFailureBroker().getId(), delay, ComputeCloudTags.DELAY_RESOURCE_FAIL, resource);
		if (Log.logger.isLoggable(Level.INFO)) {
			Log.logger.info(Log.clock()
				+ " Delay Resource Failure: "
				+ resource.getId()
				+ ", after a delay of: "
				+ delay);
		}
    }
	
	public void failResource(final Resource resource, long delay) {
		
		CloudSim.send(broker.getId(), broker.getId(), delay, ComputeCloudTags.FAIL_RESOURCE, resource);
		if (Log.logger.isLoggable(Level.INFO)) {
			Log.logger.info(Log.clock()
				+ " Failing Resource: "
				+ resource.getId()
				+ ", after a delay of: "
				+ delay);
		}
    }
	
	public void delayFailure(final Task task, long delay) {
		CloudSim.send(broker.getId(), broker.getFailureBroker().getId(), delay, ComputeCloudTags.DELAY_FAILURE, task);
		/*if (Log.logger.isLoggable(Level.INFO)) {
			Log.logger.info(Log.clock()
				+ " Delay Failure Task: "
				+ task.getId()
				+ ", after a delay of: "
				+ delay);
		}*/
    }

	public abstract void generateFailure(final Task task);
	
	public abstract void generateResourceFailure(final Resource resource);
	
}
