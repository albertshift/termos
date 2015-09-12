package alt.termos.util;

import org.junit.Test;

import alt.termos.util.Unsafe;

public class UnsafeTest {

	@Test
	public void test() {
		
		long pageSize = Unsafe.UNSAFE.pageSize();
		System.out.println("pageSize = " + pageSize);
		
	}
	
}
