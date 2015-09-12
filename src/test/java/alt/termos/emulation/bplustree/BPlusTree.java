package alt.termos.emulation.bplustree;

import java.lang.reflect.Array;
import java.util.Comparator;
import java.util.Iterator;

import alt.termos.emulation.BTree;
import alt.termos.emulation.SystemUtil;
import alt.termos.emulation.Tree;

/**
 * Simple implementation of the B+Tree
 * 
 * @author Albert Shift
 *
 * @param <K>
 * @param <V>
 */

public final class BPlusTree<K, V, E extends Tree.Entry<K, V>> implements Tree<K, V, E> {

	private final Class<E> entryClass;
	private final int branchingFactor;
	private final Comparator<? super K> keyComparator;
	
	private Node root;
	private int size;
	private E nullEntry;
	
	public BPlusTree(Class<E> entryClass, int branchingFactor, final Comparator<? super K> keyComparator) {
		notNull(entryClass, "entryClass");
		notNull(keyComparator, "keyComparator");
		if (branchingFactor < 2) {
			throw new IllegalArgumentException("illegal branchingFactor " + branchingFactor);
		}
		this.entryClass = entryClass;
		this.branchingFactor = branchingFactor;
		this.keyComparator = keyComparator;
		this.root = new LeafNode();
	}
	
	public static <K, V, E extends Tree.Entry<K, V>> BTree<K, V, E> newInstance(Class<E> entryClass, int branchingFactor, final Comparator<? super K> keyComparator) {
		return new BTree<K, V, E>(entryClass, branchingFactor, keyComparator);
	}
	
	private void notNull(Object obj, String argumentName) {
		if (obj == null) {
			throw new IllegalArgumentException("empty argument " + argumentName);
		}
	}
	
	public abstract class Node {
		
		@SuppressWarnings({ "unchecked" })
		protected final E[] entries = (E[]) Array.newInstance(entryClass, 2 * branchingFactor);

		protected int length;
		
		public abstract E get(K key);
		
		public abstract Object put(E entry); 
		
		public abstract E remove(K key);

		protected abstract E getFirstEntry();
		
		protected abstract E getNextEntry(K key);

		protected abstract E getLastEntry();

		protected abstract E removeFirst();
		
		protected abstract E removeLast();

		protected abstract E rotateClockwise(E splitEntry, Node greater);

		protected abstract E rotateCounterclockwise(E splitEntry, Node greater);
		
		protected abstract int getChildPages();
		
		public Node() {
		}
		
		public Node(Node src, int fromIndex) {
			this.length = src.length - fromIndex;
			System.arraycopy(src.entries, fromIndex, entries, 0, this.length);
		}
		
		public void join(E entry, Node greater) {
			this.entries[this.length] = entry;
			System.arraycopy(greater.entries, 0, entries, this.length+1, greater.length);
			this.length += 1 + greater.length;
		}
		
		private int binarySearch(E[] a, int fromIndex, int toIndex,
				K key) {

			int low = fromIndex;
			int high = toIndex - 1;

			while (low <= high) {
				int mid = (low >>> 1) + (high >>> 1);
				mid += ((low & 1) + (high & 1)) >>> 1;
				
				//int mid = (low + high) >>> 1;
				E midVal = a[mid];
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
		
		protected E pollFirst() {
			E entry = this.entries[0];
			delete(0);
			return entry;
		}

		protected E pollLast() {
			E entry = this.entries[this.length-1];
			this.length--;
			return entry;			
		}

		protected void insert(int index, E entry) {
			if (index < length) {
				System.arraycopy(this.entries, index, this.entries, index+1, this.length-index);
			}
			this.entries[index] = entry;
			this.length++;
		}
		
		protected E replace(int index, E newEntry) {
			E oldEntry = this.entries[index];
			this.entries[index] = newEntry;
			return oldEntry;
		}
		
		protected void delete(int index) {
			int last = this.length - 1;
			if (index < last) {
				System.arraycopy(this.entries, index + 1, this.entries, index, last-index);
			}
			this.length--;
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
					prevKey = this.entries[i].getKey();
				}
				else {
					int c = keyComparator.compare(prevKey, this.entries[i].getKey());
					if (c != -1) {
						throw new IllegalStateException("Node " + this + " has not unordered keys at " + i);
					}
				}
			}
		}

	}
	
	private final class Split {
		
		final E entry;
		final Node greater;
		
