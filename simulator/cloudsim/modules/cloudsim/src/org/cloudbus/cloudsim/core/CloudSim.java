/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2010, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.eventlist.EventList;
import org.cloudbus.cloudsim.core.eventlist.HeapEventList;

/**
 * This class extends the CloudSimCore to enable network simulation in CloudSim.
 * Also, it disables all the network models from CloudSim, to provide a simpler
 * simulation of networking. In the network model used by CloudSim, a topology
 * file written in BRITE format is used to describe the network. Later, nodes in
 * such file are mapped to CloudSim entities. Delay calculated from the BRITE
 * model are added to the messages send through CloudSim. Messages using the old
 * model are converted to the apropriate methods with the correct parameters.
 * 
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 1.0
 */
public class CloudSim {

    /** The Constant CLOUDSIM_VERSION_STRING. */
    private static final String CLOUDSIM_VERSION_STRING = "2.0";

    /** The id of CIS entity. */
    private static int cisId = -1;

    /** The id of CloudSimShutdown entity. */
    @SuppressWarnings("unused")
    private static int shutdownId = -1;

    /** The CIS object. */
    private static CloudInformationService cis = null;

    /** The Constant NOT_FOUND. */
    private static final int NOT_FOUND = -1;

    /** The trace flag. */
    @SuppressWarnings("unused")
    private static boolean traceFlag = false;

    /** The calendar. */
    private static Calendar calendar = null;

    /**
     * Initialises all the common attributes.
     * 
     * @param _calendar
     *        the _calendar
     * @param _traceFlag
     *        the _trace flag
     * @param numUser
     *        number of users
     * @throws Exception
     *         This happens when creating this entity before initialising
     *         CloudSim package or this entity name is <tt>null</tt> or empty
     * @pre $none
     * @post $none
     */
    private static void initCommonVariable(Calendar _calendar, boolean _traceFlag, int numUser)
	    throws Exception {
	initialize();
	// NOTE: the order for the below 3 lines are important
	traceFlag = _traceFlag;

	// Set the current Wall clock time as the starting time of
	// simulation
	if (_calendar == null) {
	    calendar = Calendar.getInstance();
	} else {
	    calendar = _calendar;
	}

	// creates a CloudSimShutdown object
	CloudSimShutdown shutdown = new CloudSimShutdown("CloudSimShutdown", numUser);
	shutdownId = shutdown.getId();
    }

    /**
     * Initialises CloudSim parameters. This method should be called before
     * creating any entities.
     * <p>
     * Inside this method, it will create the following CloudSim entities:
     * <ul>
     * <li>CloudInformationService.
     * <li>CloudSimShutdown
     * </ul>
     * <p>
     * 
     * @param numUser
     *        the number of User Entities created. This parameters indicates
     *        that {@link gridsim.CloudSimShutdown} first waits for all user
     *        entities's END_OF_SIMULATION signal before issuing terminate
     *        signal to other entities
     * @param cal
     *        starting time for this simulation. If it is <tt>null</tt>, then
     *        the time will be taken from <tt>Calendar.getInstance()</tt>
     * @param traceFlag
     *        <tt>true</tt> if CloudSim trace need to be written
     * @throws IOException
     * @throws SecurityException
     * 
     * @see gridsim.CloudSimShutdown
     * @see CloudInformationService.CloudInformationService
     * @pre numUser >= 0
     * @post $none
     */
    public static void init(int numUser, Calendar cal, boolean traceFlag) throws SecurityException,
	    IOException {

	Log.init("cloudsim.log");

	try {
	    initCommonVariable(cal, traceFlag, numUser);

	    // create a GIS object
	    cis = new CloudInformationService("CloudInformationService");

	    // set all the above entity IDs
	    cisId = cis.getId();
	} catch (IllegalArgumentException s) {
	    Log.logger.severe("["
		    + Double.toString(CloudSim.clock())
		    + "] "
		    + "CloudSim.init(): Unwanted errors happen");
	    Log.logger.severe("[" + Double.toString(CloudSim.clock()) + "] " + s.getMessage());
	} catch (Exception e) {
	    Log.logger.severe("["
		    + Double.toString(CloudSim.clock())
		    + "] "
		    + "CloudSim.init(): Unwanted errors happen");
	    Log.logger.severe("[" + Double.toString(CloudSim.clock()) + "] " + e.getMessage());
	}
    }

