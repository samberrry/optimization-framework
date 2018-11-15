package org.optframework;

import com.rits.cloning.Cloner;
import org.cloudbus.cloudsim.util.workload.WorkflowDAG;
import org.cloudbus.spotsim.enums.AZ;
import org.cloudbus.spotsim.enums.InstanceType;
import org.cloudbus.spotsim.enums.OS;
import org.cloudbus.spotsim.enums.Region;
import org.optframework.config.Config;
import org.optframework.core.*;
import org.optframework.core.heft.HEFTAlgorithm;
import org.optframework.core.heft.HEFTService;
import org.optframework.core.lossandgain.Loss2Algorithm;
import org.optframework.core.lossandgain.Loss3Algorithm;
import org.optframework.core.pacsa.PACSAIterationNumber;
import org.optframework.core.pacsa.PACSAOptimization;
import org.optframework.core.utils.PopulateWorkflow;
import org.optframework.core.utils.PreProcessor;
import org.optframework.core.utils.Printer;
import org.optframework.database.MySQLSolutionRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Hessam Modabberi hessam.modaberi@gmail.com
 * @version 1.0.0
 */

public class RunPACSAAlgorithm {

    private static double originalStartTemperature_SA;
    private static double originalCoolingFactor_SA;
    private static int originalMNumber;

