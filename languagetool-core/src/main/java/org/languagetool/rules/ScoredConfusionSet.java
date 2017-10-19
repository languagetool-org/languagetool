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
 * A ScoredConfusionSet has a positive score associated with it.
 * TODO remove code duplication with ConfusionSet
 */
class ScoredConfusionSet {

  private final Set<ConfusionString> set = new HashSet<>();
  private final float score;

  /**
   * @param score the score that a string must get at least to be considered a correction, must be &gt; 0
   */
  public ScoredConfusionSet(float score, List<ConfusionString> confusionStrings) {
    if (score <= 0) {
      throw new IllegalArgumentException("factor must be > 0: " + score);
    }
    this.score = score;
    set.addAll(Objects.requireNonNull(confusionStrings));
  }

  /**
   * @param score the score that a string must get at least to be considered a correction, must be &gt; 0
   */
  public ScoredConfusionSet(float score, String... words) {
    if (score <= 0) {
      throw new IllegalArgumentException("factor must be > 0: " + score);
    }
    Objects.requireNonNull(words);
    this.score = score;
    for (String word : words) {
      set.add(new ConfusionString(word, null));
    }
  }

  /* Alternative must be at least this much more probable to be considered correct. */
  public float getScore() {
    return score;
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
    org.languagetool.rules.ScoredConfusionSet other = (org.languagetool.rules.ScoredConfusionSet) o;
    return Objects.equals(set, other.set) && Objects.equals(score, other.score);
  }

  @Override
  public int hashCode() {
    return Objects.hash(set, score);
  }

}

