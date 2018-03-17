/* LanguageTool, a natural language style checker
 * Copyright (C) 2014 Daniel Naber (http://danielnaber.de/)
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

/**
 * A rule that checks for a punctuation mark at the end of a paragraph
 * @author Fred Kruse
 * @since 4.1
 */
public class PunctuationMarkAtParagraphEnd extends TextLevelRule {
  
  private final static String PUNCTUATION_MARKS[] = {".", "!", "?", ":", ",", ";"};
  private final static String QUOTATION_MARKS[] = {"„", "»", "«", "\"", "”", "″", "’", "‚", "‘", "›", "‹", "′"};

  public PunctuationMarkAtParagraphEnd(ResourceBundle messages) {
    super(messages);
    super.setCategory(Categories.PUNCTUATION.getCategory(messages));
    setLocQualityIssueType(ITSIssueType.Grammar);
  }

  @Override
  public String getId() {
    return "PUNCTUATION_PARAGRAPH_END";
  }

  @Override
  public String getDescription() {
    return messages.getString("punctuation_mark_paragraph_end_desc");
  }
  
  private static boolean isQuotationMark (AnalyzedTokenReadings tk) {
    String token = tk.getToken();
    for(int i = 0; i < QUOTATION_MARKS.length; i++) {
      if(token.equals(QUOTATION_MARKS[i])) {
        return true;
      }
    }
    return false;
  }

  private static boolean isWord (AnalyzedTokenReadings tk) {
    return Character.isLetter(tk.getToken().charAt(0));
  }

  private static boolean isWhitespace (AnalyzedTokenReadings token) {
    return token.isWhitespace() && !token.isLinebreak();
  }

  private static boolean isParaBreak (AnalyzedTokenReadings token) {
    return "\n".equals(token.getToken()) || "\r\n".equals(token.getToken()) || "\n\r".equals(token.getToken());
  }

  @Override
  public RuleMatch[] match(List<AnalyzedSentence> sentences) throws IOException {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    int lastPara = -1;
    int pos = 0;
    boolean doCheck = true;
    boolean isFirstWord = false;
    for (int n = 0; n < sentences.size(); n++) {
      AnalyzedSentence sentence = sentences.get(n);
      AnalyzedTokenReadings[] tokens = sentence.getTokens();
      if (doCheck) {
        int i = 1;
        for (; i < tokens.length && isWhitespace(tokens[i]); i++);
        if (i < tokens.length) {
          isFirstWord = isWord(tokens[i]) || (tokens.length > i + 1 && isQuotationMark(tokens[i]) && isWord(tokens[i + 1]));
        } else {
          isFirstWord = false;
        }
        doCheck = false;
      }
      for (int i = 1; i < tokens.length; i++) {
        if(isParaBreak(tokens[i]) || (n == sentences.size() - 1 && i == tokens.length - 1)) {
          // paragraphs containing less than two sentences (e.g. headlines, listings) are excluded from rule
          if (n - lastPara > 1 && isFirstWord) {
            if (n == sentences.size() - 1 && i == tokens.length - 1) {
              i++;
            }
            for (i--; i > 0 && isWhitespace(tokens[i]); i--);
            if (i > 0 && (isWord(tokens[i]) 
                || (i < tokens.length - 1 && isQuotationMark(tokens[i]) && isWord(tokens[i + 1])))) { 
              int fromPos = pos + tokens[i].getStartPos();
              int toPos = pos + tokens[i].getEndPos();
              RuleMatch ruleMatch = new RuleMatch(this, sentence, fromPos, toPos, 
                  messages.getString("punctuation_mark_paragraph_end_msg"));
              List<String> replacements = new ArrayList<String>();
              for(int j = 0; j < PUNCTUATION_MARKS.length; j++) {
                replacements.add(tokens[i].getToken() + PUNCTUATION_MARKS[j]);
              }
              ruleMatch.setSuggestedReplacements(replacements);
              ruleMatches.add(ruleMatch);
            }
          }
          lastPara = n;
          isFirstWord = false;
          doCheck = true;
        }
      }
      pos += sentence.getText().length();
    }
    return toRuleMatchArray(ruleMatches);
  }

}
