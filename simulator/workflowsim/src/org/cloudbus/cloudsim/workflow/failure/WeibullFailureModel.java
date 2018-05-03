package org.cloudbus.cloudsim.workflow.failure;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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


public class WeibullFailureModel extends TaskFailureGenerator {

	RandomGenerator r = new JDKRandomGenerator();
	Random rand = new Random(SimProperties.RNG_SEED.asLong());
	WeibullDistribution wd;
	int counter =0;
	/*FileWriter fw;
	PrintWriter out;*/
	public WeibullFailureModel(WorkflowFailureBroker broker) throws IOException {
		super(broker);
		r.setSeed(SimProperties.RNG_SEED.asLong());
		wd = new WeibullDistribution(r,SimProperties.WEIBULL_ALPHA.asDouble(), SimProperties.WEIBULL_BETA.asDouble());
		/*fw = new FileWriter("C:\\Documents and Settings\\deepakc\\My Documents\\Dropbox\\TaskDuplication work\\Results\\Exp0809\\weibullFailureData.csv");
		out = new PrintWriter(fw);*/
	}

	@Override
	public void generateFailure(Task task) {
		RandomGenerator r = new JDKRandomGenerator();
		r.setSeed(SimProperties.RNG_SEED.asLong());
		Random rand = new Random(SimProperties.RNG_SEED.asLong());

		if(task.getJob().getStatus() == JobStatus.COMPLETED){
			return;
		}
		
		Resource resource = task.getResource();
		int TIME_PERIOD = 3600;
		final double t = ((double)(CloudSim.clock() - resource.getTimeReceived()))/TIME_PERIOD;
		double reliability = 1 - wd.cumulativeProbability(t);
		double nextGaussian = rand.nextDouble();
		if (nextGaussian > reliability){
			long sample = (long) wd.sample() * TIME_PERIOD;
			this.failTask(task, sample);
		}
	
		/*//WeibullDistribution wd = new WeibullDistribution(r,2, 5);
		long sample = (long) wd.sample();
		if (sample == 0) {
			Resource resource = task.getResource();
			final double t = ((double)(CloudSim.clock() - resource.getTimeReceived()))/(1);
			//final double timeHrs = ((double) task.getActualStartTime()) / 3600;
			double cumulativeProbability = wd.cumulativeProbability(t);
			
			if (rand.nextGaussian() < cumulativeProbability) {
				this.failTask(task, 0L);
			}
		} else {
			this.delayFailure(task, sample);
		}*/
		
	}
  
	double randValue(double min, double max){
		final Random rand = new Random(SimProperties.RNG_SEED.asLong());
		return (min+(rand.nextGaussian()*((max-min) + 1)));
	}

	public void closeFiles(){
	/*	try {
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		out.close();*/
	}
	
	@Override
	public void generateResourceFailure(Resource resource) {
		//RandomGenerator r = new JDKRandomGenerator();
		//r.setSeed(SimProperties.RNG_SEED.asLong());
		//Random rand = new Random(SimProperties.RNG_SEED.asLong());
/*
		if(!resource.getState().isUsable()){
			return;
		}
		*/
		int TIME_PERIOD = 30;
		final double t = ((double)(CloudSim.clock() - resource.getTimeReceived()))/TIME_PERIOD;
		double reliability = 1 - wd.cumulativeProbability(t);
		double nextGaussian = rand.nextDouble();
		//System.out.println(" T " + t + " reliability " + reliability + " Gausian " + nextGaussian);
		long sample = (long) wd.sample() * TIME_PERIOD;
		if (nextGaussian > reliability){
			
			//System.out.println(" T " + t + " sample  faillll " + sample);
			this.failResource(resource, sample);
			/*out.print(resource.getId());
			out.print(",");
			out.println(sample);*/
		}else{
			this.delayResourceFailure(resource, SimProperties.WEIBULL_DELAY_FAILURE.asLong());
		}
		
	}
}
