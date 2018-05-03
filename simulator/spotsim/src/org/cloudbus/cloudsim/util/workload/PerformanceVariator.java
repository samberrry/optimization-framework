package org.cloudbus.cloudsim.util.workload;

public interface PerformanceVariator {

	public abstract double generationRuntimeVariation(double taskTime);
}
