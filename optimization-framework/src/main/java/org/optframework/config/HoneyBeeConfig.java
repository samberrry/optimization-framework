package org.optframework.config;

public class HoneyBeeConfig {
    Double max_speed;
    Double min_speed;
    Boolean force_speed;
    Double cooling_factor;
    Integer number_of_threads;
    Integer spermatheca_size;
    Integer generation_number;
    public static int kRandom;
    Double neighborhood_ratio;
    Integer number_of_runs;
    Double sMin_division;
    Double sMax_division;
    Boolean full_mutation;

    public Double getMax_speed() {
        return max_speed;
    }

    public void setMax_speed(Double max_speed) {
        this.max_speed = max_speed;
    }

    public Double getMin_speed() {
        return min_speed;
    }

    public void setMin_speed(Double min_speed) {
        this.min_speed = min_speed;
    }

    public Double getCooling_factor() {
        return cooling_factor;
    }

    public void setCooling_factor(Double cooling_factor) {
        this.cooling_factor = cooling_factor;
    }

    public Integer getNumber_of_threads() {
        return number_of_threads;
    }

    public void setNumber_of_threads(Integer number_of_threads) {
        this.number_of_threads = number_of_threads;
    }

    public Integer getSpermatheca_size() {
        return spermatheca_size;
    }

    public void setSpermatheca_size(Integer spermatheca_size) {
        this.spermatheca_size = spermatheca_size;
    }

    public Integer getGeneration_number() {
        return generation_number;
    }

    public void setGeneration_number(Integer generation_number) {
        this.generation_number = generation_number;
    }

    public Boolean getForce_speed() {
        return force_speed;
    }

    public void setForce_speed(Boolean force_speed) {
        this.force_speed = force_speed;
    }

    public Double getNeighborhood_ratio() {
        return neighborhood_ratio;
    }

    public void setNeighborhood_ratio(Double neighborhood_ratio) {
        this.neighborhood_ratio = neighborhood_ratio;
    }

    public Integer getNumber_of_runs() {
        return number_of_runs;
    }

    public void setNumber_of_runs(Integer number_of_runs) {
        this.number_of_runs = number_of_runs;
    }

    public Double getsMin_division() {
        return sMin_division;
    }

    public void setsMin_division(Double sMin_division) {
        this.sMin_division = sMin_division;
    }

    public Double getsMax_division() {
        return sMax_division;
    }

    public void setsMax_division(Double sMax_division) {
        this.sMax_division = sMax_division;
    }

    public Boolean getFull_mutation() {
        return full_mutation;
    }

    public void setFull_mutation(Boolean full_mutation) {
        this.full_mutation = full_mutation;
    }
}
