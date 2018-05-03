package org.cloudbus.cloudsim.workflow.failure;

import org.cloudbus.cloudsim.util.workload.Task;
import org.cloudbus.cloudsim.workflow.broker.WorkflowFailureBroker;
import org.cloudbus.spotsim.broker.resources.Resource;

public class NoFailureModel extends TaskFailureGenerator {

	public NoFailureModel(WorkflowFailureBroker broker) {
		super(broker);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void generateFailure(Task task) {
		// Don't do anything, no failures

	}

	@Override
	public void generateResourceFailure(Resource resource) {
		// TODO Auto-generated method stub
		
	}

}
