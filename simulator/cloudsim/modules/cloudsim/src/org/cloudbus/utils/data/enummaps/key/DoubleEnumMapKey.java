package org.cloudbus.utils.data.enummaps.key;

import org.cloudbus.utils.data.enummaps.DoubleMap;

public class DoubleEnumMapKey<K1 extends Enum<K1>, K2 extends Enum<K2>, V> implements
	DoubleMap<K1, K2, V> {

    @Override
    public int size() {
	// TODO Auto-generated method stub
	return 0;
    }

    @Override
    public boolean isEmpty() {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public boolean containsKeys(K1 key1, K2 key2) {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public boolean containsValue(Object value) {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public V get(K1 key1, K2 key2) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public V put(K1 key1, K2 key2, V value) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public V remove(K1 key1, K2 key2) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public void putAll(DoubleMap<? extends K1, ? extends K2, ? extends V> m) {
	// TODO Auto-generated method stub

    }

    @Override
    public void clear() {
	// TODO Auto-generated method stub

    }

}
