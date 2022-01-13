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
import org.languagetool.tagging.BaseTagger;
import org.languagetool.tools.StringTools;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * English Part-of-speech tagger.
 * The POS tagset is described in
 * <a href="https://github.com/languagetool-org/languagetool/blob/master/languagetool-language-modules/en/src/main/resources/org/languagetool/resource/en/tagset.txt">tagset.txt</a>
 * 
 * @author Marcin Milkowski
 */
public class EnglishTagger extends BaseTagger {
  public static final EnglishTagger INSTANCE = new EnglishTagger();

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
      
      if (l.isEmpty() && lowerWord.endsWith("in'")) {
        String correctedWord = word;
        if (isAllUpper) {
          correctedWord = correctedWord.substring(0, correctedWord.length() - 1) + "G";
        } else {
          correctedWord = correctedWord.substring(0, correctedWord.length() - 1) + "g";
        }
        List<AnalyzedToken> taggerTokens2 = asAnalyzedTokenListForTaggedWords(word, getWordTagger().tag(correctedWord));
        // normal case:
        addTokens(taggerTokens2, l);
        // tag non-lowercase (alluppercase or startuppercase), but not mixed-case words with lowercase word tags:
        if (!isLowercase && !isMixedCase) {
          List<AnalyzedToken> lowerTaggerTokens = asAnalyzedTokenListForTaggedWords(word,
              getWordTagger().tag(correctedWord.toLowerCase()));
          addTokens(lowerTaggerTokens, l);
        }
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

    return tokenReadings;
  }
  
  private void addTokens(final List<AnalyzedToken> taggedTokens, final List<AnalyzedToken> l) {
    if (taggedTokens != null) {
      l.addAll(taggedTokens);
    }
  }
}
