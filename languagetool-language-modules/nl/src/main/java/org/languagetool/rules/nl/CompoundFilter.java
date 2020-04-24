
/* LanguageTool, a natural language style checker
 * Copyright (C) 2019 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.nl;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.RuleFilter;

import java.util.Map;

import static java.lang.Character.isLowerCase;
import static java.lang.Character.isUpperCase;

public class CompoundFilter extends RuleFilter {

  private final static String spelledWords = "(abc|adv|aed|apk|b2b|bh|bhv|bso|btw|bv|cao|cd|cfk|ckv|cv|dc|dj|dtp|dvd|fte|gft|ggo|ggz|gm|gmo|gps|gsm|hbo|" +
           "hd|hiv|hr|hrm|hst|ic|ivf|kmo|lcd|lp|lpg|lsd|mbo|mdf|mkb|mms|msn|mt|ngo|nv|ob|ov|ozb|p2p|pc|pcb|pdf|pk|pps|" +
           "pr|pvc|roc|rvs|sms|tbc|tbs|tl|tv|uv|vbo|vj|vmbo|vsbo|vwo|wc|wo|xtc|zzp)";

  @Nullable
  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos, AnalyzedTokenReadings[] patternTokens) {
    String word1 = arguments.get("word1");
    String word2 = arguments.get("word2");
    char lastChar = word1.charAt(word1.length() - 1);
    char firstChar = word2.charAt(0);
    String connection = lastChar + String.valueOf(firstChar);
    String repl;
    if (StringUtils.containsAny(connection, "aa", "ae", "ai", "ao", "au", "ee", "ei", "eu", "ie", "ii", "oe", "oi", "oo", "ou", "ui", "uu", "ij")) {
      repl = word1 + '-' + word2;
    } else if (isUpperCase(firstChar) && isLowerCase(lastChar)) {
      repl = word1 + '-' + word2;
    } else if (isUpperCase(lastChar) && isLowerCase(firstChar)) {
      repl = word1 + '-' + word2;
    } else if (word1.matches("(^|.+-)?" + spelledWords) || word2.matches(spelledWords + "(-.+|$)?")) {
      repl = word1 + '-' + word2;
    } else if (word1.matches(".+-[a-z]$") || word2.matches("^[a-z]-.+")) {
      repl = word1 + '-' + word2;
    } else {
      repl = word1 + word2;
    }
    String message = match.getMessage().replaceAll("<suggestion>.*?</suggestion>", "<suggestion>" + repl + "</suggestion>");
    String shortMessage = match.getShortMessage().replaceAll("<suggestion>.*?</suggestion>", "<suggestion>" + repl + "</suggestion>");
    RuleMatch newMatch = new RuleMatch(match.getRule(), match.getSentence(), match.getFromPos(), match.getToPos(), message, shortMessage);
    newMatch.setSuggestedReplacement(repl);
    return newMatch;
  }

  /*
  @Nullable
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos, AnalyzedTokenReadings[] patternTokens) {
    String words = arguments.get("words");
    //System.out.println("****"+words+"******");
    String[] parts = s.split(" ");
    String compound = parts[0];
    for (int i = 1; i < parts.length; i++) {
      String word2 = parts[i];
      char lastChar = compound.charAt(compound.length() - 1);
      char firstChar = word2.charAt(0);
      String connection = lastChar + String.valueOf(firstChar);
      if (StringUtils.containsAny(connection, "aa", "ae", "ai", "ao", "au", "ee", "ei", "eu", "ie", "ii", "oe", "oi", "oo", "ou", "ui", "uu", "ij")) {
        compound = compound + '-' + word2;
      } else if (isUpperCase(firstChar) && isLowerCase(lastChar)) {
        compound = compound + '-' + word2;
      } else if (isUpperCase(lastChar) && isLowerCase(firstChar)) {
        compound = compound + '-' + word2;
      } else if (compound.matches("(^|.+-)?" + spelledWords) || word2.matches(spelledWords + "(-.+|$)?")) {
        compound = compound + '-' + word2;
      } else if (compound.matches(".+-[a-z]$") || word2.matches("^[a-z]-.+")) {
        compound = compound + '-' + word2;
      } else {
        compound = compound + word2;
      }
    }
    //System.out.println("******"+compound+"******");
    String message = match.getMessage().replaceAll("<suggestion>.*?</suggestion>", "<suggestion>" + compound + "</suggestion>");
    String shortMessage = match.getShortMessage().replaceAll("<suggestion>.*?</suggestion>", "<suggestion>" + compound + "</suggestion>");
    RuleMatch newMatch = new RuleMatch(match.getRule(), match.getSentence(), match.getFromPos(), match.getToPos(), message, shortMessage);
    newMatch.setSuggestedReplacement(compound);
    return newMatch;
  }*/

}
