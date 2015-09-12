package alt.termos.util;


import org.junit.Assert;
import org.junit.Test;

import alt.termos.util.PageSizeUtil;

/**
 * 
 * @author Albert Shift
 *
 */

public class PageSizeUtilTest {

	@Test
	public void testIsAligned() {
		
		Assert.assertFalse(PageSizeUtil.isAligned(123L));
		Assert.assertFalse(PageSizeUtil.isAligned(1234L));
	
		Assert.assertTrue(PageSizeUtil.isAligned(0L));
		Assert.assertTrue(PageSizeUtil.isAligned(4096L));
		Assert.assertTrue(PageSizeUtil.isAligned(8192L));
	}
	
	@Test
	public void testTopAlign() {
		
		Assert.assertEquals(0L, PageSizeUtil.alignTop(0L));
		Assert.assertEquals(4096L, PageSizeUtil.alignTop(4096L));
		Assert.assertEquals(4096L, PageSizeUtil.alignTop(4097L));
		Assert.assertEquals(4096L, PageSizeUtil.alignTop(8191L));
		
	}
	
	@Test
	public void testBottomAlign() {
		
		Assert.assertEquals(0L, PageSizeUtil.alignBottom(0L));
		Assert.assertEquals(4096L, PageSizeUtil.alignBottom(4096L));
		Assert.assertEquals(8192L, PageSizeUtil.alignBottom(4097L));
		Assert.assertEquals(8192L, PageSizeUtil.alignBottom(8191L));
		
	}
}
