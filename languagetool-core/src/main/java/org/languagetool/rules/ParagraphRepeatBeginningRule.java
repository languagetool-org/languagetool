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
import java.util.List;
import java.util.ResourceBundle;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;

/**
 * Check if to paragraphs begin with the same word.
 * If the first word is an article it checks if the first two words are identical
 * 
 * @author Fred Kruse
 * @since 4.1
 */
public class ParagraphRepeatBeginningRule extends TextLevelRule {

  public ParagraphRepeatBeginningRule(ResourceBundle messages) {
    super(messages);
    super.setCategory(Categories.STYLE.getCategory(messages));
    setLocQualityIssueType(ITSIssueType.Style);
    setDefaultOff();
  }

  @Override
  public String getId() {
    return "PARAGRAPH_REPEAT_BEGINNING_RULE";
  }

  @Override
  public String getDescription() {
    return messages.getString("repetition_paragraph_beginning_desc");
  }
  
  private static boolean isParagraphEnd(AnalyzedTokenReadings[] tokens) {
    for(int i = tokens.length - 1; i >= 0 && tokens[i].isWhitespace(); i--) {
      if ("\n".equals(tokens[i].getToken()) || "\r\n".equals(tokens[i].getToken()) || "\n\r".equals(tokens[i].getToken())) {
        return true;
      }
    }
    return false;
  }
  
  private static boolean isParagraphBegin(AnalyzedTokenReadings[] tokens) {
    for(int i = 0; i < tokens.length && tokens[i].isWhitespace(); i++) {
      if ("\n".equals(tokens[i].getToken()) || "\r\n".equals(tokens[i].getToken()) || "\n\r".equals(tokens[i].getToken())) {
        return true;
      }
    }
    return false;
  }
  
  public boolean isArticle(AnalyzedTokenReadings token) {
    return token.hasPosTagStartingWith("DT");
  }
  
  private int numCharEqualBeginning(AnalyzedTokenReadings[] tokens, AnalyzedTokenReadings[] nextTokens) {
    if(tokens.length < 2 || nextTokens.length < 2) {
      return 0;
    }
    int i = 0;
    for (i++; i < tokens.length && tokens[i].isWhitespace(); i++);
    if (i >= tokens.length) {
      return 0;
    }
    String token = tokens[i].getToken();
    if (token.length() == 1) {
      return 0;
    }
    int j = 0;
    for (j++; j < nextTokens.length && nextTokens[j].isWhitespace(); j++);
    if (j >= nextTokens.length) {
      return 0;
    }
    String nextToken = nextTokens[j].getToken();
    if (token.equals(nextToken)) {
      if (!isArticle(tokens[i])) {
        int len = 0;
        for(; i >= 0; i--) {
          len += tokens[i].getToken().length();
        }
        return len;
      } else {
        for (i++; i < tokens.length && tokens[i].isWhitespace(); i++);
        if (i >= tokens.length) {
          return 0;
        }
        for (j++; j < nextTokens.length && nextTokens[j].isWhitespace(); j++);
        if (j >= nextTokens.length) {
          return 0;
        }
        token = tokens[i].getToken();
        nextToken = nextTokens[j].getToken();
        if (token.equals(nextToken) && token.length() > 1) {
          int len = 0;
          for(; i >= 0; i--) {
            len += tokens[i].getToken().length();
          }
          return len;
        }
      }
    }
    return 0;
  }

  @Override
  public RuleMatch[] match(List<AnalyzedSentence> sentences) throws IOException {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    AnalyzedTokenReadings[] lastTokens = null;
    AnalyzedTokenReadings[] nextTokens = null;
    AnalyzedTokenReadings[] tokens = null;
    AnalyzedSentence sentence = null;
    AnalyzedSentence nextSentence = null;
    int pos = 0;
    int nextPos = 0;
    for (int i = 0; i < sentences.size() + 1; i++) {
      if (i > 0) {
        lastTokens = tokens;
      }
      if (i == 0) {
        nextSentence = sentences.get(i);
        nextTokens = nextSentence.getTokens();
        i++;
      }
      sentence = nextSentence;
      tokens = nextTokens;
      nextPos = 0;
      boolean isParaBegin = false;
      while (i < sentences.size() && !isParagraphEnd(nextTokens)) {
        nextSentence = sentences.get(i);
        nextTokens = nextSentence.getTokens();
        if (isParagraphBegin(nextTokens)) {
          isParaBegin = true;
          break;
        }
        nextPos += nextSentence.getText().length();
        i++;
      }
      if (!isParaBegin) {
        if (i < sentences.size()) {
          nextSentence = sentences.get(i);
          nextTokens = nextSentence.getTokens();
        } else {
          nextTokens = null;
        }
      }
      if (tokens.length > 2) {
        int endPos = 5;
        int startPos = 0;
        if (lastTokens != null) {
          endPos = numCharEqualBeginning(tokens, lastTokens);
          if (endPos > 0) {
            for(startPos = 0; tokens[startPos+1].isWhitespace(); startPos++);
            String msg = messages.getString("repetition_paragraph_beginning_last_msg");
            RuleMatch ruleMatch = new RuleMatch(this, sentence, pos+startPos, pos+endPos, msg);
            ruleMatches.add(ruleMatch);
          }
        }
        if (endPos == 0 && nextTokens != null) {
          endPos = numCharEqualBeginning(tokens, nextTokens);
          if (endPos > 0) {
            for(startPos = 0; tokens[startPos+1].isWhitespace(); startPos++);
            String msg = messages.getString("repetition_paragraph_beginning_next_msg");
            RuleMatch ruleMatch = new RuleMatch(this, sentence, pos+startPos, pos+endPos, msg);
            ruleMatches.add(ruleMatch);
          }
        }
      }
      pos += sentence.getText().length() + nextPos;
    }
    return toRuleMatchArray(ruleMatches);
  }

}
