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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.ca.CatalanTagger;

public class ValencianCatalan extends Catalan {

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

  @Override
  public List<String> getDefaultEnabledRulesForVariant() {
    List<String> rules = Arrays.asList("EXIGEIX_VERBS_VALENCIANS", "EXIGEIX_ACCENTUACIO_VALENCIANA",
        "EXIGEIX_POSSESSIUS_U", "EXIGEIX_VERBS_EIX", "EXIGEIX_VERBS_ISC", "PER_PER_A_INFINITIU", "FINS_EL_AVL");
    return Collections.unmodifiableList(rules);
  }

  @Override
  public List<String> getDefaultDisabledRulesForVariant() {
    // Important: Java rules are not disabled here
    List<String> rules = Arrays.asList("EXIGEIX_VERBS_CENTRAL", "EXIGEIX_ACCENTUACIO_GENERAL", "EXIGEIX_POSSESSIUS_V",
        "EVITA_PRONOMS_VALENCIANS", "EVITA_DEMOSTRATIUS_EIXE", "VOCABULARI_VALENCIA", "EXIGEIX_US", "FINS_EL_GENERAL");
    return Collections.unmodifiableList(rules);
  }
  
}
