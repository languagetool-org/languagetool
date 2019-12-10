/* LanguageTool, a natural language style checker
 * Copyright (C) 2019 Sohaib Afifi, Taha Zerrouki
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
package org.languagetool.tagging.ar;

import morfologik.stemming.DictionaryLookup;
import morfologik.stemming.IStemmer;
import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.tagging.BaseTagger;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ArabicTagger extends BaseTagger {

  public ArabicTagger() {
    super("/ar/arabic.dict", new Locale("ar"));
  }

  @Override
  public String getManualAdditionsFileName() {
    return "/ar/added.txt";
  }


  @Override
  public List<AnalyzedTokenReadings> tag(final List<String> sentenceTokens) {

    final List<AnalyzedTokenReadings> tokenReadings = new ArrayList<>();
    final IStemmer dictLookup = new DictionaryLookup(getDictionary());
    int pos = 0;
    for (String word : sentenceTokens) {
      final List<AnalyzedToken> l = new ArrayList<>();
      List<AnalyzedToken> taggerTokens = asAnalyzedTokenListForTaggedWords(word, getWordTagger().tag(word));
      addTokens(taggerTokens, l);
      // additional tagging with prefixes
      if (l.isEmpty()) {
        addTokens(additionalTags(word, dictLookup), l);
      }
      if (l.isEmpty()) {
        l.add(new AnalyzedToken(word, null, null));
      }
      tokenReadings.add(new AnalyzedTokenReadings(l, pos));
      pos += word.length();
    }
    return tokenReadings;
  }


  @Nullable
  protected List<AnalyzedToken> additionalTags(String word, IStemmer stemmer) {
    List<AnalyzedToken> additionalTaggedTokens = new ArrayList<>();
    //Any well-formed word started by conjuction letters like Waw and Feh is the same word without  the conjuction + a conjuction tag
    if (word.startsWith("و") || word.startsWith("ف")) {
      final String possibleWord = word.replaceAll("^[وف]", "");
      List<AnalyzedToken> taggerTokens;
      taggerTokens = asAnalyzedTokenList(word, stemmer.lookup(possibleWord));
      for (AnalyzedToken taggerToken : taggerTokens) {
        final String posTag = taggerToken.getPOSTag();
        additionalTaggedTokens.add(new AnalyzedToken(word, posTag + "W", word));
      }
    }
    // to avoid redendecy all attached pronouns in dictionary are represented only by a generic pronoun
    //  for words like بيتك بيتكما بيتهم بيتنا بيتكن there are one word which is بيتك
    // we can simulate all word forms into the same tag which endec by H( H is tag for attached pronouns الضمائر المصتلة)
    if (word.endsWith("ه") || word.endsWith("ها") ||   word.endsWith("هما") ||       word.endsWith("كما") ||    word.endsWith("هم") || word.endsWith("هن") ||
    word.endsWith("كم") || word.endsWith("كن") ||     word.endsWith("نا")   ) {
      final String possibleWord = word.replaceAll("(ه|ها|هما|هم|هن|كما|كم|كن|نا|ي)$", "ك");
      List<AnalyzedToken> taggerTokens;
      taggerTokens = asAnalyzedTokenList(word, stemmer.lookup(possibleWord));
      for (AnalyzedToken taggerToken : taggerTokens) {
        final String posTag = taggerToken.getPOSTag();
        if (posTag.endsWith("H"))
        {
          additionalTaggedTokens.add(new AnalyzedToken(word, posTag, word));
        }
      }
      // if possible word has a conjuction at the begining like Waw or FEH
      if (word.startsWith("و") || word.startsWith("ف")) {
        final String possibleWord2 = possibleWord.replaceAll("^[وف]", "");
        //List<AnalyzedToken> taggerTokens;
        taggerTokens = asAnalyzedTokenList(word, stemmer.lookup(possibleWord2));
        for (AnalyzedToken taggerToken : taggerTokens) {
          final String posTag = taggerToken.getPOSTag();
          if (posTag.endsWith("H"))
          {
            additionalTaggedTokens.add(new AnalyzedToken(word, posTag+"W", word));
          }
        }

      }
    }
    return additionalTaggedTokens;
  }


  private void addTokens(final List<AnalyzedToken> taggedTokens, final List<AnalyzedToken> l) {
    if (taggedTokens != null) {
      l.addAll(taggedTokens);
    }
  }
}
