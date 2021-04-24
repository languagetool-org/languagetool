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

import org.languagetool.tools.StringTools;

import java.util.*;

/**
 * Words that can easily be confused - for internal use only.
 * Even though there can be more words in the set, usually there
 * are two, as the factor is specific for this pair of words.
 * @since 3.0
 */
public class ConfusionSet {

  private final Set<ConfusionString> set = new HashSet<>();
  private final long factor;

  /**
   * @param factor the factor that one string must be more probable than the other to be considered a correction, must be &gt;= 1
   */
  public ConfusionSet(long factor, List<ConfusionString> confusionStrings) {
    if (factor < 1) {
      throw new IllegalArgumentException("factor must be >= 1: " + factor);
    }
    this.factor = factor;
    set.addAll(Objects.requireNonNull(confusionStrings));
  }

  /**
   * @param factor the factor that one string must be more probable than the other to be considered a correction, must be &gt;= 1
   */
  public ConfusionSet(long factor, String... words) {
    if (factor < 1) {
      throw new IllegalArgumentException("factor must be >= 1: " + factor);
    }
    Objects.requireNonNull(words);
    this.factor = factor;
    for (String word : words) {
      set.add(new ConfusionString(word, null));
    }
  }

  /* Alternative must be at least this much more probable to be considered correct. */
  public long getFactor() {
    return factor;
  }

  public Set<ConfusionString> getSet() {
    return Collections.unmodifiableSet(set);
  }

  public Set<ConfusionString> getUppercaseFirstCharSet() {
    Set<ConfusionString> result = new HashSet<>();
    for (ConfusionString s : set) {
      ConfusionString newString = new ConfusionString(StringTools.uppercaseFirstChar(s.getString()), s.getDescription());
      result.add(newString);
    }
    return Collections.unmodifiableSet(result);
  }

  @Override
  public String toString() {
    return set.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ConfusionSet other = (ConfusionSet) o;
    return Objects.equals(set, other.set) && Objects.equals(factor, other.factor);
  }

  @Override
  public int hashCode() {
    return Objects.hash(set, factor);
  }
  
}
