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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.rules.Category;
import org.languagetool.rules.CategoryId;
import org.languagetool.rules.Example;
import org.languagetool.rules.ITSIssueType;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.TextLevelRule;
import org.languagetool.rules.Category.Location;
import org.languagetool.tools.Tools;

/**
 * A rule checks the use of very short sentences repeatedly.
 * This rule detects no grammar error but a stylistic problem (default off)
 * @author Fred Kruse
 * @since 5.2
 */
public class StyleRepeatedVeryShortSentences extends TextLevelRule {
  
  private final Language lang;

  public StyleRepeatedVeryShortSentences(ResourceBundle messages, Language lang) {
    super(messages);
    super.setCategory(new Category(new CategoryId("CREATIVE_WRITING"), 
        messages.getString("category_creative_writing"), Location.INTERNAL, false));
    this.lang = lang;
    setDefaultOff();
    setLocQualityIssueType(ITSIssueType.Style);
    addExamplePair(Example.wrong("Das Auto kam <marker>näher.</marker> Der Hund <marker>schlief.</marker> Die Reifen <marker>quietschten.</marker>"),
        Example.fixed("Das Auto kam näher. Tief und fest schlief der Hund. Die Reifen quietschten."));
  }

  private final static int MIN_REPEATED = 3;
  private final static int MIN_WORDS = 4;

  @Override
  public RuleMatch[] match(List<AnalyzedSentence> sentences) throws IOException {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    if (sentences.size() < MIN_REPEATED) {
      return toRuleMatchArray(ruleMatches);
    }
    int pos = 0;
    int nRepeated = 0;
    List<Integer> startPos = new ArrayList<>();
    List<Integer> endPos = new ArrayList<>();
    List<AnalyzedSentence> repeatedSentences = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = null;
    int n = -1;
    int nPara  = -1;
    for (AnalyzedSentence sentence : sentences) {
      n++;
      nPara++;
      tokens = sentence.getTokensWithoutWhitespace();
      boolean paragraphEnd = Tools.isParagraphEnd(sentences, n, lang);
      if ((!paragraphEnd || nPara > 0) && tokens.length > 2 && tokens.length <= MIN_WORDS + 2) {  // MIN_WORDS + SENT_START + punctuation mark
        repeatedSentences.add(sentence);
        startPos.add(tokens[tokens.length - 2].getStartPos() + pos);
        endPos.add(tokens[tokens.length - 1].getEndPos() + pos);
        nRepeated++;
      } else {
        if (nRepeated >= MIN_REPEATED) {
          for (int i = 0; i < repeatedSentences.size(); i++) {
            RuleMatch ruleMatch = new RuleMatch(this, repeatedSentences.get(i), startPos.get(i), endPos.get(i), getDescription());
            ruleMatches.add(ruleMatch);
          }
        }
        repeatedSentences.clear();
        startPos.clear();
        endPos.clear();
        nRepeated = 0;
      }
      pos += sentence.getCorrectedTextLength();
      if (paragraphEnd) {
        nPara = -1;
      }
    }
    if (nRepeated >= MIN_REPEATED) {
      for (int i = 0; i < repeatedSentences.size(); i++) {
        RuleMatch ruleMatch = new RuleMatch(this, repeatedSentences.get(i), startPos.get(i), endPos.get(i), getDescription());
        ruleMatches.add(ruleMatch);
      }
    }
    return toRuleMatchArray(ruleMatches);
  }

  @Override
  public int minToCheckParagraph() {
    return MIN_REPEATED;
  }

  @Override
  public String getId() {
    return "STYLE_REPEATED_SHORT_SENTENCES";
  }

  @Override
  public String getDescription() {
    return "Stakkato-Sätze";
  }

}
