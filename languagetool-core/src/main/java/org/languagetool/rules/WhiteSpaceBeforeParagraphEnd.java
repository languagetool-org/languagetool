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
 * A rule that checks for a whitespace at the end of a paragraph
 * @author Fred Kruse
 */
public class WhiteSpaceBeforeParagraphEnd extends TextLevelRule {

  public WhiteSpaceBeforeParagraphEnd(ResourceBundle messages, boolean defaultActive) {
    super(messages);
    super.setCategory(Categories.STYLE.getCategory(messages));
    if (!defaultActive) {
        setDefaultOff();
    }
    setOfficeDefaultOn();
    setLocQualityIssueType(ITSIssueType.Style);
  }

  public WhiteSpaceBeforeParagraphEnd(ResourceBundle messages) {
    this(messages, false);
  }

  @Override
  public String getId() {
    return "WHITESPACE_PARAGRAPH";
  }

  @Override
  public String getDescription() {
    return messages.getString("whitespace_before_parapgraph_end_desc");
  }
  
  private boolean isWhitespaceDel (AnalyzedTokenReadings token) {
    // returns only whitespaces that may be deleted
    // "\u200B" is excluded to prevent function (e.g. page number, page count) in LO/OO
	  return token.isWhitespace() && !token.getToken().equals("\u200B") && !token.isLinebreak();
  }

  @Override
  public RuleMatch[] match(List<AnalyzedSentence> sentences) throws IOException {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    int pos = 0; 
    for (int n = 0; n < sentences.size(); n++) {
      AnalyzedSentence sentence = sentences.get(n);
      AnalyzedTokenReadings[] tokens = sentence.getTokens();
      int i = 2;
      // Whitespace before linebreak (includes last non-whitespace because of problems with OO dialog
      while (i < tokens.length) {
        if(isWhitespaceDel(tokens[i]) && !tokens[i - 1].isWhitespace()) {
          int lastNonWhite = i - 1;
          for (i++; i < tokens.length && isWhitespaceDel(tokens[i]); i++);
          if (i < tokens.length && tokens[i].isLinebreak()) { 
            int fromPos = pos + tokens[lastNonWhite].getStartPos();
            int toPos = pos + tokens[i].getStartPos();
            RuleMatch ruleMatch = new RuleMatch(this, sentence, fromPos, toPos, messages.getString("whitespace_before_parapgraph_end_msg"));
            ruleMatch.setSuggestedReplacement(tokens[lastNonWhite].getToken());
            ruleMatches.add(ruleMatch);
          }
        }
        i++;
      }
      // Whitespace before end of paragraph
      if (n == sentences.size() - 1) {
        for (i = tokens.length - 1; i > 0 && isWhitespaceDel(tokens[i]); i--);
        if (i < tokens.length - 1) {
          int fromPos = pos + tokens[i + 1].getStartPos();
          int toPos = pos + tokens[tokens.length - 1].getStartPos() + 1;
          RuleMatch ruleMatch = new RuleMatch(this, sentence, fromPos, toPos, messages.getString("whitespace_before_parapgraph_end_msg"));
          ruleMatch.setSuggestedReplacement("");
          ruleMatches.add(ruleMatch);
        }
      }
      pos += sentence.getText().length();
    }
    return toRuleMatchArray(ruleMatches);
  }

}
