package org.optframework.core;

import org.cloudbus.cloudsim.util.workload.WorkflowDAG;
import org.cloudbus.spotsim.enums.InstanceType;
import org.optframework.GlobalAccess;
import org.optframework.config.Config;
import org.optframework.core.heft.Gap;
import org.optframework.core.heft.Instance;

import java.util.*;
import java.text.DecimalFormat;

/**
 * This class is the Solution representation of the problem
 *
 * @author Hessam - hessam.modaberi@gmail.com
 * @since 2018
 *
 * */
public class Solution implements Cloneable{
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
     * Z array is the magic array and is used to specify job order
     * */
    public Integer zArray[];

    /**
     * Y prime represents the type of instances used in assignment X where only on-demand instances are utilized to run the workflow
     */
    public int yPrimeArray[];

    /**
     * M, is the total elapsed time required to execute the entire workflow when only SIs are employed
     */
    public int makespan;

    /**
     * The Origin denotes the algorithm which generated the solution
     * */
    public String origin;

    public short instanceUsages[];

    public double fitnessValue;

    public double beta;

    public int numberOfUsedInstances;

    public int maxNumberOfInstances;

    public double instanceTimes[];

    public double instanceStartTime[];

    public double instanceTimelines[];

    public Workflow workflow;

    public InstanceInfo instanceInfo[];

    double taskFinishTimes[];

    /**
     * M prime, is the worst case makespan of the given workflow happening when all the spot-instances fail and we switch all of them to the on-demand instances
     */
    public int makespanPrime;

    public Solution(Workflow workflow, InstanceInfo[] instanceInfo, int maxNumberOfInstances) {
        this.workflow = workflow;
        this.instanceInfo = instanceInfo;
        this.maxNumberOfInstances = maxNumberOfInstances;
        xArray = new int[workflow.getJobList().size()];
        zArray = new Integer[workflow.getJobList().size()];
        for (int i = 0; i < workflow.getJobList().size(); i++) {
            zArray[i] = new Integer(-1);
        }
        yArray = new int[maxNumberOfInstances];
        for (int i = 0; i < maxNumberOfInstances; i++) {
            yArray[i] = -1;
        }

        yPrimeArray = new int[maxNumberOfInstances];
        instanceUsages = new short[maxNumberOfInstances];
        beta = workflow.getBeta();
    }

    public void generateRandomNeighborSolution(Workflow workflow){
        this.workflow = workflow;

        Random r = new Random();
        int xOry;
        if(maxNumberOfInstances > 1) {
            xOry = r.nextInt(3);
        }
       else {
            xOry = 1 + r.nextInt(2);
        }

        int sizeofneighborhood = 1;
        switch (xOry) {
            //changes x array
            case 0:
                    for (int i = 0; i < sizeofneighborhood; i++) {
                        int randomTask = r.nextInt(xArray.length);
                        int randomInstanceId = r.nextInt(maxNumberOfInstances);

                        xArray[randomTask] = randomInstanceId;

                        //if new instance is selected
                        if (this.yArray[randomInstanceId] == -1) {
                            int randomType = r.nextInt(InstanceType.values().length);
                            yArray[randomInstanceId] = randomType;
                            if (randomInstanceId >= numberOfUsedInstances){
                                numberOfUsedInstances = randomInstanceId + 1;
                            }
                        }else if (randomInstanceId >= numberOfUsedInstances){
                            numberOfUsedInstances = randomInstanceId + 1;
                        }
                    }
                break;
            // changes y array
            case 1:
                int randomInstanceIdY = r.nextInt(numberOfUsedInstances);
                boolean isEqualY = true;

                int randomType = -1;

                while (isEqualY) {
                    int instanceType = yArray[randomInstanceIdY];
                    randomType = r.nextInt(InstanceType.values().length);

                    if (randomType != instanceType) {
                        isEqualY = false;
                    }
                }
                yArray[randomInstanceIdY] = randomType;

                break;
            //change z array
            case 2:
                int randomOldPosition;
                int n = workflow.getJobList().size();
                randomOldPosition = r.nextInt(n);
                int randomNewPosition;

                WorkflowDAG dag = workflow.getWfDAG();
                ArrayList<Integer> parentList = dag.getParents(zArray[randomOldPosition]);
                ArrayList<Integer> childList = dag.getChildren(zArray[randomOldPosition]);
                int start = randomOldPosition;
                int end = randomOldPosition;

                while (start >=0 && !ExistItemInList(parentList,zArray[start])) {
                    start--;
                }

                while (end < n && !ExistItemInList(childList,zArray[end])) {
                    end++;
                }


                int diff = (end - 1) - (start + 1);
                if (diff > 0) {
                    randomNewPosition = r.nextInt(diff);
                    randomNewPosition += (start + 1);
                    if (randomNewPosition != randomOldPosition) {
                        if (randomNewPosition > randomOldPosition) {
                            int temp = zArray[randomOldPosition];
                            for (int j = randomOldPosition; j < randomNewPosition; j++)
                                zArray[j] = zArray[j + 1];
                            zArray[randomNewPosition] = temp;
                        }
                        else {
                            int temp = zArray[randomOldPosition];
                            for (int j = randomOldPosition -1; j >= randomNewPosition; j--)
                                zArray[j+1] = zArray[j];
                            zArray[randomNewPosition] = temp;
                        }
                    }
                }
                break;
        }
        instanceUsages = new short[numberOfUsedInstances];
    }

