/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2010, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.core;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.NetworkTopology;
import org.cloudbus.cloudsim.core.eventlist.EventList;
import org.cloudbus.cloudsim.core.eventlist.HeapEventList;

/**
 * This class represents a simulation entity. An entity handles events and can
 * send events to other entities. When this class is extended, there are a few
 * methods that need to be implemented:
 * <ul>
 * <li> {@link #startEntity()} is invoked by the {@link Simulation} class when
 * the simulation is started. This method should be responsible for starting the
 * entity up.
 * <li> {@link #processEvent(SimEvent)} is invoked by the {@link Simulation}
 * class whenever there is an event in the deferred queue, which needs to be
 * processed by the entity.
 * <li> {@link #shutdownEntity()} is invoked by the {@link Simulation} before the
 * simulation finishes. If you want to save data in log files this is the method
 * in which the corresponding code would be placed.
 * </ul>
 * 
 * @author Marcos Dias de Assuncao
 * @since CloudSim Toolkit 1.0
 */
public abstract class SimEntity implements Cloneable {

    /** The name. */
    private String name;

    /** The id. */
    private int id;

    /** The entity's current state. */
    private int state;

    private final EventList incoming;

    /**
     * Creates a new entity.
     * 
     * @param name
     *        the name to be associated with this entity
     */
    public SimEntity(String name) {
	if (name.indexOf(" ") != -1) {
	    throw new IllegalArgumentException("Entity names can't contain spaces.");
	}
	this.name = name;
	this.id = -1;
	this.state = RUNNABLE;
	this.incoming = new HeapEventList();
	CloudSim.addEntity(this);
    }

    /**
     * Get the name of this entity.
     * 
     * @return The entity's name
     */
    public String getName() {
	return this.name;
    }

    /**
     * Get the unique id number assigned to this entity.
     * 
     * @return The id number
     */
    public int getId() {
	return this.id;
    }

    // The schedule functions

    /**
     * Send an event to another entity by id number, with data. Note that the
     * tag <code>9999</code> is reserved.
     * 
     * @param dest
     *        The unique id number of the destination entity
     * @param delay
     *        How long from the current simulation time the event should be sent
     * @param tag
     *        * An user-defined number representing the type of event.
     * @param data
     *        The data to be sent with the event.
     */
    public void schedule(int dest, long delay, EventTag tag, Object data) {
	CloudSim.send(this.id, dest, delay, tag, data);
    }

    /**
     * Send an event to another entity by id number and with <b>no</b> data.
     * Note that the tag <code>9999</code> is reserved.
     * 
     * @param dest
     *        The unique id number of the destination entity
     * @param delay
     *        How long from the current simulation time the event should be sent
     * @param tag
     *        An user-defined number representing the type of event.
     */
    public void schedule(int dest, long delay, EventTag tag) {
	schedule(dest, delay, tag, null);
    }

    /**
     * Send an event to another entity through a port with a given name, with
     * data. Note that the tag <code>9999</code> is reserved.
     * 
     * @param dest
     *        The name of the port to send the event through
     * @param delay
     *        How long from the current simulation time the event should be sent
     * @param tag
     *        An user-defined number representing the type of event.
     * @param data
     *        The data to be sent with the event.
     */
    public void schedule(String dest, long delay, EventTag tag, Object data) {
	schedule(CloudSim.getEntityId(dest), delay, tag, data);
    }

    /**
     * Send an event to another entity through a port with a given name, with
     * <b>no</b> data. Note that the tag <code>9999</code> is reserved.
     * 
     * @param dest
     *        The name of the port to send the event through
     * @param delay
     *        How long from the current simulation time the event should be sent
     * @param tag
     *        An user-defined number representing the type of event.
     */
    public void schedule(String dest, long delay, EventTag tag) {
	schedule(dest, delay, tag, null);
    }

    /**
     * Send an event to another entity by id number, with data. Note that the
     * tag <code>9999</code> is reserved.
     * 
     * @param dest
     *        The unique id number of the destination entity
     * @param tag
     *        An user-defined number representing the type of event.
     * @param data
     *        The data to be sent with the event.
     */
    public void scheduleNow(int dest, EventTag tag, Object data) {
	schedule(dest, 0, tag, data);
    }

    /**
     * Send an event to another entity by id number and with <b>no</b> data.
     * Note that the tag <code>9999</code> is reserved.
     * 
     * @param dest
     *        The unique id number of the destination entity
     * @param tag
     *        An user-defined number representing the type of event.
     */
    public void scheduleNow(int dest, EventTag tag) {
	schedule(dest, 0, tag, null);
    }

    /**
     * Send an event to another entity through a port with a given name, with
     * data. Note that the tag <code>9999</code> is reserved.
     * 
     * @param dest
     *        The name of the port to send the event through
     * @param tag
     *        An user-defined number representing the type of event.
     * @param data
     *        The data to be sent with the event.
     */
    public void scheduleNow(String dest, EventTag tag, Object data) {
	schedule(CloudSim.getEntityId(dest), 0, tag, data);
    }

