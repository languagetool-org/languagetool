/* LanguageTool, a natural language style checker
 * Copyright (C) 2020 Daniel Naber (http://danielnaber.de/)
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
import org.languagetool.Language;
import org.languagetool.tools.Tools;

import java.io.IOException;
import java.util.*;

/**
 * A rule that checks for a punctuation mark at the end of a paragraph.
 * Simplified implementation of PunctuationMarkAtParagraphEnd.
 */
public class PunctuationMarkAtParagraphEnd2 extends TextLevelRule {

  // more than this many word tokens needed for a "real" paragraph that requires a period (etc) at the end:
  private static final int TOKEN_THRESHOLD = 10;
  
  private final Language lang;

  public PunctuationMarkAtParagraphEnd2(ResourceBundle messages, Language lang) {
    super(messages);
    this.lang = Objects.requireNonNull(lang);
    super.setCategory(Categories.PUNCTUATION.getCategory(messages));
    setLocQualityIssueType(ITSIssueType.Grammar);
//  setDefaultTempOff();  // TODO
    setDefaultOff(); 

  }

  @Override
  public String getId() {
    return "PUNCTUATION_PARAGRAPH_END2";
  }

  @Override
  public String getDescription() {
    return messages.getString("punctuation_mark_paragraph_end_desc");
  }
  
  @Override
  public RuleMatch[] match(List<AnalyzedSentence> sentences) throws IOException {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    int pos = 0;
    int sentPos = 0;
    int tokenCount = 0;
    for (AnalyzedSentence sentence : sentences) {
      AnalyzedTokenReadings[] tokens = sentence.getTokens();
      for (AnalyzedTokenReadings token : tokens) {
        if (!token.isNonWord() && !token.isWhitespace()) {
          tokenCount++;
        }
      }
      AnalyzedTokenReadings lastNonSpaceToken = getLastNonSpaceToken(tokens);
      boolean isParaEnd = Tools.isParagraphEnd(sentences, sentPos, lang);
      if (isParaEnd && tokenCount > TOKEN_THRESHOLD &&
          lastNonSpaceToken != null && !lastNonSpaceToken.getToken().matches("[:.?!…]") && !lastNonSpaceToken.isNonWord()) {
        RuleMatch ruleMatch = new RuleMatch(this, sentence, pos + lastNonSpaceToken.getStartPos(), pos + lastNonSpaceToken.getEndPos(),
          messages.getString("punctuation_mark_paragraph_end_msg"));
        ruleMatch.setSuggestedReplacement(lastNonSpaceToken.getToken() + ".");
        ruleMatches.add(ruleMatch);
      }
      if (isParaEnd) {
        tokenCount = 0;
      }
      sentPos++;
      pos += sentence.getCorrectedTextLength();
    }
    return toRuleMatchArray(ruleMatches);
  }

  private AnalyzedTokenReadings getLastNonSpaceToken(AnalyzedTokenReadings[] tokens) {
    for (int i = tokens.length-1; i >= 0; i--) {
      if (!tokens[i].isWhitespace()) {
        return tokens[i];
      }
    }
    return null;
  }

  @Override
  public int minToCheckParagraph() {
    return 0;
  }

}
