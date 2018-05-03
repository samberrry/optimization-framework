package org.cloudbus.spotsim.enums;

public enum RuntimeEstimationMethod {
    USER_SUPPLIED("US"),
    USER_SUPPLIED_FRACTION("USF"),
    RECENT_AVERAGE("RA"),
    OPTIMAL("OP"),
    RANDOM("RND"),
    ALMOST_OPTIMAL("AOP");

    private String shortName;

    private RuntimeEstimationMethod(final String shortName) {
	this.shortName = shortName;
    }

    public String shortName() {
	return this.shortName;
    }
}