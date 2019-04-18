package org.optframework.core.grpheft;

import com.rits.cloning.Cloner;
import org.optframework.GlobalAccess;
import org.optframework.RunGRPHEFTAlgorithm;
import org.optframework.config.Config;
import org.optframework.core.*;
import org.optframework.core.heft.HEFTAlgorithm;
import org.optframework.core.utils.PopulateWorkflow;
import org.optframework.core.utils.PreProcessor;
import java.util.Collections;
import java.util.List;

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

        List<Job> orderedJobList = GlobalAccess.orderedJobList;

        double minPrice = 9999999999.0;

        for (InstanceInfo item : instanceInfo){
            if (item.getSpotPrice() < minPrice){
                minPrice = item.getSpotPrice();
            }
        }

        Config.global.m_number = GlobalAccess.maxLevel;

        if (Config.global.read_m_number_from_config){
            Config.global.m_number = originalMNumber;
        }

        workflow.setBeta(Beta.computeBetaValue(workflow, instanceInfo, Config.global.m_number));

        Integer zArray[] = new Integer[orderedJobList.size()];
        for (int i = 0; i < orderedJobList.size(); i++) {
            zArray[i] = orderedJobList.get(i).getIntId();
        }

        /**
         * HEFT algorithm which is limited by m_number
         * */

        int id_fastest_instance = findFastestInstanceId(instanceInfo);
        //next time the same instance type MUST not be chosen
        RunGRPHEFTAlgorithm.thisTypeIsUsedAsMaxEfficient[id_fastest_instance] = true;
        double cost_fastest_instance = -11111111111111111111.0;
        for (InstanceInfo item : instanceInfo){
            if (item.getType().getId() == id_fastest_instance){
                cost_fastest_instance = item.getSpotPrice();
                break;
            }
        }

        int number_of_affordable_fastest_instance = (int)((Config.global.budget)/cost_fastest_instance);

        Log.logger.info("Number of affordable fastest instances is:"+number_of_affordable_fastest_instance);

        int totalInstances[] = greedyResourceProvisioning(instanceInfo, number_of_affordable_fastest_instance, minPrice, cost_fastest_instance);

        Workflow heftWorkflow = PreProcessor.doPreProcessingForHEFT(PopulateWorkflow.populateWorkflowWithId(Config.global.budget, 0, Config.global.workflow_id), Config.global.bandwidth, totalInstances, instanceInfo);

        heftWorkflow.setBeta(Beta.computeBetaValue(heftWorkflow, instanceInfo, Config.global.m_number));

        HEFTAlgorithm grpHeftAlgorithm = new HEFTAlgorithm(heftWorkflow, instanceInfo, totalInstances, Config.global.m_number);
        Solution grpHeftSolution = grpHeftAlgorithm.modified_heft_runAlgorithm();

        return grpHeftSolution;
    }

    public static int Min(int a,int b)
    {
        if(a<b)
            return a;
        else
            return b;
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
            double maxValidCost = 0.0;
            int instanceTypeId = -2;
            for (int instance_id = instanceInfo.length-1; instance_id >=0; instance_id--) {
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
