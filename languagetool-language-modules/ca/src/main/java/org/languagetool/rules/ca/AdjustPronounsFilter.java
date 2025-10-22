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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.AnalyzedToken;
import org.languagetool.chunking.ChunkTag;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.RuleFilter;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.tools.StringTools;

import static org.languagetool.rules.ca.PronomsFeblesHelper.*;


/*
 * Add the pronoun "en" in the required place with all the necessary transformations,
 * including moving the <marker> positions.
 */

public class AdjustPronounsFilter extends RuleFilter {


  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos,
                                   AnalyzedTokenReadings[] patternTokens, List<Integer> tokenPositions) throws IOException {
    /*if (match.getSentence().getText().contains("Es prepara una")) {
      int ii=0;
      ii++;
    }*/
    List<String> replacements = new ArrayList<>();
    List<String> actions = Arrays.asList(getRequired("actions", arguments).split(","));
    Synthesizer synth = getSynthesizerFromRuleMatch(match);
    String newLemma = getOptional("newLemma", arguments);
    String newOnlyLemma = getOptional("newOnlyLemma", arguments);
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
    while (!done && posWord - toLeft > 0) {
      AnalyzedTokenReadings currentTkn = tokens[posWord - toLeft];
      String currentTknStr = currentTkn.getToken();
      // change lemma if asked
      if (toLeft == 0 && (newLemma != null || newOnlyLemma != null)) {
        List<String> postags = new ArrayList<>();
        for (AnalyzedToken reading : currentTkn) {
          if (reading.getPOSTag() != null && reading.getPOSTag().startsWith("V")) {
            postags.add(reading.getPOSTag());
          }
        }
        String targetPostag = synth.getTargetPosTag(postags, "");
        if (!targetPostag.isEmpty()) {
          AnalyzedToken at;
          if (newLemma != null) {
            at = new AnalyzedToken(currentTknStr, targetPostag, newLemma);
          } else {
            at = new AnalyzedToken(currentTknStr, targetPostag, newOnlyLemma);
          }
          String[] synthForms = synth.synthesize(at, targetPostag);
          if (synthForms != null && synthForms.length > 0) {
            replacementVerb = synthForms[0];
          }

        }
      }
      boolean isVerb = currentTkn.hasPosTagStartingWith("V");
      boolean isPronoun = currentTkn.matchesPosTagRegex("P0.{6}|PP3CN000|PP3NN000|PP3..A00|PP3CP000|PP3CSD00");
      if (isPronoun) {
        inPronouns = true;
      }
      boolean isInGV = currentTkn.getChunkTags().contains(new ChunkTag("GV"));
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
    if (!firstVerbInflected) {
      return null;
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
      if (i == posWord && !replacementVerb.isEmpty() && newLemma != null) {
        sb.append(replacementVerb);
      } else {
        sb.append(tokens[i].getToken());
      }
      if (i + 1 < tokens.length && tokens[i + 1].isWhitespaceBefore()) {
        sb.append(" ");
      }
    }
    String verbStr = sb.toString().trim();

    if (newOnlyLemma != null) {
      sb = new StringBuilder();
      for (int i = posWord - firstVerbPos; i <= posWord; i++) {
        if (i == posWord && !replacementVerb.isEmpty() && newOnlyLemma != null) {
          sb.append(replacementVerb);
        } else {
          sb.append(tokens[i].getToken());
        }
        if (i + 1 < tokens.length && tokens[i + 1].isWhitespaceBefore()) {
          sb.append(" ");
        }
      }
    }
    String verbStr2 = sb.toString().trim();

    for (String action : actions) {
      String replacement = "";
      switch (action) {
        case "removePronounEn":
          String pr = pronounsStr.replace("en","").replace("n'","").replace("'n","").strip();
          replacement = transformDavant(pr, verbStr) + verbStr;
          break;
        case "addPronounEn":
          replacement = doAddPronounEn(firstVerb, pronounsStr, verbStr, false);
          break;
        case "removePronounReflexive":
          replacement = doRemovePronounReflexive(firstVerb, pronounsStr, verbStr, false);
          break;
        case "replaceEmEn":
          replacement = doReplaceEmEn(firstVerb, pronounsStr, verbStr, false);
          break;
        case "replaceHiEn":
          replacement = doAddPronounEn(firstVerb, pronounsStr.replace("hi", "").trim(), verbStr, false);
          break;
        case "addPronounReflexive":
          replacement = doAddPronounReflexive(firstVerb, pronounsStr, verbStr, firstVerbPersonaNumber, false);
          break;
        case "addPronounReflexiveHi":
          replacement = doAddPronounReflexive(firstVerb, pronounsStr, "hi " + verbStr, firstVerbPersonaNumber, false);
          break;
        case "addPronounReflexiveImperative":
          replacement = doAddPronounReflexiveImperative(firstVerb, pronounsStr, verbStr,
            firstVerbPersonaNumberImperative);
          break;
        case "changeOnlyLemma":
          if (actions.contains("replaceHiEn")) {
            pronounsStr = pronounsStr.replace("hi", "").trim();
          }
          pronounsStr = transformDavant(pronounsStr, verbStr2);
          replacement = pronounsStr + verbStr2;
          break;
      }
      if (!replacement.isEmpty()) {
        replacements.add(StringTools.preserveCase(replacement, tokens[posWord - toLeft].getToken()).trim());
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
