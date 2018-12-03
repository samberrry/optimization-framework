package org.optframework.core.lossandgain;

import com.sun.org.apache.xalan.internal.xsltc.util.IntegerArray;
import org.cloudbus.cloudsim.util.workload.WorkflowDAG;
import org.cloudbus.spotsim.enums.InstanceType;
import org.optframework.GlobalAccess;
import org.optframework.RunSAAlgorithm;
import org.optframework.config.Config;
import org.optframework.core.*;
import org.optframework.core.heft.Gap;
import org.optframework.core.heft.Instance;
import org.optframework.core.pacsa.PACSAIterationNumber;
import org.optframework.core.sa.SimulatedAnnealingAlgorithm;
import org.optframework.core.utils.Printer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class Loss3Algorithm implements OptimizationAlgorithm {

    Solution heftSolution;
    int totalInstances[];
    Workflow workflow;
    InstanceInfo instanceInfo[];

    public Loss3Algorithm(Solution heftSolution, int[] totalInstances, Workflow workflow, InstanceInfo[] instanceInfo) {
        try {
            this.heftSolution = heftSolution.clone();
        } catch (Exception e) {
            Log.logger.info("Clone Exception");
        }
        this.totalInstances = totalInstances;
        this.workflow = workflow;
        this.instanceInfo = instanceInfo;
    }

    @Override
    public Solution runAlgorithm() {
        double matrix[][] = new double[workflow.getJobList().size()][totalInstances.length];

        /**
         * This part is for calculating matrix elements
         * */
        Solution solutionTemp = null;
        Solution solution = null;
        try {
            solution = heftSolution.clone();
        } catch (Exception e) {
            Log.logger.info("Cloning Exception");
        }

        //int newXArray[] = new int[heftSolution.xArray.length];
        boolean furtherImprovement = true;
   /*     while (solution.cost > workflow.getBudget() && furtherImprovement) {

            for (int i = 0; i < workflow.getJobList().size(); i++) {
                for (int j = 0; j < totalInstances.length; j++) {
                    try {
                        solutionTemp = solution.clone();
                    } catch (Exception e) {
                        Log.logger.info("Cloning Exception");
                    }

                    double oldMakespan = solution.makespan;

                    // double tempOld1 = workflow.getJobList().get(i).getExeTime()[solutionTemp.yArray[solutionTemp.xArray[i]]];
                    // double tempOld2 = instanceInfo[solutionTemp.yArray[solution.xArray[i]]].getSpotPrice();
                    // double oldCost = tempOld1 * tempOld2 / 3600.0;
                    double oldCost = solution.cost;

                    solutionTemp.xArray[i] = j;

                    solutionTemp.heftFitness();

                    double newMakespan = solutionTemp.makespan;
                    //  double newCost = solution.cost;
                    double temp1 = workflow.getJobList().get(i).getExeTime()[solutionTemp.yArray[solutionTemp.xArray[i]]];
                    double temp2 = instanceInfo[solutionTemp.yArray[solutionTemp.xArray[i]]].getSpotPrice();
                    double newCost = temp1 * temp2 / 3600.0;

                    double costDiff = oldCost - newCost;
                    double makespanDiff = newMakespan - oldMakespan;

                    if (costDiff <= 0) {
                        matrix[i][j] = 0;
                    } else {
                        matrix[i][j] = (makespanDiff) / (costDiff);
                        if (matrix[i][j] < 0)
                            Log.logger.info("<<<<<<<<<<<    NEW RUN " + i + "     >>>>>>>>>>>\n");
                    }
                }
            }

            // Generating loss solution from the weight matrix


            //int oldInstanceId = heftSolution.xArray[i];
            // int minInstanceId,minTaskId;
            MatrixElement MinElement = FindMinPositiveElement(matrix);
            if (MinElement.j == -1)
                furtherImprovement = false;
            else {
                solution.xArray[MinElement.i] = MinElement.j;
                matrix[MinElement.i][MinElement.j] = 0;
                solution.heftFitness();
            }

        }*/
        int test = 0;

        heftSolution.xArray = solution.xArray;

        heftSolution.heftFitness();

        return heftSolution;
    }

    public Solution runAlgorithm2() {
        double matrix[][] = new double[workflow.getJobList().size()][totalInstances.length];


        // This part is for calculating matrix elements

        Solution solutionTemp = null;
        Solution solution = null;
        try {
            solution = heftSolution.clone();
        } catch (Exception e) {
            Log.logger.info("Cloning Exception");
        }


      /*   if (Config.global.deadline_based) {
             Log.logger.info("Loss 3 shouldn't be invoked when the algorithm is executed in the deadline-based mode!!!!!");
         }

        Config.global.deadline_based = true;
        //   Config.global.deadline =126.0;//2.0 * heftSolution.makespan;

        heftSolution.solutionMapping();
        List<Job> orderedJobList = GlobalAccess.orderedJobList;
        Integer zArray[] = new Integer[orderedJobList.size()];
        for (int i = 0; i < orderedJobList.size(); i++) {
            zArray[i] = orderedJobList.get(i).getIntId();
        }
        heftSolution.zArray = zArray;
        heftSolution.maxNumberOfInstances = Config.global.m_number;

        List<Solution> initialSolutionList = null;
        initialSolutionList = new ArrayList<>();

        initialSolutionList.add(heftSolution);

        Config.global.m_number = GlobalAccess.maxLevel;

        Log.logger.info("Maximum number of instances: " + Config.global.m_number + " Number of different types of instances: " + InstanceType.values().length + " Number of tasks: "+ workflow.getJobList().size());

        workflow.setBeta(Beta.computeBetaValue(workflow, instanceInfo, Config.global.m_number));

        OptimizationAlgorithm optimizationAlgorithm;

        optimizationAlgorithm = new PACSAIterationNumber(initialSolutionList, 1.0/(10.0*(double)heftSolution.makespan), workflow, instanceInfo);
        solution = optimizationAlgorithm.runAlgorithm();
        solution.solutionMapping();

        Config.global.deadline_based = false;
        solution.fitness();*/





        /*    Thread threadList[] = new Thread[Config.pacsa_algorithm.getNumber_of_ants()];
            Solution[] solutionList = new Solution[Config.pacsa_algorithm.getNumber_of_ants()];

            for (int i = 0; i < Config.pacsa_algorithm.getNumber_of_ants(); i++) {
                int itr = i;
                threadList[i] = new Thread(() -> {
                    SimulatedAnnealingAlgorithm sa = new SimulatedAnnealingAlgorithm(heftSolution, workflow, instanceInfo);

                    Solution solution_temp = sa.runAlgorithm();
                    solutionList[itr] = solution_temp;
                });
            }

            for (int i = 0; i < Config.pacsa_algorithm.number_of_ants; i++) {
                threadList[i].start();
            }

            for (int i = 0; i < Config.pacsa_algorithm.number_of_ants; i++) {
                try {
                    threadList[i].join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }


            Config.global.deadline_based = false;

            try {
                solution = solutionList[0].clone();
            }
            catch (Exception e)
            {
                org.optframework.core.Log.logger.info("Cloning Exception");
            }
            solution.fitness();

            String list_ants_fintess = "";

            for (Solution sol: solutionList){
                sol.fitness();
                list_ants_fintess += Double.toString(sol.fitnessValue) +" MS="+sol.makespan+" Cost= "+sol.cost+ ",    ";
                if (sol.fitnessValue < solution.fitnessValue){
                    solution = sol;
                }
            }

            org.cloudbus.cloudsim.Log.logger.info("List of ants':"+list_ants_fintess);
*/

          /*  SimulatedAnnealingAlgorithm sa = new SimulatedAnnealingAlgorithm(heftSolution, workflow, instanceInfo);
            solution = sa.runAlgorithm();

            Config.global.deadline_based = false;
            solution.fitness();*/
        // }



/*        ArrayList<Integer> SetUsedInstances = new ArrayList<>();;
        ArrayList<Integer> AffordableInstances = null;
        ArrayList<Integer> AffordableIdleInstances = null;
        ArrayList<Integer> NoExtraChargeInstances = null;
        double current_budget = workflow.getBudget();
        List<Job> orderedJobList = GlobalAccess.orderedJobList;

        for (int i = 0; i < workflow.getJobList().size(); i++) {

            solution.xArray[i] = -1;
        }



        for (int i = 0; i < orderedJobList.size(); i++) {

            Solution TempSolution =null;
            NoExtraChargeInstances = new ArrayList<>();
            AffordableInstances = new ArrayList<>();
            AffordableIdleInstances = new ArrayList<>();
            int minInstanceforExecutingCurrentTask = -1;
            double minCostForExecutingCurrentTask = 9999999;

            double oldCost, newCost;

            if(i > 0){
                oldCost = TotalCostcalculation(solution,i);
                for(int j=0; j<totalInstances.length;j++){
                    try {
                        TempSolution = solution.clone();
                    }
                    catch (Exception e) {
                        Log.logger.info("Cloning Exception");
                    }

                    TempSolution.xArray[orderedJobList.get(i).getIntId()] = j;
                    newCost = TotalCostcalculation(TempSolution,i+1);
                    if(newCost < minCostForExecutingCurrentTask)
                    {
                        minCostForExecutingCurrentTask = newCost;
                        minInstanceforExecutingCurrentTask = j;
                    }
                    if(newCost == oldCost)
                    {
                        NoExtraChargeInstances.add(j);
                    }
                }
            }
            else{
                oldCost = 0;
            }


            /// we always prefer to use those instances which does not charge us to run the current task
            if(!NoExtraChargeInstances.isEmpty())// means there is at least one free of charge instance to run the current task
            {
                Random r = new Random();
                int SelectedInstance = r.nextInt(NoExtraChargeInstances.size());
                solution.xArray[orderedJobList.get(i).getIntId()] = NoExtraChargeInstances.get(SelectedInstance);
                SetUsedInstances.add(NoExtraChargeInstances.get(SelectedInstance));
            }else { // means there is no free of charge instance to run the current task and we have to select an instance
                for (int j = 0; j < totalInstances.length; j++) {
                 //   boolean alreadyused = false;
                    if (instanceInfo[solution.yArray[j]].getSpotPrice() < current_budget) {
                 //       for (Integer item:SetUsedInstances) {
                   //         if(j == item)
                   //             alreadyused = true;
                    //    }
                    //    if(!alreadyused)
                     //       AffordableIdleInstances.add(j);
                        AffordableInstances.add(j);
                    }
                }
                if (!AffordableInstances.isEmpty()) { // there is at least one affordable instance to run the current task
                    /// we use those affordable instances which are idle (i.e.,never used before)
                    if(!AffordableIdleInstances.isEmpty())
                    {
                        Random r3 = new Random();
                        int SelectedInstance = r3.nextInt(AffordableIdleInstances.size());
                        solution.xArray[orderedJobList.get(i).getIntId()] = AffordableIdleInstances.get(SelectedInstance);
                        SetUsedInstances.add(AffordableIdleInstances.get(SelectedInstance));
                        current_budget -= instanceInfo[solution.yArray[AffordableIdleInstances.get(SelectedInstance)]].getSpotPrice();
                    }
                    else {// we have to select one of the running instances since all affordable ones are running
                        Random r2 = new Random();
                        int SelectedInstance = r2.nextInt(AffordableInstances.size());
                        solution.xArray[orderedJobList.get(i).getIntId()] = AffordableInstances.get(SelectedInstance);
                        SetUsedInstances.add(AffordableInstances.get(SelectedInstance));
                        current_budget -= instanceInfo[solution.yArray[AffordableInstances.get(SelectedInstance)]].getSpotPrice();
                    }
                }
                else{// there is no feasible solution. We strive to minimize the cost violation. We still prefer to use idle instances
                    solution.xArray[orderedJobList.get(i).getIntId()] = minInstanceforExecutingCurrentTask;
                    SetUsedInstances.add(minInstanceforExecutingCurrentTask);
                    current_budget -= instanceInfo[solution.yArray[minInstanceforExecutingCurrentTask]].getSpotPrice();
                }
            }





        }*/


      /*  solution.xArray[0] = 2;
        solution.xArray[1] = 2;
        solution.xArray[2] = 0;
        solution.xArray[3] = 0;

        solution.xArray[4] = 0;
        solution.xArray[5] = 0;
        solution.xArray[6] = 2;
        solution.xArray[7] = 2;

        solution.xArray[8] = 1;
        solution.xArray[9] = 2;
        solution.xArray[10] = 2;
        solution.xArray[11] = 1;

        solution.xArray[12] = 1;
        solution.xArray[13] = 2;
        solution.xArray[14] = 0;
        solution.xArray[15] = 2;

        solution.xArray[16] = 0;
        solution.xArray[17] = 2;
        solution.xArray[18] = 0;
        solution.xArray[19] = 2;

        solution.xArray[20] = 2;
        solution.xArray[21] = 2;
        solution.xArray[22] = 1;
        solution.xArray[23] = 1;

        solution.xArray[24] = 0;
        solution.xArray[25] = 1;
        solution.xArray[26] = 2;
        solution.xArray[27] = 2;

        solution.xArray[28] = 1;
        solution.xArray[29] = 0;

        for (int z = 0; z < workflow.getJobList().size(); z++)
          solution.zArray[z] = z;

        solution.fitness();

        //int newXArray[] = new int[heftSolution.xArray.length];
      /*  boolean furtherImprovement = true;
        while (solution.cost > workflow.getBudget() && furtherImprovement) {

            for (int i = 0; i < workflow.getJobList().size(); i++) {
                for (int j = 0; j < totalInstances.length; j++) {
                    try {
                        solutionTemp = solution.clone();
                    } catch (Exception e) {
                        Log.logger.info("Cloning Exception");
                    }


                    double oldMakespan = solution.makespan;


                    //double tempOld1 = workflow.getJobList().get(i).getExeTime()[solution.yArray[solution.xArray[i]]];
                    //double tempOld2 = instanceInfo[solution.yArray[solution.xArray[i]]].getSpotPrice();
                    //double oldCost = tempOld1 * tempOld2 / 3600.0;
                    double oldCost = solution.cost;

                    solutionTemp.xArray[i] = j;

                    solutionTemp.heftFitness();

                    double newMakespan = solutionTemp.makespan;

                    double newCost = solutionTemp.cost;
                    //double temp1 = workflow.getJobList().get(i).getExeTime()[solutionTemp.yArray[solutionTemp.xArray[i]]];
                    //double temp2 = instanceInfo[solutionTemp.yArray[solutionTemp.xArray[i]]].getSpotPrice();
                    //double newCost = temp1 * temp2 / 3600.0;

                    double costDiff = oldCost - newCost;
                    double makespanDiff = newMakespan - oldMakespan;

                    if (costDiff <= 0) {
                        matrix[i][j] = 0;
                    } else {
                        matrix[i][j] = (makespanDiff) / (costDiff);
                        if (makespanDiff < 0)
                            matrix[i][j] *= -10; // to give a lower priority to better makespan VS better cost
                    }
                }
            }


             // Generating loss solution from the weight matrix


            //int oldInstanceId = heftSolution.xArray[i];
            // int minInstanceId,minTaskId;
            MatrixElement MinElement = FindMinPositiveElement(matrix);
            if (MinElement.j == -1)
                furtherImprovement = false;
            else {
                solution.xArray[MinElement.i] = MinElement.j;
                matrix[MinElement.i][MinElement.j] = 0;
                solution.heftFitness();
            }

        }
        int test = 0;*/

        //  heftSolution.xArray = solution.xArray;
        //  heftSolution.yArray = solution.yArray;
        solution.origin = "Loss3";

        Log.logger.info("Loss3 Solution Fitness is: " + solution.fitnessValue + " Makespan: " + solution.makespan + ", Cost=" + solution.cost);


        return solution;
    }

    public MatrixElement FindMinPositiveElement(double matrix[][]) {

        double minValue = 99999999999.999;
        int minTaskId = -1;
        int minInstanceId = -1;
        for (int i = 0; i < workflow.getJobList().size(); i++) {
            for (int j = 0; j < totalInstances.length; j++)
                if (matrix[i][j] > 0 && matrix[i][j] < minValue) {
                    minValue = matrix[i][j];
                    minInstanceId = j;
                    minTaskId = i;

                }

        }

        MatrixElement NewElement = new MatrixElement(minTaskId,minInstanceId);
        return NewElement;
    }

    public MatrixElement FindMinNonZeroElement(double matrix[][]) {

        double minValue = 99999999999.999;
        int minTaskId = -1;
        int minInstanceId = -1;
        for (int i = 0; i < workflow.getJobList().size(); i++) {
            for (int j = 0; j < totalInstances.length; j++)
                if (matrix[i][j] != 0 && matrix[i][j] < minValue) {
                    minValue = matrix[i][j];
                    minInstanceId = j;
                    minTaskId = i;

                }

        }

        MatrixElement NewElement = new MatrixElement(minTaskId,minInstanceId);
        return NewElement;
    }



    public double TotalCostcalculation(Solution solution, int numberofAssignedTasks){


        List<Job> originalJobList = workflow.getJobList();
        List<Job> orderedJobList = GlobalAccess.orderedJobList.subList(0,numberofAssignedTasks);


        WorkflowDAG dag = workflow.getWfDAG();

        double instanceTimeLine[] = new double[totalInstances.length];
        double instanceStartTime[] = new double[totalInstances.length];
        boolean instanceIsUsed[] = new boolean[totalInstances.length];

        double taskFinishTimes[] = new double[totalInstances.length];
        double instanceTimes[] = new double[totalInstances.length];

        Instance instanceList[] = new Instance[totalInstances.length];
        for (int i = 0; i < totalInstances.length; i++) {
            instanceList[i] = new Instance();
        }

        Job firstJob = orderedJobList.get(0);
        Job originalVersion = originalJobList.get(firstJob.getIntId());

        double exeTime = firstJob.getExeTime()[solution.yArray[solution.xArray[firstJob.getIntId()]]];

        instanceTimeLine[solution.xArray[firstJob.getIntId()]] = exeTime;
        instanceIsUsed[solution.xArray[firstJob.getIntId()]] = true;
        instanceStartTime[solution.xArray[firstJob.getIntId()]] = 0;
        taskFinishTimes[originalVersion.getIntId()] = instanceTimeLine[solution.xArray[firstJob.getIntId()]];

        //for the rest of tasks
        for (int i = 1; i < orderedJobList.size(); i++) {
            Job job = orderedJobList.get(i);
            /**
             * TempTaskFinishTime: is used to choose, benefit from the gap or not. If the temp task
             * finish time for the gap is smaller than regular scheduling, the gap is used!
             * */
            double tempTaskFinishTime = 999999999999999999.0;
            double tempTaskExeTime = -99999999999999999.9;
            boolean gapOccurred = false;
            double endOfInstanceWaitTime = -99999999999999999.9;

            //Info about the gap and the instance
            boolean gapIsUsed = false;
            int instanceGapId = -1;
            int gapId = -1;
            //

            ArrayList<Integer> parentJobs = dag.getParents(job.getIntId());

            //it is possible to have multiple start tasks without dependencies
            int maxParentId = -1;
            Job maxParentJob;
            double latestParentFinishTime = 0.0;

            //this if for gap usage
            if (instanceList[solution.xArray[job.getIntId()]].gapList.size() >= 1 && parentJobs.size() != 0){
                Collections.sort(instanceList[solution.xArray[job.getIntId()]].gapList , Gap.gapComparator);
                maxParentId = getJobWithMaxParentFinishTimeWithCij(solution, parentJobs, job.getIntId(),taskFinishTimes);
                maxParentJob = originalJobList.get(maxParentId);
                latestParentFinishTime = taskFinishTimes[maxParentJob.getIntId()];

                int k =0;
                for (Gap gap: instanceList[solution.xArray[job.getIntId()]].gapList){
                    if (latestParentFinishTime < gap.endTime){
                        double tempEdge = originalJobList.get(maxParentId).getEdge(job.getIntId());
                        double tempCIJ = tempEdge / (double) Config.global.bandwidth;
                        double taskExeTime;

                        if (solution.xArray[job.getIntId()] == solution.xArray[maxParentId]){
                            taskExeTime = job.getExeTime()[solution.yArray[solution.xArray[job.getIntId()]]];
                        }else {
                            taskExeTime = job.getExeTime()[solution.yArray[solution.xArray[job.getIntId()]]] + tempCIJ;
                        }

                        double availableGapTime = gap.endTime - latestParentFinishTime;
                        double gapTest = availableGapTime;
                        if (availableGapTime > gap.duration){
                            gapTest = gap.duration;
                        }

                        if (gapTest >= taskExeTime){
                            double remainingTimeToStartGap = gap.startTime - latestParentFinishTime;
                            double gapTaskFinishTime;

                            if (remainingTimeToStartGap >= 0){
                                double timeToSendData = gap.startTime - latestParentFinishTime;
                                if (timeToSendData >= tempCIJ){
                                    gapTaskFinishTime = gap.startTime + (taskExeTime - tempCIJ);
                                }else {
                                    gapTaskFinishTime = gap.startTime + (taskExeTime - timeToSendData);
                                }
                            }else {
                                gapTaskFinishTime = latestParentFinishTime + taskExeTime;
                            }

                            if (gapTaskFinishTime < tempTaskFinishTime){
                                tempTaskExeTime = taskExeTime;
                                tempTaskFinishTime = gapTaskFinishTime;
                                gapIsUsed = true;
                                gapOccurred = false;
                                instanceGapId = solution.xArray[job.getIntId()];
                                gapId = k;
                            }
                            break;
                        }
                        k++;
                    }else {
                        break;
                    }
                }
            }
            //this is for when there is no gap
            if (parentJobs.size() == 0){
                double taskExeTime = job.getExeTime()[solution.yArray[solution.xArray[job.getIntId()]]];
                double currentFinishTime = instanceTimeLine[solution.xArray[job.getIntId()]] + taskExeTime;

                if (currentFinishTime < tempTaskFinishTime){
                    tempTaskExeTime = taskExeTime;
                    tempTaskFinishTime = currentFinishTime;
                    gapIsUsed = false;
                }
            }else {
                //check maximum task finish time for all of the current instances
                maxParentId = getJobWithMaxParentFinishTimeWithCij(solution,parentJobs, job.getIntId(),taskFinishTimes);

                double waitingTime = taskFinishTimes[maxParentId] - instanceTimeLine[solution.xArray[job.getIntId()]];

                if (waitingTime > 0 ){
                    double currentTime = instanceTimeLine[solution.xArray[job.getIntId()]] + waitingTime;
                    double cij = 0D;
                    double taskExeTime = job.getExeTime()[solution.yArray[solution.xArray[job.getIntId()]]];
                    double currentFinishTime;

                    if (solution.xArray[job.getIntId()] == solution.xArray[maxParentId]){
                        currentFinishTime = currentTime + taskExeTime;
                    }else {
                        /**
                         * Time to send data: is the time between instance timeline and the max
                         * finish time of the parent task.
                         * */
                        double edge = originalJobList.get(maxParentId).getEdge(job.getIntId());
                        cij = edge / (double)Config.global.bandwidth;

                        double timeToSendData = currentTime - taskFinishTimes[maxParentId];

                        if (timeToSendData >= cij){
                            currentFinishTime = currentTime + taskExeTime;
                        }else {
                            currentFinishTime = currentTime + (cij - timeToSendData) + taskExeTime;
                        }
                    }

                    if (currentFinishTime < tempTaskFinishTime){
                        tempTaskExeTime = taskExeTime + cij;
                        gapOccurred = true;
                        endOfInstanceWaitTime = currentTime;
                        tempTaskFinishTime = currentFinishTime;
                        gapIsUsed = false;
                    }
                }else {
                    double cij = 0D;
                    double taskExeTime = job.getExeTime()[solution.yArray[solution.xArray[job.getIntId()]]];
                    double currentFinishTime;

                    if (solution.xArray[job.getIntId()] == solution.xArray[maxParentId]){
                        currentFinishTime = instanceTimeLine[solution.xArray[job.getIntId()]] + taskExeTime;
                    }else {
                        double timeToSendData = instanceTimeLine[solution.xArray[job.getIntId()]] - taskFinishTimes[maxParentId];

                        double edge = originalJobList.get(maxParentId).getEdge(job.getIntId());
                        cij = edge / (double)Config.global.bandwidth;

                        if (timeToSendData >= cij){
                            currentFinishTime = instanceTimeLine[solution.xArray[job.getIntId()]] + taskExeTime;
                        }else {
                            currentFinishTime = instanceTimeLine[solution.xArray[job.getIntId()]] + (cij - timeToSendData) + taskExeTime;
                        }
                    }

                    if (currentFinishTime < tempTaskFinishTime){
                        tempTaskExeTime = taskExeTime + cij;
                        tempTaskFinishTime = currentFinishTime;
                        gapIsUsed = false;
                        gapOccurred = false;
                    }
                }
            }

            if (gapOccurred && instanceIsUsed[solution.xArray[job.getIntId()]]){
                instanceList[solution.xArray[job.getIntId()]].hasGap = true;
                Gap gap = new Gap(instanceTimeLine[solution.xArray[job.getIntId()]], endOfInstanceWaitTime);
                instanceList[solution.xArray[job.getIntId()]].gapList.add(gap);
            }
            if (gapIsUsed){
                Gap gap = instanceList[instanceGapId].getGapList().get(gapId);
                gap.startTime = tempTaskFinishTime;
                if (gap.startTime >= gap.endTime){
                    instanceList[instanceGapId].gapList.remove(gapId);
                    Collections.sort(instanceList[instanceGapId].gapList , Gap.gapComparator);
                }else {
                    gap.duration = gap.endTime - gap.startTime;
                }
            }else {
                instanceTimeLine[solution.xArray[job.getIntId()]] = tempTaskFinishTime;
            }
            if (!instanceIsUsed[solution.xArray[job.getIntId()]]){
                instanceStartTime[solution.xArray[job.getIntId()]] = tempTaskFinishTime - tempTaskExeTime;
                instanceIsUsed[solution.xArray[job.getIntId()]] = true;
            }
            taskFinishTimes[job.getIntId()] = tempTaskFinishTime;
        }

        for (int i = 0; i < instanceTimes.length; i++) {
            instanceTimes[i] = instanceTimeLine[i] - instanceStartTime[i];
        }

        double totalCost = 0D;
//       Now we have exe time for each instance
        for (int i = 0; i < instanceTimes.length; i++) {
            double theHour = instanceTimes[i]/3600D;
            theHour = Math.ceil(theHour);
            totalCost += theHour * instanceInfo[solution.yArray[i]].getSpotPrice();
        }

        //instanceStartTime = instanceStartTime;
        return totalCost;
        //makespan = (int)findMaxInstanceTime(instanceTimeLine);
        //computeFitnessValue();
    }


    int getJobWithMaxParentFinishTimeWithCij(Solution solution, ArrayList<Integer> parentJobs, int jobId, double taskFinishTimes[]){
        List<Job> originalJobList = workflow.getJobList();
        double tempValue = -1;
        int tempId = -1;

        for (int parentId : parentJobs){
            double tempEdge = originalJobList.get(parentId).getEdge(jobId);
            double tempCIJ = tempEdge / (double)Config.global.bandwidth;
            double maxJobStartTime;
            if (solution.xArray[jobId] == solution.xArray[parentId]){
                maxJobStartTime = taskFinishTimes[parentId];
            }else {
                maxJobStartTime = taskFinishTimes[parentId] + tempCIJ;
            }

            if (tempValue < maxJobStartTime){
                tempValue = maxJobStartTime;
                tempId = originalJobList.get(parentId).getIntId();
            }
        }
        return tempId;
    }
}
