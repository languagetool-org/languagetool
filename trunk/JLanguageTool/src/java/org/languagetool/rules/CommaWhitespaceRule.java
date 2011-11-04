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
package de.danielnaber.languagetool.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import de.danielnaber.languagetool.AnalyzedSentence;
import de.danielnaber.languagetool.AnalyzedTokenReadings;

/**
 * A rule that matches commas and closing parenthesis preceded by whitespace and
 * opening parenthesis followed by whitespace.
 * 
 * @author Daniel Naber
 */

public class CommaWhitespaceRule extends Rule {

  public CommaWhitespaceRule(final ResourceBundle messages) {
    super(messages);
    super.setCategory(new Category(messages.getString("category_misc")));
  }

  @Override
  public final String getId() {
    return "COMMA_PARENTHESIS_WHITESPACE";
  }

  @Override
  public final String getDescription() {
    return messages.getString("desc_comma_whitespace");
  }

  @Override
  public final RuleMatch[] match(final AnalyzedSentence text) {
    final List<RuleMatch> ruleMatches = new ArrayList<RuleMatch>();
    final AnalyzedTokenReadings[] tokens = text.getTokens();
    String prevToken = "";
    String prevPrevToken = "";
    boolean prevWhite = false;
    int pos = 0;
    int prevLen = 0;
    for (int i = 0; i < tokens.length; i++) {
      final String token = tokens[i].getToken();
      final boolean isWhite = tokens[i].isWhitespace() 
      || tokens[i].isFieldCode();
      pos += token.length();
      String msg = null;
      int fixLen = 0;
      String suggestionText = null;
      if (isWhite && isLeftBracket(prevToken)) {
        msg = messages.getString("no_space_after");
        suggestionText = prevToken;
        fixLen = 1;
      } else if (!isWhite && prevToken.equals(",") 
          && isNotQuoteOrHyphen(token) 
          && containsNoNumber(prevPrevToken) 
          && containsNoNumber(token)
          && !",".equals(prevPrevToken)) {                          
        msg = messages.getString("missing_space_after_comma");
        suggestionText = ", ";        
      } else if (prevWhite) {
        if (isRightBracket(token)) {
          msg = messages.getString("no_space_before");
          suggestionText = token;
          fixLen = 1;
        } else if (token.equals(",")) {
          msg = messages.getString("space_after_comma");
          suggestionText = ",";
          fixLen = 1;
          //exception for duplicated comma (we already have another rule for that)
          if (i + 1 < tokens.length
             && ",".equals(tokens[i + 1].getToken())) {
           msg = null; 
          }
        } else if (token.equals(".")) {
          msg = messages.getString("no_space_before_dot");
          suggestionText = ".";
          fixLen = 1;
          // exception case for figures such as ".5" and ellipsis
          if (i + 1 < tokens.length
              && isNumberOrDot(tokens[i + 1].getToken())) {
            msg = null;
          }
        }
      }
      if (msg != null) {
        final int fromPos = tokens[i - 1].getStartPos();
        final int toPos = tokens[i - 1].getStartPos() + fixLen + prevLen;
        // TODO: add some good short comment here
        final RuleMatch ruleMatch = new RuleMatch(this, fromPos, toPos, msg);
        ruleMatch.setSuggestedReplacement(suggestionText);
        ruleMatches.add(ruleMatch);
      }
      prevPrevToken = prevToken;
      prevToken = token;
      prevWhite = isWhite && !tokens[i].isFieldCode(); //OOo code before comma/dot
      prevLen = tokens[i].getToken().length();
    }

    return toRuleMatchArray(ruleMatches);
  }

  static boolean isNotQuoteOrHyphen(final String str) {
    if (str.length() == 1) {
      final char c = str.charAt(0);
      if (c =='\'' || c == '-' || c == '”' 
        || c =='’' || c == '"' || c == '“'
        || c == ',') {
        return false;
      }
    } else {
      if ("&quot".equals(str)) {
        return false;
      }
      return containsNoNumber(str);
    }
    return true;
  }

  static boolean isNumberOrDot(final String str) {
    final char c = str.charAt(0);
    return (c == '.' || Character.isDigit(c)); 
  }

  static boolean isLeftBracket(final String str) {
    if (str.length() == 0) {
      return false;
    }
    final char c = str.charAt(0);
    return (c == '(' || c == '[' || c == '{');
  }

  static boolean isRightBracket(final String str) {
    if (str.length() == 0) {
      return false;
    }
    final char c = str.charAt(0);
    return (c == ')' || c == ']' || c == '}');
  }

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