    /**
     * Starts the execution of CloudSim simulation. It waits for complete
     * execution of all entities, i.e. until all entities threads reach
     * non-RUNNABLE state or there are no more events in the future event queue.
     * <p>
     * <b>Note</b>: This method should be called after all the entities have
     * been setup and added.
     * 
     * @return the double
     * @throws NullPointerException
     *         This happens when creating this entity before initialising
     *         CloudSim package or this entity name is <tt>null</tt> or empty.
     * @see gridsim.CloudSim#init(int, Calendar, boolean)
     * @pre $none
     * @post $none
     */
    public static long startSimulation() throws NullPointerException {
	Log.logger.info("["
		+ Double.toString(CloudSim.clock())
		+ "] "
		+ "Starting CloudSim version "
		+ CLOUDSIM_VERSION_STRING);
	try {
	    long c = run();

	    // reset all static variables
	    cisId = -1;
	    shutdownId = -1;
	    cis = null;
	    calendar = null;
	    traceFlag = false;

	    return c;
	} catch (IllegalArgumentException e) {
	    e.printStackTrace();
	    throw new NullPointerException("CloudSim.startCloudSimulation() :"
		    + " Error - you haven't initialized CloudSim.");
	}
    }

    /**
     * Stops Cloud Simulation (based on {@link Simulation#runStop()}). This
     * should be only called if any of the user defined entities
     * <b>explicitly</b> want to terminate simulation during execution.
     * 
     * @throws NullPointerException
     *         This happens when creating this entity before initialising
     *         CloudSim package or this entity name is <tt>null</tt> or empty
     * 
     * @see gridsim.CloudSim#init(int, Calendar, boolean)
     * @see Simulation#runStop()
     * @pre $none
     * @post $none
     */
    public static void stopSimulation() throws NullPointerException {
	try {
	    runStop();
	} catch (IllegalArgumentException e) {
	    throw new NullPointerException("CloudSim.stopCloudSimulation() : "
		    + "Error - can't stop Cloud Simulation.");
	}
    }

    /**
     * Gets a new copy of initial simulation Calendar.
     * 
     * @return a new copy of Calendar object or if CloudSim hasn't been
     *         initialized
     * @see gridsim.CloudSim#init(int, Calendar, boolean, String[], String[],
     *      String)
     * @see gridsim.CloudSim#init(int, Calendar, boolean)
     * @pre $none
     * @post $none
     */
    public static Calendar getSimulationCalendar() {
	// make a new copy
	Calendar clone = calendar;
	if (calendar != null) {
	    clone = (Calendar) calendar.clone();
	}

	return clone;
    }

    /**
     * Gets the entity ID of <tt>CloudInformationService</tt>.
     * 
     * @return the Entity ID or if it is not found
     * @pre $none
     * @post $result >= -1
     */
    public static int getCloudInfoServiceEntityId() {
	return cisId;
    }

    /**
     * Sends a request to Cloud Information Service (GIS) entity to get the list
     * of all Cloud hostList.
     * 
     * @return A List containing CloudResource ID (as an Integer object) or if a
     *         CIS entity hasn't been created before
     * @pre $none
     * @post $none
     */
    public static List<Integer> getCloudResourceList() {
	if (cis == null) {
	    return null;
	}

	return cis.getList();
    }

    // ======== SIMULATION METHODS ===============//

    // Private data members
    /** The entities. */
    private static List<SimEntity> entities;

