package org.cloudbus.cloudsim.util.workload;

public enum PerformanceVariatorEnum {

	UNIFORM_PERF_VAR("uniformPerfVariation");
	
	private String shortName;

    private PerformanceVariatorEnum(final String shortName) {
	this.shortName = shortName;
    }

    public String shortName() {
	return this.shortName;
    }
}
