package alt.termos.emulation;

import alt.termos.emulation.Tree.Entry;

public class CounterEntry<K, V> implements Entry<K, V> {

	final K key;
	final V value;
	int counter;

	public CounterEntry(K key, V value) {
		this.key = key;
		this.value = value;
		this.counter = 0;
	}
	
	public static <K, V> CounterEntry<K, V> newEntry(K key, V value) {
		return new CounterEntry<K, V>(key, value);
	}

	@Override
	public K getKey() {
		return key;
	}

	@Override
	public V getValue() {
		return value;
	}

	public void increment() {
		this.counter++;
	}
	
	public int getCounter() {
		return counter;
	}

	public void setCounter(int counter) {
		this.counter = counter;
	}

	@Override
	public String toString() {
		return "CounterEntry [key=" + key + ", value=" + value + ", counter="
				+ counter + "]";
	}

}
