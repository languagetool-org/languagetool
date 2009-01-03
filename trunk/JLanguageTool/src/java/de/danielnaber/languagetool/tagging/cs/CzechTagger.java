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
package de.danielnaber.languagetool.tagging.cs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import morfologik.stemmers.Lametyzator;
import de.danielnaber.languagetool.AnalyzedToken;
import de.danielnaber.languagetool.AnalyzedTokenReadings;
import de.danielnaber.languagetool.tagging.BaseTagger;

/**
 * Czech POS tagger based on FSA morphological dictionaries.
 * 
 * @author Jozef Licko
 */
public class CzechTagger extends BaseTagger {

  private static final String RESOURCE_FILENAME = "/resource/cs/czech.dict";

  private Lametyzator morfologik;
  private Locale csLocale = new Locale("cs");

  
  @Override
  public void setFileName() {
    System.setProperty(Lametyzator.PROPERTY_NAME_LAMETYZATOR_DICTIONARY, 
        RESOURCE_FILENAME);    
  }
  
  @Override
  public final List<AnalyzedTokenReadings> tag(final List<String> sentenceTokens)
      throws IOException {
    String[] taggerTokens;

    final List<AnalyzedTokenReadings> tokenReadings = new ArrayList<AnalyzedTokenReadings>();
    int pos = 0;
    //caching Lametyzator instance - lazy init
    if (morfologik == null) {
      setFileName();
      morfologik = new Lametyzator();
    }

    for (final String word : sentenceTokens) {
      final List<AnalyzedToken> l = new ArrayList<AnalyzedToken>();
      String[] lowerTaggerTokens = null;
      taggerTokens = morfologik.stemAndForm(word);
      if (!word.equals(word.toLowerCase(csLocale))) {
        lowerTaggerTokens = morfologik.stemAndForm(word.toLowerCase(csLocale));
      }

      if (taggerTokens != null) {
        // Lametyzator returns data as String[]
        // first lemma, then annotations
        /*
         if(taggerTokens.length > 2) {
           for (String currStr : taggerTokens)
           System.out.print(currStr + " ");
         System.out.println();
         }
         */        
        int i = 0;
        while (i < taggerTokens.length) {
          // Czech POS tags:
          // If there are multiple tags, they behave as one, i.e. they
          // are connected
          // on one line with '+' character
          final String lemma = taggerTokens[i];
          final String[] tagsArr = taggerTokens[i + 1].split("\\+");

          for (final String currTag : tagsArr) {
            l.add(new AnalyzedToken(word, currTag, lemma, pos));
          }

          i += 2;
        }
      }

      if (lowerTaggerTokens != null) {
        
        int i = 0;
        while (i < lowerTaggerTokens.length) {
          // Czech POS tags again
          final String lemma = lowerTaggerTokens[i];
          final String[] tagsArr = lowerTaggerTokens[i + 1].split("\\+");

          for (final String currTag : tagsArr) {
            l.add(new AnalyzedToken(word, currTag, lemma, pos));
          }

          i += 2;
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

}
