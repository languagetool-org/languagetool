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
package org.languagetool.tagging.en;

import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.chunking.ChunkTag;
import org.languagetool.tagging.BaseTagger;
import org.languagetool.tools.StringTools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * English Part-of-speech tagger.
 * The POS tagset is described in
 * <a href="https://github.com/languagetool-org/languagetool/blob/master/languagetool-language-modules/en/src/main/resources/org/languagetool/resource/en/tagset.txt">tagset.txt</a>
 * 
 * @author Marcin Milkowski
 */
public class EnglishTagger extends BaseTagger {
  public EnglishTagger() {
    // intern tags because we only have 47 types and get megabytes of duplicated strings
    super("/en/english.dict", Locale.ENGLISH, false, true);
  }
  
  @Override
  public List<AnalyzedTokenReadings> tag(final List<String> sentenceTokens) {

    final List<AnalyzedTokenReadings> tokenReadings = new ArrayList<>();
    int pos = 0;
    for (String word : sentenceTokens) {
      // This hack allows all rules and dictionary entries to work with typewriter apostrophe
      boolean containsTypographicApostrophe = false;
      if (word.length() > 1) {
        if (word.contains("’")) {
          containsTypographicApostrophe = true;
          word = word.replace("’", "'");
        }
      }
      final List<AnalyzedToken> l = new ArrayList<>();
      final String lowerWord = word.toLowerCase(locale);
      final boolean isLowercase = word.equals(lowerWord);
      final boolean isMixedCase = StringTools.isMixedCase(word);
      final boolean isAllUpper = StringTools.isAllUppercase(word);
      List<AnalyzedToken> taggerTokens = asAnalyzedTokenListForTaggedWords(word, getWordTagger().tag(word));
      
      // normal case:
      addTokens(taggerTokens, l);
      // tag non-lowercase (alluppercase or startuppercase), but not mixed-case words with lowercase word tags:
      if (!isLowercase && !isMixedCase) {
        List<AnalyzedToken> lowerTaggerTokens = asAnalyzedTokenListForTaggedWords(word, getWordTagger().tag(lowerWord));
        addTokens(lowerTaggerTokens, l);
      }
      
      //tag all-uppercase proper nouns (ex. FRANCE)
      if (l.isEmpty() && isAllUpper) {
        final String firstUpper = StringTools.uppercaseFirstChar(lowerWord);
        List<AnalyzedToken> firstupperTaggerTokens = asAnalyzedTokenListForTaggedWords(word, getWordTagger().tag(firstUpper));
        addTokens(firstupperTaggerTokens, l);
      }

      // additional tagging with prefixes   removed: && !isMixedCase
      /*if (l.isEmpty()) {
        addTokens(additionalTags(word), l);
      }*/

      if (l.isEmpty()) {
        l.add(new AnalyzedToken(word, null, null));
      }
      
      AnalyzedTokenReadings atr = new AnalyzedTokenReadings(l, pos);
      if (containsTypographicApostrophe) {
        atr.setTypographicApostrophe();
      }

      tokenReadings.add(atr);
      pos += word.length();
    }

    /** analyse contraction pattern before returning tags */
    return filterContractionPattern(tokenReadings);
  }
  /**
   * A Map stores English contraction pattern as following
   * i.e can't or Can't
   * key: ca or Ca
   * value: ["can", "MD", "n't"]
   *         verb   POS contraction suffix
   */
  private Map<String, List<String>> englishContraction = new HashMap<String, List<String> >();
  /**
   * The function looks for English Contraction pattern
   * Found pattern get treated by upating the relavent readings
   * @param tokenReadings 
   * @return
   */
  private List<AnalyzedTokenReadings> filterContractionPattern(List<AnalyzedTokenReadings> tokenReadings) {
    englishContraction.put("ca",Arrays.asList("can","MD", "n't"));
    englishContraction.put("Ca",Arrays.asList("can","MD", "n't"));
    
    for(int i = 1; i < tokenReadings.size(); i++){
      String contractionPrefix = tokenReadings.get(i-1).getCleanToken();
      String contractionSuffix = tokenReadings.get(i).getCleanToken();
      if(englishContraction.containsKey(contractionPrefix)){
        List<String> contraction = englishContraction.get(contractionPrefix);
        String matchSuffix = contraction.get(2);
        if(contractionSuffix.equals(matchSuffix)){
          AnalyzedToken token = new AnalyzedToken(
            tokenReadings.get(i-1).getToken()
            ,contraction.get(1)
            ,contraction.get(0));
          tokenReadings.get(i-1).addReading(token, "ruleApplied");
          tokenReadings.get(i-1).leaveReading(token);
        }
      }
    }
    return tokenReadings;
  }

  private void addTokens(final List<AnalyzedToken> taggedTokens, final List<AnalyzedToken> l) {
    if (taggedTokens != null) {
      l.addAll(taggedTokens);
    }
  }
}
