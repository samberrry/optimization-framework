package org.optframework.automator;

import org.optframework.GlobalAccess;
import org.optframework.RunGRPHEFTAlgorithm;
import org.optframework.RunGRPPACSAAlgorithm;
import org.optframework.RunIterativeGRPHEFTAlgorithm;
import org.optframework.config.Config;
import org.optframework.core.Solution;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;

import static org.optframework.automator.BudgetList.*;
import static org.optframework.automator.BudgetList.cybershake30;

/**
 * Bulk Budget Automator (Automates budget setup for small workflows)
 * This Utility class facilitates getting the result process
 * Automates budget based on Evolutionary Multi-Objective Workflow Scheduling in Cloud paper
 *
 * */

public class BulkBudgetAutomator implements GenericAutomator{
    //array of solutions which should be printed to csv file
    public static ArrayList<Solution> solutionArrayListToCSV;
    public static ArrayList<Long> timeInMilliSecArrayList;

    public void run() throws Exception{

        Integer workflowIdList[] = {
                2,
                11,
                21,
                31,
                41,
                3,
                12,
                22,
                32,
                42,
                4,
                13,
                23,
                33,
                43
        };

        //iterates through workflows
        for (Integer workflowId: workflowIdList){
            Config.global.workflow_id = workflowId;

            double budgetList[] = null;
            solutionArrayListToCSV = new ArrayList<>();
            timeInMilliSecArrayList = new ArrayList<>();
            GlobalAccess.solutionArrayListToCSV = new ArrayList<>();
            GlobalAccess.timeInMilliSecArrayList = new ArrayList<>();
            GlobalAccess.solutionRepository = new ArrayList<>();

            //array of solutions which should be printed to csv file
            ArrayList<Solution> solutionArrayListToCSV;
            ArrayList<Long> timeInMilliSecArrayList;

            switch (Config.global.workflow_id){
                case 1: budgetList = inspiral1000; break;
                case 2: budgetList = inspiral100; break;
                case 3: budgetList = inspiral50; break;
                case 4: budgetList = inspiral30; break;
                //
                case 10: budgetList = montage1000; break;
                case 11: budgetList = montage100; break;
                case 12: budgetList = montage50; break;
                case 13: budgetList = montage25; break;
                //
                case 20: budgetList = sipht1000; break;
                case 21: budgetList = sipht100; break;
                case 22: budgetList = sipht60; break;
                case 23: budgetList = sipht30; break;
                //
                case 30: budgetList = epigenomics997; break;
                case 31: budgetList = epigenomics100; break;
                case 32: budgetList = epigenomics46; break;
                case 33: budgetList = epigenomics24; break;
                //
                case 40: budgetList = cybershake1000; break;
                case 41: budgetList = cybershake100; break;
                case 42: budgetList = cybershake50; break;
                case 43: budgetList = cybershake30; break;
            }

            if (budgetList == null){
                throw new RuntimeException("This type of workflow is not supported to be automated");
            }

            for (double budget: budgetList){
                Config.global.budget = budget;
                //at the end of run** method the automator-specific static variables will be filled
                switch (Config.global.algorithm){
                    case "sa":
//                    RunSAAlgorithm.runSA();
                    case "hbmo":
//                    RunHBMOAlgorithm.runHBMO();
                    case "heft":
//                    RunHEFTAlgorithm.runSingleHEFT();
                    case "hbmo-heft":
//                    RunHEFTWithHBMO.runHEFTWithHBMO();
                    case "heft-example":
//                    RunHEFTExample.runHEFTExample();
                    case "pacsa":
//                    RunPACSAAlgorithm.runPACSA(0);
                    case "pacsa-plus":
//                    RunPACSAAlgorithm.runPACSA(1);
                    case "pso":
//                    RunPSOAlgorithm.runPSO(0);
                    case "zpso":
//                    RunPSOAlgorithm.runPSO(1);
                        throw new RuntimeException("This Algorithm does not support Automator");
                    case "iterative-grp-heft": RunIterativeGRPHEFTAlgorithm.runGRPHEFT();break;
                    case "grp-heft": RunGRPHEFTAlgorithm.runGRPHEFT();break;
                    case "grp-pacsa": RunGRPPACSAAlgorithm.runGRPPACSA();break;
                }
            }

            try (PrintWriter writer = new PrintWriter(new File("automator-"+ Config.global.algorithm + "-"+ GlobalAccess.workflowName +  ".csv"))) {
                solutionArrayListToCSV = GlobalAccess.solutionArrayListToCSV;
                timeInMilliSecArrayList = GlobalAccess.timeInMilliSecArrayList;

                if(solutionArrayListToCSV.size() == 0 || timeInMilliSecArrayList.size() == 0){
                    throw new RuntimeException("This Algorithm does not support Automator");
                }

                StringBuilder sb = new StringBuilder();
                sb.append("Budget Automator     " + Config.global.algorithm + "-"+ GlobalAccess.workflowName + "\n");
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

            }catch (RuntimeException e){
                System.out.println(e.getMessage());
            }
        }
    }
}