    /**
     * Send an event to another entity through a port with a given name, with
     * <b>no</b> data. Note that the tag <code>9999</code> is reserved.
     * 
     * @param dest
     *        The name of the port to send the event through
     * @param tag
     *        An user-defined number representing the type of event.
     */
    public void scheduleNow(String dest, EventTag tag) {
	schedule(dest, 0, tag, null);
    }

    /**
     * Set the entity to be inactive for a time period.
     * 
     * @param delay
     *        the time period for which the entity will be inactive
     */
    public void pause(long delay) {
	if (delay < 0) {
	    throw new IllegalArgumentException("Negative delay supplied.");
	}
	if (!CloudSim.running()) {
	    return;
	}
	CloudSim.pause(this.id, delay);
    }

    /**
     * Get the first event waiting in the entity's deferred queue, or if there
     * are none, wait for an event to arrive.
     * 
     * @return the simulation event
     */
    public SimEvent getNextEvent() {
	return this.incoming.take();
    }

    /**
     * This method is invoked by the {@link Simulation} class when the
     * simulation is started. This method should be responsible for starting the
     * entity up.
     */
    public abstract void startEntity();

    /**
     * This method is invoked by the {@link Simulation} class whenever there is
     * an event in the deferred queue, which needs to be processed by the
     * entity.
     * 
     * @param ev
     *        the event to be processed by the entity
     */
    public abstract void processEvent(SimEvent ev);

    /**
     * This method is invoked by the {@link Simulation} before the simulation
     * finishes. If you want to save data in log files this is the method in
     * which the corresponding code would be placed.
     */
    public abstract void shutdownEntity();

    public void run() {
	SimEvent ev = getNextEvent();

	while (ev != null) {
	    processEvent(ev);
	    if (this.state != RUNNABLE) {
		break;
	    }
	    SimEvent.release(ev);

	    ev = getNextEvent();
	}
    }

    /**
     * Get a clone of the entity. This is used when independent replications
     * have been specified as an output analysis method. Clones or backups of
     * the entities are made in the beginning of the simulation in order to
     * reset the entities for each subsequent replication. This method should
     * not be called by the user.
     * 
     * @return A clone of the entity
     * 
     * @throws CloneNotSupportedException
     *         the clone not supported exception
     */
    @Override
    protected final Object clone() throws CloneNotSupportedException {
	SimEntity copy = (SimEntity) super.clone();
	copy.setName(this.name);
	return copy;
    }

    // Used to set a cloned entity's name
    /**
     * Sets the name.
     * 
     * @param new_name
     *        the new name
     */
    private void setName(String new_name) {
	this.name = new_name;
    }

    // --------------- PACKAGE LEVEL METHODS ------------------

    /**
     * Gets the state.
     * 
     * @return the state
     */
    protected int getState() {
	return this.state;
    }

    // The entity states
    /** The Constant RUNNABLE. */
    public static final int RUNNABLE = 0;

    /** The Constant HOLDING. */
    public static final int HOLDING = 2;

    /** The Constant FINISHED. */
    public static final int FINISHED = 3;

    /**
     * Sets the state.
     * 
     * @param state
     *        the new state
     */
    protected void setState(int state) {
	this.state = state;
    }

    /**
     * Sets the id.
     * 
     * @param id
     *        the new id
     */
    protected void setId(int id) {
	this.id = id;
    }

    // --------------- EVENT / MESSAGE SEND WITH NETWORK DELAY METHODS
    // ------------------

    /**
     * Sends an event/message to another entity by <tt>delaying</tt> the
     * simulation time from the current time, with a tag representing the event
     * type.
     * 
     * @param entityId
     *        the id number of the destination entity
     * @param delay
     *        how long from the current simulation time the event should be
     *        sent. If delay is a negative number, then it will be changed to 0
     * @param cloudSimTag
     *        an user-defined number representing the type of an event/message
     * @param data
     *        A reference to data to be sent with the event
     * 
     * @pre entityID > 0
     * @pre delay >= 0.0
     * @pre data != null
     * @post $none
     */
    protected void send(int entityId, long d, EventTag cloudSimTag, Object data) {
	if (entityId < 0) {
	    return;
	}

	long delay = d;

	// if delay is -ve, then it doesn't make sense. So resets to 0.0
	if (delay < 0) {
	    delay = 0;
	}

	if (entityId < 0) {
	    Log.logger.info("["
		    + Double.toString(CloudSim.clock())
		    + "] "
		    + getName()
		    + ".send(): Error - "
		    + "invalid entity id "
		    + entityId);
	    return;
	}

	int srcId = getId();
	if (NetworkTopology.isNetworkEnabled() && entityId != srcId) {
	    delay += getNetworkDelay(srcId, entityId);
	}

	schedule(entityId, delay, cloudSimTag, data);
    }

