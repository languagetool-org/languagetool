/* LanguageTool, a natural language style checker
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.tagging;

import java.util.HashSet;
import java.util.Set;

/**
 * The unsorted values of a {@link TokenPoS}.
 * 
 * @since 2.6
 */
public class ValueSet {

  private final Set<String> values = new HashSet<>();

  ValueSet() {
  }

  public Set<String> getValues() {
    return values;
  }

  boolean hasOneOf(ValueSet otherValueSet) {
    for (String val : otherValueSet.getValues()) {
      if (values.contains(val)) {
        return true;
      }
    }
    return false;
  }

  ValueSet add(String value) {
    values.add(value);
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ValueSet valueSet = (ValueSet) o;
    if (!values.equals(valueSet.values)) return false;
    return true;
  }

  @Override
  public int hashCode() {
    return values.hashCode();
  }

  @Override
  public String toString() {
    return values.toString();
  }
}
