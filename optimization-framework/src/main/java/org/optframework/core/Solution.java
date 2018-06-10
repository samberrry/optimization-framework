package org.optframework.core;

import org.cloudbus.cloudsim.util.workload.Job;
import org.cloudbus.cloudsim.util.workload.Workflow;
import org.cloudbus.cloudsim.util.workload.WorkflowDAG;
import org.cloudbus.spotsim.enums.InstanceType;
import org.optframework.config.StaticProperties;

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

    public double fitnessValue;

    public double beta;

    public int numberOfUsedInstances;

    public int visited;

    public double instanceTimes[];

    public double instanceTimelines[];

    private Workflow workflow;

    private InstanceInfo instanceInfo[];

    /**
     * M prime, is the worst case makespan of the given workflow happening when all the spot-instances fail and we switch all of them to the on-demand instances
     */
    public int makespanPrime;

    public Solution() {
    }

    public Solution(Workflow workflow, InstanceInfo[] instanceInfo, int numberOfInstances) {
        this.workflow = workflow;
        this.instanceInfo = instanceInfo;
        xArray = new int[workflow.getJobList().size()];
        yArray = new int[numberOfInstances];
        yPrimeArray = new int[numberOfInstances];
        beta =1;
    }

    void generateRandomNeighborSolution(Workflow workflow){
        this.workflow = workflow;

        Random r = new Random();
        int xOry = r.nextInt(2);

        switch (xOry){
                //changes x array
            case 0:
                boolean newOk = true;

                int randomTask = r.nextInt(xArray.length);
                int currentInstanceId = xArray[randomTask];

                boolean isEqual = true;
                int randomInstanceId = -1;

                while (isEqual){
                    randomInstanceId = r.nextInt(numberOfUsedInstances+1);
                    if (randomInstanceId != currentInstanceId && randomInstanceId < xArray.length){
                        isEqual = false;
                    }
                }

                xArray[randomTask] = randomInstanceId;

                //if new instance is selected
                if (randomInstanceId == numberOfUsedInstances && randomInstanceId < M_NUMBER){
                    int randomType = r.nextInt(N_TYPES);
                    yArray[randomInstanceId] = randomType;
                    numberOfUsedInstances++;
                }

                break;
                // changes y array
            case 1:
                int randomInstanceIdY = r.nextInt(yArray.length);
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
    }

    void generateRandomSolution(Workflow workflow){
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
     * The fitness function for this problem computes the required cost to the workflow on the specified instances
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

        Map<Integer, ArrayList<ReadyTask>> instanceList = new HashMap();

//      Do this for the first level - First level may contain several tasks
        for (int jobId: level){
            int instanceId = this.xArray[jobId];
            int type = this.yArray[instanceId];
            double exeTime = TaskUtility.executionTimeOnType(jobList.get(jobId), instanceInfo[type].getType());

            if (!instanceList.containsKey(instanceId)){
                instanceList.put(instanceId, new ArrayList<ReadyTask>());
            }
            ArrayList<ReadyTask> readyTaskList = instanceList.get(instanceId);
            readyTaskList.add(new ReadyTask(jobId, exeTime));
        }

//      Computes maximum task's length and updates the instance times
        for (Integer instance : instanceList.keySet()){
            ArrayList<ReadyTask> readyTaskList = instanceList.get(instance);
            Collections.sort(readyTaskList);

            for (ReadyTask readyTask : readyTaskList){
                instancesTimes[instance] += readyTask.exeTime;
                instanceTimeLine[instance] += readyTask.exeTime;
                instanceUsed[instance] = true;
                Job job = jobList.get(readyTask.jobId);

                job.setExeTime(readyTask.exeTime);
                job.setWeight(readyTask.exeTime);
                job.setFinishTime(instanceTimeLine[instance]);
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
                int instanceId = this.xArray[jobId];
                int typeId = this.yArray[instanceId];

                InstanceType instanceType = instanceInfo[typeId].getType();
                double exeTime = TaskUtility.executionTimeOnType(jobList.get(jobId), instanceType);

                ArrayList<Integer> parentList = dag.getParents(jobId);

                ArrayList<ParentTask> parentTaskList = new ArrayList<>();
                for (Integer parentId : parentList){
                    parentTaskList.add(new ParentTask(parentId , jobList.get(parentId).getFinishTime() , jobList.get(parentId).getEdge(jobId)/ instanceType.getBandwidth()));
                }
                ParentTask maxParent = findMaxParentFinishTimeWithCR(parentTaskList);

                if (!instanceList.containsKey(instanceId)){
                    instanceList.put(instanceId, new ArrayList<ReadyTask>());
                }
                ArrayList<ReadyTask> readyTaskList = instanceList.get(instanceId);

                readyTaskList.add(new ReadyTask(jobId,exeTime, maxParent.parentFinishTime, jobList.get(maxParent.parentId).getWeight() , maxParent.cr));
            }

//            It is time to compute the time for every instance
            for (Integer instance : instanceList.keySet()){
                ArrayList<ReadyTask> readyTaskList = instanceList.get(instance);
                Collections.sort(readyTaskList, ReadyTask.weightComparator);

                for (ReadyTask readyTask : readyTaskList){
                    Job job = jobList.get(readyTask.jobId);

                    if (!instanceUsed[instance]){
                        instanceUsed[instance] = true;
                        instancesTimes[instance] = readyTask.exeTime + readyTask.cr;
                        instanceTimeLine[instance] = readyTask.maxParentFinishTime;
                        instanceTimeLine[instance] += readyTask.exeTime + readyTask.cr;
                    }else if (readyTask.maxParentFinishTime > instanceTimeLine[instance]){
                        double timeToWait = readyTask.maxParentFinishTime - instanceTimeLine[instance];
                        instancesTimes[instance] += (timeToWait + readyTask.exeTime + readyTask.cr);

                        instanceTimeLine[instance] = readyTask.maxParentFinishTime + readyTask.exeTime + readyTask.cr;
                    }else{
                        instancesTimes[instance] += readyTask.exeTime + readyTask.cr;
                        instanceTimeLine[instance] += readyTask.exeTime + readyTask.cr;
                    }

                    job.setExeTime(readyTask.exeTime);
                    job.setFinishTime(instanceTimeLine[instance]);
                    job.setWeight( readyTask.weight);
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

        computerFitnessValue();
    }

    void computerFitnessValue(){
        double delta = cost - workflow.getBudget();
        double penalty1 = 0;

        if (delta > 0){
            penalty1 = delta;
        }

        fitnessValue = makespan + beta * (penalty1);
    }

    ParentTask findMaxParentFinishTimeWithCR(ArrayList<ParentTask> parentTaskList){
        ParentTask max = parentTaskList.get(0);
        double maxTemp = max.parentFinishTime + max.cr;

        for (ParentTask task : parentTaskList){
            double temp = task.parentFinishTime + task.cr;
            if (temp > maxTemp)
                max = task;
        }
        return  max;
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
