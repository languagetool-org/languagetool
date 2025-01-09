/* LanguageTool, a natural language style checker 
 * Copyright (C) 2009 Daniel Naber (http://www.danielnaber.de)
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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.UserConfig;
import org.languagetool.rules.*;
import org.languagetool.rules.ca.*;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.synthesis.ca.CatalanSynthesizer;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.ca.CatalanTagger;

public class ValencianCatalan extends Catalan {

  private static final String LANGUAGE_SHORT_CODE = "ca-ES-valencia";

  private static volatile Throwable instantiationTraceValecian;

  public ValencianCatalan() {
    super(true);
    Throwable trace = instantiationTraceValecian;
    if (trace != null) {
      throw new RuntimeException("Language was already instantiated, see the cause stacktrace below.", trace);
    }
    instantiationTraceValecian = new Throwable();
  }

  public static @NotNull ValencianCatalan getInstance() {
    Language language = Objects.requireNonNull(Languages.getLanguageForShortCode(LANGUAGE_SHORT_CODE));
    if (language instanceof ValencianCatalan catalan) {
      return catalan;
    }
    throw new RuntimeException("ValencianCatalan language expected, got " + language);
  }

  @Override
  public String getName() {
    return "Catalan (Valencian)";
  }

  @Override
  public String[] getCountries() {
    return new String[]{"ES"};
  }

  @Override
  public String getVariant() {
    return "valencia";
  }
  
  @NotNull
  @Override
  public Tagger createDefaultTagger() {
    return CatalanTagger.INSTANCE_VAL;
  }

  @Nullable
  @Override
  public Synthesizer createDefaultSynthesizer() {
    return CatalanSynthesizer.INSTANCE_VAL;
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) throws IOException {
    List<Rule> relevantRules = new ArrayList<>(super.getRelevantRules(messages, userConfig, motherTongue,altLanguages));
    relevantRules.add(new WordCoherencyValencianRule(messages));
    return relevantRules;
  }

  @Override
  public List<String> getDefaultEnabledRulesForVariant() {
    List<String> rules = Arrays.asList("EXIGEIX_VERBS_VALENCIANS", "EXIGEIX_ACCENTUACIO_VALENCIANA",
        "EXIGEIX_POSSESSIUS_U", "EXIGEIX_VERBS_EIX", "EXIGEIX_VERBS_ISC", "PER_PER_A_INFINITIU", "FINS_EL_AVL", "LES_HA_FETES");
    return Collections.unmodifiableList(rules);
  }

  @Override
  public List<String> getDefaultDisabledRulesForVariant() {
    List<String> rules = Arrays.asList("EXIGEIX_VERBS_CENTRAL", "EXIGEIX_ACCENTUACIO_GENERAL", "EXIGEIX_POSSESSIUS_V",
        "EVITA_PRONOMS_VALENCIANS", "EVITA_DEMOSTRATIUS_EIXE", "VOCABULARI_VALENCIA", "EXIGEIX_US", "FINS_EL_GENERAL", 
        "EVITA_INFINITIUS_INDRE", "EVITA_DEMOSTRATIUS_ESTE");
    return Collections.unmodifiableList(rules);
  }
  
}
