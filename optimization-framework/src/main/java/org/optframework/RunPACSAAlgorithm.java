package org.optframework;

import com.rits.cloning.Cloner;
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

    public static int Best_Iteration =0;//shows in which iteration of Pacsa the general best solution has been founded

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
        boolean big_budget = false;
        InstanceInfo instanceInfo[] = InstanceInfo.populateInstancePrices(Region.EUROPE , AZ.A, OS.LINUX);
        Workflow workflow = PreProcessor.doPreProcessing(PopulateWorkflow.populateWorkflowWithId(Config.global.budget, 0, Config.global.workflow_id));

        Cloner cloner = new Cloner();
        GlobalAccess.orderedJobList = cloner.deepClone(workflow.getJobList());
        Collections.sort(GlobalAccess.orderedJobList, Job.rankComparator);

        //todo:...
        Config.global.m_number = workflow.getJobList().size();

        Log.logger.info("<<<<<<<<<<  HEFT Algorithm is started  >>>>>>>>>>>");
        int totalInstances[] = HEFTAlgorithm.getTotalInstancesForHEFT(workflow.getJobList().size() * instanceInfo.length);

        Workflow heftWorkflow = PreProcessor.doPreProcessingForHEFT(PopulateWorkflow.populateWorkflowWithId(Config.global.budget, 0, Config.global.workflow_id), Config.global.bandwidth, totalInstances, instanceInfo);

        heftWorkflow.setBeta(Beta.computeBetaValue(heftWorkflow, instanceInfo, Config.global.m_number));

        HEFTAlgorithm heftAlgorithm = new HEFTAlgorithm(heftWorkflow, instanceInfo, totalInstances);
        Solution heftSolution = heftAlgorithm.runAlgorithm();
        heftSolution.heftFitness();
        Printer.lightPrintSolution(heftSolution , 0);

        Loss2Algorithm loss2Algorithm = new Loss2Algorithm(heftSolution, totalInstances, workflow, instanceInfo);
        Solution loss2Solution = loss2Algorithm.runAlgorithm();


        Loss3Algorithm loss3Algorithm = new Loss3Algorithm(heftSolution, totalInstances, workflow, instanceInfo);
        Solution loss3Solution = heftSolution;
        if(!Config.global.deadline_based) {
            loss3Solution = loss3Algorithm.runAlgorithm2();
        }


      //  Solution loss3Solution2 = loss3Algorithm.runAlgorithm2();

        /**
        * Compute the maximum number of used instances
        * */
        //todo:...
        loss3Solution.solutionMapping();
    //    loss3Solution2.solutionMapping();
        loss2Solution.solutionMapping();
        heftSolution.solutionMapping();

        if (algorithmId == 1) {
            int m_number;
            Config.global.algorithm = "pacsa_plus";

          /*  Config.global.m_number = GlobalAccess.maxLevel;
            m_number = GlobalAccess.maxLevel;
            if (loss2Solution.numberOfUsedInstances > loss3Solution.numberOfUsedInstances){
                Config.global.m_number = loss2Solution.numberOfUsedInstances;
                m_number = loss2Solution.numberOfUsedInstances;
            }else {
                Config.global.m_number = loss3Solution.numberOfUsedInstances;
                m_number = loss3Solution.numberOfUsedInstances;
            }
            if (m_number < heftSolution.numberOfUsedInstances){
                Config.global.m_number = heftSolution.numberOfUsedInstances;
            }
        }else {
            Config.global.m_number = workflow.getJobList().size();
        }*/

            if (workflow.getJobList().size() >= 900) {
                double avg_cost_instances = 0.2;
                int estimated_number_of_used_instances = 50;
                int max_number_of_used_instaned = (int) (Config.global.budget / avg_cost_instances) + 1;
                if (max_number_of_used_instaned > estimated_number_of_used_instances)
                    Config.global.m_number = estimated_number_of_used_instances;
                else
                    Config.global.m_number = max_number_of_used_instaned;
            } else if (workflow.getJobList().size() >= 90) {
                double avg_cost_instances = 0.2;
                int estimated_number_of_used_instances = 20;
                int max_number_of_used_instaned = (int) (Config.global.budget / avg_cost_instances) + 1;
                if (max_number_of_used_instaned > estimated_number_of_used_instances)
                    Config.global.m_number = estimated_number_of_used_instances;
                else
                    Config.global.m_number = max_number_of_used_instaned;
            } else {
                double avg_cost_instances = 0.2;
                int estimated_number_of_used_instances = 8;
                int max_number_of_used_instaned = (int) (Config.global.budget / avg_cost_instances) + 1;
                if (max_number_of_used_instaned > estimated_number_of_used_instances)
                    Config.global.m_number = estimated_number_of_used_instances;
                else
                    Config.global.m_number = max_number_of_used_instaned;
            }

            double cost_fastest_instance = 0.9;
            double min_cost_instances = 0.06;

            if (Config.global.m_number <= 2)//means that the budget is very small
            {

                Config.global.m_number = (int) (Config.global.budget / min_cost_instances) + 1;
            }
         /*   else if (Config.global.budget / Config.global.m_number >= cost_fastest_instance)// means that the budget is very big
            {
                big_budget = true;
                Config.global.m_number = heftSolution.numberOfUsedInstances;
            }*/
        }


        if (Config.global.read_m_number_from_config){
            Config.global.m_number = originalMNumber;
        }

        computeCoolingFactorForSA(workflow.getJobList().size());

        Log.logger.info("Maximum number of instances: " + Config.global.m_number + " Number of different types of instances: " + InstanceType.values().length + " Number of tasks: "+ workflow.getJobList().size());

        workflow.setBeta(Beta.computeBetaValue(workflow, instanceInfo, Config.global.m_number));

        OptimizationAlgorithm optimizationAlgorithm;

        List<Solution> solutionList = new ArrayList<>();
        List<Solution> initialSolutionList = null;

        if (Config.pacsa_algorithm.insert_heft_initial_solution){
            List<Job> orderedJobList = GlobalAccess.orderedJobList;
            Integer zArray[] = new Integer[orderedJobList.size()];
            for (int i = 0; i < orderedJobList.size(); i++) {
                zArray[i] = orderedJobList.get(i).getIntId();
            }
            initialSolutionList = new ArrayList<>();

            loss2Solution.zArray = zArray;
    //        loss3Solution.zArray = zArray;
    //        loss3Solution2.zArray = zArray;
            heftSolution.zArray = zArray;

            loss2Solution.maxNumberOfInstances = Config.global.m_number;
            loss3Solution.maxNumberOfInstances = Config.global.m_number;
            heftSolution.maxNumberOfInstances = Config.global.m_number;

          //  Solution costEfficientHeftSolution = HEFTService.getCostEfficientHEFT(instanceInfo, workflow.getNumberTasks());
          //  costEfficientHeftSolution.solutionMapping();
          //  costEfficientHeftSolution.maxNumberOfInstances = Config.global.m_number;

            heftSolution.origin = "heft";
            loss2Solution.origin = "loss2";
            loss3Solution.origin = "loss3";
    //        loss3Solution2.origin = "loss3";
            //costEfficientHeftSolution.origin = "cost-efficient-heft";



            initialSolutionList.add(loss2Solution);



    //        initialSolutionList.add(loss3Solution);
    //        initialSolutionList.add(loss3Solution2);

            if(big_budget)
                initialSolutionList.add(heftSolution);
         //   initialSolutionList.add(heftSolution);



         //   initialSolutionList.add(costEfficientHeftSolution);



            /**
             * HEFT algorithm which is limited by m_number
             * */


            double cost_fastest_instance = 0.9;
            int number_of_affordable_fastest_instance = (int)(Config.global.budget/cost_fastest_instance);
            if(number_of_affordable_fastest_instance > 0) {

                int totalInstances2[] = HEFTAlgorithm.getTotalInstancesForHEFT(number_of_affordable_fastest_instance);
                Workflow heftWorkflow2 = PreProcessor.doPreProcessingForHEFT(PopulateWorkflow.populateWorkflowWithId(Config.global.budget, 0, Config.global.workflow_id), Config.global.bandwidth, totalInstances2, instanceInfo);

                heftWorkflow2.setBeta(Beta.computeBetaValue(heftWorkflow2, instanceInfo, number_of_affordable_fastest_instance));

                HEFTAlgorithm heftAlgorithm2 = new HEFTAlgorithm(heftWorkflow2, instanceInfo, totalInstances2);
                Solution heftSolution2 = heftAlgorithm2.runAlgorithm();
                heftSolution2.heftFitness();

                Integer zArray2[] = new Integer[orderedJobList.size()];
                for (int i = 0; i < orderedJobList.size(); i++) {
                    zArray2[i] = orderedJobList.get(i).getIntId();
                }

                heftSolution2.zArray = zArray2;

                initialSolutionList.add(heftSolution2);
            }



            number_of_affordable_fastest_instance++;
            int totalInstances3[] = HEFTAlgorithm.getTotalInstancesForHEFT(number_of_affordable_fastest_instance);
            Workflow heftWorkflow3 = PreProcessor.doPreProcessingForHEFT(PopulateWorkflow.populateWorkflowWithId(Config.global.budget, 0, Config.global.workflow_id), Config.global.bandwidth, totalInstances3, instanceInfo);

            heftWorkflow3.setBeta(Beta.computeBetaValue(heftWorkflow3, instanceInfo, number_of_affordable_fastest_instance));

            HEFTAlgorithm heftAlgorithm3 = new HEFTAlgorithm(heftWorkflow3, instanceInfo, totalInstances3);
            Solution heftSolution3 = heftAlgorithm3.runAlgorithm();
            heftSolution3.heftFitness();

            Integer zArray3[] = new Integer[orderedJobList.size()];
            for (int i = 0; i < orderedJobList.size(); i++) {
                zArray3[i] = orderedJobList.get(i).getIntId();
            }

            heftSolution3.zArray = zArray3;

            initialSolutionList.add(heftSolution3);

        }

        long runTimeSum = 0;

        for (int i = 0; i < Config.pacsa_algorithm.getNumber_of_runs(); i++) {
            Printer.printSplitter();
            Log.logger.info("<<<<<<<<<<<    NEW RUN "+ i +"     >>>>>>>>>>>\n");

            Config.sa_algorithm.cooling_factor = originalCoolingFactor_SA;
            Config.sa_algorithm.start_temperature = originalStartTemperature_SA;
            if (Config.pacsa_algorithm.iteration_number_based){                                ///////1.0/(10.0*workflow.getJobList().size())
                optimizationAlgorithm = new PACSAIterationNumber(initialSolutionList, 1.0/(10.0*(double)heftSolution.makespan), workflow, instanceInfo);
            }else {
                optimizationAlgorithm = new PACSAOptimization(initialSolutionList ,(1.0/4.0*(double)heftSolution.makespan),workflow, instanceInfo);
            }

            long start = System.currentTimeMillis();

            Solution solution = optimizationAlgorithm.runAlgorithm();
            solution.solutionMapping();
            solution.fitness();
            solutionList.add(solution);

            long stop = System.currentTimeMillis();

            runTimeSum += (stop - start);

            Printer.lightPrintSolution(solution,stop-start);
            Printer.printSolutionWithouthTime(solution, instanceInfo);
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
}
