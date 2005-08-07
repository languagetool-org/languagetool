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
package de.danielnaber.languagetool.rules.en;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.danielnaber.languagetool.AnalyzedSentence;
import de.danielnaber.languagetool.rules.RuleMatch;

/**
 * A rule that matches commas not followed by a whitespace
 * and whitespace preceding commas.
 * 
 * @author Daniel Naber
 */
public class CommaWhitespaceRule extends EnglishRule {

  public String getId() {
    return "COMMA_WHITESPACE";
  }

  public String getDescription() {
    return "Use of whitespace after a comma";
  }

  public RuleMatch[] match(AnalyzedSentence text) {
    List ruleMatches = new ArrayList();
    List tokens = text.getTokens();
    String prevToken = "";
    int pos = 0;
    int prevPos = 0;
    // TODO: what about numbers?
    for (Iterator iter = tokens.iterator(); iter.hasNext();) {
      String token = (String) iter.next();
      pos += token.length();
      if (token.trim().equals(",") && prevToken.trim().equals("")) {
        String msg = "Put a space after the comma, but not before the comma.";
        RuleMatch ruleMatch = new RuleMatch(this, prevPos, prevPos+prevToken.length(), msg);
        ruleMatches.add(ruleMatch);
      }
      prevToken = token;
      prevPos = pos;
    }
    return (RuleMatch[])ruleMatches.toArray(new RuleMatch[0]);
  }

}
