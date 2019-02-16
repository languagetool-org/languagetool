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
import java.util.regex.Pattern;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;

/**
 * Check if to paragraphs begin with the same word.
 * If the first word is an article it checks if the first two words are identical
 * 
 * @author Fred Kruse
 * @since 4.1
 */
public class ParagraphRepeatBeginningRule extends TextLevelRule {

  private final Language lang;
  private static final Pattern QUOTES_REGEX = Pattern.compile("[’'\"„“”»«‚‘›‹()\\[\\]]");

  public ParagraphRepeatBeginningRule(ResourceBundle messages, Language lang) {
    super(messages);
    super.setCategory(Categories.STYLE.getCategory(messages));
    this.lang = lang;
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
  
  public boolean isArticle(AnalyzedTokenReadings token) {
    return token.hasPosTagStartingWith("DT");
  }
  
  private int numCharEqualBeginning(AnalyzedTokenReadings[] lastTokens, AnalyzedTokenReadings[] nextTokens) throws IOException {
    if(lastTokens.length < 2 || nextTokens.length < 2 
        || lastTokens[1].isWhitespace() || nextTokens[1].isWhitespace()) {
      return 0;
    }
    int nToken = 1;
    String lastToken = lastTokens[nToken].getToken();
    String nextToken = nextTokens[nToken].getToken();
    if (QUOTES_REGEX.matcher(lastToken).matches() && lastToken.equals(nextToken)) {
      if(lastTokens.length <= nToken + 1 || nextTokens.length <= nToken + 1) {
        return 0;
      }
      nToken++;
      lastToken = lastTokens[nToken].getToken();
      nextToken = nextTokens[nToken].getToken();
    }
    if(!Character.isLetter(lastToken.charAt(0))) {
      return 0;
    }
    if (lastTokens.length > nToken + 1 && isArticle(lastTokens[nToken]) && lastToken.equals(nextToken)) {
      if(lastTokens.length <= nToken + 1 || nextTokens.length <= nToken + 1) {
        return 0;
      }
      nToken++;
      lastToken = lastTokens[nToken].getToken();
      nextToken = nextTokens[nToken].getToken();
    }
    if(!Character.isLetter(lastToken.charAt(0))) {
      return 0;
    }
    if (lastToken.equals(nextToken)) {
      return lastTokens[nToken].getEndPos();
    }
    return 0;
  }

  @Override
  public RuleMatch[] match(List<AnalyzedSentence> sentences) throws IOException {

    List<RuleMatch> ruleMatches = new ArrayList<>();

    if (sentences.size() < 1) {
      return toRuleMatchArray(ruleMatches);
    }

    int nextPos = 0;
    int lastPos = 0;
    int endPos = 0;
    AnalyzedSentence lastSentence = sentences.get(0);
    AnalyzedTokenReadings[] lastTokens = lastSentence.getTokensWithoutWhitespace();
    AnalyzedSentence nextSentence = null;
    AnalyzedTokenReadings[] nextTokens = null;
    
    for (int n = 0; n < sentences.size() - 1; n++) {
      nextPos += sentences.get(n).getText().length();
      if(sentences.get(n).hasParagraphEndMark(lang)) {
        nextSentence = sentences.get(n + 1);
        nextTokens = nextSentence.getTokensWithoutWhitespace();
        endPos = numCharEqualBeginning(lastTokens, nextTokens);
        if (endPos > 0) {
          int startPos = lastPos + lastTokens[1].getStartPos();
          String msg = messages.getString("repetition_paragraph_beginning_last_msg");
          RuleMatch ruleMatch = new RuleMatch(this, lastSentence, startPos, lastPos+endPos, msg);
          ruleMatches.add(ruleMatch);
          
          startPos = nextPos + nextTokens[1].getStartPos();
          msg = messages.getString("repetition_paragraph_beginning_last_msg");
          ruleMatch = new RuleMatch(this, nextSentence, startPos, nextPos+endPos, msg);
          ruleMatches.add(ruleMatch);
        }
        lastSentence = nextSentence;
        lastTokens = nextTokens;
        lastPos = nextPos;
      }
    }
    return toRuleMatchArray(ruleMatches);
  }

}  
  