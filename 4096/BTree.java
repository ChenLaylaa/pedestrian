/**
 * Created by ChenWei on 22/05/2017
 */
import java.io.*;

import java.util.*;

/**
 *
 *
 * @param <K> - Key
 * @param <V> - Value
 */
public class BTree<K, V> implements Serializable
{

    /**
     * <key, value> pairs in Btree nodes.
     * <p/>
     * In the Btree (B-tree) nodes, the key-value pairs are stored instead of just keys.
     * The values can be accessed through key
     *
     * @param <K> - Key
     * @param <V> - value
     */
     @SuppressWarnings("unchecked")
    private static class Entry<K, V> implements Serializable
    {
        private K key;
        private V value;

        public Entry(K k, V v)
        {
            this.key = k;
            this.value = v;
        }

        public K getKey()
        {
            return key;
        }

        public V getValue()
        {
            return value;
        }

        public void setValue(V value)
        {
            this.value = value;
        }

        @Override
        public String toString()
        {
            return key + ":" + value;
        }
    }

    /**
     * Search in the Btree and return results
     * <p/>
     * Judge if the searching is successful
     * if successful, show the search key's postion in Btree
     * if fail, show the correct position should be in Btree
     */
    private static class SearchResult<V>
    {
        private boolean exist;
        private int index;
        private V value;

        public SearchResult(boolean exist, int index)
        {
            this.exist = exist;
            this.index = index;
        }

        public SearchResult(boolean exist, int index, V value)
        {
            this(exist, index);
            this.value = value;
        }

        public boolean isExist()
        {
            return exist;
        }

        public int getIndex()
        {
            return index;
        }

        public V getValue()
        {
            return value;
        }

        @Override
        public String toString() {
            return "SearchResult{" +
                    "exist=" + exist +
                    ", index=" + index +
                    ", value=" + value +
                    '}';
        }
    }

    /**
     * Nodes in Btree
     *
     * TODO concurrent should be considered
     */
    private static class BTreeNode<K, V> implements Serializable
    {
        /** entry */
        private List<Entry<K,V>> entrys;//Key
        /** children */
        private List<BTreeNode<K, V>> children;//children
        /** judge if it is the leaf node */
        private boolean leaf;
        /** Comparator function */
        private Comparator<K> kComparator;

        private BTreeNode()
        {
            entrys = new ArrayList<Entry<K, V>>();
            children = new ArrayList<BTreeNode<K, V>>();
            leaf = false;
        }

        public BTreeNode(Comparator<K> kComparator)
        {
            this();
            this.kComparator = kComparator;
        }

        public boolean isLeaf()
        {
            return leaf;
        }

        public void setLeaf(boolean leaf)
        {
            this.leaf = leaf;
        }

        /**
         * Return the entry size. If the node is non-leaf node.
         * Children number of the node({@link #size()} + 1)。
         *
         * @return the number of the keys
         */
        public int size()
        {
            return entrys.size();
        }

        @SuppressWarnings("unchecked")
        int compare(K key1, K key2)
        {
            return kComparator == null ? ((Comparable<K>)key1).compareTo(key2) : kComparator.compare(key1, key2);
        }

        /**
         * search "search key" in the nodes
         * if the key can be found in the node, return results
         * mark: searching is successful, the corresponding index and the corresponding value.
         * if can not be found, return results
         * mark: searching is fail, the correct position should be and null value
         * <p/>
         * If fail, the index domain[0, {@link #size()}]；
         * If successful, the index domain[0, {@link #size()} - 1]
         * <p/>
         * Binary Search Algorithm, time complexity: O(log(t))。
         *
         * @param key - search key
         * @return - result found
         */
        public SearchResult<V> searchKey(K key)
        {
            int low = 0;
            int high = entrys.size() - 1;
            int mid = 0;
            while(low <= high)
            {
                mid = (low + high) / 2;
                Entry<K, V> entry = entrys.get(mid);
                if(compare(entry.getKey(), key) == 0) // entrys.get(mid).getKey() == key
                    break;
                else if(compare(entry.getKey(), key) > 0) // entrys.get(mid).getKey() > key
                    high = mid - 1;
                else // entry.get(mid).getKey() < key
                    low = mid + 1;
            }
            boolean result = false;
            int index = 0;
            V value = null;
            if(low <= high) // the searching is successful
            {
                result = true;
                index = mid; // index presents the position of the record.
                value = entrys.get(index).getValue();
            }
            else
            {
                result = false;
                index = low; // index presents the correct position should be
            }
            return new SearchResult<V>(result, index, value);
        }

