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
import java.lang.StringBuilder;
import java.util.Locale;

public class ArabicTagger extends BaseTagger {

  public ArabicTagger() {
    super("/ar/arabic.dict", new Locale("ar"));
  }

  @Override
  public String getManualAdditionsFileName() {
    return "/ar/added.txt";
  }

  /* Add the flag to an encoded tag */
  public String addTag(String postag, String flag) {
    StringBuilder tmp = new StringBuilder(postag);
    if (flag == "W") {
      tmp.setCharAt(postag.length() - 3, 'W');
    } else if (flag == "K") {
      tmp.setCharAt(postag.length() - 2, 'K');
    } else if (flag == "L") {
      tmp.setCharAt(postag.length() - 2, 'L');
    }
    return tmp.toString();
  }

  /* test if flag exists */
  public boolean hasFlag(String postag, String flag) {
    StringBuilder tmp = new StringBuilder(postag);
    if (flag == "has_pronoun") {
      return postag.endsWith("H");
    } else if (flag == "has_jar") {
      return postag.contains("B");
    }
    return false;
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

    // Any well-formed word started by conjunction letters like Waw and Feh is the same word without the conjunction + a conjunction tag
    // case of Lam jar + Al ta3rif
    // w + ll or f + ll
    List<String> tags = new ArrayList();
    List<String> tags_with_jar = new ArrayList();
    String possibleWord = word;
    if (word.startsWith("ولل") || word.startsWith("فلل")) {
      possibleWord = possibleWord.replaceAll("^[وف]لل", "بال");
      tags_with_jar.add("L"); // only if has_jar
      tags_with_jar.add("W"); // only if has_jar
    } else if (word.startsWith("وك") || word.startsWith("فك") || word.startsWith("ول") || word.startsWith("فل")) {
      possibleWord = possibleWord.replaceAll("^[وف][كل]", "ب");
      if (word.startsWith("وك") || word.startsWith("فك")) {
        tags_with_jar.add("K"); // only if has_jar
      }
      if (word.startsWith("ول") || word.startsWith("فل")) {
        tags_with_jar.add("L"); // only if has_jar
      }
      tags_with_jar.add("W"); // only if has_jar
    } else if (word.startsWith("و") || word.startsWith("ف")) {
      possibleWord = possibleWord.replaceAll("^[وف]", "");
      tags.add("W");

    } else if (word.startsWith("لل") || word.startsWith("لل")) {
      possibleWord = possibleWord.replaceAll("^لل", "بال");
      tags_with_jar.add("L"); // only if has_jar
    } else if (word.startsWith("ك")) {
      possibleWord = possibleWord.replaceAll("^[ك]", "ب");
      tags_with_jar.add("K"); // only if has_jar
    } else if (word.startsWith("ل")) {
      possibleWord = possibleWord.replaceAll("^[ل]", "ب");
      tags_with_jar.add("L"); // only if has_jar
    }

    // to avoid redundancy all attached pronouns in dictionary are represented only by a generic pronoun
    //  for words like بيتك بيتكما بيتهم بيتنا بيتكن there are one word which is بيتك
    // we can simulate all word forms into the same tag which endec by H( H is tag for attached pronouns الضمائر المصتلة)
    if (word.endsWith("ه")
      || word.endsWith("ها")
      || word.endsWith("هما")
      || word.endsWith("كما")
      || word.endsWith("هم")
      || word.endsWith("هن")
      || word.endsWith("كم")
      || word.endsWith("كن")
      || word.endsWith("نا")
    ) {
      possibleWord = possibleWord.replaceAll("(ه|ها|هما|هم|هن|كما|كم|كن|نا|ي)$", "ك");
      List<AnalyzedToken> taggerTokens;
      taggerTokens = asAnalyzedTokenList(possibleWord, stemmer.lookup(possibleWord));
      for (AnalyzedToken taggerToken : taggerTokens) {
        String posTag = taggerToken.getPOSTag();
        for (int i = 0; i < tags_with_jar.size(); i++) {
          if (hasFlag(posTag, "has_jar"))
            posTag = addTag(posTag, tags_with_jar.get(i));
        }
        for (int i = 0; i < tags.size(); i++) {
          posTag = addTag(posTag, tags.get(i));
        }
        additionalTaggedTokens.add(new AnalyzedToken(word, posTag, taggerToken.getLemma()));
      }
      // if possible word has a conjuction at the begining like Waw or FEH
      if (word.startsWith("و") || word.startsWith("ف")) {
        possibleWord = possibleWord.replaceAll("^[وف]", "");
        taggerTokens = asAnalyzedTokenList(possibleWord, stemmer.lookup(possibleWord));
        for (AnalyzedToken taggerToken : taggerTokens) {
          String posTag = taggerToken.getPOSTag();
          if (hasFlag(posTag, "has_pronoun")) {
            posTag = addTag(posTag, "W");
          }
          for (int i = 0; i < tags_with_jar.size(); i++) {
            if (hasFlag(posTag, "has_jar"))
              posTag = addTag(posTag, tags_with_jar.get(i));
          }
          for (int i = 0; i < tags.size(); i++) {
            posTag = addTag(posTag, tags.get(i));
          }
          additionalTaggedTokens.add(new AnalyzedToken(word, posTag, taggerToken.getLemma()));
        }
      }
    } else {
      List<AnalyzedToken> taggerTokens = asAnalyzedTokenList(word, stemmer.lookup(possibleWord));
      for (AnalyzedToken taggerToken : taggerTokens) {
        String posTag = taggerToken.getPOSTag();
        for (int i = 0; i < tags_with_jar.size(); i++) {
          if (hasFlag(posTag, "has_jar"))
            posTag = addTag(posTag, tags_with_jar.get(i));
        }
        for (int i = 0; i < tags.size(); i++) {
          posTag = addTag(posTag, tags.get(i));
        }
        additionalTaggedTokens.add(new AnalyzedToken(word, posTag, taggerToken.getLemma()));
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
