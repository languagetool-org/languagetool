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

package org.languagetool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.languagetool.tools.StringTools;

/**
 * An array of {@link AnalyzedToken}s used to store multiple POS tags and lemmas
 * for a given single token.
 * 
 * @author Marcin Milkowski
 */
public class AnalyzedTokenReadings {

  protected AnalyzedToken[] anTokReadings;

  private int startPos;
  private String token;

  private boolean isWhitespace;
  private boolean isLinebreak;
  private boolean isSentEnd;
  private boolean isSentStart;
  private boolean isParaEnd;
  private boolean isWhitespaceBefore;
  
  /**
   * If true, then the token is marked up as immune against tests:
   * it should never be matched by any rule. Used to have generalized
   * mechanism for exceptions in rules.
   */
  private boolean isImmunized;
  
  /**
   * Used to hold the string representation of the disambiguator actions on a token.
   */
  private String historicalAnnotations = "";
   
  
  public AnalyzedTokenReadings(final AnalyzedToken[] token, final int startPos) {
    anTokReadings = token.clone();
    this.startPos = startPos;
    init();
  }
    
  public AnalyzedTokenReadings(final List<AnalyzedToken> tokens, final int startPos) {
    anTokReadings = tokens.toArray(new AnalyzedToken[tokens.size()]);
    this.startPos = startPos;
    init();
  }

  public AnalyzedTokenReadings(final AnalyzedToken token, final int startPos) {
    this(token);
    this.startPos = startPos;
  }

  AnalyzedTokenReadings(final AnalyzedToken token) {
    anTokReadings = new AnalyzedToken[1];
    anTokReadings[0] = token;
    isWhitespaceBefore = token.isWhitespaceBefore();
    init();
  }
    
  private void init() {
    token = anTokReadings[0].getToken();
    isWhitespace = StringTools.isWhitespace(token);
    isLinebreak = "\n".equals(token) || "\r\n".equals(token)
        || "\r".equals(token) || "\n\r".equals(token);
    isSentStart = JLanguageTool.SENTENCE_START_TAGNAME.equals(anTokReadings[0]
        .getPOSTag());
    isParaEnd = hasPosTag(JLanguageTool.PARAGRAPH_END_TAGNAME);
    isSentEnd = hasPosTag(JLanguageTool.SENTENCE_END_TAGNAME);
    setNoRealPOStag();
  }

  public final List<AnalyzedToken> getReadings() {
    return Arrays.asList(anTokReadings);
  }

  /**
   * Get a token reading.
   * @see #getReadingsLength() getReadingsLength() for how many token readings there are
   */
  public final AnalyzedToken getAnalyzedToken(final int idx) {
    return anTokReadings[idx];
  }

  /**
   * Checks if the token has a particular POS tag.
   * 
   * @param posTag POS tag to look for
   */
  public final boolean hasPosTag(final String posTag) {
    boolean found = false;
    for (final AnalyzedToken reading : anTokReadings) {
      if (reading.getPOSTag() != null) {
        found = posTag.equals(reading.getPOSTag());
        if (found) {
          break;
        }
      }
    }
    return found;
  }

  /**
   * Checks if one of the token's readings has a particular lemma.
   *
   * @param lemma lemma POS tag to look for
   */
  public final boolean hasLemma(final String lemma) {
    boolean found = false;
    for (final AnalyzedToken reading : anTokReadings) {
      if (reading.getLemma() != null) {
        found = lemma.equals(reading.getLemma());
        if (found) {
          break;
        }
      }
    }
    return found;
  }

  /**
   * Checks if the token has a particular POS tag, whereas a part of the given POS tag needs to match.
   *
   * @param posTag POS tag substring to look for
   * @since 1.8
   */
  public final boolean hasPartialPosTag(final String posTag) {
    boolean found = false;
    for (final AnalyzedToken reading : anTokReadings) {
      if (reading.getPOSTag() != null) {
        found = reading.getPOSTag().contains(posTag);
        if (found) {
          break;
        }
      }
    }
    return found;
  }

