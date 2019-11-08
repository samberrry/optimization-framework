package org.optframework.core.utils;

import org.cloudbus.cloudsim.util.workload.Job;
import org.cloudbus.cloudsim.util.workload.Workflow;
import org.cloudbus.cloudsim.workflow.Models.DAX.Dax2Workflow;
import org.cloudbus.spotsim.main.config.SimProperties;
import org.xml.sax.Attributes;

import java.util.ArrayList;
import java.util.List;

public class OptDax2Workflow extends Dax2Workflow {
    public List<org.optframework.core.Job> jobListWithDoubleTaskLength = new ArrayList<>();

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        /*
         * Elements can be one of: 'adag' 'job' 'uses' 'child' 'parent'
         */
        int groupID = 1;
        int userID = 1;
        long submitTime = 0 ;
        int numProc = 1;

        if(qName.equalsIgnoreCase("adag")){//nothing to be done
            numJobs = Integer.parseInt(attributes.getValue("jobCount"));

            //Initiate the workflow with Jobcount number of jobs
            this.workflow = new Workflow(numJobs);

        } else if(qName.equalsIgnoreCase("job")){//a new task is being declared
            String id = attributes.getValue("id");
            String runtime = attributes.getValue("runtime");

            //Length of a job is like Million Instructions and it will not change for a cloudlet, depending on the Instance type
            //running times will change
            double jobRunTime = Double.parseDouble(runtime);
            // Make sure the DAG time are in seconds or milliseconds .. but mainly not in double
            long jobRunTimeSec = (long) Math.ceil(jobRunTime);

            //Jobs less than a second is converted in a onesec
            if(jobRunTimeSec <= 0){
                jobRunTimeSec = 1;
            }
            //running in seconds only
            long jobRunTimeLength = jobRunTimeSec * SimProperties.WORKFLOW_LENGTH_MULT.asLong();

            Job job = new Job(jobCount, submitTime, jobRunTimeLength, userID, groupID, jobRunTimeLength, numProc);

            //HESSAM ADDED
            org.optframework.core.Job doubleLengthJob = new org.optframework.core.Job(jobCount, jobRunTime);

            jobListWithDoubleTaskLength.add(doubleLengthJob);

            workflow.createTask(job);

            jobMap.put(id, job);
            jobs.add(job);

            currentJob = job;

            jobCount++;
        } else if(qName.equalsIgnoreCase("uses")){//a file dependency from the current task
            String link = attributes.getValue("link");
            String size = attributes.getValue("size");


            if(link.equalsIgnoreCase("output")){
                long sizeInBytes = Long.parseLong(size);
                long sizeInKb = sizeInBytes/1024;
                if(outputData.containsKey(currentJob.getIntId())){
                    //As the DAX does not clear mention which output data is for which child task
                    //We sum all the output data and assign the total output data to all child tasks as transfer time
                    Long oldData = outputData.get(currentJob.getIntId());
                    outputData.put(currentJob.getIntId(), sizeInKb+oldData);
                }else{
                    outputData.put(currentJob.getIntId(), sizeInKb);
                }
            }

        } else if(qName.equalsIgnoreCase("child")){//a task that depends on other(s)
            String ref = attributes.getValue("ref");
            currentJob = jobMap.get(ref);
        } else if(qName.equalsIgnoreCase("parent")){//a task that others depend on
            String ref = attributes.getValue("ref");
            Job parentJob = jobMap.get(ref);
            Long outputDataSize = outputData.get(parentJob.getIntId());

            //System.out.println("Adding Edge from "+parentJob.getIntId() + " to " + currentJob.getIntId() + " DTT " + outputDataSize);
            workflow.addEdge(parentJob,currentJob, outputDataSize/SimProperties.DATACENTER_BANDWIDTH.asLong());

        } else {
            System.out.println("WARNING: Unknown XML element:"+qName);
        }
    }
}
