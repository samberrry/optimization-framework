package org.optframework.config;

/**
 * Static parameters of the problem are those that will not change during the simulation process
 * and must be set
 * */

public interface StaticParameters {
    /** The maximum number of instances is limited to M = mNumber, normally M = 50 */
    int M_NUMBER = 100;

    /** Warning signal few minutes before an interruption informing (refereed to as interruption notice) us the instance will be forced to exit soon, refereed to as interruption notice. The tExtra is the time  before the interruption occurs */
    int T_EXTRA = 2;

    /** The number of different types of spot-instances, is equal to the number of different types of on-demand instances */
    int N_TYPES = 9;
}
