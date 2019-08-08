package org.optframework;

import org.optframework.automator.BudgetAutomator;
import org.optframework.config.Config;
import org.optframework.core.Log;
import org.optframework.core.utils.Printer;

import java.util.ArrayList;

/**
 * Entry point for this simulation
 *
 * @author Hessam hessam.modaberi@gmail.com
 * @since April 2018
 * */

public class Main {
    public static void main(String[] args) throws Exception{
        Log.init();
        Log.logger.info("Main Flow is started");

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

        GlobalAccess.solutionRepository = new ArrayList<>();

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
            case "budget-automator":
                BudgetAutomator.runAutomator();
                break;
        }
    }
}
