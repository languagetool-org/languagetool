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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.tools.Tools;

/**
 * A rule that matches ".." (but not "..." etc) and ",,".
 * 
 * @author Daniel Naber
 */
public class DoublePunctuationRule extends Rule {

  public DoublePunctuationRule(ResourceBundle messages) {
    this(messages, null);
  }

  /** @since 5.9 */
  public DoublePunctuationRule(ResourceBundle messages, URL url) {
    super(messages);
    super.setCategory(Categories.PUNCTUATION.getCategory(messages));
    setLocQualityIssueType(ITSIssueType.Typographical);
    if (url != null) {
      setUrl(url);
    }
  }

  @Override
  public String getId() {
    return "DOUBLE_PUNCTUATION";
  }

  @Override
  public final String getDescription() {
    return messages.getString("desc_double_punct");
  }
  
  public String getCommaCharacter() {
    return ",";
  }

  @Override
  public final RuleMatch[] match(AnalyzedSentence sentence) {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
    int startPos = 0;
    int dotCount = 0;
    int commaCount = 0;
    for (int i = 1; i < tokens.length; i++) {
      String token = tokens[i].getToken();
      String nextToken = null;
      String prevPrevToken = null;
      if (i < tokens.length - 1) {
        nextToken = tokens[i + 1].getToken();
      }
      if (i > 1) {
        prevPrevToken = tokens[i - 2].getToken();
      }
      if (".".equals(token)) {
        dotCount++;
        commaCount = 0;
        startPos = tokens[i].getStartPos();
      } else if (getCommaCharacter().equals(token)) {
        commaCount++;
        dotCount = 0;
        startPos = tokens[i].getStartPos();
      }


      if (dotCount == 2 && !".".equals(nextToken) && !"…".equals(nextToken) &&
          !"/".equals(token) && !"/".equals(nextToken) &&  /* Unix path */
          !"\\".equals(token) && !"\\".equals(nextToken) &&  /* Windows path */
          !"?".equals(prevPrevToken) && !"!".equals(prevPrevToken) &&
          !"…".equals(prevPrevToken) && !".".equals(prevPrevToken)) {
        int fromPos = Math.max(0, startPos - 1);
        RuleMatch ruleMatch = new RuleMatch(this, sentence, fromPos, startPos + 1,
            getDotMessage(), messages.getString("double_dots_short"));
        ruleMatch.addSuggestedReplacement(".");
        ruleMatch.addSuggestedReplacement("…");
        ruleMatches.add(ruleMatch);
        dotCount = 0;
      } else if (commaCount == 2 && !getCommaCharacter().equals(nextToken)) {
        int fromPos = Math.max(0, startPos - 1);
        RuleMatch ruleMatch = new RuleMatch(this, sentence, fromPos, startPos + 1,
            getCommaMessage(), messages.getString("double_commas_short"));
        ruleMatch.setSuggestedReplacement(getCommaCharacter());
        ruleMatches.add(ruleMatch);
        commaCount = 0;
      }
      if (!".".equals(token) && !getCommaCharacter().equals(token)) {
        dotCount = 0;
        commaCount = 0;
      }
    }

    return toRuleMatchArray(ruleMatches);
  }

  protected String getDotMessage() {
    return messages.getString("two_dots");
  }

  protected String getCommaMessage() {
    return messages.getString("two_commas");
  }
  
}
