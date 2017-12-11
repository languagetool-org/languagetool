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
 * A rule that checks for empty lines. Useful especially for office extension
 * It checks only linebreaks because empty paragraphs can't be handled in LO/OO 
 * 
 * @author Fred Kruse
 */

public class EmptyLineRule extends TextLevelRule {

  public EmptyLineRule(ResourceBundle messages, boolean defaultActive) {
    super(messages);
    super.setCategory(Categories.STYLE.getCategory(messages));

    if (!defaultActive) {
        setDefaultOff();   //  Default is Off
    }
    setOfficeDefaultOn();  // Default for LO/OO is always On
      
    setLocQualityIssueType(ITSIssueType.Style);
  }

  public EmptyLineRule(ResourceBundle messages) {
    this(messages, false);
  }

  @Override
  public String getId() {
    return "EMPTY_LINE";
  }

  @Override
  public String getDescription() {
    return messages.getString("empty_line_rule_desc");
  }

  @Override
  public org.languagetool.rules.RuleMatch[] match(List<AnalyzedSentence> sentences) throws IOException {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    int pos = 0;
    for (int n = 0; n < sentences.size(); n++) {
      AnalyzedSentence sentence = sentences.get(n);
      AnalyzedTokenReadings[] tokens = sentence.getTokens();
      for(int i = 2; i < tokens.length; i++) {
        if(tokens[i].isLinebreak() && !tokens[i - 1].isLinebreak()) {
          int firstLB = i;
          for (i++; i < tokens.length && tokens[i].isWhitespace() && !tokens[i].isLinebreak() && !tokens[i].getToken().equals("\u200B"); i++);  
          if (i < tokens.length && tokens[i].isLinebreak()) { 
            int toPos = pos + tokens[firstLB - 1].getEndPos();
            int fromPos = toPos - 1;
            RuleMatch ruleMatch = new RuleMatch(this, sentence, fromPos, toPos, messages.getString("empty_line_rule_msg"));
            // Can't use SuggestedReplacement because of problems in LO/OO dialog with linebreaks
            ruleMatches.add(ruleMatch);
            i--;
          }
        }
      }
      pos += sentence.getText().length();
    }
    return toRuleMatchArray(ruleMatches);
  }

}