        /**
         * add entry to the end of the node
         *
         * @param entry - given entry
         */
        public void addEntry(Entry<K, V> entry)
        {
            entrys.add(entry);
        }

        /**
         * delete entry for specific index
         * <p/>
         * @param index - given index
         * @param
         */
        public Entry<K, V> removeEntry(int index)
        {
            return entrys.remove(index);
        }

        /**
         * return the entry of given index
         *
         *
         * @param index - given index
         * @return the corresponding entry
         */
        public Entry<K, V> entryAt(int index)
        {
            return entrys.get(index);
        }

        /**
         * if the given key is found in the node, update the corresponding value
         * Or insert
         *
         * @param entry - given entry
         * @return null，if the key is not found，or return the old value
         */
        public V putEntry(Entry<K, V> entry)
        {
            SearchResult<V> result = searchKey(entry.getKey());
            if(result.isExist())
            {
                V oldValue = entrys.get(result.getIndex()).getValue();
                entrys.get(result.getIndex()).setValue(entry.getValue());
                return oldValue;
            }
            else
            {
                insertEntry(entry, result.getIndex());
                return null;
            }
        }

        /**
         * insert entry
         *
         * time complexity: O(t)。
         *
         * Note: the some key only appear once in BTree
         *
         * @param entry - given entry
         * @return true，if successful，false，if fail
         */
        public boolean insertEntry(Entry<K, V> entry)
        {
            SearchResult<V> result = searchKey(entry.getKey());
            if(result.isExist())
                return false;
            else
            {
                insertEntry(entry, result.getIndex());
                return true;
            }
        }

        /**
         * insert given entry in the position of the given index
         *
         * @param  - Key
         * @param index - index
         */
        public void insertEntry(Entry<K, V> entry, int index)
        {
			/*
			 * ArrayList is used.
			 */
            List<Entry<K, V>> newEntrys = new ArrayList<Entry<K, V>>();
            int i = 0;
            // index = 0 or index = keys.size()
            for(; i < index; ++ i)
                newEntrys.add(entrys.get(i));
            newEntrys.add(entry);
            for(; i < entrys.size(); ++ i)
                newEntrys.add(entrys.get(i));
            entrys.clear();
            entrys = newEntrys;
        }

        /**
         * return the children of the given index
         * @param index - given index
         * @return children
         */
        public BTreeNode<K, V> childAt(int index)
        {
            if(isLeaf())
                throw new UnsupportedOperationException("Leaf node doesn't have children.");
            return children.get(index);
        }

        /**
         * add children nodes
         *
         * @param child - given child
         */
        public void addChild(BTreeNode<K, V> child)
        {
            children.add(child);
        }

        /**
         * delete child of given index
         *
         *
         * @param index - given index
         */
        public void removeChild(int index)
        {
            children.remove(index);
        }

        /**
         * indert child in the position of the given index
         *
         * @param child - given child
         * @param index
         */
        public void insertChild(BTreeNode<K, V> child, int index)
        {
            List<BTreeNode<K, V>> newChildren = new ArrayList<BTreeNode<K, V>>();
            int i = 0;
            for(; i < index; ++ i)
                newChildren.add(children.get(i));
            newChildren.add(child);
            for(; i < children.size(); ++ i)
                newChildren.add(children.get(i));
            children = newChildren;
        }
    }

    private static final int DEFAULT_T = 2;

    /** Root Node of BTree */
    private BTreeNode<K, V> root;
    /** the number of keys of the non-leaf nodes in BTree: (t - 1) <= n <= (2t - 1) */
    private int t = DEFAULT_T;
    /** smallest number */
    private int minKeySize = t - 1;
    /** biggest number */
    private int maxKeySize = 2*t - 1;
    /** Comparatorfunction of key */
    private Comparator<K> kComparator;

    /**
     * create BTree
     */
    public BTree()
    {
        root = new BTreeNode<K, V>();
        root.setLeaf(true);
    }

    public BTree(int t)
    {
        this();
        this.t = t;
        minKeySize = t - 1;
        maxKeySize = 2*t - 1;
    }

