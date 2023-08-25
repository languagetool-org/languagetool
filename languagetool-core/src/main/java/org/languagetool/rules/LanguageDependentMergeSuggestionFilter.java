/*
 * LanguageTool, a natural language style checker
 * Copyright (c) 2023.  Stefan Viol (https://stevio.de)
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 *  USA
 */

package org.languagetool.rules;

import org.languagetool.Language;
import org.languagetool.markup.AnnotatedText;
import org.languagetool.rules.patterns.RuleSet;

import java.util.List;
import java.util.Set;

public class LanguageDependentMergeSuggestionFilter implements RuleMatchFilter {

  private final Language language;
  private final Set<String> enabledRules;
  public LanguageDependentMergeSuggestionFilter(Language language, RuleSet rules) {
    this.language = language;
    this.enabledRules = rules.allRuleIds();
  }

  @Override
  public List<RuleMatch> filter(List<RuleMatch> ruleMatches, AnnotatedText text) {
    return language.mergeSuggestions(ruleMatches, text, enabledRules);
  }
}
