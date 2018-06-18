package org.optframework.config;

public class GlobalConfig {
    public Integer budget;
    public Long bandwidth;
    public Integer m_number;
    public Integer t_extra;

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

    public Integer getBudget() {
        return budget;
    }

    public void setBudget(Integer budget) {
        this.budget = budget;
    }
}
