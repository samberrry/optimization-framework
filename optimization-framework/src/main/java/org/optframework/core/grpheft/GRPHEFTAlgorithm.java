package org.optframework.core.grpheft;

import com.rits.cloning.Cloner;
import org.cloudbus.spotsim.enums.InstanceType;
import org.optframework.GlobalAccess;
import org.optframework.config.Config;
import org.optframework.core.*;
import org.optframework.core.heft.HEFTAlgorithm;
import org.optframework.core.lossandgain.Loss2Algorithm;
import org.optframework.core.lossandgain.Loss3Algorithm;
import org.optframework.core.utils.PopulateWorkflow;
import org.optframework.core.utils.PreProcessor;
import org.optframework.core.utils.Printer;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class GRPHEFTAlgorithm implements OptimizationAlgorithm{

    private static int originalMNumber;
    private InstanceInfo instanceInfo[];

    public GRPHEFTAlgorithm(InstanceInfo[] instanceInfo) {
        this.instanceInfo = instanceInfo;
    }

    @Override
    public Solution runAlgorithm()
    {
        originalMNumber = Config.global.m_number;

        Workflow workflow = PreProcessor.doPreProcessing(PopulateWorkflow.populateWorkflowWithId(Config.global.budget, 0, Config.global.workflow_id));

        Cloner cloner = new Cloner();
        GlobalAccess.orderedJobList = cloner.deepClone(workflow.getJobList());
        Collections.sort(GlobalAccess.orderedJobList, Job.rankComparator);

        //todo:...
        Config.global.m_number = workflow.getJobList().size();

        Log.logger.info("<<<<<<<<<<  HEFT Algorithm is started  >>>>>>>>>>>");
        int totalInstances[] = HEFTAlgorithm.getTotalInstancesForHEFT(3, instanceInfo);

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

        /**
         * Compute the maximum number of used instances
         * */
        loss3Solution.solutionMapping();
        loss2Solution.solutionMapping();
        heftSolution.solutionMapping();

        List<Job> orderedJobList = GlobalAccess.orderedJobList;

        double minPrice = 9999999999.0;

        for (InstanceInfo info: instanceInfo){
            if (instanceInfo[info.getType().getId()].getSpotPrice() < minPrice){
                minPrice = instanceInfo[info.getType().getId()].getSpotPrice();
            }
        }

        Config.global.m_number = GlobalAccess.maxLevel;

        if (Config.global.read_m_number_from_config){
            Config.global.m_number = originalMNumber;
        }

        computeCoolingFactorForSA(workflow.getJobList().size());


        workflow.setBeta(Beta.computeBetaValue(workflow, instanceInfo, Config.global.m_number));

        Integer zArray[] = new Integer[orderedJobList.size()];
        for (int i = 0; i < orderedJobList.size(); i++) {
            zArray[i] = orderedJobList.get(i).getIntId();
        }

        loss2Solution.zArray = zArray;
        heftSolution.zArray = zArray;

        loss2Solution.maxNumberOfInstances = Config.global.m_number;
        loss3Solution.maxNumberOfInstances = Config.global.m_number;
        heftSolution.maxNumberOfInstances = Config.global.m_number;

        heftSolution.origin = "heft";
        loss2Solution.origin = "loss2";
        loss3Solution.origin = "loss3";


        /**
         * HEFT algorithm which is limited by m_number
         * */

        int id_fastest_instance = findFastestInstanceId(instanceInfo);
        double cost_fastest_instance = instanceInfo[id_fastest_instance].getSpotPrice();

        int number_of_affordable_fastest_instance = (int)((Config.global.budget)/cost_fastest_instance);

        Log.logger.info("Number of affordable fastest instances is:"+number_of_affordable_fastest_instance);

        int totalInstances3[] = greedyResourceProvisioning(instanceInfo, number_of_affordable_fastest_instance, minPrice, cost_fastest_instance);

        Workflow heftWorkflow3 = PreProcessor.doPreProcessingForHEFT(PopulateWorkflow.populateWorkflowWithId(Config.global.budget, 0, Config.global.workflow_id), Config.global.bandwidth, totalInstances3, instanceInfo);

        heftWorkflow3.setBeta(Beta.computeBetaValue(heftWorkflow3, instanceInfo, Config.global.m_number));

        HEFTAlgorithm grpHeftAlgorithm = new HEFTAlgorithm(heftWorkflow3, instanceInfo, totalInstances3, Config.global.m_number);
        Solution grpHeftSolution = grpHeftAlgorithm.modified_heft_runAlgorithm();

//        Printer.printSolutionWithouthTime(grpHeftSolution, instanceInfo);

        return grpHeftSolution;
    }

    public static int Min(int a,int b)
    {
        if(a<b)
            return a;
        else
            return b;
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
        int totalInstances[];
        switch (Config.global.workflow_id){
            case 30:
            case 31:
            case 32:
            case 33:
            case 34:
            case 35:
                totalInstances = HEFTAlgorithm.getTotalInstancesForHEFTMostPowerful(number_of_affordable_fastest_instance, instanceInfo);
                break;
            default:
                totalInstances = HEFTAlgorithm.getTotalInstancesForHEFTMostPowerful(Min(number_of_affordable_fastest_instance,Config.global.m_number), instanceInfo);
                break;
        }
        Config.global.m_number = totalInstances.length;

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

    public static int findFastestInstanceId(InstanceInfo instanceInfo[]){
        InstanceInfo temp = instanceInfo[0];
        for (InstanceInfo info: instanceInfo){
            if (info.getType().getEcu() > temp.getType().getEcu()){
                temp = info;
            }
        }
        return  temp.getType().getId();
    }
}
