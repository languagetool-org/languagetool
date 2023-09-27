package org.languagetool.rules;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.patterns.PatternRule;
import org.languagetool.rules.patterns.RuleFilter;
import org.languagetool.rules.spelling.SpellingCheckRule;

import java.io.IOException;
import java.util.Map;

public class MultitokenSpellerFilter extends RuleFilter {

  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos,
                                   AnalyzedTokenReadings[] patternTokens) throws IOException {
    if (match.getSentence().getText().equals("Yuval Harari")) {
      int ii=0;
      ii++;
    }
    PatternRule pr = (PatternRule) match.getRule();
    SpellingCheckRule spellingRule = pr.getLanguage().getDefaultSpellingRule();
    String originalStr = match.getOriginalErrorStr();
    AnalyzedTokenReadings[] atrsArray = new AnalyzedTokenReadings[2];
    AnalyzedTokenReadings atrs0 = new AnalyzedTokenReadings(new AnalyzedToken("", "SENT_START", ""));
    AnalyzedTokenReadings atrs1 = new AnalyzedTokenReadings(new AnalyzedToken(originalStr, null, null));
    atrsArray[0] = atrs0;
    atrsArray[1] = atrs1;
    AnalyzedSentence sentence = new AnalyzedSentence(atrsArray);
    RuleMatch[] matches = spellingRule.match(sentence);
    if (matches.length < 1) {
      return null;
    }
    match.setSuggestedReplacements(matches[0].getSuggestedReplacements());
    return match;
  }
}
