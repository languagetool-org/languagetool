/* LanguageTool, a natural language style checker 
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Checks that there's whitespace between sentences.
 *   
 * @author Daniel Naber
 */
public class SentenceWhitespaceRule extends Rule {

  private boolean isFirstSentence = true;
  private boolean prevSentenceEndsWithWhitespace = false;
  
  public SentenceWhitespaceRule(ResourceBundle messages) {
    super(messages);
    super.setCategory(new Category(messages.getString("category_misc")));
    setLocQualityIssueType(ITSIssueType.Whitespace);
  }
  
  @Override
  public String getId() {
    return "SENTENCE_WHITESPACE";
  }

  @Override
  public String getDescription() {
    return "Missing space between sentences";
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = sentence.getTokens();

    if (isFirstSentence) {
      isFirstSentence = false;
    } else {
      if (!prevSentenceEndsWithWhitespace && tokens.length > 1) {
        int startPos = 0;
        String firsToken = tokens[1].getToken();
        int endPos = firsToken.length();
        RuleMatch ruleMatch = new RuleMatch(this, startPos, endPos, messages.getString("addSpaceBetweenSentences"));
        ruleMatch.setSuggestedReplacement(" " + firsToken);
        ruleMatches.add(ruleMatch);
      }
    }
    
    if (tokens.length > 0) {
      String lastToken = tokens[tokens.length-1].getToken();
      prevSentenceEndsWithWhitespace = lastToken.trim().isEmpty() && lastToken.length() == 1;
    }

    return toRuleMatchArray(ruleMatches);
  }

  @Override
  public void reset() {
    isFirstSentence = true;
    prevSentenceEndsWithWhitespace = false;
  }

}
