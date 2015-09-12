package alt.termos.emulation;

public interface BasicTree<K, V> {

	V get(K key);

	V put(K key, V value);
	
	V remove(K key);
	
	K getMinKey();
	
	K getMaxKey();
	
	Entry<K, V> removeFirst();

	Entry<K, V> removeLast();
	
	boolean isEmpty();
	
	int size();
	
	void clear();
	
	void verify();
	
	public interface Entry<K, V> {
		
		K getKey();
		
		V getValue();
		
	}
	
}
