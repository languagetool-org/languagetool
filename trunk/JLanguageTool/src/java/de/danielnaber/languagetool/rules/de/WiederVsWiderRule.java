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
 * Check incorrect use of "spiegelt ... wider", namely using "wieder" instead
 * of "wider", e.g. in "Das spiegelt die Situation wieder" (incorrect).
 *   
 * @author Daniel Naber
 */
public class WiederVsWiderRule extends GermanRule {

  public WiederVsWiderRule(ResourceBundle messages) {
    if (messages != null)
      super.setCategory(new Category(messages.getString("category_typo")));
  }
  
  @Override
  public String getId() {
    return "DE_WIEDER_VS_WIDER";
  }

  @Override
  public String getDescription() {
    return "Möglicher Tippfehler 'spiegeln ... wieder(wider)'";
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence text) {
    final List<RuleMatch> ruleMatches = new ArrayList<RuleMatch>();
    final AnalyzedTokenReadings[] tokens = text.getTokens();
    int pos = 0;
    boolean foundSpiegelt = false;
    boolean foundWieder = false;
    boolean foundWider = false;
    for (AnalyzedTokenReadings token1 : tokens) {
      final String token = token1.getToken();
      if (token.trim().equals("")) {
        // ignore
      } else {
        if (token.equalsIgnoreCase("spiegelt") || token.equalsIgnoreCase("spiegeln") || token.equalsIgnoreCase("spiegelte")
                || token.equalsIgnoreCase("spiegelten") || token.equalsIgnoreCase("spiegelst")) {
          foundSpiegelt = true;
        } else if (token.equalsIgnoreCase("wieder") && foundSpiegelt) {
          foundWieder = true;
        } else if (token.equalsIgnoreCase("wider") && foundSpiegelt) {
          foundWider = true;
        }
        if (foundSpiegelt && foundWieder && !foundWider) {
          final String msg = "'wider' in 'widerspiegeln' wird mit 'i' statt mit 'ie' " +
                  "geschrieben, z.B. 'Das spiegelt die Situation gut wider.'";
          final RuleMatch ruleMatch = new RuleMatch(this, pos, pos + token.length(), msg);
          ruleMatch.setSuggestedReplacement("wider");
          ruleMatches.add(ruleMatch);
          foundSpiegelt = false;
          foundWieder = false;
          foundWider = false;
        }
      }
      pos += token.length();
    }
    return toRuleMatchArray(ruleMatches);
  }

  @Override
  public void reset() {
    // nothing
  }

}
