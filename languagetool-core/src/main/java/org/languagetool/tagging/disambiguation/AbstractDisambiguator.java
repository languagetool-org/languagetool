/* LanguageTool, a natural language style checker
 * Copyright (C) 2017 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.tagging.disambiguation;

import org.languagetool.*;
import org.languagetool.rules.spelling.SpellingCheckRule;

import org.jetbrains.annotations.Nullable;
import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Abstract Disambiguator class to provide default (empty) implementation
 * for {@link Disambiguator#preDisambiguate(AnalyzedSentence)}.
 *
 * @since 3.7
 */
public abstract class AbstractDisambiguator implements Disambiguator {

  @Override
  public AnalyzedSentence preDisambiguate(AnalyzedSentence input) {
    return input;
  }

  private SpellingCheckRule spellingRule = null;
  private List<String> commonWords = new ArrayList<>();
  private final List<String> commonEnglishWords = Arrays.asList("the", "to", "of", "and", "in", "a", "was", "is",
    "this", "i", "as", "for", "on", "with", "by", "that", "from", "at", "his", "an", "he", "are", "were", "don't",
    "which", "it", "or", "had", "has", "one", "but", "first", "very", "we", "not", "their", "any", "have", "who",
    "been", "her", "two", "get", "other", "after", "into", "they", "time", "all", "be", "me", "more", "must", "only",
    "when", "years", "most", "over", "used", "would", "you", "can", "during", "going", "good", "him", "new", "out",
    "she", "such", "where", "between", "made", "many", "some", "then", "there", "year", "about", "back", "became",
    "later", "part", "under", "well", "being", "including", "them", "through", "area", "both", "make", "name",
    "before", "called", "could", "fine", "people", "second", "until", "while", "will", "i'll", "against", "age",
    "city", "go", "may", "number", "population", "season", "several", "team", "these", "work", "born", "early",
    "family", "film", "now", "same", "series", "so", "use", "what", "album", "based", "four", "it's", "life",
    "released", "since", "state", "began", "century", "each", "end", "following", "found", "game", "high", "located",
    "town", "i've", "around", "because", "can't", "come", "day", "did", "didn't", "government", "group", "here",
    "home", "large", "like", "love", "music", "my", "named", "no", "often", "system", "that's", "three", "too", "up",
    "us", "won", "yes", "you're", "along", "built", "career", "former", "if", "include", "left", "line", "local",
    "long", "off", "own", "place", "small", "still", "those", "took", "band", "due", "form", "held", "its", "last",
    "major", "member", "members", "much", "don", "didn", "doesn", "wasn", "weren", "'s", "'t", "'ll");

  private String[] tagsToIgnore = new String[0];

  protected void initExtraSpellingRule(String languageCode, String[] tagsToBeIgnored) {
    Language lang = null;
    try {
      lang = Languages.getLanguageForShortCode(languageCode);
    } catch (IllegalArgumentException e) {
      // The language is not available; ignoring words will not be available
    }
    if (lang != null) {
      spellingRule = lang.getDefaultSpellingRule();
      if (tagsToBeIgnored != null) {
        tagsToIgnore = tagsToBeIgnored;
      }
      if (languageCode.startsWith("en")) {
        commonWords = commonEnglishWords;
      }
    }
  }

  /*
   * Ignore the spelling of words from a different language, e.g. English, at least two consecutive words
   * The speller is initiated by initExtraSpellingRule().
   *
   * @Since 6.4
   */
  protected AnalyzedSentence ignoreSpellingWithExtraSpellingRule(AnalyzedSentence input,
                                                                 @Nullable JLanguageTool.CheckCancelledCallback checkCanceled) throws IOException {
    if (spellingRule == null) {
      return input;
    }
    AnalyzedTokenReadings[] anTokens = input.getTokens();
    AnalyzedTokenReadings[] output = anTokens;
    boolean prevIsEnglish = false;
    boolean isEnglish;
    Integer skippedTokens = 1;
    for (int i = 1; i < anTokens.length; i++) {
      String word = output[i].getToken();
      if (word.length() < 1 || StringTools.isWhitespace(word) || StringTools.isNotWordString(word)) {
        skippedTokens++;
        continue;
      }
      String lcword = word.toLowerCase().replaceAll("’", "'");
      // avoid checking with the speller as many words as possible
      boolean currentIsEnglish = output[i].hasAnyPartialPosTag(tagsToIgnore) || commonWords.contains(lcword);
      if (!prevIsEnglish && (output[i].isIgnoredBySpeller() || output[i].isTagged() || currentIsEnglish)) {
        prevIsEnglish = currentIsEnglish;
        skippedTokens = 1;
        continue;
      }
      isEnglish = currentIsEnglish || !spellingRule.isMisspelled(word);
      if (isEnglish && prevIsEnglish && i - skippedTokens > 0) {
        output[i].ignoreSpelling();
        output[i - skippedTokens].ignoreSpelling();
      }
      skippedTokens = 1;
      prevIsEnglish = isEnglish;
    }
    return new AnalyzedSentence(output);
  }

}
