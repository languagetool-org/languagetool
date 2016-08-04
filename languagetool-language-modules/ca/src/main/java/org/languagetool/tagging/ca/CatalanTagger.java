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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import morfologik.stemming.DictionaryLookup;
import morfologik.stemming.IStemmer;

import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.chunking.ChunkTag;
import org.languagetool.tagging.BaseTagger;
import org.languagetool.tools.StringTools;

/**
 * Catalan Tagger
 *
 * Based on FreeLing tagger dictionary
 *
 * @author Jaume Ortolà 
 */
public class CatalanTagger extends BaseTagger {

  private static final Pattern ADJ_PART_FS = Pattern.compile("VMP00SF.|A[QO].[FC][SN].");
  private static final Pattern VERB = Pattern.compile("V.+");
  //private static final Pattern NOUN = Pattern.compile("NC.+");

  private static final Pattern PREFIXES_FOR_VERBS = Pattern.compile("(auto)(.*[aeiouàéèíòóïü].+[aeiouàéèíòóïü].*)",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);

  @Override
  public String getManualAdditionsFileName() {
    return "/ca/manual-tagger.txt";
  }

  public CatalanTagger() {
    super("/ca/catalan.dict",  new Locale("ca"), false);
  }
  
  @Override
  public String getManualRemovalsFileName() {
    return "/ca/removed-tagger.txt";
  }
  
  @Override
  public boolean overwriteWithManualTagger(){
    return false;
  }

  @Override
  public List<AnalyzedTokenReadings> tag(final List<String> sentenceTokens)
      throws IOException {

    final List<AnalyzedTokenReadings> tokenReadings = new ArrayList<>();
    int pos = 0;
    final IStemmer dictLookup = new DictionaryLookup(getDictionary());

    for (String word : sentenceTokens) {
      // This hack allows all rules and dictionary entries to work with
      // typewriter apostrophe
      boolean containsTypewriterApostrophe = false;
      if (word.length() > 1) {
        if (word.contains("'")) {
          containsTypewriterApostrophe = true;
        }
        word = word.replace("’", "'");
      }
      final List<AnalyzedToken> l = new ArrayList<>();
      final String lowerWord = word.toLowerCase(conversionLocale);
      final boolean isLowercase = word.equals(lowerWord);
      final boolean isMixedCase = StringTools.isMixedCase(word);
      List<AnalyzedToken> taggerTokens = asAnalyzedTokenListForTaggedWords(word, getWordTagger().tag(word));
      
      // normal case:
      addTokens(taggerTokens, l);
      // tag non-lowercase (alluppercase or startuppercase), but not mixedcase
      // word with lowercase word tags:
      if (!isLowercase && !isMixedCase) {
        List<AnalyzedToken> lowerTaggerTokens = asAnalyzedTokenListForTaggedWords(word, getWordTagger().tag(lowerWord));
        addTokens(lowerTaggerTokens, l);
      }

      // additional tagging with prefixes
      if (l.isEmpty() && !isMixedCase) {
        addTokens(additionalTags(word, dictLookup), l);
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

      tokenReadings.add(atr);
      pos += word.length();
    }

    return tokenReadings;
  }

  @Nullable
  protected List<AnalyzedToken> additionalTags(String word, IStemmer stemmer) {
    final IStemmer dictLookup = new DictionaryLookup(getDictionary());
    List<AnalyzedToken> additionalTaggedTokens = new ArrayList<>();
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
    //Any well-formed verb with prefixes is tagged as a verb copying the original tags
    Matcher matcher=PREFIXES_FOR_VERBS.matcher(word);
    if (matcher.matches()) {
      final String possibleVerb = matcher.group(2).toLowerCase();
      List<AnalyzedToken> taggerTokens;
      taggerTokens = asAnalyzedTokenList(possibleVerb, dictLookup.lookup(possibleVerb));
      for (AnalyzedToken taggerToken : taggerTokens ) {
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
    // Any well-formed noun with prefix ex- is tagged as a noun copying the original tags
    /*if (word.startsWith("ex")) {
      final String lowerWord = word.toLowerCase(conversionLocale);
      final String possibleNoun = lowerWord.replaceAll("^ex(.+)$", "$1");
      List<AnalyzedToken> taggerTokens;
      taggerTokens = asAnalyzedTokenList(possibleNoun,dictLookup.lookup(possibleNoun));
      for (AnalyzedToken taggerToken : taggerTokens) {
        final String posTag = taggerToken.getPOSTag();
        if (posTag != null) {
          final Matcher m = NOUN.matcher(posTag);
          if (m.matches()) {
            String lemma = "ex".concat(taggerToken.getLemma());
            additionalTaggedTokens.add(new AnalyzedToken(word, posTag, lemma));
          }
        }
      }
      return additionalTaggedTokens;
    }*/
    // Interpret deprecated characters of "ela geminada"
    // U+013F LATIN CAPITAL LETTER L WITH MIDDLE DOT
    // U+0140 LATIN SMALL LETTER L WITH MIDDLE DOT
    if (word.contains("\u0140") || word.contains("\u013f")) {
      final String lowerWord = word.toLowerCase(conversionLocale);
      final String possibleWord = lowerWord.replaceAll("\u0140", "l·");
      List<AnalyzedToken> taggerTokens = asAnalyzedTokenList(word, dictLookup.lookup(possibleWord));
      return taggerTokens;
    }
    return null;
  }

  private void addTokens(final List<AnalyzedToken> taggedTokens, final List<AnalyzedToken> l) {
    if (taggedTokens != null) {
      for (AnalyzedToken at : taggedTokens) {
        l.add(at);
      }
    }
  }

}
