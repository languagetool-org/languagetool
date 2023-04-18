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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.jetbrains.annotations.NotNull;
import org.languagetool.chunking.ChunkTag;
import org.languagetool.tools.StringTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import static org.languagetool.JLanguageTool.*;

/**
 * An array of {@link AnalyzedToken}s used to store multiple POS tags and lemmas
 * for a given single token.
 * 
 * @author Marcin Milkowski
 */
public final class AnalyzedTokenReadings implements Iterable<AnalyzedToken> {

  private static final Logger logger = LoggerFactory.getLogger(AnalyzedTokenReadings.class);
  private static final Pattern NON_WORD_REGEX = Pattern.compile("[.?!…:;,~’'\"„“”»«‚‘›‹()\\[\\]\\-–—*×∗·+÷/=]");

  private final boolean isWhitespace;
  private final boolean isLinebreak;
  private final boolean isSentStart;
  private final boolean isPosTagUnknown;

  private AnalyzedToken[] anTokReadings;
  private int startPos;
  private int fixPos;
  private String token;
  private String cleanToken;
  private List<ChunkTag> chunkTags = Collections.emptyList();
  private boolean isSentEnd;
  private boolean isParaEnd;
  private boolean isWhitespaceBefore;
  private String whitespaceBeforeChar;
  private boolean hasTypographicApostrophe = false;

  // If true, then the token is marked up as immune against tests:
  // it should never be matched by any rule. Used to have generalized
  // mechanism for exceptions in rules.
  private boolean isImmunized;
  private int immunizationSourceLine;

  // If true, then the token is marked up as ignored in all spelling rules:
  // other rules can freely match it.
  private boolean isIgnoredBySpeller;

  // Used to hold the string representation of the disambiguator actions on a token.
  private String historicalAnnotations = "";

  // True if the token has the same lemma value for all tokens.
  // Can be used internally to optimize matching.
  private boolean hasSameLemmas;

  public AnalyzedTokenReadings(AnalyzedToken[] tokens, int startPos) {
    this(Arrays.asList(tokens), startPos);
  }

  public AnalyzedTokenReadings(AnalyzedToken token, int startPos) {
    this(Collections.singletonList(token), startPos);
  }

  public AnalyzedTokenReadings(List<AnalyzedToken> tokens, int startPos) {
    anTokReadings = tokens.toArray(new AnalyzedToken[0]);
    this.startPos = startPos;
    token = anTokReadings[0].getToken();
    isWhitespace = StringTools.isWhitespace(token);
    isWhitespaceBefore = anTokReadings[0].isWhitespaceBefore();
    isLinebreak = "\n".equals(token) || "\r\n".equals(token) || "\r".equals(token) || "\n\r".equals(token);
    isSentStart = SENTENCE_START_TAGNAME.equals(anTokReadings[0].getPOSTag());
    isParaEnd = hasPosTag(PARAGRAPH_END_TAGNAME);
    isSentEnd = hasPosTag(SENTENCE_END_TAGNAME);
    isPosTagUnknown = tokens.size() == 1 && tokens.get(0).getPOSTag() == null;
    setNoRealPOStag();
    hasSameLemmas = areLemmasSame();
    whitespaceBeforeChar = "";
  }
  
  // Constructor from a previous AnalyzedTokenReadings with new readings, and annotation of the change  
  public AnalyzedTokenReadings(AnalyzedTokenReadings oldAtr, List<AnalyzedToken> newReadings, String ruleApplied) {
    this(newReadings, oldAtr.getStartPos());
    if (oldAtr.isSentenceEnd()) {
      this.setSentEnd();
    }
    if (oldAtr.isParagraphEnd()) {
      this.setParagraphEnd();
    }
    this.setWhitespaceBefore(oldAtr.getWhitespaceBefore());
    this.setChunkTags(oldAtr.getChunkTags());
    if (oldAtr.isImmunized()) {
      this.immunize(oldAtr.getImmunizationSourceLine());
    }
    if (oldAtr.isIgnoredBySpeller()) {
      this.ignoreSpelling();
    }
    if (oldAtr.hasTypographicApostrophe()) {
      this.setTypographicApostrophe();
    }
    this.setHistoricalAnnotations(oldAtr.getHistoricalAnnotations());
    addHistoricalAnnotations(oldAtr.toString(), ruleApplied); 
  }

  public AnalyzedTokenReadings(AnalyzedToken token) {
    this(Collections.singletonList(token), 0);
  }

