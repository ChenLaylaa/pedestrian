import java.io.*;
import java.util.*;

public class HeapPage implements Page {
	final HeapPageId pid;
	
	public HeapPage(HeapPageId id, byte[] bata) throws IOException{
		this.pid = id;
		
	}

	public PageId getId() {
		// TODO Auto-generated method stub
		return null;
	}

	public byte[] getPageData() {
		// TODO Auto-generated method stub
		return null;
	}

}
