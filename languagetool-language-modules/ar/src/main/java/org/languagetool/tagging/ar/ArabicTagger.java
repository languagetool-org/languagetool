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
import org.languagetool.language.Arabic;
import org.languagetool.tagging.BaseTagger;

import java.util.*;

/**
 * @since 4.9
 */
public class ArabicTagger extends BaseTagger {

  public ArabicTagger() {
    super("/ar/arabic.dict", new Locale("ar"));
  }

  /* Add the flag to an encoded tag */
  public String addTag(String postag, String flag) {
    StringBuilder tmp = new StringBuilder(postag);
    if (flag.equals("W")) {
      tmp.setCharAt(postag.length() - 3, 'W');
    } else if (flag.equals("K")) {
      tmp.setCharAt(postag.length() - 2, 'K');
    } else if (flag.equals("L")) {
      tmp.setCharAt(postag.length() - 2, 'L');
    }
    return tmp.toString();
  }
  
  @Override
  public List<AnalyzedTokenReadings> tag(List<String> sentenceTokens) {
    List<AnalyzedTokenReadings> tokenReadings = new ArrayList<>();
    IStemmer dictLookup = new DictionaryLookup(getDictionary());
    int pos = 0;
    for (String word : sentenceTokens) {
      List<AnalyzedToken> l = new ArrayList<>();
      String striped = word.replaceAll("[" + Arabic.TASHKEEL_CHARS + "]", "");
      List<AnalyzedToken> taggerTokens = asAnalyzedTokenListForTaggedWords(word, getWordTagger().tag(striped));
      addTokens(taggerTokens, l);
      // additional tagging with prefixes
      if (l.isEmpty()) {
        addTokens(additionalTags(striped, dictLookup), l);
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
    List<String> tags = new ArrayList<>();
    String possibleWord = word;
    if (possibleWord.startsWith("و") || possibleWord.startsWith("ف")) {
      tags.add("W");
      possibleWord = possibleWord.replaceAll("^[وف]", "");
    }
    if (possibleWord.startsWith("لل")) {
      tags.add("L");
      possibleWord = possibleWord.replaceAll("^[لل]", "");
    } else if (possibleWord.startsWith("ك")) {
      tags.add("K");
      possibleWord = possibleWord.replaceAll("^[ك]", "");
    } else if (possibleWord.startsWith("ل")) {
      tags.add("L");
      possibleWord = possibleWord.replaceAll("^[ل]", "");
    }

    if (possibleWord.endsWith("ه")
      || possibleWord.endsWith("ها")
      || possibleWord.endsWith("هما")
      || possibleWord.endsWith("كما")
      || possibleWord.endsWith("هم")
      || possibleWord.endsWith("هن")
      || possibleWord.endsWith("كم")
      || possibleWord.endsWith("كن")
      || possibleWord.endsWith("نا")
    ) {
      possibleWord = possibleWord.replaceAll("(ه|ها|هما|هم|هن|كما|كم|كن|نا|ي)$", "ك");
    }
    List<AnalyzedToken> taggerTokens;
    taggerTokens = asAnalyzedTokenList(possibleWord, stemmer.lookup(possibleWord));
    for (AnalyzedToken taggerToken : taggerTokens) {
      String posTag = taggerToken.getPOSTag();
      for (String tag : tags) {
        posTag = addTag(posTag, tag);
      }
      additionalTaggedTokens.add(new AnalyzedToken(word, posTag, taggerToken.getLemma()));
    }
    return additionalTaggedTokens;
  }

  private void addTokens(List<AnalyzedToken> taggedTokens, List<AnalyzedToken> l) {
    if (taggedTokens != null) {
      l.addAll(taggedTokens);
    }
  }
}
