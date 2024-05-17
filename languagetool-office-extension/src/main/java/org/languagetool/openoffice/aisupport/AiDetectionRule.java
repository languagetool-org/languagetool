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
package org.languagetool.openoffice.aisupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Tag;
import org.languagetool.openoffice.MessageHandler;
import org.languagetool.rules.Categories;
import org.languagetool.rules.ITSIssueType;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.TextLevelRule;

/**
 * Rule to detect errors by a AI API
 * @since 6.5
 * @author Fred Kruse
 */
public class AiDetectionRule extends TextLevelRule {
  
  public static final String RULE_ID = "LO_AI_DETECTION_RULE";
  private final static String MATCH_MESSAGE = "AI detected error";
  private final static String RULE_DESCRIPTION = "AI detected errors";
  private final ResourceBundle messages;
  private final String aiResultText;
  private final List<AnalyzedSentence> analyzedAiResult;

  
  AiDetectionRule(String aiResultText, List<AnalyzedSentence> analyzedAiResult, ResourceBundle messages) {
     this.aiResultText = aiResultText;
    this.analyzedAiResult = analyzedAiResult;
    this.messages = messages;
    
    setCategory(Categories.STYLE.getCategory(messages));
    setLocQualityIssueType(ITSIssueType.Grammar);
    setTags(Collections.singletonList(Tag.picky));

  }

  @Override
  public RuleMatch[] match(List<AnalyzedSentence> sentences) throws IOException {
    List<RuleMatch> matches = new ArrayList<>();
    List<AiToken> paraTokens = new ArrayList<>();
    int pos = 0;
    for (AnalyzedSentence sentence : sentences) {
      AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
      for (int i = 1; i < tokens.length; i++) {
        paraTokens.add(new AiToken(tokens[i].getToken(), tokens[i].getStartPos() + pos, sentence));
      }
      pos += sentence.getCorrectedTextLength();
    }
    List<AiToken> resultTokens = new ArrayList<>();
    pos = 0;
    for (AnalyzedSentence sentence : analyzedAiResult) {
      AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
      for (int i = 1; i < tokens.length; i++) {
        resultTokens.add(new AiToken(tokens[i].getToken(), tokens[i].getStartPos() + pos, null));
      }
      pos += sentence.getCorrectedTextLength();
    }
    int j = 0;
    for (int i = 0; i < paraTokens.size() && j < resultTokens.size(); i++) {
      if (!paraTokens.get(i).token.equals(resultTokens.get(j).token)) {
        int posStart = paraTokens.get(i).startPos;
        AnalyzedSentence sentence = paraTokens.get(i).sentence;
        int posEnd = 0;
        String suggestion = null;
        boolean endFound = false;
        for (int n = 1; !endFound && i + n < paraTokens.size() && j + n < resultTokens.size(); n++) {
          for (int i1 = i + n; !endFound && i1 > i; i1--) {
            for(int j1 = j + n; j1 > j; j1--) {
              if (paraTokens.get(i1).token.equals(resultTokens.get(j1).token)) {
                endFound = true;
                posEnd = paraTokens.get(i1).endPos;
                suggestion = aiResultText.substring(resultTokens.get(j).startPos, resultTokens.get(j1).endPos);
                j = j1;
                break;
              }
            }
          }
        }
        if (!endFound) {
          posEnd = paraTokens.get(paraTokens.size() - 1).endPos;
          suggestion = aiResultText.substring(resultTokens.get(j).startPos, resultTokens.get(resultTokens.size() - 1).endPos);
          j = resultTokens.size() - 1;
        }
        MessageHandler.printToLogFile("Match found: start: " + posStart + ", end: " + posEnd + ", suggestion: " + suggestion);
        RuleMatch ruleMatch = new RuleMatch(this, sentence, posStart, posEnd, MATCH_MESSAGE);
        ruleMatch.addSuggestedReplacement(suggestion);
        matches.add(ruleMatch);
      }
      j++;
    }
    return toRuleMatchArray(matches);
  }

  @Override
  public int minToCheckParagraph() {
    return 0;
  }

  @Override
  public String getId() {
    return RULE_ID;
  }

  @Override
  public String getDescription() {
    return RULE_DESCRIPTION;
  }

}
