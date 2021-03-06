/* This program is free software: you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public License
 as published by the Free Software Foundation, either version 3 of
 the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>. */

package org.opentripplanner.common.pqueue;

import java.util.Arrays;

public class BinHeap<T> implements OTPPriorityQueue<T> {
    
    public static OTPPriorityQueueFactory FACTORY = new BinHeapFactory();
    private static final double GROW_FACTOR = 2.0;
    
    private double[] prio;
    private T[] elem;
    private int size; 
    private int capacity;
    
    public BinHeap() {
    	this(1000);
    }
    
    @SuppressWarnings("unchecked")
    public BinHeap(int capacity) {
        if (capacity < 10) capacity = 10;
    	this.capacity = capacity;
        elem = (T[]) new Object[capacity + 1]; // erasure voodoo
        prio = new double[capacity + 1];       // 1-based indexing
        size = 0;
        prio[0] = Double.NEGATIVE_INFINITY;    // set sentinel
    }
    
    @Override
    public int size() {
    	return size;
    }
    
    @Override
    public boolean empty() {
    	return size <= 0;
    }

    @Override
    public double peek_min_key() {
    	if (size > 0) 
    		return prio[1];
    	else 
    		throw new IllegalStateException("An empty queue does not have a minimum key.");
   	}
    
    @Override
    public T peek_min() {
    	if (size > 0)
    		return elem[1];
    	else 
    		return null;
    }
    
    @Override
    public void insert_or_dec_key(T e, double p) {
    	throw new UnsupportedOperationException("BinHeap has no decrease key operation.");
    }

    public void rekey(T e, double p) {
        // Perform "inefficient" but straightforward linear search 
    	// for an element then change its key by sifting up or down
        int i = 0;
    	for (T t : elem) {
    		if (t == e) 
    			break;
			i++;
    	}
    	if (i > size) {
        	//System.out.printf("did not find element %s\n", e);
    		return; 
    	}
    	//System.out.printf("found element %s with key %f at %d\n", e, prio[i], i);
    	if (p > prio[i]) { 
    		// sift up (as in extract)
            while (i*2 <= size) {
                int child = i * 2;
                if (child != size && prio[child+1] < prio[child])
                    child ++;
                if (p > prio[child]) {
                    elem[i] = elem[child];
                    prio[i] = prio[child];
                    i = child;
                } else break;
            }
            elem[i] = e;
            prio[i] = p;
    	} else { 
    		// sift down (as in insert)
            while (prio[i/2] > p) {
                elem[i] = elem[i/2];
                prio[i] = prio[i/2];
                i /= 2;
            }
            elem[i] = e;
            prio[i] = p;
    	}
    }

    public void dump() {
    	for (int i=0; i<=capacity; i++) {
    		String topMarker = (i > size) ? "(UNUSED)" : ""; 
        	System.out.printf("%d\t%f\t%s\t%s\n", i, prio[i], elem[i], topMarker);
    	}
    	System.out.printf("-----------------------\n");
    }
    
    public void reset() {
    	// empties the queue in one operation
    	size=0;
    } 

    @Override
    public void insert(T e, double p) {
        int i;
        size += 1;
        if (size > capacity) 
        	resize((int) (capacity * GROW_FACTOR));
        for (i = size; prio[i/2] > p; i /= 2) {
            elem[i] = elem[i/2];
            prio[i] = prio[i/2];
        }
        elem[i] = e;
        prio[i] = p;
    }    
    
    @Override
    public T extract_min() {
        int    i, child;
        T      minElem  = elem[1];
        T      lastElem = elem[size];
        double lastPrio = prio[size];
        if (size <= 0) 
            return null;
    	size -= 1;
        for (i=1; i*2 <= size; i=child) {
            child = i*2;
            if (child != size && prio[child+1] < prio[child])
                child++;
            if (lastPrio > prio[child]) {
                elem[i] = elem[child];
                prio[i] = prio[child];
            } else break;
        }
        elem[i] = lastElem;
        prio[i] = lastPrio;
        return minElem;
    }
    
    public void resize(int capacity) {
    	// System.out.println("Growing queue to " + capacity);
    	if (capacity < size) 
    		throw new IllegalStateException("BinHeap contains too many elements to fit in new capacity.");
    	this.capacity = capacity;
    	prio = Arrays.copyOf(prio, capacity + 1);
    	elem = Arrays.copyOf(elem, capacity + 1);
    }
    
    private static class BinHeapFactory implements OTPPriorityQueueFactory {
        @Override
        public <T> OTPPriorityQueue<T> create(int maxSize) {
            return new BinHeap<T>(maxSize);
        }
    }
}
