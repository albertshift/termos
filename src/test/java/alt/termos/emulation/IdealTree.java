package alt.termos.emulation;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class IdealTree<K, V> {

	private final int branchingFactor;
	private final Comparator<? super K> keyComparator;
	private final EntryComparator entryComparator = new EntryComparator();
	
	private Node root;
	
	
	private int MAX_CACHE_SIZE = 1;
	private int MEM_LOAD_TIME = 0;
	private int DISK_LOAD_TIME = 1000;
	
	
	private Map<Node, Integer> cache = new HashMap<Node, Integer>();
	private long pageAccess;
	
	public void access(Node node) {
		Integer hits = cache.get(node);
		if (hits != null) {
			cache.put(node, hits.intValue() + 1);
			pageAccess += MEM_LOAD_TIME;
			return;
		}
		
		if (cache.size() == MAX_CACHE_SIZE) {
		
			// evict
			Map.Entry<Node, Integer> min = null;
			for (Map.Entry<Node, Integer> e : cache.entrySet()) {
				if (min == null) {
					min = e;
				}
				else if (min.getValue().intValue()  > e.getValue().intValue()) {
					min = e;
				}
			}
			
			cache.remove(min.getKey());
		}
		
		cache.put(node, 1);
		pageAccess += DISK_LOAD_TIME;
		
	}
	
	public IdealTree(int branchingFactor, final Comparator<? super K> keyComparator) {
		notNull(keyComparator, "keyComparator");
		if (branchingFactor < 2) {
			throw new IllegalArgumentException("illegal branchingFactor " + branchingFactor);
		}
		this.branchingFactor = branchingFactor;
		this.keyComparator = keyComparator;
		this.root = new Node();
	}
	
	private void notNull(Object obj, String argumentName) {
		if (obj == null) {
			throw new IllegalArgumentException("empty argument " + argumentName);
		}
	}
	
	public class Node {
		
		private final CounterEntry<K, V>[] entries = (CounterEntry<K, V>[]) Array.newInstance(CounterEntry.class, 2 * branchingFactor);
	
		private final Node[] childs = (Node[]) Array.newInstance(Node.class, 2 * branchingFactor + 1);
		
		private int length;
		
		private int binarySearch(CounterEntry<K, V>[] a, int fromIndex, int toIndex,
				K key) {

			int low = fromIndex;
			int high = toIndex - 1;

			while (low <= high) {
				int mid = (low >>> 1) + (high >>> 1);
				mid += ((low & 1) + (high & 1)) >>> 1;
				
				//int mid = (low + high) >>> 1;
				CounterEntry<K, V> midVal = a[mid];
				int cmp = keyComparator.compare(midVal.getKey(), key);

				if (cmp < 0) {
					low = mid + 1;
				}
				else if (cmp > 0) {
					high = mid - 1;
				}
				else {
					return mid; // key found
				}
			}
			return -(low + 1); // key not found.
		}
		
		protected int search(K key) {
			if (length > 7) {
				// JDK has a bug in int mid = (low + high) >>> 1; - int overflow
				//return Arrays.binarySearch(entries, 0, length, new SearchE(key), entryComparator);
				return binarySearch(entries, 0, length, key);
			}
			else {
			    for (int i = 0; i != length; i++) {
			    	int c = keyComparator.compare(entries[i].getKey(), key);
					if (c == 0) {
					    return i;
					}
					else if (c > 0) {
						return -(i + 1);
					}
			    }
			    return -(length + 1);
			}
		}
		
		CounterEntry<K, V> get(K key) {
			
			access(this);
			
			int index = search(key);
			if (index >= 0) {
				return this.entries[index];
			}
			
			index = -(index + 1);
			
			Node child = this.childs[index];
			
			if (child != null) {
				return child.get(key);
			}
			
			return null;
		}
		
		void fill(List<CounterEntry<K, V>> list) {
			
			Collections.sort(list, entryComparator);
			
			if (list.size() > entries.length) {
				throw new IllegalArgumentException("invalid size");
			}
			
			for (int i = 0; i != list.size(); ++i) {
				this.entries[i] = list.get(i);
			}
			
			length = list.size();
			
		}
		
		void load(K splitKey, List<CounterEntry<K, V>> orderedList) {

			for (int i = 0; i != length; ++i) {
				
				CounterEntry<K, V> entry = entries[i];
				
				List<CounterEntry<K, V>> elements = getTopLessThanKey(entry.getKey(), orderedList, branchingFactor * 2);
				
				if (!elements.isEmpty()) {
					Node child = new Node();
					child = new Node();
					child.fill(elements);
					child.load(entry.getKey(), orderedList);
					this.childs[i] = child;
				}
				
			}
			
			List<CounterEntry<K, V>> elements = getTopLessThanKey(splitKey, orderedList, branchingFactor * 2);
			if (!elements.isEmpty()) {
				Node child = new Node();
				child.fill(elements);
				child.load(splitKey, orderedList);
				this.childs[length] = child;
			}
			
		}
		
		int getChildPages() {
			
			int pages = 0;
			for (int i = 0; i != length+1; ++i) {
				
				Node child = this.childs[i];
				
				if (child != null) {
					pages += 1 + child.getChildPages();
				}
				
			}
			
			return pages;
		}
		
		void print(String prefix) {
			
			System.out.println(prefix + "Node " + this.hashCode());
			
			for (int i = 0; i != length; ++i) {
				
				Node child = this.childs[i];
				if (child != null) {
					child.print(prefix + "  ");
				}
				
				System.out.println(prefix + "  " + this.entries[i]);
				
			}
			
			Node child = this.childs[length];
			if (child != null) {
				child.print(prefix + "  ");
			}
			
		}
	}
	
	public void print() {
		this.root.print("");
	}
	
	public void resetPageAccess() {
		this.pageAccess = 0;
	}
	
	public long getPageAccess() {
		return this.pageAccess;
	}
	
	public int getTotalPages() {
		return 1 + this.root.getChildPages();
	}
	
	public CounterEntry<K, V> get(K key) {
		return this.root.get(key);
	}
	
	public void load(List<CounterEntry<K, V>> orderedList) {

		List<CounterEntry<K, V>> root = getTopLessThanKey(null, orderedList, branchingFactor * 2);

		this.root = new Node();
		this.root.fill(root);
		this.root.load(null, orderedList);


	}

	public List<CounterEntry<K, V>> getTopLessThanKey(K key, List<CounterEntry<K, V>> orderedList, int num) {
		
		List<CounterEntry<K, V>> result = new ArrayList<CounterEntry<K, V>>();
		
		int fromIndex = 0;
		
		for (int i = 0; i != num; ++i) {
			
			int index = findLessThanKeyEntry(key, orderedList, fromIndex);
			
			if (index == -1) {
				break;
			}
			
			result.add(orderedList.remove(index));
			
			fromIndex = index;
			
		}
		
		return result;
		
	}
	
	public int findLessThanKeyEntry(K key, List<CounterEntry<K, V>> orderedList, int fromIndex) {
		
		if (fromIndex >= orderedList.size()) {
			return -1;
		}
		
		for (int i = fromIndex; i != orderedList.size(); ++i) {
			
			if (key == null) {
				return i;
			}
			
			CounterEntry<K, V> entry = orderedList.get(i);
			
			if (keyComparator.compare(entry.getKey(), key) == -1) {
				return i;
			}
			
		}
		
		return -1;
		
	}
	
	public class EntryComparator implements Comparator<CounterEntry<K,V>> {

		@Override
		public int compare(CounterEntry<K, V> o1, CounterEntry<K, V> o2) {
			return keyComparator.compare(o1.getKey(), o2.getKey());
		}
		
	}
}
