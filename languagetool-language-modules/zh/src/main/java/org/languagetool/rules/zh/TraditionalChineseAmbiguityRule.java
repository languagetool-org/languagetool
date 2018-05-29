package org.languagetool.rules.zh;

import com.hankcs.hanlp.HanLP;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

  @Override
  public String getId() {
    return "TC_AMBIGUITY_RULE";
  }

  @Override
  public String getDescription() {
    return "A rule detects ambiguities in words.";
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
