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
package org.languagetool.tagging.pt;

import morfologik.stemming.DictionaryLookup;
import morfologik.stemming.IStemmer;
import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.chunking.ChunkTag;
import org.languagetool.tagging.BaseTagger;
import org.languagetool.tools.StringTools;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Portuguese Part-of-speech tagger.
 * Based on English tagger.
 *
 * @author Marcin Milkowski
 * Base PT file created by Jaume Ortolà
 * Extended by Tiago F. Santos
 * 
 */
public class PortugueseTagger extends BaseTagger {

  private static final Pattern ADJ_PART_FS = Pattern.compile("V.P..SF.|A[QO].[FC][SN].");
  private static final Pattern VERB = Pattern.compile("V.+");
  private static final Pattern PREFIXES_FOR_VERBS = Pattern.compile("(auto|re)(...+)",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  private static final String ORDINAL_SUFFIX_MASC = "oºᵒ";
  private static final String ORDINAL_SUFFIX_FEM = "aªᵃ";
  private static final String ORDINAL_SUFFIX_PL = "sˢ";

  private static final String ORDINAL_SUFFIXES = String.format("[%s%s][%s]?",
    ORDINAL_SUFFIX_MASC, ORDINAL_SUFFIX_FEM, ORDINAL_SUFFIX_PL);
  private static final Pattern ORDINAL_PATTERN = Pattern.compile("^\\d+[\\d,.]*\\.?" + ORDINAL_SUFFIXES + "$");
  private static final Pattern ORDINAL_MASC_SG = Pattern.compile(String.format("[%s]$", ORDINAL_SUFFIX_MASC));
  private static final Pattern ORDINAL_FEM_SG = Pattern.compile(String.format("[%s]$", ORDINAL_SUFFIX_FEM));
  private static final Pattern ORDINAL_MASC_PL = Pattern.compile(String.format("[%s][%s]$",
    ORDINAL_SUFFIX_MASC, ORDINAL_SUFFIX_PL));
  private static final Pattern ORDINAL_FEM_PL = Pattern.compile(String.format("[%s][%s]$",
    ORDINAL_SUFFIX_FEM, ORDINAL_SUFFIX_PL));

  private static final Pattern PERCENT_PATTERN = Pattern.compile("−?\\d+[\\d,.]*%");
  private static final Pattern DEGREE_PATTERN = Pattern.compile("−?\\d+[\\d,.]*°");

  public PortugueseTagger() {
    super("/pt/portuguese.dict", new Locale("pt"));
  }

  private List<AnalyzedToken> tagNumberExpressions(String word) {
    if (isOrdinal(word)) {
      return buildOrdinalTokens(word);
    }
    if (isDegree(word)) {
      return Collections.singletonList(buildMascPlNoun(word));
    }
    if (isPercent(word)) {
      // TODO: remove "PERCENTAGES" disambiguator rule
      return Collections.singletonList(buildMascPlNoun(word));
    }
    return Collections.emptyList();
  }

  private AnalyzedToken buildMascPlNoun(String word) {
    return new AnalyzedToken(word, "NCMP000", word);
  }

  private List<AnalyzedToken> buildOrdinalTokens(String word) {
    List<AnalyzedToken> resultTokens = new ArrayList<>(Collections.emptyList());
    // The lemma has the ordinal indicator form, this is arbitrary
    // It also keeps the ".", since we need to keep those forms distinct
    String lemma = word.replaceAll(ORDINAL_SUFFIXES, "º");
    String numberGenderTags = "";
    if (ORDINAL_MASC_SG.matcher(word).find()) {
      numberGenderTags = "MS";
    }
    if (ORDINAL_FEM_SG.matcher(word).find()) {
      numberGenderTags = "FS";
    }
    if (ORDINAL_MASC_PL.matcher(word).find()) {
      numberGenderTags = "MP";
    }
    if (ORDINAL_FEM_PL.matcher(word).find()) {
      numberGenderTags = "FP";
    }
    String nounTag = String.format("NC%s000", numberGenderTags);
    String adjTag = String.format("AO0%s0", numberGenderTags);
    AnalyzedToken nounToken = new AnalyzedToken(word, nounTag, lemma);
    AnalyzedToken adjToken = new AnalyzedToken(word, adjTag, lemma);
    resultTokens.add(nounToken);
    resultTokens.add(adjToken);
    return resultTokens;
  }

  private boolean isOrdinal(String word) {
    Matcher matcher = ORDINAL_PATTERN.matcher(word);
    return matcher.matches();
  }

  private boolean isPercent(String word) {
    Matcher matcher = PERCENT_PATTERN.matcher(word);
    return matcher.matches();
  }

  private boolean isDegree(String word) {
    Matcher matcher = DEGREE_PATTERN.matcher(word);
    return matcher.matches();
  }

  @Override
  public List<AnalyzedTokenReadings> tag(List<String> sentenceTokens) {

    List<AnalyzedTokenReadings> tokenReadings = new ArrayList<>();
    int pos = 0;
    IStemmer dictLookup = new DictionaryLookup(getDictionary());

    for (String word : sentenceTokens) {
      // This hack allows all rules and dictionary entries to work with
      // typewriter apostrophe
      boolean containsTypewriterApostrophe = false;
      if (word.length() > 1) {
        if (word.contains("'")) {
          containsTypewriterApostrophe = true;
        }
        word = word.replace('’', '\'');
      }
      List<AnalyzedToken> l = new ArrayList<>();
      String lowerWord = word.toLowerCase(locale);
      boolean isLowercase = word.equals(lowerWord);
      boolean isMixedCase;
      // if the word contains a hyphen, it is only mixed case if at least one element is mixed case
      if (word.contains("-")) {
        isMixedCase = false;
        String[] parts = word.split("-");
        for (String part : parts) {
          if (StringTools.isMixedCase(part)) {
            isMixedCase = true;
            break;
          }
        }
      // No hyphens found, just do a regular isMixedCase check
      } else {
        isMixedCase = StringTools.isMixedCase(word);
      }

      List<AnalyzedToken> taggerTokens = asAnalyzedTokenListForTaggedWords(word, getWordTagger().tag(word));
      
      // normal case:
      addTokens(taggerTokens, l);
      // tag non-lowercase (alluppercase or startuppercase), but not mixedcase
      // word with lowercase word tags:
      if (!isLowercase && !isMixedCase) {
        List<AnalyzedToken> lowerTaggerTokens = asAnalyzedTokenListForTaggedWords(word, getWordTagger().tag(lowerWord));
        addTokens(lowerTaggerTokens, l);
      }

      // tag ordinals and percentages, i.e. expressions that include digits
      if (l.isEmpty()) {
        addTokens(tagNumberExpressions(word), l);
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
    IStemmer dictLookup = new DictionaryLookup(getDictionary());
    List<AnalyzedToken> additionalTaggedTokens = new ArrayList<>();
    //Any well-formed adverb with suffix -mente is tagged as an adverb of manner (RG)
    if (word.endsWith("mente")){
      String lowerWord = word.toLowerCase(locale);
      String possibleAdj = lowerWord.replaceAll("^(.+)mente$", "$1");
      List<AnalyzedToken> taggerTokens;
      taggerTokens = asAnalyzedTokenList(lowerWord, dictLookup.lookup(possibleAdj));
      for (AnalyzedToken taggerToken : taggerTokens ) {
        String posTag = taggerToken.getPOSTag();
        if (posTag != null) {
          Matcher m = ADJ_PART_FS.matcher(posTag);
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
      String possibleVerb = matcher.group(2).toLowerCase();
      List<AnalyzedToken> taggerTokens;
      taggerTokens = asAnalyzedTokenList(possibleVerb, dictLookup.lookup(possibleVerb));
      for (AnalyzedToken taggerToken : taggerTokens ) {
        String posTag = taggerToken.getPOSTag();
        if (posTag != null) {
          Matcher m = VERB.matcher(posTag);
          if (m.matches()) {
            String lemma = matcher.group(1).toLowerCase().concat(taggerToken.getLemma());
            additionalTaggedTokens.add(new AnalyzedToken(word, posTag, lemma));
          }
        }
      }
      return additionalTaggedTokens;
    }
    return null;
  }

  private void addTokens(List<AnalyzedToken> taggedTokens, List<AnalyzedToken> l) {
    if (taggedTokens != null) {
      l.addAll(taggedTokens);
    }
  }

}
