package org.cloudbus.utils.data.enummaps;

public interface DoubleMap<K1, K2, V> {

    int size();

    boolean isEmpty();

    boolean containsKeys(K1 key1, K2 key2);

    boolean containsValue(Object value);

    V get(K1 key1, K2 key2);

    // Modification Operations

    V put(K1 key1, K2 key2, V value);

    V remove(K1 key1, K2 key2);

    // Bulk Operations

    void putAll(DoubleMap<? extends K1, ? extends K2, ? extends V> m);

    void clear();

    // Comparison and hashing

    boolean equals(Object o);

    int hashCode();
}
