package org.cloudbus.cloudsim.workflow.failure;

import org.cloudbus.cloudsim.core.EventTag;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.util.workload.Task;
import org.cloudbus.cloudsim.workflow.broker.WorkflowBroker;
import org.cloudbus.spotsim.ComputeCloudTags;
import org.cloudbus.spotsim.broker.resources.Resource;

public class FailureBroker extends SimEntity {

	TaskFailureGenerator failureGen;
	WorkflowBroker wfBroker;
	public FailureBroker(TaskFailureGenerator failureGen, WorkflowBroker broker) {
		super("FailureBroker");
		this.failureGen = failureGen;
		this.wfBroker = broker;
	}

	@Override
	public void processEvent(SimEvent ev) {
		final EventTag eventTag = ev.getEventTag();
		if (eventTag instanceof ComputeCloudTags) {
		    final ComputeCloudTags tag = (ComputeCloudTags) eventTag;
		    switch (tag) {
		    case DELAY_FAILURE:
		    	failureGen.generateFailure(((Task) ev.getData()));
				break;
		    case TASK_FAILED:
		    	failureGen.generateFailure(((Task) ev.getData()));
		    	break;
		    case DELAY_RESOURCE_FAIL:
		    	failureGen.generateResourceFailure(((Resource) ev.getData()));
		    	break;
		    case FAIL_RESOURCE:
		    	failureGen.generateResourceFailure(((Resource) ev.getData()));
		    	break;
		    	
		    default:
			throw new RuntimeException("Unexpected event "
				+ tag
				+ " cannot be processed by the client");
		    }
		} else {
		    throw new RuntimeException("Unexpected event "
			    + eventTag
			    + " cannot be processed by the client");
		}
	}

	@Override
	public void shutdownEntity() {
		failureGen.closeFiles();

	}

	@Override
	public void startEntity() {
		// TODO Auto-generated method stub

	}

}
