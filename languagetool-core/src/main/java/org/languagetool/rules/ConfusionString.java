/* LanguageTool, a natural language style checker 
 * Copyright (C) 2015 Daniel Naber (http://www.danielnaber.de)
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

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * A string in a {@link ConfusionPair} - for internal use only.
 * @since 3.0
 */
public class ConfusionString {

  private final String str;
  private final String description;

  ConfusionString(String str, String description) {
    this.str = Objects.requireNonNull(str);
    this.description = description;
  }

  public String getString() {
    return str;
  }

  @Nullable
  public String getDescription() {
    return description;
  }

  @Override
  public String toString() {
    return str;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ConfusionString other = (ConfusionString) o;
    return Objects.equals(str, other.str) && Objects.equals(description, other.description);
  }

  @Override
  public int hashCode() {
    return Objects.hash(str, description);
  }
}