    /**
     * Sends an event/message to another entity by <tt>delaying</tt> the
     * simulation time from the current time, with a tag representing the event
     * type.
     * 
     * @param entityId
     *        the id number of the destination entity
     * @param delay
     *        how long from the current simulation time the event should be
     *        sent. If delay is a negative number, then it will be changed to 0
     * @param cloudSimTag
     *        an user-defined number representing the type of an event/message
     * 
     * @pre entityID > 0
     * @pre delay >= 0.0
     * @post $none
     */
    protected void send(int entityId, long delay, EventTag cloudSimTag) {
	send(entityId, delay, cloudSimTag, null);
    }

    /**
     * Sends an event/message to another entity by <tt>delaying</tt> the
     * simulation time from the current time, with a tag representing the event
     * type.
     * 
     * @param entityName
     *        the name of the destination entity
     * @param delay
     *        how long from the current simulation time the event should be
     *        sent. If delay is a negative number, then it will be changed to 0
     * @param cloudSimTag
     *        an user-defined number representing the type of an event/message
     * @param data
     *        A reference to data to be sent with the event
     * 
     * @pre entityName != null
     * @pre delay >= 0.0
     * @pre data != null
     * @post $none
     */
    protected void send(String entityName, long delay, EventTag cloudSimTag, Object data) {
	send(CloudSim.getEntityId(entityName), delay, cloudSimTag, data);
    }

    /**
     * Sends an event/message to another entity by <tt>delaying</tt> the
     * simulation time from the current time, with a tag representing the event
     * type.
     * 
     * @param entityName
     *        the name of the destination entity
     * @param delay
     *        how long from the current simulation time the event should be
     *        sent. If delay is a negative number, then it will be changed to 0
     * @param cloudSimTag
     *        an user-defined number representing the type of an event/message
     * 
     * @pre entityName != null
     * @pre delay >= 0.0
     * @post $none
     */
    protected void send(String entityName, long delay, EventTag cloudSimTag) {
	send(entityName, delay, cloudSimTag, null);
    }

    /**
     * Sends an event/message to another entity by <tt>delaying</tt> the
     * simulation time from the current time, with a tag representing the event
     * type.
     * 
     * @param entityId
     *        the id number of the destination entity
     * @param delay
     *        how long from the current simulation time the event should be
     *        sent. If delay is a negative number, then it will be changed to 0
     * @param cloudSimTag
     *        an user-defined number representing the type of an event/message
     * @param data
     *        A reference to data to be sent with the event
     * 
     * @pre entityID > 0
     * @pre delay >= 0.0
     * @pre data != null
     * @post $none
     */
    protected void sendNow(int entityId, EventTag cloudSimTag, Object data) {
	send(entityId, 0, cloudSimTag, data);
    }

    /**
     * Sends an event/message to another entity by <tt>delaying</tt> the
     * simulation time from the current time, with a tag representing the event
     * type.
     * 
     * @param entityId
     *        the id number of the destination entity
     * @param delay
     *        how long from the current simulation time the event should be
     *        sent. If delay is a negative number, then it will be changed to 0
     * @param cloudSimTag
     *        an user-defined number representing the type of an event/message
     * 
     * @pre entityID > 0
     * @pre delay >= 0.0
     * @post $none
     */
    protected void sendNow(int entityId, EventTag cloudSimTag) {
	send(entityId, 0, cloudSimTag, null);
    }

    /**
     * Sends an event/message to another entity by <tt>delaying</tt> the
     * simulation time from the current time, with a tag representing the event
     * type.
     * 
     * @param entityName
     *        the name of the destination entity
     * @param delay
     *        how long from the current simulation time the event should be
     *        sent. If delay is a negative number, then it will be changed to 0
     * @param cloudSimTag
     *        an user-defined number representing the type of an event/message
     * @param data
     *        A reference to data to be sent with the event
     * 
     * @pre entityName != null
     * @pre delay >= 0.0
     * @pre data != null
     * @post $none
     */
    protected void sendNow(String entityName, EventTag cloudSimTag, Object data) {
	send(CloudSim.getEntityId(entityName), 0, cloudSimTag, data);
    }

    /**
     * Sends an event/message to another entity by <tt>delaying</tt> the
     * simulation time from the current time, with a tag representing the event
     * type.
     * 
     * @param entityName
     *        the name of the destination entity
     * @param delay
     *        how long from the current simulation time the event should be
     *        sent. If delay is a negative number, then it will be changed to 0
     * @param cloudSimTag
     *        an user-defined number representing the type of an event/message
     * 
     * @pre entityName != null
     * @pre delay >= 0.0
     * @post $none
     */
    protected void sendNow(String entityName, EventTag cloudSimTag) {
	send(entityName, 0, cloudSimTag, null);
    }

    /**
     * Gets the network delay associated to the sent of a message from a given
     * source to a given destination.
     * 
     * @param src
     *        source of the message
     * @param dst
     *        destination of the message
     * 
     * @return delay to send a message from src to dst
     * 
     * @pre src >= 0
     * @pre dst >= 0
     */
    private long getNetworkDelay(int src, int dst) {
	if (NetworkTopology.isNetworkEnabled()) {
	    return (long) NetworkTopology.getDelay(src, dst);
	}
	return 0;
    }

    public void addEvent(SimEvent e) {
	this.incoming.put(e);
    }
}
