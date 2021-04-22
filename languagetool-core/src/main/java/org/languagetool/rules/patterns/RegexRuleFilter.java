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

/**
 * Filter rule matches after a RegexPatternRule has matched already.
 * Can be used from the XML using the {@code filter} element.
 * @since 5.2
 */
public abstract class RegexRuleFilter {

  /**
   * Returns the original rule match or a modified one, or {@code null}
   * if the rule match is filtered out.
   * @param arguments the resolved argument from the {@code args} attribute in the XML.
   * @return {@code null} if this rule match should be removed, or any other RuleMatch (e.g. the one from
   *         the arguments) that properly describes the detected error
   */
  @Nullable
  public abstract RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, AnalyzedSentence sentenceObj, Matcher patternMatcher);

}
