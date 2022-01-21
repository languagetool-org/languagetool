/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.language;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.languagetool.*;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.de.SwissCompoundRule;
import org.languagetool.rules.de.SwissGermanSpellerRule;
import org.languagetool.rules.spelling.SpellingCheckRule;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.de.SwissGermanTagger;

import java.io.IOException;
import java.util.*;

@SuppressWarnings("deprecation")
public class SwissGerman extends German {

  @NotNull
  @Override
  public Tagger createDefaultTagger() {
    return new SwissGermanTagger();
  }

  @Override
  public String[] getCountries() {
    return new String[]{"CH"};
  }

  @Override
  public String getName() {
    return "German (Swiss)";
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) throws IOException {
    List<Rule> rules = new ArrayList<>(super.getRelevantRules(messages, userConfig, motherTongue, altLanguages));
    rules.add(new SwissCompoundRule(messages, this, userConfig));
    return rules;
  }

  @Override
  public SpellingCheckRule createDefaultSpellingRule(ResourceBundle messages) throws IOException {
    return new SwissGermanSpellerRule(messages, this);
  }

  @Override
  public List<Rule> getRelevantLanguageModelCapableRules(ResourceBundle messages, @Nullable LanguageModel languageModel, GlobalConfig globalConfig, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) throws IOException {
    List<Rule> rules = new ArrayList<>(super.getRelevantLanguageModelCapableRules(messages, languageModel, globalConfig, userConfig, motherTongue, altLanguages));
    rules.add(new SwissGermanSpellerRule(messages, this,
      userConfig, languageModel));
    return rules;
  }

  @Override
  public boolean isVariant() {
    return true;
  }
  
  @Override
  public List<RuleMatch> adaptSuggestions(List<RuleMatch> ruleMatches, Set<String> enabledRules) {
    List<RuleMatch> newRuleMatches = new ArrayList<>();
    for (RuleMatch rm : ruleMatches) {
      List<String> replacements = rm.getSuggestedReplacements();
      List<String> newReplacements = new ArrayList<>();
      for (String s : replacements) {
        s = s.replaceAll("ß", "ss");
        newReplacements.add(s);
      }
      RuleMatch newMatch = new RuleMatch(rm, newReplacements);
      newRuleMatches.add(newMatch);
    }
    return newRuleMatches;
  }

  @Override
  public String getOpeningDoubleQuote() {
    return "«";
  }

  @Override
  public String getClosingDoubleQuote() {
    return "»";
  }
}
