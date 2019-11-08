package org.optframework.database;

import org.optframework.config.Config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class MySQLSolutionRepository {
    private final String DB_DRIVER = "com.mysql.jdbc.Driver";
    public void checkDbConnection(){
        System.out.println("-------- MySQL JDBC Connection Testing ------------");

        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Where is your MySQL JDBC Driver?");
            e.printStackTrace();
            return;
        }

        System.out.println("MySQL JDBC Driver Registered!");
        Connection connection = null;

        try {
            connection = DriverManager
                    .getConnection("jdbc:mysql://localhost:3306/optdb","hessamdb", "He$123456789");

        } catch (SQLException e) {
            System.out.println("Connection Failed! Check output console");
            e.printStackTrace();
            return;
        }

        if (connection != null) {
            System.out.println("You made it, take control your database now!");
        } else {
            System.out.println("Failed to make connection!");
        }
    }

    public void updateRecord(Double minFitness, Double maxFitness, Double averageFitness, Double cost, double avgRuntime){
        Connection dbConnection;
        Statement statement;

        String updateTableSQL = "UPDATE "+ Config.global.workflow_name
                + " SET "+ Config.global.algorithm +"_max_fitness = "+ maxFitness +
                ", "+ Config.global.algorithm +"_min_fitness = "+ minFitness +
                ", "+ Config.global.algorithm +"_avg_fitness = "+ averageFitness +
                ", "+ Config.global.algorithm +"_cost = "+ cost +
                ", "+ Config.global.algorithm +"_avg_runtime = "+ avgRuntime +
                ", "+ Config.global.algorithm +"_budget = "+ Config.global.budget
                + " WHERE cost = "+ Config.global.budget;
        try {
            dbConnection = getDBConnection();
            statement = dbConnection.createStatement();
            System.out.println(updateTableSQL);
            statement.execute(updateTableSQL);
            System.out.println("Record is updated!");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private  Connection getDBConnection() {
        Connection dbConnection = null;
        try {
            Class.forName(DB_DRIVER);
        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }
        try {
            dbConnection = DriverManager.getConnection(
                    Config.global.connection_string,
                    Config.global.mysql_username,
                    Config.global.mysql_password);
            return dbConnection;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return dbConnection;
    }
}
