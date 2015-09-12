package alt.termos.emulation;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Simple implementation of the B-Tree
 * 
 * @author Albert Shift
 *
 * @param <K>
 * @param <V>
 */

public final class SimpleBTree<K, V> implements BasicTree<K, V> {

	private final Class<K> keyClass;
	private final Class<V> valueClass;
	private final int branchingFactor;
	private final Comparator<? super K> comparator;  
	
	private Node root; 
	private int size;
	private V nullKeyValue;
	
	public SimpleBTree(Class<K> keyClass, Class<V> valueClass, int branchingFactor, Comparator<? super K> comparator) {
		notNull(keyClass, "keyClass");
		notNull(valueClass, "valueClass");
		notNull(comparator, "comparator");
		if (branchingFactor < 2) {
			throw new IllegalArgumentException("illegal branchingFactor " + branchingFactor);
		}
		
		this.keyClass = keyClass;
		this.valueClass = valueClass;
		this.branchingFactor = branchingFactor;
		this.comparator = comparator;
		this.root = new LeafNode();
	}
	
	private void notNull(Object obj, String argumentName) {
		if (obj == null) {
			throw new IllegalArgumentException("empty argument " + argumentName);
		}
	}
	
	public abstract class Node {
		
		@SuppressWarnings({ "unchecked" })
		protected final K[] keys = (K[]) Array.newInstance(keyClass, 2 * branchingFactor);
		
		@SuppressWarnings({ "unchecked" })
		protected final V[] values = (V[]) Array.newInstance(valueClass, 2 * branchingFactor);
		
		protected int length;
		
		public abstract V get(K key);
		
		public abstract Object put(K key, V value); 
		
		public abstract V remove(K key);

		protected abstract K getFirstKey();

		protected abstract K getLastKey();

		protected abstract SimpleEntry removeFirst();
		
		protected abstract SimpleEntry removeLast();

		protected abstract SimpleEntry rotateClockwise(K key, V value, Node greater);

		protected abstract SimpleEntry rotateCounterclockwise(K key, V value, Node greater);
		
		public Node() {
		}
		
		public Node(Node src, int fromIndex) {
			this.length = src.length - fromIndex;
			System.arraycopy(src.keys, fromIndex, keys, 0, this.length);
			System.arraycopy(src.values, fromIndex, values, 0, this.length);
		}
		
		public void join(K key, V value, Node greater) {
			this.keys[this.length] = key;
			this.values[this.length] = value;
			System.arraycopy(greater.keys, 0, keys, this.length+1, greater.length);
			System.arraycopy(greater.values, 0, values, this.length+1, greater.length);
			this.length += 1 + greater.length;
		}
		
