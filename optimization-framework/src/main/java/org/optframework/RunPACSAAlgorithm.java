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
import org.optframework.core.heft.Instance;
import org.optframework.core.lossandgain.Loss2Algorithm;
import org.optframework.core.lossandgain.Loss3Algorithm;
import org.optframework.core.pacsa.PACSAIterationNumber;
import org.optframework.core.pacsa.PACSAOptimization;
import org.optframework.core.utils.PopulateWorkflow;
import org.optframework.core.utils.PreProcessor;
import org.optframework.core.utils.Printer;
import org.optframework.database.MySQLSolutionRepository;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

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
        int totalInstances[] = HEFTAlgorithm.getTotalInstancesForHEFT(3);

        Workflow heftWorkflow = PreProcessor.doPreProcessingForHEFT(PopulateWorkflow.populateWorkflowWithId(Config.global.budget, 0, Config.global.workflow_id), Config.global.bandwidth, totalInstances, instanceInfo);

        heftWorkflow.setBeta(Beta.computeBetaValue(heftWorkflow, instanceInfo, Config.global.m_number));

        HEFTAlgorithm heftAlgorithm = new HEFTAlgorithm(heftWorkflow, instanceInfo, totalInstances);
        Solution heftSolution = heftAlgorithm.runAlgorithm();
        heftSolution.heftFitness();

        heftSolution.fitness();
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

        List<Job> orderedJobList = GlobalAccess.orderedJobList;

        double minPrice = 9999999999.0;

        for (InstanceType type : InstanceType.values()) {
            if (instanceInfo[type.getId()].getSpotPrice() < minPrice) {
                minPrice = instanceInfo[type.getId()].getSpotPrice();
            }
        }

        if (algorithmId == 1) {
            int m_number;
            Config.global.algorithm = "pacsa_plus";

            Config.global.m_number = GlobalAccess.maxLevel;
        /*    m_number = GlobalAccess.maxLevel;
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







          /*  double cost_Fastest_instance = 0.9;
            if (workflow.getJobList().size() >= 900) {
                double avg_cost_instances = 0.2;
                int estimated_number_of_used_instances = Max(50,(int)(Config.global.budget/cost_Fastest_instance)+1);
                estimated_number_of_used_instances = Min(estimated_number_of_used_instances,GlobalAccess.maxLevel);
                int max_number_of_used_instaned = (int) (Config.global.budget / avg_cost_instances) + 1;
                if (max_number_of_used_instaned > estimated_number_of_used_instances)
                    Config.global.m_number = estimated_number_of_used_instances;
                else
                    Config.global.m_number = max_number_of_used_instaned;
            } else if (workflow.getJobList().size() >= 90) {
                double avg_cost_instances = 0.2;
                int estimated_number_of_used_instances = Max(20,(int)(Config.global.budget/cost_Fastest_instance)+1);
                estimated_number_of_used_instances = Min(estimated_number_of_used_instances,GlobalAccess.maxLevel);
                int max_number_of_used_instaned = (int) (Config.global.budget / avg_cost_instances) + 1;
                if (max_number_of_used_instaned > estimated_number_of_used_instances)
                    Config.global.m_number = estimated_number_of_used_instances;
                else
                    Config.global.m_number = max_number_of_used_instaned;
            } else {
                double avg_cost_instances = 0.2;
                int estimated_number_of_used_instances = Max(8,(int)(Config.global.budget/cost_Fastest_instance)+1);
                estimated_number_of_used_instances = Min(estimated_number_of_used_instances,GlobalAccess.maxLevel);
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
            }*/

        }


        if (Config.global.read_m_number_from_config){
            Config.global.m_number = originalMNumber;
        }

        computeCoolingFactorForSA(workflow.getJobList().size());


        workflow.setBeta(Beta.computeBetaValue(workflow, instanceInfo, Config.global.m_number));

        OptimizationAlgorithm optimizationAlgorithm;

        List<Solution> solutionList = new ArrayList<>();
        List<Solution> initialSolutionList = null;
        int modifiedHeftMNumber = -1;

        if (Config.pacsa_algorithm.insert_heft_initial_solution) {

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


        //    initialSolutionList.add(loss2Solution);


            //        initialSolutionList.add(loss3Solution);
            //        initialSolutionList.add(loss3Solution2);

       //     if (big_budget)
        //        initialSolutionList.add(heftSolution);
            //   initialSolutionList.add(heftSolution);


            //   initialSolutionList.add(costEfficientHeftSolution);


            /**
             * HEFT algorithm which is limited by m_number
             * */



         /*   if(number_of_affordable_fastest_instance > 0) {

                int totalInstances2[] = HEFTAlgorithm.getTotalInstancesForHEFTMostPowerful(number_of_affordable_fastest_instance);
                Workflow heftWorkflow2 = PreProcessor.doPreProcessingForHEFT(PopulateWorkflow.populateWorkflowWithId(Config.global.budget, 0, Config.global.workflow_id), Config.global.bandwidth, totalInstances2, instanceInfo);

                heftWorkflow2.setBeta(Beta.computeBetaValue(heftWorkflow2, instanceInfo, Config.global.m_number));

                HEFTAlgorithm heftAlgorithm2 = new HEFTAlgorithm(heftWorkflow2, instanceInfo, totalInstances2, Config.global.m_number);
                Solution heftSolution2 = heftAlgorithm2.runAlgorithm();
                heftSolution2.heftFitness();

                Integer zArray2[] = new Integer[orderedJobList.size()];
                for (int i = 0; i < orderedJobList.size(); i++) {
                    zArray2[i] = orderedJobList.get(i).getIntId();
                }

                heftSolution2.zArray = zArray2;

                initialSolutionList.add(heftSolution2);
                Printer.printSolutionWithouthTime(heftSolution2,instanceInfo);
            }*/

            // number_of_affordable_fastest_instance++;


          //  int sorted_instanceTypes_based_on_Cost_per_ComputeUnit[] = {7,6,4,2,5,3,1,0};

            int id_fastest_instance = 7;
            double cost_fastest_instance = instanceInfo[id_fastest_instance].getSpotPrice();

            int number_of_affordable_fastest_instance = (int)((Config.global.budget)/cost_fastest_instance);


         /*   double cost_of_additional_hours = 0;// when an instance is run for more than one hour

            double sum_execution_cost_on_fastest_instance = 0;
            for (int i = 0; i < workflow.getJobList().size(); i++) {

                    sum_execution_cost_on_fastest_instance += workflow.getJobList().get(i).getExeTime()[id_fastest_instance];

            }

            int consumed_hours_for_running_task_on_fastest_instance = (int)(sum_execution_cost_on_fastest_instance/3600) + 1;

            if(consumed_hours_for_running_task_on_fastest_instance*cost_fastest_instance > Config.global.budget)
            {
                number_of_affordable_fastest_instance /= 2;
                Log.logger.info("-------------------Some instances take longer than one hour:"+number_of_affordable_fastest_instance+" needs "+consumed_hours_for_running_task_on_fastest_instance+"hour(s)");
            }

            //number_of_affordable_fastest_instance -= consumed_hours_for_running_task_on_fastest_instance;
            //cost_of_additional_hours +=  cost_fastest_instance * consumed_hours_for_running_task_on_fastest_instance;

            //  Log.logger.info("cost_of_additional hours is:"+cost_of_additional_hours);

         //   number_of_affordable_fastest_instance = (int) ((Config.global.budget - cost_of_additional_hours) / cost_fastest_instance);

*/

            Log.logger.info("Number of affordable fastest instances is:"+number_of_affordable_fastest_instance);

            int totalInstances3[] = greedyResourceProvisioning(instanceInfo, number_of_affordable_fastest_instance, minPrice, cost_fastest_instance);

            Workflow heftWorkflow3 = PreProcessor.doPreProcessingForHEFT(PopulateWorkflow.populateWorkflowWithId(Config.global.budget, 0, Config.global.workflow_id), Config.global.bandwidth, totalInstances3, instanceInfo);

            heftWorkflow3.setBeta(Beta.computeBetaValue(heftWorkflow3, instanceInfo, Config.global.m_number));

            HEFTAlgorithm heftAlgorithm3 = new HEFTAlgorithm(heftWorkflow3, instanceInfo, totalInstances3, Config.global.m_number);
            Solution heftSolution3 = heftAlgorithm3.modified_heft_runAlgorithm();
            //todo: here you can test the modified heft algorithm
            modifiedHeftMNumber = heftSolution3.maxNumberOfInstances;

            initialSolutionList.add(heftSolution3);
            Printer.printSolutionWithouthTime(heftSolution3, instanceInfo);

        }

        long runTimeSum = 0;

        for (int i = 0; i < Config.pacsa_algorithm.getNumber_of_runs(); i++) {
            Printer.printSplitter();
            Log.logger.info("<<<<<<<<<<<    NEW RUN "+ i +"     >>>>>>>>>>>\n");

            int computedMNumberBiased = Calculating_M_number_For_PACSA_Plus_With_Biase(minPrice,instanceInfo);

            if (modifiedHeftMNumber != -1){
                if (computedMNumberBiased < modifiedHeftMNumber){
                    Config.global.m_number = computedMNumberBiased;
                }else {
                    Config.global.m_number = modifiedHeftMNumber;
                }
            }else {
                Config.global.m_number = Calculating_M_number_For_PACSA_Plus_With_Biase(minPrice,instanceInfo);//Calculating_M_number_For_PACSA_Plus(minPrice,instanceInfo);
            }

            Log.logger.info("Maximum number of instances (m_number): " + Config.global.m_number + " Number of different types of instances: " + InstanceType.values().length + " Number of tasks: "+ workflow.getJobList().size());

            Config.sa_algorithm.cooling_factor = originalCoolingFactor_SA;
            Config.sa_algorithm.start_temperature = originalStartTemperature_SA;
            if (Config.pacsa_algorithm.iteration_number_based){                                ///////1.0/(10.0*workflow.getJobList().size())
                optimizationAlgorithm = new PACSAIterationNumber(initialSolutionList, 1.0/(10.0*(double)heftSolution.makespan), workflow, instanceInfo, Config.global.m_number);
            }else {
                optimizationAlgorithm = new PACSAOptimization(initialSolutionList ,(1.0/4.0*(double)heftSolution.makespan),workflow, instanceInfo, Config.global.m_number);
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

    public static int Max(int a,int b)
    {
        if(a>b)
            return a;
        else
            return b;
    }

    public static int Min(int a,int b)
    {
        if(a<b)
            return a;
        else
            return b;
    }

    public static int Max(int a,int b,int c)
    {
        if(a>=b && a>=c)
            return a;
        else if (b>=a && b>=c)
            return b;
        else
            return c;
    }

    public static int Calculating_M_number_For_PACSA_Plus(double minInstancePrice,InstanceInfo instanceInfo[])
    {



        int totalInstances2[] = new int[0];
        double remainingBudget = Config.global.budget;


        while (minInstancePrice <= remainingBudget && totalInstances2.length < Config.global.m_number){
            double maxValidCost = 0.0;
            int instanceTypeId = -2;

            Random r = new Random();
            int randomType;

            randomType = r.nextInt(InstanceType.values().length);
            while (instanceInfo[randomType].getSpotPrice() > remainingBudget)
            {
                randomType = r.nextInt(InstanceType.values().length);
            }
            maxValidCost = instanceInfo[randomType].getSpotPrice();
            instanceTypeId = randomType;


            int newTotalInstance[] = new int[totalInstances2.length + 1];
            for (int i = 0; i < totalInstances2.length; i++) {
                newTotalInstance[i] = totalInstances2[i];
            }

            newTotalInstance[totalInstances2.length] = instanceTypeId;
            totalInstances2 = newTotalInstance;

            remainingBudget -= maxValidCost;


            DecimalFormat df = new DecimalFormat ("#.#####");
            remainingBudget = Double.parseDouble(df.format(remainingBudget));


        }

        return totalInstances2.length;

      /*  Workflow heftWorkflow2 = PreProcessor.doPreProcessingForHEFT(PopulateWorkflow.populateWorkflowWithId(Config.global.budget, 0, Config.global.workflow_id), Config.global.bandwidth, totalInstances2, instanceInfo);

        heftWorkflow2.setBeta(Beta.computeBetaValue(heftWorkflow2, instanceInfo, Config.global.m_number));

        HEFTAlgorithm heftAlgorithm2 = new HEFTAlgorithm(heftWorkflow2, instanceInfo, totalInstances2, Config.global.m_number);
        Solution heftSolution2 = heftAlgorithm2.runAlgorithm();
        heftSolution2.heftFitness();

        Integer zArray2[] = new Integer[orderedJobList.size()];
        for (int i = 0; i < orderedJobList.size(); i++) {
            zArray2[i] = orderedJobList.get(i).getIntId();
        }

        heftSolution2.zArray = zArray2;

        //     initialSolutionList.add(heftSolution2);
        Printer.printSolutionWithouthTime(heftSolution2,instanceInfo);*/

    }

    public static double get_price_per_unit(int InstanceId, InstanceInfo instanceInfo[])
    {
        //   double test1 = instanceInfo[InstanceId].getType().getEcu();
        //   double test2 = instanceInfo[InstanceId].getSpotPrice();
        return instanceInfo[InstanceId].getType().getEcu()/instanceInfo[InstanceId].getSpotPrice() ;
    }

    public static int get_next_baised_instance_id(InstanceInfo instanceInfo[])
    {
        double sum = 0;
        for (int id=0; id<instanceInfo.length;id++) {

            //       double test = get_price_per_unit(id,instanceInfo);
            sum += get_price_per_unit(id,instanceInfo)+instanceInfo[id].getType().getEcu(); //= instanceInfo[].getType().getEc2units()/instanceInfo[type.getId()].getSpotPrice() ;

        }

        Random rand = new Random();
        double randomY = rand.nextDouble();
        double probabilitySumTemp = 0;
        int selectedInstance = -1;

        for (int i = instanceInfo.length - 1; i >=0 ; i--) {
            //   Log.logger.info("Probability of selecting "+i+" instanceType is"+(get_price_per_unit(i, instanceInfo)+instanceInfo[i].getType().getEcu())/ sum);
            probabilitySumTemp += (get_price_per_unit(i, instanceInfo)+instanceInfo[i].getType().getEcu())/ sum;
            if (probabilitySumTemp > randomY) {
                selectedInstance = i;
                break;
            }
        }

        //    Log.logger.info("selectedInstance:"+selectedInstance);
        return selectedInstance;
    }

    public static int Calculating_M_number_For_PACSA_Plus_With_Biase(double minInstancePrice,InstanceInfo instanceInfo[])
    {

        int totalInstances2[] = new int[0];
        double remainingBudget = Config.global.budget;


        while (minInstancePrice <= remainingBudget && totalInstances2.length < Config.global.m_number){
            double maxValidCost = 0.0;
            int instanceTypeId = -2;

            Random r = new Random();
            int randomType;

            randomType = get_next_baised_instance_id(instanceInfo);//r.nextInt(InstanceType.values().length);
            while (instanceInfo[randomType].getSpotPrice() > remainingBudget)
            {
                randomType = get_next_baised_instance_id(instanceInfo); //r.nextInt(InstanceType.values().length);
            }
            maxValidCost = instanceInfo[randomType].getSpotPrice();
            instanceTypeId = randomType;


            int newTotalInstance[] = new int[totalInstances2.length + 1];
            for (int i = 0; i < totalInstances2.length; i++) {
                newTotalInstance[i] = totalInstances2[i];
            }

            newTotalInstance[totalInstances2.length] = instanceTypeId;
            totalInstances2 = newTotalInstance;

            remainingBudget -= maxValidCost;


            DecimalFormat df = new DecimalFormat ("#.#####");
            remainingBudget = Double.parseDouble(df.format(remainingBudget));


        }


     /*   Workflow heftWorkflow2 = PreProcessor.doPreProcessingForHEFT(PopulateWorkflow.populateWorkflowWithId(Config.global.budget, 0, Config.global.workflow_id), Config.global.bandwidth, totalInstances2, instanceInfo);

        heftWorkflow2.setBeta(Beta.computeBetaValue(heftWorkflow2, instanceInfo, Config.global.m_number));

        HEFTAlgorithm heftAlgorithm2 = new HEFTAlgorithm(heftWorkflow2, instanceInfo, totalInstances2, Config.global.m_number);
        Solution heftSolution2 = heftAlgorithm2.runAlgorithm();
        heftSolution2.heftFitness();


        List<Job> orderedJobList = GlobalAccess.orderedJobList;
        Integer zArray2[] = new Integer[orderedJobList.size()];
        for (int i = 0; i < orderedJobList.size(); i++) {
            zArray2[i] = orderedJobList.get(i).getIntId();
        }

        heftSolution2.zArray = zArray2;

        //     initialSolutionList.add(heftSolution2);
        Printer.printSolutionWithouthTime(heftSolution2,instanceInfo);*/

        return totalInstances2.length;

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

    /**
     * Greedy Resource Provisioning Algorithm (GRP)
     * */
    static int[] greedyResourceProvisioning(InstanceInfo instanceInfo[], int number_of_affordable_fastest_instance, double minPrice, double cost_fastest_instance){
        int totalInstances[] = HEFTAlgorithm.getTotalInstancesForHEFTMostPowerful(Min(number_of_affordable_fastest_instance,Config.global.m_number));

        double remainingBudget = Config.global.budget - ((number_of_affordable_fastest_instance) * cost_fastest_instance);

        while (minPrice <= remainingBudget && totalInstances.length < Config.global.m_number) {
            //    Log.logger.info("TotalInstances Length is:"+totalInstances3.length);
            double maxValidCost = 0.0;
            int instanceTypeId = -2;
            for (int instance_id = instanceInfo.length-1; instance_id >=0; instance_id--) {//for (int instance_id: sorted_instanceTypes_based_on_Cost_per_ComputeUnit) {
                if (instanceInfo[instance_id].getSpotPrice() <= remainingBudget && instanceInfo[instance_id].getSpotPrice() >= maxValidCost) {
                    maxValidCost = instanceInfo[instance_id].getSpotPrice();
                    instanceTypeId = instance_id;
                    break;
                }
            }

            int newTotalInstance[] = new int[totalInstances.length + 1];
            for (int i = 0; i < totalInstances.length; i++) {
                newTotalInstance[i] = totalInstances[i];
            }

            newTotalInstance[totalInstances.length] = instanceTypeId;
            totalInstances = newTotalInstance;

            remainingBudget -= maxValidCost;
        }

        return totalInstances;
    }
}
