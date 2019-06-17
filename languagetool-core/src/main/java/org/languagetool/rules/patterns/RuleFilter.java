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
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.Map;

/**
 * Filter rule matches after a PatternRule has matched already.
 * Can be used from the XML using the {@code filter} element.
 * @since 2.7 (changed from interface to abstract class in 3.2)
 */
public abstract class RuleFilter {

  /**
   * Returns the original rule match or a modified one, or {@code null}
   * if the rule match is filtered out.
   * @param arguments the resolved argument from the {@code args} attribute in the XML. Resolved
   *                  means that e.g. {@code \1} has been resolved to the actual string at that match position.
   * @param patternTokens those tokens of the text that correspond the matched pattern
   * @return {@code null} if this rule match should be removed, or any other RuleMatch (e.g. the one from
   *         the arguments) that properly describes the detected error
   */
  @Nullable
  public abstract RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, AnalyzedTokenReadings[] patternTokens);

  /** @since 3.2 */
  public boolean matches(Map<String, String> arguments, AnalyzedTokenReadings[] patternTokens) {
    RuleMatch fakeMatch = new RuleMatch(new FakeRule(), null, 0, 1, "(internal rule)");
    return acceptRuleMatch(fakeMatch, arguments, patternTokens) != null;
  }

  private static class FakeRule extends Rule {
    @Override public String getId() { return "FAKE-RULE-FOR-FILTER"; }
    @Override public String getDescription() { return "<none>"; }
    @Override public RuleMatch[] match(AnalyzedSentence sentence) throws IOException { return new RuleMatch[0]; }
  }
  
}
