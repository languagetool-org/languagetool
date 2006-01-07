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
import java.util.List;

/**
 * A sentence that has been tokenized and analyzed.
 * 
 * @author Daniel Naber
 */
public class AnalyzedSentence {

  private AnalyzedToken[] tokens;
  
  public AnalyzedSentence(AnalyzedToken[] tokens) {
    this.tokens = tokens;
  }

  /**
   * Returns the {@link AnalyzedToken}s of the analyzed text. Whitespace is also a token.
   */
  public AnalyzedToken[] getTokens() {
    return tokens;
  }

  /**
   * Returns the {@link AnalyzedToken}s of the analyzed text, with whitespace tokens removed
   * but with the artificial <code>SENT_START</code> token included.
   */
  public AnalyzedToken[] getTokensWithoutWhitespace() {
    List l = new ArrayList();
    for (int i = 0; i < tokens.length; i++) {
      AnalyzedToken token = tokens[i];
      if (!token.isWhitespace() || (token.getPOSTag() != null &&
          token.getPOSTag().equals(JLanguageTool.SENTENCE_START_TAGNAME))) {
        l.add(token);
      }
    }
    return (AnalyzedToken[])l.toArray(new AnalyzedToken[0]);
  }
  
  public String toString() {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < tokens.length; i++) {
      if (JLanguageTool.SENTENCE_START_TAGNAME.equals(tokens[i].getPOSTag()))
        sb.append("<S>");
      else if (tokens[i] != null && tokens[i].getPOSTag() == null)
        sb.append(tokens[i].getToken());
      else
        sb.append(tokens[i]);
    }
    return sb.toString();
  }

}
