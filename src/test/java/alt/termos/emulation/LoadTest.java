package alt.termos.emulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.junit.Assert;
import org.junit.Test;

public class LoadTest {

	private static Random random = new Random(System.currentTimeMillis());
	
	private ExponentialDistribution exponentialDistribution = new ExponentialDistribution(5.0);
	
	private enum Operation {
		GET, PUT, REMOVE;
	}
	
	@Test
	public void test() {

		int branchingFactor = 2;
		
		BTree<Integer, Integer, CounterEntry<Integer, Integer>> testMap = BTree.newInstance(CounterEntry.class, branchingFactor, Comparators.INTEGER);
		
		for (int i = 0; i != 1000000; ++i) {
			
			Operation op = getRandomOperation();
			int key = getNextKey();
			CounterEntry<Integer, Integer> entry;
			 
			//System.out.println("Operation = " + op + ", key = " + key); 

			switch(op) {
			case GET:
				entry = (CounterEntry<Integer, Integer>) testMap.get(key);
				if (entry != null) {
					entry.increment();
				}
				break;
			case PUT:
				CounterEntry<Integer, Integer> newEntry = CounterEntry.newEntry(key, key);
				entry = (CounterEntry<Integer, Integer>) testMap.put(newEntry);
				if (entry != null) {
					newEntry.setCounter(entry.getCounter() + 1);
				}
				break;
			case REMOVE:
				testMap.remove(key);
				break;
			}
			
		}
		
		System.out.println("Size = " + testMap.size());
		
		testMap.print();
		
		
		//for (Tree.Entry<Integer, Integer> e = testMap.getFirstEntry(); e != null; e = testMap.getNextEntry(e.getKey())) {
			
		//	System.out.println(e);
			
		//}
		
		List<CounterEntry<Integer,Integer>> list = new ArrayList<CounterEntry<Integer,Integer>>(testMap.size());
		
		for (CounterEntry<Integer,Integer> e : testMap.entries()) {
			list.add(e);
		}
		
		Assert.assertEquals(testMap.size(), list.size());
		
		Collections.sort(list, counterComparator);
		

		/**
		 * Build unbalanced tree
		 */
		IdealTree<Integer, Integer> idealTree = new IdealTree<Integer, Integer>(branchingFactor, Comparators.INTEGER);
		
		idealTree.load(list);
		
		idealTree.print();
		
		for (CounterEntry<Integer,Integer> e : testMap.entries()) {
			Assert.assertNotNull(idealTree.get(e.getKey()));
		}
		
		System.out.println("BTree total pages = " + testMap.getTotalPages());
		System.out.println("IdealTree total pages = " + idealTree.getTotalPages());
		
		System.out.println("BTree total pages = " + testMap.getTotalPages());
		System.out.println("IdealTree total pages = " + idealTree.getTotalPages());
		
		
		int N = 100000000;

		testMap.resetPageAccess();
		long t0 = System.currentTimeMillis();
		for (int i = 0; i != N; ++i) {
			int key = getNextKey();
			testMap.get(key);
		}
		long td = System.currentTimeMillis() - t0;
		
		System.out.println("BTree = " + td + ", pageAccess = " + testMap.getPageAccess());
		
		idealTree.resetPageAccess();
		long it0 = System.currentTimeMillis();
		for (int i = 0; i != N; ++i) {
			int key = getNextKey();
			idealTree.get(key);
		}
		long itd = System.currentTimeMillis() - it0;
		
		System.out.println("IdealTree = " + itd + ", pageAccess = " + idealTree.getPageAccess());
		
		System.out.println("Performance better = " + (double) (td - itd) * 100.0 / itd + "%");
		System.out.println("PageAccess better = " + (double) (testMap.getPageAccess() - idealTree.getPageAccess()) * 100.0 / idealTree.getPageAccess() + "%");
		
	}
	
	private int getNextKey() {
		int expKey = (int) Math.round(exponentialDistribution.sample());
		return mapKey(expKey);
	}
	
	private int mapKey(int expKey) {
		return expKey;
		//return expKey % 13;
		//return ( expKey * 11 + 7 ) % 13;
		//return ( expKey * 1235677 + 453277 ) % 1301;
	}
	
	private Operation getRandomOperation() {
		int ordinal = random.nextInt(100);
		if (ordinal <= 80) {
			return Operation.GET;
		}
		//if (ordinal <= 99) {
			return Operation.PUT;
		//}
		//return Operation.REMOVE;
	}
	
	public static class CounterComparator implements Comparator<CounterEntry<Integer,Integer>> {

			@Override
			public int compare(CounterEntry<Integer, Integer> o1,
					CounterEntry<Integer, Integer> o2) {
				return Comparators.INTEGER.compare(o2.getCounter(), o1.getCounter());
			}
			
		}
	
	public static final CounterComparator counterComparator = new CounterComparator();
	

}
