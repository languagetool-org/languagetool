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
package de.danielnaber.languagetool.tagging.ro;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import morfologik.stemming.Dictionary;
import morfologik.stemming.DictionaryLookup;
import morfologik.stemming.IStemmer;
import morfologik.stemming.WordData;

import de.danielnaber.languagetool.AnalyzedToken;
import de.danielnaber.languagetool.AnalyzedTokenReadings;
import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.tagging.BaseTagger;

/**
 * Romanian Part-of-speech tagger 
 * 
 * @author Ionuț Păduraru
 */
public class RomanianTagger extends BaseTagger {

  private String RESOURCE_FILENAME = "/ro/romanian.dict";

  private IStemmer morfologik;
  private static final Locale roLocale = new Locale("ro");

  @Override
  public final String getFileName() {
    return JLanguageTool.getDataBroker().getResourceDir() + RESOURCE_FILENAME;
  }

  public RomanianTagger() {
    super();
    setLocale(roLocale);
  }

  public RomanianTagger(final String fileName) {
    super();
    RESOURCE_FILENAME = fileName;
    setLocale(roLocale);
  }

  @Override
  public final List<AnalyzedTokenReadings> tag(
      final List<String> sentenceTokens) throws IOException {
    List<WordData> taggerTokens;

    final List<AnalyzedTokenReadings> tokenReadings = new ArrayList<AnalyzedTokenReadings>();
    int pos = 0;
    // caching Lametyzator instance - lazy init
    if (morfologik == null) {      
      final URL url = JLanguageTool.getDataBroker().getFromResourceDirAsUrl(RESOURCE_FILENAME);
      morfologik = new DictionaryLookup(Dictionary.read(url));
    }

    for (final String word : sentenceTokens) {
      final List<AnalyzedToken> l = new ArrayList<AnalyzedToken>();
      taggerTokens = morfologik.lookup(word.toLowerCase(roLocale));
      if (taggerTokens != null) {
        for (WordData wd : taggerTokens) {
          final String[] tagsArr = wd.getStem().toString().split("\\+");
          for (final String currTag : tagsArr) {
            l.add(new AnalyzedToken(word, 
                wd.getTag().toString(), currTag));
          }
        }			
      }

      if (taggerTokens == null || taggerTokens.isEmpty()) {
        l.add(new AnalyzedToken(word, null, null));
      }			
      tokenReadings.add(new AnalyzedTokenReadings(l
          .toArray(new AnalyzedToken[l.size()]), pos));
      pos += word.length();
    }

    return tokenReadings;

  }

}
