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
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.RuleFilter;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.synthesis.ca.VerbSynthesizer;
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
    VerbSynthesizer verbSynthesizer = new VerbSynthesizer(tokens, posWord, getLanguageFromRuleMatch(match));
    if (verbSynthesizer.isUndefined()) {
      // || tokens[verbSynthesizer.getLastVerbPos()].getEndPos() > match.getToPos()
      return null;
    }
    /*if (verbSynthesizer.getLastVerbPos() > match.getToPos() && verbSynthesizer.getNumPronounsAfter()==0) {
      verbSynthesizer.setLastVerbPos(match.getToPos());
    }*/
    String verbStr = verbSynthesizer.getVerbStr();
    if (newLemma != null) {
      verbSynthesizer.setLemma(newLemma);
      verbStr = verbSynthesizer.synthesize();
    }
    String verbStr2 = verbStr;
    if (newOnlyLemma != null) {
      verbSynthesizer.setLemma(newOnlyLemma);
      verbStr2 = verbSynthesizer.synthesize();
    }
    String firstVerbPersonaNumber = verbSynthesizer.getFirstVerbPersonaNumber();
    String pronounsStr = "";
    if (verbSynthesizer.getNumPronounsBefore() > 0) {
      pronounsStr = verbSynthesizer.getPronounsStrBefore();
    } else if (verbSynthesizer.getNumPronounsAfter() > 0) {
      pronounsStr = verbSynthesizer.getPronounsStrAfter();
    }
    int startUnderlineIndex = verbSynthesizer.getFirstVerbIndex() - verbSynthesizer.getNumPronounsBefore();
    int endUnderlineIndex = verbSynthesizer.getLastVerbIndex() + verbSynthesizer.getNumPronounsAfter();
    for (String action : actions) {
      String replacement = "";
      switch (action) {
        case "removePronounEn":
          String pr = pronounsStr.replace("en", "").replace("n'", "").replace("'n", "").strip();
          replacement = transformDavant(pr, verbStr) + verbStr;
          break;
        case "addPronounEn":
          String newPronoun = doAddPronounEn(pronounsStr, verbStr, !verbSynthesizer.isFirstVerbIS());
          if (!newPronoun.isEmpty()) {
            replacement = (verbSynthesizer.isFirstVerbIS() ? newPronoun + verbStr : verbStr + newPronoun);
          }
          break;
        case "removePronounReflexive":
          replacement = doRemovePronounReflexive(pronounsStr, verbStr, false);
          break;
        case "replaceEmEn":
          replacement = doReplaceEmEn(pronounsStr, verbStr, false);
          break;
        case "replaceHiEn":
          replacement = doAddPronounEn(transform(pronounsStr.replace("hi", "").trim(), PronounPosition.NORMALIZED),
            verbStr, false) + verbStr;
          break;
        case "addPronounReflexive":
          replacement = doAddPronounReflexive(pronounsStr, verbStr, firstVerbPersonaNumber, !verbSynthesizer.isFirstVerbIS());
          break;
        case "addPronounReflexiveHi":
          replacement = doAddPronounReflexive(pronounsStr, "hi " + verbStr, firstVerbPersonaNumber, false);
          break;
        case "addPronounReflexiveImperative":
          replacement = doAddPronounReflexiveImperative(pronounsStr, verbStr, verbSynthesizer.getFirstVerbPersonaNumberImperative());
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
        replacements.add(StringTools.preserveCase(replacement, verbSynthesizer.getCasingModel()));
      }
    }
    if (replacements.isEmpty()) {
      return null;
    }
    RuleMatch ruleMatch = new RuleMatch(match.getRule(), match.getSentence(), tokens[startUnderlineIndex].getStartPos(),
      tokens[endUnderlineIndex].getEndPos(), match.getMessage(), match.getShortMessage());
    ruleMatch.setType(match.getType());
    ruleMatch.setSuggestedReplacements(replacements);
    return ruleMatch;
  }

}
