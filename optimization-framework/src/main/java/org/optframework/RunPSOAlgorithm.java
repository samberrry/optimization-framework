package org.optframework;

import org.cloudbus.spotsim.enums.AZ;
import org.cloudbus.spotsim.enums.InstanceType;
import org.cloudbus.spotsim.enums.OS;
import org.cloudbus.spotsim.enums.Region;
import org.optframework.config.Config;
import org.optframework.core.*;
import org.optframework.core.heft.HEFTService;
import org.optframework.core.pso.PSOOptimization;
import org.optframework.core.pso.Particle;
import org.optframework.core.pso.ZPSOOptimization;
import org.optframework.core.utils.PopulateWorkflow;
import org.optframework.core.utils.PreProcessor;
import org.optframework.core.utils.Printer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RunPSOAlgorithm {
    public static void runPSO(int algorithmId)
    {
        Log.logger.info("<<<<<<<<< PSO Algorithm is started >>>>>>>>>");

        InstanceInfo instanceInfo[] = InstanceInfo.populateInstancePrices(Region.EUROPE , AZ.A, OS.LINUX);

        Workflow workflow = PreProcessor.doPreProcessing(PopulateWorkflow.populateWorkflowWithId(Config.global.budget, 0, Config.global.workflow_id));

        Config.global.m_number = workflow.getJobList().size();

        GlobalAccess.orderedJobList = workflow.getJobList();
        Collections.sort(GlobalAccess.orderedJobList, Job.rankComparator);

        Log.logger.info("Maximum number of instances: " + Config.global.m_number + " Number of different types of instances: " + InstanceType.values().length + " Number of tasks: "+ workflow.getJobList().size());

        workflow.setBeta(Beta.computeBetaValue(workflow, instanceInfo, Config.global.m_number));

        OptimizationAlgorithm optimizationAlgorithm = null;

        List<Solution> solutionList = new ArrayList<>();

        for (int i = 0; i < Config.pso_algorithm.getNumber_of_runs(); i++) {
            Printer.printSplitter();
            Log.logger.info("<<<<<<<<<<<    NEW RUN "+ i +"     >>>>>>>>>>>\n");

            switch (algorithmId){
                case 0:
                    optimizationAlgorithm = new PSOOptimization(null
                            , workflow, instanceInfo);
                    break;
                case 1:
                    optimizationAlgorithm = new ZPSOOptimization(null
                            , workflow, instanceInfo);
                    break;
            }

            long start = System.currentTimeMillis();

            Solution solution = optimizationAlgorithm.runAlgorithm();
            solutionList.add(solution);

            long stop = System.currentTimeMillis();

            Printer.lightPrintSolution(solution,stop-start);
        }

        double fitnessSum = 0.0, costSum = 0.0;
        double fitnessMax = 0.0, costMax = 0.0;
        double fitnessMin = 999999999999.9, costMin = 9999999999.9;
        Solution bestSolution = null;

        for (Solution solution: solutionList){
            fitnessSum += solution.fitnessValue;
            if (solution.fitnessValue > fitnessMax){
                fitnessMax = solution.fitnessValue;
            }
            if (solution.fitnessValue < fitnessMin){
                fitnessMin = solution.fitnessValue;
                bestSolution = solution;
            }

            costSum += solution.cost;
            if (solution.cost > costMax){
                costMax = solution.cost;
            }
            if (solution.cost < costMin){
                costMin = solution.cost;
            }
        }
        Printer.printSplitter();

        Printer.printSolutionWithouthTime(bestSolution, instanceInfo);

        Printer.printSplitter();

        Log.logger.info("Average Fitness value: " + fitnessSum / Config.pso_algorithm.getNumber_of_runs());
        Log.logger.info("Average Cost value: " + costSum / Config.pso_algorithm.getNumber_of_runs());

        Log.logger.info("Max fitness: " + fitnessMax + " Min fitness: "+ fitnessMin);
    }
}
