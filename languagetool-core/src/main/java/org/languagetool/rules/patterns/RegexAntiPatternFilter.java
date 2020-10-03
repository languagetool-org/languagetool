/* LanguageTool, a natural language style checker
 * Copyright (C) 2020 Daniel Naber (http://www.danielnaber.de)
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

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Filters matches that match a regex. Limitations: 1. antipatterns cannot contain spaces,
 * 2. The pipe (|) is used to delimit several patterns and cannot be used inside a pattern.
 * @since 5.2
 */
public class RegexAntiPatternFilter extends RegexRuleFilter {

  @Nullable
  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, AnalyzedSentence sentenceObj, Matcher patternMatcher) {
    String antiPatternStr = arguments.get("antipatterns");
    if (antiPatternStr == null) {
      throw new RuntimeException("Missing 'antiPatterns:' in 'args' in <filter> of rule " + match.getRule().getFullId());
    }
    String[] antiPatterns = antiPatternStr.split("\\|");
    for (String antiPattern : antiPatterns) {
      Pattern p = Pattern.compile(antiPattern);
      Matcher matcher = p.matcher(sentenceObj.getText());
      if (matcher.find()) {
        // partial overlap is enough to filter out a match:
        if (matcher.start() <= match.getToPos() && matcher.end() >= match.getToPos() ||
            matcher.start() <= match.getFromPos() && matcher.end() >= match.getFromPos()) {
          return null;
        }
      }
    }
    return match;
  }
}
