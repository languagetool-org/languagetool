/* LanguageTool, a natural language style checker 
 * Copyright (C) 2009 Daniel Naber (http://www.danielnaber.de)
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 * USA
 */

package org.languagetool.rules;

import java.util.ArrayList;
import java.util.EmptyStackException;

/**
 * Implements unsynchronized stack (contrary to default Java {@link java.util.Stack},
 * this one is based on ArrayList). Usage is the same as the java.util.Stack.
 * 
 * @author Marcin Miłkowski.
 */
public class UnsyncStack<E> extends ArrayList<E> {
  
  /** Generated automatically. */
  private static final long serialVersionUID = -4984830372178073605L;

  UnsyncStack() {
  }

  /**
   * Pushes an item onto the top of this stack. This has exactly the same effect
   * as: {@code add(item)}
   * 
   * @param item the item to be pushed onto this stack.
   * @return the <code>item</code> argument.
   * @see ArrayList#add
   */
  public E push(E item) {
    add(item);
    return item;
  }

  /**
   * Removes the object at the top of this stack and returns that object as the
   * value of this function.
   * 
   * @return The object at the top of this stack (the last item of the
   *         <tt>ArrayList</tt> object).
   * @exception EmptyStackException if this stack is empty.
   */
  public E pop() {
    E obj;
    int len = size();
    obj = peek();
    remove(len - 1);
    return obj;
  }

  /**
   * Looks at the object at the top of this stack without removing it from the
   * stack.
   * 
   * @return the object at the top of this stack (the last item of the
   *         <tt>ArrayList</tt> object).
   * @exception EmptyStackException if this stack is empty.
   */
  public E peek() {
    int len = size();
    if (len == 0) {
      throw new EmptyStackException();
    }
    return get(len - 1);
  }

  /**
   * Tests if this stack is empty.
   * 
   * @return <code>true</code> if and only if this stack contains no items;
   *         <code>false</code> otherwise.
   */
  public boolean empty() {
    return size() == 0;
  }

  /**
   * Returns the 1-based position where an object is on this stack. If the
   * object <tt>o</tt> occurs as an item in this stack, this method returns the
   * distance from the top of the stack of the occurrence nearest the top of the
   * stack; the topmost item on the stack is considered to be at distance
   * <tt>1</tt>. The <tt>equals</tt> method is used to compare <tt>o</tt> to the
   * items in this stack.
   * 
   * @param o
   *          the desired object.
   * @return the 1-based position from the top of the stack where the object is
   *         located; the return value <code>-1</code> indicates that the object
   *         is not on the stack.
   */
  public int search(Object o) {
    int i = lastIndexOf(o);
    if (i >= 0) {
      return size() - i;
    }
    return -1;
  }
  
}
