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
import java.util.*;

import org.jetbrains.annotations.Nullable;
import org.languagetool.GlobalConfig;
import org.languagetool.Language;
import org.languagetool.UserConfig;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.rules.Rule;
import org.languagetool.rules.en.BritishReplaceRule;
import org.languagetool.rules.en.MorfologikBritishSpellerRule;
import org.languagetool.rules.en.UnitConversionRuleImperial;
import org.languagetool.rules.spelling.SpellingCheckRule;

public class BritishEnglish extends English {

  @Override
  public String[] getCountries() {
    return new String[]{"GB"};
  }

  @Override
  public String getName() {
    return "English (GB)";
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) throws IOException {
    List<Rule> rules = new ArrayList<>();
    rules.addAll(super.getRelevantRules(messages, userConfig, motherTongue, altLanguages));
    rules.add(new BritishReplaceRule(messages, "/en/en-GB/replace.txt"));
    rules.add(new UnitConversionRuleImperial(messages));
    return rules;
  }

  @Override
  public SpellingCheckRule createDefaultSpellingRule(ResourceBundle messages) throws IOException {
    return new MorfologikBritishSpellerRule(messages, this, null, Collections.emptyList());
  }

  @Override
  public List<Rule> getRelevantLanguageModelCapableRules(ResourceBundle messages, @Nullable LanguageModel lm, GlobalConfig globalConfig, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) throws IOException {
    List<Rule> rules = new ArrayList<>(super.getRelevantLanguageModelCapableRules(messages, lm, globalConfig, userConfig, motherTongue, altLanguages));
    rules.add(new MorfologikBritishSpellerRule(messages, this, globalConfig, userConfig, altLanguages, lm, motherTongue));
    return rules;
  }

  private final static Map<String, Integer> id2prio = new HashMap<>();
  static {
    id2prio.put("OXFORD_SPELLING_ISATION_NOUNS", -20);
    id2prio.put("OXFORD_SPELLING_ISE_VERBS", -21);
    id2prio.put("OXFORD_SPELLING_IZE", -22);
  }

  @Override
  public Map<String, Integer> getPriorityMap() {
    return id2prio;
  }

  @Override
  protected int getPriorityForId(String id) {
    Integer prio = id2prio.get(id);
    if (prio != null) {
      return prio;
    }
    return super.getPriorityForId(id);
  }
}
