/* LanguageTool, a natural language style checker 
 * Copyright (C) 2011 Daniel Naber (http://www.danielnaber.de)
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
package de.danielnaber.languagetool.rules.km;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import de.danielnaber.languagetool.AnalyzedSentence;
import de.danielnaber.languagetool.AnalyzedTokenReadings;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.rules.Rule;
import de.danielnaber.languagetool.rules.RuleMatch;
import de.danielnaber.languagetool.rules.Category;

/**
 * Check if a word is repeated twice in Khmer, e.g. the equivalent of "the the".
 *   
 * @author Daniel Naber and Lee Nakamura
 */
public class KhmerWordRepeatRule extends Rule {

  public KhmerWordRepeatRule(final ResourceBundle messages, final Language language) {
    super(messages);
    super.setCategory(new Category(messages.getString("category_misc")));
  }

  public boolean ignore(final AnalyzedSentence text, final AnalyzedTokenReadings[] tokensWithWhiteSpace, final int position) {
    // Don't mark an error for cases like:
    // LEN Rewrite for Khmer: ignore real space separating 2 repeated words
    final int origPos = text.getOriginalPosition(position); // LEN get orig pos of current token
    if (position >=1 && "\u0020".equals(tokensWithWhiteSpace[origPos-1].getToken())) {
      return true;
    }
    return false;
  }

  @Override
  public String getId() {
    return "KM_WORD_REPEAT_RULE";
  }

  @Override
  public String getDescription() {
    return messages.getString("desc_repetition");
  }

  @Override
  // LEN modified July, 2011 - add general comments, modify code for Khmer repeated word processing
  public RuleMatch[] match(final AnalyzedSentence text) {
    final List<RuleMatch> ruleMatches = new ArrayList<RuleMatch>();
    final AnalyzedTokenReadings[] tokens = text.getTokensWithoutWhitespace(); // LEN original
    final AnalyzedTokenReadings[] tokensWithWS = text.getTokens(); // LEN with whitespace!

    String prevToken = "";
    //token no. 0 is guaranteed to be SENT_START
    //note: we start from token 1
    for (int i = 1; i < tokens.length; i++) {  // LEN i is a counter which runs from 1 to the number of tokens
      final String token = tokens[i].getToken();
      // avoid "..." etc. to be matched:
      boolean isWord = true;
      if (token.length() == 1) {
        final char c = token.charAt(0);
        if (!Character.isLetter(c)) {
          isWord = false;
        }
      }
      final boolean isException = ignore(text, tokensWithWS, i); // LEN i represents the current token
      // LEN if we have a word, and the previous token is the same (ignoring case) as the current token and we
      //     do not have an exception, provide the rule in Khmer
      if (isWord && prevToken.toLowerCase().equals(token.toLowerCase()) && (isException!=true)) {
        final String msg = messages.getString("repetition"); // LEN get the repetition message (long version)
        final int prevPos = tokens[i - 1].getStartPos();     // LEN find the position of the previous token
        final int pos = tokens[i].getStartPos();               // LEN find position of the current token
        // LEN create a new RuleMatch object, passing in the rule itself, the pos of the previous token, the 
        //     length of the prev token, the repetition msg, and the short version of the repetition msg
        //     This results in the specific violation of the rule being shown in the popup
        // LEN Note: not multiple rules, but multiple suggestions, therefore we need to
        //     use RuleMatch.setSuggestedReplacements(final List<String> replacement)		
        final RuleMatch ruleMatch = new RuleMatch(this, prevPos, pos+prevToken.length(), msg,
                messages.getString("desc_repetition_short"));
        final List<String> replacementSuggs = new ArrayList<String>(); // LEN create empty list of suggestion strings
        replacementSuggs.add(prevToken+" "+token);	// LEN case 1: replace zero-width space w/ real space 
        replacementSuggs.add(prevToken);			// LEN case 2: remove repeated word - same as original suggestion 
        replacementSuggs.add(prevToken+"ៗ");			// LEN case 3: same as case 2, just add "repetition character"
        ruleMatch.setSuggestedReplacements(replacementSuggs); // LEN the suggestions to use
        ruleMatches.add(ruleMatch); // LEN add rule to list of rules
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