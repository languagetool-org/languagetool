/* LanguageTool, a natural language style checker
 * Copyright (C) 2013 Daniel Naber (http://www.danielnaber.de)
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

package org.languagetool.rules.zh;

import com.hankcs.hanlp.HanLP;
import org.languagetool.AnalyzedSentence;
import org.languagetool.rules.RuleMatch;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class TraditionalChineseNgramProbabilityRule extends ChineseNgramProbabilityRule {

  public TraditionalChineseNgramProbabilityRule() {
    super();
  }

  public TraditionalChineseNgramProbabilityRule(ResourceBundle messages) {
    super(messages);
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    String ss = sentence.getText();
    String maxProbSentence = super.getMaxProbSentence(HanLP.convertToSimplifiedChinese(ss));
    maxProbSentence = HanLP.s2hk(maxProbSentence);

    for (int i = 0; i < ss.length(); i++) {
      String oldChar = Character.toString(ss.charAt(i));
      String newChar = Character.toString(maxProbSentence.charAt(i));
      if (!oldChar.equals(newChar)) {
        RuleMatch ruleMatch = new RuleMatch(this, sentence, i, i + 1, "");
        ruleMatch.setSuggestedReplacement(newChar);
        ruleMatches.add(ruleMatch);
      }
    }

    return toRuleMatchArray(ruleMatches);
  }

}
