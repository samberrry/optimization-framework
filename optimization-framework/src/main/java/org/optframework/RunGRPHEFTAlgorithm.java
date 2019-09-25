package org.optframework;

import com.rits.cloning.Cloner;
import org.cloudbus.spotsim.enums.AZ;
import org.cloudbus.spotsim.enums.OS;
import org.cloudbus.spotsim.enums.Region;
import org.optframework.automator.BudgetAutomator;
import org.optframework.core.InstanceInfo;
import org.optframework.core.Log;
import org.optframework.core.Solution;
import org.optframework.core.grpheft.GRPHEFTAlgorithm;
import org.optframework.core.utils.Printer;

/**
 * Non-iterative version of the Modified-HEFT GRP Algorithm
 * @author Hessam hessam.mdoaberi@gmail.com
 * */

public class RunGRPHEFTAlgorithm {
    public static boolean thisTypeIsUsedAsMaxEfficient[];

    public static void runGRPHEFT(){
        Log.logger.info("<<<<<<<<< GRP-HEFT Algorithm is started >>>>>>>>>");

        /**
         * Assumptions:
         * Region: europe
         * Availability Zone: A
         * OS type: Linux System
         * */
        InstanceInfo instanceInfo[] = InstanceInfo.populateInstancePrices(Region.EUROPE , AZ.A, OS.LINUX);

        long start = System.currentTimeMillis();
        Cloner cloner = new Cloner();
        InstanceInfo originalInstanceInfo[] = cloner.deepClone(instanceInfo);
        InstanceInfo tempInstanceInfo[] = instanceInfo;
        RunGRPHEFTAlgorithm.thisTypeIsUsedAsMaxEfficient = new boolean[instanceInfo.length];

        GRPHEFTAlgorithm grpheftAlgorithm = new GRPHEFTAlgorithm(tempInstanceInfo);
        Solution finalSolution = grpheftAlgorithm.runAlgorithm();

        finalSolution.origin = "non-iterative-grp-heft";

        long end = System.currentTimeMillis();
        Log.logger.info("<<<<<<<<< GRP Final Result >>>>>>>>>");
        Printer.printSolutionWithouthTime(finalSolution, originalInstanceInfo);
//        BudgetAutomator.solution = finalSolution;
//        BudgetAutomator.timeInMilliSec = end - start;

        GlobalAccess.solutionRepository.add(finalSolution);
        GlobalAccess.latestSolution = finalSolution;
    }
}
