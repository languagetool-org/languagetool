/*
 * LanguageTool, a natural language style checker
 * Copyright (C) 2021 Stefan Viol (https://stevio.de)
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

package org.languagetool.rules;

import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.rules.patterns.RuleFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public abstract class AbstractNumberInWordFilter extends RuleFilter {

  protected final Language language;

  public static final Pattern typoPattern = Pattern.compile("[0-9]");

  protected AbstractNumberInWordFilter(Language language) {
    this.language = language;
  }

  @Nullable
  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos, AnalyzedTokenReadings[] patternTokens) throws IOException {
    String word = arguments.get("word");
    String wordReplacingZeroO = word.replaceAll("0","o");
    String wordWithoutNumberCharacter = typoPattern.matcher(word).replaceAll("");
    List<String> replacements = new ArrayList<>();
    
    if (!isMisspelled(wordReplacingZeroO) && !word.equals(wordReplacingZeroO) ) {
      replacements.add(wordReplacingZeroO);
    }
    if (!isMisspelled(wordWithoutNumberCharacter)) {
      replacements.add(wordWithoutNumberCharacter);
    } 
    if (replacements.isEmpty()){
      List<String> suggestions = getSuggestions(wordWithoutNumberCharacter);
      replacements.addAll(suggestions);
    }
    if (!replacements.isEmpty()) {
      RuleMatch ruleMatch = new RuleMatch(match);
      ruleMatch.setSuggestedReplacements(replacements);
      return ruleMatch;
    }
    return null;
  }

  protected abstract boolean isMisspelled(String word) throws IOException;

  protected abstract List<String> getSuggestions(String word) throws IOException;

}
