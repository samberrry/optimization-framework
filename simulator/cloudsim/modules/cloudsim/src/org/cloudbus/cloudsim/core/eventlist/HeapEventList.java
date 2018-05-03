package org.cloudbus.cloudsim.core.eventlist;

import java.util.Iterator;
import java.util.PriorityQueue;

import org.cloudbus.cloudsim.core.SimEvent;

/**
 * 
 * 
 * 
 * @author William Voorsluys
 * 
 */
public class HeapEventList extends AbstractEventList implements EventList {

    private PriorityQueue<SimEvent> queue = new PriorityQueue<SimEvent>();

    @Override
    public boolean insert(SimEvent newEvent) {
	return this.queue.add(newEvent);
    }

    @Override
    public int size() {
	return this.queue.size();
    }

    @Override
    public boolean remove(SimEvent event) {
	return this.queue.remove(event);
    }

    @Override
    public void clear() {
	this.queue.clear();
    }

    @Override
    public boolean hasMore(long time) {
	if (this.queue.isEmpty()) {
	    return false;
	}
	return this.queue.peek().getTime() == time;
    }

    @Override
    public SimEvent take() {
	return this.queue.poll();
    }

    @Override
    public long timeOfFirst() {
	return this.queue.peek().getTime();
    }

    @Override
    public SimEvent get() {
	return this.queue.poll();
    }

    @Override
    public Iterator<SimEvent> iterator() {
	return this.queue.iterator();
    }
}