  public List<AnalyzedToken> getReadings() {
    return Arrays.asList(anTokReadings);
  }

  /**
   * Get a token reading.
   * @see #getReadingsLength() getReadingsLength() for how many token readings there are
   */
  public AnalyzedToken getAnalyzedToken(int idx) {
    return anTokReadings[idx];
  }

  /**
   * Checks if the token has a particular POS tag.
   * @param posTag POS tag to look for
   */
  public boolean hasPosTag(String posTag) {
    boolean found = false;
    for (AnalyzedToken reading : anTokReadings) {
      found = posTag.equals(reading.getPOSTag());
      if (found) {
        break;
      }
    }
    return found;
  }
  
  /**
   * Checks if the token has a particular POS tag and lemma.
   * @param posTag POS tag and lemma to look for
   */
  public boolean hasPosTagAndLemma(String posTag, String lemma) {
    boolean found = false;
    for (AnalyzedToken reading : anTokReadings) {
      found = posTag.equals(reading.getPOSTag()) && lemma.equals(reading.getLemma());
      if (found) {
        break;
      }
    }
    return found;
  }

  /**
   * Checks if there is at least one POS tag
   * @since 4.7
   */
  public boolean hasReading() {
    return anTokReadings != null && anTokReadings.length > 0;
  }

