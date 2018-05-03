package org.cloudbus.spotsim.enums;

public enum FaultToleranceMethod {

    NONE("NON"),
    REPLICATION("REP"),
    CHKPT("CKP"),
    CHKPT_PERFECT("CKPPERF")

    // MIGRATION("MIG")
    ;

    private final String shortName;

    private FaultToleranceMethod(final String shortName) {
	this.shortName = shortName;
    }

    public String shortName() {
	return this.shortName;
    }
}