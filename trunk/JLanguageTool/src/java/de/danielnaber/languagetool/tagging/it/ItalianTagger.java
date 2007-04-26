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
package de.danielnaber.languagetool.tagging.it;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.dawidweiss.stemmers.Lametyzator;

import de.danielnaber.languagetool.AnalyzedToken;
import de.danielnaber.languagetool.AnalyzedTokenReadings;
import de.danielnaber.languagetool.tagging.Tagger;

/**
 * Italian tagger
 * 
 * Uses morph-it! lexicon compiled by Marco Baroni and Eros Zanchetta
 * 
 * see resource/it/readme-morph-it.txt for tagset
 * 
 * @author Marcin Milkowski
 */
public class ItalianTagger implements Tagger {

  private static final String RESOURCE_FILENAME = "/resource/it/italian.dict";

  private Lametyzator morfologik = null;

  public List<AnalyzedTokenReadings> tag(final List<String> sentenceTokens) throws IOException {
    String[] taggerTokens;
    List<AnalyzedTokenReadings> tokenReadings = new ArrayList<AnalyzedTokenReadings>();
    int pos = 0;
    //caching Lametyzator instance - lazy init
    if (morfologik == null) {
      morfologik = new Lametyzator(this.getClass().getResourceAsStream(RESOURCE_FILENAME),
          "iso8859-15", '+');
    }

    for (String word : sentenceTokens) {
      List<AnalyzedToken> l = new ArrayList<AnalyzedToken>();
      String[] lowerTaggerTokens = null;
      taggerTokens = morfologik.stemAndForm(word);
      if (!word.equals(word.toLowerCase())) {
        lowerTaggerTokens = morfologik.stemAndForm(word.toLowerCase());
      }

      if (taggerTokens != null) {
        int i = 0;
        while (i < taggerTokens.length) {
          //Lametyzator returns data as String[]
          //first lemma, then annotations
          l.add(new AnalyzedToken(word, taggerTokens[i + 1], taggerTokens[i]));
          i = i + 2;
        }
      }
      if (lowerTaggerTokens != null) {
        int i = 0;
        while (i < lowerTaggerTokens.length) {
          //Lametyzator returns data as String[]
          //first lemma, then annotations
          l.add(new AnalyzedToken(word, lowerTaggerTokens[i + 1], lowerTaggerTokens[i]));
          i = i + 2;
        }
      }

      if (lowerTaggerTokens == null && taggerTokens == null) {
        l.add(new AnalyzedToken(word, null, pos));
      }
      pos += word.length();
      tokenReadings
          .add(new AnalyzedTokenReadings((AnalyzedToken[]) l.toArray(new AnalyzedToken[l.size()])));
    }

    return tokenReadings;

  }

  /** 
   * @see de.danielnaber.languagetool.tagging.Tagger#createNullToken(java.lang.String, int)
   * @return AnalyzedTokenReadings
   */
  public final Object createNullToken(final String token, final int startPos) {
    return new AnalyzedTokenReadings(new AnalyzedToken(token, null, startPos));
  }

}
