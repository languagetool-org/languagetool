/* LanguageTool, a natural language style checker
 * Copyright (C) 2011 Michael Bryant
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;

public class LongSentenceRule extends Rule {

  public LongSentenceRule(final ResourceBundle messages) {
    super(messages);
    super.setCategory(new Category(messages.getString("category_misc")));
    setDefaultOff();
  }

  @Override
  public String getDescription() {
    return "Readability: sentence over 40 words";
  }

  @Override
  public String getId() {
    return "TOO_LONG_SENTENCE";
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence text) throws IOException {
    final List<RuleMatch> ruleMatches = new ArrayList<RuleMatch>();
    final AnalyzedTokenReadings[] tokens = text.getTokensWithoutWhitespace();
    final String msg = "Sentence is over 40 words long, consider revising.";
    int numWords = 0;
    int pos = 0;
    if (tokens.length < 41) {   // just a short-circuit
      return toRuleMatchArray(ruleMatches);
    } else {
      for (int i=0;i<tokens.length;i++) {
        String token = tokens[i].getToken();
        pos += token.length();  // won't match the whole offending sentence, but much of it
        if (!token.matches("[!-~]") && !tokens[i].isSentStart()) {
          numWords++;
        }
      }
    }
    if (numWords > 40) {
      RuleMatch ruleMatch = new RuleMatch(this,0,pos,msg);
      ruleMatches.add(ruleMatch);
    }
    return toRuleMatchArray(ruleMatches);

  }

  @Override
  public void reset() {
    // nothing here
  }

}