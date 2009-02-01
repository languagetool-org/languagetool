/* LanguageTool, a natural language style checker 
 * Copyright (C) 2006 Daniel Naber (http://www.danielnaber.de)
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
package de.danielnaber.languagetool.tagging;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import morfologik.stemmers.Lametyzator;
import de.danielnaber.languagetool.AnalyzedToken;
import de.danielnaber.languagetool.AnalyzedTokenReadings;

/** Base tagger using Lametyzator.
 * 
 * @author Marcin Milkowski
 */
public abstract class BaseTagger implements Tagger {

  private Lametyzator morfologik;
  private Locale conversionLocale = Locale.getDefault();  

 /**
  * Set the filename in a JAR, eg. <tt>/resource/fr/french.dict</tt>.
  **/   
  public abstract void setFileName();

  public void setLocale(Locale loc) {
    conversionLocale = loc;
  }

  @Override
  public List<AnalyzedTokenReadings> tag(final List<String> sentenceTokens) throws IOException {
    String[] taggerTokens;
    List<AnalyzedTokenReadings> tokenReadings = new ArrayList<AnalyzedTokenReadings>();
    int pos = 0;
    //caching Lametyzator instance - lazy init
    if (morfologik == null) {
      setFileName();
      morfologik = new Lametyzator();
    }

    for (String word : sentenceTokens) {
      final List<AnalyzedToken> l = new ArrayList<AnalyzedToken>();
      String[] lowerTaggerTokens = null;
      taggerTokens = morfologik.stemAndForm(word);
      if (!word.equals(word.toLowerCase(conversionLocale))) {
        lowerTaggerTokens = morfologik.stemAndForm(word.toLowerCase(conversionLocale));
      }

      if (taggerTokens != null) {
        int i = 0;
        while (i < taggerTokens.length) {
          //Lametyzator returns data as String[]
          //first lemma, then annotations
          l.add(new AnalyzedToken(word, taggerTokens[i + 1], taggerTokens[i], pos));
          i = i + 2;
        }
      }
      if (lowerTaggerTokens != null) {
        int i = 0;
        while (i < lowerTaggerTokens.length) {
          //Lametyzator returns data as String[]
          //first lemma, then annotations
          l.add(new AnalyzedToken(word, lowerTaggerTokens[i + 1], lowerTaggerTokens[i], pos));
          i = i + 2;
        }
      }

      if (lowerTaggerTokens == null && taggerTokens == null) {
        l.add(new AnalyzedToken(word, null, pos));
      }
      pos += word.length();
      tokenReadings
          .add(new AnalyzedTokenReadings(l.toArray(new AnalyzedToken[l.size()])));
    }

    return tokenReadings;

  }

  
  /* (non-Javadoc)
   * @see de.danielnaber.languagetool.tagging.Tagger#createNullToken(java.lang.String, int)
   */
  @Override
  public final AnalyzedTokenReadings createNullToken(final String token, final int startPos) {
    return new AnalyzedTokenReadings(new AnalyzedToken(token, null, startPos));
  }
  
  @Override
  public AnalyzedToken createToken(String token, String posTag, int startPos) {
    return new AnalyzedToken(token, posTag, startPos);
  }

}
