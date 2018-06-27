package org.optframework.core;

import com.rits.cloning.Cloner;
import org.cloudbus.cloudsim.util.workload.WorkflowDAG;
import org.cloudbus.spotsim.enums.InstanceType;
import org.optframework.config.Config;
import org.optframework.core.heft.Gap;
import org.optframework.core.heft.Instance;

import java.util.*;

/**
 * This class is the Solution representation of the problem
 *
 * @author Hessam - hessam.modaberi@gmail.com
 * @since 2018
 *
 * */
public class Solution {
    int id;
    /**
     * Cost of the solution
     * */
    public double cost = -1D;
    /**
     *  Integer array X is used to represent the assignment of tasks to instances.
     * The value of the ith element of this array specifies the index of instance to which this task is assigned.`
     * The length of X is equal to the number of tasks in the workflow, and it is equal to n
     */
    public int xArray[];

    /**
     * Represents the type of instances used in assignment X where only spot-instances are employed to run the workflow
     */
    public int yArray[];

    /**
     * Y prime represents the type of instances used in assignment X where only on-demand instances are utilized to run the workflow
     */
    public int yPrimeArray[];

    /**
     * M, is the total elapsed time required to execute the entire workflow when only SIs are employed
     */
    public int makespan;

    public short instanceUsages[];

    public double fitnessValue;

    public double beta;

    public int numberOfUsedInstances;

    public int maxNumberOfInstances;

    public int visited;

    public double instanceTimes[];

    public double instanceStartTime[];

    public double instanceTimelines[];

    public Workflow workflow;

    List<Job> orderedJobList;

    List<Job> originalJobList;

    public InstanceInfo instanceInfo[];

    /**
     * M prime, is the worst case makespan of the given workflow happening when all the spot-instances fail and we switch all of them to the on-demand instances
     */
    public int makespanPrime;

    public static final int M_NUMBER = Config.global.m_number;

    public Solution(Workflow workflow, InstanceInfo[] instanceInfo, int numberOfInstances) {
        this.workflow = workflow;
        this.instanceInfo = instanceInfo;
        xArray = new int[workflow.getJobList().size()];
        yArray = new int[numberOfInstances];
        yPrimeArray = new int[numberOfInstances];
        instanceUsages = new short[numberOfInstances];
        maxNumberOfInstances = numberOfInstances;
        beta = workflow.getBeta();
    }

    public void generateRandomNeighborSolution(Workflow workflow){
        this.workflow = workflow;

        Random r = new Random();
        int xOry = r.nextInt(2);

        switch (xOry){
                //changes x array
            case 0:
                int randomTask = r.nextInt(xArray.length);
                int currentInstanceId = xArray[randomTask];

                boolean isEqual = true;
                int randomInstanceId = -1;

                while (isEqual){
                    randomInstanceId = r.nextInt(numberOfUsedInstances+1);
                    if (randomInstanceId != currentInstanceId && randomInstanceId < M_NUMBER){
                        isEqual = false;
                    }
                }

                xArray[randomTask] = randomInstanceId;

                //if new instance is selected
                if (randomInstanceId == numberOfUsedInstances){
                    int randomType = r.nextInt(InstanceType.values().length);
                    yArray[randomInstanceId] = randomType;
                    numberOfUsedInstances++;
                }

                break;
                // changes y array
            case 1:
                int randomInstanceIdY = r.nextInt(numberOfUsedInstances);
                boolean isEqualY = true;

                int randomType = -1;

                while (isEqualY){
                    int instanceType = yArray[randomInstanceIdY];
                    randomType = r.nextInt(InstanceType.values().length);

                    if (randomType != instanceType){
                        isEqualY = false;
                    }
                }
                yArray[randomInstanceIdY] = randomType;

                break;
        }

        solutionMapping();
    }

    /**
     * This method does the mapping of new x Array with the help of two arrays, map and mapper
     * This method also useful after recombination of the solutions (crossover)
     * */
    public void solutionMapping(){
        boolean map[] = new boolean[numberOfUsedInstances];
        int realNumberOfInstances = 0;

        for (int i = 0; i < xArray.length; i++) {
            if (map[xArray[i]] == false){
                map[xArray[i]] = true;
                realNumberOfInstances++;
            }
            instanceUsages[xArray[i]]++;
        }

        if (realNumberOfInstances != numberOfUsedInstances){
            int mapper[] = new int[numberOfUsedInstances];

            int newYArray[] = new int[M_NUMBER];

            int instanceCounter = 0;
            for (int i = 0; i < numberOfUsedInstances; i++) {
                if (map[i]){
                    mapper[i] = instanceCounter;
                    newYArray[instanceCounter] = yArray[i];

                    instanceCounter++;
                }
            }

            int newXArray[] = new int[xArray.length];
            for (int i = 0; i < xArray.length; i++) {
                newXArray[i] = mapper[xArray[i]];
            }

            this.numberOfUsedInstances = realNumberOfInstances;
            this.xArray = newXArray;
            this.yArray= newYArray;
            settingInstanceUsageArray();
        }
    }

