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

package org.languagetool.rules.de;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import org.apache.commons.lang3.StringUtils;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.rules.Categories;
import org.languagetool.rules.ITSIssueType;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.TextLevelRule;
import org.languagetool.tagging.de.GermanTagger;

public class BorderNumberRule extends TextLevelRule {

  public BorderNumberRule(ResourceBundle messages, Language language) {
    super(messages);
    super.setCategory(Categories.TYPOGRAPHY.getCategory(messages));
    setLocQualityIssueType(ITSIssueType.Whitespace);
  }

  @Override
  public String getId() {
    return "BORDER_NUMBER";
  }

  @Override
  public String getDescription() {
    return "Nummern gefolgt von 3 Leerzeichen sind Randnummern und beginnen nach den Leerzeichen mit einem Großbuchstaben";
  }
  
  @Override
  public RuleMatch[] match(List<AnalyzedSentence> sentences) {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    int pos = 0;
    for (AnalyzedSentence sentence : sentences) {
      AnalyzedTokenReadings[] tokens = sentence.getTokens();
      //note: we start from token 1
      //token no. 0 is guaranteed to be SENT_START

      int i = 1;
      int whiteSpaceCount = 0;
      while (i < tokens.length) {
        if (StringUtils.isNumeric(tokens[i].getToken())) {
          int startIndex = i;
          if (i + 1 < tokens.length) {
            while (tokens[++i].isWhitespace()) {
              whiteSpaceCount++;
            }

            if (whiteSpaceCount > 0) {
              char firstCharacter = tokens[i].getToken().charAt(0);
              if (whiteSpaceCount != 3 && (firstCharacter >= 65 && firstCharacter <= 90)) {
                ruleMatches.add(new RuleMatch(this, sentence, pos + tokens[startIndex].getStartPos(), pos + tokens[i].getEndPos(), "No border number."));
              }
              whiteSpaceCount = 0;
            }
          }
        }
        i++;
//        if(isFirstWhite(tokens[i])) {
//          int nFirst = i;
//          for (i++; i < tokens.length && isRemovableWhite(tokens[i]); i++);
//          i--;
//          if (i > nFirst) {
//            String message = messages.getString("whitespace_repetition");
//            RuleMatch ruleMatch = new RuleMatch(this, sentence, pos + tokens[nFirst].getStartPos(),
//                pos + tokens[i].getEndPos(), message);
//            ruleMatch.setSuggestedReplacement(tokens[nFirst].getToken());
//            ruleMatches.add(ruleMatch);
//          }
//        } else if (tokens[i].isLinebreak()) {
//          for (i++; i < tokens.length && isRemovableWhite(tokens[i]); i++);
//        }
      }
      pos += sentence.getCorrectedTextLength();
    }
    return toRuleMatchArray(ruleMatches);
  }

  @Override
  public int minToCheckParagraph() {
    return 0;
  }
}
