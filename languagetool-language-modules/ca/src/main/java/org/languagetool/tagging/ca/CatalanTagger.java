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
package org.languagetool.tagging.ca;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import morfologik.stemming.Dictionary;
import morfologik.stemming.DictionaryLookup;
import morfologik.stemming.WordData;

import org.languagetool.AnalyzedToken;
import org.languagetool.JLanguageTool;
import org.languagetool.tagging.BaseTagger;

/**
 * Catalan Tagger
 *
 * Based on FreeLing tagger dictionary
 *
 * @author Jaume Ortolà 
 */
public class CatalanTagger extends BaseTagger {

  private static final String DICT_FILENAME = "/ca/catalan.dict";
  private static final Pattern ADJ_PART_FS = Pattern.compile("VMP00SF.|A[QO]0[FC][SN].");
  private static final Pattern VERB = Pattern.compile("V.+");

  @Override
  public final String getFileName() {
    return DICT_FILENAME;
  }

  public CatalanTagger() {
    super();
    setLocale(new Locale("ca"));
    this.dontTagLowercaseWithUppercase();
  }

  public boolean existsWord(String word) throws IOException {
    // caching Lametyzator instance - lazy init
    if (dictLookup == null) {
      final URL url = JLanguageTool.getDataBroker().getFromResourceDirAsUrl(DICT_FILENAME);
      dictLookup = new DictionaryLookup(Dictionary.read(url));
    }
    final String lowerWord = word.toLowerCase(conversionLocale);
    List<WordData> posTagsFromDict = dictLookup.lookup(lowerWord);
    if (posTagsFromDict.isEmpty()) {
      posTagsFromDict = dictLookup.lookup(word);
      if (posTagsFromDict.isEmpty())
        return false;
    }
    return true;
  }

  @Override
  public List<AnalyzedToken> additionalTags(String word) {
    List<AnalyzedToken> additionalTaggedTokens = new ArrayList<AnalyzedToken>();
    //Any well-formed adverb with suffix -ment is tagged as an adverb (RG)
    //Adjectiu femení singular o participi femení singular + -ment
    if (word.endsWith("ment")){
      final String lowerWord = word.toLowerCase(conversionLocale);
      final String possibleAdj = lowerWord.replaceAll("^(.+)ment$", "$1");
      List<AnalyzedToken> taggerTokens;
      taggerTokens = asAnalyzedTokenList(possibleAdj, dictLookup.lookup(possibleAdj));
      for (AnalyzedToken taggerToken : taggerTokens ) {
        final String posTag = taggerToken.getPOSTag();
        if (posTag != null) {
          final Matcher m = ADJ_PART_FS.matcher(posTag);
          if (m.matches()) {
            additionalTaggedTokens.add(new AnalyzedToken(word, "RG", lowerWord));
            return additionalTaggedTokens;
          }
        }
      }
    }
    //Any well-formed verb with preffix auto- is tagged as a verb copying the original tags 
    if (word.startsWith("auto")){
      final String lowerWord = word.toLowerCase(conversionLocale);
      final String possibleVerb = lowerWord.replaceAll("^auto(.+)$", "$1");
      List<AnalyzedToken> taggerTokens;
      taggerTokens = asAnalyzedTokenList(possibleVerb, dictLookup.lookup(possibleVerb));
      for (AnalyzedToken taggerToken : taggerTokens ) {
        final String posTag = taggerToken.getPOSTag();
        if (posTag != null) {
          final Matcher m = VERB.matcher(posTag);
          if (m.matches()) {
            String lemma="auto".concat(taggerToken.getLemma());
            additionalTaggedTokens.add(new AnalyzedToken(word, posTag, lemma));
          }
        }
      }
      return additionalTaggedTokens;
    }
    return null;
  }

}
