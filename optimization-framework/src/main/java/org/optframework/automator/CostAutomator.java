package org.optframework.automator;

import org.optframework.*;
import org.optframework.config.Config;
import org.optframework.core.Log;
import org.optframework.core.Solution;
import org.optframework.core.utils.Printer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * Cost Automator (Automates cost setup)
 * This Utility class facilitates getting the result process
 *
 * */

public class CostAutomator {

    public static Solution solution;
    public static long timeInMilliSec;
    public static ArrayList<Solution> solutionArrayList;
    public static ArrayList<Long> timeInMilliSecArrayList;

    public static double[] budgetList = {
            1.463,
            2.025
    };

    public static void main(String[] args) throws Exception{
        solutionArrayList = new ArrayList<>();
        timeInMilliSecArrayList = new ArrayList<>();

        Log.init();
        Log.logger.info("+++++++++ CostAutomator is started +++++++++");

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

        for (double budget: budgetList){
            Config.global.budget = budget;
            switch (Config.global.algorithm){
                case "sa":
                    RunSAAlgorithm.runSA();
                    break;
                case "hbmo":
                    RunHBMOAlgorithm.runHBMO();
                    break;
                case "heft":
                    RunHEFTAlgorithm.runSingleHEFT();
                    break;
                case "hbmo-heft":
                    RunHEFTWithHBMO.runHEFTWithHBMO();
                    break;
                case "heft-example":
                    RunHEFTExample.runHEFTExample();
                    break;
                case "pacsa":
                    RunPACSAAlgorithm.runPACSA(0);
                    break;
                case "pacsa-plus":
                    RunPACSAAlgorithm.runPACSA(1);
                    break;
                case "pso":
                    RunPSOAlgorithm.runPSO(0);
                    break;
                case "zpso":
                    RunPSOAlgorithm.runPSO(1);
                    break;
                case "grp-heft":
                    RunGRPHEFTAlgorithm.runGRPHEFT();
                    break;
            }
            timeInMilliSecArrayList.add(timeInMilliSec);
            solutionArrayList.add(solution);
        }

        try (PrintWriter writer = new PrintWriter(new File("cost-automator.csv"))) {

            StringBuilder sb = new StringBuilder();
            sb.append("Budget");
            sb.append(',');
            sb.append("Cost");
            sb.append(',');
            sb.append("Makespan");
            sb.append(',');
            sb.append("Fitness");
            sb.append(',');
            sb.append("MilliSec");
            sb.append('\n');

            for (int i = 0; i < solutionArrayList.size(); i++) {
                sb.append(budgetList[i]);
                sb.append(',');
                sb.append(solutionArrayList.get(i).cost);
                sb.append(',');
                sb.append(solutionArrayList.get(i).makespan);
                sb.append(',');
                sb.append(solutionArrayList.get(i).fitnessValue);
                sb.append(',');
                sb.append(timeInMilliSecArrayList.get(i));
                sb.append('\n');
            }

            writer.write(sb.toString());

            System.out.println("done!");

        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }
}
