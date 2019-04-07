/* LanguageTool, a natural language style checker 
 * Copyright (C) 2019 Daniel Naber (http://www.danielnaber.de)
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
 * Two words that can easily be confused - for internal use only.
 * @since 4.6
 */
public class ConfusionPair {

  private final ConfusionString term1;
  private final ConfusionString term2;
  private final long factor;
  private final boolean bidirectional;

  public ConfusionPair(ConfusionString cs1, ConfusionString cs2, long factor, boolean bidirectional) {
    this.term1 = Objects.requireNonNull(cs1);
    this.term2 = Objects.requireNonNull(cs2);
    if (factor < 1) {
      throw new IllegalArgumentException("factor must be >= 1: " + factor);
    }
    this.factor = factor;
    this.bidirectional = bidirectional;
  }

  public ConfusionPair(String token1, String token2, Long factor, boolean bidirectional) {
    term1 = new ConfusionString(token1, null);
    term2 = new ConfusionString(token2, null);
    if (factor < 1) {
      throw new IllegalArgumentException("factor must be >= 1: " + factor);
    }
    this.factor = factor;
    this.bidirectional = bidirectional;
  }

  /* Alternative must be at least this much more probable to be considered correct. */
  public long getFactor() {
    return factor;
  }

  /* If true, only direction term1 -> term2 is possible, i.e. if term1 is used incorrectly, term2 is suggested - not vice versa. */
  public boolean isBidirectional() {
    return bidirectional;
  }

  public ConfusionString getTerm1() {
    return term1;
  }
  
  public ConfusionString getTerm2() {
    return term2;
  }
  
  public List<ConfusionString> getTerms() {
    return Collections.unmodifiableList(Arrays.asList(term1, term2));
  }
  
  public List<ConfusionString> getUppercaseFirstCharTerms() {
    List<ConfusionString> result = new ArrayList<>();
    result.add(new ConfusionString(StringTools.uppercaseFirstChar(term1.getString()), term1.getDescription()));
    result.add(new ConfusionString(StringTools.uppercaseFirstChar(term2.getString()), term2.getDescription()));
    return Collections.unmodifiableList(result);
  }

  @Override
  public String toString() {
    return term1 + (bidirectional ? "; " : " -> ") + term2;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ConfusionPair that = (ConfusionPair) o;
    return factor == that.factor &&
            bidirectional == that.bidirectional &&
            term1.equals(that.term1) &&
            term2.equals(that.term2);
  }

  @Override
  public int hashCode() {
    return Objects.hash(term1, term2, factor, bidirectional);
  }
}
