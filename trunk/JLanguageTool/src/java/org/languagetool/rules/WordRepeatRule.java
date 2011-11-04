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
 * Check if a word is repeated twice, e.g. "the the".
 *   
 * @author Daniel Naber
 */
public class WordRepeatRule extends Rule {

  public WordRepeatRule(final ResourceBundle messages, final Language language) {
    super(messages);
    super.setCategory(new Category(messages.getString("category_misc")));
  }

  /**
   * Implement this method to return <code>true</code> if there's
   * a potential word repetition at the current position should be ignored,
   * i.e. if no error should be created.
   * 
   * @param tokens the tokens of the sentence currently being checked
   * @param position the current position in the tokens 
   * @return this implementation always returns false
   */
  public boolean ignore(final AnalyzedTokenReadings[] tokens, final int position) {
    return false;
  }

  @Override
  public String getId() {
    return "WORD_REPEAT_RULE";
  }

  @Override
  public String getDescription() {
    return messages.getString("desc_repetition");
  }

  @Override
  public RuleMatch[] match(final AnalyzedSentence text) {
    final List<RuleMatch> ruleMatches = new ArrayList<RuleMatch>();
    final AnalyzedTokenReadings[] tokens = text.getTokensWithoutWhitespace();
    String prevToken = "";
    //note: we start from token 1
    //token no. 0 is guaranteed to be SENT_START
    for (int i = 1; i < tokens.length; i++) {
      final String token = tokens[i].getToken();
      // avoid "..." etc. to be matched:
      boolean isWord = true;
      if (token.length() == 1) {
        final char c = token.charAt(0);
        if (!Character.isLetter(c)) {
          isWord = false;
        }
      }
      final boolean isException = ignore(tokens, i);
      if (isWord && prevToken.toLowerCase().equals(token.toLowerCase()) && !isException) {
        final String msg = messages.getString("repetition");
        final int prevPos = tokens[i - 1].getStartPos();
        final int pos = tokens[i].getStartPos();
        final RuleMatch ruleMatch = new RuleMatch(this, prevPos, pos+prevToken.length(), msg, 
            messages.getString("desc_repetition_short"));
        ruleMatch.setSuggestedReplacement(prevToken);
        ruleMatches.add(ruleMatch);
      }
      prevToken = token;
    }
    return toRuleMatchArray(ruleMatches);
  }

  @Override
  public void reset() {
    // nothing
  }

}
