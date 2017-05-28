import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

/**
 * Created by ChenWei on 2017/5/27.
 */
 @SuppressWarnings("unchecked")
public class Search {

    private static BTree<Integer, List<BTree.Data>> btree = new BTree<Integer, List<BTree.Data>>(3);

    public static void main(String[] args) {
        read();
        Integer hour = 7;
        long endTime1=System.currentTimeMillis();
        List list = btree.search(hour);
        for (int j = 0; j < list.size(); j++) {
            BTree.Data data = (BTree.Data)list.get(j);
            BTree.searchFromHeapFile(data);
        }
        long endTime2=System.currentTimeMillis();

      //  BTree.searchKeyByStupid(7);
      //  long endTime3=System.currentTimeMillis();

        System.out.println("search key:7 by B tree use time:  "+(endTime2 - endTime1));
      //  System.out.println("search key:7 by stupid method use time:  "+(endTime3 - endTime2));

    }

    //at the start of the program, read object from file
    public static void read(){
        try {
            FileInputStream reader = new FileInputStream(BTree.indexfile);
            ObjectInputStream objectInputStream = new ObjectInputStream(reader);
            btree = (BTree<Integer, List<BTree.Data>>)objectInputStream.readObject();
            objectInputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

}
