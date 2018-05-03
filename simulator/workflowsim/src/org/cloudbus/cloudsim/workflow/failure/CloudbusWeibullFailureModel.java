package org.cloudbus.cloudsim.workflow.failure;

import java.math.BigInteger;
import java.util.Random;

import org.apache.commons.math3.distribution.WeibullDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well1024a;
import org.apache.commons.math3.random.Well512a;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.distributions.WeibullDistr;
import org.cloudbus.cloudsim.util.workload.Task;
import org.cloudbus.cloudsim.util.workload.Job.JobStatus;
import org.cloudbus.cloudsim.workflow.broker.WorkflowFailureBroker;
import org.cloudbus.spotsim.broker.resources.Resource;
import org.cloudbus.spotsim.main.config.SimProperties;


public class CloudbusWeibullFailureModel extends TaskFailureGenerator {

	public CloudbusWeibullFailureModel(WorkflowFailureBroker broker) {
		super(broker);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void generateFailure(Task task) {
		final double scaleMin = 0.2;
		final double scaleMax = 2;
		final double shapeMin = 0.5;
		final double shapeMax = 5;
		RandomGenerator r = new Well1024a(10L);
		Random rand = new Random(SimProperties.RNG_SEED.asLong());

		if(task.getJob().getStatus() == JobStatus.COMPLETED){
			return;
		}

		WeibullDistr weibull = new WeibullDistr(rand, 0.5, 2);
		double sample2 = weibull.sample();
		
		//WeibullDistribution wd = new WeibullDistribution(r,0.5, 2);
		WeibullDistribution wd = new WeibullDistribution(r,2, 5);
		long sample = (long) wd.sample();
		System.out.println("Task " + task.getId() + "Timmee " + CloudSim.clock());
		System.out.println(" Sampleeeee valueeee ::: " + sample);
		if (sample == 0) {
			Resource resource = task.getResource();
			final double t = ((double)(CloudSim.clock() - resource.getTimeReceived()))/3600;
			//final double timeHrs = ((double) task.getActualStartTime()) / 3600;
			double cumulativeProbability = wd.cumulativeProbability(t);
			
			System.out.println("task "+task.getId()+ " Time hrs " + t+" Cumulative prob" + cumulativeProbability);
			if (rand.nextGaussian() < cumulativeProbability) {
				System.out
						.println("Failllllllllllllllllllllllllllllllllinnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnggggggggggggggggggggggggg");
				this.failTask(task, 0L);
			}
		} else {
			this.delayFailure(task, sample);
		}
		
	}

	double randValue(double min, double max){
		final Random rand = new Random(SimProperties.RNG_SEED.asLong());
		return (min+(rand.nextGaussian()*((max-min) + 1)));
	}

	@Override
	public void generateResourceFailure(Resource resource) {
		// TODO Auto-generated method stub
		
	}
}
