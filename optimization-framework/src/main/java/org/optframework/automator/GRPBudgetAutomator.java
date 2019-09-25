package org.optframework.automator;

import org.optframework.GlobalAccess;
import org.optframework.RunGRPHEFTAlgorithm;
import org.optframework.config.Config;
import org.optframework.core.Log;
import org.optframework.core.Solution;
import org.optframework.core.utils.Printer;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import static org.optframework.automator.BudgetList.*;
import static org.optframework.automator.BudgetList.cybershake100;

/**
 * Budget Automator (Automates budget setup)
 * This Utility class facilitates getting the result process
 * Automates budget based on Evolutionary Multi-Objective Workflow Scheduling in Cloud paper
 *
 * This will ignore the algorithm specified in config file
 * Specially designed for GRP Algorithm
 * */

public class GRPBudgetAutomator {

    public static void main(String[] args)throws Exception{
        double budgetList[] = null;
        GlobalAccess.solutionArrayListToCSV = new ArrayList<>();
        GlobalAccess.timeInMilliSecArrayList = new ArrayList<>();
        GlobalAccess.solutionRepository = new ArrayList<>();

        //array of solutions which should be printed to csv file
        ArrayList<Solution> solutionArrayListToCSV;
        ArrayList<Long> timeInMilliSecArrayList;

        Log.init();
        Log.logger.info("+++++++++ GRPBudgetAutomator is started +++++++++");

        /**
         * Initializes Cloudsim Logger
         * */
        org.cloudbus.cloudsim.Log.init("cloudsim.log");

        org.cloudbus.spotsim.main.config.Config.load(null);

        /**
         * Loads configs from YAML file
         * */
        Config.initConfig();
        Printer.printSplitter();

        switch (Config.global.workflow_id){
            case 1: budgetList = inspiral1000; break;
            case 2: budgetList = inspiral100; break;
            case 10: budgetList = montage1000; break;
            case 11: budgetList = montage100; break;
            case 20: budgetList = sipht1000; break;
            case 21: budgetList = sipht100; break;
            case 30: budgetList = epigenomics997; break;
            case 31: budgetList = epigenomics100; break;
            case 40: budgetList = cybershake1000; break;
            case 41: budgetList = cybershake100; break;
        }

        for (double budget: budgetList){
            Config.global.budget = budget;
            //at the end of runGRPHEFT the appropriate methods will be updated
            RunGRPHEFTAlgorithm.runGRPHEFT();
        }

        try (PrintWriter writer = new PrintWriter(new File("cost-automator.csv"))) {
            solutionArrayListToCSV = GlobalAccess.solutionArrayListToCSV;
            timeInMilliSecArrayList = GlobalAccess.timeInMilliSecArrayList;

            StringBuilder sb = new StringBuilder();
            sb.append("GRP Budget Automator\n");
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

            for (int i = 0; i < solutionArrayListToCSV.size(); i++) {
                sb.append(budgetList[i]);
                sb.append(',');
                sb.append(solutionArrayListToCSV.get(i).cost);
                sb.append(',');
                sb.append(solutionArrayListToCSV.get(i).makespan);
                sb.append(',');
                sb.append(solutionArrayListToCSV.get(i).fitnessValue);
                sb.append(',');
                sb.append(timeInMilliSecArrayList.get(i));
                sb.append(',');
                sb.append(timeInMilliSecArrayList.get(i)/1000);
                sb.append('\n');
            }

            writer.write(sb.toString());

            System.out.println("done!");

        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }
}
