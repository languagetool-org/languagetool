package org.languagetool.rules;

import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.patterns.RuleFilter;

import java.util.Map;

/**
 * Checks whether the date range is valid, i.e., that the starting
 * date happens before the end date. The check is trivial: simply check
 * whether the first integer number is smaller than the second, so this
 * can be implemented for any language.
 *
 * The parameters used in the XML file are called 'x' and 'y'.
 *
 */
public class DateRangeChecker extends RuleFilter {
  @Nullable
  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, AnalyzedTokenReadings[] patternTokens) {
    try {
      int x = Integer.parseInt(arguments.get("x"));
      int y = Integer.parseInt(arguments.get("y"));
      if (x >= y) {
        return new RuleMatch(match.getRule(), match.getFromPos(), match.getToPos(), match.getMessage(), match.getShortMessage());
      }
    } catch (IllegalArgumentException ignore) {
      // if something's fishy with the number – ignore it silently,
      // it's not a date range
      return null;
    }
    return null;
  }
}
