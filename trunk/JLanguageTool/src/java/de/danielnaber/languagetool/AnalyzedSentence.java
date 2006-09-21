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

import de.danielnaber.languagetool.tagging.de.AnalyzedGermanTokenReadings;

/**
 * A sentence that has been tokenized and analyzed.
 * 
 * @author Daniel Naber
 */
public class AnalyzedSentence {

  private AnalyzedTokenReadings[] tokens;
  
  /**
   * Sets {@link AnalyzedTokenReadings}. 
   * Whitespace is also a token.
   */
  public AnalyzedSentence(final AnalyzedTokenReadings[] tokens) {
    this.tokens = tokens;
  }

  /**
   * Returns the {@link AnalyzedTokenReadings} of the analyzed text. 
   * Whitespace is also a token.
   */
  public final AnalyzedTokenReadings[] getTokens() {
    return tokens;
  }

  /**
   * Returns the {@link AnalyzedTokenReadings} of the analyzed text, with whitespace tokens removed
   * but with the artificial <code>SENT_START</code> token included.
   */
  public final AnalyzedTokenReadings[] getTokensWithoutWhitespace() {
	    List<AnalyzedTokenReadings> l = new ArrayList<AnalyzedTokenReadings>();
	    for (AnalyzedTokenReadings token : tokens) {
	      if (!token.isWhitespace() || token.isSentStart()) {
            l.add(token);
	      }
	    }
	    return (AnalyzedTokenReadings[])l.toArray(new AnalyzedTokenReadings[0]);
	  }
  
  public final String toString() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < tokens.length; i++) {
      if (!"".equals(tokens[i].token.trim())) {
        sb.append(tokens[i].token);
        sb.append("[");
      }
      for (int j = 0; j < tokens[i].getReadingsLength(); j++) {
        if (JLanguageTool.SENTENCE_START_TAGNAME.equals(tokens[i].getAnalyzedToken(j).getPOSTag())) {
          sb.append("<S>");
        } else if (tokens[i].getAnalyzedToken(j) != null
            && tokens[i].getAnalyzedToken(j).getPOSTag() == null
            && !(tokens[i] instanceof AnalyzedGermanTokenReadings)) {
          // FIXME: don't depend on AnalyzedGermanTokenReadings here
          sb.append(tokens[i].getAnalyzedToken(j).getToken());
        } else {
          if (!"".equals(tokens[i].token.trim())) {
            sb.append(tokens[i].getAnalyzedToken(j));
            if (j < tokens[i].getReadingsLength() - 1) {
              sb.append(",");
            }
          }
        }
      }
      if (!"".equals(tokens[i].token.trim())) {
        sb.append("]");
      } else {
        sb.append(" ");
      }

    }
    return sb.toString();
  }

}
