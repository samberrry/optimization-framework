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
      0.225,
      0.338,
      0.45,
      0.705,
            0.9,
            1.35,
            1.8,
            1.913,
            2.7,
            3.15,
            4.08,
            4.5,
            4.95,
            5.85,
            6.75,
            7.65,
            8.1,
            8.55
    };

    public static double[] montage1000 = {
      0.788,
      0.9,
      1.8,
      2.7,
      3.6,
      4.5,
      5.4,
      6.3,
      7.2,
      9,
      10.8,
      11.7,
      12.6,
      13.5,
            14.4,
            15.78,
            16.68,
            19.073,
            21,
            22.83
    };

    public static double[] sipht1000 = {
      14.4,
      14.85,
      14.97,
      15.75,
      16.2,
      16.65,
      17.55,
      18,
      18.48,
      18.9,
      19.8,
        20.7,
        21.6,
        22.5,
        23.4,
        23.625,
        24.3,
        23.975
    };

    public static double[] shipht100 = {
            1.35,
            1.41,
            1.575,
            1.695,
            1.8,
            1.86,
            1.913,
            2.025,
            2.25,
            2.505,
            2.7,
            3.15,
            3.6,
            4.5,
            5.4
    };

    public static double[] cybershake100 = {
            0.173,
            0.225,
            0.285,
            0.36,
            0.398,
            0.45,
            0.675,
            0.9,
            1.35,
            1.8,
            2.25,
            2.7,
            3.15,
            3.833,
            4.5,
            5.205,
            6.105,
            6.975,
            7.455,
            7.875
    };

    public static double[] cybershake1000 = {
            1.575,
            1.8,
            2.7,
            3.6,
            4.5,
            6.075,
            6.975,
            7.65,
            9,
            10.35,
            11.7,
            13.5,
            14.4,
            15.3,
            16.71,
            18,
            18.93,
            19.83,
            21.405,
            23.01
    };

    public static double[] inspiral1000 = {
            17.78,
            18,297,
            18.709,
            18.822,
            19.338,
            19.377,
            19.59,
            20.295,
            20.415,
            20.82,
            21.045,
            21.495,
            21.81,
            22.26,
            22.29,
            22.71,
            23.16,
            23.745,
            24.975,
            26.295
    };

    public static double[] inspiral100 = {
            1.463,
            2.025,
            2.25,
            2.7,
            3.375,
            3.6,
            4.05,
            4.5,
            4.98,
            5.85,
            6.33,
            6.75,
            7.2,
            7.65,
            8.1,
            8.55,
            9,
            9.9,
            11.7
    };

    public static void main(String[] args) throws Exception{
        solutionArrayList = new ArrayList<>();
        timeInMilliSecArrayList = new ArrayList<>();
        GlobalAccess.solutionRepository = new ArrayList<>();

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
                case "iterative-grp-heft":
                    RunIterativeGRPHEFTAlgorithm.runGRPHEFT();
                    break;
                case "grp-heft":
                    RunGRPHEFTAlgorithm.runGRPHEFT();
                    break;
                case "grp-pacsa":
                    RunGRPPACSAAlgorithm.runGRPPACSA();
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
            sb.append(',');
            sb.append("Sec");
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
