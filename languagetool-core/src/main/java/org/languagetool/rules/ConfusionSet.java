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
 * @since 3.0
 */
public class ConfusionSet {

  private final Set<ConfusionString> set = new HashSet<>();
  private final int factor;

  public ConfusionSet(int factor, List<ConfusionString> confusionStrings) {
    this.factor = factor;
    set.addAll(confusionStrings);
  }

  public ConfusionSet(int factor, String... words) {
    this.factor = factor;
    for (String word : words) {
      set.add(new ConfusionString(word, null));
    }
  }

  /* Alternative must be at least this much more probable to be considered correct. */
  public int getFactor() {
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
    ConfusionSet that = (ConfusionSet) o;
    if (factor != that.factor) return false;
    if (!set.equals(that.set)) return false;
    return true;
  }

  @Override
  public int hashCode() {
    int result = set.hashCode();
    result = 31 * result + factor;
    return result;
  }
}
