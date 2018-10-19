package org.optframework;

import org.cloudbus.spotsim.enums.AZ;
import org.cloudbus.spotsim.enums.InstanceType;
import org.cloudbus.spotsim.enums.OS;
import org.cloudbus.spotsim.enums.Region;
import org.optframework.config.Config;
import org.optframework.core.*;
import org.optframework.core.heft.HEFTAlgorithm;
import org.optframework.core.pacsa.PACSAOptimization;
import org.optframework.core.utils.PopulateWorkflow;
import org.optframework.core.utils.PreProcessor;
import org.optframework.core.utils.Printer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Hessam Modabberi hessam.modaberi@gmail.com
 * @version 1.0.0
 */

public class RunPACSAAlgorithm {

    private static double originalStartTemperature_SA;
    private static double originalCoolingFactor_SA;

    public static void runPACSA()
    {
        originalCoolingFactor_SA = Config.sa_algorithm.cooling_factor;
        originalStartTemperature_SA = Config.sa_algorithm.start_temperature;

        Log.logger.info("<<<<<<<<< PACSA Algorithm is started >>>>>>>>>");

        /**
         * Assumptions:
         * Region: europe
         * Availability Zone: A
         * OS type: Linux System
         * */
        InstanceInfo instanceInfo[] = InstanceInfo.populateInstancePrices(Region.EUROPE , AZ.A, OS.LINUX);

        int maxECUId = -1;
        double maxECU = 0.0;

        for (InstanceType type : InstanceType.values()){
            if (type.getEcu() > maxECU){
                maxECUId = type.getId();
                maxECU = type.getEcu();
            }
        }

        /**
         * Initializes available instances for the HEFT algorithm with the max number of instances and sets them to the most powerful instance type (that is 6)
         * */
        int totalInstances[] = new int[Config.global.m_number];
        for (int i = 0; i < Config.global.m_number; i++) {
            totalInstances[i] = maxECUId;
        }

        Log.logger.info("<<<<<<<<<<  HEFT Algorithm is started  >>>>>>>>>>>");

        Workflow heftWorkflow = PreProcessor.doPreProcessingForHEFT(PopulateWorkflow.populateWorkflowWithId(Config.global.budget, 0, Config.global.workflow_id), Config.global.bandwidth, totalInstances, instanceInfo);

        heftWorkflow.setBeta(Beta.computeBetaValue(heftWorkflow, instanceInfo, Config.global.m_number));

        HEFTAlgorithm heftAlgorithm = new HEFTAlgorithm(heftWorkflow, instanceInfo, totalInstances);
        Solution heftSolution = heftAlgorithm.runAlgorithm();
        heftSolution.fitness();

        if (Config.pacsa_algorithm.m_number_from_heft){
            Config.global.m_number = heftSolution.numberOfUsedInstances;
        }

        Workflow workflow = PreProcessor.doPreProcessing(PopulateWorkflow.populateWorkflowWithId(Config.global.budget, 0, Config.global.workflow_id));

        computeCoolingFactorForSA(workflow.getJobList().size());

        Log.logger.info("Maximum number of instances: " + Config.global.m_number + " Number of different types of instances: " + InstanceType.values().length + " Number of tasks: "+ workflow.getJobList().size());

        workflow.setBeta(Beta.computeBetaValue(workflow, instanceInfo, Config.global.m_number));

        OptimizationAlgorithm optimizationAlgorithm;

        List<Solution> solutionList = new ArrayList<>();

        for (int i = 0; i < Config.pacsa_algorithm.getNumber_of_runs(); i++) {
            Printer.printSplitter();
            Log.logger.info("<<<<<<<<<<<    NEW RUN "+ i +"     >>>>>>>>>>>\n");

            Config.sa_algorithm.cooling_factor = originalCoolingFactor_SA;
            Config.sa_algorithm.start_temperature = originalStartTemperature_SA;
            optimizationAlgorithm = new PACSAOptimization((1/(double)heftSolution.makespan),workflow, instanceInfo);

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

        Log.logger.info("Average Fitness value: " + fitnessSum / Config.pacsa_algorithm.getNumber_of_runs());
        Log.logger.info("Average Cost value: " + costSum / Config.pacsa_algorithm.getNumber_of_runs());

        Log.logger.info("Max fitness: " + fitnessMax + " Min fitness: "+ fitnessMin);
    }

    static void computeCoolingFactorForSA(int numberOfTasks){
        if (!Config.sa_algorithm.force_cooling){
            if (numberOfTasks >= 10){
                Config.sa_algorithm.cooling_factor = 1 - 1 / (double)numberOfTasks;
            }else {
                Config.sa_algorithm.cooling_factor = 0.9;
            }
        }
    }
}
