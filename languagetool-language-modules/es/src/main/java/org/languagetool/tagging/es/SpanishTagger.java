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
package org.languagetool.tagging.es;

import morfologik.stemming.DictionaryLookup;
import morfologik.stemming.IStemmer;
import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.tagging.BaseTagger;
import org.languagetool.tools.StringTools;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Spanish Tagger.
 * 
 * @author Jaume Ortolà
 */
public class SpanishTagger extends BaseTagger {
  
  public static final SpanishTagger INSTANCE = new SpanishTagger();

  private static final Pattern ADJ_PART_FS = Pattern.compile("VMP00SF|A[QO].[FC]S.");
  private static final Pattern VERB = Pattern.compile("V.+");
  private static final Pattern PREFIXES_FOR_VERBS = Pattern.compile("(auto)([^r]...+)",
      Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  private static final Pattern PREFIXES_FOR_VERBS2 = Pattern.compile("(autor)(r...+)",
      Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  private static final Pattern PREFIXES_FOR_ADJ = Pattern.compile("(.+)-(.+)",
      Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  private static final Pattern ADJ = Pattern.compile("AQ.+");
  private static final Pattern ADJ_MS = Pattern.compile("AQ.MS.|AQ.CS.|AQ.MN.");
  private static final Pattern NO_PREFIXES_FOR_ADJ = Pattern.compile("(anti|pre|ex|pro|afro|ultra|super|súper)",
      Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    
  public SpanishTagger() {
    super("/es/es-ES.dict", new Locale("es"));
  }

  @Override
  public List<AnalyzedTokenReadings> tag(final List<String> sentenceTokens) {

    final List<AnalyzedTokenReadings> tokenReadings = new ArrayList<>();
    int pos = 0;
    final IStemmer dictLookup = new DictionaryLookup(getDictionary());

    for (String word : sentenceTokens) {
      final List<AnalyzedToken> l = new ArrayList<>();
      final String lowerWord = word.toLowerCase(locale);
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
      // tag all-uppercase proper nouns (ex. FRANCIA)
      if (isAllUpper) {
        final String firstUpper = StringTools.uppercaseFirstChar(lowerWord);
        List<AnalyzedToken> firstupperTaggerTokens = asAnalyzedTokenListForTaggedWords(word,
            getWordTagger().tag(firstUpper));
        addTokens(firstupperTaggerTokens, l);
      }

      // additional tagging with prefixes
      if (l.isEmpty() && !isMixedCase) {
        addTokens(additionalTags(word, dictLookup), l);
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

  @Nullable
  protected List<AnalyzedToken> additionalTags(String word, IStemmer stemmer) {
    final IStemmer dictLookup = new DictionaryLookup(getDictionary());
    List<AnalyzedToken> additionalTaggedTokens = new ArrayList<>();
    // Any well-formed adverb with suffix -mente is tagged as an adverb (RG)
    // adjective o participle feminine singular + -mente
    final String lowerWord = word.toLowerCase(locale);
    if (lowerWord.endsWith("mente")) {
      final String possibleAdj = lowerWord.replaceAll("^(.+)mente$", "$1");
      List<AnalyzedToken> taggerTokens;
      taggerTokens = asAnalyzedTokenList(possibleAdj, dictLookup.lookup(possibleAdj));
      for (AnalyzedToken taggerToken : taggerTokens) {
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
    // Any well-formed verb with prefixes is tagged as a verb copying the original
    // tags
    Matcher matcher = PREFIXES_FOR_VERBS.matcher(word);
    if (matcher.matches()) {
      final String possibleVerb = matcher.group(2).toLowerCase();
      List<AnalyzedToken> taggerTokens = asAnalyzedTokenList(possibleVerb, dictLookup.lookup(possibleVerb));
      for (AnalyzedToken taggerToken : taggerTokens) {
        final String posTag = taggerToken.getPOSTag();
        if (posTag != null) {
          final Matcher m = VERB.matcher(posTag);
          if (m.matches()) {
            String lemma = matcher.group(1).toLowerCase().concat(taggerToken.getLemma());
            additionalTaggedTokens.add(new AnalyzedToken(word, posTag, lemma));
          }
        }
      }
      return additionalTaggedTokens;
    }
   
    matcher = PREFIXES_FOR_VERBS2.matcher(word);
    if (matcher.matches()) {
      final String possibleVerb = matcher.group(2).toLowerCase();
      List<AnalyzedToken> taggerTokens = asAnalyzedTokenList(possibleVerb, dictLookup.lookup(possibleVerb));
      for (AnalyzedToken taggerToken : taggerTokens) {
        final String posTag = taggerToken.getPOSTag();
        if (posTag != null) {
          final Matcher m = VERB.matcher(posTag);
          if (m.matches()) {
            String lemma = matcher.group(1).toLowerCase().concat(taggerToken.getLemma());
            additionalTaggedTokens.add(new AnalyzedToken(word, posTag, lemma));
          }
        }
      }
      return additionalTaggedTokens;
    }

    matcher = PREFIXES_FOR_ADJ.matcher(word);
    if (matcher.matches()) {
      final String possibleAdjPrefix = matcher.group(1).toLowerCase();
      Matcher matcher2 = NO_PREFIXES_FOR_ADJ.matcher(possibleAdjPrefix);
      if (!matcher2.matches()) {
        final String possibleAdj = matcher.group(2).toLowerCase();
        boolean prefixMatches = false;
        boolean adjMatches = false;
        String newPostag = "";
        String newLemma = "";
        List<AnalyzedToken> taggerTokens = asAnalyzedTokenList(possibleAdjPrefix, dictLookup.lookup(possibleAdjPrefix));
        for (AnalyzedToken taggerToken : taggerTokens) {
          final String posTag = taggerToken.getPOSTag();
          if (posTag != null) {
            final Matcher m = ADJ_MS.matcher(posTag);
            if (m.matches()) {
              prefixMatches = true;
              break;
            }
          }
        }
        taggerTokens = asAnalyzedTokenList(possibleAdj, dictLookup.lookup(possibleAdj));
        for (AnalyzedToken taggerToken : taggerTokens) {
          final String posTag = taggerToken.getPOSTag();
          if (posTag != null) {
            final Matcher m = ADJ.matcher(posTag);
            if (m.matches()) {
              adjMatches = true;
              newPostag = posTag;
              newLemma = possibleAdjPrefix + "-" + taggerToken.getLemma();
              break;
            }
          }
        }
        if (adjMatches && prefixMatches) {
          additionalTaggedTokens.add(new AnalyzedToken(word, newPostag, newLemma));
          return additionalTaggedTokens;
        }
      }
    }

    return null;
  }

  private void addTokens(final List<AnalyzedToken> taggedTokens, final List<AnalyzedToken> l) {
    if (taggedTokens != null) {
      l.addAll(taggedTokens);
    }
  }
}
