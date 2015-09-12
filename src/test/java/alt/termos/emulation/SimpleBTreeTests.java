package alt.termos.emulation;

import java.util.TreeMap;

import org.junit.Test;

public class SimpleBTreeTests {

	@Test
	public void testRemoveInLeafWithJoin() {

		SimpleBTree<Integer, Integer> tree = new SimpleBTree<Integer, Integer>(Integer.class, Integer.class, 2, Comparators.INTEGER);

		tree.put(1, 1);
		tree.put(2, 2);
		tree.put(3, 3);
		tree.put(4, 4);
		tree.put(5, 5);
		
		tree.remove(1);

		//tree.print();
	}

	@Test
	public void testRemoveInLeafWithJoin2() {
		
		SimpleBTree<Integer, Integer> tree = new SimpleBTree<Integer, Integer>(Integer.class, Integer.class, 2, Comparators.INTEGER);

		tree.put(1, 1);
		tree.put(2, 2);
		tree.put(3, 3);
		tree.put(4, 4);
		tree.put(5, 5);

		tree.print();

		tree.remove(5);

		tree.print();
	}

	//@Test
	public void testRemoveInInnerWithJoin() {

		SimpleBTree<Integer, Integer> tree = new SimpleBTree<Integer, Integer>(Integer.class, Integer.class, 2, Comparators.INTEGER);

		tree.put(1, 1);
		tree.put(2, 2);
		tree.put(3, 3);
		tree.put(4, 4);
		tree.put(5, 5);
		
		tree.remove(3);

		tree.print();
	}
	
	//@Test
	public void test() {
		
		SimpleBTree<Integer, Integer> tree = new SimpleBTree<Integer, Integer>(Integer.class, Integer.class, 2, Comparators.INTEGER);
		
		System.out.println("old = " + tree.put(1, 1));
		
		System.out.println("old = " + tree.put(1, 11));
		
		System.out.println("old = " + tree.put(3, 3));
		
		System.out.println("old = " + tree.put(2, 2));

		System.out.println("old = " + tree.put(5, 5));
		
		
		System.out.println("old = " + tree.put(4, 4));

		System.out.println("size = " + tree.size());
		

		
		System.out.println("old = " + tree.put(7, 7));
		

		
		System.out.println("old = " + tree.put(9, 9));

		for (int i = 20; i != 11; --i) {
			System.out.println("old = " + tree.put(i, i));
		}
		
		
		System.out.println("old = " + tree.put(8, 8));
		
		tree.print();

		for (int i = 0; i != 21; ++i) {
			System.out.println("get(" + i + ") = " + tree.get(i));
		}

		System.out.println("size = " + tree.size());

		
	}
	
}
