package org.cloudbus.spotsim.broker;

import java.util.Deque;
import java.util.LinkedList;

/**
 * 
 * A bounded deque that discards elements from the opposite end of insertion
 * when it's capacity has been reached
 * 
 * @author William Voorsluys - williamvoor@gmail.com
 * 
 * @param <E>
 *        type of elements in this deque
 */
public class BoundedNonBlockingDeque<E> extends LinkedList<E> implements Deque<E> {

    private static final long serialVersionUID = 1L;

    private final int capacity;

    public BoundedNonBlockingDeque(final int capacity) {
	if (capacity <= 0) {
	    throw new IllegalArgumentException("Deque capacity must be greater than zero: "
		    + capacity);
	}
	this.capacity = capacity;
    }

    @Override
    public void addFirst(final E e) {
	if (this.remainingCapacity() == 0) {
	    this.removeLast();
	}
	super.addFirst(e);
    }

    @Override
    public void addLast(final E e) {
	if (this.remainingCapacity() == 0) {
	    this.removeFirst();
	}
	super.addLast(e);
    }

    @Override
    public boolean offerFirst(final E e) {
	this.addFirst(e);
	return true;
    }

    @Override
    public boolean offerLast(final E e) {
	this.addFirst(e);
	return true;
    }

    @Override
    public void push(final E e) {
	this.addFirst(e);
    }

    public int remainingCapacity() {
	return this.capacity - this.size();
    }

}
