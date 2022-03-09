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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;

/**
 * Checks that there's whitespace between sentences.
 *   
 * @author Daniel Naber
 * @since 2.5
 */
public class SentenceWhitespaceRule extends TextLevelRule {

  public SentenceWhitespaceRule(ResourceBundle messages) {
    super(messages);
    super.setCategory(Categories.TYPOGRAPHY.getCategory(messages));
    setLocQualityIssueType(ITSIssueType.Whitespace);
  }
  
  @Override
  public String getId() {
    return "SENTENCE_WHITESPACE";
  }

  @Override
  public String getDescription() {
    return messages.getString("missing_space_between_sentences");
  }

  public String getMessage(boolean prevSentenceEndsWithNumber) {
    return messages.getString("addSpaceBetweenSentences");
  }

  @Override
  public RuleMatch[] match(List<AnalyzedSentence> sentences) throws IOException {
    boolean isFirstSentence = true;
    boolean prevSentenceEndsWithWhitespace = false;
    boolean prevSentenceEndsWithNumber = false;
    List<RuleMatch> ruleMatches = new ArrayList<>();
    int pos = 0;
    for (AnalyzedSentence sentence : sentences) {
      AnalyzedTokenReadings[] tokens = sentence.getTokens();
      if (isFirstSentence) {
        isFirstSentence = false;
      } else {
        if (!prevSentenceEndsWithWhitespace && tokens.length > 1) {
          int startPos = 0;
          String firstToken = tokens[1].getToken();
          int endPos = firstToken.length();
          RuleMatch ruleMatch = new RuleMatch(this, sentence, pos+startPos, pos+endPos, getMessage(prevSentenceEndsWithNumber));
          ruleMatch.setSuggestedReplacement(" " + firstToken);
          ruleMatches.add(ruleMatch);
        }
      }
      if (tokens.length > 0) {
        String lastToken = tokens[tokens.length-1].getToken();
        prevSentenceEndsWithWhitespace = lastToken.replace('\u00A0',' ').trim().isEmpty() && lastToken.length() == 1;
      }
      if (tokens.length > 1) {
        String prevLastToken = tokens[tokens.length-2].getToken();
        prevSentenceEndsWithNumber = StringUtils.isNumeric(prevLastToken);
      }
      pos += sentence.getCorrectedTextLength();
    }
    return toRuleMatchArray(ruleMatches);
  }

  @Override
  public int minToCheckParagraph() {
    return 0;
  }
  
}
