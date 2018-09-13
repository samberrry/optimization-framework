package org.optframework;

import com.rits.cloning.Cloner;
import org.cloudbus.spotsim.enums.AZ;
import org.cloudbus.spotsim.enums.InstanceType;
import org.cloudbus.spotsim.enums.OS;
import org.cloudbus.spotsim.enums.Region;
import org.optframework.config.Config;
import org.optframework.core.*;
import org.optframework.core.hbmo.HBMOAlgorithm;
import org.optframework.core.heft.HEFTAlgorithm;
import org.optframework.core.utils.PopulateWorkflow;
import org.optframework.core.utils.PreProcessor;
import org.optframework.core.utils.Printer;

public class RunHEFTBeforeHBMO {
    public static final int M_NUMBER = Config.global.m_number;

    public static void runHEFTBeforeHBMO(){

        /**
         * Assumptions:
         * Region: europe
         * Availability Zone: A
         * OS type: Linux System
         * */
        InstanceInfo instanceInfo[] = InstanceInfo.populateInstancePrices(Region.EUROPE , AZ.A, OS.LINUX);

        int minECUId = -1;
        double minECU = 9999999999999.9;

        for (InstanceType type : InstanceType.values()){
            if (type.getEcu() < minECU){
                minECUId = type.getId();
                minECU = type.getEcu();
            }
        }

        /**
         * Initializes available instances for the HEFT algorithm with the max number of instances and sets them to the most powerful instance type (that is 6)
         * */
        int totalInstances[] = new int[M_NUMBER];
        for (int i = 0; i < M_NUMBER; i++) {
            totalInstances[i] = minECUId;
        }

//        int totalInstances[] = new int[Config.global.m_number];
//        for (int i = 0; i < Config.global.m_number; i++) {
//            totalInstances[i] = 6;
//        }

        Log.logger.info("<<<<<<<<<<  HEFT Algorithm is started  >>>>>>>>>>>");

        Workflow workflow = PreProcessor.doPreProcessingForHEFT(PopulateWorkflow.populateWorkflowWithId(Config.global.budget, 0, Config.global.workflow_id), Config.global.bandwidth, totalInstances, instanceInfo);

        workflow.setBeta(Beta.computeBetaValue(workflow, instanceInfo, M_NUMBER));

        HEFTAlgorithm heftAlgorithm = new HEFTAlgorithm(workflow, instanceInfo, totalInstances);

        Solution heftSolution = heftAlgorithm.runAlgorithm();

        /**
         * Extension to the heft before hbmo algorithm
         * */
        Cloner cloner = new Cloner();
        for (int i = 0; i < heftSolution.numberOfUsedInstances; i++) {
            for (InstanceType type : InstanceType.values()){
                Solution temp = cloner.deepClone(heftSolution);
                temp.yArray[i] = type.getId();
                temp.fitness();
                if (temp.fitnessValue < heftSolution.fitnessValue && temp.cost <= Config.global.budget){
                    heftSolution = temp;
                }
            }
        }

        Log.logger.info("Optimized Initial Solution makespan"+ heftSolution.makespan+ " cost: "+ heftSolution.cost);

        Log.logger.info("<<<<<<<<<<  HBMO Algorithm is started  >>>>>>>>>>>");

        Workflow hbmoWorkflow = PreProcessor.doPreProcessing(PopulateWorkflow.populateWorkflowWithId(Config.global.budget, 0, Config.global.workflow_id));

        hbmoWorkflow.setBeta(Beta.computeBetaValue(hbmoWorkflow, instanceInfo, M_NUMBER));

        HBMOAlgorithm hbmoAlgorithm = new HBMOAlgorithm(true, hbmoWorkflow, instanceInfo, Config.honeybee_algorithm.getGeneration_number());

        hbmoAlgorithm.initialSolution = heftSolution;

        double fitnessValueList[] = new double[Config.honeybee_algorithm.getNumber_of_runs()];
        double costValueList[] = new double[Config.honeybee_algorithm.getNumber_of_runs()];

        for (int i = 0; i < Config.honeybee_algorithm.getNumber_of_runs(); i++) {
            Printer.printSplitter();
            Log.logger.info("<<<<<<<<<<<    NEW RUN "+ i +"     >>>>>>>>>>>\n");

            long start = System.currentTimeMillis();

            Solution solution = hbmoAlgorithm.runAlgorithm();

            fitnessValueList[i] = solution.fitnessValue;
            costValueList[i] = solution.cost;

            long stop = System.currentTimeMillis();

            Printer.lightPrintSolution(solution,stop-start);
        }

        double sum = 0.0, costSum = 0.0;
        double max = 0.0, costMax = 0.0;
        double min = 999999999999.9, costMin = 99999999.9;
        for (double value : fitnessValueList){
            sum += value;
            if (value > max){
                max = value;
            }
            if (value < min){
                min = value;
            }
        }

        for (double value : costValueList){
            costSum += value;
            if (value > costMax){
                costMax = value;
            }
            if (value < costMin){
                costMin = value;
            }
        }

        Printer.printSplitter();
        Log.logger.info("Average fitness value: " + sum / Config.honeybee_algorithm.getNumber_of_runs());

        Log.logger.info("Average Cost value: " + costSum / Config.honeybee_algorithm.getNumber_of_runs());

        Log.logger.info("Max fitness: " + max + " Min fitness: "+ min);
        Printer.printHoneBeeInfo();
    }
}
