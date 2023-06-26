/* LanguageTool, a natural language style checker
 * Copyright (C) 2022 Sohaib Afifi, Taha Zerrouki
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
package org.languagetool.rules.ar.filters;

import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.language.Arabic;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.RuleFilter;
import org.languagetool.synthesis.ar.ArabicSynthesizer;
import org.languagetool.tagging.ar.ArabicTagManager;
import org.languagetool.tagging.ar.ArabicTagger;
import org.languagetool.tools.ArabicNumbersWords;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.Arrays.asList;

/**
 * Filter that maps suggestion for numeric phrases.
 */
public class ArabicNumberPhraseFilter extends RuleFilter {

  private static final ArabicTagManager tagmanager = new ArabicTagManager();
  private final ArabicSynthesizer synthesizer = new ArabicSynthesizer(new Arabic());


  @Nullable
  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos, AnalyzedTokenReadings[] patternTokens) {

    // get the previous word
    String previousWord = arguments.getOrDefault("previous", "");
    // previous word index in token list
    int previousWordPos = getPreviousPos(arguments);

    // get the inflect mark
    String inflectArg = arguments.getOrDefault("inflect", "");
    // get the next  word as units
    String nextWord = arguments.getOrDefault("next", "");

    int nextWordPos = getNextPos(arguments, patternTokens.length);

    List<String> numWordTokens = new ArrayList<>();
    /// get all numeric tokens
    int startPos = (previousWordPos > 0) ? previousWordPos + 1 : 0;

    int endPos = (nextWordPos > 0) ? Integer.min(nextWordPos, patternTokens.length) : patternTokens.length + nextWordPos;

    for (int i = startPos; i < endPos; i++) {
      numWordTokens.add(patternTokens[i].getToken().trim());
    }

    String numPhrase = String.join(" ", numWordTokens);
    // extract features from previous
    boolean feminine = false;
    boolean attached = false;
    String inflection = getInflectedCase(patternTokens, previousWordPos, inflectArg);
    List<String> suggestionList;
    if (nextWord.isEmpty()) {
      suggestionList = prepareSuggestion(numPhrase, previousWord, null, feminine, attached, inflection);
    } else {
      AnalyzedTokenReadings nextWordToken = null;
      if (endPos > 0 && endPos < patternTokens.length) {
        nextWordToken = patternTokens[endPos];
      }
      suggestionList = prepareSuggestionWithUnits(numPhrase, previousWord, nextWordToken, feminine, attached, inflection);
    }
    RuleMatch newMatch = new RuleMatch(match.getRule(), match.getSentence(), match.getFromPos(), match.getToPos(), match.getMessage(), match.getShortMessage());

    if (!suggestionList.isEmpty()) {
      for (String sug : suggestionList) {
        newMatch.addSuggestedReplacement(sug);
      }
    }
    return newMatch;
  }

  // extract inflection case
  private static String getInflectedCase(AnalyzedTokenReadings[] patternTokens, int previousPos, String inflect) {
    if (!Objects.equals(inflect, "")) {
      return inflect;
    }
    // if the previous is Jar

    if (previousPos >= 0 && previousPos < patternTokens.length) {
      AnalyzedTokenReadings previousToken = patternTokens[previousPos];
      for (AnalyzedToken tk : patternTokens[previousPos]) {
        if (tk.getPOSTag() != null && tk.getPOSTag().startsWith("PR")) {
          return "jar";
        }
      }

    }
    String firstWord = patternTokens[previousPos + 1].getToken();
    if (firstWord.startsWith("ب")
      || firstWord.startsWith("ل")
      || firstWord.startsWith("ك")
    ) {
      return "jar";
    }
    return "";
  }

  // extract inflection case
  private static boolean getFemininCase(AnalyzedTokenReadings[] patternTokens, int nextPos) {
    // if the previous is Jar
    for (AnalyzedToken tk : patternTokens[nextPos]) {
      if (tagmanager.isFeminin(tk.getPOSTag())) {
        return true;
      }
    }
    return false;
  }

  /* prepare suggestion for given phrases */
  public static List<String> prepareSuggestion(String numPhrase, String previousWord, AnalyzedTokenReadings nextWord, boolean feminin, boolean attached, String inflection) {

    List<String> tmpsuggestionList = ArabicNumbersWords.getSuggestionsNumericPhrase(numPhrase, feminin, attached, inflection);
    List<String> suggestionList = new ArrayList<>();
    if (!tmpsuggestionList.isEmpty()) {
      for (String sug : tmpsuggestionList)
        if (!previousWord.isEmpty()) {
          suggestionList.add(previousWord + " " + sug);
        }
    }
    return suggestionList;
  }

  /* prepare suggestion for given phrases */
  public List<String> prepareSuggestionWithUnits(String numPhrase, String previousWord, AnalyzedTokenReadings nextWord, boolean feminin, boolean attached, String inflection) {

    String defaultUnit = "دينار";

    List<Map<String, String>> tmpsuggestionList = ArabicNumbersWords.getSuggestionsNumericPhraseWithUnits(numPhrase, defaultUnit, feminin, attached, inflection);
    List<String> suggestionList = new ArrayList<>();
    if (!tmpsuggestionList.isEmpty()) {
      for (Map<String, String> sugMap : tmpsuggestionList) {
        String sug = sugMap.get("phrase");
        List<String> inflectedUnitList = inflectUnit(nextWord, sugMap);
        for (String unit : inflectedUnitList) {
          StringBuilder tmp = new StringBuilder();
          if (!previousWord.isEmpty()) {
            tmp.append(previousWord + " ");
          }
          tmp.append(sug);
          if (unit != null && !unit.isEmpty()) {
            tmp.append(" " + unit);
          }
          suggestionList.add(tmp.toString());
        }
      }
    }

    return suggestionList;
  }

  /* get suitable forms for the given unit */
  private List<String> inflectUnit(AnalyzedTokenReadings unit, Map<String, String> sugMap) {
    if (unit == null) {
      return null;
    } else {
      String inflection = sugMap.getOrDefault("unitInflection", "");
      String number = sugMap.getOrDefault("unitNumber", "");
      String inflected = unit.getToken() + "{" + inflection + "+" + number + "}";
      List<String> tmpList = new ArrayList<>();
      List<String> inflectedList = new ArrayList<>();
      for (AnalyzedToken tk : unit) {
        String postag = tk.getPOSTag();
        if (tagmanager.isNoun(postag) && !tagmanager.isDefinite(postag) && !tagmanager.hasPronoun(postag)) {
          // add inflection flag
          if (inflection.equals("jar")) {
            postag = tagmanager.setMajrour(postag);
          } else if (inflection.equals("raf3")) {
            postag = tagmanager.setMarfou3(postag);
          } else if (inflection.equals("nasb")) {
            postag = tagmanager.setMansoub(postag);
          } else {
            postag = tagmanager.setMarfou3(postag);
          }

          // add number flag
          if (number.equals("one")) {
            postag = tagmanager.setSingle(postag);
          } else if (number.equals("two")) {
            postag = tagmanager.setDual(postag);
          } else if (number.equals("plural")) {
            postag = tagmanager.setPlural(postag);

          } else {
            postag = tagmanager.setSingle(postag);

          }
          //  add Tanwin
          if (number.equals("one") && inflection.equals("nasb")) {
            postag = tagmanager.setTanwin(postag);
          }

          //
          // for each potag generate a new token
          if (!tmpList.contains(postag)) {
            tmpList.add(postag);
            List<String> syhthesizedList = asList(synthesizer.synthesize(tk, postag));
            if (syhthesizedList != null && !syhthesizedList.isEmpty()) {
              inflectedList.addAll(syhthesizedList);
            }
          }
        }
      }
      return inflectedList;
    }
  }

  private static int getPreviousPos(Map<String, String> args) {
    int previousWordPos = 0;
    if (args.get("previousPos") != null)
      try {
        if (args.get("previousPos") != null)
          previousWordPos = Integer.valueOf(args.get("previousPos")) - 1;
      } catch (NumberFormatException e) {
        previousWordPos = -1;
      }
    return previousWordPos;

  }

  private static int getNextPos(Map<String, String> args, int size) {
    int nextPos = 0;
    try {
      nextPos = Integer.parseInt(args.getOrDefault("nextPos", "0"));
      // the next token is index with a negative offset
      if (nextPos < 0) {
        nextPos = size + nextPos;
      }
    } catch (NumberFormatException e) {
      nextPos = 0;
    }
    return nextPos;

  }
}
