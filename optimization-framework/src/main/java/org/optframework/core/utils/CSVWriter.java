package org.optframework.core.utils;

import org.optframework.GlobalAccess;
import org.optframework.automator.RunResult;
import org.optframework.automator.SimpleResult;
import org.optframework.config.Config;
import org.optframework.core.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * Creates desired CSV files
 * */

public class CSVWriter {
    //gets array of RunResults which are different runs of an algorithm
    public static void processResults(ArrayList<RunResult> runResults, int budgeListSize){
        Log.logger.info("Starts Processing Results to generate CSV files");

        //best
        ArrayList<SimpleResult> bestResults = new ArrayList<>();
        //worst
        ArrayList<SimpleResult> worstResults = new ArrayList<>();
        //avg
        ArrayList<SimpleResult> avgResults = new ArrayList<>();

        for (int i = 0; i < budgeListSize; i++) {
            int makespanAvg;
            double costAvg, fitnessAvg,
            cSum = 0 , mSum = 0 , fSum = 0, fMax = 0, fMin = 999999999999999.9;
            long sec = -1;
            SimpleResult maxResult = null, minResult = null;
            for (int j = 0; j < runResults.size(); j++) {
               cSum += runResults.get(j).solutionArrayListToCSV.get(i).cost;
               mSum += runResults.get(j).solutionArrayListToCSV.get(i).makespan;
               fSum += runResults.get(j).solutionArrayListToCSV.get(i).fitnessValue;
               sec = runResults.get(j).timeInMilliSecArrayList.get(i);

               if (fMax < runResults.get(j).solutionArrayListToCSV.get(i).fitnessValue){
                   maxResult = new SimpleResult(Config.global.budget,runResults.get(j).solutionArrayListToCSV.get(i).cost, runResults.get(j).solutionArrayListToCSV.get(i).makespan, runResults.get(j).solutionArrayListToCSV.get(i).fitnessValue, sec);
               }
               if (fMin > runResults.get(j).solutionArrayListToCSV.get(i).fitnessValue){
                   minResult = new SimpleResult(Config.global.budget,runResults.get(j).solutionArrayListToCSV.get(i).cost, runResults.get(j).solutionArrayListToCSV.get(i).makespan, runResults.get(j).solutionArrayListToCSV.get(i).makespan, sec);
               }
            }
            bestResults.add(maxResult);
            worstResults.add(minResult);

            costAvg = cSum / runResults.size();
            makespanAvg = (int)(mSum / runResults.size());
            fitnessAvg = fSum / runResults.size();

            avgResults.add(new SimpleResult(Config.global.budget, costAvg, makespanAvg, fitnessAvg, sec));
        }
        CSVWriter.write(bestResults, "automator-" + Config.global.algorithm + "-" + GlobalAccess.workflowName + "-best.csv", "Worst");
        CSVWriter.write(worstResults, "automator-" + Config.global.algorithm + "-" + GlobalAccess.workflowName + "-worst.csv", "Best");
        CSVWriter.write(avgResults, "automator-" + Config.global.algorithm + "-" + GlobalAccess.workflowName + "-avg.csv", "Average");
    }

    public static void write(ArrayList<SimpleResult> theArray, String fileName, String type){
        Log.logger.info("Starts to Write into CSV files");

        try {
            PrintWriter writer = new PrintWriter(new File(fileName));
            if(theArray.size() == 0){
                throw new RuntimeException("This Algorithm does not support Automator");
            }

            StringBuilder sb = new StringBuilder();
            sb.append("Budget Automator     " + type  + " " + Config.global.algorithm + "-"+ GlobalAccess.workflowName + "\n");
            sb.append("Budget");
            sb.append(',');
            sb.append("Cost");
            sb.append(',');
            sb.append("Makespan");
            sb.append(',');
            sb.append("Fitness");
            sb.append(',');
            sb.append("MilliSec");
            sb.append(',');
            sb.append("Sec");
            sb.append('\n');

            for (int i = 0; i < theArray.size(); i++) {
                SimpleResult result = theArray.get(i);
                sb.append(Config.global.budget);
                sb.append(',');
                sb.append(result.cost);
                sb.append(',');
                sb.append(result.makespan);
                sb.append(',');
                sb.append(result.fitness);
                sb.append(',');
                sb.append(result.milli);
                sb.append(',');
                sb.append(result.milli/1000);
                sb.append('\n');
            }

            writer.write(sb.toString());
            writer.close();
            System.out.println("done!");

        }catch (RuntimeException | FileNotFoundException e){
            System.out.println(e.getMessage());
        }
    }
}
