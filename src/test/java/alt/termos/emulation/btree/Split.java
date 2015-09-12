package alt.termos.emulation.btree;

import alt.termos.emulation.btree.Tree.Entry;

/**
 * Split object
 * 
 * 
 * @author Albert Shift
 *
 * @param <K> Key type
 * @param <V> Value type
 */

public final class Split<K, V> {
	
	private final Entry<K, V> entry;
	private final AbstractNode<K, V> greater;
	
	public Split(Entry<K, V> entry, AbstractNode<K, V> greater) {
		this.entry = entry;
		this.greater = greater;
	}

	public Entry<K, V> getEntry() {
		return entry;
	}

	public AbstractNode<K, V> getGreater() {
		return greater;
	}

}