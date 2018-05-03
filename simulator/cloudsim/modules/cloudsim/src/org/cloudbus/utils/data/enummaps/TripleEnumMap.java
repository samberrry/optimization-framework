package org.cloudbus.utils.data.enummaps;

import java.util.EnumMap;

public class TripleEnumMap<K1 extends Enum<K1>, K2 extends Enum<K2>, K3 extends Enum<K3>, V>
	extends DoubleEnumMap<K1, K2, EnumMap<K3, V>> {

    private transient int size = 0;

    public TripleEnumMap() {
    }

    public TripleEnumMap(TripleEnumMap<K1, K2, K3, V> m) {
	super(m);
	this.size = m.size;
    }

    public boolean containsKeys(K1 key1, K2 key2, K3 key3) {

	if (!super.containsKeys(key1, key2)) {
	    return false;
	}

	if (key3 == null) {
	    return false;
	}

	return super.get(key1, key2).containsKey(key3);
    }

    public void put(K1 key1, K2 key2, K3 key3, V value) {

	if (!validKeys(key1, key2, key3)) {
	    throw new IllegalArgumentException("Invalid keys");
	}

	EnumMap<K3, V> map2 = null;
	if (!super.containsKeys(key1, key2)) {
	    map2 = new EnumMap<K3, V>(key3.getDeclaringClass());
	    super.put(key1, key2, map2);
	} else {
	    map2 = super.get(key1, key2);
	}
	this.size++;
	map2.put(key3, value);
    }

    protected boolean validKeys(K1 key1, K2 key2, K3 key3) {
	return super.validKeys(key1, key2) && key3 != null;
    }

    public V get(K1 key1, K2 key2, K3 key3) {

	if (!validKeys(key1, key2, key3)) {
	    throw new IllegalArgumentException("Invalid keys");
	}

	if (!super.containsKeys(key1, key2)) {
	    return null;
	}
	return super.get(key1, key2).get(key3);
    }

    public V remove(K1 key1, K2 key2, K3 key3) {

	if (!validKeys(key1, key2, key3)) {
	    throw new IllegalArgumentException("Invalid keys");
	}

	EnumMap<K3, V> removed = super.get(key1, key2);
	if (removed != null) {
	    V val = removed.remove(key3);
	    if (val != null) {
		if (removed.isEmpty()) {
		    super.remove(key1, key2);
		}
		this.size--;
		return val;
	    }
	}

	return null;
    }
}
