package alt.termos.core;

import alt.termos.util.Arguments;
import alt.termos.util.Unsafe;
import alt.termos.util.UnsafeMemoryMappedFile;

/**
 * 
 * @author Albert Shift
 *
 */
public class MetaPage {

	public static final int MAGIC = 0x2E464D4D;
	public static final long MAGIC_OFFSET = 0;
	public static final long MAGIC_SIZE = 4;
	
	public static final int DEFAULT_VERSION = 0x100;
	public static final long VERSION_OFFSET = MAGIC_OFFSET + MAGIC_SIZE;
	public static final long VERSION_SIZE = 4;
	
	public static final int DEFAULT_DISK_PAGESIZE = 4096;
	public static final long DISK_PAGESIZE_OFFSET = VERSION_OFFSET + VERSION_SIZE;
	public static final long DISK_PAGESIZE_SIZE = 4;
	
	public static final long REVERVED1_OFFSET = DISK_PAGESIZE_OFFSET + DISK_PAGESIZE_SIZE;
	public static final long RESERVED1_SIZE = 4;
	
	public static final long DEFAULT_NEXTNEW_PAGE = 1;
	public static final long NEXTNEW_PAGE_OFFSET = REVERVED1_OFFSET + RESERVED1_SIZE;
	public static final long NEXTNEW_PAGE_SIZE = 8; 

	/*
	 * Needs to be an array of linked lists. Hot, medium, cold and so on. Length of this array can be configurable.
	 * Not need to be a very big array. Tail of the hot linked list can have the same temperature as a head of cold linked list.
	 * 
	 * TODO: implement this as a simple array, group of temperature/hit counter is an index  
	 */
	
	public static final long DEFAULT_FREEHEAD_PAGE = 0;
	public static final long FREEHEAD_PAGE_OFFSET = NEXTNEW_PAGE_OFFSET + NEXTNEW_PAGE_SIZE;
	public static final long FREEHEAD_PAGE_SIZE = 8; 

	/*
	 * Needs to be an array of linked lists, each element has different k (distance from the root node)
	 * 
	 * TODO: implement this as a simple array, k is an index
	 */
	
	public static final long DEFAULT_LEAFHEAD_PAGE = 0;
	public static final long LEAFHEAD_PAGE_OFFSET = FREEHEAD_PAGE_OFFSET + FREEHEAD_PAGE_SIZE;
	public static final long LEAFHEAD_PAGE_SIZE = 8; 



	public static final long LAST_OFFSET = LEAFHEAD_PAGE_OFFSET + LEAFHEAD_PAGE_SIZE;
	
	private final long address;
	private final long fileSize;
	
	public MetaPage(UnsafeMemoryMappedFile mmf) {
		Arguments.notNull(mmf);
		Arguments.greater(mmf.getSize(), LAST_OFFSET);
		
		this.address = mmf.getAddress();
		this.fileSize = mmf.getSize();
	}
	
	public long getAddress() {
		return address;
	}

	public long getFileSize() {
		return fileSize;
	}

	public int getMagic() {
		return Unsafe.UNSAFE.getInt(address + MAGIC_OFFSET);
	}
	
	public void setMagic(int magic) {
		Unsafe.UNSAFE.putInt(address + MAGIC_OFFSET, magic);
	}
	
	public int getVersion() {
		return Unsafe.UNSAFE.getInt(address + VERSION_OFFSET);
	}

	public void setVersion(int version) {
		Unsafe.UNSAFE.putInt(address + VERSION_OFFSET, version);
	}
	
	public long getDiskPageSize() {
		return (long) Unsafe.UNSAFE.getInt(address + DISK_PAGESIZE_OFFSET);
	}
	
	public void setDiskPageSize(long diskPageSize) {
		Unsafe.UNSAFE.putInt(address + DISK_PAGESIZE_OFFSET, (int) diskPageSize);
	}
	
	public long getNextNewPage() {
		return Unsafe.UNSAFE.getLong(address + NEXTNEW_PAGE_OFFSET);
	}
	
	public void setNextNewPage(long pageNum) {
		Unsafe.UNSAFE.putLong(address + NEXTNEW_PAGE_OFFSET, pageNum);
	}

	public long getFreeHeadPage() {
		return Unsafe.UNSAFE.getLong(address + FREEHEAD_PAGE_OFFSET);
	}
	
	public void setFreeHeadPage(long pageNum) {
		Unsafe.UNSAFE.putLong(address + FREEHEAD_PAGE_OFFSET, pageNum);
	}

	public long getLeafHeadPage() {
		return Unsafe.UNSAFE.getLong(address + LEAFHEAD_PAGE_OFFSET);
	}
	
	public void setLeafHeadPage(long pageNum) {
		Unsafe.UNSAFE.putLong(address + LEAFHEAD_PAGE_OFFSET, pageNum);
	}

	public void formatNew(long diskPageSize) {
		Arguments.greater(fileSize, diskPageSize);
		Arguments.aligned(fileSize, diskPageSize);
		setMagic(MAGIC);
		setVersion(DEFAULT_VERSION);
		setDiskPageSize(diskPageSize);
		setNextNewPage(DEFAULT_NEXTNEW_PAGE);
		setFreeHeadPage(DEFAULT_FREEHEAD_PAGE);
		setLeafHeadPage(DEFAULT_LEAFHEAD_PAGE);
	}
	
	public boolean isNew() {
		return getMagic() == 0;
	}
	
	public void validate() {
		int magic = getMagic();
		if (magic != MAGIC) {
			throw new IllegalStateException("wrong magic " + Integer.toHexString(magic));
		}
		int version = getVersion();
		if (version != DEFAULT_VERSION) {
			throw new IllegalStateException("unsupported version " + version);
		}
		
		long diskPageSize = getDiskPageSize();
		Arguments.positive(diskPageSize);		
		Arguments.pageAligned(diskPageSize);
		Arguments.aligned(fileSize, diskPageSize);
		
		long pageNum = getNextNewPage();
		Arguments.positive(pageNum);
		Arguments.greaterOrEquals(fileSize, pageNum * diskPageSize);

		pageNum = getFreeHeadPage();
		Arguments.positive(pageNum);
		Arguments.greaterOrEquals(fileSize, pageNum * diskPageSize);

		pageNum = getLeafHeadPage();
		Arguments.positive(pageNum);
		Arguments.greaterOrEquals(fileSize, pageNum * diskPageSize);

	}
}
