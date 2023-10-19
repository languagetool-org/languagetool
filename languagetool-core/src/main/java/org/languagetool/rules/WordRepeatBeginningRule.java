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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;

/**
 * Check if three successive sentences begin with the same word, e.g. "I am Max. I am living in Germany. I like ice cream.",
 * and if two successive sentences begin with the same adverb, e.g. "Furthermore, he is ill. Furthermore, he likes her."
 * 
 * @author Markus Brenneis
 */
public class WordRepeatBeginningRule extends TextLevelRule {
  
  public WordRepeatBeginningRule(ResourceBundle messages, Language language) {
    super(messages);
    super.setCategory(Categories.STYLE.getCategory(messages));
    setLocQualityIssueType(ITSIssueType.Style);
  }

  @Override
  public String getId() {
    return "WORD_REPEAT_BEGINNING_RULE";
  }

  @Override
  public String getDescription() {
    return messages.getString("desc_repetition_beginning");
  }
  
  protected boolean isAdverb(AnalyzedTokenReadings token) {
    return false;
  }
  
  public boolean isException(String token) {
    // avoid warning when having lists like "2007: ..." or the like
    return token.equals(":") || token.equals("–") || token.equals("-") || token.equals("✔️") || token.equals("➡️")
        || token.equals("—") || token.equals("⭐️") || token.equals("⚠️");
  }

  @Override
  public RuleMatch[] match(List<AnalyzedSentence> sentences) throws IOException {
    String lastToken = "";
    String beforeLastToken = "";
    List<RuleMatch> ruleMatches = new ArrayList<>();
    int pos = 0;
    AnalyzedSentence prevSentence = null;
    for (AnalyzedSentence sentence : sentences) {
      AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
      if (tokens.length > 3) {
        AnalyzedTokenReadings analyzedToken = tokens[1];
        String token = analyzedToken.getToken();
        // avoid "..." etc. to be matched:
        boolean isWord = true;
        if (token.length() == 1) {
          if (!Character.isLetter(token.charAt(0))) {
            isWord = false;
          }
        }
        if (isWord && lastToken.equals(token)
                && !isException(token) && !isException(tokens[2].getToken()) && !isException(tokens[3].getToken())
                && prevSentence != null && prevSentence.getText().trim().matches(".+[.?!]$")) {  // no matches for e.g. table cells
          String shortMsg;
          if (isAdverb(analyzedToken)) {
            shortMsg = messages.getString("desc_repetition_beginning_adv");
          } else if (beforeLastToken.equals(token)) {
            shortMsg = messages.getString("desc_repetition_beginning_word");
          } else {
            shortMsg = "";
          }
          if (!shortMsg.isEmpty()) {
            String msg = shortMsg + " " + messages.getString("desc_repetition_beginning_thesaurus");
            int startPos = analyzedToken.getStartPos();
            int endPos = startPos + token.length();
            RuleMatch ruleMatch = new RuleMatch(this, sentence, pos+startPos, pos+endPos, msg, shortMsg);
            List<String> suggestions = getSuggestions(analyzedToken);
            if (suggestions.size() > 0) {
              ruleMatch.setSuggestedReplacements(suggestions);
            }
            ruleMatches.add(ruleMatch);
          }
        }
        beforeLastToken = lastToken;
        lastToken = token;
      }
      pos += sentence.getCorrectedTextLength();
      prevSentence = sentence;
    }
    return toRuleMatchArray(ruleMatches);
  }

  protected List<String> getSuggestions(AnalyzedTokenReadings analyzedToken) {
    return Collections.emptyList();
  }

  @Override
  public int minToCheckParagraph() {
    return 2;
  }

}
