/* LanguageTool, a natural language style checker 
 * Copyright (C) 2006 Daniel Naber (http://www.danielnaber.de)
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
package de.danielnaber.languagetool.rules;

public class Category {

  private static final int DEFAULT_PRIORITY = 50;
  
  private int priority;
  private String name;

  /**
   * Create a new category with the given name and priority.
   * @param name name of the category
   * @param priority a value between 0 and 100 (inclusive)
   */
  public Category(final String name, final int priority) {
    if (priority < 0 || priority > 100)
      throw new IllegalArgumentException("priority must be in range 0 - 100");
    this.name = name;
    this.priority = priority;
  }

  /**
   * Create a new category with the default priority (50).
   * @param name name of the category
   */
  public Category(final String name) {
    this(name, DEFAULT_PRIORITY);
  }

  public String getName() {
    return name;
  }

  public int getPriority() {
    return priority;
  }
  
  public String toString() {
    return name + "(prio=" +priority+ ")";
  }
  
}
