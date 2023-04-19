/* LanguageTool, a natural language style checker 
 * Copyright (C) 2021 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.ca;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.RuleFilter;
import org.languagetool.tools.StringTools;

/*
 * Add the pronoun "en" in the required place with all the necessary transformations, 
 * including moving the <marker> positions.
 */

public class AddPronounEnFilter extends RuleFilter {

  Pattern pApostropheNeeded = Pattern.compile("h?[aeiouàèéíòóú].*", Pattern.CASE_INSENSITIVE);

  private static Map<String, String> transform1 = new HashMap<>();
  static {
    transform1.put("m'", "me n'");
    transform1.put("t'", "te n'");
    transform1.put("s'", "se n'");
    transform1.put("ens", "ens n'");
    transform1.put("us", "us n'");
    transform1.put("vos", "vos n'");
    transform1.put("li", "li n'");
    transform1.put("els", "els n'");
    transform1.put("se m'", "se me n'");
    transform1.put("se t'", "se te n'");
    transform1.put("se li", "se li n'");
    transform1.put("se'ns", "se'ns n'");
    transform1.put("se us", "se us n'");
    transform1.put("se vos", "se vos n'");
    transform1.put("se'ls", "se'ls n'");
    transform1.put("hi", "n'hi");
    transform1.put("", "n'");
  }

  private static Map<String, String> transform2 = new HashMap<>();
  static {
    transform2.put("em", "me'n");
    transform2.put("et", "te'n");
    transform2.put("es", "se'n");
    transform2.put("ens", "ens en");
    transform2.put("us", "us en");
    transform1.put("li", "li'n");
    transform1.put("els", "els en");
    transform2.put("se'm", "se me'n");
    transform2.put("se't", "se te'n");
    transform2.put("se li", "se li'n");
    transform2.put("se'ns", "se'ns en");
    transform2.put("se us", "se us en");
    transform2.put("se vos", "se vos en");
    transform2.put("se'ls", "se'ls en");
    transform2.put("hi", "n'hi");
    transform2.put("", "en");
  }

  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos,
      AnalyzedTokenReadings[] patternTokens) throws IOException {

    int posWord = 0;
    AnalyzedTokenReadings[] tokens = match.getSentence().getTokensWithoutWhitespace();
    while (posWord < tokens.length
        && (tokens[posWord].getStartPos() < match.getFromPos() || tokens[posWord].isSentenceStart())) {
      posWord++;
    }
    int toLeft = 0;
    boolean done = false;
    String firstVerb = "";
    int firstVerbPos = 0;
    while (!done && posWord - toLeft > 0) {
      AnalyzedTokenReadings currentTkn = tokens[posWord - toLeft];
      String currentTknStr = currentTkn.getToken();
      if (currentTkn.matchesPosTagRegex("V.*|P0.{6}|PP3CN000|PP3NN000|PP3..A00|PP3CP000|PP3CSD00")
          || currentTknStr.equalsIgnoreCase("de") || currentTknStr.equalsIgnoreCase("d'")) {
        if (currentTkn.hasPosTagStartingWith("V")) {
          firstVerb = currentTknStr;
          firstVerbPos = toLeft;
        }
        toLeft++;
      } else {
        done = true;
        if (toLeft > 0) {
          toLeft--;
        }
      }
    }
    StringBuilder sb = new StringBuilder();
    for (int i = posWord - toLeft; i < posWord - firstVerbPos; i++) {
      sb.append(tokens[i].getToken());
      if (tokens[i + 1].isWhitespaceBefore()) {
        sb.append(" ");
      }
    }
    String pronounsStr = sb.toString().trim();
    sb = new StringBuilder();
    for (int i = posWord - firstVerbPos; i <= posWord; i++) {
      sb.append(tokens[i].getToken());
      if (tokens[i + 1].isWhitespaceBefore()) {
        sb.append(" ");
      }
    }
    String verbStr = sb.toString().trim();
    String replacement = "";
    Map<String, String> transform;
    String between = "";
    if (pApostropheNeeded.matcher(firstVerb).matches()) {
      transform = transform1;
    } else {
      transform = transform2;
      between = " ";
    }
    String pronounsReplacement = transform.get(pronounsStr.toLowerCase());
    if (pronounsReplacement != null) {
      replacement = StringTools.preserveCase(pronounsReplacement, pronounsStr) + between + verbStr;
    }
    if (replacement.isEmpty()) {
      return null;
    }
    RuleMatch ruleMatch = new RuleMatch(match.getRule(), match.getSentence(), tokens[posWord - toLeft].getStartPos(),
        match.getToPos(), match.getMessage(), match.getShortMessage());
    ruleMatch.setType(match.getType());
    ruleMatch.setSuggestedReplacement(replacement);
    return ruleMatch;
  }

}
