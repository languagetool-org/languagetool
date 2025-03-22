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
import org.languagetool.GlobalConfig;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.UserConfig;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.rules.Rule;
import org.languagetool.rules.de.GermanCompoundRule;
import org.languagetool.rules.de.GermanSpellerRule;

import java.io.IOException;
import java.util.*;

import static org.languagetool.JLanguageTool.getDataBroker;

public class GermanyGerman extends German {
  private static final String GERMANY_GERMAN_SHORT_CODE = "de-DE";

  /**
   * @deprecated don't use this method besides the inheritance or core code. Languages are not supposed to be
   * instantiated multiple times. They may contain heavy data which may waste the memory.
   * Use {@link #getInstance()} instead.
   */
  @Deprecated
  public GermanyGerman() {
  }

  @Override
  public String[] getCountries() {
    return new String[]{"DE"};
  }

  @Override
  public String getName() {
    return "German (Germany)";
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) throws IOException {
    List<Rule> rules = new ArrayList<>(super.getRelevantRules(messages, userConfig, motherTongue, altLanguages));
    rules.add(new GermanCompoundRule(messages, this, userConfig));
    return rules;
  }

  @Override
  public List<Rule> getRelevantLanguageModelCapableRules(ResourceBundle messages, @Nullable LanguageModel languageModel, GlobalConfig globalConfig, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) throws IOException {
    List<Rule> rules = new ArrayList<>(super.getRelevantLanguageModelCapableRules(messages, languageModel, globalConfig, userConfig, motherTongue, altLanguages));
    Rule rule = userConfig == null && languageModel == null && altLanguages.isEmpty()
                ? getDefaultSpellingRule()
                : new GermanSpellerRule(messages, this, userConfig, null, altLanguages, languageModel);
    rules.add(rule);
    return rules;
  }

  @Override
  public List<String> getRuleFileNames() {
    List<String> ruleFileNames = new ArrayList<>(super.getRuleFileNames());
    ruleFileNames.add(getDataBroker().getRulesDir() + "/de/de-DE-AT/grammar.xml");
    return Collections.unmodifiableList(ruleFileNames);
  }

  @Override
  public boolean isVariant() {
    return true;
  }

  public static @NotNull German getInstance() {
    Language language = Objects.requireNonNull(Languages.getLanguageForShortCode(GERMANY_GERMAN_SHORT_CODE));
    if (language instanceof German germanyGerman) { // cannot use GermanyGerman here as in premium GERMANY_GERMAN_SHORT_CODE returns GermanyGermanPremium
      return germanyGerman;
    }
    throw new RuntimeException("GermanyGerman(Premium) language expected, got " + language);
  }
}
