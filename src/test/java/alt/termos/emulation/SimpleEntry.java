package alt.termos.emulation;

import alt.termos.emulation.Tree.Entry;

public final class SimpleEntry<K, V> implements Entry<K, V> {

	final K key;
	final V value;

	public SimpleEntry(K key, V value) {
		this.key = key;
		this.value = value;
	}
	
	public static <K, V> SimpleEntry<K, V> newEntry(K key, V value) {
		return new SimpleEntry<K, V>(key, value);
	}

	@Override
	public K getKey() {
		return key;
	}

	@Override
	public V getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "SimpleEntry [key=" + key + ", value=" + value + "]";
	}
	
}
