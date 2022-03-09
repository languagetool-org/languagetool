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

package org.languagetool.rules;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Pattern;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.patterns.RuleFilter;
import org.languagetool.tools.StringTools;


public class AddCommasFilter extends RuleFilter {
  
  private static final Pattern OPENING_QUOTES = Pattern.compile("[«“\"‘'„¿¡]", Pattern.DOTALL);
  
  
  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos,
      AnalyzedTokenReadings[] patternTokens) throws IOException {
   
    // for patterns ", aun así" suggest "; aun así," and ", aun así,"
    String suggestSemicolon = getOptional("suggestSemicolon", arguments);
    boolean bSuggestSemicolon = false;
    if (suggestSemicolon != null && suggestSemicolon.equalsIgnoreCase("true")) {
      bSuggestSemicolon = true;
    }

    AnalyzedTokenReadings[] tokens = match.getSentence().getTokensWithoutWhitespace();
    int postagFrom = 1;
    while (postagFrom < tokens.length && tokens[postagFrom].getStartPos() < match.getFromPos()) {
      postagFrom++;
    }
    int postagTo = postagFrom;
    while (postagTo < tokens.length && tokens[postagTo].getEndPos() < match.getToPos()) {
      postagTo++;
    }
    boolean beforeOK = (postagFrom == 1) || StringTools.isPunctuationMark(tokens[postagFrom - 1].getToken())
        || StringTools.isCapitalizedWord(tokens[postagFrom].getToken());
    boolean afterOK = !(postagTo + 1 > tokens.length - 1)
        && StringTools.isPunctuationMark(tokens[postagTo + 1].getToken())
        && !(tokens[postagTo + 1].isWhitespaceBefore() && OPENING_QUOTES.matcher(tokens[postagTo + 1].getToken()).matches());
    if (beforeOK && afterOK) {
      return null;
    }
    RuleMatch newMatch = null;
    if (bSuggestSemicolon && tokens[postagFrom - 1].getToken().equals(",") && !afterOK) {
      newMatch = new RuleMatch(match.getRule(), match.getSentence(), tokens[postagFrom - 1].getStartPos(),
          tokens[postagTo].getEndPos(), match.getMessage(), match.getShortMessage());
      newMatch.addSuggestedReplacement(
          "; " + match.getSentence().getText().substring(match.getFromPos(), match.getToPos()) + ",");
      newMatch.addSuggestedReplacement(
          ", " + match.getSentence().getText().substring(match.getFromPos(), match.getToPos()) + ",");
    } else if (beforeOK && !afterOK) {
      newMatch = new RuleMatch(match.getRule(), match.getSentence(), tokens[postagTo].getStartPos(), match.getToPos(),
          match.getMessage(), match.getShortMessage());
      newMatch.setSuggestedReplacement(tokens[postagTo].getToken() + ",");
    } else if (!beforeOK && afterOK) {
      int startPos = tokens[postagFrom].getStartPos();
      if (tokens[postagFrom].isWhitespaceBefore()) {
        startPos--;
      }
      newMatch = new RuleMatch(match.getRule(), match.getSentence(), startPos, tokens[postagFrom].getEndPos(),
          match.getMessage(), match.getShortMessage());
      newMatch.setSuggestedReplacement(", " + tokens[postagFrom].getToken());
    } else if (!beforeOK && !afterOK) {
      int startPos = tokens[postagFrom].getStartPos();
      if (tokens[postagFrom].isWhitespaceBefore()) {
        startPos--;
      }
      newMatch = new RuleMatch(match.getRule(), match.getSentence(), startPos, tokens[postagTo].getEndPos(),
          match.getMessage(), match.getShortMessage());
      newMatch.setSuggestedReplacement(
          ", " + match.getSentence().getText().substring(match.getFromPos(), match.getToPos()) + ",");
    }
    return newMatch;
  }

}
