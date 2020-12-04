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

import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedSentence;
import org.languagetool.Language;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A base class for {@link PatternToken}-based rules.
 * It's public for implementation reasons and should not be used outside LanguageTool.
 */
public abstract class AbstractTokenBasedRule extends AbstractPatternRule {
  // Tokens used for fast checking whether a rule can ever match.
  @Nullable
  private final String[] inflectedRuleTokens;

  @Nullable
  private final String[][] formHints;

  protected AbstractTokenBasedRule(String id, String description, Language language, List<PatternToken> patternTokens, boolean getUnified) {
    super(id, description, language, patternTokens, getUnified);

    Set<String> inflectedRuleTokens = new HashSet<>();
    Set<Set<String>> formHints = new HashSet<>();

    for (PatternToken pToken : patternTokens) {
      if (pToken.isInflected() && !pToken.getNegation() && pToken.hasStringThatMustMatch() && !pToken.isRegularExpression()) {
        inflectedRuleTokens.add(Objects.requireNonNull(pToken.getString()).toLowerCase());
      }
      Set<String> hints = pToken.calcFormHints();
      if (hints != null) {
        formHints.add(hints.stream().map(String::toLowerCase).collect(Collectors.toSet()));
      }
    }

    this.inflectedRuleTokens = inflectedRuleTokens.isEmpty() ? null : inflectedRuleTokens.toArray(new String[0]);
    this.formHints = formHints.isEmpty() ? null : formHints.stream()
      .map(set -> set.toArray(new String[0]))
      .sorted(Comparator
        .comparing((String[] a) -> a.length)
        .thenComparing((String[] a) -> -Arrays.stream(a).mapToInt(String::length).min().orElse(0)))
      .toArray(String[][]::new);
  }

  /**
   * A fast check whether this rule can be ignored for the given sentence
   * because it can never match. Used for performance optimization.
   */
  protected boolean canBeIgnoredFor(AnalyzedSentence sentence) {
    if (inflectedRuleTokens != null) {
      for (String token : inflectedRuleTokens) {
        if (!sentence.getLemmaSet().contains(token)) {
          return true;
        }
      }
    }
    if (formHints != null) {
      Set<String> tokenSet = sentence.getTokenSet();
      for (String[] hints : formHints) {
        if (!containsAny(tokenSet, hints)) {
          return true;
        }
      }
    }
    return false;
  }

  private static boolean containsAny(Set<String> set, String[] elements) {
    for (String hint : elements) {
      if (set.contains(hint)) {
        return true;
      }
    }
    return false;
  }

}
