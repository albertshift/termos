package alt.termos.integration;

import java.io.File;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import alt.termos.core.MetaPage;
import alt.termos.core.PagedMemoryManager;
import alt.termos.util.MapFileMode;
import alt.termos.util.UnsafeMemoryMappedFile;

public class MainTest {

	public static final String testFileName = "test.mmf";
	
	protected static UnsafeMemoryMappedFile mmf;
	
	public static final long DISK_PAGE_SIZE = 8192L;
	
	@BeforeClass
	public static void setup() throws Exception  {
		mmf = new UnsafeMemoryMappedFile(testFileName, MapFileMode.READ_WRITE, 16384L);
	}
	
	@AfterClass
	public static void tearDown() {
		mmf.close();
		new File(testFileName).delete();
	}
	
	@Test
	public void test() {
		
		MetaPage metaPage = new MetaPage(mmf);

		if (metaPage.isNew()) {
			System.out.println("create new");
			metaPage.formatNew(DISK_PAGE_SIZE);
		}
		
		metaPage.validate();
		
		System.out.println(metaPage.getVersion());
		
		PagedMemoryManager pda = new PagedMemoryManager(metaPage); 
		
		System.out.println("total pages = " + pda.getTotalPages());
		
		System.out.println("free page num = " + metaPage.getNextNewPage());
		
		
	}
	
}
