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
package org.languagetool.rules.de;

import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.*;

/**
 * Prüft, dass in Bindestrich-Komposita kein Leerzeichen eingefügt wird (wie z.B. in 'Diäten- Erhöhung').
 *   
 * @author Daniel Naber
 */
public class DashRule extends Rule {

  public DashRule(ResourceBundle messages) {
    super.setCategory(Categories.COMPOUNDING.getCategory(messages));
    addExamplePair(Example.wrong("Bundestag beschließt <marker>Diäten- Erhöhung</marker>"),
                   Example.fixed("Bundestag beschließt <marker>Diäten-Erhöhung</marker>"));
  }

  @Override
  public String getId() {
    return "DE_DASH";
  }

  @Override
  public String getDescription() {
    return "Keine Leerzeichen in Bindestrich-Komposita (wie z.B. in 'Diäten- Erhöhung')";
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
    String prevToken = null;
    for (int i = 0; i < tokens.length; i++) {
      String token = tokens[i].getToken();
      if (prevToken != null &&
          prevToken.endsWith("-") &&
          !prevToken.equals("-") &&
          !prevToken.contains("--") &&
          !prevToken.contains("–-")    // first char is some special kind of dash, found in Wikipedia
          ) {
        char firstChar = token.charAt(0);
        if (Character.isUpperCase(firstChar)) {
          if (!StringUtils.equalsAny(token, "UND", "ODER", "BZW") ) {  // ignore "NORD- UND SÜDKOREA"
            String msg = "Möglicherweise fehlt ein 'und' oder ein Komma, oder es wurde nach dem Wort " +
                    "ein überflüssiges Leerzeichen eingefügt. Eventuell haben Sie auch versehentlich einen Bindestrich statt eines Punktes eingefügt.";
            String shortMsg = "Fehlendes 'und' oder Komma oder überflüssiges Leerzeichen?";
            int fromPos = tokens[i - 1].getStartPos();
            RuleMatch ruleMatch = new RuleMatch(this, sentence, fromPos,
                    tokens[i].getEndPos(), msg, shortMsg);
            ruleMatch.addSuggestedReplacement(tokens[i-1].getToken() + tokens[i].getToken());
            if (StringUtils.countMatches(tokens[i-1].getToken(), "-") + StringUtils.countMatches(tokens[i].getToken(), "-") <= 1) {
              ruleMatch.addSuggestedReplacement(tokens[i-1].getToken() + ", " + tokens[i].getToken());
            }
            ruleMatches.add(ruleMatch);
          }
        }
      }      
      prevToken = token;
    }
    return toRuleMatchArray(ruleMatches);
  }

}
