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

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.tools.StringTools;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Check if there is duplicated whitespace in a sentence.
 * Considers two spaces as incorrect, and proposes a single space instead.
 * 
 * @author Marcin Miłkowski
 */
public class MultipleWhitespaceRule extends TextLevelRule {

  public MultipleWhitespaceRule(ResourceBundle messages, Language language) {
    super(messages);
    super.setCategory(Categories.TYPOGRAPHY.getCategory(messages));
    setLocQualityIssueType(ITSIssueType.Whitespace);
  }

  @Override
  public String getId() {
    return "WHITESPACE_RULE";
  }

  @Override
  public String getDescription() {
    return messages.getString("desc_whitespacerepetition");
  }
  
  // First White space is not a linebreak, function or footnote
  private static boolean isFirstWhite(AnalyzedTokenReadings token) {
    return (token.isWhitespace() || StringTools.isNonBreakingWhitespace(token.getToken())) 
        && !token.isLinebreak() && !token.getToken().equals("\u200B"); 
  }

  // Removable white space are not linebreaks, tabs, functions or footnotes
  private static boolean isRemovableWhite(AnalyzedTokenReadings token) {
    return (token.isWhitespace() || StringTools.isNonBreakingWhitespace(token.getToken())) 
        && !token.isLinebreak() && !token.getToken().equals("\t") && !token.getToken().equals("\u200B"); 
  }

  @Override
  public RuleMatch[] match(List<AnalyzedSentence> sentences) {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    int pos = 0;
    for (AnalyzedSentence sentence : sentences) {
      AnalyzedTokenReadings[] tokens = sentence.getTokens();
      //note: we start from token 1
      //token no. 0 is guaranteed to be SENT_START
      for (int i = 1; i < tokens.length; i++) {
        if(isFirstWhite(tokens[i])) {
          int nFirst = i;
          for (i++; i < tokens.length && isRemovableWhite(tokens[i]); i++);
          i--;
          if (i > nFirst) {
            String message = messages.getString("whitespace_repetition");
            RuleMatch ruleMatch = new RuleMatch(this, sentence, pos + tokens[nFirst].getStartPos(),
                pos + tokens[i].getEndPos(), message);
            ruleMatch.setSuggestedReplacement(tokens[nFirst].getToken());
            ruleMatches.add(ruleMatch);
          }
        } else if (tokens[i].isLinebreak()) {
          for (i++; i < tokens.length && isRemovableWhite(tokens[i]); i++);
        }
      }
      pos += sentence.getText().length();
    }
    return toRuleMatchArray(ruleMatches);
  }

}