    /** The future event queue. */
    private static EventList future;

    /** The simulation clock. */
    private static long clock;

    /** Flag for checking if the simulation is running. */
    private static boolean running;

    /*
     * (non-javadoc)
     */
    /** The entities by name. */
    private static Map<String, SimEntity> entitiesByName;

    /** The paused. */
    private static boolean paused = false;

    /** The pause at. */
    private static long pauseAt = -1;

    /** The abrupt terminate. */
    private static boolean abruptTerminate = false;

    /**
     * Initialise the simulation for stand alone simulations. This function
     * should be called at the start of the simulation.
     */
    protected static void initialize() {
	Log.logger.info("[" + Double.toString(CloudSim.clock()) + "] " + "Initialising...");
	entities = new ArrayList<SimEntity>();
	entitiesByName = new LinkedHashMap<String, SimEntity>();
	future = new HeapEventList();
	clock = 0;
	running = false;
    }

    // Public access methods

    /**
     * Get the current simulation time.
     * 
     * @return the simulation time
     */
    public static final long clock() {
	return clock;
    }

    /**
     * Get the current number of entities in the simulation.
     * 
     * @return The number of entities
     */
    public static int getNumEntities() {
	return entities.size();
    }

    /**
     * Get the entity with a given id.
     * 
     * @param id
     *        the entity's unique id number
     * @return The entity, or if it could not be found
     */
    public static SimEntity getEntity(int id) {
	return entities.get(id);
    }

    /**
     * Get the entity with a given name.
     * 
     * @param name
     *        The entity's name
     * @return The entity
     */
    public static SimEntity getEntity(String name) {
	return entitiesByName.get(name);
    }

    /**
     * Get the id of an entity with a given name.
     * 
     * @param name
     *        The entity's name
     * @return The entity's unique id number
     */
    public static int getEntityId(String name) {
	SimEntity obj = entitiesByName.get(name);
	if (obj == null) {
	    return NOT_FOUND;
	}
	return obj.getId();
    }

    /**
     * Gets name of the entity given its entity ID.
     * 
     * @param entityID
     *        the entity ID
     * @return the Entity name or if this object does not have one
     * @pre entityID > 0
     * @post $none
     */
    public static String getEntityName(int entityID) {
	try {
	    return getEntity(entityID).getName();
	} catch (IllegalArgumentException e) {
	    return null;
	} catch (Exception e) {
	    return null;
	}
    }

    /**
     * Gets name of the entity given its entity ID.
     * 
     * @param entityID
     *        the entity ID
     * @return the Entity name or if this object does not have one
     * @pre entityID > 0
     * @post $none
     */
    public static String getEntityName(Integer entityID) {
	if (entityID != null) {
	    return getEntityName(entityID.intValue());
	}
	return null;
    }

    /**
     * Returns a list of entities created for the simulation.
     * 
     * @return the entity iterator
     */
    public static List<SimEntity> getEntityList() {
	// create a new list to prevent the user from changing
	// the list of entities used by Simulation
	List<SimEntity> list = new ArrayList<SimEntity>(entities);
	return list;
    }

    // Public update methods

    /**
     * Add a new entity to the simulation. This is present for compatibility
     * with existing simulations since entities are automatically added to the
     * simulation upon instantiation.
     * 
     * @param e
     *        The new entity
     */
    public static void addEntity(SimEntity e) {
	SimEvent evt;
	if (running) {
	    // Post an event to make this entity
	    evt = new SimEvent(SimEvent.CREATE, clock, 1, 0, null, e);
	    future.put(evt);
	}
	if (e.getId() == -1) { // Only add once!
	    int id = entities.size();
	    e.setId(id);
	    entities.add(e);
	    entitiesByName.put(e.getName(), e);
	}
    }

