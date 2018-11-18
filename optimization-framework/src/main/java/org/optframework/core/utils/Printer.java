package org.optframework.core.utils;

import org.optframework.config.Config;
import org.optframework.core.InstanceInfo;
import org.optframework.core.Log;
import org.optframework.core.Solution;
import java.util.concurrent.TimeUnit;

public class Printer {
    public static String millisToShortDHMS(long duration) {
        String res = "";
        long days  = TimeUnit.MILLISECONDS.toDays(duration);
        long hours = TimeUnit.MILLISECONDS.toHours(duration)
                - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(duration));
        long minutes = TimeUnit.MILLISECONDS.toMinutes(duration)
                - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(duration));
        long seconds = TimeUnit.MILLISECONDS.toSeconds(duration)
                - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration));
        if (days == 0) {
            res = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }
        else {
            res = String.format("%dd%02d:%02d:%02d", days, hours, minutes, seconds);
        }
        return res;
    }

    public static void printSolution(Solution solution, InstanceInfo instanceInfo[], long time){
        Log.logger.info("Number of used Instances: " + solution.numberOfUsedInstances);

        for (int i = 0; i < solution.instanceTimes.length; i++) {
            Log.logger.info("Requested time for instance " + instanceInfo[solution.yArray[i]].getType().getName() + " : " + solution.instanceTimes[i]);
        }

        for (int i = 0; i < solution.instanceTimelines.length; i++) {
            Log.logger.info("Timeline for instance " + instanceInfo[solution.yArray[i]].getType().getName() + " : " + solution.instanceTimelines[i]);
        }

        String xArray = "";
        for (int val : solution.xArray){
            xArray += " " + String.valueOf(val);
        }
        Log.logger.info("Value of the X Array: "+ xArray);

        String yArray = "";
        for (int i = 0; i < solution.numberOfUsedInstances; i++) {
            yArray += " " + String.valueOf(solution.yArray[i]);
        }
        Log.logger.info("Value of the Y Array: "+ yArray);

        Log.logger.info("Total Cost: " + solution.cost);
        Log.logger.info("Makespan: " + solution.makespan);
        Log.logger.info("Fitness Value: "+ solution.fitnessValue);

        Log.logger.info("Algorithm runtime: "+ millisToShortDHMS(time));
    }

    public static void printStartTime(Solution solution, InstanceInfo instanceInfo[]){
        for (int i = 0; i < solution.instanceTimes.length; i++) {
            Log.logger.info("Timeline for instance " + instanceInfo[solution.yArray[i]].getType().getName() + " : " + solution.instanceTimelines[i]);
        }
    }

    public static void lightPrintSolution(Solution solution, long time){
        Log.logger.info("Number of used Instances: " + solution.numberOfUsedInstances);

        Log.logger.info(  "Fitness Value: "+ solution.fitnessValue + " Makespan: " + solution.makespan+" Total Cost: " + solution.cost);

        Log.logger.info("Algorithm runtime: "+ millisToShortDHMS(time));
    }

    public static void lightPrintSolutionForHBMOItr(Solution solution, InstanceInfo instanceInfo[]){
        Log.logger.info("Number of used Instances: " + solution.numberOfUsedInstances);

        Log.logger.info(  "Fitness Value: "+ solution.fitnessValue + " Makespan: " + solution.makespan+" Total Cost: " + solution.cost);
    }

    public static void printSolutionWithoutTime(Solution solution, InstanceInfo instanceInfo[]){
        Log.logger.info("Number of used Instances: " + solution.numberOfUsedInstances);

        for (int i = 0; i < solution.instanceTimes.length; i++) {
            Log.logger.info("Requested time for instance " + instanceInfo[solution.yArray[i]].getType().getName() + " : " + solution.instanceTimes[i]);
        }

        for (int i = 0; i < solution.instanceTimes.length; i++) {
            Log.logger.info("Timeline for instance " + instanceInfo[solution.yArray[i]].getType().getName() + " : " + solution.instanceTimelines[i]);
        }

        String xArray = "";
        for (int val : solution.xArray){
            xArray += " " + String.valueOf(val);
        }
        Log.logger.info("Value of the X Array: "+ xArray);

        String yArray = "";
        for (int i = 0; i < solution.numberOfUsedInstances; i++) {
            yArray += " " + String.valueOf(solution.yArray[i]);
        }
        Log.logger.info("Value of the Y Array: "+ yArray);

        Log.logger.info("Total Cost: " + solution.cost);
        Log.logger.info("Makespan: " + solution.makespan);
        Log.logger.info("Fitness Value: "+ solution.fitnessValue);
    }

    public static void printSplitter(){
        Log.logger.info("\n===================================================\n" +
                "---------------------< MOWSC >---------------------\n" +
                "===================================================\n");
    }

    public static void printHoneBeeInfo(){
        Log.logger.info("Config File ---------- "+" itr: "+ Config.honeybee_algorithm.getGeneration_number()+ " sp size: "+ Config.honeybee_algorithm.getSpermatheca_size()+ " nbh ratio: "+ Config.honeybee_algorithm.getNeighborhood_ratio()+ " force speed: "+ Config.honeybee_algorithm.getForce_speed()+ " runs#: " + Config.honeybee_algorithm.getNumber_of_runs());
    }

    public static void printTime(long time){
        Log.logger.info("Algorithm runtime: "+ millisToShortDHMS(time));
    }

    public static void printSAInfo(){
        Log.logger.info("Simulated Annealing parameters Initial temp: "+ Config.sa_algorithm.start_temperature + " Final temp: " + Config.sa_algorithm.final_temperature + " Cooling Factor: " + Config.sa_algorithm.cooling_factor + " Equilibrium point: " + Config.sa_algorithm.equilibrium_point);
    }

    public static void printSolutionWithouthTime(Solution solution, InstanceInfo instanceInfo[]){
        String toPrint = "\n";
        toPrint += "Number of used Instances: " + solution.numberOfUsedInstances + "\n\n";
        toPrint += "======================================================================\n";

        for (int i = 0; i < solution.instanceTimes.length; i++) {
            toPrint += "Requested time for instance " + instanceInfo[solution.yArray[i]].getType().getName() + " : " + solution.instanceTimes[i] + "\n";
        }
        toPrint += "======================================================================\n";

        for (int i = 0; i < solution.instanceTimelines.length; i++) {
            toPrint += "Timeline for instance " + instanceInfo[solution.yArray[i]].getType().getName() + " : " + solution.instanceTimelines[i] + "\n";
        }

        Log.logger.info(toPrint);

        String xArray = "";
        for (int val : solution.xArray){
            xArray += " " + String.valueOf(val);
        }
        Log.logger.info("Value of the X Array: "+ xArray);

        String yArray = "";
        for (int i = 0; i < solution.numberOfUsedInstances; i++) {
            yArray += " " + String.valueOf(solution.yArray[i]);
        }
        Log.logger.info("Value of the Y Array: "+ yArray);

        String zArray = "";
        for (int val : solution.zArray){
            zArray += " " + String.valueOf(val);
        }
        Log.logger.info("Value of the Z Array: "+ zArray);

        toPrint = "\nTotal Cost: " + solution.cost + "\n";
        toPrint += "Makespan: " + solution.makespan + "\n";
        toPrint += "Fitness Value: "+ solution.fitnessValue + "\n";

        Log.logger.info(toPrint);
    }

    public static void printUtilization(double utilization[]){
        double utilizationSum = 0;
        for (int i = 0; i < utilization.length ; i++) {
            utilizationSum += utilization[i];
        }

        double utilizationAverage = utilizationSum / utilization.length;
        String str = "\n";

        for (int i = 0; i < utilization.length; i++) {
            str += "Instance" + i + ": "+ utilization[i] + "\n";
        }

        str += "\nAVERAGE RESOURCE UTILIZATION: " + utilizationAverage;

        Log.logger.info(str);
    }
}