    /**
     * create BTree according to kComparator
     *
     * @param kComparator
     */
    public BTree(Comparator<K> kComparator)
    {
        root = new BTreeNode<K, V>(kComparator);
        root.setLeaf(true);
        this.kComparator = kComparator;
    }

    public BTree(Comparator<K> kComparator, int t)
    {
        this(kComparator);
        this.t = t;
        minKeySize = t - 1;
        maxKeySize = 2*t - 1;
    }

    @SuppressWarnings("unchecked")
    int compare(K key1, K key2)
    {
        return kComparator == null ? ((Comparable<K>)key1).compareTo(key2) : kComparator.compare(key1, key2);
    }

    /**
     * search key
     *
     * @param key - given key
     * @return value if exists, or return null
     */
    public V search(K key)
    {
        return search(root, key);
    }

    /**
     * recursively search
     *
     * @param node
     * @param key
     * @return value
     */
    private V search(BTreeNode<K, V> node, K key)
    {
        SearchResult<V> result = node.searchKey(key);
        if(result.isExist())
            return result.getValue();
        else
        {
            if(node.isLeaf())
                return null;
            else {
                return search(node.childAt(result.getIndex()), key);
            }
        }
    }

    /**
     * split node
     *
     * @param parentNode
     * @param childNode
     * @param index
     */
    private void splitNode(BTreeNode<K, V> parentNode, BTreeNode<K, V> childNode, int index)
    {
        assert childNode.size() == maxKeySize;

        BTreeNode<K, V> siblingNode = new BTreeNode<K, V>(kComparator);
        siblingNode.setLeaf(childNode.isLeaf());
        // insert the (t - 1)entry to the new node
        for(int i = 0; i < minKeySize; ++ i)
            siblingNode.addEntry(childNode.entryAt(t + i));
        // extract the index (t - 1)
        Entry<K, V> entry = childNode.entryAt(t - 1);
        // delete index: [t - 1, 2t - 2]'s t entry
        for(int i = maxKeySize - 1; i >= t - 1; -- i)
            childNode.removeEntry(i);
        if(!childNode.isLeaf()) // if full node is not the leaf node
        {

            for(int i = 0; i < minKeySize + 1; ++ i)
                siblingNode.addChild(childNode.childAt(t + i));

            for(int i = maxKeySize; i >= t; -- i)
                childNode.removeChild(i);
        }
        // insert entry to the parent node
        parentNode.insertEntry(entry, index);
        // insert new nodes for parent node
        parentNode.insertChild(siblingNode, index + 1);
    }

    /**
     * insert given entry in the non-full node
     *
     * @param node
     * @param entry
     * @return
     */
    private boolean insertNotFull(BTreeNode<K, V> node, Entry<K, V> entry)
    {
        assert node.size() < maxKeySize;

        if(node.isLeaf()) // leaf node
            return node.insertEntry(entry);
        else
        {
			/* Find the entry where the given node should be inserted, then the entry should be inserted
			 * The location corresponds to the subtree
			 */
            SearchResult<V> result = node.searchKey(entry.getKey());
            // if exists
            if(result.isExist())
                return false;
            BTreeNode<K, V> childNode = node.childAt(result.getIndex());
            if(childNode.size() == 2*t - 1) // if the children node is full
            {
                // split firstly
                splitNode(node, childNode, result.getIndex());
				/* If the key of the given entry is greater than the key of the new entry after splitting, you need to insert the right of the new item,
				 * otherwise left.
				 */
                if(compare(entry.getKey(), node.entryAt(result.getIndex()).getKey()) > 0)
                    childNode = node.childAt(result.getIndex() + 1);
            }
            return insertNotFull(childNode, entry);
        }
    }

    /**
     * Insert a given key-value pair in the B-tree.
     *
     * @param key
     * @param value
     */
    public boolean insert(K key, V value)
    {
        if(root.size() == maxKeySize) // If the root node is full, the B tree is taller
        {
            BTreeNode<K, V> newRoot = new BTreeNode<K, V>(kComparator);
            newRoot.setLeaf(false);
            newRoot.addChild(root);
            splitNode(newRoot, root, 0);
            root = newRoot;
        }
        return insertNotFull(root, new Entry<K, V>(key, value));
    }