    public boolean ExistItemInList(ArrayList<Integer> l, int item)
    {
        for (Integer i : l) {
            if (i == item) {
                return true;
            }
        }
        return false;
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

            int newYArray[] = new int[maxNumberOfInstances];

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

    public void generateFullyRandomSolution(){
        numberOfUsedInstances = -1;
        List<Job> jobList = workflow.getJobList();

        /**
         * Generates random xArray
         * */
        Random r = new Random();
        for (int i = 0; i < jobList.size(); i++) {
            int random = r.nextInt(maxNumberOfInstances);
            xArray[i] = random;
            if (random >= numberOfUsedInstances){
                numberOfUsedInstances = random+1;
            }
        }

        /**
         * Generate random yArray
         * */

        for (int i = 0; i < numberOfUsedInstances; i++) {
            int random = r.nextInt(InstanceType.values().length);
            yArray[i] = random;
        }

        /**
         * Generate random zArray
         * */
        WorkflowDAG dag = workflow.getWfDAG();
        List<Job> originalJobList = workflow.getJobList();
        Random random = new Random();

        ArrayList<Integer> readyTasksToOrder = dag.getFirstLevel();
        int randomId, taskId;

        int numberOfParentList[] = GlobalAccess.numberOfParentsList;
        int parentsSum[] = new int[workflow.getNumberTasks()];

        for (int i = 0; i < workflow.getJobList().size(); i++) {
            randomId = random.nextInt(readyTasksToOrder.size());
            taskId = readyTasksToOrder.get(randomId);

            ArrayList<Integer> children = dag.getChildren(taskId);
            for (int k = 0; k < children.size(); k++) {
                int childId = children.get(k);
                parentsSum[childId]++;
                if (parentsSum[childId] == numberOfParentList[childId]){
                    readyTasksToOrder.add(childId);
                }
            }
            zArray[i] = originalJobList.get(taskId).getIntId();
            readyTasksToOrder.remove(randomId);
        }

        this.origin = "Fully random Solution";
    }

    public void generateRandomSolution(Workflow workflow){
        List<Job> jobList = workflow.getJobList();

        /**
         * Generates random xArray
         * */
        Random r = new Random();
        int bound = 0;
        int used = 0;

//        It always assigns task 0 (first task) to instance 0 (first instance)
        xArray[jobList.get(0).getIntId()] = bound;

        bound++;
        used++;
        for (int i = 1; i < jobList.size(); i++) {
            Job job = jobList.get(i);
            int random = r.nextInt(bound + 1);

            xArray[job.getIntId()] = random;

            if (bound == random && used < maxNumberOfInstances){
                used++;
                if (bound != maxNumberOfInstances -1){
                    bound++;
                }
            }
        }

        /**
         * Generate random yArray
         * */
        numberOfUsedInstances = used;

        for (int i = 0; i < numberOfUsedInstances; i++) {
            int random = r.nextInt(InstanceType.values().length);
            yArray[i] = random;
        }

        /**
         * Generate random zArray
         * */
        WorkflowDAG dag = workflow.getWfDAG();
        List<Job> originalJobList = workflow.getJobList();
        Random random = new Random();

        ArrayList<Integer> readyTasksToOrder = dag.getFirstLevel();
        int randomId, taskId;

        int numberOfParentList[] = GlobalAccess.numberOfParentsList;
        int parentsSum[] = new int[workflow.getNumberTasks()];

        for (int i = 0; i < workflow.getJobList().size(); i++) {
            randomId = random.nextInt(readyTasksToOrder.size());
            taskId = readyTasksToOrder.get(randomId);

            ArrayList<Integer> children = dag.getChildren(taskId);
            for (int k = 0; k < children.size(); k++) {
                int childId = children.get(k);
                parentsSum[childId]++;
                if (parentsSum[childId] == numberOfParentList[childId]){
                    readyTasksToOrder.add(childId);
                }
            }
            zArray[i] = originalJobList.get(taskId).getIntId();
            readyTasksToOrder.remove(randomId);
        }
    }

    /**
     * The fitness function for this problem computes the makespan and the fitness value of the
     *  scheduled tasks of the workflow with a fixed number of instances
     * */
    public void fitness(){
        if (workflow == null || instanceInfo == null){
            Log.logger.warning("Problem with fitness function properties");
            return;
        }
        List<Job> originalJobList = workflow.getJobList();
        WorkflowDAG dag = workflow.getWfDAG();

        double instanceTimeLine[] = new double[numberOfUsedInstances];
        double instanceStartTime[] = new double[numberOfUsedInstances];
        boolean instanceIsUsed[] = new boolean[numberOfUsedInstances];

        taskFinishTimes = new double[workflow.getJobList().size()];

        instanceTimes = new double[numberOfUsedInstances];

        Instance instanceList[] = new Instance[numberOfUsedInstances];
        for (int i = 0; i < numberOfUsedInstances; i++) {
            instanceList[i] = new Instance();
        }

        Job firstJob = originalJobList.get(zArray[0]);

        double exeTime = firstJob.getExeTime()[yArray[xArray[firstJob.getIntId()]]];

        instanceTimeLine[xArray[firstJob.getIntId()]] = exeTime;
        instanceIsUsed[xArray[firstJob.getIntId()]] = true;
        instanceStartTime[xArray[firstJob.getIntId()]] = 0;

        taskFinishTimes[firstJob.getIntId()] = instanceTimeLine[xArray[firstJob.getIntId()]];

        //for the rest of tasks
        for (int i = 1; i < zArray.length; i++) {
            Job job = originalJobList.get(zArray[i]);
            /**
             * TempTaskFinishTime: is used to choose, benefit from the gap or not. If the temp task
             * finish time for the gap is smaller than regular scheduling, the gap is used!
             * */
            double tempTaskFinishTime = 999999999999999999.0;
            double tempTaskExeTime = -99999999999999999.9;

            ArrayList<Integer> parentJobs = dag.getParents(job.getIntId());

            //it is possible to have multiple start tasks without dependencies
            int maxParentId = -1;

            if (parentJobs.size() == 0){
                double taskExeTime = job.getExeTime()[yArray[xArray[job.getIntId()]]];
                double currentFinishTime = instanceTimeLine[xArray[job.getIntId()]] + taskExeTime;

                if (currentFinishTime < tempTaskFinishTime){
                    tempTaskExeTime = taskExeTime;
                    tempTaskFinishTime = currentFinishTime;
                }
            }else {
                //check maximum task finish time for all of the current instances
                maxParentId = getJobWithMaxParentFinishTimeWithCij(parentJobs, job.getIntId());

                double waitingTime = taskFinishTimes[maxParentId] - instanceTimeLine[xArray[job.getIntId()]];

                if (waitingTime > 0 ){
                    double currentTime = instanceTimeLine[xArray[job.getIntId()]] + waitingTime;
                    double cij = 0D;
                    double taskExeTime = job.getExeTime()[yArray[xArray[job.getIntId()]]];
                    double currentFinishTime;

                    if (xArray[job.getIntId()] == xArray[maxParentId]){
                        currentFinishTime = currentTime + taskExeTime;
                    }else {
                        /**
                         * Time to send data: is the time between instance timeline and the max
                         * finish time of the parent task.
                         * */
                        double edge = Math.abs(originalJobList.get(maxParentId).getEdge(job.getIntId()));
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
                        tempTaskFinishTime = currentFinishTime;
                    }
                }else {
                    double cij = 0D;
                    double taskExeTime = job.getExeTime()[yArray[xArray[job.getIntId()]]];
                    double currentFinishTime;

                    if (xArray[job.getIntId()] == xArray[maxParentId]){
                        currentFinishTime = instanceTimeLine[xArray[job.getIntId()]] + taskExeTime;
                    }else {
                        double timeToSendData = instanceTimeLine[xArray[job.getIntId()]] - taskFinishTimes[maxParentId];

                        double edge = Math.abs(originalJobList.get(maxParentId).getEdge(job.getIntId()));
                        cij = edge / (double)Config.global.bandwidth;

                        if (timeToSendData >= cij){
                            currentFinishTime = instanceTimeLine[xArray[job.getIntId()]] + taskExeTime;
                        }else {
                            currentFinishTime = instanceTimeLine[xArray[job.getIntId()]] + (cij - timeToSendData) + taskExeTime;
                        }
                    }

                    if (currentFinishTime < tempTaskFinishTime){
                        tempTaskExeTime = taskExeTime + cij;
                        tempTaskFinishTime = currentFinishTime;
                    }
                }
            }
            instanceTimeLine[xArray[job.getIntId()]] = tempTaskFinishTime;

            if (!instanceIsUsed[xArray[job.getIntId()]]){
                instanceStartTime[xArray[job.getIntId()]] = tempTaskFinishTime - tempTaskExeTime;
                instanceIsUsed[xArray[job.getIntId()]] = true;
            }
            taskFinishTimes[job.getIntId()] = tempTaskFinishTime;
        }

        for (int i = 0; i < instanceTimes.length; i++) {
            instanceTimes[i] = instanceTimeLine[i] - instanceStartTime[i];
        }

        double totalCost = 0D;
//       Now we have exe time for each instance
        for (int i = 0; i < instanceTimes.length; i++) {
            if (yArray[i] != -1){
                double theHour = instanceTimes[i]/3600D;
                theHour = Math.ceil(theHour);
                totalCost += theHour * instanceInfo[this.yArray[i]].spotPrice;
            }
        }

        this.instanceTimelines = instanceTimeLine;
        this.instanceStartTime = instanceStartTime;
        this.cost = totalCost;
        this.makespan = (int)findMaxInstanceTime(instanceTimeLine);


        if(Config.global.deadline_based)
        {
            computeFitnessValue_DeadlineBased();
        }
        else
        {
            computeFitnessValue();
        }



    }

    int getJobWithMaxParentFinishTimeWithCij(ArrayList<Integer> parentJobs, int jobId){
        List<Job> originalJobList = workflow.getJobList();
        double tempValue = -1;
        int tempId = -1;

        for (int parentId : parentJobs){
            double tempEdge = Math.abs(originalJobList.get(parentId).getEdge(jobId));
            double tempCIJ = tempEdge / (double)Config.global.bandwidth;
            double maxJobStartTime;
            if (xArray[jobId] == xArray[parentId]){
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

    void computeFitnessValue(){

        //double test = 0.91238500001;
        DecimalFormat df = new DecimalFormat ("#.######");
        cost = Double.parseDouble(df.format(cost));
       // Log.logger.info("Test is:"+test);

        double delta = cost - workflow.getBudget();
        double penalty1 = 0;

        if (delta > 0){
            penalty1 = delta;
        }

        fitnessValue = makespan + beta * (penalty1);
    }


    void computeFitnessValue_DeadlineBased(){

        DecimalFormat df = new DecimalFormat ("#.######");
        cost = Double.parseDouble(df.format(cost));

        double delta = makespan - Config.global.deadline;
        double penalty1 = 0;

        if (delta > 0){
            penalty1 = delta;
        }

        fitnessValue = cost + beta * (penalty1);

    }


    double findMaxInstanceTime(double instanceTimes[]){
        double max = instanceTimes[0];

        for (double temp : instanceTimes){
            if (temp > max)
                max = temp;
        }
        return max;
    }

    /**
     * ================================================================================================
     * ================================================================================================
     * These methods are HEFT version of the solution methods
     * */

    /**
     * To use heft fitness you MUST first set the public static orderedJobList in GlobalAccess class
     * */
    public void heftFitness(){
        if (workflow == null || instanceInfo == null){
            Log.logger.warning("Problem with fitness function properties");
            return;
        }
        List<Job> originalJobList = workflow.getJobList();
        List<Job> orderedJobList = GlobalAccess.orderedJobList;

        WorkflowDAG dag = workflow.getWfDAG();

        double instanceTimeLine[] = new double[numberOfUsedInstances];
        double instanceStartTime[] = new double[numberOfUsedInstances];
        boolean instanceIsUsed[] = new boolean[numberOfUsedInstances];

        taskFinishTimes = new double[workflow.getJobList().size()];
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
        taskFinishTimes[originalVersion.getIntId()] = instanceTimeLine[xArray[firstJob.getIntId()]];

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
            if (instanceList[xArray[job.getIntId()]].gapList.size() >= 1 && parentJobs.size() != 0){
                Collections.sort(instanceList[xArray[job.getIntId()]].gapList , Gap.gapComparator);
                maxParentId = getJobWithMaxParentFinishTimeWithCij(parentJobs, job.getIntId());
                maxParentJob = originalJobList.get(maxParentId);
                latestParentFinishTime = taskFinishTimes[maxParentJob.getIntId()];

                int k =0;
                for (Gap gap: instanceList[xArray[job.getIntId()]].gapList){
                    if (latestParentFinishTime < gap.endTime){
                        double tempEdge = Math.abs(originalJobList.get(maxParentId).getEdge(job.getIntId()));
                        double tempCIJ = tempEdge / (double)Config.global.bandwidth;
                        double taskExeTime;

                        if (xArray[job.getIntId()] == xArray[maxParentId]){
                            taskExeTime = job.getExeTime()[yArray[xArray[job.getIntId()]]];
                        }else {
                            taskExeTime = job.getExeTime()[yArray[xArray[job.getIntId()]]] + tempCIJ;
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
                                    gapTaskFinishTime = gap.startTime + taskExeTime;
                                }else {
                                    gapTaskFinishTime = gap.startTime + taskExeTime +( tempCIJ - timeToSendData);
                                }
                            }else {
                                gapTaskFinishTime = latestParentFinishTime + taskExeTime;
                            }

                            if (gapTaskFinishTime < tempTaskFinishTime){
                                tempTaskExeTime = taskExeTime;
                                tempTaskFinishTime = gapTaskFinishTime;
                                gapIsUsed = true;
                                gapOccurred = false;
                                instanceGapId = xArray[job.getIntId()];
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
                double taskExeTime = job.getExeTime()[yArray[xArray[job.getIntId()]]];
                double currentFinishTime = instanceTimeLine[xArray[job.getIntId()]] + taskExeTime;

                if (currentFinishTime < tempTaskFinishTime){
                    tempTaskExeTime = taskExeTime;
                    tempTaskFinishTime = currentFinishTime;
                    gapIsUsed = false;
                }
            }else {
                //check maximum task finish time for all of the current instances
                maxParentId = getJobWithMaxParentFinishTimeWithCij(parentJobs, job.getIntId());

                double waitingTime = taskFinishTimes[maxParentId] - instanceTimeLine[xArray[job.getIntId()]];

                if (waitingTime > 0 ){
                    double currentTime = instanceTimeLine[xArray[job.getIntId()]] + waitingTime;
                    double cij = 0D;
                    double taskExeTime = job.getExeTime()[yArray[xArray[job.getIntId()]]];
                    double currentFinishTime;

                    if (xArray[job.getIntId()] == xArray[maxParentId]){
                        currentFinishTime = currentTime + taskExeTime;
                    }else {
                        /**
                         * Time to send data: is the time between instance timeline and the max
                         * finish time of the parent task.
                         * */
                        double edge = Math.abs(originalJobList.get(maxParentId).getEdge(job.getIntId()));
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
                    double taskExeTime = job.getExeTime()[yArray[xArray[job.getIntId()]]];
                    double currentFinishTime;

                    if (xArray[job.getIntId()] == xArray[maxParentId]){
                        currentFinishTime = instanceTimeLine[xArray[job.getIntId()]] + taskExeTime;
                    }else {
                        double timeToSendData = instanceTimeLine[xArray[job.getIntId()]] - taskFinishTimes[maxParentId];

                        double edge = Math.abs(originalJobList.get(maxParentId).getEdge(job.getIntId()));
                        cij = edge / (double)Config.global.bandwidth;

                        if (timeToSendData >= cij){
                            currentFinishTime = instanceTimeLine[xArray[job.getIntId()]] + taskExeTime;
                        }else {
                            currentFinishTime = instanceTimeLine[xArray[job.getIntId()]] + (cij - timeToSendData) + taskExeTime;
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

            if (gapOccurred && instanceIsUsed[xArray[job.getIntId()]]){
                instanceList[xArray[job.getIntId()]].hasGap = true;
                Gap gap = new Gap(instanceTimeLine[xArray[job.getIntId()]], endOfInstanceWaitTime);
                instanceList[xArray[job.getIntId()]].gapList.add(gap);
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
                instanceTimeLine[xArray[job.getIntId()]] = tempTaskFinishTime;
            }
            if (!instanceIsUsed[xArray[job.getIntId()]]){
                instanceStartTime[xArray[job.getIntId()]] = tempTaskFinishTime - tempTaskExeTime;
                instanceIsUsed[xArray[job.getIntId()]] = true;
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
            totalCost += theHour * instanceInfo[this.yArray[i]].spotPrice;
        }

        this.instanceTimelines = instanceTimeLine;
        this.instanceStartTime = instanceStartTime;
        this.cost = totalCost;
        this.makespan = (int)findMaxInstanceTime(instanceTimeLine);

        if(Config.global.deadline_based)
        {
            computeFitnessValue_DeadlineBased();
        }
        else
        {
            computeFitnessValue();
        }
    }

    public void heftGenerateRandomNeighborSolution(Workflow workflow){
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
                    if (randomInstanceId != currentInstanceId && randomInstanceId < maxNumberOfInstances){
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
        instanceUsages = new short[numberOfUsedInstances];

        solutionMapping();
    }

    public void heftGenerateRandomSolution(Workflow workflow){
        List<Job> jobList = workflow.getJobList();

        /**
         * Generates random xArray
         * */
        Random r = new Random();
        int bound = 0;
        int used = 0;

//        It always assigns task 0 (first task) to instance 0 (first instance)
        xArray[jobList.get(0).getIntId()] = bound;

        bound++;
        used++;
        for (int i = 1; i < jobList.size(); i++) {
            Job job = jobList.get(i);
            int random = r.nextInt(bound + 1);

            xArray[job.getIntId()] = random;

            if (bound == random && used < maxNumberOfInstances){
                used++;
                if (bound != maxNumberOfInstances -1){
                    bound++;
                }
            }
        }

        /**
         * Generate random yArray
         * */
        numberOfUsedInstances = used;

        for (int i = 0; i < numberOfUsedInstances; i++) {
            int random = r.nextInt(InstanceType.values().length);
            yArray[i] = random;
        }
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

    @Override
    public Solution clone() throws CloneNotSupportedException {
        Solution solution = (Solution)super.clone();

        int newX[] = new int[workflow.getJobList().size()];
        int newY[] = new int[maxNumberOfInstances];
        Integer newZ[] = new Integer[workflow.getJobList().size()];

        for (int i = 0; i < workflow.getJobList().size(); i++) {
            newX[i] = xArray[i];
        }

        for (int i = 0; i < maxNumberOfInstances; i++) {
            newY[i] = yArray[i];
        }

        for (int i = 0; i < workflow.getJobList().size(); i++) {
            newZ[i] = new Integer(zArray[i]);
        }

        solution.xArray = newX;
        solution.yArray = newY;
        solution.zArray = newZ;

        solution.instanceUsages = new short[maxNumberOfInstances];
        solution.taskFinishTimes = new double[workflow.getJobList().size()];
        solution.instanceTimelines = new double[maxNumberOfInstances];
        solution.instanceStartTime = new double[maxNumberOfInstances];
        solution.instanceTimes = new double[numberOfUsedInstances];

        return solution;
    }
}
