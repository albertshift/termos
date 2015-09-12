package alt.termos.util;

/**
 * 
 * @author Albert Shift
 *
 */
public final class Arguments {

	public final static void notNull(Object o) {
		if (o == null) {
			throw new IllegalArgumentException("null argument");
		}
	}
	
	public final static void equals(int v1, int v2) {
		
	}
	
	public final static void positive(long v) {
		if (v < 0) {
			throw new IllegalArgumentException("not a positive argument " + v);
		}
	}
	
	public final static void greater(long v, long than) {
		if (v <= than) {
			throw new IllegalStateException("argument " + v + " must be greater than " + than);
		}
	}
	
	public final static void greaterOrEquals(long v, long than) {
		if (v < than) {
			throw new IllegalStateException("argument " + v + " must be greater or equals than " + than);
		}
	}
	
	public final static void aligned(long s, long pageSize) {
    	if (!PageSizeUtil.isAligned(s, pageSize)) {
    		throw new IllegalArgumentException("argument " + s + " must be aligned to " + pageSize);
    	}
	}
	
	public final static void pageAligned(long s) {
    	if (!PageSizeUtil.isAligned(s)) {
    		throw new IllegalArgumentException("argument " + s + " must be aligned to " + PageSizeUtil.getPageSize());
    	}
	}
	
}
