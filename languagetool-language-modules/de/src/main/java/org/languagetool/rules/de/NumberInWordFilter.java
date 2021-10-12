/* LanguageTool, a natural language style checker
 * Copyright (C) 2021 Stefan Viol
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

import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.language.German;
import org.languagetool.language.GermanyGerman;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.RuleFilter;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

public class NumberInWordFilter extends RuleFilter {

  private final German language = new GermanyGerman();
  private final static Pattern typoPattern = Pattern.compile("[0-9]");

  @Nullable
  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos, AnalyzedTokenReadings[] patternTokens) throws IOException {
    ResourceBundle messages = JLanguageTool.getDataBroker().getResourceBundle(JLanguageTool.MESSAGE_BUNDLE, new Locale(language.getShortCode()));
    String word = arguments.get("word");
    String wordWithoutNumberCharacter = typoPattern.matcher(word).replaceAll("");
    List<String> replacements = new ArrayList<>();

    GermanSpellerRule germanSpellerRule = new GermanSpellerRule(messages, language);
    boolean misspelled = germanSpellerRule.isMisspelled(wordWithoutNumberCharacter);

    if (misspelled) {
      List<String> suggestions = germanSpellerRule.getSuggestions(wordWithoutNumberCharacter);
      replacements.addAll(suggestions);
    } else {
      replacements.add(wordWithoutNumberCharacter);
    }
    if (!replacements.isEmpty()) {
      RuleMatch ruleMatch = new RuleMatch(match);
      ruleMatch.setSuggestedReplacements(replacements);
      return ruleMatch;
    }
    return null;
  }
}
