/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2010, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.core;

import org.cloudbus.cloudsim.util.ArraySmallObjectCache;
import org.cloudbus.cloudsim.util.SmallObjectCache;

/**
 * This class represents a simulation event which is passed between the entities
 * in the simulation.
 * 
 * @author Costas Simatos
 * 
 * @see Simulation
 * @see SimEntity
 */
public class SimEvent implements Cloneable, Comparable<SimEvent> {
    private int etype; // internal event type

    private long time; // time at which event should occur

    private double endWaitingTime; // time that the event was removed from the

    // queue for service
    private int src; // id of entity who scheduled event

    private int dest; // id of entity event will be sent to

    private EventTag tag; // the user defined type of the event

    private Object payload; // any data the event is carrying

    private long serial = -1;

    // Internal event types
    public static final int ENULL = 0;

    public static final int SEND = 1;

    public static final int HOLD_DONE = 2;

    public static final int CREATE = 3;

    private static SmallObjectCache<SimEvent> cache = new ArraySmallObjectCache<SimEvent>();

    private static boolean cacheObjects = true;

    /**
     * Create a blank event.
     */
    public SimEvent() {
	this.etype = ENULL;
	this.time = -1L;
	this.endWaitingTime = -1.0;
	this.src = -1;
	this.dest = -1;
	this.payload = null;
	this.tag = null;
    }

    // ------------------- PACKAGE LEVEL METHODS --------------------------

    static SimEvent newEvent(int evtype, long time, int src, int dest, EventTag tag, Object payload) {
	if (!cacheObjects || cache.isEmpty()) {
	    return new SimEvent(evtype, time, src, dest, tag, payload);
	}

	final SimEvent simEvent = cache.obtain();
	simEvent.etype = evtype;
	simEvent.time = time;
	simEvent.src = src;
	simEvent.dest = dest;
	simEvent.payload = payload;
	simEvent.tag = tag;
	return simEvent;
    }

    SimEvent(int evtype, long time, int src, int dest, EventTag tag, Object edata) {
	this.etype = evtype;
	this.time = time;
	this.src = src;
	this.dest = dest;
	this.tag = tag;
	this.payload = edata;
    }

    static SimEvent newEvent(int evtype, long time, int src) {
	if (!cacheObjects || cache.isEmpty()) {
	    return new SimEvent(evtype, time, src);
	}

	final SimEvent simEvent = cache.obtain();
	simEvent.etype = evtype;
	simEvent.time = time;
	simEvent.src = src;
	simEvent.dest = -1;
	simEvent.payload = null;
	simEvent.tag = null;
	return simEvent;
    }

    SimEvent(int evtype, long time, int src) {
	this.etype = evtype;
	this.time = time;
	this.src = src;
	this.dest = -1;
	this.payload = null;
	this.tag = null;
    }

    public static void release(SimEvent ev) {
	if (cacheObjects && !cache.isFull()) {
	    ev.payload = null;
	    cache.release(ev);
	}
    }

    public void setSerial(long serial) {
	this.serial = serial;
    }

    // Used to set the time at which this event finished waiting in the event
    // queue. This is used for statistical purposes.
    protected void setEndWaitingTime(double end_waiting_time) {
	this.endWaitingTime = end_waiting_time;
    }

    @Override
    public String toString() {
	return "Event tag = "
		+ this.tag
		+ " source = "
		+ this.src
		+ " destination = "
		+ this.dest
		+ " time = "
		+ this.time;
    }

    // The internal type
    public int getType() {
	return this.etype;
    }

    // ------------------- PUBLIC METHODS --------------------------

    /**
     * @see Comparable#compareTo(Object)
     */
    @Override
    public int compareTo(SimEvent event) {
	if (event == null) {
	    return 1;
	} else if (this.time < event.time) {
	    return -1;
	} else if (this.time > event.time) {
	    return 1;
	} else if (this.serial < event.serial) {
	    return -1;
	} else if (this == event) {
	    return 0;
	} else {
	    return 1;
	}
    }

    /**
     * Get the unique id number of the entity which received this event.
     * 
     * @return the id number
     */
    public int getDestination() {
	return this.dest;
    }

    /**
     * Get the unique id number of the entity which scheduled this event.
     * 
     * @return the id number
     */
    public int getSource() {
	return this.src;
    }

    /**
     * Get the simulation time that this event was scheduled.
     * 
     * @return The simulation time
     */
    public long getTime() {
	return this.time;
    }

    /**
     * Get the simulation time that this event was removed from the queue for
     * service.
     * 
     * @return The simulation time
     */
    public double endWaitingTime() {
	return this.endWaitingTime;
    }

    /**
     * Get the user-defined tag of this event
     * 
     * @return The tag
     */
    public EventTag type() {
	return this.tag;
    }

    /**
     * Get the unique id number of the entity which scheduled this event.
     * 
     * @return the id number
     */
    public int scheduledBy() {
	return this.src;
    }

    /**
     * Get the user-defined tag of this event.
     * 
     * @return The tag
     */
    public EventTag getTag() {
	return this.tag;
    }

    /**
     * Get the data passed in this event.
     * 
     * @return A reference to the data
     */
    public Object getData() {
	return this.payload;
    }

    /**
     * Create an exact copy of this event.
     * 
     * @return The event's copy
     */
    @Override
    public Object clone() {
	return new SimEvent(this.etype, this.time, this.src, this.dest, this.tag, this.payload);
    }

    /**
     * Set the source entity of this event.
     * 
     * @param s
     *        The unique id number of the entity
     */
    public void setSource(int s) {
	this.src = s;
    }

    /**
     * Set the destination entity of this event.
     * 
     * @param d
     *        The unique id number of the entity
     */
    public void setDestination(int d) {
	this.dest = d;
    }

    public EventTag getEventTag() {
	return this.tag;
    }
}
