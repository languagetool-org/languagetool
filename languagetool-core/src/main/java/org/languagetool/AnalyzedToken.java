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

import java.util.Objects;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * A word (or punctuation, or whitespace) and its analysis (part-of-speech tag, lemma)
 * 
 * @author Daniel Naber
 */
public class AnalyzedToken {

  private final String token;
  private final String posTag;
  private final String lemma;

  /**
   * used only for matching with Elements
   */
  private final String tokenInflected;

  private boolean isWhitespaceBefore;
  
  private boolean hasNoPOSTag;

  public AnalyzedToken(final String token, final String posTag, final String lemma) {
    this.token = Objects.requireNonNull(token, "token cannot be null");
    this.posTag = posTag;
    this.lemma = lemma;    
    if (lemma == null) {
      tokenInflected = token;
    } else {
      tokenInflected = lemma;
    }
    hasNoPOSTag = (posTag == null 
        || JLanguageTool.SENTENCE_END_TAGNAME.equals(posTag)
        || JLanguageTool.PARAGRAPH_END_TAGNAME.equals(posTag));
  }

  public final String getToken() {
    return token;
  }

  public final String getPOSTag() {
    return posTag;
  }

  /**
   * @return the token's lemma or {@code null}
   */
  public final String getLemma() {
    return lemma;
  }

  /**
   * Like {@link #getLemma()}, but returns the token if the lemma is {@code null}
   */
  public final String getTokenInflected() {
    return tokenInflected;
  }
  
  public final void setWhitespaceBefore(final boolean isWhite) {
    isWhitespaceBefore = isWhite;
  }

  public final boolean isWhitespaceBefore() {
    return isWhitespaceBefore;
  }

  
  /**
   * @param an AnalyzedToken to test
   * @return true if all of the non-null values (lemma, POS, token) of AnalyzedToken match this token
   * @since 1.5
   */
  public final boolean matches(final AnalyzedToken an) {
    if (this.equals(an)) {
      return true;
    }
    //empty tokens never match anything
    if ("".equals(an.getToken()) && an.getLemma() == null 
        && an.getPOSTag() == null) {
      return false;
    }
    boolean found = true;
    if (!"".equals(an.getToken())) { //token cannot be null
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
  public final boolean hasNoTag() {
    return hasNoPOSTag;
  }
  
  /** 
   * If other readings of the token have real POS tags,
   * you can set the flag here that they do, so that the
   * test in the Element class would be correct for all
   * cases. 
   * @since 1.5
   */
  public final void setNoPOSTag(final boolean noTag) {
    hasNoPOSTag = noTag;
  }
  
  @Override
  public String toString() {
    return tokenInflected + '/' + posTag;
  }

  @Override
  public final int hashCode() {
    return new HashCodeBuilder().append(isWhitespaceBefore).append(lemma).append(posTag).append(token).toHashCode();
  }

  @Override
  public final boolean equals(final Object obj) {
    if (obj == null) { return false; }
    if (obj == this) { return true; }
    if (obj.getClass() != getClass()) {
      return false;
    }
    final AnalyzedToken rhs = (AnalyzedToken) obj;
    return new EqualsBuilder()
            .append(token, rhs.token)
            .append(posTag, rhs.posTag)
            .append(lemma, rhs.lemma)
            .append(isWhitespaceBefore, rhs.isWhitespaceBefore)
            .isEquals();
  }

}
