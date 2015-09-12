package alt.termos.emulation;

import org.junit.Test;

public class BTreeTests {

	@Test
	public void testRemoveInLeafWithJoin() {

		BTree<Integer, Integer, SimpleEntry<Integer, Integer>> tree = BTree.newInstance(SimpleEntry.class, 2, Comparators.INTEGER);

		tree.put(SimpleEntry.newEntry(1, 1));
		tree.put(SimpleEntry.newEntry(2, 2));
		tree.put(SimpleEntry.newEntry(3, 3));
		tree.put(SimpleEntry.newEntry(4, 4));
		tree.put(SimpleEntry.newEntry(5, 5));
		
		tree.remove(1);

		//tree.print();
	}

	@Test
	public void testRemoveInLeafWithJoin2() {
		
		BTree<Integer, Integer, SimpleEntry<Integer, Integer>> tree = BTree.newInstance(SimpleEntry.class, 2, Comparators.INTEGER);

		tree.put(SimpleEntry.newEntry(1, 1));
		tree.put(SimpleEntry.newEntry(2, 2));
		tree.put(SimpleEntry.newEntry(3, 3));
		tree.put(SimpleEntry.newEntry(4, 4));
		tree.put(SimpleEntry.newEntry(5, 5));

		tree.print();

		tree.remove(5);

		tree.print();
	}

	//@Test
	public void testRemoveInInnerWithJoin() {

		BTree<Integer, Integer, SimpleEntry<Integer, Integer>> tree = BTree.newInstance(SimpleEntry.class, 2, Comparators.INTEGER);

		tree.put(SimpleEntry.newEntry(1, 1));
		tree.put(SimpleEntry.newEntry(2, 2));
		tree.put(SimpleEntry.newEntry(3, 3));
		tree.put(SimpleEntry.newEntry(4, 4));
		tree.put(SimpleEntry.newEntry(5, 5));
		
		tree.remove(3);

		tree.print();
	}
	
	//@Test
	public void test() {
		
		BTree<Integer, Integer, SimpleEntry<Integer, Integer>> tree = BTree.newInstance(SimpleEntry.class, 2, Comparators.INTEGER);
		
		System.out.println("old = " + tree.put(SimpleEntry.newEntry(1, 1)));
		
		System.out.println("old = " + tree.put(SimpleEntry.newEntry(1, 11)));
		
		System.out.println("old = " + tree.put(SimpleEntry.newEntry(3, 3)));
		
		System.out.println("old = " + tree.put(SimpleEntry.newEntry(2, 2)));

		System.out.println("old = " + tree.put(SimpleEntry.newEntry(5, 5)));
		
		
		System.out.println("old = " + tree.put(SimpleEntry.newEntry(4, 4)));

		System.out.println("size = " + tree.size());
		

		
		System.out.println("old = " + tree.put(SimpleEntry.newEntry(7, 7)));
		

		
		System.out.println("old = " + tree.put(SimpleEntry.newEntry(9, 9)));

		for (int i = 20; i != 11; --i) {
			System.out.println("old = " + tree.put(SimpleEntry.newEntry(i, i)));
		}
		
		
		System.out.println("old = " + tree.put(SimpleEntry.newEntry(8, 8)));
		
		tree.print();

		for (int i = 0; i != 21; ++i) {
			System.out.println("get(" + i + ") = " + tree.get(i));
		}

		System.out.println("size = " + tree.size());

		
	}
	
}
