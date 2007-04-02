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

  public String toString() {
    return token + "/" + posTag;
  }

}
