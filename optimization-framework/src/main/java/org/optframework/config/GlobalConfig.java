package org.optframework.config;

public class GlobalConfig {
    public String algorithm;
    public Integer workflow_id;
    public Double budget;
    public Long bandwidth;
    public Integer m_number;
    public Integer t_extra;
    public Integer initial_solution_from_heft_id;

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public Long getBandwidth() {
        return bandwidth;
    }

    public void setBandwidth(Long bandwidth) {
        this.bandwidth = bandwidth;
    }

    public Integer getM_number() {
        return m_number;
    }

    public void setM_number(Integer m_number) {
        this.m_number = m_number;
    }

    public Integer getT_extra() {
        return t_extra;
    }

    public void setT_extra(Integer t_extra) {
        this.t_extra = t_extra;
    }

    public Integer getWorkflow_id() {
        return workflow_id;
    }

    public void setWorkflow_id(Integer workflow_id) {
        this.workflow_id = workflow_id;
    }

    public Double getBudget() {
        return budget;
    }

    public void setBudget(Double budget) {
        this.budget = budget;
    }

    public Integer getInitial_solution_from_heft_id() {
        return initial_solution_from_heft_id;
    }

    public void setInitial_solution_from_heft_id(Integer initial_solution_from_heft_id) {
        this.initial_solution_from_heft_id = initial_solution_from_heft_id;
    }
}