    /**
     * If a given key exists, the value associated with the update key is updated,
     * Otherwise insert the given item.
     *
     * @param node - non-full
     * @param entry
     * @return
     */
    private V putNotFull(BTreeNode<K, V> node, Entry<K, V> entry)
    {
        assert node.size() < maxKeySize;

        if(node.isLeaf()) // If it is a leaf node, insert it directly
            return node.putEntry(entry);
        else
        {
			/* Find the entry where the given node should be inserted, then the entry should be inserted
			 * The location corresponds to the subtree
			 */
            SearchResult<V> result = node.searchKey(entry.getKey());
            // if exists, update
            if(result.isExist())
                return node.putEntry(entry);
            BTreeNode<K, V> childNode = node.childAt(result.getIndex());
            if(childNode.size() == 2*t - 1) // if childNode is full
            {
                // split firstly
                splitNode(node, childNode, result.getIndex());
				/* If the key of the given entry is greater than the key of the new entry after splitting, you need to insert the right of the new item,
				 * otherwise left.
				 */
                if(compare(entry.getKey(), node.entryAt(result.getIndex()).getKey()) > 0)
                    childNode = node.childAt(result.getIndex() + 1);
            }
            return putNotFull(childNode, entry);
        }
    }

    /**
     * If a given key exists in the B tree, the value is updated.
     * Otherwise insert
     *
     * @param key
     * @param value
     * @return
     */
    public V put(K key, V value)
    {
        if(root.size() == maxKeySize) // If the root node is full, the B tree is taller
        {
            BTreeNode<K, V> newRoot = new BTreeNode<K, V>(kComparator);
            newRoot.setLeaf(false);
            newRoot.addChild(root);
            splitNode(newRoot, root, 0);
            root = newRoot;
        }
        return putNotFull(root, new Entry<K, V>(key, value));
    }

    /**
     * Removes an item associated with a given key from the B tree.
     *
     * @param key
     * @return
     */
    public Entry<K, V> delete(K key)
    {
        return delete(root, key);
    }

