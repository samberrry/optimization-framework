package org.cloudbus.utils.data.enummaps;

import java.util.EnumMap;

public class QuadEnumMap<K1 extends Enum<K1>, K2 extends Enum<K2>, K3 extends Enum<K3>, K4 extends Enum<K4>, V>
	extends TripleEnumMap<K1, K2, K3, EnumMap<K4, V>> {

    private int size = 0;

    public QuadEnumMap() {
    }

    public boolean containsKeys(K1 key1, K2 key2, K3 key3, K4 key4) {
	if (!super.containsKeys(key1, key2, key3)) {
	    return false;
	}

	if (key4 == null) {
	    return false;
	}

	return super.get(key1, key2, key3).containsKey(key4);
    }

    public void put(K1 key1, K2 key2, K3 key3, K4 key4, V value) {

	if (!validKeys(key1, key2, key3, key4)) {
	    throw new IllegalArgumentException("Invalid keys");
	}

	EnumMap<K4, V> map2 = null;
	if (!super.containsKeys(key1, key2, key3)) {
	    map2 = new EnumMap<K4, V>(key4.getDeclaringClass());
	    super.put(key1, key2, key3, map2);
	} else {
	    map2 = super.get(key1, key2, key3);
	}
	this.size++;
	map2.put(key4, value);
    }

    public V get(K1 key1, K2 key2, K3 key3, K4 key4) {

	if (!validKeys(key1, key2, key3, key4)) {
	    throw new IllegalArgumentException("Invalid keys");
	}

	if (!super.containsKeys(key1, key2, key3)) {
	    return null;
	}
	return super.get(key1, key2, key3).get(key4);
    }

    public V remove(K1 key1, K2 key2, K3 key3, K4 key4) {

	if (!validKeys(key1, key2, key3, key4)) {
	    throw new IllegalArgumentException("Invalid keys");
	}

	if (!super.containsKeys(key1, key2, key3)) {
	    return null;
	}

	EnumMap<K4, V> removed = super.get(key1, key2, key3);
	V val = removed.remove(key4);
	if (val != null) {
	    if (removed.isEmpty()) {
		super.remove(key1, key2, key3);
	    }
	    this.size--;
	    return val;
	}
	return null;
    }

    protected boolean validKeys(K1 key1, K2 key2, K3 key3, K4 key4) {
	return super.validKeys(key1, key2, key3) && key4 != null;
    }
}
