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
package de.danielnaber.languagetool.rules.de;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import de.danielnaber.languagetool.AnalyzedSentence;
import de.danielnaber.languagetool.AnalyzedTokenReadings;
import de.danielnaber.languagetool.rules.Category;
import de.danielnaber.languagetool.rules.RuleMatch;

/**
 * Pr&uuml;ft, dass in Bindestrich-Komposita kein Leerzeichen eingef&uuml;gt wird (wie z.B. in 'Di&auml;ten- Erh&ouml;hung').
 *   
 * @author Daniel Naber
 */
public class DashRule extends GermanRule {

  public DashRule(final ResourceBundle messages) {
    if (messages != null)
      super.setCategory(new Category(messages.getString("category_misc")));
  }
  
  public String getId() {
    return "DE_DASH";
  }

  public String getDescription() {
    return "Keine Leerzeichen in Bindestrich-Komposita (wie z.B. in 'Diäten- Erhöhung')";
  }

  public RuleMatch[] match(final AnalyzedSentence text) {
    List<RuleMatch> ruleMatches = new ArrayList<RuleMatch>();
    AnalyzedTokenReadings[] tokens = text.getTokensWithoutWhitespace();
    int pos = 0;
    String prevToken = null;
    for (int i = 0; i < tokens.length; i++) {
      String token = tokens[i].getToken();
      if (token.trim().equals("")) {
        // ignore
        continue;
      } else {
        if (prevToken != null && !prevToken.equals("-") && prevToken.indexOf("--") == -1 && prevToken.endsWith("-")) {
          char firstChar = token.charAt(0);
          if (Character.isUpperCase(firstChar)) {
            String msg = "Möglicherweise fehlt ein 'und' oder es wurde nach dem Wort " +
                    "ein überflüssiges Leerzeichen eingefügt.";
            RuleMatch ruleMatch = new RuleMatch(this, tokens[i-1].getStartPos(),
                tokens[i-1].getStartPos()+prevToken.length()+1, msg);
            ruleMatch.setSuggestedReplacement(tokens[i-1].getToken());
            ruleMatches.add(ruleMatch);
          }
        }
      }
      prevToken = token;
      pos += token.length();
    }
    return toRuleMatchArray(ruleMatches);
  }

  public void reset() {
    // nothing
  }

}
