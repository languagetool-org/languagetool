package org.languagetool.rules;

import java.util.Map;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.patterns.RuleFilter;

public class WhitespaceCheckFilter extends RuleFilter  {

  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos,
      AnalyzedTokenReadings[] patternTokens) {
    String wsChar = getRequired("whitespaceChar", arguments);
    int pos = Integer.parseInt(getRequired("position", arguments));
    if (pos < 1 || pos > patternTokens.length) {
      throw new IllegalArgumentException("Wrong position in WhitespaceCheckFilter: " + pos);
    }
    if (!patternTokens[pos - 1].getWhitespaceBefore().equals(wsChar)) {
      return match;
    } else {
      return null;
    }
  }

  protected String getRequired(String key, Map<String, String> map) {
    String result = map.get(key);
    if (result == null) {
      throw new IllegalArgumentException("Missing key '" + key + "'");
    }
    return result;
  }

}
