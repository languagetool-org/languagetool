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
 * A rule that matches ".." (but not "..." etc) and ",,".
 * 
 * @author Daniel Naber
 */
public class DoublePunctuationRule extends Rule {

  public DoublePunctuationRule(final ResourceBundle messages) {
    super(messages);
    super.setCategory(new Category(messages.getString("category_misc")));
  }

  @Override
  public final String getId() {
    return "DOUBLE_PUNCTUATION";
  }

  @Override
  public final String getDescription() {
    return messages.getString("desc_double_punct");
  }

  @Override
  public final RuleMatch[] match(final AnalyzedSentence text) {
    final List<RuleMatch> ruleMatches = new ArrayList<RuleMatch>();
    final AnalyzedTokenReadings[] tokens = text.getTokens();
    int startPos = 0;
    int dotCount = 0;
    int commaCount = 0;
    for (int i = 0; i < tokens.length; i++) {
      final String token = tokens[i].getToken();
      String nextToken = null;
      if (i < tokens.length - 1) {
        nextToken = tokens[i + 1].getToken();
      }
      if (".".equals(token)) {
        dotCount++;
        commaCount = 0;
        startPos = tokens[i].getStartPos();
      } else if (",".equals(token)) {
        commaCount++;
        dotCount = 0;
        startPos = tokens[i].getStartPos();
      }
      if (dotCount == 2 && !".".equals(nextToken)) {
        final String msg = messages.getString("two_dots");
        final int fromPos = Math.max(0, startPos - 1);
        final RuleMatch ruleMatch = new RuleMatch(this, fromPos, startPos + 1,
            msg, messages.getString("double_dots_short"));
        ruleMatch.setSuggestedReplacement(".");
        ruleMatches.add(ruleMatch);
        dotCount = 0;
      } else if (commaCount == 2 && !",".equals(nextToken)) {
        final String msg = messages.getString("two_commas");
        final int fromPos = Math.max(0, startPos);
        final RuleMatch ruleMatch = new RuleMatch(this, fromPos, startPos + 1,
            msg, messages.getString("double_commas_short"));
        ruleMatch.setSuggestedReplacement(",");
        ruleMatches.add(ruleMatch);
        commaCount = 0;
      }
      if (!".".equals(token) && !",".equals(token)) {
        dotCount = 0;
        commaCount = 0;
      }
    }

    return toRuleMatchArray(ruleMatches);
  }

  @Override
  public void reset() {
    // nothing
  }

}
