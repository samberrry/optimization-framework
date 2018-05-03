package org.cloudbus.cloudsim.workflow.failure;

import java.util.Random;

import org.apache.commons.math3.distribution.WeibullDistribution;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.util.workload.Job.JobStatus;
import org.cloudbus.cloudsim.util.workload.Task;
import org.cloudbus.cloudsim.workflow.broker.WorkflowFailureBroker;
import org.cloudbus.spotsim.broker.resources.Resource;
import org.cloudbus.spotsim.main.config.SimProperties;


public class NewWeibullFailureModel extends TaskFailureGenerator {

	RandomGenerator r = new JDKRandomGenerator();
	Random rand = new Random(SimProperties.RNG_SEED.asLong());
	WeibullDistribution wd;
	int counter =0;
	public NewWeibullFailureModel(WorkflowFailureBroker broker) {
		super(broker);
		r.setSeed(SimProperties.RNG_SEED.asLong());
		wd = new WeibullDistribution(r,SimProperties.WEIBULL_ALPHA.asDouble(), SimProperties.WEIBULL_BETA.asDouble());
	}

	@Override
	public void generateFailure(Task task) {
		RandomGenerator r = new JDKRandomGenerator();
		r.setSeed(SimProperties.RNG_SEED.asLong());

		if(task.getJob().getStatus() == JobStatus.COMPLETED){
			return;
		}

		long sample = (long) wd.sample();
		final long sampleSecs = sample * 3600;
		/*Resource resource = task.getResource();
		final double t = ((double)(CloudSim.clock() - resource.getTimeReceived()))/(1);
		//final double timeHrs = ((double) task.getActualStartTime()) / 3600;
		double cumulativeProbability = wd.cumulativeProbability(t);
		double nextGaussian = Math.abs(rand.nextGaussian());
		System.out.println("CDF " + cumulativeProbability + "gausian " + nextGaussian);
		if (nextGaussian < cumulativeProbability) {*/
			System.out.println("Faillll");
			this.failTask(task, sampleSecs);
	/*	}else {
			System.out.println("Delayyyyy");
			this.delayFailure(task, sampleSecs);
		}*/
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
