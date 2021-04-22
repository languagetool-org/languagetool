/* LanguageTool, a natural language style checker
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.patterns;

import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedSentence;
import org.languagetool.rules.RuleMatch;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * Evaluates a {@link RegexRuleFilter} for {@link RegexPatternRule}s.
 * @since 5.2
 */
public class RegexRuleFilterEvaluator {

  private final RegexRuleFilter filter;

  public RegexRuleFilterEvaluator(RegexRuleFilter filter) {
    this.filter = filter;
  }

  @Nullable
  public RuleMatch runFilter(String filterArgs, RuleMatch ruleMatch, AnalyzedSentence sentenceObj, Matcher patternMatcher) {
    Map<String,String> args = getResolvedArguments(filterArgs);
    return filter.acceptRuleMatch(ruleMatch, args, sentenceObj, patternMatcher);
  }

  private Map<String,String> getResolvedArguments(String filterArgs) {
    Map<String,String> result = new HashMap<>();
    String[] arguments = filterArgs.split("\\s+");
    for (String arg : arguments) {
      int delimPos = arg.indexOf(':');
      if (delimPos == -1) {
        throw new RuntimeException("Invalid syntax for key/value, expected 'key:value', got: '" + arg + "'");
      }
      String key = arg.substring(0, delimPos);
      String val = arg.substring(delimPos + 1);
      result.put(key, val);
    }
    return result;
  }

}
