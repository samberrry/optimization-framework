package org.optframework.config;

/**
 * Static properties of the problem those that will not change during the simulation process
 * and must be set
 * */

public interface StaticProperties {
    /** The maximum number of instances is limited to M = mNumber, normally M = 50 */
    int M_NUMBER = 100;

    /** Warning signal few minutes before an interruption informing (refereed to as interruption notice) us the instance will be forced to exit soon, refereed to as interruption notice. The tExtra is the time  before the interruption occurs */
    int T_EXTRA = 2;

    /** The number of different types of spot-instances, is equal to the number of different types of on-demand instances */
    int N_TYPES = 9;

    double START_TEMP = 850;

    double FINAL_TEMP = 0.001;

    double COOLING_FACTOR = 0.995;

    double INCREASE_RATE_OF_CF = 1.004;

    double EQUILIBRIUM_POINT = 0.99;

    double SA_EQUILIBRIUM_COUNT = 10;

    double INTENSIFICATION_RATE = 0.95;

    double EVAPORATION_FACTOR = 0.9;

    int NUMBER_OF_ANTS = 16;

    double PENALTY_COEFFICIENT = 10;

    int NUMBER_OF_HBMO_THREADS = 8;

    int SPERMATHECA_SIZE = 100;

    int HBMO_GENERATION_NUMBER = 30;
}
