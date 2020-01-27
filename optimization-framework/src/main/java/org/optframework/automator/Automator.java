package org.optframework.automator;

import org.optframework.config.Config;
import org.optframework.core.Log;
import org.optframework.core.utils.Printer;

public class Automator {
    public static void main(String[] args) {
        try {
            Log.init();
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

            Log.logger.info("Number of Runs for Automator: " + Config.automator.number_of_runs);

            GenericAutomator automator;
            switch (Config.automator.type){
                case "bulk-automator":
                    automator = new BulkBudgetAutomator();
                    break;
                case "single-automator":
                    automator = new BudgetAutomator();
                    break;
                default:
                    throw new RuntimeException("This type of automator is not supported");
            }

            automator.run();
        }catch (Exception e){
            System.out.println(e);
        }
    }
}
