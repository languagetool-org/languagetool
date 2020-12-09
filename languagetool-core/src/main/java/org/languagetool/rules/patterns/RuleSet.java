/* LanguageTool, a natural language style checker
 * Copyright (C) 2020 Daniel Naber (http://www.danielnaber.de)
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

import org.jetbrains.annotations.ApiStatus;
import org.languagetool.AnalyzedSentence;
import org.languagetool.rules.Rule;

import java.util.*;

/**
 * An object holding a set of rules with an optional possibility to fetch only the ones applicable for a given sentence
 * (via {@link #rulesForSentence}), to avoid even invoking the definitely inapplicable ones.
 * The filtering is based on cues provided by the rules, e.g. possible token strings or lemmas in pattern rules.
 *
 * @since 5.2
 */
@ApiStatus.Internal
public abstract class RuleSet {

  /**
   * @return all rules in this set, not filtered
   */
  public abstract List<Rule> allRules();

  /**
   * @return all rules from {@link #allRules} that might be applicable to the given sentence.
   */
  public abstract List<Rule> rulesForSentence(AnalyzedSentence sentence);

  /**
   * @return a simple RuleSet that returns all the rules from {@link #rulesForSentence}
   */
  public static RuleSet plain(List<Rule> rules) {
    List<Rule> allRules = Collections.unmodifiableList(rules);
    return new RuleSet() {
      @Override
      public List<Rule> allRules() {
        return allRules;
      }

      @Override
      public List<Rule> rulesForSentence(AnalyzedSentence sentence) {
        return allRules;
      }
    };
  }

  /**
   * @return a RuleSet whose {@link #rulesForSentence} excludes rules requiring token texts or lemmas
   * that don't occur in the given sentence
   */
  public static RuleSet textLemmaHinted(List<? extends Rule> rules) {
    return hinted(rules, true);
  }

  /**
   * @return a RuleSet whose {@link #rulesForSentence} excludes rules requiring token texts
   * that don't occur in the given sentence.
   */
  public static RuleSet textHinted(List<? extends Rule> rules) {
    return hinted(rules, false);
  }

  private static RuleSet hinted(List<? extends Rule> rules, boolean withLemmaHints) {
    List<Rule> allRules = Collections.unmodifiableList(rules);
    Map<String, BitSet> byToken = new HashMap<>();
    Map<String, BitSet> byLemma = new HashMap<>();
    BitSet unclassified = new BitSet();
    for (int i = 0; i < allRules.size(); i++) {
      Rule rule = allRules.get(i);
      if (rule instanceof AbstractTokenBasedRule) {
        String[] inflectedRuleTokens = new String[0];
        if (withLemmaHints) {
          inflectedRuleTokens = ((AbstractTokenBasedRule) rule).inflectedRuleTokens;
          if (inflectedRuleTokens != null) {
            byLemma.computeIfAbsent(inflectedRuleTokens[0], __ -> new BitSet()).set(i);
          }
        }
        String[][] formHints = ((AbstractTokenBasedRule) rule).formHints;
        if (formHints != null) {
          for (String token : formHints[0]) {
            byToken.computeIfAbsent(token, __ -> new BitSet()).set(i);
          }
        } else if (inflectedRuleTokens == null || !withLemmaHints) {
          unclassified.set(i);
        }
      } else {
        unclassified.set(i);
      }
    }
    return new RuleSet() {
      @Override
      public List<Rule> allRules() {
        return allRules;
      }

      @Override
      public List<Rule> rulesForSentence(AnalyzedSentence sentence) {
        BitSet included = new BitSet();
        included.or(unclassified);
        if (!byLemma.isEmpty()) {
          for (String lemma : sentence.getLemmaSet()) {
            BitSet set = byLemma.get(lemma);
            if (set != null) {
              included.or(set);
            }
          }
        }
        for (String token : sentence.getTokenSet()) {
          BitSet set = byToken.get(token);
          if (set != null) {
            included.or(set);
          }
        }
        return filterList(included, allRules);
      }
    };
  }

  @ApiStatus.Internal
  public static <T> List<T> filterList(BitSet includedIndices, List<T> list) {
    List<T> result = new ArrayList<>();
    int i = -1;
    while (true) {
      i = includedIndices.nextSetBit(i + 1);
      if (i < 0) break;
      result.add(list.get(i));
    }
    return result;
  }

}
