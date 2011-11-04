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
import de.danielnaber.languagetool.Language;

/**
 * Check if there is duplicated whitespace in a sentence.
 * Considers two spaces as incorrect, and proposes a single space instead.
 *    
 * @author Marcin Miłkowski
 */

public class WhitespaceRule extends Rule {

  public WhitespaceRule(final ResourceBundle messages, final Language language) {
    super(messages);
    super.setCategory(new Category(messages.getString("category_misc")));
  }

  @Override
  public final String getId() {
    return "WHITESPACE_RULE";
  }

  @Override
  public final String getDescription() {
    return messages.getString("desc_whitespacerepetition");
  }

  @Override
  public RuleMatch[] match(final AnalyzedSentence text) {
    final List<RuleMatch> ruleMatches = new ArrayList<RuleMatch>();
    final AnalyzedTokenReadings[] tokens = text.getTokens();
    boolean prevWhite = false;
    int prevLen = 0;
    int prevPos = 0;
    //note: we start from token 1
    //token no. 0 is guaranteed to be SENT_START
    int i = 1;
    while (i < tokens.length) {
      if (tokens[i].isWhitespace() && prevWhite && !tokens[i -1].isLinebreak()) {
        final int pos = tokens[i -1].getStartPos();
        while (i < tokens.length && tokens[i].isWhitespace()) {
          prevLen += tokens[i].getToken().length();
          i++;
        }
        final RuleMatch ruleMatch = new RuleMatch(this, prevPos, pos + prevLen, messages
            .getString("whitespace_repetition"));
        ruleMatch.setSuggestedReplacement(" ");
        ruleMatches.add(ruleMatch);
      }
      if (i < tokens.length) {
        prevWhite = tokens[i].isWhitespace();
        prevLen = tokens[i].getToken().length();
        prevPos = tokens[i].getStartPos();
        i++;
      }
    }
    return toRuleMatchArray(ruleMatches);
  }

  @Override
  public void reset() {
    // nothing
  }

}
