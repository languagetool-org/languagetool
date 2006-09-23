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

import java.util.Arrays;
import java.util.List;

/**
 * An array of {@link AnalyzedToken}s used to store multiple POS tags and lemmas
 * for a given single token.
 * 
 * @author Marcin Milkowski
 */
public class AnalyzedTokenReadings {

  protected AnalyzedToken[] anTokReadings;
	protected int startPos;
	protected String token; 
	
	public AnalyzedTokenReadings(final AnalyzedToken[] r) {
		anTokReadings = r;
		token = anTokReadings[0].getToken();
		this.startPos = anTokReadings[0].getStartPos();
	}
	
	public AnalyzedTokenReadings(final AnalyzedToken at) {
		anTokReadings = new AnalyzedToken[1];
		anTokReadings[0] = at;
		token = anTokReadings[0].getToken();
		startPos = at.getStartPos();
	}
  
  public final List<AnalyzedToken> getReadings() {
    return (List<AnalyzedToken>) Arrays.asList(anTokReadings);
  }

  public final AnalyzedToken getAnalyzedToken(final int i) {
		return anTokReadings[i];
	}

  public final int getReadingsLength() {
    return anTokReadings.length;
  }
  
  public final boolean isWhitespace() {
    return token.trim().equals("");
  }
  
  
  public final boolean isSentStart() {
    //helper method added after profiling
   boolean isSE = false;
   if (anTokReadings[0].posTag!= null) {
     isSE = anTokReadings[0].posTag.equals(JLanguageTool.SENTENCE_START_TAGNAME);
   }
   return isSE;
  }
  
  public final int getStartPos() {
		return startPos;
	}
	
	public final String getToken(){
		return token;
	}
	
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < anTokReadings.length; i++) {
      sb.append(anTokReadings[i]);
    }
    return sb.toString();
  }
	
}
