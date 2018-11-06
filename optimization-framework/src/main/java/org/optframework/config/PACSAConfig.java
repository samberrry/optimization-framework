package org.optframework.config;

public class PACSAConfig {
    public int iteration_number;
    public boolean iteration_number_based;
    public int number_of_ants;
    public int number_of_runs;
    public double cf_increase_ratio;
    public double temp_decrease_ratio;
    public double equilibrium_point;
    public double evaporation_factor;
    public boolean global_based;
    public boolean insert_heft_initial_solution;

    public int getNumber_of_ants() {
        return number_of_ants;
    }

    public void setNumber_of_ants(int number_of_ants) {
        this.number_of_ants = number_of_ants;
    }

    public int getNumber_of_runs() {
        return number_of_runs;
    }

    public void setNumber_of_runs(int number_of_runs) {
        this.number_of_runs = number_of_runs;
    }

    public double getCf_increase_ratio() {
        return cf_increase_ratio;
    }

    public void setCf_increase_ratio(double cf_increase_ratio) {
        this.cf_increase_ratio = cf_increase_ratio;
    }

    public double getTemp_decrease_ratio() {
        return temp_decrease_ratio;
    }

    public void setTemp_decrease_ratio(double temp_decrease_ratio) {
        this.temp_decrease_ratio = temp_decrease_ratio;
    }

    public double getEquilibrium_point() {
        return equilibrium_point;
    }

    public void setEquilibrium_point(double equilibrium_point) {
        this.equilibrium_point = equilibrium_point;
    }

    public double getEvaporation_factor() {
        return evaporation_factor;
    }

    public void setEvaporation_factor(double evaporation_factor) {
        this.evaporation_factor = evaporation_factor;
    }

    public boolean isGlobal_based() {
        return global_based;
    }

    public void setGlobal_based(boolean global_based) {
        this.global_based = global_based;
    }

    public int getIteration_number() {
        return iteration_number;
    }

    public void setIteration_number(int iteration_number) {
        this.iteration_number = iteration_number;
    }

    public boolean isIteration_number_based() {
        return iteration_number_based;
    }

    public void setIteration_number_based(boolean iteration_number_based) {
        this.iteration_number_based = iteration_number_based;
    }

    public boolean isInsert_heft_initial_solution() {
        return insert_heft_initial_solution;
    }

    public void setInsert_heft_initial_solution(boolean insert_heft_initial_solution) {
        this.insert_heft_initial_solution = insert_heft_initial_solution;
    }
}
