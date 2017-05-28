import java.io.*;
import java.util.*;

public class HeapFile implements DbFile
{
	/*
	 * constructs heap file
	 */

	private File file;
	/*transfer some specific file to heap file*/
	public HeapFile(File file)
	{
		this.file = file;
	}
	/* get file*/
	public File getFile()
	{
		return this.file;
	}
	
	/*
	 * @see DbFile#readPage(PageId)
	 */
	
	public Page readPage(PageId pid) 
	{
		try{
			RandomAccessFile file = new RandomAccessFile(this.file, "r");
			int offset = BufferOne.PAGE_SIZE * pid.pageNumber();
			byte[] data = new byte[BufferOne.PAGE_SIZE];
			if (offset + BufferOne.PAGE_SIZE > file.length()){
				System.err.println("The offset is bigger than the max size!");
				System.exit(1);
			}
			file.seek(offset);
			file.readFully(data);
			file.close();
			return new HeapPage((HeapPageId) pid, data);
		} catch (FileNotFoundException e){
			System.err.println("FileNotFoundException: " + e.getMessage());
            throw new IllegalArgumentException();
		} catch (IOException e) {
			 System.err.println("Caught IOException: " + e.getMessage());
	            throw new IllegalArgumentException();

		}
		
	}
	@Override
	public void writePage(Page p) throws IOException {
		// TODO Auto-generated method stub
		RandomAccessFile raf = new RandomAccessFile(this.file, "rw");
    	PageId pid = Page.getId();
    	int offset = BufferOne.PAGE_SIZE * pid.pageNumber();
    	raf.seek(offset);
    	raf.write(Page.getPageData(), 0, BufferOne.PAGE_SIZE);
    	raf.close();
		
	}
	
	/*
     * Returns the number of pages in this HeapFile.
     */
	
    public int numPages() {
        return (int) Math.ceil(this.file.length()/BufferOne.PAGE_SIZE);
    }
    
 

}