    public static void runPACSA(int algorithmId)
    {
        originalCoolingFactor_SA = Config.sa_algorithm.cooling_factor;
        originalStartTemperature_SA = Config.sa_algorithm.start_temperature;
        originalMNumber = Config.global.m_number;

        Log.logger.info("<<<<<<<<< PACSA Algorithm is started >>>>>>>>>");

        /**
         * Assumptions:
         * Region: europe
         * Availability Zone: A
         * OS type: Linux System
         * */
        InstanceInfo instanceInfo[] = InstanceInfo.populateInstancePrices(Region.EUROPE , AZ.A, OS.LINUX);
        Workflow workflow = PreProcessor.doPreProcessing(PopulateWorkflow.populateWorkflowWithId(Config.global.budget, 0, Config.global.workflow_id));

        Cloner cloner = new Cloner();
        GlobalAccess.orderedJobList = cloner.deepClone(workflow.getJobList());
        Collections.sort(GlobalAccess.orderedJobList, Job.rankComparator);

        /**
         * Compute the maximum number of used instances
         * */
        if (algorithmId == 1){
            Config.global.algorithm = "pacsa_plus";
            WorkflowDAG dag = workflow.getWfDAG();
            ArrayList<Integer> nextLevel = dag.getFirstLevel();
            int temp = nextLevel.size();

            while (nextLevel.size() != 0){
                if (nextLevel.size() > temp){
                    temp = nextLevel.size();
                }
                nextLevel = dag.getNextLevel(nextLevel);
            }
            Config.global.m_number = temp;
        }else {
            Config.global.m_number = workflow.getJobList().size();
        }

        if (Config.global.read_m_number_from_config){
            Config.global.m_number = originalMNumber;
        }

        Log.logger.info("<<<<<<<<<<  HEFT Algorithm is started  >>>>>>>>>>>");
        int totalInstances[] = HEFTAlgorithm.getTotalInstancesForHEFT(workflow.getJobList().size());

        Workflow heftWorkflow = PreProcessor.doPreProcessingForHEFT(PopulateWorkflow.populateWorkflowWithId(Config.global.budget, 0, Config.global.workflow_id), Config.global.bandwidth, totalInstances, instanceInfo);

        heftWorkflow.setBeta(Beta.computeBetaValue(heftWorkflow, instanceInfo, Config.global.m_number));

        HEFTAlgorithm heftAlgorithm = new HEFTAlgorithm(heftWorkflow, instanceInfo, totalInstances);
        Solution heftSolution = heftAlgorithm.runAlgorithm();
        heftSolution.heftFitness();
        Printer.lightPrintSolution(heftSolution , 0);

        Loss2Algorithm loss2Algorithm = new Loss2Algorithm(heftSolution, totalInstances, workflow, instanceInfo);
        Solution loss2Solution = loss2Algorithm.runAlgorithm();

        Loss3Algorithm loss3Algorithm = new Loss3Algorithm(heftSolution, totalInstances, workflow, instanceInfo);
        Solution loss3Solution = loss3Algorithm.runAlgorithm();


        computeCoolingFactorForSA(workflow.getJobList().size());

        Log.logger.info("Maximum number of instances: " + Config.global.m_number + " Number of different types of instances: " + InstanceType.values().length + " Number of tasks: "+ workflow.getJobList().size());

        workflow.setBeta(Beta.computeBetaValue(workflow, instanceInfo, Config.global.m_number));

        OptimizationAlgorithm optimizationAlgorithm;

        List<Solution> solutionList = new ArrayList<>();
        List<Solution> initialSolutionList = getInitialSolution(instanceInfo);

        long runTimeSum = 0;

        for (int i = 0; i < Config.pacsa_algorithm.getNumber_of_runs(); i++) {
            Printer.printSplitter();
            Log.logger.info("<<<<<<<<<<<    NEW RUN "+ i +"     >>>>>>>>>>>\n");

            Config.sa_algorithm.cooling_factor = originalCoolingFactor_SA;
            Config.sa_algorithm.start_temperature = originalStartTemperature_SA;
            if (Config.pacsa_algorithm.iteration_number_based){
                optimizationAlgorithm = new PACSAIterationNumber(initialSolutionList, (1/(double)heftSolution.makespan),workflow, instanceInfo);
            }else {
                optimizationAlgorithm = new PACSAOptimization(initialSolutionList ,(1/(double)heftSolution.makespan),workflow, instanceInfo);
            }

            long start = System.currentTimeMillis();

            Solution solution = optimizationAlgorithm.runAlgorithm();
            solutionList.add(solution);

            long stop = System.currentTimeMillis();

            runTimeSum += (stop - start);

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

        //compute resource utilization
        double resourceUtilization[] = new double[bestSolution.numberOfUsedInstances];
        double onlyTaskUtilization[] = new double[bestSolution.numberOfUsedInstances];
        for (Job job : workflow.getJobList()){
            onlyTaskUtilization[bestSolution.xArray[job.getIntId()]] += job.getExeTime()[bestSolution.yArray[bestSolution.xArray[job.getIntId()]]];
        }

        for (int i = 0; i < bestSolution.numberOfUsedInstances; i++) {
            resourceUtilization[i] = onlyTaskUtilization[i] / bestSolution.instanceTimes[i];
        }
        Printer.printUtilization(resourceUtilization);

        Printer.printSplitter();

        String toPrint = "\n";
        toPrint += "Average Fitness value: " + fitnessSum / Config.pacsa_algorithm.getNumber_of_runs() + "\n";
        toPrint += "Average Cost value: " + costSum / Config.pacsa_algorithm.getNumber_of_runs() + "\n";
        toPrint += "Max fitness: " + fitnessMax + " Min fitness: "+ fitnessMin + "\n";
        Log.logger.info(toPrint);

        if (Config.global.use_mysql_to_log){
            MySQLSolutionRepository sqlSolutionRepository = new MySQLSolutionRepository();
            sqlSolutionRepository.updateRecord(
                    fitnessMin,
                    fitnessMax,
                    fitnessSum / Config.pacsa_algorithm.getNumber_of_runs(),
                    costSum / Config.pacsa_algorithm.getNumber_of_runs(),
                    (runTimeSum / Config.pacsa_algorithm.getNumber_of_runs())/1000);

            Log.logger.info("Successfully logged to the mysql database!");
        }
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

    public static List<Solution> getInitialSolution(InstanceInfo instanceInfo[]){
        List<Solution> initialSolutionList = new ArrayList<>();
        if (Config.pacsa_algorithm.insert_heft_initial_solution){
            Solution initialSolution = HEFTService.getCostEfficientHEFT(instanceInfo);
            initialSolutionList.add(initialSolution);

            Solution heftSolution = HEFTService.getHEFT(instanceInfo);
            initialSolutionList.add(heftSolution);
        }
        return initialSolutionList;
    }
}
