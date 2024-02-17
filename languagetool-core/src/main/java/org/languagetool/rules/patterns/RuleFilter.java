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
import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.util.List;
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
   *
   * @param arguments      the resolved argument from the {@code args} attribute in the XML. Resolved
   *                       means that e.g. {@code \1} has been resolved to the actual string at that match position.
   * @param patternTokens  those tokens of the text that correspond the matched pattern
   * @return {@code null} if this rule match should be removed, or any other RuleMatch (e.g. the one from
   * the arguments) that properly describes the detected error
   */
  @Nullable
  public abstract RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos, AnalyzedTokenReadings[] patternTokens, List<Integer> tokenPositions) throws IOException;

  /** @since 3.2 */
  public boolean matches(Map<String, String> arguments, AnalyzedTokenReadings[] patternTokens, int firstMatchToken, List<Integer> tokenPositions) throws IOException {
    RuleMatch fakeMatch = new RuleMatch(new FakeRule(), null, 0, 1, "(internal rule)");
    return acceptRuleMatch(fakeMatch, arguments, firstMatchToken, patternTokens, tokenPositions) != null;
  }

  private static class FakeRule extends Rule {
    @Override public String getId() { return "FAKE-RULE-FOR-FILTER"; }
    @Override public String getDescription() { return "<none>"; }
    @Override public RuleMatch[] match(AnalyzedSentence sentence) throws IOException { return new RuleMatch[0]; }
  }

  protected String getRequired(String key, Map<String, String> map) {
    String result = map.get(key);
    if (result == null) {
      throw new IllegalArgumentException("Missing key '" + key + "'");
    }
    return result;
  }

  protected String getOptional(String key, Map<String, String> map) {
    return map.get(key);
  }

  protected String getOptional(String key, Map<String, String> map, String defaultValue) {
    String value = map.get(key);
    if (value == null) {
      return defaultValue;
    }
    return value;
  }

  protected int getPosition(String fromStr, AnalyzedTokenReadings[] patternTokens, RuleMatch match) {
    int i;
    if (fromStr.startsWith("marker")) {
      i = 0;
      while (i < patternTokens.length && patternTokens[i].getStartPos() < match.getFromPos()
        || patternTokens[i].isSentenceStart()) {
        i++;
      }
      i++;
      if (fromStr.length()>6) {
        i += Integer.parseInt(fromStr.replace("marker", ""));
      }
    } else {
      i = Integer.parseInt(fromStr);
    }
    if (i < 1 || i > patternTokens.length) {
      throw new IllegalArgumentException("RuleFilter: Index out of bounds in "
        + match.getRule().getFullId() + ", value: " + fromStr);
    }
    return i - 1;
  }

  protected boolean isMatchAtSentenceStart(AnalyzedTokenReadings[] tokens, RuleMatch match) {
    int i = 0;
    while (i < tokens.length && tokens[i].getStartPos() < match.getFromPos()) {
      i++;
    }
    while (i > 0 && StringTools.isPunctuationMark(tokens[i].getToken())) {
      i--;
    }
    return i == 0;
  }

  // when there's a 'skip', we need to adapt the reference number
  protected int getSkipCorrectedReference(List<Integer> tokenPositions, int refNumber) {
    if (refNumber < 0) {
      return refNumber;
    }
    int correctedRef = 0;
    int i = 0;
    for (int tokenPosition : tokenPositions) {
      if (i++ >= refNumber) {
        break;
      }
      correctedRef += tokenPosition;
    }
    return correctedRef - 1;
  }

}