  /**
   * Add a new reading.
   * @param token new reading, given as {@link AnalyzedToken}
   */
  public final void addReading(final AnalyzedToken token) {
    final ArrayList<AnalyzedToken> l = new ArrayList<>();
    for (int i = 0; i < anTokReadings.length - 1; i++) {
      l.add(anTokReadings[i]);
    }
    if (anTokReadings[anTokReadings.length - 1].getPOSTag() != null) {
      l.add(anTokReadings[anTokReadings.length - 1]);
    }
    token.setWhitespaceBefore(isWhitespaceBefore);
    l.add(token);
    anTokReadings = l.toArray(new AnalyzedToken[l.size()]);
    if (token.getToken().length() > this.token.length()) { //in case a longer token is added
      this.token = token.getToken();
    }
    anTokReadings[anTokReadings.length - 1].setWhitespaceBefore(isWhitespaceBefore);
    isParaEnd = hasPosTag(JLanguageTool.PARAGRAPH_END_TAGNAME);
    isSentEnd = hasPosTag(JLanguageTool.SENTENCE_END_TAGNAME);
    setNoRealPOStag();
  }   

  /**
   * Removes a reading from the list of readings. Note: if the token
   * has only one reading, then a new reading with an empty POS tag
   * and an empty lemma is created.
   * @param token reading to be removed
   */
  public final void removeReading(final AnalyzedToken token) {
    final ArrayList<AnalyzedToken> l = new ArrayList<>();
    final AnalyzedToken tmpTok = new AnalyzedToken(token.getToken(), token.getPOSTag(), token.getLemma());
    tmpTok.setWhitespaceBefore(isWhitespaceBefore);
    for (AnalyzedToken anTokReading : anTokReadings) {
      if (!anTokReading.matches(tmpTok)) {
        l.add(anTokReading);
      }
    }
    if (l.isEmpty()) {
      l.add(new AnalyzedToken(this.token, null, null));
    }
    anTokReadings = l.toArray(new AnalyzedToken[l.size()]);
    setNoRealPOStag();
  }

  /** 
   * Removes all the readings but the one that match the token given.
   * @since 1.5
   * @param token Token to be matched
   */
  public final void leaveReading(final AnalyzedToken token) {
    final ArrayList<AnalyzedToken> l = new ArrayList<>();
    final AnalyzedToken tmpTok = new AnalyzedToken(token.getToken(), token.getPOSTag(), token.getLemma());
    tmpTok.setWhitespaceBefore(isWhitespaceBefore);
    for (AnalyzedToken anTokReading : anTokReadings) {
      if (anTokReading.matches(tmpTok)) {
        l.add(anTokReading);
      }
    }
    if (l.isEmpty()) {
      l.add(new AnalyzedToken(this.token, null, null));
    }
    anTokReadings = l.toArray(new AnalyzedToken[l.size()]);
    setNoRealPOStag();
  }

  /**
   * Number of readings.
   */
  public final int getReadingsLength() {
    return anTokReadings.length;
  }

  public final boolean isWhitespace() {
    return isWhitespace;
  }
  
  /**
   * Returns true if the token equals {@code \n}, {@code \r}, {@code \n\r}, or {@code \r\n}.
   */
  public final boolean isLinebreak() {
    return isLinebreak;
  }

  public final boolean isSentStart() {
    return isSentStart;
  }

  /**
   * @return true when the token is a last token in a paragraph.
   */
  public final boolean isParaEnd() {
    return isParaEnd;
  }

  /**
   * Add PARA_END tag.
   */
  public void setParaEnd() {
    if (!isParaEnd()) {
      final AnalyzedToken paragraphEnd = new AnalyzedToken(getToken(),
          JLanguageTool.PARAGRAPH_END_TAGNAME, getAnalyzedToken(0).getLemma());
      addReading(paragraphEnd);
    }
  }

  /**
   * @return true when the token is a last token in a sentence.
   */
  public final boolean isSentEnd() {
    return isSentEnd;
  }
  
