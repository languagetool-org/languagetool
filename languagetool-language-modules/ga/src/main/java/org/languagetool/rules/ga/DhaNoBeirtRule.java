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
package org.languagetool.rules.ga;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.*;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static org.languagetool.rules.ga.DhaNoBeirtData.getNumberReplacements;
import static org.languagetool.rules.ga.DhaNoBeirtData.getDaoine;

public class DhaNoBeirtRule extends Rule {
  public DhaNoBeirtRule(ResourceBundle messages) {
    super.setCategory(Categories.MISC.getCategory(messages));
    setLocQualityIssueType(ITSIssueType.Misspelling);
    addExamplePair(Example.wrong("The train arrived <marker>a hour</marker> ago."),
      Example.fixed("The train arrived <marker>an hour</marker> ago."));
  }

  @Override
  public String getId() {
    return "GA_DHA_NO_BEIRT";
  }

  @Override
  public String getDescription() {
    return "'dhá' nó 'beirt'";
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
    int markEnd = 1;
    String replacement = null;
    for (int i = 1; i < tokens.length; i++) {  // ignoring token 0, i.e., SENT_START
      if (isNumber(tokens[i]) && (i < tokens.length - 1 && isPerson(tokens[i + 1]))) {
        markEnd = i + 1;
        if ("dhá".equalsIgnoreCase(tokens[i].getToken())) {
          for (int j = i + 1; j < tokens.length; j++) {
            if ("déag".equalsIgnoreCase(tokens[j].getToken())) {
              markEnd = j;
              replacement = "dáréag";
            }
          }
        }
        if (replacement == null) {
          replacement = getNumberReplacements().get(tokens[i]);
        }
      }
    }
    return null;
  }

  private boolean isNumber(AnalyzedTokenReadings tok) {
    for (String num : getNumberReplacements().keySet()) {
      if (num.equalsIgnoreCase(tok.getToken())) {
        return true;
      }
    }
    return false;
  }
  private boolean isPerson(AnalyzedTokenReadings tok) {
    if (getDaoine().contains(tok.getToken().toLowerCase())) {
      return true;
    }
    for (AnalyzedToken reading : tok.getReadings()) {
      if (getDaoine().contains(reading.getLemma())) {
        return true;
      }
    }
    return false;
  }
}
