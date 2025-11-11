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
import org.languagetool.synthesis.ca.VerbSynthesizer;
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
    VerbSynthesizer verbSynthesizer = new VerbSynthesizer(tokens, posWord, getLanguageFromRuleMatch(match));
    // verb found out of bounds
    if (verbSynthesizer.isUndefined() || tokens[verbSynthesizer.getLastVerbPos()].getEndPos() > match.getToPos()) {
      return null;
    }
    for (String originalSuggestion : match.getSuggestedReplacements()) {
      originalSuggestion = originalSuggestion.toLowerCase();
      boolean makeIntrasitive = false;
      String desiredNumber = "";
      String desiredPersona = "";
      String action = "removePronounReflexive";
      if (originalSuggestion.endsWith(" [intr]")) {
        originalSuggestion = originalSuggestion.substring(0, originalSuggestion.length() - 7);
        makeIntrasitive = true;
      }
      if (originalSuggestion.endsWith(" [3s]")) {
        originalSuggestion = originalSuggestion.substring(0, originalSuggestion.length() - 5);
        desiredNumber = "S";
        desiredPersona = "3";
      }
      if (originalSuggestion.startsWith("[datiu] ")) {
        originalSuggestion = originalSuggestion.substring(8);
        action = "addPronounDative";
      }
      int firstSpaceIndex = originalSuggestion.indexOf(" ");
      String newLemma = originalSuggestion;
      String afterLemma = "";

      if (firstSpaceIndex != -1) {
        newLemma = originalSuggestion.substring(0, firstSpaceIndex);
        afterLemma = originalSuggestion.substring(firstSpaceIndex + 1);
        List<AnalyzedSentence> tokensAfterLemma = lt.analyzeText(afterLemma);
        if (numberFromNextWords) {
          desiredNumber = (tokensAfterLemma.get(0).getTokensWithoutWhitespace()[1].hasPartialPosTag("S") ? "S" :  "P");
        }
      }
      if (newLemma.equals("haver")) {
        desiredNumber = "S";
      }
      if (!forceNumber.isEmpty()) {
        desiredNumber = forceNumber;
      }

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
      for (AnalyzedToken reading : tokens[verbSynthesizer.getFirstVerbPos()]) {
        if (reading.getPOSTag() != null && reading.getPOSTag().startsWith("V")) {
          String postag = reading.getPOSTag();
          if (!desiredNumber.isEmpty()) {
            if (!postag.substring(2, 3).equals("P") && (postag.substring(5, 6).equals("S") || postag.substring(5
              , 6).equals("P"))) {
              postag = postag.substring(0, 5) + desiredNumber + postag.substring(6);
            }
          }
          if (!desiredPersona.isEmpty()) {
            if (!postag.substring(2, 3).equals("P") && (postag.substring(4, 5).matches("[123]"))) {
              postag = postag.substring(0, 4) + desiredPersona + postag.substring(5);
            }
          }
          postags.add(postag);
        }
      }
      String targetPostag = synth.getTargetPosTag(postags, "");
      String verbStr = "";
      if (!targetPostag.isEmpty()) {
        verbSynthesizer.setLemmaAndPostag(newLemma, targetPostag);
        verbStr = verbSynthesizer.synthesize();
      }
      String pronounsStr="";
      boolean isPronounsAfter = verbSynthesizer.getNumPronounsAfter() > 0 || !verbSynthesizer.isFirstVerbIS();
      if (verbSynthesizer.getNumPronounsBefore() > 0) {
        pronounsStr = verbSynthesizer.getPronounsStrBefore();
      } else if (verbSynthesizer.getNumPronounsAfter() > 0) {
        pronounsStr = verbSynthesizer.getPronounsStrAfter();
      }
      pronounsStr = pronounsStr.toLowerCase();
      String firstVerbPersonaNumber = verbSynthesizer.getFirstVerbPersonaNumber();
      String replacement = "";
      switch (action) {
        case "addPronounEn":
          String newPronoun = doAddPronounEn(pronounsStr, verbStr);
          if (!newPronoun.isEmpty()) {
            replacement = newPronoun + verbStr;
          }
          break;
        case "removePronounReflexive":
          replacement = doRemovePronounReflexive(pronounsStr, verbStr, isPronounsAfter);
          break;
        case "addPronounReflexiveEn":
          replacement = doAddPronounReflexiveEn(pronounsStr, verbStr, firstVerbPersonaNumber, isPronounsAfter);
          break;
        case "replaceEmEn": // not used
          replacement = doReplaceEmEn(pronounsStr, verbStr, isPronounsAfter);
          break;
        case "addPronounReflexive":
          replacement = doAddPronounReflexive(pronounsStr, verbStr, firstVerbPersonaNumber, isPronounsAfter);
          break;
        case "addPronounReflexiveHi":
          replacement = doAddPronounReflexive("", "hi " + verbStr, firstVerbPersonaNumber, isPronounsAfter);
          break;
        case "addPronounDative":
          String dativePronoun = getDativePronoun(firstVerbPersonaNumber);
          if (isPronounsAfter) {
            replacement = verbStr + transformDarrere(dativePronoun, verbStr);
          } else {
            replacement = transformDavant(dativePronoun, verbStr) + verbStr;
          }
          break;
        case "addPronounReflexiveHo":
          String reflexivePronoun = getReflexivePronoun(firstVerbPersonaNumber);
          if (reflexivePronoun.isEmpty()) {
            if (!pronounsStr.isEmpty()) {
               String rp = transform(pronounsStr, PronounPosition.NORMALIZED);
              if (lReflexivePronouns.contains(rp)) {
                reflexivePronoun= rp;
              }
            }
          }
          if (reflexivePronoun.isEmpty()) {
            reflexivePronoun = "es";
          }
          String pronounsNormalized =  reflexivePronoun + " ho";
          if (isPronounsAfter) {
            replacement = verbStr + transformDarrere(pronounsNormalized, verbStr);
          } else {
            replacement = transformDavant(pronounsNormalized, verbStr) + verbStr;
          }
          break;
        case "addPronounHi": // S'ignoren altres pronoms?
          replacement = "hi " + verbStr;
          break;
        case "addPronounReflexiveImperative": //TODO
          replacement = doAddPronounReflexiveImperative(pronounsStr, verbStr, firstVerbPersonaNumber);
          break;
      }
      if (!replacement.isEmpty()) {
        if (makeIntrasitive) {
          replacement = convertPronounsForIntransitiveVerb(replacement);
        }
        replacement = fixApostrophes(replacement);
        replacement = (replacement + " " + afterLemma).trim();
        replacements.add(StringTools.preserveCase(replacement, verbSynthesizer.getCasingModel()));
      }
    }
    if (replacements.isEmpty()) {
      return null;
    }
    int posStartUnderline = verbSynthesizer.getFirstVerbPos() - verbSynthesizer.getNumPronounsBefore();
    RuleMatch ruleMatch = new RuleMatch(match.getRule(), match.getSentence(), tokens[posStartUnderline].getStartPos(),
      match.getToPos(), match.getMessage(), match.getShortMessage());
    ruleMatch.setType(match.getType());
    ruleMatch.setSuggestedReplacements(replacements);
    return ruleMatch;
  }

}
