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
import org.languagetool.rules.Category;
import org.languagetool.rules.CategoryId;
import org.languagetool.rules.Example;
import org.languagetool.rules.ITSIssueType;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.TextLevelRule;
import org.languagetool.rules.Category.Location;

/**
 * A rule checks sentences beginning repeatedly with a subject.
 * This rule detects no grammar error but a stylistic problem (default off)
 * @author Fred Kruse
 * @since 5.2
 */
public class StyleRepeatedSentenceBeginning extends TextLevelRule {
  
  public StyleRepeatedSentenceBeginning(ResourceBundle messages) {
    super(messages);
    super.setCategory(new Category(new CategoryId("CREATIVE_WRITING"), 
        messages.getString("category_creative_writing"), Location.INTERNAL, false));
    setDefaultOff();
    setLocQualityIssueType(ITSIssueType.Style);
    addExamplePair(Example.wrong("<marker>Das Auto</marker> kam näher. <marker>Der Hund</marker> lief langsam über die Straße. <marker>Die Reifen</marker> quietschten."),
        Example.fixed("Das Auto kam näher. Langsam lief der Hund über die Straße. Die Reifen quietschten."));
  }

  private final static int MIN_REPEATED = 3;

  @Override
  public RuleMatch[] match(List<AnalyzedSentence> sentences) throws IOException {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    if (sentences.size() < MIN_REPEATED) {
      return toRuleMatchArray(ruleMatches);
    }
    List<Integer> startPos = new ArrayList<>();
    List<Integer> endPos = new ArrayList<>();
    List<AnalyzedSentence> repeatedSentences = new ArrayList<>();
    int pos = 0;
    int nRepeated = 0;
    for (AnalyzedSentence sentence : sentences) {
      AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
      if (tokens[1].hasPosTagStartingWith("ART:DEF:NOM") || tokens[1].hasPosTagStartingWith("ART:IND:NOM")) {
        boolean noSub = true;
        for (int i = 2; i < tokens.length && !tokens[i].hasPosTagStartingWith("VER"); i++) {
          if (tokens[i].hasPosTagStartingWith("SUB")) {
            noSub = false;
            endPos.add(tokens[i].getEndPos() + pos);
            break;
          }
        }
        if (noSub) {
          endPos.add(tokens[1].getEndPos() + pos);
        }
        repeatedSentences.add(sentence);
        startPos.add(tokens[1].getStartPos() + pos);
        nRepeated++;
      } else if (tokens[1].hasPosTagStartingWith("PRO:PER:NOM")) {
        repeatedSentences.add(sentence);
        startPos.add(tokens[1].getStartPos() + pos);
        endPos.add(tokens[1].getEndPos() + pos);
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
    return "STYLE_REPEATED_SENTENCE_BEGINNING";
  }

  @Override
  public String getDescription() {
    return "Subjekt als wiederholter Satzanfang";
  }

}
