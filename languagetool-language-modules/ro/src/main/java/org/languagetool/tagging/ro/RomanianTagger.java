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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import morfologik.stemming.DictionaryLookup;
import morfologik.stemming.IStemmer;
import morfologik.stemming.WordData;

import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.tagging.BaseTagger;
import org.languagetool.tagging.ManualTagger;
import org.languagetool.tagging.TaggedWord;

/**
 * Romanian Part-of-speech tagger 
 * 
 * @author Ionuț Păduraru
 */
public class RomanianTagger extends BaseTagger {

  private static final String DEFAULT_BINARY_DICT = "/ro/romanian.dict";
  private static final String DEFAULT_PLAINTEXT_DICT = "/ro/added.txt";
  private static final Locale RO_LOCALE = new Locale("ro");

  private final String binaryDictPath;
  private final String plaintextDictPath;

  private ManualTagger manualTagger;

  public RomanianTagger() {
    this(DEFAULT_BINARY_DICT, DEFAULT_PLAINTEXT_DICT);
  }

  public RomanianTagger(final String dictFileName, final String userDictFileName) {
    binaryDictPath = dictFileName;
    plaintextDictPath = userDictFileName;
    setLocale(RO_LOCALE);
  }

  @Override
  public final String getFileName() {
    return binaryDictPath;
  }

  @Override
  public String getManualAdditionsFileName() {
    return null;  // TODO: make use of this
  }

  @Override
  public final List<AnalyzedTokenReadings> tag(
      final List<String> sentenceTokens) throws IOException {
    final List<AnalyzedTokenReadings> tokenReadings = new ArrayList<>();
    int pos = 0;
    final IStemmer morfologik = new DictionaryLookup(getDictionary());
    if (manualTagger == null && plaintextDictPath != null) {
      manualTagger = new ManualTagger(JLanguageTool.getDataBroker().getFromResourceDirAsStream(plaintextDictPath));
    }

    for (final String word : sentenceTokens) {
      final List<AnalyzedToken> l = new ArrayList<>();
      final String lowerCaseWord = word.toLowerCase(RO_LOCALE);
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
        final List<TaggedWord> manualTags = manualTagger.tag(lowerCaseWord);
        for (TaggedWord manualTag : manualTags) {
          l.add(new AnalyzedToken(word, manualTag.getPosTag(), manualTag.getLemma()));
        }
      }

      if (l.isEmpty()) {
        l.add(new AnalyzedToken(word, null, null));
      }
      tokenReadings.add(new AnalyzedTokenReadings(l, pos));
      pos += word.length();
    }

    return tokenReadings;
  }

}