    /**
     * Internal method used to add a new entity to the simulation when the
     * simulation is running. It should <b>not</b> be called from user
     * simulations.
     * 
     * @param e
     *        The new entity
     */
    protected static void addEntityDynamically(SimEntity e) {
	if (e == null) {
	    throw new IllegalArgumentException("Adding null entity.");
	}
	Log.logger.info(Log.clock() + "Adding: " + e.getName());
	e.startEntity();
    }

    /**
     * Internal method used to run one tick of the simulation. This method
     * should <b>not</b> be called in simulations.
     * 
     * @return true, if successful otherwise
     */
    public static boolean runClockTick() {
	boolean queue_empty;

	for (SimEntity ent : entities) {
	    if (ent.getState() == SimEntity.RUNNABLE) {
		ent.run();
	    }
	}

	// If there are more future events then deal with them
	if (future.size() > 0) {
	    queue_empty = false;
	    SimEvent first = future.take();
	    processEvent(first);

	    // Check if next events are at same time...
	    boolean tryMore = future.hasMore(first.getTime());
	    while (tryMore) {
		SimEvent next = future.take();
		processEvent(next);
		tryMore = future.hasMore(first.getTime());
	    }
	} else {
	    queue_empty = true;
	    running = false;
	    Log.logger.info(Log.clock() + "Simulation: No more future events");
	}

	return queue_empty;
    }

    /**
     * Internal method used to stop the simulation. This method should
     * <b>not</b> be used directly.
     */
    public static void runStop() {
	Log.logger.info(Log.clock() + "Simulation completed.");
    }

    /**
     * Used to hold an entity for some time.
     * 
     * @param src
     *        the src
     * @param delay
     *        the delay
     */
    public static void hold(int src, long delay) {
	SimEvent e = new SimEvent(SimEvent.HOLD_DONE, clock + delay, src);
	future.put(e);
	entities.get(src).setState(SimEntity.HOLDING);
    }

    /**
     * Used to pause an entity for some time.
     * 
     * @param src
     *        the src
     * @param delay
     *        the delay
     */
    public static void pause(int src, long delay) {
	hold(src, delay);
    }

    /**
     * Used to send an event from one entity to another.
     * 
     * @param src
     *        the src
     * @param dest
     *        the dest
     * @param delay
     *        the delay
     * @param tag
     *        the tag
     * @param data
     *        the data
     */
    public static void send(int src, int dest, long delay, EventTag tag, Object data) {

	if (!running()) {
	    System.out.println("CLOUDSIM NOT RUNNING");
	    return;
	}

	if (delay < 0 || delay > 9999999999999L) {
	    throw new IllegalArgumentException("Send delay can't be negative or too large: "
		    + delay);
	}

	SimEvent e = SimEvent.newEvent(SimEvent.SEND, clock + delay, src, dest, tag, data);
	future.put(e);
	if (Log.logger.isLoggable(Level.FINER)) {
	    Log.logger.finer(Log.clock()
		    + "Scheduling event at time: "
		    + (clock + delay)
		    + ", tag: "
		    + tag
		    + ", Events in future: "
		    + future.size());
	}
    }

    public static void setClock(long clock) {
	CloudSim.clock = clock;
    }

    //
    // Private internal methods
    //

    /**
     * Processes an event.
     * 
     * @param e
     *        the e
     */
    private static void processEvent(SimEvent e) {
	// Update the system's clock
	if (e.getTime() < clock) {
	    throw new IllegalArgumentException("Past event detected "
		    + e.getEventTag()
		    + ", from "
		    + e.getSource()
		    + " to "
		    + e.getDestination()
		    + " at "
		    + e.getTime()
		    + " now "
		    + clock);
	}
	clock = e.getTime();

	// Ok now process it
	switch (e.getType()) {
	case SimEvent.ENULL:
	    throw new IllegalArgumentException("Event has a null type.");

	case SimEvent.CREATE:
	    SimEntity newe = (SimEntity) e.getData();
	    addEntityDynamically(newe);
	    break;

	case SimEvent.SEND:
	    // Check for matching wait
	    int dest = e.getDestination();
	    if (dest < 0) {
		throw new IllegalArgumentException("Attempt to send to a null entity detected.");
	    }
	    entities.get(dest).addEvent(e);
	    break;

	case SimEvent.HOLD_DONE:
	    int src = e.getSource();
	    if (src < 0) {
		throw new IllegalArgumentException("Null entity holding.");
	    }
	    entities.get(src).setState(SimEntity.RUNNABLE);
	    break;

	default:
	    break;
	}
    }

