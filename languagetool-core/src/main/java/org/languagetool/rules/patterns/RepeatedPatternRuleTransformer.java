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
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.TextLevelRule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RepeatedPatternRuleTransformer implements PatternRuleTransformer {
  
  protected int maxDistance = 350; // numer of tokens

  /**
   * Wrapper for loaded {@link AbstractPatternRule} instances to act as text-level rules
   */
  public class RepeatedPatternRule extends TextLevelRule {

    RepeatedPatternRule(AbstractPatternRule rule) {
      this.rule = rule;
    }

    private AbstractPatternRule rule;

    public AbstractPatternRule getWrappedRule() {
      return rule;
    }

    @Override
    public String getId() {
      return rule.getId();
    }

    @Override
    public String getDescription() {
      return rule.getDescription();
    }

    @Override
    public RuleMatch[] match(List<AnalyzedSentence> sentences) throws IOException {
      List<RuleMatch> matches = new ArrayList<>();
      int offset = 0;
      int prevFromPos = 0;
      int prevMatches = 0;
      // we need to adjust offsets since each pattern rule returns offsets relative to the sentence, not text
      for (AnalyzedSentence s : sentences) {
        RuleMatch[] sentenceMatches = rule.match(s);
        for (RuleMatch m : sentenceMatches) {
          int fromPos = m.getFromPos() + offset;
          int toPos = m.getToPos() + offset;
          m.setOffsetPosition(fromPos, toPos);
          if (fromPos - prevFromPos <= maxDistance && prevMatches > getMinPrevMatches()) {
            matches.add(m);
          }
          prevFromPos = fromPos;
          prevMatches++;
        }
        offset += s.getText().length();
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
      return rule.supportsLanguage(language);
    }
  }

  @Override
  public Optional<Rule> apply(AbstractPatternRule abstractPatternRule) {
    if (abstractPatternRule.getMinPrevMatches() > 0) {
      return Optional.of(new RepeatedPatternRule(abstractPatternRule));
    } else {
      return Optional.empty();
    }
  }
}
