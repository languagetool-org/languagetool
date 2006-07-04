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

  protected String token;
  protected String posTag;
  protected int startPos;
  protected String lemma;
  
  public AnalyzedToken(String token, String posTag, int startPos) {
    this.token = token;
    this.posTag = posTag;
    this.startPos = startPos;
  }
  
  public AnalyzedToken(String token, String posTag, String lemma) {
	    this.token = token;
	    this.posTag = posTag;
	    this.lemma= lemma;
	  }
	  
  
  public boolean isWhitespace() {
    return token.trim().equals("");
  }
  
  public String getToken() {
    return token;
  }

  public String getPOSTag() {
    return posTag;
  }

  public String getLemma() {
	  return lemma;
  }
  
  public int getStartPos() {
    return startPos;
  }

  public String toString() {
    return token + "/" + posTag;
  }

}
