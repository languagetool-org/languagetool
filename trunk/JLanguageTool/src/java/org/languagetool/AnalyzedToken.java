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

/**
 * A word (or punctuation, or whitespace) and its part-of-speech tag.
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
    if (token == null) {
      throw new NullPointerException("Token cannot be null!");
    }
    this.token = token;
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

  public final String getLemma() {
    return lemma;
  }
  
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
   * @since 1.5
   * @param an AnalyzedToken to test
   * @return true if all of the non-null values (lemma, POS, token)
   * of AnalyzedToken match this token.
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
      found &= this.token.equals(an.getToken());      
    }
    if (an.getLemma() != null) {
      found &= this.lemma.equals(an.getLemma());             
    }
    if (an.getPOSTag() != null) {
      found &= this.posTag.equals(an.getPOSTag());      
    }
    return found;
  }
  
  /**
   * @since 1.5
   * @return true if the AnalyzedToken has no real POS tag 
   * (= is not null or a special tag)
   */
  public final boolean hasNoTag() {
    return hasNoPOSTag;
  }
  
  /** 
   * @since 1.5
   * If other readings of the token have real POS tags,
   * you can set the flag here that they do, so that the
   * test in the Element class would be correct for all
   * cases. 
   * 
   */
  public final void setNoPOSTag(final boolean noTag) {
    hasNoPOSTag = noTag;
  }
  
  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append(tokenInflected);    
    sb.append('/');
    sb.append(posTag);
    return sb.toString();
  }

  @Override
  public final int hashCode() {
    // TODO: use Apache Commons Lang HashCodeBuilder
    final int prime = 31;
    int result = 1;
    result = prime * result + (isWhitespaceBefore ? 1231 : 1237);
    result = prime * result + ((lemma == null) ? 0 : lemma.hashCode());
    result = prime * result + ((posTag == null) ? 0 : posTag.hashCode());    
    result = prime * result + ((token == null) ? 0 : token.hashCode());
    return result;
  }

  @Override
  public final boolean equals(final Object obj) {
    // TODO: use Apache Commons Lang EqualsBuilder
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final AnalyzedToken other = (AnalyzedToken) obj;
    if (isWhitespaceBefore != other.isWhitespaceBefore) {
      return false;
    }
    if (lemma == null) {
      if (other.lemma != null) {
        return false;
      }
    } else if (!lemma.equals(other.lemma)) {
      return false;
    }
    if (posTag == null) {
      if (other.posTag != null) {
        return false;
      }
    } else if (!posTag.equals(other.posTag)) {
      return false;
    }    
    if (token == null) {
      if (other.token != null) {
        return false;
      }
    } else if (!token.equals(other.token)) {
      return false;
    }
    return true;
  }

}