    /**
     * Removes the item associated with the given key from the subtree root with the given node.
     *
     * @param node
     * @param key
     * @return
     */
    private Entry<K, V> delete(BTreeNode<K, V> node, K key)
    {
        // The process needs to ensure that the number of keywords is at least t when a delete operation is performed on a non-root node.
        assert node.size() >= t || node == root;

        SearchResult<V> result = node.searchKey(key);
		/*
		 * it is the situation of the successful，0 <= result.getIndex() <= (node.size() - 1)，
		 * So (result.getIndex () + 1) does not overflow.
		 */
        if(result.isExist())
        {
            // If the keyword is in the node node and is a leaf node, it is deleted directly.
            if(node.isLeaf())
                return node.removeEntry(result.getIndex());
            else
            {
                // If the child node in the node node precedes the key contains at least t items
                BTreeNode<K, V> leftChildNode = node.childAt(result.getIndex());
                if(leftChildNode.size() >= t)
                {
                    // Use the last item in leftChildNode instead of the item you want to delete in the node
                    node.removeEntry(result.getIndex());
                    node.insertEntry(leftChildNode.entryAt(leftChildNode.size() - 1), result.getIndex());
                    // Recursively delete the last item in the left child node
                    return delete(leftChildNode, leftChildNode.entryAt(leftChildNode.size() - 1).getKey());
                }
                else
                {
                    // If the child node in the node node after the key contains at least t keywords
                    BTreeNode<K, V> rightChildNode = node.childAt(result.getIndex() + 1);
                    if(rightChildNode.size() >= t)
                    {
                        // Use the first entry in rightChildNode instead of the item you want to delete in the node
                        node.removeEntry(result.getIndex());
                        node.insertEntry(rightChildNode.entryAt(0), result.getIndex());
                        // Recursively delete the first item in the right child node
                        return delete(rightChildNode, rightChildNode.entryAt(0).getKey());
                    }
                    else // The sub-nodes preceding the key and after the key contain only t-1 items
                    {
                        Entry<K, V> deletedEntry = node.removeEntry(result.getIndex());
                        node.removeChild(result.getIndex() + 1);
                        // Merge the items associated with key in the node and the items in the rightChildNode into the leftChildNode
                        leftChildNode.addEntry(deletedEntry);
                        for(int i = 0; i < rightChildNode.size(); ++ i)
                            leftChildNode.addEntry(rightChildNode.entryAt(i));
                        // Merge the child nodes in rightChildNode into leftChildNode, if any
                        if(!rightChildNode.isLeaf())
                        {
                            for(int i = 0; i <= rightChildNode.size(); ++ i)
                                leftChildNode.addChild(rightChildNode.childAt(i));
                        }
                        return delete(leftChildNode, key);
                    }
                }
            }
        }
        else
        {
			/*
			 * Fail situation，0 <= result.getIndex() <= node.size()，
			 * (result.getIndex() + 1) will overflow
			 */
            if(node.isLeaf()) // If the keyword is not in the node node and is the leaf node, nothing is done because the keyword is not in the B tree
            {
                System.out.println("The key: " + key + " isn't in this BTree.");
                return null;
            }
            BTreeNode<K, V> childNode = node.childAt(result.getIndex());
            if(childNode.size() >= t) // If the child node has no less than t items, then recursively deleted
                return delete(childNode, key);
            else
            {
                // Find the right sibling node first
                BTreeNode<K, V> siblingNode = null;
                int siblingIndex = -1;
                if(result.getIndex() < node.size()) // exists
                {
                    if(node.childAt(result.getIndex() + 1).size() >= t)
                    {
                        siblingNode = node.childAt(result.getIndex() + 1);
                        siblingIndex = result.getIndex() + 1;
                    }
                }
                // If the right sibling node does not meet the criteria, try the left sibling node
                if(siblingNode == null)
                {
                    if(result.getIndex() > 0) // exists
                    {
                        if(node.childAt(result.getIndex() - 1).size() >= t)
                        {
                            siblingNode = node.childAt(result.getIndex() - 1);
                            siblingIndex = result.getIndex() - 1;
                        }
                    }
                }
                // There is an adjacent sibling node that contains at least t items
                if(siblingNode != null)
                {
                    if(siblingIndex < result.getIndex()) // Left brother node satisfies condition
                    {
                        childNode.insertEntry(node.entryAt(siblingIndex), 0);
                        node.removeEntry(siblingIndex);
                        node.insertEntry(siblingNode.entryAt(siblingNode.size() - 1), siblingIndex);
                        siblingNode.removeEntry(siblingNode.size() - 1);
                        // Move the last child of the left brother node to childNode
                        if(!siblingNode.isLeaf())
                        {
                            childNode.insertChild(siblingNode.childAt(siblingNode.size()), 0);
                            siblingNode.removeChild(siblingNode.size());
                        }
                    }
                    else // The right sibling node satisfies the condition
                    {
                        childNode.insertEntry(node.entryAt(result.getIndex()), childNode.size() - 1);
                        node.removeEntry(result.getIndex());
                        node.insertEntry(siblingNode.entryAt(0), result.getIndex());
                        siblingNode.removeEntry(0);
                        // Move the first child of the right sibling node to childNode
                        // childNode.insertChild(siblingNode.childAt(0), childNode.size() + 1);
                        if(!siblingNode.isLeaf())
                        {
                            childNode.addChild(siblingNode.childAt(0));
                            siblingNode.removeChild(0);
                        }
                    }
                    return delete(childNode, key);
                }
                else // If its adjacent left and right nodes contain t-1 items
                {
                    if(result.getIndex() < node.size()) // There is the right brother, added directly in the back
                    {
                        BTreeNode<K, V> rightSiblingNode = node.childAt(result.getIndex() + 1);
                        childNode.addEntry(node.entryAt(result.getIndex()));
                        node.removeEntry(result.getIndex());
                        node.removeChild(result.getIndex() + 1);
                        for(int i = 0; i < rightSiblingNode.size(); ++ i)
                            childNode.addEntry(rightSiblingNode.entryAt(i));
                        if(!rightSiblingNode.isLeaf())
                        {
                            for(int i = 0; i <= rightSiblingNode.size(); ++ i)
                                childNode.addChild(rightSiblingNode.childAt(i));
                        }
                    }
                    else // There is a left node that is inserted in front
                    {
                        BTreeNode<K, V> leftSiblingNode = node.childAt(result.getIndex() - 1);
                        childNode.insertEntry(node.entryAt(result.getIndex() - 1), 0);
                        node.removeEntry(result.getIndex() - 1);
                        node.removeChild(result.getIndex() - 1);
                        for(int i = leftSiblingNode.size() - 1; i >= 0; -- i)
                            childNode.insertEntry(leftSiblingNode.entryAt(i), 0);
                        if(!leftSiblingNode.isLeaf())
                        {
                            for(int i = leftSiblingNode.size(); i >= 0; -- i)
                                childNode.insertChild(leftSiblingNode.childAt(i), 0);
                        }
                    }
                    // If node is root and node does not contain any items
                    if(node == root && node.size() == 0)
                        root = childNode;
                    return delete(childNode, key);
                }
            }
        }
    }

