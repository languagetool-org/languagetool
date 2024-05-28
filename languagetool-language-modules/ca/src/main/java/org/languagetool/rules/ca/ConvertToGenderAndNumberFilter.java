/* LanguageTool, a natural language style checker
 * Copyright (C) 2024 Jaume Ortolà
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
package org.languagetool.rules.ca;

import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.RuleFilter;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.languagetool.rules.ca.ApostophationHelper.getPrepositionAndDeterminer;

public class ConvertToGenderAndNumberFilter extends RuleFilter {

  private Pattern splitGenderNumber = Pattern.compile("(N.|A..|V.P..|D..|PX.)(.)(.)(.*)");
  private Pattern splitGenderNumberNoNoun = Pattern.compile("(A..|V.P..|D..|PX.)(.)(.)(.*)");

  @Nullable
  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos,
                                   AnalyzedTokenReadings[] patternTokens, List<Integer> tokenPositions) throws IOException {
    AnalyzedTokenReadings[] tokens = match.getSentence().getTokensWithoutWhitespace();
    Synthesizer synth = getSynthesizerFromRuleMatch(match);
    int posWord = 0;
    while (posWord < tokens.length
      && (tokens[posWord].getStartPos() < match.getFromPos() || tokens[posWord].isSentenceStart())) {
      posWord++;
    }
    String desiredGenderStr = getOptional("gender", arguments, "");
    String desiredNumberStr = getOptional("number", arguments, "");
    String lemmaSelect = getRequired("lemmaSelect", arguments);
    boolean keepOriginal = getOptional("keepOriginal", arguments, "false").equalsIgnoreCase("true");

    AnalyzedToken atrNoun = tokens[posWord].readingWithTagRegex(lemmaSelect);
    String[] splitPostag = splitGenderAndNumber(atrNoun);
    if (desiredGenderStr.isEmpty()) {
      desiredGenderStr = splitPostag[2];
    }
    if (desiredNumberStr.isEmpty()) {
      desiredNumberStr = splitPostag[3];
    }
    int startPos = posWord;
    int endPos = posWord;
    List<String> suggestions = new ArrayList<>();

    for (char genderCh : desiredGenderStr.toCharArray()) {
      for (char numberCh : desiredNumberStr.toCharArray()) {
        String desiredGender = String.valueOf(genderCh);
        String desiredNumber = String.valueOf(numberCh);
        StringBuilder suggestionBuilder = new StringBuilder();
        boolean ignoreThisSuggestion = false;
        if (!keepOriginal) {
          String s = synthesizeWithGenderAndNumber(atrNoun, splitPostag, desiredGender, desiredNumber, synth);
          if (s.isEmpty()) {
            ignoreThisSuggestion=true;
          }
          suggestionBuilder.append(s);
        } else {
          suggestionBuilder.append(atrNoun.getToken());
        }
        //backwards
        boolean stop = false;
        int i = posWord;
        String prepositionToAdd = "";
        boolean addDeterminer = false;
        StringBuilder conditionalAddedString = new StringBuilder();
        while (!stop && i > 1) {
          i--;
          AnalyzedToken atr = getReadingWithPriority(tokens[i]);
          if (atr != null) {
            if (atr.getPOSTag().startsWith("DA")) {
              addDeterminer = true;
              startPos = i;
            } else {
              if (!addDeterminer) {
                String s = synthesizeWithGenderAndNumber(atr, splitGenderAndNumber(atr), desiredGender, desiredNumber, synth);
                if (s.isEmpty()) {
                  ignoreThisSuggestion=true;
                }
                if (s.equals("bo")) {
                  s = "bon";
                }
                suggestionBuilder.insert(0, conditionalAddedString);
                conditionalAddedString.setLength(0);
                if (tokens[i+1].isWhitespaceBefore()) {
                  suggestionBuilder.insert(0, " ");
                }
                suggestionBuilder.insert(0, s);
                startPos = i;
                if (atr.getPOSTag().startsWith("D")) {
                  stop = true;
                }
              } else {
                stop = true;
              }
            }
          } else if (tokens[i].hasPosTag("SPS00") || tokens[i].hasPosTag("LOC_PREP")) {
            if (addDeterminer) {
              String preposition = tokens[i].getToken().toLowerCase();
              if (preposition.equals("pe")) {
                preposition = "per";
              }
              if (preposition.equals("d'")) {
                preposition = "de";
              }
              if (preposition.equals("a") || preposition.equals("de") || preposition.equals("per")) {
                prepositionToAdd = preposition;
                startPos = i;
              }
            }
            stop = true;
          } else if (tokens[i].hasPosTag("RG") || tokens[i].hasPosTag("CC")) {
            conditionalAddedString.insert(0, tokens[i].getToken() + " ");
          } else if (tokens[i].hasPosTag("_PUNCT_CONT")) {
            conditionalAddedString.insert(0, tokens[i].getToken() + " ");
          } else {
            stop = true;
          }
        }
        // forwards
        stop = false;
        i = posWord;
        conditionalAddedString.setLength(0);
        while (!stop && i < tokens.length - 1) {
          i++;
          AnalyzedToken atr = getReadingWithPriority(tokens[i]);
          if (atr != null) {
            String s = synthesizeWithGenderAndNumber(atr, splitGenderAndNumber(atr), desiredGender, desiredNumber, synth);
            if (s.isEmpty()) {
              ignoreThisSuggestion=true;
            }
            suggestionBuilder.append(conditionalAddedString);
            conditionalAddedString.setLength(0);
            suggestionBuilder.append(" " + s);
            endPos = i;
          } else if (tokens[i].hasPosTag("RG") || tokens[i].hasPosTag("CC")) {
            conditionalAddedString.append(" " + tokens[i].getToken());
          } else if (tokens[i].hasPosTag("_PUNCT_CONT")) {
            conditionalAddedString.append(tokens[i].getToken());
          } else {
            stop = true;
          }
        }
        if (addDeterminer) {
          suggestionBuilder.insert(0, getPrepositionAndDeterminer(suggestionBuilder.toString(), desiredGender + desiredNumber, prepositionToAdd));
        } else if (!prepositionToAdd.isEmpty()) {
          suggestionBuilder.insert(0, prepositionToAdd + " ");
        }
        String suggestion = StringTools.preserveCase(suggestionBuilder.toString(), tokens[startPos].getToken());
        if (endPos == posWord && startPos == posWord && tokens[posWord].getToken().equals(suggestion)) {
          continue;
        }
        //TODO: una d'aquests vessants; una dels vessants
        if (!ignoreThisSuggestion) {
          suggestions.add(suggestion);
        }
      }
    }
    if (suggestions.isEmpty()) {
      return null;
    }
    RuleMatch ruleMatch = new RuleMatch(match.getRule(), match.getSentence(), tokens[startPos].getStartPos(),
      tokens[endPos].getEndPos(), match.getMessage(), match.getShortMessage());
    ruleMatch.setType(match.getType());
    ruleMatch.setSuggestedReplacements(suggestions);
    return ruleMatch;
  }

  private String[] splitGenderAndNumber(AnalyzedToken atr) {
    String[] results = new String[4];
    Matcher matcherSplit = splitGenderNumber.matcher(atr.getPOSTag());
    if (matcherSplit.matches()) {
      results[0] = matcherSplit.group(1);
      results[1] = matcherSplit.group(4);
      String gender = matcherSplit.group(2);
      String number = matcherSplit.group(3);
      if (results[0].startsWith("V")) {
        results[2] = number;
        results[3] = gender;
      } else {
        results[2] = gender;
        results[3] = number;
      }
      return results;
    }
    return null;
  }

  private String synthesizeWithGenderAndNumber(AnalyzedToken atr, String[] splitPostag, String gender, String number,
                                               Synthesizer synth) throws IOException {
    if (splitPostag[0].startsWith("V")) {
      String keepGender = gender;
      gender = number;
      number = keepGender;
    }
    String addGender = "C";
    if (splitPostag[0].startsWith("DA")) {
      addGender = "";
    }
    String[] synhtesized = synth.synthesize(atr, splitPostag[0] + "[" + gender + addGender + "]" + "[" + number + "N" +
        "]" + splitPostag[1], true);
    if (synhtesized.length > 0) {
      return synhtesized[0];
    } else {
      return "";
    }
  }

  private AnalyzedToken getReadingWithPriority(AnalyzedTokenReadings token) {
    AnalyzedToken atr = token.readingWithTagRegex(splitGenderNumberNoNoun);
    if (atr != null) {
      return atr;
    }
    atr = token.readingWithTagRegex(splitGenderNumber);
    return atr;
  }

}
