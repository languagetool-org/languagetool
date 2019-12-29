/* LanguageTool, a natural language style checker
 * Copyright (C) 2019 Nataliia Stulova (s0nata.github.io)
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

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.tools.StringTools;

/**
 * A rule that matches quotation marks surrounded by whitespace.
 *
 * @author Nataliia Stulova
 * @since 4.8
 */
public class QuotesWhitespaceRule extends Rule {


  /** @since 4.8 */
  public QuotesWhitespaceRule(ResourceBundle messages, IncorrectExample incorrectExample, CorrectExample correctExample) {
    super(messages);
    super.setCategory(Categories.TYPOGRAPHY.getCategory(messages));
    setLocQualityIssueType(ITSIssueType.Whitespace);
    if (incorrectExample != null && correctExample != null) {
      addExamplePair(incorrectExample, correctExample);
    }
  }

  @Override
  public String getId() {
    return "QUOTES_WHITESPACE";
  }

  @Override
  public final String getDescription() {
    return messages.getString("desc_quotes_whitespace");
  }

  public String getCommaCharacter() {
    return ",";
  }

  @Override
  public final RuleMatch[] match(AnalyzedSentence sentence) {

    List<RuleMatch> ruleMatches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = sentence.getTokens();

    String prevToken = "";
    String prevPrevToken = "";
    for (int i = 0; i < tokens.length; i++) {
      String token = tokens[i].getToken();
      boolean isWhitespace = isWhitespaceToken(tokens[i]);
      String msg = null;
      String suggestionText = null;

      if (isWhitespace && isQuote(prevToken) && prevPrevToken.equals(" ")) {
      	  msg = messages.getString("no_space_around_quotes");
          suggestionText = "";

      }

      if (msg != null) {
        int fromPos = tokens[i - 1].getStartPos();
        int toPos = tokens[i].getEndPos();
        RuleMatch ruleMatch = new RuleMatch(this, sentence, fromPos, toPos, msg);
        ruleMatch.setSuggestedReplacement(suggestionText);
        ruleMatches.add(ruleMatch);
      }

      prevPrevToken = prevToken;
      prevToken = token;
    }

    return toRuleMatchArray(ruleMatches);
  }

  private static boolean isWhitespaceToken(AnalyzedTokenReadings token) {
	  return (   token.isWhitespace()
			  || StringTools.isNonBreakingWhitespace(token.getToken())
			  || token.isFieldCode()) && !token.equals("\u200B");
  }

  public static boolean isQuote(String str) {
	    if (str.length() == 1) {
	      char c = str.charAt(0);
	      if (c =='\'' || c == '"' || c =='’'
	          || c == '”' || c == '“'
	          || c == '«'|| c == '»') {
	        return true;
	      }
	    }
	    return false;
  }

}
