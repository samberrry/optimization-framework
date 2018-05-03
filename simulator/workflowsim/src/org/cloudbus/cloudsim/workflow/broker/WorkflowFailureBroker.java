package org.cloudbus.cloudsim.workflow.broker;

import static org.cloudbus.cloudsim.util.workload.TaskState.POSTPONED;
import static org.cloudbus.cloudsim.util.workload.TaskState.READY;

import java.util.ArrayList;
import java.util.logging.Level;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.util.workload.Task;
import org.cloudbus.cloudsim.util.workload.TaskState;
import org.cloudbus.cloudsim.util.workload.Workflow;
import org.cloudbus.cloudsim.util.workload.Job.JobStatus;
import org.cloudbus.cloudsim.workflow.failure.FailureBroker;
import org.cloudbus.spotsim.ComputeCloudTags;
import org.cloudbus.spotsim.broker.SchedulingDecision;
import org.cloudbus.spotsim.broker.resources.Resource;
import org.cloudbus.spotsim.cloudprovider.ComputeCloud;
import org.cloudbus.spotsim.enums.AZ;
import org.cloudbus.spotsim.enums.FaultToleranceMethod;
import org.cloudbus.spotsim.main.config.SimProperties;
import org.cloudbus.spotsim.simrecords.SimulationData;

public class WorkflowFailureBroker extends WorkflowBroker {

	FailureBroker failureBroker;
	public WorkflowFailureBroker(String name, ComputeCloud provider,
			WorkflowSchedulingPolicy policy, Workflow workflow)
			throws Exception {
		super(name, provider, policy, workflow);
	}
	
	protected void instanceCreated(final long requestToken, final AZ az) {
		super.instanceCreated(requestToken, az);
		final Resource resource = this.requests.get(requestToken);
		if (resource != null) {
			this.sendNow(failureBroker.getId(), ComputeCloudTags.FAIL_RESOURCE, resource);
		}
		
	}
	
	
	protected void submitTaskToCloud(final Resource resource, final Task task) {
		super.submitTaskToCloud(resource,task);
		//this.sendNow(failureBroker.getId(), ComputeCloudTags.TASK_FAILED, task);
	}
	
	protected void allocateTask(final Task task) {

		final TaskState state = task.getState();
		if (state != READY && state != POSTPONED) {
			throw new IllegalArgumentException(
					"Only READY and POSTPONED tasks can be scheduled. Probably a bug. State of task "
							+ task.getId() + " is " + state);
		}

		/* Call the actual scheduling policy */
		ArrayList<SchedulingDecision> schedule = this.policy.schedule(task,
				this.resources.getAllUsableResources(), reachedMax(),
				this.provider.getRegion());

		for (SchedulingDecision dec : schedule) {

			final long maxWaitTime = dec.getMaxWaitTime();
			if (dec.postponed()) {
				this.postpone(dec.getTask(), maxWaitTime);
			} else {
				dec.checkSanity();
				SimulationData.singleton().jobScheduled(dec);

				Resource resource;
				if (dec.isStartNewInstance()) {
					dec.getTask().scheduled(dec.getEstimatedRuntime(), maxWaitTime);
					resource = this
							.requestNewInstance(dec.getTask(), dec.getRegion(),
									dec.getAz(), dec.getInstanceType(),
									dec.getOs(), dec.getBidPrice(),
									maxWaitTime, dec.getPriceModel());
					dec.getTask().getJob().setStatus(JobStatus.RUNNING);
				} else {
					resource = dec.getResource();
					dec.getTask().setResource(resource);
					dec.getTask().scheduled(dec.getEstimatedRuntime(), maxWaitTime);
					this.runTaskOnExistingInstance(dec.getTask(), resource);
					dec.getTask().getJob().setStatus(JobStatus.RUNNING);
				}

				if (Log.logger.isLoggable(Level.INFO)) {
					Log.logger.info(Log.clock()
							+ " Scheduling Decision: "
							+ dec
							+ ", total cost: "
							+ SimulationData.singleton().getStats()
									.getEstimatedCost()
							+ ", total runtime: "
							+ SimulationData.singleton().getStats()
									.getEstimatedRunTime() + ", Resource: "
							+ resource.getId());
				}
			}}
	}

	   
	public FailureBroker getFailureBroker() {
		return failureBroker;
	}
	public void setFailureBroker(FailureBroker failureBroker) {
		this.failureBroker = failureBroker;
	}

}
