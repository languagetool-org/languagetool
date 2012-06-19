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
package org.languagetool.tagging.ro;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import morfologik.stemming.Dictionary;
import morfologik.stemming.DictionaryLookup;
import morfologik.stemming.IStemmer;
import morfologik.stemming.WordData;

import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.tagging.BaseTagger;
import org.languagetool.tagging.ManualTagger;

/**
 * Romanian Part-of-speech tagger 
 * 
 * @author Ionuț Păduraru
 */
public class RomanianTagger extends BaseTagger {

  private String RESOURCE_FILENAME = "/ro/romanian.dict";
  private String USER_DICT_FILENAME = "/ro/added.txt";

  private IStemmer morfologik;
  private ManualTagger manualTagger;
  private static final Locale roLocale = new Locale("ro");

  @Override
  public final String getFileName() {
    return RESOURCE_FILENAME;
  }

  public RomanianTagger() {
    super();
    setLocale(roLocale);
  }

  public RomanianTagger(final String dictFileName, final String userDictFileName) {
    super();
    RESOURCE_FILENAME = dictFileName;
    USER_DICT_FILENAME = userDictFileName;
    setLocale(roLocale);
  }

  @Override
  public final List<AnalyzedTokenReadings> tag(
      final List<String> sentenceTokens) throws IOException {
    final List<AnalyzedTokenReadings> tokenReadings = new ArrayList<AnalyzedTokenReadings>();
    int pos = 0;
    // caching Lametyzator instance - lazy init
    if (morfologik == null) {      
      final URL url = JLanguageTool.getDataBroker().getFromResourceDirAsUrl(RESOURCE_FILENAME);
      morfologik = new DictionaryLookup(Dictionary.read(url));
    }
    if (manualTagger == null && USER_DICT_FILENAME != null) {
        manualTagger = new ManualTagger(JLanguageTool.getDataBroker().getFromResourceDirAsStream(USER_DICT_FILENAME));
    }


    for (final String word : sentenceTokens) {
      final List<AnalyzedToken> l = new ArrayList<AnalyzedToken>();
      final String lowerCaseWord = word.toLowerCase(roLocale);
      final List<WordData> taggerTokens = morfologik.lookup(lowerCaseWord);
      if (taggerTokens != null) {
        for (WordData wd : taggerTokens) {
          final String[] tagsArr = wd.getStem().toString().split("\\+");
          for (final String currTag : tagsArr) {
            l.add(new AnalyzedToken(word, 
                wd.getTag().toString(), currTag));
          }
        }			
      }
      if (manualTagger != null) { // add user tags, if any
    	  final String[] manualTags = manualTagger.lookup(lowerCaseWord);
    	  if (manualTags != null) {
    		  for (int i = 0; i < manualTags.length/2; i=i+2) {
    			  l.add(new AnalyzedToken(word, manualTags[i+1], manualTags[i]));
			}
    	  }
      }

      if (l.isEmpty()) {
        l.add(new AnalyzedToken(word, null, null));
      }			
      tokenReadings.add(new AnalyzedTokenReadings(l
          .toArray(new AnalyzedToken[l.size()]), pos));
      pos += word.length();
    }

    return tokenReadings;

  }

}