    /**
     * A simple hierarchical traversal B tree implementation for outputting B-trees.
     */
    public void output()
    {
        Queue<BTreeNode<K, V>> queue = new LinkedList<BTreeNode<K, V>>();
        queue.offer(root);
        while(!queue.isEmpty())
        {
            BTreeNode<K, V> node = queue.poll();
            for(int i = 0; i < node.size(); ++ i)
                System.out.print(node.entryAt(i) + " ");
            System.out.println();
            if(!node.isLeaf())
            {
                for(int i = 0; i <= node.size(); ++ i)
                    queue.offer(node.childAt(i));
            }
        }
    }

    //Storage block number, start byte, size
    public static class Data implements Serializable{
        public int blockNum;
        public long startBytes;
        public int sizeBytes;

        public Data(int blockNum, long startBytes, int sizeBytes) {
            this.blockNum = blockNum;
            this.startBytes = startBytes;
            this.sizeBytes = sizeBytes;
        }

        @Override
        public String toString() {
            return "Data{" +
                    "blockNum=" + blockNum +
                    ", startBytes=" + startBytes +
                    ", sizeBytes=" + sizeBytes +
                    '}';
        }
    }
    @SuppressWarnings("unchecked")
    public static void main(String[] args)
    {
        BTree<Integer, List<Data>> btree = new BTree<Integer, List<Data>>(3);

        Map<Integer,List<Data>> map = new HashMap();

        long startTime = System.currentTimeMillis();
        readFileAndSaveToHeap(map);
        long endTime=System.currentTimeMillis();
        System.out.println("read the csv file and write the heapfile use time: "+(endTime - startTime));

        for (Map.Entry<Integer, List<Data>> entry : map.entrySet()) {
            int hour = entry.getKey();
            List list = (List<Data>)entry.getValue();
            btree.insert(hour,list);
        }
        long endTime1=System.currentTimeMillis();
        System.out.println("build B tree use time: "+(endTime1 - endTime));

        Integer hour = 7;
        List list = btree.search(hour);
        for (int j = 0; j < list.size(); j++) {
            Data data = (Data)list.get(j);
            searchFromHeapFile(data);
        }
        long endTime2=System.currentTimeMillis();
        System.out.println("search key:7 by B tree use time: "+(endTime2 - endTime1));

      //  searchKeyByStupid(7);
      //  long endTime3=System.currentTimeMillis();
      //  System.out.println("search key:7 by stupid method use time: "+(endTime3 - endTime2));

        //save object to indexfile
        saveToFile(btree,map);

        System.out.println("read the csv file and write the heapfile use time:  "+(endTime - startTime));
        System.out.println("build B tree use time: "+(endTime1 - endTime));
        System.out.println("search key:7 by B tree use time:  "+(endTime2 - endTime1));
      //  System.out.println("search key:7 by stupid method use time:  "+(endTime3 - endTime2));
//        btree.output();


//        test();

//        for (int i = 0; i < tempList.size(); i++) {
//            Integer hour = (Integer) tempList.get(i);
//            System.out.println("The key is: "+hour+"  "+map.get(hour));
//            List list = btree.search(hour);
//            for (int j = 0; j < list.size(); j++) {
//                Data data = (Data)list.get(j);
//                searchFromHeapFile(data);
//            }
//            System.out.println();
//        }

//        System.out.println(map);

//        System.out.println("----------------------");
//        btree.output();
//        System.out.println("----------------------");
//        btree.output();

        //Test, read the file
        System.out.println();
//        readFromHeapfile();

        //Test, whether to successfully read from a specific block and size to accurate data
//        testReadBlockSize(4,266,65);
    }

