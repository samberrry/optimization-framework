package org.cloudbus.cloudsim.core.eventlist;

import java.util.Iterator;

import org.cloudbus.cloudsim.core.SimEvent;

public interface EventList {

	/**
	 * Add a new event to the queue. Adding a new event to the queue preserves
	 * the temporal order of the events in the queue.
	 * 
	 * @param newEvent
	 *            The event to be put in the queue.
	 * @return
	 */
	boolean put(SimEvent newEvent);

	/**
	 * Returns the size of this event queue.
	 * 
	 * @return the size
	 */
	int size();

	/**
	 * Removes the event from the queue.
	 * 
	 * @param event
	 *            the event
	 * 
	 * @return true, if successful
	 */
	boolean remove(SimEvent event);

	/**
	 * Clears the queue.
	 */
	void clear();

	boolean hasMore(long time);

	SimEvent take();

	SimEvent get();

	long timeOfFirst();

	Iterator<SimEvent> iterator();
}