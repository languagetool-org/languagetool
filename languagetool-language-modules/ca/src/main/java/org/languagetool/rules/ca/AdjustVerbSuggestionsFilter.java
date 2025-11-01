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
import org.languagetool.chunking.ChunkTag;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.PatternRule;
import org.languagetool.rules.patterns.RuleFilter;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.languagetool.rules.ca.PronomsFeblesHelper.*;

public class AdjustVerbSuggestionsFilter extends RuleFilter {

  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos,
                                   AnalyzedTokenReadings[] patternTokens, List<Integer> tokenPositions) throws IOException {
    /*if (match.getSentence().getText().contains("Demà es compliran")) {
      int ii=0;
      ii++;
    }*/
    JLanguageTool lt = ((PatternRule) match.getRule()).getLanguage().createDefaultJLanguageTool();
    List<String> replacements = new ArrayList<>();
    boolean numberFromNextWords = getOptional("numberFromNextWords", arguments, "false").equalsIgnoreCase("true");
    String forceNumber = getOptional("forceNumber", arguments, "");
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
    boolean firstVerbInflected = false;
    // if there are pronouns after the verb, ignore everything before
    String[] twoPronounsAfter = getTwoNextPronouns(tokens, posWord + 1);
    if (Integer.parseInt(twoPronounsAfter[1]) > 0) {
      done = true;
    }
    while (!done && posWord - toLeft > 0) {
      AnalyzedTokenReadings currentTkn = tokens[posWord - toLeft];
      String currentTknStr = currentTkn.getToken();
      boolean isVerb = currentTkn.hasPosTagStartingWith("V");
      boolean isPronoun = currentTkn.matchesPosTagRegex("P0.{6}|PP3CN000|PP3NN000|PP3..A00|PP3CP000|PP3CSD00");
      if (isPronoun) {
        inPronouns = true;
      }
      boolean isInGV =  currentTkn.getChunkTags().contains(new ChunkTag("GV"));
      if (isPronoun || (isVerb && !inPronouns && !firstVerbInflected && (toLeft == 0 || isInGV)) || (isInGV && !firstVerbInflected)) {
        if (isVerb) {
          firstVerb = currentTknStr;
          firstVerbPos = toLeft;
          firstVerbInflected = currentTkn.matchesPosTagRegex("V.[SI].*");
          if (firstVerbInflected) {
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
      // avoid the SENT_START token
      toLeft--;
    }
    for (String originalSuggestion : match.getSuggestedReplacements()) {
      originalSuggestion = originalSuggestion.toLowerCase();
      boolean makeIntrasitive = false;
      if (originalSuggestion.endsWith(" [intr]")) {
        originalSuggestion = originalSuggestion.substring(0, originalSuggestion.length() - 7);
        makeIntrasitive = true;
      }
      int firstSpaceIndex = originalSuggestion.indexOf(" ");
      String newLemma = originalSuggestion;
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
      if (!forceNumber.isEmpty()) {
        desiredNumber = forceNumber;
      }
      String action = "removePronounReflexive";
      if (newLemma.endsWith("-se'n")) {
        newLemma = newLemma.substring(0, newLemma.length() - 5);
        action = "addPronounReflexiveEn";
      } else if (newLemma.endsWith("-se")) {
        newLemma = newLemma.substring(0, newLemma.length() - 3);
        action = "addPronounReflexive";
      } else if (newLemma.endsWith("'s")) {
        newLemma = newLemma.substring(0, newLemma.length() - 2);
        action = "addPronounReflexive";
      }else if (newLemma.endsWith("-hi")) {
        newLemma = newLemma.substring(0, newLemma.length() - 3);
        action = "addPronounHi";
      } else if (newLemma.endsWith("-s'ho")) {
        newLemma = newLemma.substring(0, newLemma.length() - 5);
        action = "addPronounReflexiveHo";
      } else if (newLemma.endsWith("-s'hi")) {
        newLemma = newLemma.substring(0, newLemma.length() - 5);
        action = "addPronounReflexiveHi";
      }
      // synthesize with new lemma
      List<String> postags = new ArrayList<>();
      for (AnalyzedToken reading : tokens[posWord]) {
        if (reading.getPOSTag() != null && reading.getPOSTag().startsWith("V")) {
          String postag = reading.getPOSTag();
          if (newLemma.equals("haver")) {
            postag = "VA" + postag.substring(2);
          }
          if (newLemma.equals("ser")) {
            postag = "VS" + postag.substring(2);
          }
          if (!desiredNumber.isEmpty()) {
            if (!postag.substring(2, 3).equals("P") && (postag.substring(5, 6).equals("S") || postag.substring(5
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
      String replacement = "";
      String verbStr = sb.toString().trim().toLowerCase();
      if (!firstVerbInflected) {
        pronounsStr = twoPronounsAfter[0];
      }
      pronounsStr = pronounsStr.toLowerCase();
      switch (action) {
        case "addPronounEn":
          replacement = doAddPronounEn(firstVerb, pronounsStr, verbStr, !firstVerbInflected);
          break;
        case "removePronounReflexive":
          replacement = doRemovePronounReflexive(firstVerb, pronounsStr, verbStr, !firstVerbInflected);
          break;
        case "addPronounReflexiveEn":
          replacement = doAddPronounReflexiveEn(firstVerb, pronounsStr, verbStr, firstVerbPersonaNumber, !firstVerbInflected);
          break;
        case "replaceEmEn":
          replacement = doReplaceEmEn(firstVerb, pronounsStr, verbStr, !firstVerbInflected);
          break;
        case "addPronounReflexive":
          replacement = doAddPronounReflexive(firstVerb, pronounsStr, verbStr, firstVerbPersonaNumber,
            !firstVerbInflected);
          break;
        case "addPronounReflexiveHi":
          replacement = doAddPronounReflexive(firstVerb, "", "hi " + verbStr, firstVerbPersonaNumber,
            !firstVerbInflected);
          break;
        case "addPronounReflexiveHo":
          String newVerbStr = verbStr;
          if (firstVerbInflected) {
            newVerbStr = "ho " + verbStr;
          } else if (!pronounsStr.isEmpty()){
            pronounsStr = pronounsStr+"-ho";
          }
          replacement = doAddPronounReflexive(firstVerb, pronounsStr, newVerbStr, firstVerbPersonaNumber,
            !firstVerbInflected);
          break;
        case "addPronounHi":
          replacement = doAddPronounHi(firstVerb, "", verbStr, !firstVerbInflected);
          break;
        case "addPronounReflexiveImperative":
          replacement = doAddPronounReflexiveImperative(firstVerb, pronounsStr, verbStr,
            firstVerbPersonaNumberImperative);
          break;
      }
      if (!replacement.isEmpty()) {
        if (makeIntrasitive) {
          replacement = convertPronounsForIntransitiveVerb(replacement);
        }
        replacement = fixApostrophes(replacement);
        replacements.add(StringTools.preserveCase(replacement + " " + afterLemma,
          tokens[posWord - toLeft].getToken()).trim());
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
