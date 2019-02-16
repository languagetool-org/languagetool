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
import org.languagetool.Language;

/**
 * A rule that checks for a whitespace at the end of a paragraph
 * @author Fred Kruse
 */
public class WhiteSpaceBeforeParagraphEnd extends TextLevelRule {

  private final Language lang;
  
  public WhiteSpaceBeforeParagraphEnd(ResourceBundle messages, Language lang, boolean defaultActive) {
    super(messages);
    super.setCategory(Categories.STYLE.getCategory(messages));
    this.lang = lang;
    if (!defaultActive) {
      setDefaultOff();
    }
    setOfficeDefaultOn();
    setLocQualityIssueType(ITSIssueType.Style);
  }

  public WhiteSpaceBeforeParagraphEnd(ResourceBundle messages, Language lang) {
    this(messages, lang, false);
  }

  @Override
  public String getId() {
    return "WHITESPACE_PARAGRAPH";
  }

  @Override
  public String getDescription() {
    return messages.getString("whitespace_before_parapgraph_end_desc");
  }
  
  @Override
  public RuleMatch[] match(List<AnalyzedSentence> sentences) throws IOException {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    int pos = 0; 
    for (int n = 0; n < sentences.size(); n++) {
      AnalyzedSentence sentence = sentences.get(n);
      if(n == sentences.size() - 1 || sentence.hasParagraphEndMark(lang)) {
        AnalyzedTokenReadings[] tokens = sentence.getTokens();
        int lb;
        int lw;
        for (lb = tokens.length - 1; lb > 0 && tokens[lb].isLinebreak(); lb--);
        for (lw = lb; lw > 0 && tokens[lw].isWhitespace(); lw--);
        if(lw < lb) {
          int fromPos = pos + tokens[lw].getStartPos();
          int toPos = pos + tokens[lb].getEndPos();
          RuleMatch ruleMatch = new RuleMatch(this, sentence, fromPos, toPos, messages.getString("whitespace_before_parapgraph_end_msg"));
          if (lw > 0 && !tokens[lw].isWhitespace()) {
            ruleMatch.setSuggestedReplacement(tokens[lw].getToken());
          } else {
            ruleMatch.setSuggestedReplacement("");
          }
          ruleMatches.add(ruleMatch);
        }
      }
      pos += sentence.getText().length();
    }
    return toRuleMatchArray(ruleMatches);
  }

}