  /**
   * @since 0.9.9
   * @return true if the token is LibreOffice/OpenOffice field code.
   */
  public final boolean isFieldCode() {
    return "\u0001".equals(token) || "\u0002".equals(token);
  }

  /**
   * Add a SENT_END tag.
   */
  public final void setSentEnd() {
    if (!isSentEnd()) {
      final AnalyzedToken sentenceEnd = new AnalyzedToken(getToken(),
          JLanguageTool.SENTENCE_END_TAGNAME, getAnalyzedToken(0).getLemma());
      addReading(sentenceEnd);
    }
  }

  public final int getStartPos() {
    return startPos;
  }

  public final void setStartPos(final int position) {
    startPos = position;
  }

  public final String getToken() {
    return token;
  }

  public final void setWhitespaceBefore(final boolean isWhiteSpaceBefore) {
    isWhitespaceBefore = isWhiteSpaceBefore;
    for (final AnalyzedToken aTok : anTokReadings) {
      aTok.setWhitespaceBefore(isWhiteSpaceBefore);
    }
  }

  public final boolean isWhitespaceBefore() {
    return isWhitespaceBefore;
  }

  public final void immunize() {
    isImmunized = true;
  }
  
  public final boolean isImmunized() {
    return isImmunized;
  }
  
  /**
   * Sets the flag on AnalyzedTokens to make matching
   * on {@code UNKNOWN} POS tag correct in the Element class.
   */
  private void setNoRealPOStag() {
    boolean hasNoPOStag = !isLinebreak();
    for (AnalyzedToken an: anTokReadings) {
      if (JLanguageTool.PARAGRAPH_END_TAGNAME.equals(an.getPOSTag())
              || JLanguageTool.SENTENCE_END_TAGNAME.equals(an.getPOSTag())) {
        continue;
      }
      if (an.getPOSTag() != null) {
        hasNoPOStag = false;
      }
    }
    for (AnalyzedToken an: anTokReadings) {
      an.setNoPOSTag(hasNoPOStag);
    }
  }

  /**
   * Used to track disambiguator actions.
   * @return the historicalAnnotations
   */
  public String getHistoricalAnnotations() {
    return historicalAnnotations;
  }

  /**
   * Used to track disambiguator actions.
   * @param historicalAnnotations the historicalAnnotations to set
   */
  public void setHistoricalAnnotations(String historicalAnnotations) {
    this.historicalAnnotations = historicalAnnotations;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append(token);
    sb.append("[");
    for (final AnalyzedToken element : anTokReadings) {
      sb.append(element);
      if (!element.isWhitespaceBefore()) {
        sb.append("*");
      }
      sb.append(",");
    }
    sb.delete(sb.length() - 1, sb.length());
    sb.append("]");
    return sb.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(anTokReadings);
    result = prime * result + (isLinebreak ? 1231 : 1237);
    result = prime * result + (isParaEnd ? 1231 : 1237);
    result = prime * result + (isSentEnd ? 1231 : 1237);
    result = prime * result + (isSentStart ? 1231 : 1237);
    result = prime * result + (isWhitespace ? 1231 : 1237);
    result = prime * result + (isWhitespaceBefore ? 1231 : 1237);
    result = prime * result + startPos;
    result = prime * result + ((token == null) ? 0 : token.hashCode());
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
    final AnalyzedTokenReadings other = (AnalyzedTokenReadings) obj;
    if (!Arrays.equals(anTokReadings, other.anTokReadings))
      return false;
    if (isLinebreak != other.isLinebreak)
      return false;
    if (isParaEnd != other.isParaEnd)
      return false;
    if (isSentEnd != other.isSentEnd)
      return false;
    if (isSentStart != other.isSentStart)
      return false;
    if (isWhitespace != other.isWhitespace)
      return false;
    if (isWhitespaceBefore != other.isWhitespaceBefore)
      return false;
    if (isImmunized != other.isImmunized)
      return false;
    if (startPos != other.startPos)
      return false;
    if (token == null) {
      if (other.token != null)
        return false;
    } else if (!token.equals(other.token))
      return false;
    return true;
  }
  
}
