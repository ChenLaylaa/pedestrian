import java.io.*;
import java.util.*;

public interface DbFile
{
	/*
	 * read one specific page from the disk
	 */
	public Page readPage(PageId id);
	
	/*
	 * return specific page to disk
	 */
	public void writePage(Page p) throws IOException;
	

}
