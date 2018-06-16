package org.optframework.core;

import org.cloudbus.cloudsim.util.workload.WorkflowDAG;
import org.optframework.config.StaticProperties;
import org.optframework.core.utils.PreProcessor;

import java.util.*;

/**
 * This class is the Solution representation of the problem
 *
 * @author Hessam - hessam.modaberi@gmail.com
 * @since 2018
 *
 * */
public class Solution implements StaticProperties {
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
    public double makespan;

    public short instanceUsages[];

    public double fitnessValue;

    public double beta;

    public int numberOfUsedInstances;

    public int maxNumberOfInstances;

    public int visited;

    public double instanceTimes[];

    public double instanceTimelines[];

    public Workflow workflow;

    public InstanceInfo instanceInfo[];

    /**
     * M prime, is the worst case makespan of the given workflow happening when all the spot-instances fail and we switch all of them to the on-demand instances
     */
    public int makespanPrime;

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
                    int randomType = r.nextInt(N_TYPES);
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
                    randomType = r.nextInt(N_TYPES);

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
            int random = r.nextInt(N_TYPES);
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

        double totalCost = 0D;

        WorkflowDAG dag = workflow.getWfDAG();
        ArrayList<Integer> level = dag.getFirstLevel();

        ArrayList<Job> jobList = (ArrayList<Job>) workflow.getJobList();

        double instancesTimes[] = new double[this.numberOfUsedInstances];

        boolean instanceUsed[] = new boolean[this.numberOfUsedInstances];

        double instanceTimeLine[] = new double[this.numberOfUsedInstances];

        Map<Integer, ArrayList<Job>> instanceList = new HashMap();

//      Do this for the first level - First level may contain several tasks
        for (int jobId: level){
            int instanceId = this.xArray[jobId];

            if (!instanceList.containsKey(instanceId)){
                instanceList.put(instanceId, new ArrayList<Job>());
            }
            ArrayList<Job> readyTaskList = instanceList.get(instanceId);
            readyTaskList.add(jobList.get(jobId));
        }

//      Computes maximum task's length and updates the instance times
        for (Integer instance : instanceList.keySet()){
            ArrayList<Job> readyTaskList = instanceList.get(instance);
            Collections.sort(readyTaskList, Job.rankComarator);

            for (Job readyTask : readyTaskList){
                instancesTimes[instance] += readyTask.getExeTime()[yArray[xArray[readyTask.getIntId()]]];

                instanceTimeLine[instance] += readyTask.getExeTime()[yArray[xArray[readyTask.getIntId()]]];
                instanceUsed[instance] = true;

                readyTask.setFinishTime(instanceTimeLine[instance]);
            }
        }

        //Go to the second level
        level = dag.getNextLevel(level);

        //Do this for the levels after the initial level
        while (level.size() != 0){
            instanceList = new HashMap<>();

            /**
             * This 'for' does the following:
             * - finds max parent's finish time for every task in a level
             * - assigns all of them to an instance
             * - computes weights for all of the tasks in a level and make them ready to run on instance
             * */
            for (int jobId: level){
                Job readyTask = jobList.get(jobId);
                int instanceId = this.xArray[jobId];

                double jobStartTime = getJobStartTime(jobId, dag.getParents(jobId), PreProcessor.bw, jobList);
                readyTask.setStartTime(jobStartTime);

                if (!instanceList.containsKey(instanceId)){
                    instanceList.put(instanceId, new ArrayList<Job>());
                }
                ArrayList<Job> readyTaskList = instanceList.get(instanceId);

                readyTaskList.add(readyTask);
            }

//            It is time to compute the time for every instance
            for (Integer instance : instanceList.keySet()){
                ArrayList<Job> readyTaskList = instanceList.get(instance);
                Collections.sort(readyTaskList, Job.rankComarator);

                for (Job readyTask : readyTaskList){
                    int instanceTypeId = yArray[xArray[readyTask.getIntId()]];

                    if (!instanceUsed[instance]){
                        instanceUsed[instance] = true;
                        // ASSUMPTION 2
                        instancesTimes[instance] = readyTask.getExeTime()[instanceTypeId];
                        instanceTimeLine[instance] = readyTask.getStartTime();
                        instanceTimeLine[instance] += readyTask.getExeTime()[instanceTypeId];
                    }else if (readyTask.getStartTime() > instanceTimeLine[instance]){
                        double timeToWait = readyTask.getStartTime() - instanceTimeLine[instance];
                        instancesTimes[instance] += (timeToWait + readyTask.getExeTime()[instanceTypeId]);

                        instanceTimeLine[instance] = readyTask.getStartTime() + readyTask.getExeTime()[instanceTypeId];
                    }else{
                        instancesTimes[instance] += readyTask.getExeTime()[instanceTypeId];
                        instanceTimeLine[instance] += readyTask.getExeTime()[instanceTypeId];
                    }

                    readyTask.setFinishTime(instanceTimeLine[instance]);
                }
            }

            level = dag.getNextLevel(level);
        }

//       Now we have exe time for each instance
        for (int i = 0; i < instancesTimes.length; i++) {
            totalCost += (instancesTimes[i]/3600D) * instanceInfo[this.yArray[i]].spotPrice;
        }

        this.instanceTimelines = instanceTimeLine;
        this.instanceTimes = instancesTimes;
        this.cost = totalCost;
        this.makespan = findMaxInstanceTime(instanceTimeLine);

        computeFitnessValue();
    }

    void computeFitnessValue(){
        double delta = cost - workflow.getBudget();
        double penalty1 = 0;

        if (delta > 0){
            penalty1 = delta;
        }

        fitnessValue = makespan + beta * (penalty1);
    }

    double getJobStartTime(int jobId, ArrayList<Integer> parentList, double bw, ArrayList<Job> jobList){
        Job parentJob = jobList.get(parentList.get(0));
        double maxTemp = parentJob.getFinishTime() + parentJob.getEdge(jobId)/bw;

        for (int i = 1; i < parentList.size(); i++) {
            double temp = jobList.get(parentList.get(i)).getFinishTime() + jobList.get(parentList.get(i)).getEdge(jobId)/bw;
            if (temp > maxTemp)
                maxTemp = temp;
        }
        return  maxTemp;
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
        return id == solution.id &&
                Arrays.equals(xArray, solution.xArray) &&
                Arrays.equals(yArray, solution.yArray);
    }

    @Override
    public int hashCode() {

        int result = Objects.hash(id);
        result = 31 * result + Arrays.hashCode(xArray);
        result = 31 * result + Arrays.hashCode(yArray);
        return result;
    }
}
