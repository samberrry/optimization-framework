package org.optframework.core.utils;

import org.optframework.core.InstanceInfo;
import org.optframework.core.Log;
import org.optframework.core.Solution;

public class Printer {
    public static void printSolution(Solution solution, InstanceInfo instanceInfo[], long time){
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
        String timePrefix;
        long sec = time/1000;
        long min = sec/60;
        long hr = min/60;

        long converted;

        if (sec < 0){
            timePrefix = "Milisec";
            converted = time;
        }else if (min < 1){
            timePrefix = "Seconds";
            converted = sec;
        }else if (hr < 1){
            timePrefix = "Minutes";
            converted = min;
        }else {
            timePrefix = "Hours";
            converted = hr;
        }

        Log.logger.info("Algorithm runtime: "+ converted + " "+ timePrefix + " ["+time+"]");
    }
}
