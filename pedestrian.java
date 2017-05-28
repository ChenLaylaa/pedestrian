import java.io.*;
import java.util.*;

public  class pedestrian {
	public static void main(String[] args) { 
		File file = new File("pedestrian.csv");
		HeapFile heapfile = new HeapFile(file);
		System.out.println(heapfile.numPages());
		
	}

}
