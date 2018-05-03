package org.cloudbus.spotsim.simrecords;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.util.workload.Job;
import org.cloudbus.cloudsim.util.workload.Job.JobStatus;
import org.cloudbus.cloudsim.util.workload.Task;
import org.cloudbus.spotsim.broker.SchedulingDecision;
import org.cloudbus.spotsim.broker.resources.Resource;
import org.cloudbus.spotsim.main.Results;
import org.cloudbus.spotsim.main.config.Config;
import org.cloudbus.spotsim.main.config.SimProperties;

import com.thoughtworks.xstream.XStream;

/**
 * For result analysis purpose
 */
public class SimulationData {

    private static transient SimulationData singleton;

    private Properties props;

    private Calendar startDate;

    private transient Profiler profiler = new Profiler();

    private final Statistics stats = new Statistics();

    private transient Map<Long, JobSummary> jobsDB;

    private transient List<Resource> instancesDB;

    private transient XStream xstream;

    private File simPath;

    private SimulationData() {

	this.simPath = new File(Results.RESULTS_DIR + File.separator + Config.uniqueKey());

	loadXStream();

	if (SimProperties.REPORT_DETAILED.asBoolean()) {
	    this.jobsDB = new LinkedHashMap<Long, JobSummary>();
	    this.instancesDB = new LinkedList<Resource>();
	}

	Log.logger.info(Log.clock()
		+ "Initialized Simulation Data in: "
		+ this.simPath.getAbsolutePath());
    }

    public static void reset() {
	singleton = null;
    }
    
    public static void resetStats() {
    	singleton.stats.flush();
        }

    public static SimulationData singleton() {
	if (singleton == null) {
	    init();
	}
	return singleton;
    }

    private static void init() {
	singleton = new SimulationData();
    }

    public void addNewJob(final Job j) {
	this.stats.incrJobsSubmitted();
	final JobSummary jobSummary = new JobSummary(j);
	if (SimProperties.REPORT_DETAILED.asBoolean()) {
	    this.jobsDB.put(j.getId(), jobSummary);
	}
    }

    public void commit() {
	this.stats.computeUtilization();
    }

    public Map<Long, JobSummary> getActiveJobs() {
	return this.jobsDB;
    }

    public List<Resource> getInstances() {
	if (this.instancesDB == null) {
	    loadXStream();
	}
	return this.instancesDB;
    }

    public Profiler getProfiler() {
	return this.profiler;
    }

    public Properties getProps() {
	return this.props;
    }

    public Calendar getStartDate() {
	return this.startDate;
    }

    public Statistics getStats() {
	return this.stats;
    }

    public void instanceFinished(final Resource resource) {
	if (SimProperties.REPORT_DETAILED.asBoolean()) {
	    this.instancesDB.add(resource);
	}
    }

    public void jobFinished(final Job j, final long actualEndTime) {
	if (j == null) {
	    throw new IllegalArgumentException("Job cannot be null");
	}
	if (SimProperties.REPORT_DETAILED.asBoolean()) {
	    final JobSummary jobSummary = getJobSummary(j.getId());
	    jobSummary.setActualEndTime(actualEndTime);
	    jobSummary.setFinalStatus(JobStatus.COMPLETED);
	}

	this.stats.incrActualRuntime(j.getTimeTaken());
	this.stats.incrJobsCompleted();
	if (actualEndTime > j.getDeadline()) {
	    if (Log.logger.isLoggable(Level.INFO)) {
		Log.logger.info(Log.clock() + " Deadline of job breached: " + j);
	    }
	    this.stats.incrDeadlineBreach();
	    double percent = ((j.getDeadline() - actualEndTime)/actualEndTime)*100;
	    if(this.stats.getPrecentDlBreaches()>0){
	    	percent = (percent+this.stats.getPrecentDlBreaches())/2;
	    }
	    this.stats.setPrecentDlBreaches(percent);
	    
	}
    }

    public void jobScheduled(final SchedulingDecision dec) {
	if (SimProperties.REPORT_DETAILED.asBoolean()) {
	    final JobSummary j = getJobSummary(dec.getTask().getJob().getId());
	    j.setStartedANewInstance(dec.isStartNewInstance());
	    j.setEstimatedRunTime(dec.getEstimatedRuntime());
	    j.setEstimatedCost(dec.getCost());
	    j.setScheduledStartTime(dec.getStartTime());
	}

	this.stats.incrEstimatedCost(dec.getCost());
	this.stats.incrEstimatedRunTime(dec.getEstimatedRuntime());
    }

    public void setFinishTime(final long clock) {

    }

    public void setProps(final Properties props) {
	this.props = props;
    }

    public void setStartDate(final Calendar calendar) {
	this.startDate = calendar;
    }

    public void taskSubmittedToCloud(final Task task, final Resource resource) {

	if (task.getReplicaId() == 0) {
	    final long jobWaitTime = task.getActualStartTime() - task.getJob().getSubmitTime();
	    this.stats.incrWaitingTime(jobWaitTime);

	    if (SimProperties.REPORT_DETAILED.asBoolean()) {
		final JobSummary j = getJobSummary(task.getJob().getId());
		j.setActualStartTime(task.getActualStartTime());
		j.setInstanceTypeUsed(resource.getType());
		j.setInstanceID(resource.getId());
	    }
	}
    }

    private JobSummary getJobSummary(final long id) {
	if (!this.jobsDB.containsKey(id)) {
	    throw new RuntimeException("Job summary: "
		    + id
		    + " does not exist. Jobs: "
		    + this.jobsDB.size());
	}
	return this.jobsDB.get(id);
    }

    public void writeDBs() throws IOException {
	if (!this.simPath.exists()) {
	    this.simPath.mkdirs();
	}
	toXML(new File(this.simPath, "jobs.xml"), this.jobsDB);
	toXML(new File(this.simPath, "instances.xml"), this.instancesDB);
	toXML(new File(this.simPath, "results.xml"), this.stats);
    }

    public void readDBs() throws IOException {

	final File instancesFile = new File(this.simPath, "instances.xml");
	final File jobsFile = new File(this.simPath, "jobs.xml");

	this.instancesDB = (List<Resource>) fromXML(instancesFile);
	this.jobsDB = (Map<Long, JobSummary>) fromXML(jobsFile);
    }

    private Object fromXML(final File instancesFile) throws IOException {
	FileInputStream inFile = new FileInputStream(instancesFile);
	StringBuilder xmlBuilder = new StringBuilder();

	FileChannel inChannel = inFile.getChannel();
	ByteBuffer buf = ByteBuffer.allocate(1024);

	while (inChannel.read(buf) != -1) {
	    buf.flip();
	    xmlBuilder.append(buf.toString());
	    buf.clear();
	}
	inChannel.close();
	final Object obj = this.xstream.fromXML(xmlBuilder.toString());
	return obj;
    }

    private void toXML(final File outPutFile, Object obj) throws IOException {
	OutputStream outFile = new BufferedOutputStream(new FileOutputStream(outPutFile));
	this.xstream.toXML(obj, outFile);
    }

    private void loadXStream() {
	this.xstream = new XStream();
	this.xstream.processAnnotations(SimulationData.class);
	this.xstream.processAnnotations(Resource.class);
	this.xstream.processAnnotations(JobSummary.class);
    }
}
