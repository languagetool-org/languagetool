/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Marcin Miłkowski (http://www.languagetool.org)
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import org.jetbrains.annotations.Nullable;
import org.languagetool.GlobalConfig;
import org.languagetool.Language;
import org.languagetool.UserConfig;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.rules.Rule;
import org.languagetool.rules.en.MorfologikAustralianSpellerRule;
import org.languagetool.rules.en.UnitConversionRuleImperial;
import org.languagetool.rules.spelling.SpellingCheckRule;

public class AustralianEnglish extends English {

  @Override
  public String[] getCountries() {
    return new String[]{"AU"};
  }

  @Override
  public String getName() {
    return "English (Australian)";
  }

  @Override
  public SpellingCheckRule createDefaultSpellingRule(ResourceBundle messages) throws IOException {
    return new MorfologikAustralianSpellerRule(messages, this, null, Collections.emptyList());
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) throws IOException {
    List<Rule> rules = new ArrayList<>();
    rules.addAll(super.getRelevantRules(messages, userConfig, motherTongue, altLanguages));    
    rules.add(new UnitConversionRuleImperial(messages));
    return rules;
  }

  @Override
  public List<Rule> getRelevantLanguageModelCapableRules(ResourceBundle messages, @Nullable LanguageModel lm, GlobalConfig globalConfig, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) throws IOException {
    List<Rule> rules = new ArrayList<>(super.getRelevantLanguageModelCapableRules(messages, lm, globalConfig, userConfig, motherTongue, altLanguages));
    rules.add(new MorfologikAustralianSpellerRule(messages, this, globalConfig, userConfig, altLanguages, lm, motherTongue));
    return rules;
  }

}
