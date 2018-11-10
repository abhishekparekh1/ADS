/**
 * Copyright 2005 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.util.*;

/**
 * A Fibonacci Heap, as described in <i>Introduction to Algorithms</i> by
 * Charles E. Leiserson, Thomas H. Cormen, Ronald L. Rivest.
 *
 * <p>
 *
 * A Fibonacci heap is a very efficient data structure for priority
 * queuing.  
 *
 */
public class FibonacciHeap {
  private FibonacciHeapNode max;
  private HashMap<String,FibonacciHeapNode> itemsToNodes;

  // private node class
  private static class FibonacciHeapNode {
    private String userObject;
    private int priority;

    private FibonacciHeapNode parent;
    private FibonacciHeapNode prevSibling;
    private FibonacciHeapNode nextSibling;
    private FibonacciHeapNode child;
    private int degree;
    private boolean mark;

    FibonacciHeapNode(String userObject, int priority) {
      this.userObject= userObject;
      this.priority= priority;

      this.parent= null;
      this.prevSibling= this;
      this.nextSibling= this;
      this.child= null;
      this.degree= 0;
      this.mark= false;
    }

    public String toString() {
      return "["+userObject+", "+degree+"]";
    }
  }

  /**
   * Creates a new <code>FibonacciHeap</code>.
   */
  public FibonacciHeap() {
    this.max= null;
    this.itemsToNodes= new HashMap<>();
  }

  /**
   *  Adds the Object <code>item</code>, with the supplied
   *  <code>priority</code>.
   */
  public void add(String item, int priority) {
    if (itemsToNodes.containsKey(item))
      throw new IllegalStateException("heap already contains item! (item= "
                                      + item + ")");
    FibonacciHeapNode newNode= new FibonacciHeapNode(item, priority);
    itemsToNodes.put(item, newNode);

    if (max == null) {
      max= newNode;
    } else {
      concatenateSiblings(newNode, max);
      if (newNode.priority > max.priority) 
        max= newNode;
    }
  }

  /**
   * Returns <code>true</code> if <code>item</code> exists in this
   * <code>FibonacciHeap</code>, false otherwise.
   */
  public boolean contains(String item) {
    return itemsToNodes.containsKey(item);
  }

  // makes x's nextSibling and prevSibling point to itself
  private void removeFromSiblings(FibonacciHeapNode x) {
    if (x.nextSibling == x) 
      return;
    x.nextSibling.prevSibling= x.prevSibling;
    x.prevSibling.nextSibling= x.nextSibling;
    x.nextSibling= x;
    x.prevSibling= x;
  }

  // joins siblings lists of a and b
  private void concatenateSiblings(FibonacciHeapNode a, FibonacciHeapNode b) {
    a.nextSibling.prevSibling= b;
    b.nextSibling.prevSibling= a;
    FibonacciHeapNode origAnext= a.nextSibling;
    a.nextSibling= b.nextSibling;
    b.nextSibling= origAnext;
  }

  /**
   * Returns the same Object that {@link #popMin()} would, without
   * removing it.
   */ 
  public String peekMax() {
    if (max == null) 
      return null;
    return max.userObject;
  }

  /**
   * Returns the number of objects in the heap.
   */
  public int size() {
    return itemsToNodes.size();
  }

  /**
   * Returns the object which has the <em>lowest</em> priority in the
   * heap.  If the heap is empty, <code>null</code> is returned.
   */
  public String popMax() {
    if (max == null) 
      return null;
    if (max.child != null) {
      FibonacciHeapNode tmp= max.child;
      // rempve parent pointers to max
      while (tmp.parent != null) {
        tmp.parent= null;
        tmp= tmp.nextSibling;
      }
      // add children of max to root list
      concatenateSiblings(tmp, max);
    }
    // remove max from root list
    FibonacciHeapNode oldMax= max;
    if (max.nextSibling == max) {
      max= null;
    } else {
      max= max.nextSibling;
      removeFromSiblings(oldMax);
      consolidate();
    }
    itemsToNodes.remove(oldMax.userObject);
    return oldMax.userObject;
  }

