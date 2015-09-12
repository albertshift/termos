package alt.termos.emulation;

import java.util.Random;
import java.util.TreeMap;

import org.junit.Assert;
import org.junit.Test;

public class SimpleBTreeConsistencyTest {

	private static Random random = new Random(5);
	
	private enum Operation {
		GET, PUT, REMOVE;
	}
	
	private Operation op;
	private int key;
	
	@Test
	public void test() {
		
		TreeMap<Integer, Integer> controlMap = new TreeMap<Integer, Integer>();
		
		SimpleBTree<Integer, Integer> testMap = new SimpleBTree<Integer, Integer>(Integer.class, Integer.class, 3, Comparators.INTEGER);
		//HashMap<Integer, Integer> testMap = new HashMap<Integer, Integer>();
		
		try {
			testLoop(controlMap, testMap);
		}
		catch(IllegalStateException e) {
			System.out.println("Last operation " + this.op + ", last key = " + this.key);
			e.printStackTrace();
			testMap.print();
		}
		
		Assert.assertEquals(controlMap.size(), testMap.size());
	}
	
	private void testLoop(TreeMap<Integer, Integer> controlMap, SimpleBTree<Integer, Integer> testMap) {
		for (int i = 0; i != 10000000; ++i) {
			this.key = getNextKey();
			this.op = getRandomOperation();
			Integer controlValue = null;
			Integer testValue = null;
			switch(this.op) {
			case GET:
				controlValue = controlMap.get(this.key);
				testValue = testMap.get(this.key);
				Assert.assertEquals(controlValue, testValue);
				testMap.verify();
				break;
			case PUT:
				controlValue = controlMap.put(this.key, this.key);
				testValue = testMap.put(this.key, this.key);
				Assert.assertEquals(controlValue, testValue);
				testMap.verify();
				break;
			case REMOVE:
				controlValue = controlMap.remove(this.key);
				testValue = testMap.remove(this.key);
				Assert.assertEquals(controlValue, testValue);
				testMap.verify();
				break;
			}
		}
	}
	
	private int getNextKey() {
		return random.nextInt(1000);
	}
	
	private Operation getRandomOperation() {
		int ordinal = random.nextInt(3);
		return Operation.values()[ordinal];
	}
	
}
