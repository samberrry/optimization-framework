package org.optframework.config;

public class GlobalConfig {
    public Integer budget;
    public Long bandwidth;
    public Long m_number;
    public Integer t_extera;

    public Long getBandwidth() {
        return bandwidth;
    }

    public void setBandwidth(Long bandwidth) {
        this.bandwidth = bandwidth;
    }

    public Long getM_number() {
        return m_number;
    }

    public void setM_number(Long m_number) {
        this.m_number = m_number;
    }

    public Integer getT_extera() {
        return t_extera;
    }

    public void setT_extera(Integer t_extera) {
        this.t_extera = t_extera;
    }

    public Integer getBudget() {
        return budget;
    }

    public void setBudget(Integer budget) {
        this.budget = budget;
    }
}
