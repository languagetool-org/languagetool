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

  private String token;
  protected String posTag;
  private int startPos;
  private String lemma;

  private boolean isWhitespaceBefore = false;
  
  public AnalyzedToken(final String token, final String posTag, final int startPos) {
    this.token = token;
    this.posTag = posTag;
    this.startPos = startPos;
  }
  
  public AnalyzedToken(final String token, final String posTag, final String lemma) {
    this.token = token;
    this.posTag = posTag;
    this.lemma = lemma;
  }

  public AnalyzedToken(final String token, final String posTag, final String lemma, int startPos) {
    this.token = token;
    this.posTag = posTag;
    this.lemma = lemma;
    this.startPos = startPos;
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
  
  public final int getStartPos() {
    return startPos;
  }
  
  public void setWhitespaceBefore(final boolean isWhite) {
    isWhitespaceBefore = isWhite;
  }
  
  public boolean isWhitespaceBefore(){
    return isWhitespaceBefore;
  }
  

  public String toString() {
    StringBuilder sb = new StringBuilder();
    if (lemma != null) {
      sb.append(lemma);
    } else {
      sb.append(token);
    }
    sb.append("/");
    sb.append(posTag);
    return sb.toString();
  }
  
  public boolean equals(java.lang.Object object) {
    if (!(object instanceof AnalyzedToken)) {
     return false; 
    } else {
      AnalyzedToken otherToken = (AnalyzedToken) object;
      return (otherToken.getToken().equals(token)
          && otherToken.getPOSTag().equals(posTag)
          && otherToken.getLemma().equals(lemma)
          && otherToken.getStartPos() == startPos
          && otherToken.isWhitespaceBefore() == isWhitespaceBefore);
    }
  }

}
