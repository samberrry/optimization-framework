package org.cloudbus.spotsim.pricing;

/*
 * Defines how price variation traces will serve as input to the simulator
 */
public enum PriceTraceGen {
    // Will use actual traces collected from Amazon EC2
    HISTORY,

    // Will use a random model
    RANDOM;
}