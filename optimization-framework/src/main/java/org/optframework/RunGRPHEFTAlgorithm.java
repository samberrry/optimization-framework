package org.optframework;

import org.cloudbus.spotsim.enums.AZ;
import org.cloudbus.spotsim.enums.InstanceType;
import org.cloudbus.spotsim.enums.OS;
import org.cloudbus.spotsim.enums.Region;
import org.optframework.core.InstanceInfo;
import org.optframework.core.Log;
import org.optframework.core.Solution;
import org.optframework.core.grpheft.GRPHEFTAlgorithm;
import org.optframework.core.utils.Printer;

import java.util.ArrayList;

public class RunGRPHEFTAlgorithm {

    public static void runGRPHEFT(){
        Log.logger.info("<<<<<<<<< GRP-HEFT Algorithm is started >>>>>>>>>");

        /**
         * Assumptions:
         * Region: europe
         * Availability Zone: A
         * OS type: Linux System
         * */
        InstanceInfo instanceInfo[] = InstanceInfo.populateInstancePrices(Region.EUROPE , AZ.A, OS.LINUX);

        ArrayList<Solution> solutionArrayList = new ArrayList<>();
        InstanceInfo tempInstanceInfo[] = instanceInfo;


        for (int i = 0; i < InstanceType.values().length; i++) {
            Log.logger.info("Run " + i);
            System.out.println(i);

            GRPHEFTAlgorithm grpheftAlgorithm = new GRPHEFTAlgorithm(tempInstanceInfo);
            Solution solution = grpheftAlgorithm.runAlgorithm();
            solutionArrayList.add(solution);

            tempInstanceInfo = new InstanceInfo[instanceInfo.length-1];
            for (int j = 0; j < instanceInfo.length - 1; j++) {
                tempInstanceInfo[j] = instanceInfo[j];
            }
            instanceInfo = tempInstanceInfo;
        }

        double temp = 99999999999999.9;
        Solution finalSolution = null;
        for (Solution solution : solutionArrayList){
            if (solution.fitnessValue < temp){
                temp = solution.fitnessValue;
                finalSolution = solution;
            }
        }

        Log.logger.info("<<<<<<<<< Final Result >>>>>>>>>");

        Printer.printSolutionWithouthTime(finalSolution, instanceInfo);

    }

}
