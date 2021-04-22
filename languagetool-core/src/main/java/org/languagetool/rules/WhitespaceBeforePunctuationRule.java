/* LanguageTool, a natural language style checker
 * Copyright (C) 2013 Daniel Naber (http://www.danielnaber.de)
 *                    Paolo Bianchini
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

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.tools.StringTools;

/**
 * A rule that matches several punctuation signs such as {@code :} {@code ;} and {@code %} preceded by whitespace.
 * 
 * Checks for:
 * <pre>
 *    &lt;a word&gt; : and suggests &lt;a word&gt;:
 *    &lt;a word&gt; ; and suggests &lt;a word&gt;;
 *    &lt;a number&gt; % and suggests &lt;a number&gt;%
 * </pre>
 * 
 * @author Paolo Bianchini
 */
public class WhitespaceBeforePunctuationRule extends Rule {

  public WhitespaceBeforePunctuationRule(ResourceBundle messages) {
    super(messages);
    super.setCategory(Categories.TYPOGRAPHY.getCategory(messages));
    setLocQualityIssueType(ITSIssueType.Whitespace);
  }

  @Override
  public final String getId() {
    return "WHITESPACE_PUNCTUATION";
  }

  @Override
  public final String getDescription() {
    return messages.getString("desc_whitespace_before_punctuation");
  }

  @Override
  public final RuleMatch[] match(AnalyzedSentence sentence) {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = sentence.getTokens();
    boolean prevWhite = false;
    int prevLen = 0;
    for (int i = 0; i < tokens.length; i++) {
      String token = tokens[i].getToken();
      boolean isWhitespace = tokens[i].isWhitespace() || StringTools.isNonBreakingWhitespace(token)
          || tokens[i].isFieldCode();
      String msg = null;
      String suggestionText = null;
      if (prevWhite) {
        if (token.equals(":")) {
          msg = messages.getString("no_space_before_colon");
          suggestionText = ":";
          // exception case for figures such as " : 0"
          if (i + 2 < tokens.length
              && tokens[i + 1].isWhitespace()
              && Character.isDigit(tokens[i + 2].getToken().charAt(0))) {
            msg = null;
          }
        } else if (token.equals(";")) {
          msg = messages.getString("no_space_before_semicolon");
          suggestionText = ";";
        } else if (i > 1 && token.equals("%")) {
          String prevPrevToken = tokens[i - 2].getToken();
          if (prevPrevToken.length() > 0 && Character.isDigit(prevPrevToken.charAt(0))) {
            msg = messages.getString("no_space_before_percentage");
            suggestionText = "%";
          }            
        }
      }
      if (msg != null) {
        int fromPos = tokens[i - 1].getStartPos();
        int toPos = tokens[i - 1].getStartPos() + 1 + prevLen;
        RuleMatch ruleMatch = new RuleMatch(this, sentence, fromPos, toPos, msg);
        ruleMatch.setSuggestedReplacement(suggestionText);
        ruleMatches.add(ruleMatch);
      }
      prevWhite = isWhitespace && !tokens[i].isFieldCode(); //OOo code before comma/dot
      prevLen = tokens[i].getToken().length();
    }

    return toRuleMatchArray(ruleMatches);
  }

}
