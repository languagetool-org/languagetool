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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.language.German;
import org.languagetool.language.GermanyGerman;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.RuleFilter;
import org.languagetool.tools.StringTools;

public class PotentialCompoundFilter extends RuleFilter {

  private final German language = new GermanyGerman();
  private JLanguageTool lt;

  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos,
      AnalyzedTokenReadings[] patternTokens) throws IOException {
    initLt();
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
    List<RuleMatch> matches = lt.check(joinedWord);
    if (matches.isEmpty()) {
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

  private void initLt() {
    if (lt == null) {
      lt = new JLanguageTool(language);
      for (Rule rule : lt.getAllActiveRules()) {
        if (!rule.getId().equals("GERMAN_SPELLER_RULE")) {
          lt.disableRule(rule.getId());
        }
      }
    }
  }

}
