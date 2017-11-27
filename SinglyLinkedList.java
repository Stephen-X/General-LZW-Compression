import java.util.Iterator;

/**
 * A simple implementation of a singly linked list.
 *
 * @author Stephen Xie &lt;[redacted]@cmu.edu&gt;
 */
public class SinglyLinkedList<E> implements Iterable<E>{

    // head and tail of the doubly linked list
    private Node head;
    private Node tail;
    // size of the singly linked list
    private int size;

    /**
     * Constructs a new SinglyLinkedList object with head and tail as null.
     */
    public SinglyLinkedList() {
        this.head = null;
        this.tail = null;
        this.size = 0;
    }

    /**
     * Returns true if the list is empty false otherwise.
     *
     * @return true if the list empty false otherwise
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Add an element to the end of the linked list.
     *
     * @param e an element to be added
     */
    public void append(E e) {
        // create a new node with no next node
        Node newNode = new Node(e, null);
        // set the new node as the next node of the current tail node if the latter is not null,
        // then move the tail pointer
        if (tail != null) tail.next = newNode;
        tail = newNode;
        if (head == null) head = tail;  // this must be the first node added to the list
        size++;
    }

    /**
     * Add an element to the front of the linked list.
     *
     * @param e an element to be added
     */
    public void prepend(E e) {
        // create a new node with the head of the current linked list as its next node
        Node newNode = new Node(e, head);
        // set the new node as the previous node of the current head node if the latter is not null,
        // then move the head pointer
        head = newNode;
        if (tail == null) tail = head;  // this must be the first node added to the list
        size++;
    }

    /**
     * Remove and return the element at the front of the linked list.
     *
     * @return the element at front
     */
    public E poll() {
        if (size <= 0)
            throw new NullPointerException("Trying to remove from an empty list.");

        Node removed = head;
        head = head.next;
        if (head == null) tail = null;
        size--;
        return removed.val;
    }

    /**
     * Remove and return the element at the end of the linked list.
     *
     * @return the element at the end
     */
    public E pop() {
        if (size <= 0)
            throw new NullPointerException("Trying to remove from an empty list.");

        Node removed = tail;
        Node pointer = head;

        if (size > 1) {
            while (pointer.next != removed) {  // traverse the list to find the previous node of tail
                pointer = pointer.next;
            }
            tail = pointer;
        } else {  // the list only has 1 node before removal
            tail = null;
            head = null;
        }
        size--;
        return removed.val;
    }

    /**
     * Return the size of the list.
     *
     * @return the number of nodes in the singly linked list between head and tail (inclusive)
     */
    public int getSize() {
        return size;
    }

    /**
     * This returns an iterator that traverses the list from head to tail.
     *
     * @return an iterator that traverses the list from head to tail
     */
    @Override
    public Iterator<E> iterator() {
        return new Iterator<E>() {
            private Node pointer = head;

            @Override
            public boolean hasNext() {
                return pointer != null;
            }

            @Override
            public E next() {
                Node result = pointer;
                pointer = pointer.next;
                return result.val;
            }
        };
    }


    // a helper class for representing a single node on a singly linked list
    private class Node {

        public Node next;  // pointer to the next nnode
        public E val;  // content of the current node

        /**
         * Constructor.
         * @param val value of this node
         * @param next pointer to the next node
         */
        public Node(E val, Node next) {
            this.val = val;
            this.next = next;
        }
    }

}
