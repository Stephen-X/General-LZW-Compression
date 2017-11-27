/**
 * A basic implementation of the hash table (HashMap) data structure.
 *
 * @author Stephen Xie &lt;[redacted]@andrew.cmu.edu&gt;
 */
public class HashTable<K, V> {
    private int tableSize;
    private SinglyLinkedList<KVPair>[] hashTable;

    /**
     * Constructor.
     */
    public HashTable() {
        this(57);  // table size as specified in assignment
    }

    /**
     * Constructor that also specifies the table size.
     *
     * @param tableSize hash table size (prime number suggested)
     */
    public HashTable(int tableSize) {
        this.tableSize = tableSize;
        hashTable = new SinglyLinkedList[tableSize];
        for (int i = 0; i <tableSize; i++) {
            hashTable[i] = new SinglyLinkedList<>();
        }
    }

    /**
     * Get the value paired with the given key in the hash table.
     *
     * @param key the key object
     * @return the matching value object, or null if none found
     */
    public V get(K key) {
        for (KVPair p : hashTable[getIndex(key)]) {
            if (p.key.equals(key)) {
                return p.value;
            }
        }
        return null;
    }

    /**
     * Put a given key value pair into the hash table, or update its value if the key already exists.
     *
     * @param key the key object
     * @param value the value object
     */
    public void put(K key, V value) {
        int ind = getIndex(key);
        for (KVPair p : hashTable[ind]) {
            if (p.key.equals(key)) {
                // update existing value instead
                p.value = value;
                return;
            }
        }
        hashTable[ind].append(new KVPair(key, value));
    }

    /**
     * Does the hash table contains the specified key?
     *
     * @param key the key to be searched for
     * @return true / false answer
     */
    public boolean containsKey(K key) {
        for (KVPair p : hashTable[getIndex(key)]) {
            if (key.equals(p.key))
                return true;
        }
        return false;
    }

    /**
     * Get the index of the list to which the key belongs in the table.
     *
     * @param key the key object
     * @return index to the destination list
     */
    private int getIndex(K key) {
        // int has 4 bytes (2^32 bits); mask off the sign bit
        // before mapping to the respective index in the hash table
        return (key.hashCode() & 0x7FFFFFFF) % tableSize;
    }


    // a helper class for storing the key-value pairs
    // in the table
    private class KVPair {
        public K key;
        public V value;

        public KVPair(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

}
