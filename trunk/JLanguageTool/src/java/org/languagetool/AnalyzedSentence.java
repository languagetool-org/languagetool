/* LanguageTool, a natural language style checker
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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
package de.danielnaber.languagetool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.danielnaber.languagetool.tagging.de.AnalyzedGermanTokenReadings;

/**
 * A sentence that has been tokenized and analyzed.
 * 
 * @author Daniel Naber
 */
public class AnalyzedSentence {

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(nonBlankTokens);
    result = prime * result + Arrays.hashCode(tokens);
    result = prime * result + Arrays.hashCode(whPositions);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final AnalyzedSentence other = (AnalyzedSentence) obj;
    if (!Arrays.equals(nonBlankTokens, other.nonBlankTokens))
      return false;
    if (!Arrays.equals(tokens, other.tokens))
      return false;
    if (!Arrays.equals(whPositions, other.whPositions))
      return false;
    return true;
  }

  private final AnalyzedTokenReadings[] tokens;

  private AnalyzedTokenReadings[] nonBlankTokens;

  /**
   * Array mapping positions of tokens as returned with
   * getTokensWithoutWhitespace() to the internal tokens array.
   */
  private int[] whPositions;

  /**
   * Sets {@link AnalyzedTokenReadings}. Whitespace is also a token.
   */
  public AnalyzedSentence(final AnalyzedTokenReadings[] tokens) {    
    this.tokens = tokens;
  }
  
  public AnalyzedSentence(final AnalyzedTokenReadings[] tokens, final 
      int[] whPositions) {
    this.tokens = tokens;
    this.setWhPositions(whPositions);
    getTokensWithoutWhitespace();
  } 

  /**
   * Returns the {@link AnalyzedTokenReadings} of the analyzed text. Whitespace
   * is also a token.
   */
  public final AnalyzedTokenReadings[] getTokens() {
    return tokens;
  }

  /**
   * Returns the {@link AnalyzedTokenReadings} of the analyzed text, with
   * whitespace tokens removed but with the artificial <code>SENT_START</code>
   * token included.
   */
  public final AnalyzedTokenReadings[] getTokensWithoutWhitespace() {
    if (nonBlankTokens == null) {
      int whCounter = 0;
      int nonWhCounter = 0;
      final int[] mapping = new int[tokens.length + 1];
      final List<AnalyzedTokenReadings> l = new ArrayList<AnalyzedTokenReadings>();
      for (final AnalyzedTokenReadings token : tokens) {
        if (!token.isWhitespace() || token.isSentStart() || token.isSentEnd()
            || token.isParaEnd()) {
          l.add(token);
          mapping[nonWhCounter] = whCounter;
          nonWhCounter++;
        }
        whCounter++;
      }
      setNonBlankTokens(l.toArray(new AnalyzedTokenReadings[l.size()]));
      setWhPositions(mapping.clone());
    }
    return nonBlankTokens.clone();
  }

  /**
   * Get a position of a non-whitespace token in the original sentence with
   * whitespace.
   * 
   * @param nonWhPosition
   *          Position of a non-whitespace token
   * @return int position in the original sentence.
   */
  public final int getOriginalPosition(final int nonWhPosition) {
    if (nonBlankTokens == null) {
      getTokensWithoutWhitespace();
    }
    return getWhPositions()[nonWhPosition];
  }

  @Override
  public final String toString() {
    final StringBuilder sb = new StringBuilder();
    for (final AnalyzedTokenReadings element : tokens) {
      if (!element.isWhitespace()) {
        sb.append(element.getToken());
        sb.append('[');
      }
      for (int j = 0; j < element.getReadingsLength(); j++) {
        final String posTag = element.getAnalyzedToken(j).getPOSTag();
        if (element.isSentStart()) {
          sb.append("<S>");
        } else if (JLanguageTool.SENTENCE_END_TAGNAME.equals(element
            .getAnalyzedToken(j).getPOSTag())) {
          sb.append("</S>");
        } else if (JLanguageTool.PARAGRAPH_END_TAGNAME.equals(element
            .getAnalyzedToken(j).getPOSTag())) {
          sb.append("<P/>");
        } else if (element.getAnalyzedToken(j) != null && posTag == null
            && !(element instanceof AnalyzedGermanTokenReadings)) {
          // FIXME: don't depend on AnalyzedGermanTokenReadings here
          sb.append(element.getAnalyzedToken(j).getToken());
        } else {
          if (!element.isWhitespace()) {
            sb.append(element.getAnalyzedToken(j));
            if (j < element.getReadingsLength() - 1) {
              sb.append(',');
            }
          }
        }
      }
      if (!element.isWhitespace()) {
        sb.append(']');
      } else {
        sb.append(' ');
      }

    }
    return sb.toString();
  }

  /**
   * @param whPositions the whPositions to set
   */
  public void setWhPositions(int[] whPositions) {
    this.whPositions = whPositions;
  }

  /**
   * @return the whPositions
   */
  public int[] getWhPositions() {
    return whPositions;
  }

  /**
   * @param nonBlankTokens the nonBlankTokens to set
   */
  public void setNonBlankTokens(AnalyzedTokenReadings[] nonBlankTokens) {
    this.nonBlankTokens = nonBlankTokens;
  } 
  
}