  /**
   * Checks if one of the token's readings has a particular lemma.
   * @param lemma lemma POS tag to look for
   */
  public boolean hasLemma(String lemma) {
    boolean found = false;
    for (AnalyzedToken reading : anTokReadings) {
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
   * Checks if one of the token's readings has one of the given lemmas
   * @param lemmas lemmas to look for
   */
  public boolean hasAnyLemma(String... lemmas) {
    boolean found = false;
    for(String lemma : lemmas) {
      for (AnalyzedToken reading : anTokReadings) {
        found = lemma.equals(reading.getLemma());
        if (found) {
          return found;
        }
      }
    }
    return found;
  }

  /**
   * Checks if the token has a particular POS tag, where only a part of the given POS tag needs to match.
   *
   * @param posTag POS tag substring to look for
   * @since 1.8
   */
  public boolean hasPartialPosTag(String posTag) {
    boolean found = false;
    for (AnalyzedToken reading : anTokReadings) {
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
  * Checks if the token has any of the given particular POS tags (only a part of the given POS tag needs to match)
  *
  * @param posTags POS tag substring to look for
  * @since 4.0
  */
  public boolean hasAnyPartialPosTag(String... posTags) {
    for (String posTag : posTags) {
      if (hasPartialPosTag(posTag)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Checks if the token has a POS tag starting with the given string.
   *
   * @param posTag POS tag substring to look for
   * @since 4.0
   */
  public boolean hasPosTagStartingWith(String posTag) {
    boolean found = false;
    for (AnalyzedToken reading : anTokReadings) {
      if (reading.getPOSTag() != null) {
        found = reading.getPOSTag().startsWith(posTag);
        if (found) {
          break;
        }
      }
    }
    return found;
  }

  /**
   * Checks if at least one of the readings matches a given POS tag regex.
   *
   * @param posTagRegex POS tag regular expression to look for
   * @since 2.9
   */
  public boolean matchesPosTagRegex(String posTagRegex) {
    Pattern pattern = Pattern.compile(posTagRegex);
    boolean found = false;
    for (AnalyzedToken reading : anTokReadings) {
      if (reading.getPOSTag() != null) {
        found = pattern.matcher(reading.getPOSTag()).matches();
        if (found) {
          break;
        }
      }
    }
    return found;
  }
  
  public boolean matchesChunkRegex(String chunkRegex) {
    Pattern pattern = Pattern.compile(chunkRegex);
    boolean found = false;
    for ( ChunkTag chunk : getChunkTags()) {
      if (chunk != null) {
        found = pattern.matcher(chunk.getChunkTag()).matches();
        if (found) {
          break;
        }
      }
    }
    return found;
  }
  
  /**
   * Returns the first reading that matches a given POS tag regex.
   *
   * @param posTagRegex POS tag regular expression to look for
   * @since 5.5
   */
  public AnalyzedToken readingWithTagRegex(String posTagRegex) {
    Pattern pattern = Pattern.compile(posTagRegex);
    boolean found = false;
    for (AnalyzedToken reading : anTokReadings) {
      if (reading.getPOSTag() != null) {
        found = pattern.matcher(reading.getPOSTag()).matches();
        if (found) {
          return reading;
        }
      }
    }
    return null;
  }
  
  /**
   * Returns the first reading that matches a given lemma.
   * @since 5.8
   */
  public AnalyzedToken readingWithLemma(String lemma) {
    boolean found;
    for (AnalyzedToken reading : anTokReadings) {
      if (reading.getLemma() != null) {
        found = reading.getLemma().equals(lemma);
        if (found) {
          return reading;
        }
      }
    }
    return null;
  }

  /**
   * Add a new reading.
   * @param token new reading, given as {@link AnalyzedToken}
   */
  public void addReading(AnalyzedToken token, String ruleApplied) {
    String oldValue = this.toString();
    List<AnalyzedToken> l = new ArrayList<>(Arrays.asList(anTokReadings).subList(0, anTokReadings.length - 1));
    if (anTokReadings[anTokReadings.length - 1].getPOSTag() != null) {
      l.add(anTokReadings[anTokReadings.length - 1]);
    }
    token.setWhitespaceBefore(isWhitespaceBefore);
    l.add(token);
    anTokReadings = l.toArray(new AnalyzedToken[0]);
    if (token.getToken().length() > this.token.length()) { //in case a longer token is added
      this.token = token.getToken();
    }
    anTokReadings[anTokReadings.length - 1].setWhitespaceBefore(isWhitespaceBefore);
    isParaEnd = hasPosTag(PARAGRAPH_END_TAGNAME);
    isSentEnd = hasPosTag(SENTENCE_END_TAGNAME);
    setNoRealPOStag();
    hasSameLemmas = areLemmasSame();
    addHistoricalAnnotations(oldValue, ruleApplied); 
  }

  /**
   * Removes a reading from the list of readings. Note: if the token
   * has only one reading, then a new reading with an empty POS tag
   * and an empty lemma is created.
   * @param token reading to be removed
   */
  public void removeReading(AnalyzedToken token, String ruleApplied) {
    String oldValue = this.toString();
    List<AnalyzedToken> l = new ArrayList<>();
    AnalyzedToken tmpTok = new AnalyzedToken(token.getToken(), token.getPOSTag(), token.getLemma());
    tmpTok.setWhitespaceBefore(isWhitespaceBefore);
    boolean removedSentEnd = false;
    boolean removedParaEnd = false;
    for (AnalyzedToken anTokReading : anTokReadings) {
      if (!anTokReading.matches(tmpTok)) {
        l.add(anTokReading);
      } else if (SENTENCE_END_TAGNAME.equals(anTokReading.getPOSTag())) {
        removedSentEnd = true;
      } else if (PARAGRAPH_END_TAGNAME.equals(anTokReading.getPOSTag())) {
        removedParaEnd = true;
      }
    }
    if (l.isEmpty()) {
      l.add(new AnalyzedToken(this.token, null, null));
      l.get(0).setWhitespaceBefore(isWhitespaceBefore);
    }
    anTokReadings = l.toArray(new AnalyzedToken[0]);
    setNoRealPOStag();
    if (removedSentEnd) {
      isSentEnd = false;
      setSentEnd();
    }
    if (removedParaEnd) {
      isParaEnd = false;
      setParagraphEnd();
    }
    hasSameLemmas = areLemmasSame();
    addHistoricalAnnotations(oldValue, ruleApplied); 
  }

  /**
   * Removes all readings but the one that matches the token given.
   * @param token Token to be matched
   * @since 1.5
   */
  public void leaveReading(AnalyzedToken token) {
    List<AnalyzedToken> l = new ArrayList<>();
    AnalyzedToken tmpTok = new AnalyzedToken(token.getToken(), token.getPOSTag(), token.getLemma());
    tmpTok.setWhitespaceBefore(isWhitespaceBefore);
    for (AnalyzedToken anTokReading : anTokReadings) {
      if (anTokReading.matches(tmpTok)) {
        l.add(anTokReading);
      }
    }
    if (l.isEmpty()) {
      l.add(new AnalyzedToken(this.token, null, null));
      l.get(0).setWhitespaceBefore(isWhitespaceBefore);
    }
    anTokReadings = l.toArray(new AnalyzedToken[0]);
    setNoRealPOStag();
    hasSameLemmas = areLemmasSame();
  }

  /**
   * Number of readings.
   */
  public int getReadingsLength() {
    return anTokReadings.length;
  }

  public boolean isWhitespace() {
    return isWhitespace;
  }

  /**
   * Returns true if the token equals {@code \n}, {@code \r}, {@code \n\r}, or {@code \r\n}.
   */
  public boolean isLinebreak() {
    return isLinebreak;
  }

  /**
   * @since 2.3
   */
  public boolean isSentenceStart() {
    return isSentStart;
  }

  /**
   * @return true when the token is a last token in a paragraph.
   * @since 2.3
   */
  public boolean isParagraphEnd() {
    return isParaEnd;
  }

  /**
   * Add a reading with a paragraph end token unless this is already a paragraph end.
   * @since 2.3
   */
  public void setParagraphEnd() {
    if (!isParagraphEnd()) {
      AnalyzedToken paragraphEnd = new AnalyzedToken(getToken(),
          PARAGRAPH_END_TAGNAME, getAnalyzedToken(0).getLemma());
      addReading(paragraphEnd, "add_paragaph_end");
    }
  }

  /**
   * @return true when the token is a last token in a sentence.
   * @since 2.3
   */
  public boolean isSentenceEnd() {
    return isSentEnd;
  }

  /**
   * @return true if the token is LibreOffice/OpenOffice field code.
   * @since 0.9.9
   */
  public boolean isFieldCode() {
    return "\u0001".equals(token) || "\u0002".equals(token);
  }

  /**
   * Add a SENT_END tag.
   */
  public void setSentEnd() {
    if (!isSentenceEnd()) {
      AnalyzedToken sentenceEnd = new AnalyzedToken(getToken(),
          SENTENCE_END_TAGNAME, getAnalyzedToken(0).getLemma());
      addReading(sentenceEnd, "");
    }
  }

  public int getStartPos() {
    return startPos;
  }

  /** @since 2.9 */
  public int getEndPos() {
    return startPos + token.length();
  }

  public void setStartPos(int position) {
    startPos = position;
  }

  /** @since 5.1 */
  public void setPosFix(int fix) {
    fixPos = fix;
  }

  /** @since 5.1 */
  public int getPosFix() {
    return fixPos;
  }

  public String getToken() {
    return token;
  }

  public void setWhitespaceBefore(String prevToken) {
    isWhitespaceBefore = !prevToken.isEmpty() && StringTools.isWhitespace(prevToken);
    for (AnalyzedToken aTok : anTokReadings) {
      aTok.setWhitespaceBefore(isWhitespaceBefore);
    }
    if (isWhitespaceBefore) {
      whitespaceBeforeChar = prevToken;
    }
  }

  public String getWhitespaceBefore() {
    return whitespaceBeforeChar;
  }
  
  public boolean isWhitespaceBefore() {
    return isWhitespaceBefore;
  }

  public void immunize(int sourceLine) {
    isImmunized = true;
    immunizationSourceLine = sourceLine;
  }

  public boolean isImmunized() {
    if (isImmunized && logger.isDebugEnabled()) {
      logger.debug("'" + getToken() + "' is immunized by antipattern in line " + immunizationSourceLine);
    }
    return isImmunized;
  }

  public int getImmunizationSourceLine() {
    return immunizationSourceLine;
  }

  /**
   * Make the token ignored by all spelling rules.
   * @since 2.5
   */
  public void ignoreSpelling() {
    isIgnoredBySpeller = true;
  }

  /**
   * Test if the token can be ignored by spelling rules.
   * @return true if the token should be ignored.
   * @since 2.5
   */
  public boolean isIgnoredBySpeller() {
    return isIgnoredBySpeller;
  }

  /**
   * Test if the token's POStag equals null.
   * @return true if the token does not have a POStag
   * @since 3.9
   */
  public boolean isPosTagUnknown() {
    return isPosTagUnknown;
  }

  /**
   * Sets the flag on AnalyzedTokens to make matching
   * on {@code UNKNOWN} POS tag correct in the Element class.
   */
  private void setNoRealPOStag() {
    boolean hasNoPOStag = !isLinebreak();
    for (AnalyzedToken an: anTokReadings) {
      String posTag = an.getPOSTag();
      if (PARAGRAPH_END_TAGNAME.equals(posTag) ||
          SENTENCE_END_TAGNAME.equals(posTag)) {
        continue;
      }
      if (posTag != null) {
        hasNoPOStag = false;
        break;
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
  private void setHistoricalAnnotations(String historicalAnnotations) {
    this.historicalAnnotations = historicalAnnotations;
  }
  
  private void addHistoricalAnnotations(String oldValue, String ruleApplied) {
    if (!ruleApplied.isEmpty()) {
      this.historicalAnnotations = this.getHistoricalAnnotations() + "\n" + ruleApplied + ": " + oldValue + " -> "
          + this;
    }
  }
  

  /**
   * @since 2.3
   */
  public void setChunkTags(List<ChunkTag> chunkTags) {
    this.chunkTags = Objects.requireNonNull(chunkTags);
  }

  /**
   * @since 2.3
   */
  public List<ChunkTag> getChunkTags() {
    return chunkTags;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(token);
    sb.append('[');
    for (AnalyzedToken element : anTokReadings) {
      sb.append(element);
      if (!element.isWhitespaceBefore()) {
        sb.append('*');
      }
      sb.append(',');
    }
    sb.delete(sb.length() - 1, sb.length());
    if (!chunkTags.isEmpty()) {
      sb.append(',');
      sb.append(StringUtils.join(chunkTags, "|"));
    }
    sb.append(']');
    if (isImmunized()) {
      sb.append("{!},");
    }
    return sb.toString();
  }

  /**
   * @return true if AnalyzedTokenReadings has some real POS tag (= not null or a special tag)
   * @since 2.3
   */
  public boolean isTagged() {
    for (AnalyzedToken element : anTokReadings) {
      if (!element.hasNoTag()) {
        return true;
      }
    }
    return false;
  }

  /**
   * Used to configure the internal variable for lemma equality.
   * @return true if all {@link AnalyzedToken} lemmas are the same.
   * @since 2.5
   */
  private boolean areLemmasSame() {
    String previousLemma = anTokReadings[0].getLemma();
    if (previousLemma == null) {
      for (AnalyzedToken element : anTokReadings) {
        if (element.getLemma() != null) {
          return false;
        }
      }
      return true;
    }
    for (AnalyzedToken element : anTokReadings) {
      if (!previousLemma.equals(element.getLemma())) {
        return false;
      }
    }
    return true;
  }

  /**
   * Used to optimize pattern matching.
   * @return true if all {@link AnalyzedToken} lemmas are the same.
   */
  public boolean hasSameLemmas() {
    return hasSameLemmas;
  }

  /**
   * @return true if AnalyzedTokenReadings is a punctuation mark, bracket, etc
   * @since 4.4
   */
  public boolean isNonWord() {
    return NON_WORD_REGEX.matcher(token).matches();
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(anTokReadings) +
           Objects.hash(isLinebreak, isParaEnd, isSentEnd, isSentStart, isWhitespace, isWhitespaceBefore, chunkTags, startPos, token);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) { return true; }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    AnalyzedTokenReadings other = (AnalyzedTokenReadings) obj;
    return new EqualsBuilder()
      .append(anTokReadings, other.anTokReadings)
      .append(isLinebreak, other.isLinebreak)
      .append(isParaEnd, other.isParaEnd)
      .append(isSentEnd, other.isSentEnd)
      .append(isSentStart, other.isSentStart)
      .append(isWhitespace, other.isWhitespace)
      .append(isWhitespaceBefore, other.isWhitespaceBefore)
      .append(isImmunized, other.isImmunized)
      .append(startPos, other.startPos)
      .append(chunkTags, other.chunkTags)
      .append(hasSameLemmas, other.hasSameLemmas)
      .append(isIgnoredBySpeller, other.isIgnoredBySpeller)
      .append(token, other.token)
      .append(hasTypographicApostrophe, other.hasTypographicApostrophe)
      .isEquals();
  }

  /**
   * @since 2.3
   */
  @NotNull
  @Override
  public Iterator<AnalyzedToken> iterator() {
    AtomicInteger i = new AtomicInteger(0);
    return new Iterator<AnalyzedToken>() {
      @Override
      public boolean hasNext() {
        return i.get() < getReadingsLength();
      }
      @Override
      public AnalyzedToken next() {
        try {
          return anTokReadings[i.getAndAdd(1)];
        } catch (ArrayIndexOutOfBoundsException e) {
          throw new NoSuchElementException("No such element: " + i + ", element count: " + anTokReadings.length);
        }
      }
      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  /**
   * @since 5.1
   */
  @Experimental
  public void setCleanToken(String cleanToken) {
    this.cleanToken = cleanToken;
  }

  /**
   * @since 5.1
   */
  @Experimental
  public String getCleanToken() {
    return cleanToken != null ? cleanToken : token;
  }
  
  /**
   * @since 5.2
   */
  public void setTypographicApostrophe() {
    hasTypographicApostrophe = true;
  }

  /**
   * @since 5.2
   */
  public boolean hasTypographicApostrophe() {
    return hasTypographicApostrophe;
  }
}
