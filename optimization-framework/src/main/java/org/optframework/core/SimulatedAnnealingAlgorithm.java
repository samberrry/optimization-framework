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
        fitness(bestCurrent);

        //LOOP at a fixed temperature:
        while (temp >= FINAL_TEMP){
            for (int i = 0; i < SA_EQUILIBRIUM_COUNT; i++) {
                //GENERATES random neighbor
                Solution randomNeighbor = new Solution(workflow.getJobList().size(), M_NUMBER);
                if (!visited_solutions.contains(randomNeighbor)){
                    visited_solutions.add(randomNeighbor);

                    fitness(randomNeighbor);

                    double delta = randomNeighbor.cost - bestCurrent.cost;
                    if (delta <= 0){
                        bestCurrent = randomNeighbor;
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
        return bestCurrent;
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
        long elapsedTime;
        double cost;

        Workflow clonedWorkflow = cloner.deepClone(workflow);

        WorkflowDAG dag = clonedWorkflow.getWfDAG();
        ArrayList<Integer> level = dag.getFirstLevel();

        ArrayList<Job> jobList = (ArrayList<Job>) clonedWorkflow.getJobList();

        int instancesTimes[] = new int[solution.numberOfUsedInstances];

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
                Job job = jobList.get(readyTask.jobId);

                job.setExeTime(readyTask.exeTime);
                job.setWeight(readyTask.exeTime);
                job.setLatestFinishTime(instancesTimes[instance]);
            }
        }

        //Do this for the levels after the initial level
        while (dag.getNextLevel(level).size() != 0){
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
                   parentTaskList.add(new ParentTask(parentId , jobList.get(parentId).getLatestFinishTime() , jobList.get(parentId).getEdge(jobId)/ instanceType.getBandwidth()));
               }
               ParentTask maxParent = findMaxParentFinishTimeWithCR(parentTaskList);

                if (!instanceList.containsKey(instanceId)){
                    instanceList.put(instanceId, new ArrayList<ReadyTask>());
                }
                ArrayList<ReadyTask> readyTaskList = instanceList.get(instanceId);

                readyTaskList.add(new ReadyTask(jobId,exeTime, maxParent.parentFinishTime, maxParent.cr));
            }

//            It is time to compute the time for every instance
            for (Integer instance : instanceList.keySet()){
                ArrayList<ReadyTask> readyTaskList = instanceList.get(instance);


                for (ReadyTask readyTask : readyTaskList){
////////////////////// For all tasks on an instance

                }
            }

            level = dag.getNextLevel(level);
        }
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
}