    void settingInstanceUsageArray(){
        for (int i = 0; i < xArray.length; i++) {
            instanceUsages[xArray[i]]++;
        }
    }

    public void generateRandomSolution(Workflow workflow){
        List<Job> jobList = workflow.getJobList();

        /**
         * Generates random xArray
         * */
        Random r = new Random();
        int bound = 0;

//        It always assigns task 0 (first task) to instance 0 (first instance)
        xArray[jobList.get(0).getIntId()] = bound;

        bound++;
        for (int i = 1; i < jobList.size(); i++) {
            Job job = jobList.get(i);
            int random = r.nextInt(bound + 1);

            xArray[job.getIntId()] = random;

            if (bound == random && bound < M_NUMBER){
                bound++;
            }
        }

        /**
         * Generate random yArray
         * */
        numberOfUsedInstances = bound;

        for (int i = 0; i < numberOfUsedInstances; i++) {
            int random = r.nextInt(InstanceType.values().length);
            yArray[i] = random;
        }
    }

    /**
     * The fitness function for this problem computes the makespan of the workflow with a fixed number of instances
     * */
    public void fitness(){
        if (workflow == null || instanceInfo == null){
            Log.logger.warning("Problem with fitness function properties");
            return;
        }
        Cloner cloner = new Cloner();

        originalJobList = workflow.getJobList();

        orderedJobList = cloner.deepClone(originalJobList);

        Collections.sort(orderedJobList, Job.rankComparator);
        WorkflowDAG dag = workflow.getWfDAG();

        double instanceTimeLine[] = new double[numberOfUsedInstances];
        double instanceStartTime[] = new double[numberOfUsedInstances];
        boolean instanceIsUsed[] = new boolean[numberOfUsedInstances];

        instanceTimes = new double[numberOfUsedInstances];

        Instance instanceList[] = new Instance[numberOfUsedInstances];
        for (int i = 0; i < numberOfUsedInstances; i++) {
            instanceList[i] = new Instance();
        }

        Job firstJob = orderedJobList.get(0);
        Job originalVersion = originalJobList.get(firstJob.getIntId());

        double exeTime = firstJob.getExeTime()[yArray[xArray[firstJob.getIntId()]]];

        instanceTimeLine[xArray[firstJob.getIntId()]] = exeTime;
        instanceIsUsed[xArray[firstJob.getIntId()]] = true;
        instanceStartTime[xArray[firstJob.getIntId()]] = 0;
        originalVersion.setFinishTime(instanceTimeLine[xArray[firstJob.getIntId()]]);

        //for the rest of tasks
        for (int i = 1; i < orderedJobList.size(); i++) {
            Job job = orderedJobList.get(i);
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

            if (parentJobs.size() != 0){
                maxParentId = getJobWithMaxParentFinishTime(parentJobs);
                maxParentJob = originalJobList.get(maxParentId);
                latestParentFinishTime = maxParentJob.getFinishTime();
            }

            if (instanceList[xArray[i]].gapList.size() > 0){
                Collections.sort(instanceList[xArray[i]].gapList , Gap.gapComparator);
            }

            //this if for gap usage
            if (instanceList[xArray[i]].gapList.size() >= 1 && parentJobs.size() != 0){
                int k =0;
                for (Gap gap: instanceList[xArray[i]].gapList){
                    if (latestParentFinishTime < gap.endTime){
                        double tempEdge = originalJobList.get(maxParentId).getEdge(job.getIntId());
                        double tempCIJ = tempEdge / (double)Config.global.bandwidth;

                        double taskExeTime = job.getExeTime()[yArray[xArray[i]]] + tempCIJ;

                        if (gap.duration >= taskExeTime){
                            double gapTaskFinishTime = gap.startTime + taskExeTime;

                            if (gapTaskFinishTime < tempTaskFinishTime){
                                tempTaskExeTime = taskExeTime;
                                tempTaskFinishTime = gap.startTime + taskExeTime;
                                gapIsUsed = true;
                                instanceGapId = xArray[i];
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
                double taskExeTime = job.getExeTime()[yArray[xArray[i]]];
                double currentFinishTime = instanceTimeLine[xArray[i]] + taskExeTime;

                if (currentFinishTime < tempTaskFinishTime){
                    tempTaskExeTime = taskExeTime;
                    tempTaskFinishTime = currentFinishTime;
                    gapIsUsed = false;
                }
            }else {
                //check minimum task finish time for all of the current instances
                maxParentId = getJobWithMaxParentFinishTime(parentJobs);

                double waitingTime = originalJobList.get(maxParentId).getFinishTime() - instanceTimeLine[xArray[i]];

                if (waitingTime > 0 ){
                    double currentTime = instanceTimeLine[xArray[i]] + waitingTime;
                    double edge = originalJobList.get(maxParentId).getEdge(job.getIntId());
                    double cij = edge / (double)Config.global.bandwidth;
                    double taskExeTime = job.getExeTime()[yArray[xArray[i]]];

                    double currentFinishTime = currentTime + cij + taskExeTime;

                    if (currentFinishTime < tempTaskFinishTime){
                        tempTaskExeTime = taskExeTime + cij;
                        gapOccurred = true;
                        endOfInstanceWaitTime = currentTime;
                        tempTaskFinishTime = currentFinishTime;
                        gapIsUsed = false;
                    }
                }else {
                    double edge = originalJobList.get(maxParentId).getEdge(job.getIntId());
                    double cij = edge / (double)Config.global.bandwidth;
                    double taskExeTime = job.getExeTime()[yArray[xArray[i]]];

                    double currentFinishTime = instanceTimeLine[xArray[i]] + cij + taskExeTime;

                    if (currentFinishTime < tempTaskFinishTime){
                        tempTaskExeTime = taskExeTime + cij;
                        tempTaskFinishTime = currentFinishTime;
                        gapIsUsed = false;
                    }
                }
            }
/////////////////////////**********************************************************
            if (gapOccurred){
                instanceList[xArray[i]].hasGap = true;
                Gap gap = new Gap(instanceTimeLine[xArray[i]], endOfInstanceWaitTime);
                instanceList[xArray[i]].gapList.add(gap);
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
            }
         ///////////////////***********************************************************
            if (!instanceIsUsed[xArray[i]]){
                instanceStartTime[xArray[i]] = tempTaskFinishTime - tempTaskExeTime;
                instanceIsUsed[xArray[i]] = true;
            }
            instanceTimeLine[xArray[i]] = tempTaskFinishTime;
            originalJobList.get(job.getIntId()).setFinishTime(tempTaskFinishTime);
        }

/////////////////////////////////////////////////////////////////////////
        for (int i = 0; i < instanceTimes.length; i++) {
            instanceTimes[i] = instanceTimeLine[i] - instanceStartTime[i];
        }

        double totalCost = 0D;
//       Now we have exe time for each instance
        for (int i = 0; i < instanceTimes.length; i++) {
            totalCost += (instanceTimes[i]/3600D) * instanceInfo[this.yArray[i]].spotPrice;
        }

        this.instanceTimelines = instanceTimeLine;
        this.instanceStartTime = instanceStartTime;
        this.cost = totalCost;
        this.makespan = (int)findMaxInstanceTime(instanceTimeLine);

        computeFitnessValue();
    }

    int getJobWithMaxParentFinishTime(ArrayList<Integer> parentJobs){
        double tempValue = originalJobList.get(parentJobs.get(0)).getFinishTime();
        int tempId = originalJobList.get(parentJobs.get(0)).getIntId();

        for (int parentId : parentJobs){
            if (tempValue < originalJobList.get(parentId).getFinishTime()){
                tempId = originalJobList.get(parentId).getIntId();
            }
        }
        return tempId;
    }

    void computeFitnessValue(){
        double delta = cost - workflow.getBudget();
        double penalty1 = 0;

        if (delta > 0){
            penalty1 = delta;
        }

        fitnessValue = makespan + beta * (penalty1) - saving();
    }

    double saving(){
        double e = 1 / workflow.getBudget();
        if (cost < workflow.getBudget()){
            return e * (workflow.getBudget() - cost);
        }else {
            return 0.0;
        }
    }

    double findMaxInstanceTime(double instanceTimes[]){
        double max = instanceTimes[0];

        for (double temp : instanceTimes){
            if (temp > max)
                max = temp;
        }
        return max;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Solution solution = (Solution) o;
        return Double.compare(solution.fitnessValue, fitnessValue) == 0;
    }

    @Override
    public int hashCode() {

        return Objects.hash(fitnessValue);
    }
}
