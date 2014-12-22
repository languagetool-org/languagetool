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
package org.languagetool.rules;

/**
 * A rule's category. Categories are used to group rules for
 * a better overview.
 * 
 * @author Daniel Naber
 */
public class Category {

  public enum Location {
    /** The rules in this category are part of the main distribution of
     * LanguageTool and are thus available on <a href="http://community.languagetool.org">community.languagetool.org</a>. */
    INTERNAL,
    /** The rules in this category are not part of the main distribution of LanguageTool. */
    EXTERNAL
  }

  private static final int DEFAULT_PRIORITY = 50;
  
  private final int priority;
  private final String name;
  private final Location location;

  private boolean defaultOff;

  /**
   * Create a new category with the given name and priority.
   * @param name name of the category
   * @param priority a value between 0 and 100 (inclusive)
   * @deprecated priority will be removed, as it had not been used (deprecated since 2.8)
   */
  public Category(final String name, final int priority, Location location) {
    if (priority < 0 || priority > 100) {
      throw new IllegalArgumentException("priority must be in range 0 - 100: " + priority);
    }
    this.name = name;
    this.priority = priority;
    this.location = location;
  }

  /**
   * @since 2.8
   * @deprecated priority will be removed, as it had not been used (deprecated since 2.8)
   */
  Category(final String name, final int priority) {
    this(name, priority, Location.INTERNAL);
  }

  /**
   * Create a new category with the default priority (50).
   * @param name name of the category
   */
  public Category(final String name) {
    this(name, Location.INTERNAL);
  }

  /**
   * Create a new category with the default priority (50).
   * @param name name of the category
   * @since 2.8
   */
  public Category(String name, Location location) {
    this(name, DEFAULT_PRIORITY, location);
  }

  public String getName() {
    return name;
  }

  public int getPriority() {
    return priority;
  }
  
  @Override
  public String toString() {
    return name;
  }

  /**
   * Checks whether the category has been turned off
   * by default by the category author.
   * @return True if the category is turned off by default.
   */
  public final boolean isDefaultOff() {
    return defaultOff;
  }
  
  /**
   * Turns the category off by default.
   */
  public final void setDefaultOff() {
    defaultOff = true;
  }

  /**
   * @since 2.8
   */
  public Location getLocation() {
    return location;
  }

}
