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
package org.languagetool.tagging.fr;

import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.chunking.ChunkTag;
import org.languagetool.tagging.BaseTagger;
import org.languagetool.tools.StringTools;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * French Tagger
 * 
 * Based on Dicollecte (http://www.dicollecte.org/) implemented in FSA.
 * 
 * @author Jaume Ortolà
 */
public class FrenchTagger extends BaseTagger {
  
  public static final FrenchTagger INSTANCE = new FrenchTagger();

  private static final Pattern VERB = Pattern.compile("V .+");
  private static final Pattern PREFIXES_FOR_VERBS = Pattern.compile(
      "(auto|auto-|re-|sur-)([^-].*[aeiouêàéèíòóïü].+[aeiouêàéèíòóïü].*)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

  private static final Pattern NOUN_ADJ = Pattern.compile("[NJ] .+|V ppa.*");
  private static final Pattern PREFIXES_NOUN_ADJ = Pattern.compile(
      "(post-|sur-|mini-|méga-|demi-|péri-|anti-|géo-|nord-|sud-|néo-|méga-|ultra-|pro-|inter-|micro-|macro-|sous-|haut-|auto-|ré-|pré-|super-|vice-|hyper-|proto-|grand-|pseudo-)(.+)",
      Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  private static final Pattern PREFIXES_FOR_NOUN_ADJ = Pattern.compile(
      "(mini|méga)([^-].*[aeiouêàéèíòóïü].+[aeiouêàéèíòóïü].*)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  // |nord-|sud-|post

  public FrenchTagger() {
    super("/fr/french.dict", Locale.FRENCH, false);
  }

  @Override
  public boolean overwriteWithManualTagger() {
    return false;
  }

  @Override
  public List<AnalyzedTokenReadings> tag(final List<String> sentenceTokens) {

    final List<AnalyzedTokenReadings> tokenReadings = new ArrayList<>();
    int pos = 0;
    for (String word : sentenceTokens) {
      // This hack allows all rules and dictionary entries to work with
      // typewriter apostrophe
      boolean containsTypewriterApostrophe = false;
      boolean containsTypographicApostrophe = false;
      if (word.length() > 1) {
        if (word.contains("'")) {
          containsTypewriterApostrophe = true;
        }
        if (word.contains("’")) {
          containsTypographicApostrophe = true;
          word = word.replace("’", "'");
        }
      }
      List<AnalyzedToken> l = tagWord(word, word);
      if (l.isEmpty() && word.toLowerCase().contains("oe")) {
        l = tagWord(word.replaceAll("oe", "œ").replaceAll("OE", "Œ"), word);
      }
      if (l.isEmpty()) {
        l.add(new AnalyzedToken(word, null, null));
      }
      AnalyzedTokenReadings atr = new AnalyzedTokenReadings(l, pos);
      if (containsTypewriterApostrophe) {
        List<ChunkTag> listChunkTags = new ArrayList<>();
        listChunkTags.add(new ChunkTag("containsTypewriterApostrophe"));
        atr.setChunkTags(listChunkTags);
      }
      if (containsTypographicApostrophe) {
        List<ChunkTag> listChunkTags = new ArrayList<>();
        listChunkTags.add(new ChunkTag("containsTypographicApostrophe"));
        atr.setChunkTags(listChunkTags);
      }
      tokenReadings.add(atr);
      pos += word.length();
    }
    return tokenReadings;
  }

  private List<AnalyzedToken> tagWord(String word, String originalWord) {
    final List<AnalyzedToken> l = new ArrayList<>();
    final String lowerWord = word.toLowerCase(locale);
    final boolean isLowercase = word.equals(lowerWord);
    final boolean isMixedCase = StringTools.isMixedCase(word);
    final boolean isAllUpper = StringTools.isAllUppercase(word);
    List<AnalyzedToken> taggerTokens = asAnalyzedTokenListForTaggedWords(originalWord, getWordTagger().tag(word));
    // normal case:
    addTokens(taggerTokens, l);
    // tag non-lowercase (alluppercase or startuppercase), but not mixedcase
    // word with lowercase word tags:
    if (!isLowercase && !isMixedCase) {
      List<AnalyzedToken> lowerTaggerTokens = asAnalyzedTokenListForTaggedWords(originalWord,
          getWordTagger().tag(lowerWord));
      addTokens(lowerTaggerTokens, l);
    }
    // tag all-uppercase proper nouns (ex. FRANCE)
    if (l.isEmpty() && isAllUpper) {
      final String firstUpper = StringTools.uppercaseFirstChar(lowerWord);
      List<AnalyzedToken> firstupperTaggerTokens = asAnalyzedTokenListForTaggedWords(originalWord,
          getWordTagger().tag(firstUpper));
      addTokens(firstupperTaggerTokens, l);
    }
    // additional tagging with prefixes removed: && !isMixedCase
    if (l.isEmpty()) {
      addTokens(additionalTags(word), l);
    }
    return l;
  }

  @Nullable
  protected List<AnalyzedToken> additionalTags(String word) {
    List<AnalyzedToken> additionalTaggedTokens = new ArrayList<>();
    // Any well-formed verb with prefixes is tagged as a verb copying the original tags
    Matcher matcher = PREFIXES_FOR_VERBS.matcher(word);
    if (matcher.matches()) {
      final String possibleVerb = matcher.group(2).toLowerCase();
      List<AnalyzedToken> taggerTokens = asAnalyzedTokenListForTaggedWords(word, getWordTagger().tag(possibleVerb));
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
      if (!additionalTaggedTokens.isEmpty()) {
        return additionalTaggedTokens;
      }
    }
    // Any well-formed verb with prefixes is tagged as a verb copying the original tags
    matcher = PREFIXES_FOR_NOUN_ADJ.matcher(word);
    if (matcher.matches()) {
      final String possibleNoun = matcher.group(2).toLowerCase();
      List<AnalyzedToken> taggerTokens = asAnalyzedTokenListForTaggedWords(word, getWordTagger().tag(possibleNoun));
      for (AnalyzedToken taggerToken : taggerTokens) {
        final String posTag = taggerToken.getPOSTag();
        if (posTag != null) {
          final Matcher m = NOUN_ADJ.matcher(posTag);
          if (m.matches()) {
            String lemma = matcher.group(1).toLowerCase().concat(taggerToken.getLemma());
            additionalTaggedTokens.add(new AnalyzedToken(word, posTag, lemma));
          }
        }
      }
      if (!additionalTaggedTokens.isEmpty()) {
        return additionalTaggedTokens;
      }
    }
    matcher = PREFIXES_NOUN_ADJ.matcher(word);
    if (matcher.matches()) {
      String possibleNoun = matcher.group(2).toLowerCase();
      List<AnalyzedToken> taggerTokens = asAnalyzedTokenListForTaggedWords(word, getWordTagger().tag(possibleNoun));
      for (AnalyzedToken taggerToken : taggerTokens) {
        final String posTag = taggerToken.getPOSTag();
        if (posTag != null) {
          final Matcher m = NOUN_ADJ.matcher(posTag);
          if (m.matches()) {
            String lemma = matcher.group(1).toLowerCase().concat(taggerToken.getLemma());
            additionalTaggedTokens.add(new AnalyzedToken(word, posTag, lemma));
          }
        }
      }
      // with lower case
      if (additionalTaggedTokens.isEmpty()) {
        taggerTokens = asAnalyzedTokenListForTaggedWords(word, getWordTagger().tag(possibleNoun.toLowerCase()));
        for (AnalyzedToken taggerToken : taggerTokens) {
          final String posTag = taggerToken.getPOSTag();
          if (posTag != null) {
            final Matcher m = NOUN_ADJ.matcher(posTag);
            if (m.matches()) {
              String lemma = matcher.group(1).toLowerCase().concat(taggerToken.getLemma());
              additionalTaggedTokens.add(new AnalyzedToken(word, posTag, lemma));
            }
          }
        }
      }
      return additionalTaggedTokens;
    }
    return null;
  }

  private void addTokens(final List<AnalyzedToken> taggedTokens, final List<AnalyzedToken> l) {
    if (taggedTokens != null) {
      l.addAll(taggedTokens);
    }
  }
}
