package org.cloudbus.cloudsim.util;


/**
 * 
 * A cache to avoid frequently created objects to be reclaimed by the GC.
 * 
 * @author William Voorsluys
 * 
 * @param <T>
 */
public final class ArraySmallObjectCache<T> implements SmallObjectCache<T> {

    private static final int MAX_CACHE = 50000;

    private Object[] cache;

    private int cacheIndex = -1;

    private final int size;

    public ArraySmallObjectCache() {
	this(MAX_CACHE);
    }

    public ArraySmallObjectCache(int size) {
	this.size = size;
	this.cache = new Object[size];
    }

    @Override
    public final boolean isEmpty() {
	return this.cacheIndex == -1;
    }

    @Override
    public final boolean isFull() {
	return this.cacheIndex == this.size - 1;
    }

    @Override
    @SuppressWarnings("unchecked")
    public final T obtain() {
	if (isEmpty()) {
	    return null;
	}
	return (T) this.cache[this.cacheIndex--];
    }

    @Override
    public final void release(T obj) {
	if (!isFull()) {
	    this.cache[++this.cacheIndex] = obj;
	}
    }
}