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

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * A word (or punctuation, or whitespace) and its analysis (part-of-speech tag, lemma)
 * 
 * @author Daniel Naber
 */
public final class AnalyzedToken {

  private final String token;
  private final String posTag;
  private final String lemma;
  private final String lemmaOrToken;  // used only for matching with Elements

  private boolean isWhitespaceBefore;
  private boolean hasNoPOSTag;

  public AnalyzedToken(String token, String posTag, String lemma) {
    this.token = Objects.requireNonNull(token, "token cannot be null");
    this.posTag = posTag;
    this.lemma = lemma;    
    if (lemma == null) {
      lemmaOrToken = token;
    } else {
      lemmaOrToken = lemma;
    }
    hasNoPOSTag = (posTag == null 
        || JLanguageTool.SENTENCE_END_TAGNAME.equals(posTag)
        || JLanguageTool.PARAGRAPH_END_TAGNAME.equals(posTag));
  }

  public String getToken() {
    return token;
  }

  /**
   * @return the token's part-of-speech tag or {@code null}
   */
  @Nullable
  public String getPOSTag() {
    return posTag;
  }

  /**
   * @return the token's lemma or {@code null}
   */
  @Nullable
  public String getLemma() {
    return lemma;
  }

  public void setWhitespaceBefore(boolean whitespaceBefore) {
    isWhitespaceBefore = whitespaceBefore;
  }

  public boolean isWhitespaceBefore() {
    return isWhitespaceBefore;
  }
  
  /**
   * @param an AnalyzedToken to test
   * @return true if all of the non-null values (lemma, POS, token) of AnalyzedToken match this token
   * @since 1.5
   */
  public boolean matches(AnalyzedToken an) {
    if (this.equals(an)) {
      return true;
    }
    //empty tokens never match anything
    if (an.getToken().isEmpty() && an.getLemma() == null
        && an.getPOSTag() == null) {
      return false;
    }
    boolean found = true;
    if (!an.getToken().isEmpty()) { //token cannot be null
      found = an.getToken().equals(this.token);
    }
    if (an.getLemma() != null) {
      found &= an.getLemma().equals(this.lemma);
    }
    if (an.getPOSTag() != null) {
      found &= an.getPOSTag().equals(this.posTag);
    }
    return found;
  }
  
  /**
   * @return true if the AnalyzedToken has no real POS tag (= is not null or a special tag)
   * @since 1.5
   */
  public boolean hasNoTag() {
    return hasNoPOSTag;
  }
  
  /** 
   * If other readings of the token have real POS tags,
   * you can set the flag here that they do, so that the
   * test in the Element class would be correct for all
   * cases. 
   * @since 1.5
   */
  public void setNoPOSTag(boolean noTag) {
    hasNoPOSTag = noTag;
  }
  
  @Override
  public String toString() {
    return lemmaOrToken + '/' + posTag;
  }

  @Override
  public int hashCode() {
    return Objects.hash(isWhitespaceBefore, lemma, posTag, token);
  }

  @Override
  public boolean equals(Object o) {
    if (o == null) return false;
    if (o == this) return true;
    if (o.getClass() != getClass()) return false;
    AnalyzedToken other = (AnalyzedToken) o;
    return Objects.equals(token, other.token)
        && Objects.equals(posTag, other.posTag)
        && Objects.equals(lemma, other.lemma)
        && Objects.equals(isWhitespaceBefore, other.isWhitespaceBefore);
  }

}
