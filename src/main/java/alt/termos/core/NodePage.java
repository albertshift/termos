package alt.termos.core;

/**
 * 
 * @author Albert Shift
 *
 */
public class NodePage {

	/*
	 * Next page reference is using for creating linked lists of free pages or leaf pages
	 */
	
	public static final long NEXT_PAGE_OFFSET = 0;
	public static final long NEXT_PAGE_SIZE = 8; 

	/*
	 * Hit counter of the page is using to measure traffic in the page. 
	 */
	
	public static final long HIT_COUNTER_OFFSET = NEXT_PAGE_OFFSET + NEXT_PAGE_SIZE;
	public static final long HIT_COUNTER_SIZE = 8; 

}
