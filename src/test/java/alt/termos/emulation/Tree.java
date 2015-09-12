package alt.termos.emulation;

public interface Tree<K, V, E extends Tree.Entry<K, V>> {

	E get(K key);
	
	E put(E entry);

	E remove(K key);
	
	E getFirstEntry();
	
	E getNextEntry(K key);
	
	E getLastEntry();
	
	E removeFirst();

	E removeLast();
	
	boolean isEmpty();
	
	int size();
	
	void clear();
	
	void verify();
	
	Iterable<E> entries();
	
	public interface Entry<K, V> {
		
		K getKey();
		
		V getValue();
		
	}
	
}
