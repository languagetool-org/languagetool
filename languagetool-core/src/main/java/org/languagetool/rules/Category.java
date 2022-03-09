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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * A rule's category. Categories are used to group rules for
 * a better overview.
 */
public final class Category {

  public enum Location {
    /** The rules in this category are part of the main distribution of
     * LanguageTool and are thus available on <a href="http://community.languagetool.org">community.languagetool.org</a>. */
    INTERNAL,
    /** The rules in this category are not part of the main distribution of LanguageTool. */
    EXTERNAL
  }

  private final String name;
  private final CategoryId id;
  private final Location location;
  private final boolean defaultOff;
  private final String tabName;
  
  /**
   * @since 3.3
   */
  public Category(CategoryId id, String name) {
    this(id, name, Location.INTERNAL, true);
  }

  /** @since 3.3 */
  public Category(CategoryId id, String name, Location location) {
    this(id, name, location, true);
  }

  /** @since 3.3 */
  public Category(CategoryId id, String name, Location location, boolean onByDefault) {
    this(id, name, location, onByDefault, null);
  }

  /** @since 4.3 */
  public Category(CategoryId id, String name, Location location, boolean onByDefault, String tabName) {
    this.id = Objects.requireNonNull(id);
    this.name = Objects.requireNonNull(name);
    this.location = location;
    this.defaultOff = !onByDefault;
    this.tabName = tabName;
  }

  /** @since 3.3 */
  @NotNull
  public CategoryId getId() {
    return id;
  }

  @NotNull
  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return name;
  }

  /** @since 4.3 */
  @Nullable
  public String getTabName() {
    return tabName;
  }

  /**
   * Checks whether the category has been turned off
   * by default by the category author.
   * @return True if the category is turned off by default.
   */
  public boolean isDefaultOff() {
    return defaultOff;
  }

  /**
   * @since 2.8
   */
  public Location getLocation() {
    return location;
  }

}