    /**
     * Internal method used to start the simulation. This method should
     * <b>not</b> be used by user simulations.
     */
    public static void runStart() {
	running = true;
	// Start all the entities
	for (SimEntity ent : entities) {
	    ent.startEntity();
	    Log.logger.info("["
		    + Double.toString(CloudSim.clock())
		    + "] "
		    + "Entity "
		    + ent.getId()
		    + " "
		    + ent.getName()
		    + " is starting");
	}

	Log.logger.info(Log.clock() + entities.size() + " Entities started.");
    }

    /**
     * Check if the simulation is still running. This method should be used by
     * entities to check if they should continue executing.
     * 
     * @return if the simulation is still running, otherwise
     */
    public final static boolean running() {
	return running;
    }

    /**
     * This method is called if one wants to pause the simulation.
     * 
     * @return true, if successful otherwise.
     */
    public static boolean pauseSimulation() {
	paused = true;
	return paused;
    }

    /**
     * This method is called if one wants to pause the simulation at a given
     * time.
     * 
     * @param time
     *        the time at which the simulation has to be paused
     * @return true, if successful otherwise.
     */
    public static boolean pauseSimulation(long time) {
	if (time <= clock) {
	    return false;
	} else {
	    pauseAt = time;
	}
	return true;
    }

    /**
     * This method is called if one wants to resume the simulation that has
     * previously been paused.
     * 
     * @return if the simulation has been restarted or or otherwise.
     */
    public static boolean resumeSimulation() {
	paused = false;

	if (pauseAt <= clock) {
	    pauseAt = -1;
	}

	return !paused;
    }

    /**
     * Start the simulation running. This should be called after all the
     * entities have been setup and added, and their ports linked.
     * 
     * @return the double last clock value
     */
    public static long run() {
	if (!running) {
	    runStart();
	}
	while (true) {
	    if (runClockTick() || abruptTerminate) {
		break;
	    }

	    if (pauseAt != -1
		    && (future.size() > 0 && clock <= pauseAt && pauseAt <= future.timeOfFirst() || future
			.size() == 0 && pauseAt <= clock)) {
		pauseSimulation();
		clock = pauseAt;
	    }

	    while (paused) {
		try {
		    Thread.sleep(100);
		} catch (InterruptedException e) {
		    e.printStackTrace();
		}
	    }
	}

	finishSimulation();
	runStop();

	return clock;
    }

    /**
     * Internal method that allows the entities to terminate. This method should
     * <b>not</b> be used in user simulations.
     */
    public static void finishSimulation() {
	// Allow all entities to exit their body method
	if (!abruptTerminate) {
	    for (SimEntity ent : entities) {
		if (ent.getState() != SimEntity.FINISHED) {
		    ent.run();
		}
	    }
	}

	for (SimEntity ent : entities) {
	    ent.shutdownEntity();
	}

	// reset all static variables
	// Private data members
	entities = null;
	entitiesByName = null;
	future = null;
	clock = 0L;
	running = false;

	paused = false;
	pauseAt = -1;
	abruptTerminate = false;
    }

    /**
     * Abruptally terminate.
     */
    public static void abruptallyTerminate() {
	abruptTerminate = true;
    }

    /**
     * Checks if is paused.
     * 
     * @return true, if is paused
     */
    public static boolean isPaused() {
	return paused;
    }

}
