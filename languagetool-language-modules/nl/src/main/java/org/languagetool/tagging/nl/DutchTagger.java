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
  // custom code to deal with words carrying optional accents
  @Override
  public List<AnalyzedTokenReadings> tag(final List<String> sentenceTokens) {

    final List<AnalyzedTokenReadings> tokenReadings = new ArrayList<>();
    int pos = 0;

    for (String word : sentenceTokens) {
      boolean ignoreSpelling = false;
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
        String word2 = lowerWord;
        // remove single accented characterd
        word2 = word2.replace('á', 'a').replace('é', 'e').replace('í', 'i').replace('ó', 'o').replace('ú', 'u');
        
        // TODO: remove optional hyphens one at a time; for now just all will be removed
        // best would be to check the parts as well (uncompound)
        word2 = word2.replaceAll("([a-z])-([a-z])", "$1$2");
        
        if (!word2.equals(word)) {
          List<AnalyzedToken> l2 = asAnalyzedTokenListForTaggedWords(word, getWordTagger().tag(word2));
          if (l2 != null) {
            addTokens(l2, l);

            String word3 = word;
            word3 = word.replaceAll("([a-z])-([a-z])", "$1$2");
            // remove allowed accented characterd

            word3 = word3.replace("áá", "aa");
            word3 = word3.replace("áé", "ae");
            word3 = word3.replace("áí", "ai");
            word3 = word3.replace("áú", "au");
            word3 = word3.replace("éé", "ee");
            word3 = word3.replace("éí", "ei");
            word3 = word3.replace("éú", "eu");
            word3 = word3.replace("íé", "ie");
            word3 = word3.replace("óé", "oe");
            word3 = word3.replace("óí", "oi");
            word3 = word3.replace("óó", "oo");
            word3 = word3.replace("óú", "ou");
            word3 = word3.replace("úí", "ui");
            word3 = word3.replace("úú", "uu");
            word3 = word3.replace("íj", "ij");
            
            word3 = word3.replaceAll("(^|[^aeiou])á([^aeiou]|$)", "$1a$2");
            word3 = word3.replaceAll("(^|[^aeiou])é([^aeiou]|$)", "$1e$2");
            word3 = word3.replaceAll("(^|[^aeiou])í([^aeiou]|$)", "$1i$2");
            word3 = word3.replaceAll("(^|[^aeiou])ó([^aeiou]|$)", "$1o$2");
            word3 = word3.replaceAll("(^|[^aeiou])ú([^aeiou]|$)", "$1u$2");
            if (word3.equals(word2)) {
              ignoreSpelling = true;
            }
          }
        }
      }

      if (l.isEmpty()) {
        l.add(new AnalyzedToken(word, null, null));
      }

      AnalyzedTokenReadings atr = new AnalyzedTokenReadings(l, pos);
      if (ignoreSpelling) {
        atr.ignoreSpelling();
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