		protected int search(K key) {
			if (length > 7) {
				return Arrays.binarySearch(keys, 0, length, key, comparator);
			}
			else {
			    for (int i = 0; i != length; i++) {
			    	int c = comparator.compare(keys[i], key);
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
		
		protected SimpleEntry pollFirst() {
			K key = this.keys[0];
			V value = this.values[0];
			delete(0);
			return new SimpleEntry(key, value);
		}

		protected SimpleEntry pollLast() {
			K key = this.keys[this.length-1];
			V value = this.values[this.length-1];
			this.length--;
			return new SimpleEntry(key, value);			
		}

		protected void insert(int index, K key, V value) {
			if (index < length) {
				System.arraycopy(keys, index, keys, index+1, length-index);
				System.arraycopy(values, index, values, index+1, length-index);
			}
			keys[index] = key;
			values[index] = value;
			length++;
		}
		
		protected V replace(int index, V value) {
			V oldValue = values[index];
			values[index] = value;
			return oldValue;
		}
		
		protected void delete(int index) {
			int last = length - 1;
			if (index < last) {
				System.arraycopy(keys, index + 1, keys, index, last-index);
				System.arraycopy(values, index + 1, values, index, last-index);
			}
			length--;
		}
				
		protected abstract void print(String prefix);
		
		public void verify(boolean root) {
			if (length > branchingFactor * 2) {
				throw new IllegalStateException("too big length " + length + " in Node " + this);
			}
			if (!root) {
				if (length < branchingFactor) {
					throw new IllegalStateException("too low length " + length + " in Node " + this);
				}
			}
			
			K prevKey = null;
			for (int i = 0; i != length; ++i) {
				if (prevKey == null) {
					prevKey = this.keys[i];
				}
				else {
					int c = comparator.compare(prevKey, this.keys[i]);
					if (c != -1) {
						throw new IllegalStateException("Node " + this + " has not unordered keys at " + i);
					}
				}
			}
		}

	}
	
	private final class Split {
		
		final K key;
		final V value;
		final Node greater;
		
		public Split(K key, V value, Node greater) {
			this.key = key;
			this.value = value;
			this.greater = greater;
		}
		
	}
	
	private final class SimpleEntry implements Entry<K, V> {

		final K key;
		final V value;

		public SimpleEntry(K key, V value) {
			this.key = key;
			this.value = value;
		}

		@Override
		public K getKey() {
			return key;
		}

		@Override
		public V getValue() {
			return value;
		}
		
	}
	
	public final class InnerNode extends Node {
		
		@SuppressWarnings({ "unchecked" })
		private Node[] childs = (Node[]) Array.newInstance(Node.class, 2* branchingFactor + 1);

		public InnerNode(Node firstChild, InnerNode src, int fromIndex) {
			super(src, fromIndex);
			System.arraycopy(src.childs, fromIndex+1, childs, 1, this.length);
			childs[0] = firstChild;
		}

		public InnerNode(InnerNode src, int fromIndex) {
			super(src, fromIndex);
			System.arraycopy(src.childs, fromIndex, childs, 0, this.length+1);
		}

		public InnerNode(Node lesser, K key, V value, Node greater) {
			this.keys[0] = key;
			this.values[0] = value;
			this.childs[0] = lesser;
			this.childs[1] = greater;
			this.length = 1;
		}
		
		@SuppressWarnings("unchecked")
		public void join(K key, V value, Node greater) {
			if (greater instanceof SimpleBTree.InnerNode) {
				InnerNode inner = (InnerNode) greater;
				System.arraycopy(inner.childs, 0, childs, this.length+1, greater.length+1);
				super.join(key, value, greater);
			}
			else {
				this.keys[this.length] = key;
				this.values[this.length] = value;
				this.childs[this.length+1] = greater;
				this.length++;
			}
		}
		
		@Override
		public V get(K key) {
			int index = search(key);
			if (index >= 0) {
				return values[index];
			}
			index = -(index + 1);
			return this.childs[index].get(key);
		}

		@Override
		@SuppressWarnings("unchecked")
		public Object put(K key, V value) {
			
			int index = search(key);
			if (index >= 0) {
				return replace(index, value);
			}
			
			index = -(index + 1);
			
			Object result = this.childs[index].put(key, value);
			
			if (result == null) {
				return null;
			}
			
			if (result instanceof SimpleBTree.Split) {
				Split split = (Split) result;

				if (length < keys.length) {
					insertChild(index+1, split.greater);
					insert(index, split.key, split.value);
					return null;
				}
				
				return split(index, split);
				
			}
			else {
				return result;
			}

		}
		
		@Override
		public V remove(K key) {
			
			int index = search(key);
			if (index >= 0) {		
				V removedValue = this.values[index];
				removeByIndex(index);
				return removedValue;
			}
			
			index = -(index + 1);
			
			V removedValue = this.childs[index].remove(key);
			
			if (removedValue != null) {
				balance(index);
			}
			return removedValue;
		}
		
		protected boolean canJoin(Node lesserChild, Node greaterChild) {
			if (lesserChild instanceof SimpleBTree.LeafNode) {
				return lesserChild.length + greaterChild.length <= keys.length;
			}
			else {
				return lesserChild.length + greaterChild.length + 1 <= keys.length;
			}
		}
		
		protected void removeByIndex(int index) {
			
			Node lesserChild = this.childs[index];
			Node greaterChild = this.childs[index+1];

			if (canJoin(lesserChild, greaterChild)) {
				
				SimpleEntry entry = lesserChild.removeLast();
				lesserChild.join(entry.key, entry.value, greaterChild);

				deleteChild(index+1);
				delete(index);
				
				return;
			}
			
			if (lesserChild.length >= greaterChild.length) {
				
				SimpleEntry entry = lesserChild.removeLast();
				this.keys[index] = entry.key;
				this.values[index] = entry.value;
				
				balance(index);
				return;
			}
			else {
				
				SimpleEntry entry = greaterChild.removeFirst();
				this.keys[index] = entry.key;
				this.values[index] = entry.value;
				
				balance(index);
				return;
			}

		}
		
		protected void balance(int index) {
			if (index == length) {
				joinOrRotate(index-1);
			}
			else if (index == 0) {
				joinOrRotate(0);
			}
			else if (!joinOrRotate(index-1)) {
				joinOrRotate(index);
			}
		}
		
		protected boolean joinOrRotate(int index) {

			Node lesserChild = this.childs[index];
			Node greaterChild = this.childs[index+1];

			K splitKey = this.keys[index];
			V splitValue = this.values[index];
			
			if (lesserChild.length + greaterChild.length + 1 <= keys.length) {
								
				lesserChild.join(splitKey, splitValue, greaterChild);
				deleteChild(index+1);
				delete(index);
				
				return true;
			}

			if (lesserChild.length < branchingFactor && greaterChild.length > branchingFactor) {
				
				SimpleEntry entry = lesserChild.rotateCounterclockwise(splitKey, splitValue, greaterChild);
				if (entry != null) {

					this.keys[index] = entry.key;
					this.values[index] = entry.value;
					
					return true;

				}
				
			}

			if (lesserChild.length > branchingFactor && greaterChild.length < branchingFactor) {

				SimpleEntry entry = lesserChild.rotateClockwise(splitKey, splitValue, greaterChild);
				if (entry != null) {
					
					this.keys[index] = entry.key;
					this.values[index] = entry.value;
					
					return true;

				}
				
			}

			return false;
		}
		
		@Override
		protected SimpleEntry rotateClockwise(K key, V value, Node greater) {
			if (greater instanceof SimpleBTree.InnerNode) {
				
				InnerNode greaterInner = (InnerNode) greater;
				
				Node child = this.pollLastChild();
				SimpleEntry entry = this.pollLast();
				greaterInner.insertChild(0, child);
				greaterInner.insert(0, key, value);
				
				return entry;
				
			}
			return null;
		}

		@Override
		protected SimpleEntry rotateCounterclockwise(K key, V value, Node greater) {
			if (greater instanceof SimpleBTree.InnerNode) {
				
				InnerNode greaterInner = (InnerNode) greater;
				
				Node child = greaterInner.pollFirstChild();
				SimpleEntry entry = greaterInner.pollFirst();
				this.insertChild(this.length+1, child);
				this.insert(this.length, key, value);
				
				return entry;
				
			}
			return null;
		}

		protected Object split(int index, Split split) {
			
			if (index == branchingFactor) {

				InnerNode greater = new InnerNode(split.greater, this, branchingFactor);
				this.length = branchingFactor;
				return new Split(split.key, split.value, greater);
			}

			if (index < branchingFactor) {

				InnerNode greater = new InnerNode(this, branchingFactor);
				this.length = branchingFactor;
				K splitKey = keys[length-1];
				V splitValue = values[length-1];
				this.length--;
				this.insertChild(index+1, split.greater);
				this.insert(index, split.key, split.value);

				return new Split(splitKey, splitValue, greater);
			}
			
			K splitKey = keys[branchingFactor];
			V splitValue = values[branchingFactor];
			
			InnerNode greater = new InnerNode(this, branchingFactor + 1);
			this.length = branchingFactor;

			greater.insertChild(index - branchingFactor, split.greater);
			greater.insert(index - branchingFactor - 1, split.key, split.value);
			
			return new Split(splitKey, splitValue, greater);
		}

		@Override
		protected K getFirstKey() {
			return this.childs[0].getFirstKey();
		}

		@Override
		protected K getLastKey() {
			return this.childs[length].getLastKey();
		}
		
		@Override
		protected SimpleEntry removeFirst() {
			SimpleEntry entry = this.childs[0].removeFirst();
			balance(0);
			return entry;
		}

		@Override
		protected SimpleEntry removeLast() {
			SimpleEntry entry = this.childs[length].removeLast();
			balance(length);
			return entry;
		}

		protected Node pollFirstChild() {
			Node child = childs[0];
			deleteChild(0);
			return child;
		}

		protected Node pollLastChild() {
			return childs[length];
		}
		
		protected void insertChild(int childIndex, Node child) {
			
			int childLength = length+1;
			if (childIndex < childLength) {
				System.arraycopy(childs, childIndex, childs, childIndex+1, childLength-childIndex);
			}
			childs[childIndex] = child;
			
		}

		protected void deleteChild(int index) {
			int last = length + 1 - 1;
			if (index < last) {
				System.arraycopy(childs, index+1, childs, index, last-index);
			}
		}
		
		protected void print(String prefix) {
			System.out.println(prefix + "InnerNode " + SystemUtil.getHexAddress(this) + ", length=" + length);
			for (int i = 0; i != length; ++i) {
				childs[i].print(prefix + "  ");
				System.out.println(prefix + "  Entry (" + keys[i] +"," + values[i] + ")");
			}
			childs[length].print(prefix + "  ");
		}
		
		public void verify(boolean root) {
			super.verify(root);
			
			for (int i = 0; i != length; ++i) {
				Node lesserChild = this.childs[i];
				Node greaterChild = this.childs[i+1];
				
				K lesserKey = lesserChild.getLastKey();
				K key = this.keys[i];
				K greaterKey = greaterChild.getFirstKey();
				
				int c = comparator.compare(lesserKey, key);
				if (c != -1) {
					throw new IllegalStateException("invalid key in lesserChild " + lesserKey + ", current key = "+ key + ", Node = " + this);
				}
				
				c = comparator.compare(key, greaterKey);
				if (c != -1) {
					throw new IllegalStateException("invalid key in greaterChild " + greaterKey + ", current key = "+ key + ", Node = " + this);
				}
			}
			
			for (int i = 0; i != length + 1; ++i) {
				this.childs[i].verify(false);
			}
		}
		
	}
	
	public final class LeafNode extends Node {
		
		public LeafNode() {
		}

		public LeafNode(Node src, int fromIndex) {
			super(src, fromIndex);
		}

		@Override
		public V get(K key) {
			int index = search(key);
			if (index >= 0) {
				return values[index];
			}
			return null;
		}
		
		@Override
		public Object put(K key, V value) {
			
			if (length == 0) {
				insert(0, key, value);
				return null;
			}
			
			int index = search(key);
			if (index >= 0) {
				return replace(index, value);
			}
			
			index = -(index + 1);
			
			if (length < keys.length) {
				insert(index, key, value);
				return null;
			}
			
			return split(index, key, value);
		}

		@Override
		public V remove(K key) {
			
			int index = search(key);
			if (index >= 0) {
				V oldValue = values[index];
				delete(index);
				return oldValue;
			}
			
			return null;
		}
		
		@Override
		protected K getFirstKey() {
			return this.keys[0];
		}

		@Override
		protected K getLastKey() {
			return this.keys[this.length-1];
		}

		@Override
		protected SimpleEntry removeFirst() {
			return pollFirst();
		}

		@Override
		protected SimpleEntry removeLast() {
			return pollLast();
		}
		
		@Override
		protected SimpleEntry rotateClockwise(K key, V value, Node greater) {
			if (greater instanceof SimpleBTree.LeafNode) {
				
				LeafNode greaterLeaf = (LeafNode) greater;
				
				SimpleEntry entry = this.removeLast();
				greaterLeaf.insert(0, key, value);
				
				return entry;
			}
			return null;
		}

		@Override
		protected SimpleEntry rotateCounterclockwise(K key, V value, Node greater) {
			if (greater instanceof SimpleBTree.LeafNode) {
				
				LeafNode greaterLeaf = (LeafNode) greater;
				
				SimpleEntry entry = greaterLeaf.removeFirst();
				this.insert(this.length, key, value);
				
				return entry;
			}
			return null;
		}

		protected Split split(int index, K key, V value) {
			
			if (index <= branchingFactor) {
				
				LeafNode greater = new LeafNode(this, branchingFactor);
				this.length = branchingFactor;
				
				if (index == branchingFactor) {
					return new Split(key, value, greater);
				}
				else {
					K splitKey = keys[length-1];
					V splitValue = values[length-1];
					this.length--;
					this.insert(index, key, value);
					return new Split(splitKey, splitValue, greater);
				}
				
			}
				
			K splitKey = keys[branchingFactor];
			V splitValue = values[branchingFactor];
			
			LeafNode greater = new LeafNode(this, branchingFactor + 1);
			this.length = branchingFactor;
			
			greater.insert(index - branchingFactor - 1, key, value);
			
			return new Split(splitKey, splitValue, greater);
		}

		protected void print(String prefix) {
			System.out.println(prefix + "LeafNode " + SystemUtil.getHexAddress(this) + ", length=" + length);
			for (int i = 0; i != length; ++i) {
				System.out.println(prefix + "  Entry (" + keys[i] +"," + values[i] + ")");
			}
		}
		
	}
	
	public V get(K key) {
		if (key == null) {
			return this.nullKeyValue;
		}
		
		return root.get(key);
	}
	
	@SuppressWarnings("unchecked")
	public V put(K key, V value) {
		if (key == null) {
			return updateNullKeyValue(value);
		}
		
		if (value == null) {
			return remove(key);
		}
		
		Object result = root.put(key, value);
		if (result == null) {
			this.size++;
			return null;
		}
		if (result instanceof SimpleBTree.Split) {
			Split split = (Split) result;
			this.root = new InnerNode(root, split.key, split.value, split.greater);
			this.size++;
			return null;
		}
		else {
			return (V) result;
		}
	}
	
	public V remove(K key) {
		if (key == null) {
			return updateNullKeyValue(null);
		}
		
		V removedValue = root.remove(key);
		afterRemoveProcessing(removedValue);
		return removedValue;
	}

	@Override
	public K getMinKey() {
		if (isEmpty()) {
			return null;
		}
		return hasNullKeyValue() ? null : root.getFirstKey();
	}

	@Override
	public K getMaxKey() {
		if (isEmpty()) {
			return null;
		}
		return hasTreeEntries() ? root.getLastKey() : null;
	}
	
	@Override
	public Entry<K, V> removeFirst() {
		if (isEmpty()) {
			return null;
		}
		if (hasNullKeyValue()) {
			return new SimpleEntry(null, updateNullKeyValue(null));
		}
		SimpleEntry removedEntry = root.removeFirst();
		afterRemoveProcessing(removedEntry.getValue());
		return removedEntry;
	}

	@Override
	public Entry<K, V> removeLast() {
		if (isEmpty()) {
			return null;
		}
		if (hasTreeEntries()) {
			SimpleEntry removedEntry = root.removeLast();
			afterRemoveProcessing(removedEntry.getValue());
			return removedEntry;
		}
		return new SimpleEntry(null, updateNullKeyValue(null));
	}

	private void afterRemoveProcessing(V removedValue) {
		if (removedValue != null) {
			if (root instanceof SimpleBTree.InnerNode && root.length == 0) {
				InnerNode inner = (InnerNode) root;
				this.root = inner.childs[0];
			}
			this.size--;
		}
	}
	
	public void print() {
		root.print("");
	}

	@Override
	public boolean isEmpty() {
		return !hasNullKeyValue() && !hasTreeEntries();
	}
	
	private boolean hasTreeEntries() {
		if (root instanceof SimpleBTree.LeafNode && root.length == 0) {
			return false;
		}
		return true;
	}
	
	private boolean hasNullKeyValue() {
		return this.nullKeyValue != null;
	}
	
	private V updateNullKeyValue(V newValue) {
		V result = this.nullKeyValue;
		this.nullKeyValue = newValue;
		return result;
	}
	
	@Override
	public int size() {
		return this.size + (hasNullKeyValue() ? 1 : 0);
	}

	@Override
	public void clear() {
		this.root = new LeafNode();
		this.size = 0;
	}
	
	@Override
	public void verify() {
		this.root.verify(true);
	}
}
