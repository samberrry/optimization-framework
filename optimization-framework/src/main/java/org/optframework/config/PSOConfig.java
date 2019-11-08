package org.optframework.config;

public class PSOConfig {
    public Integer number_of_runs;
    public Integer number_of_particles;
    public Integer maximum_iteration;
    public Double weight;
    public Double acceleration_coefficient1;
    public Double acceleration_coefficient2;

    public Integer getNumber_of_runs() {
        return number_of_runs;
    }

    public void setNumber_of_runs(Integer number_of_runs) {
        this.number_of_runs = number_of_runs;
    }

    public Integer getNumber_of_particles() {
        return number_of_particles;
    }

    public void setNumber_of_particles(Integer number_of_particles) {
        this.number_of_particles = number_of_particles;
    }

    public Integer getMaximum_iteration() {
        return maximum_iteration;
    }

    public void setMaximum_iteration(Integer maximum_iteration) {
        this.maximum_iteration = maximum_iteration;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Double getAcceleration_coefficient1() {
        return acceleration_coefficient1;
    }

    public void setAcceleration_coefficient1(Double acceleration_coefficient1) {
        this.acceleration_coefficient1 = acceleration_coefficient1;
    }

    public Double getAcceleration_coefficient2() {
        return acceleration_coefficient2;
    }

    public void setAcceleration_coefficient2(Double acceleration_coefficient2) {
        this.acceleration_coefficient2 = acceleration_coefficient2;
    }
}