  // consolidates heaps of same degree
  private void consolidate() {
    int size= size();
    FibonacciHeapNode[] newRoots= new FibonacciHeapNode[size];

    FibonacciHeapNode node= max;
    FibonacciHeapNode start= max;
    do {
      FibonacciHeapNode x= node;
      int currDegree= node.degree;
      while (newRoots[currDegree] != null) {
        FibonacciHeapNode y= newRoots[currDegree];
        if (x.priority < y.priority) {
          FibonacciHeapNode tmp= x;
          x= y;
          y= tmp;
        }
        if (y == start) {
          start= start.nextSibling;
        }
        if (y == node) {
          node= node.prevSibling;
        }
        link(y, x);
        newRoots[currDegree++]= null;
      }
      newRoots[currDegree]= x;
      node= node.nextSibling;
    } while (node != start);

    max= null;
    for (int i= 0; i < newRoots.length; i++) 
      if (newRoots[i] != null) {
        if ( (max == null) 
             || (newRoots[i].priority > max.priority) )
          max = newRoots[i];
      }
  }

  // links y under x
  private void link(FibonacciHeapNode y, FibonacciHeapNode x) {
    removeFromSiblings(y);
    y.parent= x;
    if (x.child == null) 
      x.child= y;
    else 
      concatenateSiblings(x.child, y);
    x.degree++;
    y.mark= false;
  }

  /**
   * Inccreases the <code>priority</code> value associated with
   * <code>item</code>.
   *
   * <p>
   *
   * <code>item<code> must exist in the heap, and it's current
   * priority must be greater than <code>priority</code>.  
   *
   * @throws IllegalStateException if <code>item</code> does not exist
   * in the heap, or if <code>item</code> already has an equal or
   * lower priority than the supplied<code>priority</code>.
   */
  public void increaseKey(String item, int priority) {
    FibonacciHeapNode node= 
      (FibonacciHeapNode) itemsToNodes.get(item);
    if (node == null) 
      throw new IllegalStateException("No such element: " + item);
    if (node.priority > priority) 
      throw new IllegalStateException("increaseKey(" + item + ", " 
                                      + priority + ") called, but priority="
                                      + node.priority);
    node.priority= priority;
    FibonacciHeapNode parent= node.parent;
    if ( (parent != null) && (node.priority > parent.priority) ) {
      cut(node, parent);
      cascadingCut(parent);
    }
    if (node.priority > max.priority) 
      max= node;

  }

  // cut node x from below y
  private void cut(FibonacciHeapNode x, FibonacciHeapNode y) {
    // remove x from y's children
    if (y.child == x) 
      y.child= x.nextSibling;
    if (y.child == x) 
      y.child= null;

    y.degree--;
    removeFromSiblings(x);
    concatenateSiblings(x, max);
    x.parent= null;
    x.mark= false;
                
  }

  private void cascadingCut(FibonacciHeapNode y) {
    FibonacciHeapNode z= y.parent;
    if (z != null) {
      if (!y.mark) {
        y.mark= true;
      } else {
        cut(y, z);
        cascadingCut(z);
      }
    }
  }
  
  public static void main(String...args) {
	  FibonacciHeap h = new FibonacciHeap();
	  String str = "";
	  try(Scanner sc = new Scanner(new File(args[0]))){
		  while(sc.hasNext()) {

				str = sc.next();
				System.out.print(str);
				if(str.equals("stop"))break;
				else if(str.charAt(0)=='$') {
					int count = Integer.parseInt(sc.next());
					System.out.println(" " + count);

					str = str.substring(1);
						if(!h.contains(str)) {
							h.add(str, count);
						}
						else {
							count = count + h.itemsToNodes.get(str).priority;
							h.increaseKey(str, count);
						}
				}
				else {
					int n = Integer.parseInt(str);
					System.out.println("\n"+n);
				    StringBuilder total = new StringBuilder();
					 List<FibonacciHeapNode> list = new ArrayList<>();

//					 for(int i=0;i<n;i++) {
					 for(;h.size()>0;) {

//						 total.append(h.peekMax()+",");
							total.append(h.peekMax()+"|--"+h.itemsToNodes.get(h.peekMax()).priority+" --|"+ ", ");

						 list.add(h.max);
						 h.popMax();
					 }
							
					 for(FibonacciHeapNode x:list)
					 h.add(x.userObject, x.priority);
					System.out.println(total.toString()+"\r\n");

				}
			
		  }
		  
	  }
	  catch(Exception e) {
		  e.printStackTrace();
	  }
	  
  }

}
