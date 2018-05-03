package org.cloudbus.cloudsim.util.workload;
/*************************************************************************
 *  Compilation:  javac Bag.java
 *  Execution:    java Bag < input.txt
 *
 *  A generic bag or multiset, implemented using a linked list.
 *
 *************************************************************************/

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 *  The <tt>Bag</tt> class represents a bag (or multiset) of 
 *  generic items. It supports insertion and iterating over the 
 *  items in arbitrary order.
 *  <p>
 *  The <em>add</em>, <em>isEmpty</em>, and <em>size</em>  operation 
 *  take constant time. Iteration takes time proportional to the number of items.
 *  <p>
 *  For additional documentation, see <a href="http://algs4.cs.princeton.edu/13stacks">Section 1.3</a> of
 *  <i>Algorithms, 4th Edition</i> by Robert Sedgewick and Kevin Wayne.
 */
public class Bag<Job> implements Iterable<Job> {
    private int N;         // number of elements in bag
    private Node first;    // beginning of bag
    private Job nodeInfo;
    // helper linked list class
    private class Node {
        private Job item;
        private Node next;
    }

   /**
     * Create an empty stack.
     */
    public Bag() {
        first = null;
        N = 0;
        assert check();
    }

   /**
     * Is the BAG empty?
     */
    public boolean isEmpty() {
        return first == null;
    }

   /**
     * Return the number of items in the bag.
     */
    public int size() {
        return N;
    }

   /**
     * Add the item to the bag.
     */
    public void add(Job item) {
        Node oldfirst = first;
        first = new Node();
        first.item = item;
        first.next = oldfirst;
        N++;
        assert check();
    }
    
    /**
     * Add the item to the bag.
     */
    public void remove(Job item) {
        Node prev = first;
        Node cur = first.next;
        if(first.item.equals(item)){
        	first = first.next;
        	N--;
        	return;
        }
        for(int i= 0; i < size()-1; i++){
        	if(cur.item.equals(item)){
            	prev.next = cur.next;
            	N--;
            	return;
            }
        	prev = prev.next;
        	cur = cur.next;
        }
    }

    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + N;
		result = prime * result + ((first == null) ? 0 : first.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Bag other = (Bag) obj;
		if (N != other.N)
			return false;
		if (first == null) {
			if (other.first != null)
				return false;
		} else if (!first.equals(other.first))
			return false;
		return true;
	}

	// check internal invariants
    private boolean check() {
        if (N == 0) {
            if (first != null) return false;
        }
        else if (N == 1) {
            if (first == null)      return false;
            if (first.next != null) return false;
        }
        else {
            if (first.next == null) return false;
        }

        // check internal consistency of instance variable N
        int numberOfNodes = 0;
        for (Node x = first; x != null; x = x.next) {
            numberOfNodes++;
        }
        if (numberOfNodes != N) return false;

        return true;
    } 


   /**
     * Return an iterator that iterates over the items in the bag.
     */
    public Iterator<Job> iterator()  {
        return new ListIterator();  
    }

    public Job getNodeInfo() {
    	return nodeInfo;
    }

    public void setNodeInfo(Job nodeInfo) {
    	this.nodeInfo = nodeInfo;
    }

	// an iterator, doesn't implement remove() since it's optional
    private class ListIterator implements Iterator<Job> {
        private Node current = first;

        public boolean hasNext()  { return current != null;                     }
        public void remove()      { throw new UnsupportedOperationException();  }

        public Job next() {
            if (!hasNext()) throw new NoSuchElementException();
            Job item = current.item;
            current = current.next; 
            return item;
        }
    }

}
