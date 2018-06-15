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
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * A rule that raises suggestions when the user inputs TC sentences
 * which directly converted from SC sentences by other tools
 * with some ambiguous words.
 *
 * e.g.
 * Origin(zh-SC):      打印机  公历
 * Converted(zh-TW):   打印機  公曆
 * Correct(zh-TW):     印表機  西曆
 *
 * @author Ze Dang
 */
public class TraditionalChineseAmbiguityRule extends Rule {

  public TraditionalChineseAmbiguityRule() {
    super();
  }

  public TraditionalChineseAmbiguityRule(ResourceBundle messages) {
    super(messages);
  }

  @Override
  public String getId() {
    return "TC_AMBIGUITY_RULE";
  }

  @Override
  public String getDescription() {
    return "A rule detects ambiguities in TC words.";
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();

    for (AnalyzedTokenReadings token : tokens) {
      String word = token.getToken();
      String suggestion = HanLP.t2tw(word);
      if (!word.equals(suggestion)) {
        RuleMatch ruleMatch = new RuleMatch(this, sentence, token.getStartPos(), token.getEndPos(), "");
        ruleMatch.setSuggestedReplacement(suggestion);
        ruleMatches.add(ruleMatch);
      }
    }

    return toRuleMatchArray(ruleMatches);
  }
}
