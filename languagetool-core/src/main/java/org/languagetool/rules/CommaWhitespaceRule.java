/* LanguageTool, a natural language style checker
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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

import static org.languagetool.tools.StringTools.isEmpty;

/**
 * A rule that matches periods, commas and closing parenthesis preceded by whitespace and
 * opening parenthesis followed by whitespace.
 * 
 * @author Daniel Naber
 */
public class CommaWhitespaceRule extends Rule {

  public CommaWhitespaceRule(final ResourceBundle messages) {
    super(messages);
    super.setCategory(new Category(messages.getString("category_misc")));
    setLocQualityIssueType(ITSIssueType.Whitespace);
  }

  @Override
  public final String getId() {
    return "COMMA_PARENTHESIS_WHITESPACE";
  }

  @Override
  public final String getDescription() {
    return messages.getString("desc_comma_whitespace");
  }
  
  public String getCommaCharacter() {
	  return ",";
  }

  @Override
  public final RuleMatch[] match(final AnalyzedSentence sentence) {
    final List<RuleMatch> ruleMatches = new ArrayList<>();
    final AnalyzedTokenReadings[] tokens = sentence.getTokens();
    String prevToken = "";
    String prevPrevToken = "";
    boolean prevWhite = false;
    for (int i = 0; i < tokens.length; i++) {
      final String token = tokens[i].getToken();
      final boolean isWhitespace = tokens[i].isWhitespace() || StringTools.isNonBreakingWhitespace(token)
          || tokens[i].isFieldCode();
      String msg = null;
      String suggestionText = null;
      if (isWhitespace && isLeftBracket(prevToken)) {
        msg = messages.getString("no_space_after");
        suggestionText = prevToken;
      } else if (!isWhitespace && prevToken.equals(getCommaCharacter())
          && isNotQuoteOrHyphen(token)
          && containsNoNumber(prevPrevToken)
          && containsNoNumber(token)
          && !",".equals(prevPrevToken)) {
        msg = messages.getString("missing_space_after_comma");
        suggestionText = getCommaCharacter() + " " + tokens[i].getToken();
      } else if (prevWhite) {
        if (isRightBracket(token)) {
          msg = messages.getString("no_space_before");
          suggestionText = token;
        } else if (token.equals(getCommaCharacter())) {
          msg = messages.getString("space_after_comma");
          suggestionText = getCommaCharacter();
          // exception for duplicated comma (we already have another rule for that)
          if (i + 1 < tokens.length
              && getCommaCharacter().equals(tokens[i + 1].getToken())) {
            msg = null;
          }
        } else if (token.equals(".")) {
          msg = messages.getString("no_space_before_dot");
          suggestionText = ".";
          // exception case for figures such as ".5" and ellipsis
          if (i + 1 < tokens.length
              && isNumberOrDot(tokens[i + 1].getToken())) {
            msg = null;
          }
        }
      }
      if (msg != null) {
        final int fromPos = tokens[i - 1].getStartPos();
        final int toPos = tokens[i].getStartPos() + tokens[i].getToken().length();
        final RuleMatch ruleMatch = new RuleMatch(this, fromPos, toPos, msg);
        ruleMatch.setSuggestedReplacement(suggestionText);
        ruleMatches.add(ruleMatch);
      }
      prevPrevToken = prevToken;
      prevToken = token;
      prevWhite = isWhitespace && !tokens[i].isFieldCode(); // LO/OO code before comma/dot
    }

    return toRuleMatchArray(ruleMatches);
  }

  /** @deprecated will be made private (deprecated since 2.7) */
  static boolean isNotQuoteOrHyphen(final String str) {
    if (str.length() == 1) {
      final char c = str.charAt(0);
      if (c =='\'' || c == '-' || c == '”'
          || c =='’' || c == '"' || c == '“'
          || c == ',') {
        return false;
      }
    } else {
      return containsNoNumber(str);
    }
    return true;
  }

  /** @deprecated will be made private (deprecated since 2.7) */
  static boolean isNumberOrDot(final String str) {
    if (isEmpty(str)) {
      return false;
    }
    final char c = str.charAt(0);
    return c == '.' || Character.isDigit(c);
  }

  /** @deprecated will be made private (deprecated since 2.7) */ 
  static boolean isLeftBracket(final String str) {
    if (str.length() == 0) {
      return false;
    }
    final char c = str.charAt(0);
    return c == '(' || c == '[' || c == '{';
  }

  /** @deprecated will be made private (deprecated since 2.7) */
  static boolean isRightBracket(final String str) {
    if (str.length() == 0) {
      return false;
    }
    final char c = str.charAt(0);
    return c == ')' || c == ']' || c == '}';
  }

  /** @deprecated will be made private (deprecated since 2.7) */
  static boolean containsNoNumber(final String str) {
    for (int i = 0; i < str.length(); i++) {
      if (Character.isDigit(str.charAt(i))) {
        return false;
      }
    }
    return true;
  }

  @Override
  public void reset() {
    // nothing
  }

}
