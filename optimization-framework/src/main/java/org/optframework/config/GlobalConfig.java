package org.optframework.config;

public class GlobalConfig {
    public String algorithm;
    public String workflow_name;
    public Integer workflow_id;
    public Double budget;
    public Long bandwidth;
    public Boolean read_m_number_from_config;
    public Integer t_extra;
    public Integer m_number;
    public Integer initial_solution_from_heft_id;
    public Boolean use_mysql_to_log;
    public String mysql_username;
    public String mysql_password;
    public String connection_string;
    public Boolean deadline_based;
    public Double deadline;

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

    public String getWorkflow_name() {
        return workflow_name;
    }

    public void setWorkflow_name(String workflow_name) {
        this.workflow_name = workflow_name;
    }

    public String getMysql_username() {
        return mysql_username;
    }

    public void setMysql_username(String mysql_username) {
        this.mysql_username = mysql_username;
    }

    public String getMysql_password() {
        return mysql_password;
    }

    public void setMysql_password(String mysql_password) {
        this.mysql_password = mysql_password;
    }

    public String getConnection_string() {
        return connection_string;
    }

    public void setConnection_string(String connection_string) {
        this.connection_string = connection_string;
    }

    public Boolean getUse_mysql_to_log() {
        return use_mysql_to_log;
    }

    public void setUse_mysql_to_log(Boolean use_mysql_to_log) {
        this.use_mysql_to_log = use_mysql_to_log;
    }

    public Boolean getRead_m_number_from_config() {
        return read_m_number_from_config;
    }

    public void setRead_m_number_from_config(Boolean read_m_number_from_config) {
        this.read_m_number_from_config = read_m_number_from_config;
    }

    public Boolean getDeadline_based_from_config() {
        return this.deadline_based;
    }

    public void setDeadline_based_from_config(Boolean deadline_based_from_config) {
        this.deadline_based = deadline_based_from_config;
    }

    public double getDeadline_from_config() {
        return this.deadline;
    }

    public void setRead_m_number_from_config(double deadline_from_config) {
        this.deadline = deadline_from_config;
    }
}
