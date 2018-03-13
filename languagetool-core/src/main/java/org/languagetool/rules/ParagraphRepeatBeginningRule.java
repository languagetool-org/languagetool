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
  
  private static int getParagraphBegin(AnalyzedTokenReadings[] tokens, int start) {
    if (start < 1) {
      start = 1;
    }
    for(int i = start; i < tokens.length; i++) {
      if ("\n".equals(tokens[i].getToken()) || "\r\n".equals(tokens[i].getToken()) || "\n\r".equals(tokens[i].getToken())) {
        for (i++; i < tokens.length && tokens[i].isWhitespace(); i++);
        return i;
      }
    }
    return -1;
  }
  
  
  public boolean isArticle(AnalyzedTokenReadings token) {
    return token.hasPosTagStartingWith("DT");
  }
  
  private int numCharEqualBeginning(AnalyzedTokenReadings[] tokens, int start, AnalyzedTokenReadings[] nextTokens, int nextStart) throws IOException {
    if(tokens.length < 2 || nextTokens.length < 2) {
      return 0;
    }
    int i = start;
    if (i < 1) {
      i = 1;
    }
    for (; i < tokens.length && (tokens[i].isWhitespace() || tokens[i].isSentenceStart()); i++);
    if (i >= tokens.length) {
      return 0;
    }
    String token = tokens[i].getToken();
    if (token.length() == 1) {
      return 0;
    }
    int j = nextStart;
    if (j < 1) {
      j = 1;
    }
    for (; j < nextTokens.length && (nextTokens[j].isWhitespace() || nextTokens[j].isSentenceStart()); j++);
    if (j >= nextTokens.length) {
      return 0;
    }
    String nextToken = nextTokens[j].getToken();
    if (token.equals(nextToken)) {
      if (!isArticle(tokens[i])) {
        return tokens[i].getEndPos() - tokens[i].getStartPos();
      } else {
        int startPos = tokens[i].getStartPos();
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
          return tokens[i].getEndPos() - startPos;
        }
      }
    }
    return 0;

  }

  @Override
  public RuleMatch[] match(List<AnalyzedSentence> sentences) throws IOException {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    if (sentences.size() < 1) {
      return toRuleMatchArray(ruleMatches);
    }
    int pos = 0;
    int nextPos = 0;
    int lastParaBegin = 1;
    int paraBegin = -1;
    int nextParaBegin = 1;
    int numNextSentence = 0;
    AnalyzedTokenReadings[] lastTokens = null;
    AnalyzedSentence nextSentence = sentences.get(0);
    AnalyzedSentence sentence = null;
    AnalyzedTokenReadings[] tokens = null;
    AnalyzedTokenReadings[] nextTokens = nextSentence.getTokens();
    while (numNextSentence < sentences.size()) {
      if (numNextSentence > 0 || paraBegin >= 0) {
        lastTokens = tokens;
        lastParaBegin = paraBegin;
      }
      sentence = nextSentence;
      tokens = nextTokens;
      paraBegin = nextParaBegin;
      nextPos = 0;
      boolean isPara = false;
      while (numNextSentence < sentences.size() && !isPara) {
        nextParaBegin = getParagraphBegin(nextTokens, nextParaBegin);
        if(nextParaBegin >= 0) {
          isPara = true;
        }
        if (nextParaBegin < 0 || nextParaBegin >= nextTokens.length) {
          numNextSentence++;
          if(numNextSentence < sentences.size()) {
            if(nextParaBegin >= nextTokens.length) {
              nextParaBegin = 1;
            }
            nextPos += nextSentence.getText().length();
            nextSentence = sentences.get(numNextSentence);
            nextTokens = nextSentence.getTokens();
          } else {
            nextTokens = null;
          }
        }
      }
      if (tokens.length > 2) {
        int endPos = 0;
        int num = 0;
        num = paraBegin;
        if (num < 1) {
          num = 1;
        }
        if (lastTokens != null) {
          endPos = numCharEqualBeginning(tokens, paraBegin, lastTokens, lastParaBegin);
          if (endPos > 0) {
            for(; tokens[num].isWhitespace() || tokens[num].isSentenceStart(); num++);
            int startPos = pos + tokens[num].getStartPos();
            String msg = messages.getString("repetition_paragraph_beginning_last_msg");
            RuleMatch ruleMatch = new RuleMatch(this, sentence, startPos, startPos+endPos, msg);
            ruleMatches.add(ruleMatch);
          }
        }
        if (endPos == 0 && nextTokens != null) {
          endPos = numCharEqualBeginning(tokens, paraBegin, nextTokens, nextParaBegin);
          if (endPos > 0) {
            for(; tokens[num].isWhitespace() || tokens[num].isSentenceStart(); num++);
            int startPos = pos + tokens[num].getStartPos();
            String msg = messages.getString("repetition_paragraph_beginning_next_msg");
            RuleMatch ruleMatch = new RuleMatch(this, sentence, startPos, startPos+endPos, msg);
            ruleMatches.add(ruleMatch);
          }
        }
      }
      pos += nextPos;
    }
    return toRuleMatchArray(ruleMatches);
  }

}  
  