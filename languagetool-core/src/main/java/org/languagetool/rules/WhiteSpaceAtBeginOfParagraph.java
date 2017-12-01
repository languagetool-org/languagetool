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
 * A rule that checks for WhiteSpaces at the begin of a paragraph
 * @since 4.0
 * @author Fred Kruse
 */
public class WhiteSpaceAtBeginOfParagraph extends Rule {

  public WhiteSpaceAtBeginOfParagraph(ResourceBundle messages, boolean defaultActive) {
    super(messages);
    super.setCategory(Categories.STYLE.getCategory(messages));
    if (!defaultActive) {
      setDefaultOff();
    }
    setOfficeDefaultOn();
    setLocQualityIssueType(ITSIssueType.Style);
  }

  public WhiteSpaceAtBeginOfParagraph(ResourceBundle messages) {
    this(messages, false);
  }

  @Override
  public String getId() {
    return "WHITESPACE_PARAGRAPH_BEGIN";
  }

  @Override
  public String getDescription() {
    return messages.getString("whitespace_at_begin_parapgraph_desc");
  }

  private boolean isWhitespaceDel (AnalyzedTokenReadings token) {
    // returns only whitespaces that may be deleted
    // "\u200B" is excluded to prevent function (e.g. page number, page count) in LO/OO
    return token.isWhitespace() && !token.getToken().equals("\u200B") && !token.isLinebreak();
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = sentence.getTokens();
    int i;
    for (i = 1; i < tokens.length && isWhitespaceDel(tokens[i]); i++);
    if (i > 1 && i < tokens.length && !tokens[i].isLinebreak()) {
      RuleMatch ruleMatch = new RuleMatch(this, sentence, tokens[1].getStartPos(),
              tokens[i].getEndPos(), messages.getString("whitespace_at_begin_parapgraph_msg"));
      ruleMatch.setSuggestedReplacement(tokens[i].getToken());
      ruleMatches.add(ruleMatch);
    }
    return toRuleMatchArray(ruleMatches);
  }

}
