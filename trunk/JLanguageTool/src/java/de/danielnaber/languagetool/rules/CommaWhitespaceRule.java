/* JLanguageTool, a natural language style checker 
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

import de.danielnaber.languagetool.AnalyzedSentence;
import de.danielnaber.languagetool.AnalyzedToken;
import de.danielnaber.languagetool.Language;

/**
 * A rule that matches commas and closing parenthesis preceeded by whitespace
 * and opening parenthesis followed by whitespace.
 * 
 * @author Daniel Naber
 */
public class CommaWhitespaceRule extends Rule {

  public String getId() {
    return "COMMA__PARENTHESIS_WHITESPACE";
  }

  public String getDescription() {
    return "Use of whitespace before comma and before/after parentheses";
  }

  public Language[] getLanguages() {
    return new Language[] { Language.ENGLISH, Language.GERMAN };
  }

  public RuleMatch[] match(AnalyzedSentence text) {
    List ruleMatches = new ArrayList();
    AnalyzedToken[] tokens = text.getTokens();
    String prevToken = "";
    int pos = 0;
    int prevPos = 0;
    // TODO: find error in "neu definierte,spielt ihr" -> but what about numbers?
    for (int i = 0; i < tokens.length; i++) {
      String token = tokens[i].getToken();
      pos += token.length();
      String msg = null;
      int fixPos = 0;
      if (token.trim().equals("") && prevToken.trim().equals("(")) {
        msg = "Don't put a space after the opening parenthesis.";
      } else if (token.trim().equals(")") && prevToken.trim().equals("")) {
        msg = "Don't put a space before the closing parenthesis.";
        fixPos = -1;
      } else if (token.trim().equals(",") && prevToken.trim().equals("")) {
        msg = "Put a space after the comma, but not before the comma.";
        fixPos = -1;
      }
      if (msg != null) {
        RuleMatch ruleMatch = new RuleMatch(this, prevPos+fixPos, prevPos+fixPos+prevToken.length(), msg);
        ruleMatches.add(ruleMatch);
      }
      prevToken = token;
      prevPos = pos;
    }
    return toRuleMatchArray(ruleMatches);
  }

  public void reset() {
    // nothing
  }

}
