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
import de.danielnaber.languagetool.tools.StringTools;

/**
 * Base tagger using Lametyzator.
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
  
  public List<AnalyzedTokenReadings> tag(final List<String> sentenceTokens)
      throws IOException {
    String[] taggerTokens;
    List<AnalyzedTokenReadings> tokenReadings = new ArrayList<AnalyzedTokenReadings>();
    int pos = 0;
    // caching Lametyzator instance - lazy init
    if (morfologik == null) {
      setFileName();
      morfologik = new Lametyzator();
    }

    for (String word : sentenceTokens) {
      final List<AnalyzedToken> l = new ArrayList<AnalyzedToken>();
      String[] lowerTaggerTokens = null;
      taggerTokens = morfologik.stemAndForm(word);
      if (!word.equals(word.toLowerCase(conversionLocale))) {
        lowerTaggerTokens = morfologik.stemAndForm(word
            .toLowerCase(conversionLocale));
      }

      //normal case
      addTokens(word, taggerTokens, l, pos);
      
      //lowercase
      addTokens(word, lowerTaggerTokens, l, pos);

      //uppercase
      if (lowerTaggerTokens == null && taggerTokens == null) {
        if (word.equals(word.toLowerCase(conversionLocale))) {
          String[] upperTaggerTokens = null;
          upperTaggerTokens = morfologik.stemAndForm(StringTools
              .uppercaseFirstChar(word));
          if (upperTaggerTokens != null) {
            addTokens(word, upperTaggerTokens, l, pos);
          } else {
            l.add(new AnalyzedToken(word, null, null));
          }
        } else {
          l.add(new AnalyzedToken(word, null, null));
        }
      }          
      tokenReadings.add(new AnalyzedTokenReadings(l.toArray(new AnalyzedToken[l
          .size()]), pos));
      pos += word.length();
    }

    return tokenReadings;

  }

  private void addTokens(final String word, final String[] taggedTokens,
      final List<AnalyzedToken> l, final int pos) {
    if (taggedTokens != null) {
      int i = 0;
      while (i < taggedTokens.length) {
        // Lametyzator returns data as String[]
        // first lemma, then annotations
        l.add(new AnalyzedToken(word, taggedTokens[i + 1], taggedTokens[i]));
        i = i + 2;
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * de.danielnaber.languagetool.tagging.Tagger#createNullToken(java.lang.String
   * , int)
   */  
  public final AnalyzedTokenReadings createNullToken(final String token,
      final int startPos) {
    return new AnalyzedTokenReadings(new AnalyzedToken(token, null, null), startPos);
  }

  public AnalyzedToken createToken(String token, String posTag) {
    return new AnalyzedToken(token, posTag, null);
  }

}
