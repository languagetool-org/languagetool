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
package org.languagetool.rules.de;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.language.GermanyGerman;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.RuleFilter;
import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


/*
 * This filter creates a compound word from two tokens. 
 * If the joined word is a valid word and its length is less than 20 characters, 
 * the rule provides the joined word as a suggestion. 
 * Otherwise, it provides the hyphenated word.
 */

public class PotentialCompoundFilter extends RuleFilter {

  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos,
      AnalyzedTokenReadings[] patternTokens) throws IOException {
    String part1 = arguments.get("part1");
    String part2 = arguments.get("part2");
    String part1capitalized = part1;
    String part2capitalized = part2;
    String part2lowercase = part2;
    if (!StringTools.isMixedCase(part2) && !StringTools.isAllUppercase(part2)) {
      part2lowercase = part2.toLowerCase();
      part2capitalized = StringTools.uppercaseFirstChar(part2.toLowerCase());
    }
    if (!StringTools.isMixedCase(part1) && !StringTools.isAllUppercase(part1)) {
      part1capitalized = StringTools.uppercaseFirstChar(part1.toLowerCase());
    }
    String joinedWord = part1capitalized + part2lowercase;
    String hyphenatedWord = part1capitalized + "-" + part2capitalized;
    List<String> replacements = new ArrayList<>();
    // create an AnalyzedSentence without instantiating a new JLanguageTool
    List<String> tokens =  Collections.singletonList(joinedWord);
    List<AnalyzedTokenReadings> tokensList = GermanyGerman.INSTANCE.getTagger().tag(tokens);
    AnalyzedTokenReadings[] tokensArray = new AnalyzedTokenReadings[2];
    AnalyzedToken sentenceStartToken = new AnalyzedToken("", "SENT_START", null);
    AnalyzedToken[] startTokenArray = new AnalyzedToken[1];
    startTokenArray[0] = sentenceStartToken;
    tokensArray[0] = new AnalyzedTokenReadings(startTokenArray, 0);
    tokensArray[1] = tokensList.get(0);
    AnalyzedSentence analyzedSentence = new AnalyzedSentence(tokensArray);
    // check with the spelling rule
    RuleMatch[] matches = GermanyGerman.INSTANCE.getDefaultSpellingRule().match(analyzedSentence);
    if (matches.length == 0) {
      if (joinedWord.length() > 20) {
        replacements.add(hyphenatedWord);
      }
      replacements.add(joinedWord);
    } else {
      replacements.add(hyphenatedWord);
    }
    if (!replacements.isEmpty()) {
      String message = match.getMessage();
      RuleMatch ruleMatch = new RuleMatch(match.getRule(), match.getSentence(), match.getFromPos(), match.getToPos(),
          message, match.getShortMessage());
      ruleMatch.setType(match.getType());
      ruleMatch.setSuggestedReplacements(replacements);
      return ruleMatch;
    }
    return null;
  }
}
