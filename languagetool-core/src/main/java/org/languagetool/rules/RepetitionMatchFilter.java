package org.languagetool.rules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.languagetool.Language;
import org.languagetool.rules.patterns.RuleSet;

public class RepetitionMatchFilter implements RuleMatchFilter {

  protected Language language;
  protected List<String> rulesToCheck;
  protected int distance; // numer of tokens

  public RepetitionMatchFilter(Language lang, RuleSet rules) {
    language = lang;
    rulesToCheck = Collections.singletonList("CA_REPEAT_PATTERN_TEST"); //rules.allRuleIds();
    distance = 350; // characters
  }

  @Override
  public List<RuleMatch> filter(List<RuleMatch> ruleMatches) {
    if (language.getShortCode().equals("ca")) {
      List<RuleMatch> newRuleMatches = new ArrayList<>();
      Map<String,Integer> mapRulesPostions = new HashMap<>();
      for (RuleMatch rm : ruleMatches) {
        boolean ignoreRule = false;
        String ruleId = rm.getRule().getId();
        int pos = rm.getFromPos();
        if (rulesToCheck.contains(ruleId)) {
          if (mapRulesPostions.containsKey(ruleId)) {
            int lastSeenPos = mapRulesPostions.get(ruleId);
            ignoreRule = pos-lastSeenPos>distance;
            mapRulesPostions.put(ruleId, pos);
          } else {
            mapRulesPostions.put(ruleId, pos);
            ignoreRule = true;
          }
        }
        if (!ignoreRule) {
          newRuleMatches.add(rm);
        }
      }
      return newRuleMatches;
    }
    return ruleMatches;
  }
}