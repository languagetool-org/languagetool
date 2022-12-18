/* LanguageTool, a natural language style checker
 * Copyright (C) 2021 Jaume Ortolà
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

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.rules.patterns.RuleFilter;
import org.languagetool.rules.spelling.SpellingCheckRule;
import org.languagetool.tagging.Tagger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractSuppressMisspelledSuggestionsFilter extends RuleFilter {

  protected final Language language;
  protected Tagger tagger;

  protected AbstractSuppressMisspelledSuggestionsFilter(Language language) {
    this.language = language;
    this.tagger = language.getTagger();
  }

  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos,
      AnalyzedTokenReadings[] patternTokens) throws IOException {
    RuleMatch ruleMatch = match;
    List<String> replacements = match.getSuggestedReplacements();
    List<String> newReplacements = new ArrayList<>();
    String suppressMatch = getRequired("suppressMatch", arguments);
    String suppressPostag = getOptional("SuppressPostag", arguments);
    List<AnalyzedTokenReadings> atrs = new ArrayList<>();
    if (tagger != null && suppressPostag != null) {
      atrs = tagger.tag(replacements);
    }
    for (int i = 0; i < replacements.size(); i++) {
      if (!isMisspelled(replacements.get(i))) {
        if (tagger != null && suppressPostag != null) {
          if (!atrs.get(i).matchesPosTagRegex(suppressPostag)) {
            newReplacements.add(replacements.get(i));
          }
        } else {
          newReplacements.add(replacements.get(i));
        }
      }
    }
    boolean bSuppressMatch = true;
    if (suppressMatch != null && suppressMatch.equalsIgnoreCase("false")) {
      bSuppressMatch = false;
    }
    if (newReplacements.isEmpty() && bSuppressMatch) {
      return null;
    } else {
      ruleMatch.setSuggestedReplacements(newReplacements);
      return ruleMatch;
    }
  }

  public boolean isMisspelled(String s) throws IOException {
    SpellingCheckRule spellerRule = language.getDefaultSpellingRule();
    if (spellerRule == null)
      return false;

    try {
      List<String> tokens = language.getWordTokenizer().tokenize(s);
      boolean isMisspelled = false;
      for (String token : tokens) {
        isMisspelled = isMisspelled || (spellerRule != null && spellerRule.isMisspelled(token));
      }
      return isMisspelled;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}