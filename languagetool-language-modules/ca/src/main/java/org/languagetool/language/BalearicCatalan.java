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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.synthesis.ca.CatalanSynthesizer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class BalearicCatalan extends Catalan {

  private static final String LANGUAGE_SHORT_CODE = "ca-ES-balear";

  private static volatile Throwable instantiationTraceBalear;

  public BalearicCatalan() {
    super(true);
    Throwable trace = instantiationTraceBalear;
    if (trace != null) {
      throw new RuntimeException("Language was already instantiated, see the cause stacktrace below.", trace);
    }
    instantiationTraceBalear = new Throwable();
  }

  public static @NotNull BalearicCatalan getInstance() {
    Language language = Objects.requireNonNull(Languages.getLanguageForShortCode(LANGUAGE_SHORT_CODE));
    if (language instanceof BalearicCatalan catalan) {
      return catalan;
    }
    throw new RuntimeException("BalearicCatalan language expected, got " + language);
  }

  @Override
  public String getName() {
    return "Catalan (Balearic)";
  }

  @Override
  public String[] getCountries() {
    return new String[]{"ES"};
  }

  @Nullable
  @Override
  public Synthesizer createDefaultSynthesizer() {
    return CatalanSynthesizer.INSTANCE_BAL;
  }

  @Override
  public String getVariant() {
    // unlike Valencian (ca-ES-valencia) this code is not registered by IANA language subtag registry
    return "balear";
  }

  @Override
  public List<String> getDefaultEnabledRulesForVariant() {
    List<String> rules = Arrays.asList("EXIGEIX_VERBS_BALEARS");
    return Collections.unmodifiableList(rules);
  }

  @Override
  public List<String> getDefaultDisabledRulesForVariant() {
    List<String> rules = Arrays.asList("EXIGEIX_VERBS_CENTRAL","CA_SIMPLE_REPLACE_BALEARIC");
    return Collections.unmodifiableList(rules);
  }
  
}
