package org.optframework.core;

import com.rits.cloning.Cloner;
import org.cloudbus.cloudsim.util.workload.Job;
import org.cloudbus.cloudsim.util.workload.Workflow;
import org.cloudbus.cloudsim.util.workload.WorkflowDAG;
import org.cloudbus.spotsim.enums.InstanceType;
import org.optframework.config.StaticProperties;

import java.util.*;

public class SimulatedAnnealingAlgorithm implements StaticProperties {

    double temp;

    Set<Solution> visited_solutions = new HashSet<>();

    Solution bestCurrent;

    Solution globalBest;

    Workflow workflow;

    InstanceInfo instanceInfo[];

    Cloner cloner = new Cloner();

    public SimulatedAnnealingAlgorithm() {
    }

    public SimulatedAnnealingAlgorithm(Workflow workflow, InstanceInfo[] instanceInfo) {
        this.workflow = workflow;
        this.instanceInfo = instanceInfo;
    }

    public Solution runSA(){
        Log.logger.info("Starts SA Algorithm");
        Log.logger.info("Simulated Annealing parameters Initial temp: "+ START_TEMP+ " Final temp: " + FINAL_TEMP + " Cooling Factor: " + COOLING_FACTOR + " Equilibrium point: " + SA_EQUILIBRIUM_COUNT);

        Solution initialSolution = new Solution(workflow.getJobList().size(), M_NUMBER);

        //Initializes the initial solution with random values
        initialSolution.generateRandomSolution(workflow);

        temp = START_TEMP;
        bestCurrent = initialSolution;
        globalBest = bestCurrent;
        fitness(bestCurrent);

        //LOOP at a fixed temperature:
        while (temp >= FINAL_TEMP){
            for (int i = 0; i < SA_EQUILIBRIUM_COUNT; i++) {
                //GENERATES random neighbor
                Solution randomNeighbor = new Solution(workflow.getJobList().size(), M_NUMBER);
                randomNeighbor.generateRandomSolution(workflow);
                if (!visited_solutions.contains(randomNeighbor)){
                    visited_solutions.add(randomNeighbor);

                    fitness(randomNeighbor);

                    double delta = randomNeighbor.cost - bestCurrent.cost;
                    if (delta <= 0){
                        bestCurrent = randomNeighbor;
                        if((randomNeighbor.cost - globalBest.cost) <= 0){
                            globalBest = randomNeighbor;
                        }
                    }else {
                        //Generate a uniform random value x in the range (0,1)
                        Random r = new Random();
                        double random = r.nextDouble();

                        if (random < bolzmanDist(delta, temp)){
                            bestCurrent = randomNeighbor;
                        }
                    }
                }else{
                    updateVisitedField(randomNeighbor);
                }
            }
            temp -= COOLING_FACTOR;
        }
        if(bestCurrent.cost - globalBest.cost <= 0){
            globalBest = bestCurrent;
            return globalBest;
        }else {
            return bestCurrent;
        }
    }

    void updateVisitedField(Solution solution){
        for (Solution temp: visited_solutions){
            if (temp.equals(solution)){
                temp.visited ++;
            }
        }
    }

    double bolzmanDist(double delta, double temp){
        return Math.exp(-(Math.abs(delta))/temp);
    }

    /**
     * The fitness function for this problem computes the required cost to the workflow on the specified instances
     * */
    void fitness(Solution solution){
        double totalCost = 0;

        Workflow clonedWorkflow = cloner.deepClone(workflow);

        WorkflowDAG dag = clonedWorkflow.getWfDAG();
        ArrayList<Integer> level = dag.getFirstLevel();

        ArrayList<Job> jobList = (ArrayList<Job>) clonedWorkflow.getJobList();

        double instancesTimes[] = new double[solution.numberOfUsedInstances];

        boolean instanceUsed[] = new boolean[solution.numberOfUsedInstances];

        double instanceTimeLine[] = new double[solution.numberOfUsedInstances];

        Map<Integer, ArrayList<ReadyTask>> instanceList = new HashMap();

//      Do this for the first level - First level may contain several tasks
        for (int jobId: level){
            int instanceId = solution.xArray[jobId];
            int type = solution.yArray[instanceId];
            long exeTime = TaskUtility.executionTimeOnType(jobList.get(jobId), instanceInfo[type].getType());

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
             * This for do the following:
             * - finds max parent finish time for every task in a level
             * - assigns all of them to an instance
             * - computes weights for all of the tasks in a level and make them ready to run on instance
             * */
            for (int jobId: level){
                int instanceId = solution.xArray[jobId];
                int typeId = solution.yArray[instanceId];

                InstanceType instanceType = instanceInfo[typeId].getType();
                long exeTime = TaskUtility.executionTimeOnType(jobList.get(jobId), instanceType);


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
            totalCost += (instancesTimes[i]/1) * instanceInfo[solution.yArray[i]].spotPrice;
//            totalCost += 100*((instancesTimes[i]/3600D) * instanceInfo[solution.yArray[i]].spotPrice);
        }
        solution.instanceTimes = instancesTimes;
        solution.cost = totalCost;
        solution.makespan = findMaxInstanceTime(instancesTimes);
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
}
