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
package org.languagetool.tagging.nl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.tagging.BaseTagger;
import org.languagetool.tools.StringTools;

/**
 * Dutch tagger.
 * 
 * @author Marcin Milkowski
 */
public class DutchTagger extends BaseTagger {

  @Override
  public String getManualAdditionsFileName() {
    return "/nl/added.txt";
  }

  @Override
  public String getManualRemovalsFileName() {
    return "/nl/removed.txt";
  }

  public DutchTagger() {
    super("/nl/dutch.dict", new Locale("nl"));
  }

  @Override
  public List<AnalyzedTokenReadings> tag(final List<String> sentenceTokens) {

    final List<AnalyzedTokenReadings> tokenReadings = new ArrayList<>();
    int pos = 0;

    for (String word : sentenceTokens) {
      final List<AnalyzedToken> l = new ArrayList<>();
      final String lowerWord = word.toLowerCase(conversionLocale);
      final boolean isLowercase = word.equals(lowerWord);
      final boolean isMixedCase = StringTools.isMixedCase(word);
      final boolean isAllUpper = StringTools.isAllUppercase(word);
      List<AnalyzedToken> taggerTokens = asAnalyzedTokenListForTaggedWords(word, getWordTagger().tag(word));

      // normal case:
      addTokens(taggerTokens, l);
      // tag non-lowercase (alluppercase or startuppercase), but not mixedcase
      // word with lowercase word tags:
      if (!isLowercase && !isMixedCase) {
        List<AnalyzedToken> lowerTaggerTokens = asAnalyzedTokenListForTaggedWords(word, getWordTagger().tag(lowerWord));
        addTokens(lowerTaggerTokens, l);
      }

      // tag all-uppercase proper nouns
      if (l.isEmpty() && isAllUpper) {
        final String firstUpper = StringTools.uppercaseFirstChar(lowerWord);
        List<AnalyzedToken> firstupperTaggerTokens = asAnalyzedTokenListForTaggedWords(word,
            getWordTagger().tag(firstUpper));
        addTokens(firstupperTaggerTokens, l);
      }

      if (l.isEmpty()) {
        String word2 = word;
        word2 = word2.replace("á", "a").replace("é", "e").replace("í", "i").replace("ó", "o").replace("ú", "u");
        if (!word2.equals(word)) {
          List<AnalyzedToken> l2 = asAnalyzedTokenListForTaggedWords(word, getWordTagger().tag(word2));
          if (l2 != null) {
            addTokens(l2, l);
          }
        }
      }

      if (l.isEmpty()) {
        l.add(new AnalyzedToken(word, null, null));
      }

      AnalyzedTokenReadings atr = new AnalyzedTokenReadings(l, pos);

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
