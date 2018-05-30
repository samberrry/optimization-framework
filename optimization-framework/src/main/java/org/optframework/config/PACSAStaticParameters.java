package org.optframework.config;

/**
 * Static parameters related to Simulated Annealing with Max-Min Ant Colony System
 * */

public interface PACSAStaticParameters extends StaticParameters{
    double START_TEMP = 850;

    double FINAL_TEMP = 0.1;

    double COOLING_FACTOR = 0.8; //CF

    double INCREASE_RATE_OF_CF = 1.004;

    double EQUILIBRIUM_POINT = 0.99;

    double INTENSIFICATION_RATE = 0.95;

    double EVAPORATION_FACTOR = 0.9;

    int NUMBER_OF_ANTS = 16;

    double PENALTY_COEFFICIENT = 10;
}
