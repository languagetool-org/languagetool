/* LanguageTool, a natural language style checker 
 * Copyright (C) 2021 Fabian Richter
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

package org.languagetool.rules.patterns;

import org.languagetool.AnalyzedSentence;
import org.languagetool.Language;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.SameRuleGroupFilter;
import org.languagetool.rules.TextLevelRule;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class RepeatedPatternRuleTransformer implements PatternRuleTransformer {
  
  protected int defaultMaxDistanceTokens = 60; // number of tokens 
  protected final Language transformerLanguage;

  public RepeatedPatternRuleTransformer(Language lang) {
    transformerLanguage = lang;
  }

  /**
   * Wrapper for loaded {@link AbstractPatternRule} instances to act as text-level rules
   */
  public class RepeatedPatternRule extends TextLevelRule {

    protected final Language ruleLanguage;
    
    RepeatedPatternRule(List<AbstractPatternRule> rules, Language lang) {
      this.rules = Collections.unmodifiableList(rules);
      this.ruleLanguage = lang;
      setPremium(rules.stream().anyMatch(r -> r.isPremium()));
    }

    private final List<AbstractPatternRule> rules;

    public List<AbstractPatternRule> getWrappedRules() {
      return rules;
    }

    @Override
    public String getId() {
      return rules.get(0).getId();
    }

    @Override
    public String getDescription() {
      return rules.get(0).getDescription();
    }
    
    @Override
    public RuleMatch[] match(List<AnalyzedSentence> sentences) throws IOException {
      List<RuleMatch> matches = new ArrayList<>();
      int offsetChars = 0;
      int offsetTokens = 0;
      int prevFromToken = 0;
      int prevMatches = 0;
      // we need to adjust offsets since each pattern rule returns offsets relative to the sentence, not text
      for (AnalyzedSentence s : sentences) {
        List<RuleMatch> sentenceMatches = new ArrayList<>();
        for (AbstractPatternRule rule : rules) {
          RuleMatch[] ruleMatches = rule.match(s);
          sentenceMatches.addAll(Arrays.asList(ruleMatches));
        }
        sentenceMatches = new SameRuleGroupFilter().filter(sentenceMatches);
        // no sorting: SameRuleGroupFilter sorts rule matches already
        int sentenceLenghtTokens = s.getTokensWithoutWhitespace().length;
        for (RuleMatch m : sentenceMatches) {
          int fromToken = 0;
          while (fromToken < sentenceLenghtTokens
              && s.getTokensWithoutWhitespace()[fromToken].getStartPos() < m.getFromPos()) {
            fromToken++;
          }
          fromToken += offsetTokens;
          int fromPos = m.getFromPos() + offsetChars;
          int toPos = m.getToPos() + offsetChars;
          m.setOffsetPosition(fromPos, toPos);
          int maxDistanceTokens = m.getRule().getDistanceTokens();
          if (maxDistanceTokens < 1) {
            maxDistanceTokens = defaultMaxDistanceTokens;
          }
          if (fromToken - prevFromToken <= maxDistanceTokens && prevMatches >= m.getRule().getMinPrevMatches()) {
            matches.add(m);
          }
          prevFromToken = fromToken;
          prevMatches++;
        }
        offsetChars += s.getText().length();
        offsetTokens += sentenceLenghtTokens - 1; // -1 -> not counting SENT_START
      }
      return matches.toArray(new RuleMatch[0]); 
    }

    @Override
    public int minToCheckParagraph() {
      // TODO: what should we use here? calculate based on min_prev_matches?
      return 0;
    }
    
    @Override
    public boolean supportsLanguage(Language language) {
      return language.equalsConsiderVariantsIfSpecified(this.ruleLanguage);
    }

  }

  @Override
  public TransformedRules apply(List<AbstractPatternRule> patternRules) {
    List<AbstractPatternRule> remaining = new ArrayList<>();
    Map<String, List<AbstractPatternRule>> toTransform = new HashMap<>();
    // rules in a rule group / with the same ID should be combined so repetitions of similar patterns are matched
    for (AbstractPatternRule abstractPatternRule : patternRules) {
      if (abstractPatternRule.getMinPrevMatches() > 0) {
        toTransform.compute(abstractPatternRule.getId(), (id, rules) -> {
          if (rules == null) {
            return new ArrayList<>(Collections.singletonList(abstractPatternRule));
          } else {
            rules.add(abstractPatternRule);
            return rules;
          }
        });
      } else {
        remaining.add(abstractPatternRule);
      }
    }
    List<Rule> transformed = toTransform.values().stream()
      .map(group -> new RepeatedPatternRule(group, transformerLanguage))
      .collect(Collectors.toList());

    return new TransformedRules(remaining, transformed);
  }
 }