    @SuppressWarnings("unchecked")
    public static void readFileAndSaveToHeap(Map map){

        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
           reader.readLine();//The first line of information, for the title information
            String line = null;

            // To open the file in a readable and writable way, use RandomAccessFile to create the file.
            RandomAccessFile fc = new RandomAccessFile(heapfile, "rw");
            //The read and write of the file channel is based on the read and write of the file stream itself
//            MappedByteBuffer out = fc.map(FileChannel.MapMode.READ_WRITE, 0, pageSize);

            //Record the block number
            int blockNum = 0;
            long byteSize = 0;
            //Record the last one
            long oldbyteSize = 0;
            while((line=reader.readLine())!=null){

              //  System.out.println("current blockNum: " +blockNum);
                String item[] = line.split(",");//The CSV format file is a comma delimited file, which is segmented by comma

                String last = item[item.length-1];//the data needed

              //  System.out.println(last);

                //store in map
                int hourly_counts = Integer.valueOf(last);

                List list =null;
                if(map.containsKey(hourly_counts))
                    list = (ArrayList<Data>)map.get(hourly_counts);
                else {
                    list = new ArrayList();
                }

                //store to the heap file
                //fixed page size
                byteSize += line.getBytes().length;
                if(byteSize <= pageSize) {
                    fc.write(line.getBytes());

                    Data data = new Data(blockNum,byteSize-line.getBytes().length,line.getBytes().length);
                    list.add(data);
                    map.put(hourly_counts,list);

                }
                else {
//                    long space = byteSize - pageSize;
                    long space = pageSize - byteSize + line.getBytes().length;
                    for (int i = 0; i < space; i++) {
                        fc.write((byte)'X');
                    }
                    blockNum ++;
                    byteSize = 0;

                    byteSize += line.getBytes().length;
                    fc.write(line.getBytes());

                    Data data = new Data(blockNum,byteSize-line.getBytes().length,line.getBytes().length);
                    list.add(data);
                    map.put(hourly_counts,list);
                }
                oldbyteSize = line.getBytes().length;

            }
            System.out.println("end: blockNum: "+blockNum);
            fc.close();
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //Test, from the heapfile file to read a certain size of the data
    public static void readFromHeapfile(){

        try {
            RandomAccessFile fc = new RandomAccessFile(heapfile, "r");
            for (int i = 0; i < 5; i++) {
                byte[] bytes = new byte[pageSize];
                fc.read(bytes);
                System.out.println(new String(bytes));
            }

            fc.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //test
    public static void testReadBlockSize(int blockNum,int startBytes,int sizeBytes){
        System.out.println();

        try {
            RandomAccessFile fc = new RandomAccessFile(heapfile, "r");
            int temp = blockNum * pageSize + startBytes;
            fc.seek(temp);
            byte[] bytes = new byte[sizeBytes];
            fc.read(bytes);
            System.out.println(new String(bytes));

            fc.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //search from heap file
    public static void searchFromHeapFile(Data data){
        System.out.println("search result： ");

        int blockNum = data.blockNum;
        long startBytes = data.startBytes;
        int sizeBytes = data.sizeBytes;
        try {
            RandomAccessFile fc = new RandomAccessFile(heapfile, "r");
            long temp = blockNum * pageSize + startBytes;
            fc.seek(temp);
            byte[] bytes = new byte[sizeBytes];
            fc.read(bytes);
            System.out.println(new String(bytes));

            fc.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Test, traversal
    public static void test(){
        Random random = new Random();
        BTree<Integer, Integer> btree = new BTree<Integer, Integer>(3);
        List<Integer> save = new ArrayList<Integer>();
        for(int i = 0; i < 10; ++ i)
        {
            int r = random.nextInt(100);
            save.add(r);
            System.out.println(r);
            btree.insert(r, r);
        }

        System.out.println("----------------------");
        btree.output();
        System.out.println("----------------------");
        for (int i = 0; i < save.size(); i++) {
            System.out.println(btree.search(save.get(i)));
        }
//        btree.output();

    }
/**
    //Use the general method, from the file to find a specific key
    public static void searchKeyByStupid(int key){
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
           reader.readLine();//The first line of information is the title information
            String line = null;

            while((line=reader.readLine())!=null){

                String item[] = line.split(",");//The CSV format file is a comma delimited file, which is segmented by comma

                String last = item[item.length-1];//the data needed
                int a = Integer.valueOf(last);
                if(a == key)
                    System.out.println("found： "+line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
*/
    public static void saveToFile(BTree<Integer, List<BTree.Data>> btree,Map<Integer,List<BTree.Data>> map ){
        try {
            FileOutputStream outStream = new FileOutputStream(indexfile);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outStream);
            objectOutputStream.writeObject(btree);
            objectOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public static String filename = "pedestrian.csv";
    public static String heapfile = "heapfile";
    public static String indexfile = "index";
    public static int pageSize = 4096;

}
