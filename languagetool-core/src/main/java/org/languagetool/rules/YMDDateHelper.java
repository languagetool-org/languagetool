package org.languagetool.rules;

import java.util.Calendar;
import java.util.Map;

public class YMDDateHelper {
  public YMDDateHelper() {
  }

  public Map<String, String> parseDate(Map<String, String> args) {
    java.lang.String dateString = args.get("date");
    if (dateString == null) {
      throw new IllegalArgumentException("Missing key 'date'");
    }
    java.lang.String[] parts = dateString.split("-");
    if (parts.length != 3) {
      throw new java.lang.RuntimeException("Expected date in format 'yyyy-mm-dd': '" + dateString + "'");
    }
    args.put("year", parts[0]);
    args.put("month", parts[1]);
    args.put("day", parts[2]);
    return args;
  }

  public RuleMatch correctDate(RuleMatch match, Map<String, String> args) {
    String year = args.get("year");
    String month = args.get("month");
    String day = args.get("day");
    int correctYear = Integer.parseInt(year) + 1;
    String correctDate = String.format("%d-%s-%s", correctYear, month, day);
    String message = match.getMessage()
            .replace("{realDate}", correctDate);
    return new RuleMatch(match.getRule(), match.getSentence(), match.getFromPos(),
            match.getToPos(), message, match.getShortMessage());
  }
}