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
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.languagetool.rules.ca.ApostophationHelper.getPrepositionAndDeterminer;

public class ConvertToGenderAndNumberFilter extends RuleFilter {

  private Pattern splitGenderNumber = Pattern.compile("(N.|A..|V.P..|D..|PX.)(.)(.)(.*)");

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
    String desiredGender = getOptional("gender", arguments, "");
    String desiredNumber = getOptional("number", arguments, "");
    String lemmaSelect = getRequired("lemmaSelect", arguments);
    boolean keepOriginal = getOptional("keepOriginal", arguments, "false").equalsIgnoreCase("true");

    AnalyzedToken atrNoun = tokens[posWord].readingWithTagRegex(lemmaSelect);
    String[] splitPostag = splitGenderAndNumber(atrNoun);
    if (desiredGender.isEmpty()) {
      desiredGender = splitPostag[2];
    }
    if (desiredNumber.isEmpty()) {
      desiredNumber = splitPostag[3];
    }
    StringBuilder suggestionBuilder = new StringBuilder();
    if (!keepOriginal) {
      String s = synthesizeWithGenderAndNumber(atrNoun, splitPostag, desiredGender, desiredNumber, synth);
      if (s.isEmpty()) {
        return null;
      }
      suggestionBuilder.append(s);
    } else {
      suggestionBuilder.append(atrNoun.getToken());
    }
    //backwards
    boolean stop = false;
    int i = posWord;
    int startPos = posWord;
    int endPos = posWord;
    String prepositionToAdd = "";
    boolean addDeterminer = false;
    while (!stop && i > 1) {
      i--;
      AnalyzedToken atr = tokens[i].readingWithTagRegex(splitGenderNumber);
      if (atr != null) {
        if (atr.getPOSTag().startsWith("DA")) {
          addDeterminer = true;
        } else {
          String s = synthesizeWithGenderAndNumber(atr, splitGenderAndNumber(atr), desiredGender, desiredNumber, synth);
          suggestionBuilder.insert(0, s + " ");
        }
        startPos = i;
      } else if (tokens[i].hasPosTag("SPS00")) {
        if (addDeterminer) {
          String preposition = tokens[i].readingWithTagRegex("SPS00").getLemma().toLowerCase();
          if (preposition.equals("a") || preposition.equals("de") || preposition.equals("per")) {
            prepositionToAdd = preposition;
            startPos = i;
          }
        }
        stop = true;
      } else if (tokens[i].hasPosTag("RG")) {
        suggestionBuilder.insert(0, tokens[i].getToken() + " ");
        startPos = i;
      } else {
        stop = true;
      }
    }
    // forwards
    stop = false;
    i = posWord;
    while (!stop && i < tokens.length) {
      i++;
      AnalyzedToken atr = tokens[i].readingWithTagRegex(splitGenderNumber);
      if (atr != null) {
        String s = synthesizeWithGenderAndNumber(atr, splitGenderAndNumber(atr), desiredGender, desiredNumber, synth);
        suggestionBuilder.insert(0, s + " ");
        endPos = i;
      } else if (tokens[i].hasPosTag("RG")) {
        suggestionBuilder.insert(0, tokens[i].getToken() + " ");
        endPos = i;
      } else {
        stop = true;
      }
    }
    if (addDeterminer) {
      suggestionBuilder.insert(0, getPrepositionAndDeterminer(suggestionBuilder.toString(),desiredGender + desiredNumber, prepositionToAdd));
    } else if (!prepositionToAdd.isEmpty()) {
      suggestionBuilder.insert(0, prepositionToAdd + " ");
    }
    String suggestion = StringTools.preserveCase(suggestionBuilder.toString(), tokens[startPos].getToken());
    if (endPos == posWord && startPos == posWord && tokens[posWord].getToken().equals(suggestion)) {
      return null;
    }
    RuleMatch ruleMatch = new RuleMatch(match.getRule(), match.getSentence(), tokens[startPos].getStartPos(),
      tokens[endPos].getEndPos(), match.getMessage(), match.getShortMessage());
    ruleMatch.setType(match.getType());
    ruleMatch.setSuggestedReplacement(suggestion);
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

}
