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
package de.danielnaber.languagetool.tagging.pl;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import morfologik.stemming.Dictionary;
import morfologik.stemming.DictionaryLookup;
import morfologik.stemming.IStemmer;
import de.danielnaber.languagetool.AnalyzedToken;
import de.danielnaber.languagetool.AnalyzedTokenReadings;
import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.tagging.BaseTagger;
import de.danielnaber.languagetool.tools.StringTools;

/**
 * Polish POS tagger based on FSA morphological dictionaries.
 * 
 * @author Marcin Milkowski
 */

public class PolishTagger extends BaseTagger {

  private static final String RESOURCE_FILENAME = "/pl/polish.dict";
  private IStemmer morfologik;
  private final Locale plLocale = new Locale("pl");

  @Override
  public final String getFileName() {
    return JLanguageTool.getDataBroker().getResourceDir() + RESOURCE_FILENAME;
  }

  @Override
  public final List<AnalyzedTokenReadings> tag(final List<String> sentenceTokens)
  throws IOException {
    List<AnalyzedToken> taggerTokens;
    List<AnalyzedToken> lowerTaggerTokens;
    List<AnalyzedToken> upperTaggerTokens;    
    final List<AnalyzedTokenReadings> tokenReadings = new ArrayList<AnalyzedTokenReadings>();
    int pos = 0;
    // caching Lametyzator instance - lazy init
    if (morfologik == null) {      
      final URL url = JLanguageTool.getDataBroker().getFromResourceDirAsUrl(RESOURCE_FILENAME);
      morfologik = new DictionaryLookup(Dictionary.read(url));
    }

    for (String word : sentenceTokens) {
      final List<AnalyzedToken> l = new ArrayList<AnalyzedToken>();
      final String lowerWord = word.toLowerCase(plLocale);
      taggerTokens = asAnalyzedTokenList(word, morfologik.lookup(word));
      lowerTaggerTokens = asAnalyzedTokenList(word, morfologik.lookup(lowerWord));       
      final boolean isLowercase = word.equals(lowerWord);

      //normal case
      addTokens(taggerTokens, l);

      if (!isLowercase) {             
        //lowercase        
        addTokens(lowerTaggerTokens, l);
      }

      //uppercase
      if (lowerTaggerTokens.isEmpty() && taggerTokens.isEmpty()) {
        if (isLowercase) {          
          upperTaggerTokens = asAnalyzedTokenList(word, morfologik.lookup(StringTools
              .uppercaseFirstChar(word)));
          if (!upperTaggerTokens.isEmpty()) {
            addTokens(upperTaggerTokens, l);
          } else {
            l.add(new AnalyzedToken(word, null, null));
          }
        } else {
          l.add(new AnalyzedToken(word, null, null));
        }
      }          
      tokenReadings.add(new AnalyzedTokenReadings(l, pos));
      pos += word.length();
    }

    return tokenReadings;
  }

  private void addTokens(final List<AnalyzedToken> taggedTokens,
      final List<AnalyzedToken> l) {
    if (taggedTokens != null) {
      for (AnalyzedToken at : taggedTokens) {
        final String[] tagsArr = StringTools.asString(at.getPOSTag()).split("\\+");
        for (final String currTag : tagsArr) {
          l.add(new AnalyzedToken(at.getToken(), currTag,
              at.getLemma()));
        }
      }
    }
  }

    
}