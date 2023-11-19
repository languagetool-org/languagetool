/* LanguageTool, a natural language style checker
 * Copyright (C) 2019 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules;

import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Filter suggestions against a rule. If the rule triggers an error for that suggestion,
 * it is filtered out.
 * @since 4.7
 */
public class SuggestionFilter {

  private final Rule rule;
  private final JLanguageTool lt;

  public SuggestionFilter(Rule rule, Language lang) {
    this.rule = Objects.requireNonNull(rule);
    this.lt = lang.createDefaultJLanguageTool();
  }

  public List<String> filter(List<String> replacements, String template) {
    List<String> newReplacements = new ArrayList<>();
    for (String repl : replacements) {
      try {
        List<AnalyzedSentence> analyzedSentences = lt.analyzeText(template.replace("{}", repl));
        RuleMatch[] matches = rule.match(analyzedSentences.get(0));
        if (matches.length == 0) {
          newReplacements.add(repl);
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    return newReplacements;
  }
  
}
