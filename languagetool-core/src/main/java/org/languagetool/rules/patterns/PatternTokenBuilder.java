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
package org.languagetool.rules.patterns;

import java.util.Objects;

/**
 * Helper to build {@link PatternToken}s.
 * @since 3.1
 */
public class PatternTokenBuilder {

  private String token;
  private String posTag;
  private boolean marker = true;
  private boolean matchInflectedForms = false;
  private boolean caseSensitive;
  private boolean regexp;
  private boolean negation;
  private boolean isWhiteSpaceSet = false;
  private boolean isWhiteSpaceBefore;
  private int minOccurrence = 1;
  private int maxOccurrence = 1;
  private int skip;
  private String tokenException;

  /**
   * Add a case-insensitive token. 
   */
  public PatternTokenBuilder token(String token) {
    this.token = Objects.requireNonNull(token);
    return this;
  }

  /**
   * Add a case-sensitive token. 
   * @since 3.3 
   */
  public PatternTokenBuilder csToken(String token) {
    this.token = Objects.requireNonNull(token);
    caseSensitive = true;
    return this;
  }

  public PatternTokenBuilder tokenRegex(String token) {
    this.token = Objects.requireNonNull(token);
    regexp = true;
    return this;
  }

  public PatternTokenBuilder csTokenRegex(String token) {
    this.token = Objects.requireNonNull(token);
    regexp = true;
    caseSensitive = true;
    return this;
  }

  public PatternTokenBuilder pos(String posTag) {
    return pos(posTag, false);
  }

  public PatternTokenBuilder posRegex(String posTag) {
    return pos(posTag, true);
  }

  /** @since 4.9 */
  public PatternTokenBuilder min(int val) {
    if (val < 0) {
      throw new IllegalArgumentException("minOccurrence must be >= 0: " + minOccurrence);
    }
    minOccurrence = val;
    return this;
  }

  /** @since 4.9 */
  public PatternTokenBuilder max(int val) {
    maxOccurrence = val;
    return this;
  }

  /**
   * Corresponds to {@code <marker>...</marker>} in XML. Note that there
   * can be more tokens with a mark, but then must all be adjacent.
   * @since 4.6
   */
  public PatternTokenBuilder mark(boolean isMarked) {
    this.marker = isMarked;
    return this;
  }

  public PatternTokenBuilder posRegexWithStringException(String posTag, String tokenExceptionRegex) {
    return pos(posTag, true, tokenExceptionRegex);
  }

  private PatternTokenBuilder pos(String posTag, boolean regexp) {
    this.posTag = Objects.requireNonNull(posTag);
    this.regexp = regexp;
    return this;
  }

  private PatternTokenBuilder pos(String posTag, boolean regexp, String tokenExceptionRegex) {
    this.posTag = Objects.requireNonNull(posTag);
    this.regexp = regexp;
    this.tokenException = tokenExceptionRegex;
    return this;
  }

  /** @since 3.3 */
  public PatternTokenBuilder negate() {
    this.negation = true;
    return this;
  }

  /** @since 4.0 */
  public PatternTokenBuilder setSkip(int skip) {
    this.skip = skip;
    return this;
  }

  /** @since 4.4 */
  public PatternTokenBuilder setIsWhiteSpaceBefore(boolean whiteSpaceBefore) {
    this.isWhiteSpaceBefore = whiteSpaceBefore;
    this.isWhiteSpaceSet = true;
    return this;
  }

  /**
   * Also match inflected forms of the given word - note this will only work when the
   * given token actually is a baseform.
   * @since 3.8 
   */
  public PatternTokenBuilder matchInflectedForms() {
    matchInflectedForms = true;
    return this;
  }
  
  public PatternToken build() {
    PatternToken patternToken;
    if (posTag != null) {
      patternToken = new PatternToken(null, false, false, false);
      patternToken.setPosToken(new PatternToken.PosToken(posTag, regexp, false));
    } else {
      patternToken = new PatternToken(token, caseSensitive, regexp, matchInflectedForms);
    }
    if (isWhiteSpaceSet) {
      patternToken.setWhitespaceBefore(isWhiteSpaceBefore);
    }
    if (maxOccurrence < minOccurrence) {
      throw new IllegalArgumentException("minOccurrence must <= maxOccurrence: minOccurrence " + minOccurrence + ", maxOccurrence " + maxOccurrence);
    }
    if (tokenException != null) {
      patternToken.setStringPosException(tokenException, true, false, false, false, false, null, false, false, false);
    }
    patternToken.setMinOccurrence(minOccurrence);
    patternToken.setMaxOccurrence(maxOccurrence);
    patternToken.setNegation(negation);
    patternToken.setSkipNext(skip);
    patternToken.setInsideMarker(marker);
    return patternToken;
  }
}