		public Split(E entry, Node greater) {
			this.entry = entry;
			this.greater = greater;
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

		public InnerNode(Node lesser, E entry, Node greater) {
			this.entries[0] = entry;
			this.childs[0] = lesser;
			this.childs[1] = greater;
			this.length = 1;
		}
		
		@Override
		protected int getChildPages() {
			
			int count = 0;
			
			for (int i = 0; i != length+1; ++i) {
				count += 1 + childs[i].getChildPages();
				
			}
			
			return count;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public void join(E entry, Node greater) {
			if (greater instanceof BPlusTree.InnerNode) {
				InnerNode inner = (InnerNode) greater;
				System.arraycopy(inner.childs, 0, childs, this.length+1, greater.length+1);
				super.join(entry, greater);
			}
			else {
				this.entries[this.length] = entry;
				this.childs[this.length+1] = greater;
				this.length++;
			}
		}
		
		@Override
		public E get(K key) {
			
			int index = search(key);
			if (index >= 0) {
				return this.entries[index];
			}
			index = -(index + 1);
			return this.childs[index].get(key);
		}

		@Override
		@SuppressWarnings("unchecked")
		public Object put(E entry) {
			
			int index = search(entry.getKey());
			if (index >= 0) {
				return replace(index, entry);
			}
			
			index = -(index + 1);
			
			Object result = this.childs[index].put(entry);
			
			if (result == null) {
				return null;
			}
			
			if (result instanceof BPlusTree.Split) {
				Split split = (Split) result;

				if (this.length < this.entries.length) {
					insertChild(index+1, split.greater);
					insert(index, split.entry);
					return null;
				}
				
				return split(index, split);
				
			}
			else {
				return result;
			}

		}
		
		@Override
		public E remove(K key) {
			
			int index = search(key);
			if (index >= 0) {		
				E removedEntry = this.entries[index];
				removeByIndex(index);
				return removedEntry;
			}
			
			index = -(index + 1);
			
			E removedEntry = this.childs[index].remove(key);
			
			if (removedEntry != null) {
				balance(index);
			}
			return removedEntry;
		}
		
		protected boolean canJoin(Node lesserChild, Node greaterChild) {
			if (lesserChild instanceof BPlusTree.LeafNode) {
				return lesserChild.length + greaterChild.length <= this.entries.length;
			}
			else {
				return lesserChild.length + greaterChild.length + 1 <= this.entries.length;
			}
		}
		
		protected void removeByIndex(int index) {
			
			Node lesserChild = this.childs[index];
			Node greaterChild = this.childs[index+1];

			if (canJoin(lesserChild, greaterChild)) {
				
				E entry = lesserChild.removeLast();
				lesserChild.join(entry, greaterChild);

				deleteChild(index+1);
				delete(index);
				
				return;
			}
			
			if (lesserChild.length >= greaterChild.length) {
				
				E entry = lesserChild.removeLast();
				this.entries[index] = entry;
				
				balance(index);
				return;
			}
			else {
				
				E entry = greaterChild.removeFirst();
				this.entries[index] = entry;
				
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

			E splitEntry = this.entries[index];
			
			if (lesserChild.length + greaterChild.length + 1 <= this.entries.length) {
								
				lesserChild.join(splitEntry, greaterChild);
				deleteChild(index+1);
				delete(index);
				
				return true;
			}

			if (lesserChild.length < branchingFactor && greaterChild.length > branchingFactor) {
				
				E entry = lesserChild.rotateCounterclockwise(splitEntry, greaterChild);
				if (entry != null) {

					this.entries[index] = entry;
					return true;

				}
				
			}

			if (lesserChild.length > branchingFactor && greaterChild.length < branchingFactor) {

				E entry = lesserChild.rotateClockwise(splitEntry, greaterChild);
				if (entry != null) {
					
					this.entries[index] = entry;
					return true;

				}
				
			}

			return false;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		protected E rotateClockwise(E splitEntry, Node greater) {
			if (greater instanceof BPlusTree.InnerNode) {
				
				InnerNode greaterInner = (InnerNode) greater;
				
				Node child = this.pollLastChild();
				E entry = this.pollLast();
				greaterInner.insertChild(0, child);
				greaterInner.insert(0, splitEntry);
				
				return entry;
				
			}
			return null;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected E rotateCounterclockwise(E splitEntry, Node greater) {
			if (greater instanceof BPlusTree.InnerNode) {
				
				InnerNode greaterInner = (InnerNode) greater;
				
				Node child = greaterInner.pollFirstChild();
				E entry = greaterInner.pollFirst();
				this.insertChild(this.length+1, child);
				this.insert(this.length, splitEntry);
				
				return entry;
				
			}
			return null;
		}

		protected Object split(int index, Split split) {
			
			if (index == branchingFactor) {

				InnerNode greater = new InnerNode(split.greater, this, branchingFactor);
				this.length = branchingFactor;
				return new Split(split.entry, greater);
			}

			if (index < branchingFactor) {

				InnerNode greater = new InnerNode(this, branchingFactor);
				this.length = branchingFactor;
				E splitEntry = pollLast();
				this.insertChild(index+1, split.greater);
				this.insert(index, split.entry);

				return new Split(splitEntry, greater);
			}
			
			E splitEntry = this.entries[branchingFactor];
			
			InnerNode greater = new InnerNode(this, branchingFactor + 1);
			this.length = branchingFactor;

			greater.insertChild(index - branchingFactor, split.greater);
			greater.insert(index - branchingFactor - 1, split.entry);
			
			return new Split(splitEntry, greater);
		}

		@Override
		protected E getFirstEntry() {
			return this.childs[0].getFirstEntry();
		}

		@Override
		protected E getNextEntry(K key) {
			
			int index = search(key);
			if (index >= 0) {
				return this.childs[index+1].getFirstEntry();
			}
			
			index = -(index + 1);
			
			E nextEntry = this.childs[index].getNextEntry(key);
			
			if (nextEntry == null && index < this.length) {
				nextEntry = this.entries[index];
			}
			
			return nextEntry;
		}
		
		@Override
		protected E getLastEntry() {
			return this.childs[length].getLastEntry();
		}
		
		@Override
		protected E removeFirst() {
			E entry = this.childs[0].removeFirst();
			balance(0);
			return entry;
		}

		@Override
		protected E removeLast() {
			E entry = this.childs[length].removeLast();
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
		
		@Override
		protected void print(String prefix) {
			System.out.println(prefix + "InnerNode " + SystemUtil.getHexAddress(this) + ", length=" + this.length);
			for (int i = 0; i != length; ++i) {
				this.childs[i].print(prefix + "  ");
				System.out.println(prefix + "  " + this.entries[i]);
			}
			this.childs[length].print(prefix + "  ");
		}
		
		@Override
		public void verify(boolean root) {
			super.verify(root);
			
			for (int i = 0; i != length; ++i) {
				Node lesserChild = this.childs[i];
				Node greaterChild = this.childs[i+1];
				
				K lesserKey = lesserChild.getLastEntry().getKey();
				K key = this.entries[i].getKey();
				K greaterKey = greaterChild.getFirstEntry().getKey();
				
				int c = keyComparator.compare(lesserKey, key);
				if (c != -1) {
					throw new IllegalStateException("invalid key in lesserChild " + lesserKey + ", current key = "+ key + ", Node = " + this);
				}
				
				c = keyComparator.compare(key, greaterKey);
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
		protected int getChildPages() {
			return 0;
		}

		@Override
		public E get(K key) {
			
			int index = search(key);
			if (index >= 0) {
				return this.entries[index];
			}
			return null;
		}
		
		@Override
		public Object put(E entry) {
			
			if (length == 0) {
				insert(0, entry);
				return null;
			}
			
			int index = search(entry.getKey());
			if (index >= 0) {
				return replace(index, entry);
			}
			
			index = -(index + 1);
			
			if (this.length < this.entries.length) {
				insert(index, entry);
				return null;
			}
			
			return split(index, entry);
		}

		@Override
		public E remove(K key) {
			
			int index = search(key);
			if (index >= 0) {
				E removedEntry = this.entries[index];
				delete(index);
				return removedEntry;
			}
			
			return null;
		}
		
		@Override
		protected E getFirstEntry() {
			return this.entries[0];
		}

		@Override
		protected E getNextEntry(K key) {
			
			int index = search(key);
			if (index >= 0) {
				return index+1 < this.length ? this.entries[index+1] : null;
			}
			
			index = -(index + 1);
			
			return index < this.length ? this.entries[index] : null;
			
		}
		
		@Override
		protected E getLastEntry() {
			return this.entries[this.length-1];
		}

		@Override
		protected E removeFirst() {
			return pollFirst();
		}

		@Override
		protected E removeLast() {
			return pollLast();
		}
		
		@SuppressWarnings("unchecked")
		@Override
		protected E rotateClockwise(E splitEntry, Node greater) {
			if (greater instanceof BPlusTree.LeafNode) {
				
				LeafNode greaterLeaf = (LeafNode) greater;
				
				E entry = this.removeLast();
				greaterLeaf.insert(0, splitEntry);
				
				return entry;
			}
			return null;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected E rotateCounterclockwise(E splitEntry, Node greater) {
			if (greater instanceof BPlusTree.LeafNode) {
				
				LeafNode greaterLeaf = (LeafNode) greater;
				
				E entry = greaterLeaf.removeFirst();
				this.insert(this.length, splitEntry);
				
				return entry;
			}
			return null;
		}

		protected Split split(int index, E splitEntry) {
			
			if (index <= branchingFactor) {
				
				LeafNode greater = new LeafNode(this, branchingFactor);
				this.length = branchingFactor;
				
				if (index == branchingFactor) {
					return new Split(splitEntry, greater);
				}
				else {
					E entry = pollLast();
					this.insert(index, splitEntry);
					return new Split(entry, greater);
				}
				
			}
				
			E entry = this.entries[branchingFactor];

			LeafNode greater = new LeafNode(this, branchingFactor + 1);
			this.length = branchingFactor;
			
			greater.insert(index - branchingFactor - 1, splitEntry);
			
			return new Split(entry, greater);
		}

		protected void print(String prefix) {
			System.out.println(prefix + "LeafNode " + SystemUtil.getHexAddress(this) + ", length=" + length);
			for (int i = 0; i != length; ++i) {
				System.out.println(prefix + "  " + this.entries[i]);
			}
		}
		
	}
	
	@Override
	public E get(K key) {
		if (key == null) {
			return this.nullEntry;
		}
		
		return root.get(key);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public E put(E entry) {
		if (entry.getKey() == null) {
			return updateNullEntry(entry);
		}
		
		if (entry.getValue() == null) {
			return remove(entry.getKey());
		}
		
		Object result = root.put(entry);
		if (result == null) {
			this.size++;
			return null;
		}
		if (result instanceof BPlusTree.Split) {
			Split split = (Split) result;
			this.root = new InnerNode(root, split.entry, split.greater);
			this.size++;
			return null;
		}
		else {
			return (E) result;
		}
	}

	public E remove(K key) {
		if (key == null) {
			return updateNullEntry(null);
		}
		
		E removedEntry = root.remove(key);
		if (removedEntry != null) {
			afterRemoveProcessing();
		}
		return removedEntry;
	}

	@Override
	public E getFirstEntry() {
		if (hasNullEntry()) {
			return this.nullEntry;
		}
		if (hasTreeEntries()) {
			return root.getFirstEntry();
		}
		return null;
	}

	@Override
	public E getNextEntry(K key) {
		if (key == null) {
			return root.getFirstEntry();
		}
		return root.getNextEntry(key);
	}
	
	@Override
	public E getLastEntry() {
		if (hasTreeEntries()) {
			return root.getLastEntry();
		}
		if (hasNullEntry()) {
			return this.nullEntry;
		}
		return null;
	}
	
	@Override
	public E removeFirst() {
		if (isEmpty()) {
			return null;
		}
		if (hasNullEntry()) {
			return updateNullEntry(null);
		}
		E removedEntry = root.removeFirst();
		if (removedEntry != null) {
			afterRemoveProcessing();
		}
		return removedEntry;
	}

	@Override
	public E removeLast() {
		if (isEmpty()) {
			return null;
		}
		if (hasTreeEntries()) {
			E removedEntry = root.removeLast();
			if (removedEntry != null) {
				afterRemoveProcessing();
			}
			return removedEntry;
		}
		return updateNullEntry(null);
	}

	@SuppressWarnings("unchecked")
	private void afterRemoveProcessing() {
		if (root instanceof BPlusTree.InnerNode && root.length == 0) {
			InnerNode inner = (InnerNode) root;
			this.root = inner.childs[0];
		}
		this.size--;
	}
	
	public void print() {
		root.print("");
	}

	@Override
	public boolean isEmpty() {
		return !hasNullEntry() && !hasTreeEntries();
	}
	
	private boolean hasTreeEntries() {
		if (root instanceof BPlusTree.LeafNode && root.length == 0) {
			return false;
		}
		return true;
	}
	
	private boolean hasNullEntry() {
		return this.nullEntry != null;
	}
	
	private E updateNullEntry(E entry) {
		E result = this.nullEntry;
		this.nullEntry = entry;
		return result;
	}
	
	@Override
	public int size() {
		return this.size + (hasNullEntry() ? 1 : 0);
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

	@Override
	public Iterable<E> entries() {
		return new IterableEntries();
	}
	
	public class IterableEntries implements Iterable<E> {

		private final E firstEntry;
		
		public IterableEntries() {
			this.firstEntry = getFirstEntry();
		}
		
		@Override
		public Iterator<E> iterator() {
			return new Iterator<E>() {

				private E nextEntry = firstEntry;
				
				@Override
				public boolean hasNext() {
					return nextEntry != null;
				}

				@Override
				public E next() {
					E result = this.nextEntry;
					this.nextEntry = getNextEntry(this.nextEntry.getKey());
					return result;
				}

				@Override
				public void remove() {
					throw new IllegalStateException("unsupported operation");
				}
				
			};
		}
		
	}
	
	public int getTotalPages() {
		return 1 + this.root.getChildPages();
	}

}
