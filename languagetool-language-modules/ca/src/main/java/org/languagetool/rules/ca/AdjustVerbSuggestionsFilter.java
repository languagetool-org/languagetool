/* LanguageTool, a natural language style checker
 * Copyright (C) 2021 Daniel Naber (http://www.danielnaber.de)
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

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.PatternRule;
import org.languagetool.rules.patterns.RuleFilter;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.languagetool.rules.ca.PronomsFeblesHelper.*;

public class AdjustVerbSuggestionsFilter extends RuleFilter {

  private static Pattern de_apostrof = Pattern.compile("(d')[^aeiouh]");

  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos,
                                   AnalyzedTokenReadings[] patternTokens, List<Integer> tokenPositions) throws IOException {
    JLanguageTool lt = ((PatternRule) match.getRule()).getLanguage().createDefaultJLanguageTool();
    List<String> replacements = new ArrayList<>();
    List<String> actions = new ArrayList<>();
    boolean numberFromNextWords = getOptional("numberFromNextWords", arguments, "false").equalsIgnoreCase("true");
    Synthesizer synth = getSynthesizerFromRuleMatch(match);
    int posWord = 0;
    AnalyzedTokenReadings[] tokens = match.getSentence().getTokensWithoutWhitespace();
    while (posWord < tokens.length
      && (tokens[posWord].getStartPos() < match.getFromPos() || tokens[posWord].isSentenceStart())) {
      posWord++;
    }
    int toLeft = 0;
    boolean done = false;
    String firstVerb = "";
    String firstVerbPersonaNumber = "";
    String firstVerbPersonaNumberImperative = "";
    String replacementVerb = "";
    int firstVerbPos = 0;
    boolean inPronouns = false;
    boolean firstVerbValid = false;
    while (!done && posWord - toLeft > 0) {
      AnalyzedTokenReadings currentTkn = tokens[posWord - toLeft];
      String currentTknStr = currentTkn.getToken();
      boolean isVerb = currentTkn.hasPosTagStartingWith("V");
      boolean isPronoun = currentTkn.matchesPosTagRegex("P0.{6}|PP3CN000|PP3NN000|PP3..A00|PP3CP000|PP3CSD00");
      if (isPronoun) {
        inPronouns = true;
      }
      if (isPronoun || (isVerb && !inPronouns && !firstVerbValid) || currentTknStr.equalsIgnoreCase("de")
        || currentTknStr.equalsIgnoreCase("d'")) {
        if (isVerb) {
          firstVerb = currentTknStr;
          firstVerbPos = toLeft;
          firstVerbValid = currentTkn.matchesPosTagRegex("V.[SI].*");
          if (firstVerbValid) {
            firstVerbPersonaNumber = currentTkn.readingWithTagRegex("V.[SI].*").getPOSTag().substring(4, 6);
          }
          if (currentTkn.matchesPosTagRegex("V.M.*")) {
            firstVerbPersonaNumberImperative = currentTkn.readingWithTagRegex("V.M.*").getPOSTag().substring(4, 6);
          }
        }
        toLeft++;
      } else {
        done = true;
        if (toLeft > 0) {
          toLeft--;
        }
      }
    }
    if (posWord - toLeft == 0) {
      toLeft--;
    }
    if (!firstVerbValid) {
      return null;
    }
    for (String originalSuggestion : match.getSuggestedReplacements()) {
      int firstSpaceIndex = originalSuggestion.indexOf(" ");
      String newLemma = originalSuggestion.toLowerCase();
      String afterLemma = "";
      String desiredNumber = "";
      if (firstSpaceIndex != -1) {
        newLemma = originalSuggestion.substring(0, firstSpaceIndex);
        afterLemma = originalSuggestion.substring(firstSpaceIndex + 1);
        List<AnalyzedSentence> tokensAfterLemma = lt.analyzeText(afterLemma);
        if (numberFromNextWords) {
          desiredNumber = (tokensAfterLemma.get(0).getTokensWithoutWhitespace()[1].hasPartialPosTag("S") ? "S" :
            "P");
        }
      }
      if (newLemma.equals("haver")) {
        desiredNumber = "S";
      }
      actions.clear();
      if (newLemma.endsWith("-se")) {
        newLemma = newLemma.substring(0, newLemma.length() - 3);
        actions.add("addPronounReflexive");
      } else if (newLemma.endsWith("-hi")) {
        newLemma = newLemma.substring(0, newLemma.length() - 3);
        actions.add("addPronounHi");
      } else if (newLemma.endsWith("-s'ho")) {
        newLemma = newLemma.substring(0, newLemma.length() - 5);
        actions.add("addPronounReflexiveHo");
      } else if (newLemma.endsWith("-s'hi")) {
        newLemma = newLemma.substring(0, newLemma.length() - 5);
        actions.add("addPronounReflexiveHi");
      }
      // synthesize with new lemma
      List<String> postags = new ArrayList<>();
      for (AnalyzedToken reading : tokens[posWord]) {
        if (reading.getPOSTag() != null && reading.getPOSTag().startsWith("V")) {
          String postag = reading.getPOSTag();
          if (!desiredNumber.isEmpty()) {
            if (newLemma.equals("haver")) {
              if (!postag.substring(2, 3).equals("P") && (postag.substring(5, 6).equals("S") || postag.substring(5,
                6).equals("P"))) {
                postag = "VA" + postag.substring(2, 5) + "S" + postag.substring(6);
              } else {
                // gerundi o infinitiu
                postag = "VA" + postag.substring(2, 3) + "00000";
              }
            } else if (!postag.substring(2, 3).equals("P") && (postag.substring(5, 6).equals("S") || postag.substring(5
              , 6).equals("P"))) {
              postag = postag.substring(0, 5) + desiredNumber + postag.substring(6);
            }
          }
          postags.add(postag);
        }
      }
      String targetPostag = synth.getTargetPosTag(postags, "");
      if (!targetPostag.isEmpty()) {
        AnalyzedToken at = new AnalyzedToken(tokens[posWord].getToken(), targetPostag, newLemma);
        String[] synthForms = synth.synthesize(at, targetPostag);
        if (synthForms != null && synthForms.length > 0) {
          replacementVerb = synthForms[0];
        }
      }

      StringBuilder sb = new StringBuilder();
      for (int i = posWord - toLeft; i < posWord - firstVerbPos; i++) {
        sb.append(tokens[i].getToken());
        if (tokens[i + 1].isWhitespaceBefore()) {
          sb.append(" ");
        }
      }
      String pronounsStr = sb.toString().trim();
      sb = new StringBuilder();
      for (int i = posWord - firstVerbPos; i <= posWord; i++) {
        if (i == posWord && !replacementVerb.isEmpty()) {
          sb.append(replacementVerb);
        } else {
          // change number if necessary.
          String newFirstVerb = tokens[i].getToken();
          if (i == posWord - firstVerbPos) {
            String number = "";
            AnalyzedToken atr = tokens[i].readingWithTagRegex("V.[SI].*");
            if (atr != null) {
              number = atr.getPOSTag().substring(5, 6);
              String postag = atr.getPOSTag();
              if ((number.equals("S") || number.equals("P") && !number.equals(desiredNumber) && !desiredNumber.isEmpty())) {
                postag = postag.substring(0, 5) + desiredNumber + postag.substring(6);
                String[] synthForms = synth.synthesize(atr, postag);
                if (synthForms != null && synthForms.length > 0) {
                  newFirstVerb = synthForms[0];
                }
              }
            }
          }
          sb.append(newFirstVerb);
        }
        if (i + 1 < tokens.length && tokens[i + 1].isWhitespaceBefore()) {
          sb.append(" ");
        }
      }
      String verbStr = sb.toString().trim();
      for (String action : actions) {
        String replacement = "";
        switch (action) {
          case "addPronounEn":
            replacement = doAddPronounEn(firstVerb, pronounsStr, verbStr);
            break;
          case "removePronounReflexive":
            replacement = doRemovePronounReflexive(firstVerb, pronounsStr, verbStr);
            break;
          case "replaceEmEn":
            replacement = doReplaceEmEn(firstVerb, pronounsStr, verbStr);
            break;
          case "addPronounReflexive":
            replacement = doAddPronounReflexive(firstVerb, "", verbStr, firstVerbPersonaNumber);
            break;
          case "addPronounReflexiveHi":
            replacement = doAddPronounReflexive(firstVerb, "", "hi " + verbStr, firstVerbPersonaNumber);
            break;
          case "addPronounReflexiveHo":
            replacement = doAddPronounReflexive(firstVerb, "", "ho " + verbStr, firstVerbPersonaNumber);
            break;
          case "addPronounHi":
            replacement = doAddPronounHi(firstVerb, "", verbStr);
            break;
          case "addPronounReflexiveImperative":
            replacement = doAddPronounReflexiveImperative(firstVerb, pronounsStr, verbStr,
              firstVerbPersonaNumberImperative);
            break;
        }
        if (!replacement.isEmpty()) {
          replacements.add(StringTools.preserveCase(replacement + " " + afterLemma,
            tokens[posWord - toLeft].getToken()).trim());
        }
      }
      if (actions.isEmpty()) {
        replacements.add(StringTools.preserveCase(verbStr + " " + afterLemma, tokens[posWord - toLeft].getToken()).trim());
      }
    }
    if (replacements.isEmpty()) {
      return null;
    }
    RuleMatch ruleMatch = new RuleMatch(match.getRule(), match.getSentence(), tokens[posWord - toLeft].getStartPos(),
      match.getToPos(), match.getMessage(), match.getShortMessage());
    ruleMatch.setType(match.getType());
    ruleMatch.setSuggestedReplacements(replacements);
    return ruleMatch;
  }

}
