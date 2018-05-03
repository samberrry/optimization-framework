package org.cloudbus.spotsim.pricing;

import java.util.Iterator;

public class ReadOnlyIterator<E> implements Iterator<E> {

    private final Iterator<E> it;

    public ReadOnlyIterator(final Iterator<E> it) {
	super();
	this.it = it;
    }

    @Override
    public boolean hasNext() {
	return this.it.hasNext();
    }

    @Override
    public E next() {
	return this.it.next();
    }

    @Override
    public void remove() {
	throw new UnsupportedOperationException("This iterator is read-only");
    }
}
