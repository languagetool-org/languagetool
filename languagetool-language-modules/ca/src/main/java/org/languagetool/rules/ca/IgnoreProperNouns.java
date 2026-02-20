/* LanguageTool, a natural language style checker
 * Copyright (C) 2026 Jaume Ortolà
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
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.TextLevelRule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class IgnoreProperNouns extends TextLevelRule {


  public IgnoreProperNouns(ResourceBundle messages) {
    super(messages);
    super.setCategory(Categories.MISC.getCategory(messages));
  }

  @Override
  public RuleMatch[] match(List<AnalyzedSentence> sentences) throws IOException {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    int pos = 0;
    List<String> seenProperNouns = new ArrayList<>();
    for (AnalyzedSentence sentence : sentences) {
      AnalyzedTokenReadings[] tokens = sentence.getTokens();
      for (AnalyzedTokenReadings token : tokens) {
        if (token.hasPosTagStartingWith("NP")) {
          seenProperNouns.add(token.getToken());
        }
        if (!token.isTagged() && seenProperNouns.contains(token.getToken())) {
          RuleMatch ruleMatch = new RuleMatch(this, sentence, pos+token.getStartPos(), pos+token.getEndPos(),
            "Aquesta paraula ja aparegut abans i es pot donar per correcta.");
          ruleMatches.add(ruleMatch);
        }
      }
      pos += sentence.getCorrectedTextLength();
    }
    return toRuleMatchArray(ruleMatches);
  }

  @Override
  public int minToCheckParagraph() {
    return 0;
  }

  @Override
  public String getId() {
    return "IGNORE_PROPER_NOUNS";
  }

  @Override
  public String getDescription() {
    return "Ignora noms propis que hagin aparegut en altres parts del text.";
  }
}
