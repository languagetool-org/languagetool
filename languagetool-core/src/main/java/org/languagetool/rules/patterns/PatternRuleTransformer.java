/*
 *  LanguageTool, a natural language style checker
 *  * Copyright (C) 2021 Fabian Richter
 *  *
 *  * This library is free software; you can redistribute it and/or
 *  * modify it under the terms of the GNU Lesser General Public
 *  * License as published by the Free Software Foundation; either
 *  * version 2.1 of the License, or (at your option) any later version.
 *  *
 *  * This library is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  * Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public
 *  * License along with this library; if not, write to the Free Software
 *  * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 *  * USA
 *
 */

package org.languagetool.rules.patterns;

import org.languagetool.rules.Rule;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Allows transformation of pattern rules to add additional matching/filtering logic
 * Used in {@link org.languagetool.JLanguageTool#transformPatternRules}
 * Return a value if a transform is applied, none otherwise
 * @since 5.6
 */
public interface PatternRuleTransformer extends Function<List<AbstractPatternRule>, PatternRuleTransformer.TransformedRules> {

  class TransformedRules {
    private final List<AbstractPatternRule> remainingRules;
    private final List<Rule> transformedRules;

    public TransformedRules(List<AbstractPatternRule> remainingRules, List<Rule> transformedRules) {
      this.remainingRules = Collections.unmodifiableList(remainingRules);
      this.transformedRules = Collections.unmodifiableList(transformedRules);
    }

    public List<AbstractPatternRule> getRemainingRules() {
      return remainingRules;
    }

    public List<Rule> getTransformedRules() {
      return transformedRules;
    }
  }

}
