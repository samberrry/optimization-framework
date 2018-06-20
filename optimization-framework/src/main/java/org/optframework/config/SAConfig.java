package org.optframework.config;

public class SAConfig {
    public Double start_temperature;
    public Double final_temperature;
    public Double cooling_factor;
    public Integer equilibrium_point;
    public Boolean force_cooling;
    public Integer number_of_runs;

    public Double getStart_temperature() {
        return start_temperature;
    }

    public void setStart_temperature(Double start_temperature) {
        this.start_temperature = start_temperature;
    }

    public Double getFinal_temperature() {
        return final_temperature;
    }

    public void setFinal_temperature(Double final_temperature) {
        this.final_temperature = final_temperature;
    }

    public Double getCooling_factor() {
        return cooling_factor;
    }

    public void setCooling_factor(Double cooling_factor) {
        this.cooling_factor = cooling_factor;
    }

    public Integer getEquilibrium_point() {
        return equilibrium_point;
    }

    public void setEquilibrium_point(Integer equilibrium_point) {
        this.equilibrium_point = equilibrium_point;
    }

    public Boolean getForce_cooling() {
        return force_cooling;
    }

    public void setForce_cooling(Boolean force_cooling) {
        this.force_cooling = force_cooling;
    }

    public Integer getNumber_of_runs() {
        return number_of_runs;
    }

    public void setNumber_of_runs(Integer number_of_runs) {
        this.number_of_runs = number_of_runs;
    }
}
