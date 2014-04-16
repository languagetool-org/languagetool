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

import org.languagetool.JLanguageTool;

import java.util.Map;

/**
 * A structured representation of a token's part-of-speech analysis. Can
 * contain more than one value per property, e.g. {@code person=1|2}.
 * @since 2.6
 */
public class TokenPoS {
  
  private final Map<String,ValueSet> posSet;

  public static TokenPoS getSentenceStart() {
    return new TokenPoSBuilder().add("pos", JLanguageTool.SENTENCE_START_TAGNAME).create();
  }

  public static TokenPoS getSentenceEnd() {
    return new TokenPoSBuilder().add("pos", JLanguageTool.SENTENCE_END_TAGNAME).create();
  }

  TokenPoS(Map<String, ValueSet> posSet) {
    this.posSet = posSet;
  }

  public ValueSet getValues(String key) {
    return posSet.get(key);
  }

  public boolean isEmpty() {
    return posSet.size() == 0;
  }

  public boolean isSubsetOf(TokenPoS tokenPosFromText) {
    for (Map.Entry<String, ValueSet> entry : posSet.entrySet()) {
      if (!tokenPosFromText.hasType(entry.getKey())) {
        return false;
      } else {
        ValueSet textValues = tokenPosFromText.getValues(entry.getKey());
        if (!textValues.hasOneOf(entry.getValue())) {
          // at least one of the values must be in tokenPosFromText's values
          return false;
        }
      }
    }
    return true;
  }

  private boolean hasType(String key) {
    return posSet.containsKey(key);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TokenPoS tokenPoS = (TokenPoS) o;
    if (!posSet.equals(tokenPoS.posSet)) return false;
    return true;
  }

  @Override
  public int hashCode() {
    return posSet.hashCode();
  }

  @Override
  public String toString() {
    return posSet.toString();
  }
}
