package alt.termos.util;

/**
 * 
 * @author Albert Shift
 *
 */

public final class PageSizeUtil {

    public static long PAGE_SIZE = (long) Unsafe.UNSAFE.pageSize();
    public static long PAGE_SIZE_MASK = PAGE_SIZE - 1L;
  
    public static long getPageSize() {
    	return PAGE_SIZE;
    }
    
    public static final long alignTop(long address) {
    	return address & ~PAGE_SIZE_MASK;
    }
    
    public static final long alignTop(long address, long customPageSize) {
    	return address & ~(customPageSize - 1L);
    }
    
    public static final long alignBottom(long address) {
    	long aligned = address & ~PAGE_SIZE_MASK;
    	if (address != aligned) {
    		aligned += PAGE_SIZE;
    	}
    	return aligned;
    }
    
    public static final long alignBottom(long address, long customPageSize) {
    	long aligned = address & ~(customPageSize - 1L);
    	if (address != aligned) {
    		aligned += customPageSize;
    	}
    	return aligned;
    }

    public static boolean isAligned(long address) {
    	long aligned = address & ~PAGE_SIZE_MASK;
    	if (address != aligned) {
    		return false;
    	}
    	return true;
    }
    
    public static boolean isAligned(long address, long customPageSize) {
    	long aligned = address & ~(customPageSize - 1L);
    	if (address != aligned) {
    		return false;
    	}
    	return true;
    }
}
