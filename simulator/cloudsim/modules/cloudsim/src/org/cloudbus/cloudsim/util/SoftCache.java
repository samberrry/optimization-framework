package org.cloudbus.cloudsim.util;


import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class SoftCache<T> implements SmallObjectCache<T> {

    private Set<SoftReference<T>> cache = new HashSet<>();

    private ReferenceQueue<T> queue = new ReferenceQueue<>();

    @Override
    public boolean isEmpty() {
	return this.cache.isEmpty();
    }

    @Override
    public boolean isFull() {
	return false;
    }

    @Override
    public T obtain() {
	cleanup();
	Iterator<SoftReference<T>> it = this.cache.iterator();
	while (it.hasNext()) {
	    T ret = it.next().get();
	    if (ret != null) {
		it.remove();
		return ret;
	    }
	}
	return null;
    }

    private void cleanup() {

	Reference<? extends T> ref = this.queue.poll();
	while (ref != null) {
	    this.cache.remove(ref);
	    ref = this.queue.poll();
	}
    }

    @Override
    public void release(T obj) {
	cleanup();
	this.cache.add(new SoftReference<>(obj, this.queue));
    }
}
