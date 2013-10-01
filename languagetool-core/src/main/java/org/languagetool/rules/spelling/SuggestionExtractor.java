/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.spelling;

import org.languagetool.Language;
import org.languagetool.rules.Rule;
import org.languagetool.rules.patterns.PatternRule;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extract tokens from suggestions.
 */
public class SuggestionExtractor {

  private static final Pattern SUGGESTION_PATTERN = Pattern.compile("<suggestion.*?>(.*?)</suggestion>");
  private static final Pattern BACK_REFERENCE_PATTERN = Pattern.compile("\\\\" + "\\d+");

  public SuggestionExtractor() {
  }

  /**
   * Get the tokens of simple suggestions, i.e. those that don't use back references.
   */
  public List<String> getSuggestionTokens(Rule rule, Language language) {
    final List<String> wordsToBeIgnored = new ArrayList<>();
    if (rule instanceof PatternRule) {
      final PatternRule patternRule = (PatternRule) rule;
      final String message = patternRule.getMessage();
      final List<String> suggestions = getSimpleSuggestions(message);
      final List<String> tokens = getSuggestionTokens(suggestions, language);
      wordsToBeIgnored.addAll(tokens);
    }
    return wordsToBeIgnored;
  }

  /**
   * Get suggestions that don't use back references or regular expressions.
   */
  List<String> getSimpleSuggestions(String message) {
    final Matcher matcher = SUGGESTION_PATTERN.matcher(message);
    int startPos = 0;
    final List<String> suggestions = new ArrayList<>();
    while (matcher.find(startPos)) {
      final String suggestion = matcher.group(1);
      startPos = matcher.end();
      if (isSimpleSuggestion(suggestion)) {
        suggestions.add(suggestion);
      }
    }
    return suggestions;
  }

  private boolean isSimpleSuggestion(String suggestion) {
    if (suggestion.contains("<match")) {
      return false;
    }
    final Matcher matcher = BACK_REFERENCE_PATTERN.matcher(suggestion);
    return !matcher.find();
  }

  private List<String> getSuggestionTokens(List<String> suggestions, Language language) {
    final List<String> tokens = new ArrayList<>();
    for (String suggestion : suggestions) {
      final List<String> suggestionTokens = language.getWordTokenizer().tokenize(suggestion);
      for (String suggestionToken : suggestionTokens) {
        if (!suggestionToken.trim().isEmpty()) {
          tokens.add(suggestionToken);
        }
      }
    }
    return tokens;
  }

}
