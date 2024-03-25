/* LanguageTool, a natural language style checker
 * Copyright (C) 2024 Jaume Ortolà
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
package org.languagetool.rules.ca;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.Categories;
import org.languagetool.rules.ITSIssueType;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.TextLevelRule;
import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class EndOfParagraphPunctuationRule extends TextLevelRule {

  public EndOfParagraphPunctuationRule(ResourceBundle messages) {
    this.setCategory(Categories.PUNCTUATION.getCategory(messages));
    this.setLocQualityIssueType(ITSIssueType.Grammar);
    this.setDefaultTempOff();
  }

  private String ruleMessage = "Falta un punt al final del paràgraf.";

  private String shortMessage = "Puntuació";

  @Override
  public String getId() {
    return "CA_END_PARAGRAPH_PUNCTUATION";
  }

  @Override
  public String getDescription() {
    return "Puntuació al final del paràgraf.";
  }

  @Override
  public RuleMatch[] match(List<AnalyzedSentence> sentences) throws IOException {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    int sentencesInParagraph = 0;
    for (AnalyzedSentence sentence : sentences) {
      AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
      int lastTokenPos = tokens.length - 1;
      AnalyzedTokenReadings lastToken = tokens[lastTokenPos];
      String lastTokenStr = lastToken.getToken();
      if (lastToken.hasPosTag("PARA_END")) {
        if (sentencesInParagraph > 0) {
          if (!StringTools.isPunctuationMark(lastTokenStr) || lastTokenStr.equals(",") || lastTokenStr.equals(";")) {
            RuleMatch ruleMatch = new RuleMatch(this, sentence, lastToken.getStartPos(), lastToken.getEndPos(),
              ruleMessage, shortMessage);
            if (!StringTools.isPunctuationMark(lastTokenStr)) {
              ruleMatch.setSuggestedReplacement(lastTokenStr + ".");
            } else {
              ruleMatch.setSuggestedReplacement(".");
            }
            ruleMatches.add(ruleMatch);
          }
        }
        sentencesInParagraph = 0;
      } else {
        sentencesInParagraph++;
      }
    }
    return ruleMatches.toArray(new RuleMatch[0]);
  }


  @Override
  public int minToCheckParagraph() {
    return 0;
  }
}
