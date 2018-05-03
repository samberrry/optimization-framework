package org.cloudbus.cloudsim.util;


public interface SmallObjectCache<T> {

    boolean isEmpty();

    boolean isFull();

    T obtain();

    void release(T obj);
}