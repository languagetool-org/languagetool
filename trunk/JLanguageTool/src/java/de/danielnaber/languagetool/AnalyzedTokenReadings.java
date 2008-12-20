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
import java.util.Arrays;
import java.util.List;

import de.danielnaber.languagetool.tools.StringTools;

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

  private boolean isWhitespace;
  private boolean isLinebreak;
  private boolean isSentEnd;
  private boolean isParaEnd;

  private boolean isWhitespaceBefore = false;

  public AnalyzedTokenReadings(final AnalyzedToken[] r) {
    anTokReadings = r.clone();		
    this.startPos = anTokReadings[0].getStartPos();
    init();
  }

  public AnalyzedTokenReadings(final AnalyzedToken at) {
    anTokReadings = new AnalyzedToken[1];
    anTokReadings[0] = at;		
    startPos = at.getStartPos();
    init();
  }

  private void init() {
    token = anTokReadings[0].getToken();
    isWhitespace = StringTools.isWhitespace(token);
    isLinebreak = token.equals("\n") || token.equals("\r\n") || token.equals("\r") || token.equals("\n\r");
    isParaEnd = false;
    for (final AnalyzedToken reading : anTokReadings) {
      if (reading.posTag != null) {
        isParaEnd |= reading.posTag.equals(JLanguageTool.PARAGRAPH_END_TAGNAME);
        if (isParaEnd) {
          break;
        }
      }
    }
    isSentEnd = false;
    for (final AnalyzedToken reading : anTokReadings) {
      if (reading.posTag != null) {
        isSentEnd |= reading.posTag.equals(JLanguageTool.SENTENCE_END_TAGNAME);
        if (isSentEnd) {
          break;
        }
      }
    }
  }


  public final List<AnalyzedToken> getReadings() {
    return Arrays.asList(anTokReadings);
  }

  /**
   * Checks if the token has a particular POS tag.
   * @param POS POS Tag to check
   * @return True if it does.
   */
  public final boolean hasPosTag(final String POS) {
    boolean found = false;
    for (final AnalyzedToken reading: anTokReadings) {
      if (reading.posTag != null) {
        found |= POS.equals(reading.posTag);
        if (found) {
          break;
        }
      }
    }
    return found;
  }

  public final AnalyzedToken getAnalyzedToken(final int i) {
    return anTokReadings[i];
  }

  public final void addReading(final AnalyzedToken tok) {
    final ArrayList <AnalyzedToken> l = new ArrayList <AnalyzedToken>(); 

    for (int i = 0; i < anTokReadings.length - 1; i++) {
      l.add(anTokReadings[i]);     
    }

    if (anTokReadings[anTokReadings.length - 1].getPOSTag() != null) {
      l.add(anTokReadings[anTokReadings.length - 1]);
    }

    l.add(tok);

    anTokReadings = l.toArray(new AnalyzedToken[l.size()]);
  }

  public final void removeReading(final AnalyzedToken tok) {
    final ArrayList <AnalyzedToken> l = new ArrayList <AnalyzedToken>();
    AnalyzedToken tmpTok = new AnalyzedToken(tok.getToken(), tok.getPOSTag(), tok.getLemma(), startPos);
    tmpTok.setWhitespaceBefore(isWhitespaceBefore);
    for (int i = 0; i < anTokReadings.length; i++) {
      if (!anTokReadings[i].equals(tmpTok))
        l.add(anTokReadings[i]);     
    }
    anTokReadings = l.toArray(new AnalyzedToken[l.size()]);
  }

  public final int getReadingsLength() {
    return anTokReadings.length;
  }

  public final boolean isWhitespace() {
    return isWhitespace;
  }

  /**
   * Returns true if the token equals \n, \r\n \n\r or \r\n.
   * @return
   */
  public boolean isLinebreak() {
    return isLinebreak;
  }    

  public final boolean isSentStart() {
    //helper method added after profiling
    boolean isSE = false;
    if (anTokReadings[0].posTag != null) {
      isSE = anTokReadings[0].posTag.equals(JLanguageTool.SENTENCE_START_TAGNAME);
    }
    return isSE;
  }

  /**
   * @return true when the token is a last token in a paragraph. 
   */   
  public final boolean isParaEnd() {
    return isParaEnd;
  }

  /**
   * @return true when the token is a last token in a sentence. 
   */   
  public final boolean isSentEnd() {   
    return isSentEnd;
  }


  public final int getStartPos() {
    return startPos;
  }

  public final String getToken(){
    return token;
  }

  public void setWhitespaceBefore(final boolean isWhite) {
    isWhitespaceBefore = isWhite;
    for (AnalyzedToken aTok : anTokReadings) {
      aTok.setWhitespaceBefore(isWhite);
    }
  }

  public boolean isWhitespaceBefore(){
    return isWhitespaceBefore;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    for (final AnalyzedToken element : anTokReadings) {
      sb.append(element);
    }
    return sb.toString();
  }

}
