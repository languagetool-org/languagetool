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
package org.languagetool.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;

/**
 * Check if a word is repeated, e.g. "the the".
 *   
 * @author Daniel Naber
 */
public class WordRepeatRule extends Rule {

  public WordRepeatRule(ResourceBundle messages, Language language) {
    super(messages);
    super.setCategory(Categories.MISC.getCategory(messages));
    setLocQualityIssueType(ITSIssueType.Duplication);
  }

  /**
   * Implement this method to return <code>true</code> if there's
   * a potential word repetition at the current position that should be ignored,
   * i.e. if no error should be created.
   * @param tokens the tokens of the sentence currently being checked
   * @param position the current position in the tokens 
   * @return this implementation always returns false
   */
  public boolean ignore(AnalyzedTokenReadings[] tokens, int position) {
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
  public int estimateContextForSureMatch() {
    return 1;
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = getSentenceWithImmunization(sentence).getTokensWithoutWhitespace();
    String prevToken = "";
    // we start from token 1, token no. 0 is guaranteed to be SENT_START
    for (int i = 1; i < tokens.length; i++) {
      String token = tokens[i].getToken();
      if (tokens[i].isImmunized()) {
        prevToken = "";
        continue;
      }
      if (isWord(token) && prevToken.equalsIgnoreCase(token) && !ignore(tokens, i)) {
        String msg = messages.getString("repetition");
        int prevPos = tokens[i - 1].getStartPos();
        int pos = tokens[i].getStartPos();
        RuleMatch ruleMatch = createRuleMatch(prevToken, token, prevPos, pos, msg, sentence);
        ruleMatches.add(ruleMatch);
      }
      prevToken = token;
    }
    return toRuleMatchArray(ruleMatches);
  }

  protected RuleMatch createRuleMatch(String prevToken, String token, int prevPos, int pos, String msg, AnalyzedSentence sentence) {
    RuleMatch ruleMatch = new RuleMatch(this, sentence, prevPos, pos+prevToken.length(), msg, messages.getString("desc_repetition_short"));
    ruleMatch.setSuggestedReplacement(prevToken);
    return ruleMatch;
  }

  // avoid "..." etc. to be matched:
  private boolean isWord(String token) {
    boolean isWord = true;
    if (StringUtils.isNumericSpace(token)) {
      isWord = false;
    } else if (token.length() == 1) {
      char c = token.charAt(0);
      if (!Character.isLetter(c)) {
        isWord = false;
      }
    }
    return isWord;
  }

}
