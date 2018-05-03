package org.cloudbus.cloudsim.util.workload;

import java.util.Random;

import org.cloudbus.spotsim.main.config.SimProperties;

public class UniformPerformanceVariator implements PerformanceVariator {

	@Override
	public double generationRuntimeVariation(double taskTime) {
		
		Random rnd = new Random(SimProperties.PERFORMANCE_VARIATION_SEED.asLong());
		
		double std = taskTime * SimProperties.PERFORMANCE_VARIATION_STD.asDouble() ;
		
		double newTaskTime = (long) (std * rnd.nextGaussian() + taskTime);
		
		if(newTaskTime <=0 ){
			newTaskTime = taskTime;
		}
		
		return newTaskTime;
	}

}
