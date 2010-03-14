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

// TODO: add logic to check missing whitespace before ([{
// and after )}]
public class CommaWhitespaceRule extends Rule {

  public CommaWhitespaceRule(final ResourceBundle messages) {
    super(messages);
    super.setCategory(new Category(messages.getString("category_misc")));
  }

  public final String getId() {
    return "COMMA_PARENTHESIS_WHITESPACE";
  }

  public final String getDescription() {
    return messages.getString("desc_comma_whitespace");
  }

  public final RuleMatch[] match(final AnalyzedSentence text) {
    final List<RuleMatch> ruleMatches = new ArrayList<RuleMatch>();
    final AnalyzedTokenReadings[] tokens = text.getTokens();
    String prevToken = "";
    boolean prevWhite = false;
    int pos = 0;
    int prevLen = 0;
    for (int i = 0; i < tokens.length; i++) {
      final String token = tokens[i].getToken().trim();
      final boolean isWhite = tokens[i].isWhitespace() 
          || tokens[i].isFieldCode();
      pos += token.length();
      String msg = null;
      int fixLen = 0;
      String suggestionText = null;
      if (isWhite && prevToken.equals("(")) {
        msg = messages.getString("no_space_after");
        suggestionText = "(";
        fixLen = 1;
      } else if (token.equals(")") && prevWhite) {
        msg = messages.getString("no_space_before");
        suggestionText = ")";
        fixLen = 1;
      } else if (prevToken.equals(",") && !isWhite && !token.equals("'")
          && !token.equals("&quot") && !token.equals("”") && !token.equals("’")
          && !token.equals("\"") && !token.equals("“")
          && !token.matches(".*\\d.*") && !token.equals("-")) {
        msg = messages.getString("missing_space_after_comma");

        suggestionText = ", ";
      } else if (token.equals(",") && prevWhite) {
        msg = messages.getString("space_after_comma");
        suggestionText = ",";
        fixLen = 1;
      } else if (token.equals(".") && prevWhite) {
        msg = messages.getString("no_space_before_dot");
        suggestionText = ".";
        fixLen = 1;
        // exception case for figures such as ".5" and ellipsis
        if (i + 1 < tokens.length
            && tokens[i + 1].getToken().matches("\\d.*|\\.")) {
          msg = null;
        }
      }
      if (msg != null) {
        final int fromPos = tokens[i - 1].getStartPos();
        final int toPos = tokens[i - 1].getStartPos() + fixLen + prevLen;
        // TODO: add some good short comment here
        final RuleMatch ruleMatch = new RuleMatch(this, fromPos, toPos, msg);
        if (suggestionText != null) {
          ruleMatch.setSuggestedReplacement(suggestionText);
        }
        ruleMatches.add(ruleMatch);
      }
      prevToken = token;
      prevWhite = isWhite && !tokens[i].isFieldCode(); //OOo code before comma/dot
      prevLen = tokens[i].getToken().length();
    }

    return toRuleMatchArray(ruleMatches);
  }

  public void reset() {
    // nothing
  }

}
