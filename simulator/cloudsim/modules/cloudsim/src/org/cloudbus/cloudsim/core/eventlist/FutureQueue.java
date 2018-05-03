/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2010, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.core.eventlist;

import java.util.Iterator;
import java.util.TreeSet;

import org.cloudbus.cloudsim.core.SimEvent;

/**
 * This class implements the future event queue used by {@link Simulation}. The
 * event queue uses a {@link TreeSet} in order to store the events.
 * 
 * @author Marcos Dias de Assuncao
 * @since CloudSim Toolkit 1.0
 * 
 * @see Simulation
 * @see java.util.TreeSet
 */
public class FutureQueue extends AbstractEventList implements EventList {

    /** The sorted set. */
    private final TreeSet<SimEvent> sortedSet = new TreeSet<SimEvent>();

    @Override
    public boolean insert(SimEvent newEvent) {
	return this.sortedSet.add(newEvent);
    }

    @Override
    public int size() {
	return this.sortedSet.size();
    }

    @Override
    public boolean remove(SimEvent event) {
	return this.sortedSet.remove(event);
    }

    @Override
    public void clear() {
	this.sortedSet.clear();
    }

    @Override
    public boolean hasMore(long time) {
	if (!this.sortedSet.isEmpty()) {
	    SimEvent first = this.sortedSet.first();
	    return first.getTime() == time;
	}
	return false;
    }

    @Override
    public SimEvent take() {
	return this.sortedSet.pollFirst();
    }

    @Override
    public long timeOfFirst() {
	return this.sortedSet.first().getTime();
    }

    @Override
    public SimEvent get() {
	return this.sortedSet.first();
    }

    @Override
    public Iterator<SimEvent> iterator() {
	return this.sortedSet.iterator();
    }
}