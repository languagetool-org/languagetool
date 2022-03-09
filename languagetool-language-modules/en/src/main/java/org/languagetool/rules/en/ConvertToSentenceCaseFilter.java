/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Jaume Ortolà i Font
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
package org.languagetool.rules.en;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Pattern;

import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.RuleFilter;
import org.languagetool.tools.StringTools;

public class ConvertToSentenceCaseFilter extends RuleFilter {

  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos,
      AnalyzedTokenReadings[] patternTokens) throws IOException {
    RuleMatch ruleMatch = match;
    boolean firstDone = false;
    StringBuilder replacement = new StringBuilder();
    for (int i = patternTokenPos; i < patternTokens.length; i++) {
      if (patternTokens[i].getStartPos() < match.getFromPos() || patternTokens[i].getEndPos() > match.getToPos()) {
        continue;
      }
      String normalizedCase = normalizedCase(patternTokens[i]);
      if (i+1<patternTokens.length && patternTokens[i+1].getToken().equals(".")) {
        if (normalizedCase.length()==1) {
          normalizedCase = normalizedCase.toUpperCase();
        } else if (normalizedCase.equals("corp")) {
          normalizedCase = "Corp";
        }
      }
      String tokenString = patternTokens[i].getToken();
      String tokenCapitalized = StringTools.uppercaseFirstChar(normalizedCase);
      if (!firstDone & !isPunctuation(tokenString) && !tokenString.isEmpty()) {
        firstDone = true;
        replacement.append(tokenCapitalized);
      } else {
        if (patternTokens[i].isWhitespaceBefore()) {
          replacement.append(" ");
        }
        replacement.append(normalizedCase);
      }
    }
    ruleMatch.setSuggestedReplacement(replacement.toString());
    return ruleMatch;
  }

  private boolean isPunctuation(String s) {
    return Pattern.matches("\\p{IsPunctuation}", s);
  }

  private String normalizedCase(AnalyzedTokenReadings atr) {
    String tokenLowercase = atr.getToken().toLowerCase();
    if (atr.hasTypographicApostrophe()) {
      tokenLowercase = tokenLowercase.replaceAll("'", "’");
    }
    if (tokenLowercase.equals("me")) {
      // exception: the lemma is "I"
      return tokenLowercase;
    }
    String tokenCapitalized = StringTools.uppercaseFirstChar(tokenLowercase);
    //boolean lemmaIsAllUppercase = false;
    boolean lemmaIsCapitalized = false;
    boolean lemmaIsLowercase = false;
    for (AnalyzedToken at : atr) {
      if (at.hasNoTag() || at.getLemma() == null) {
        return tokenCapitalized;
      }
      // for multi-words lemmas, take the first word
      String lemma = at.getLemma().split(" ")[0];
      //lemmaIsAllUppercase = lemmaIsAllUppercase || StringTools.isAllUppercase(lemma);
      lemmaIsCapitalized = lemmaIsCapitalized || StringTools.isCapitalizedWord(lemma);
      lemmaIsLowercase = lemmaIsLowercase || !StringTools.isNotAllLowercase(lemma);
    }
    if (lemmaIsLowercase) {
      return tokenLowercase;
    }
    if (lemmaIsCapitalized) {
      return tokenCapitalized;
    }
    return atr.getToken();
  }

}
