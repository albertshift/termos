package alt.termos.core;

import alt.termos.util.Arguments;


/**
 * 
 * @author Albert Shift
 *
 */

public class PagedMemoryManager {
	
	private final MetaPage metaPage;
	
	private final long address;
	private final long diskPageSize;
	private final long totalPages;
	
	public PagedMemoryManager(MetaPage metaPage) {
		Arguments.notNull(metaPage);
		metaPage.validate();

		this.metaPage = metaPage;
		
		this.address = metaPage.getAddress();
		this.diskPageSize = metaPage.getDiskPageSize();
		this.totalPages = metaPage.getFileSize() / diskPageSize;
	}
	
	public long getPage(long pageNum) {
		if (pageNum >= totalPages) {
			throw new IndexOutOfBoundsException("PageNum: "+pageNum+", TotalPages: "+totalPages);
		}
		return address + pageNum * diskPageSize;
	}
	
	public long getTotalPages() {
		return totalPages;
	}

	public long allocateNewPage() {
		/*
		 * Free pages list
		 */
		
		/*
		 * New pages list
		 */
		
		/*
		 * Leaf pages evict
		 */
		
		return 0;
	}
	
	public void freePage(long pageNum) {
		/*
		 * Adds page to the free list
		 */
	}
	
}
