/* LanguageTool, a natural language style checker 
 * Copyright (C) 2023 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.ca;

import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.language.Catalan;
import org.languagetool.rules.AbstractSuppressIfAnyRuleMatchesFilter;
import org.languagetool.rules.Rule;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SuppressIfAnyRuleMatchesFilter extends AbstractSuppressIfAnyRuleMatchesFilter {
  private final static JLanguageTool lt = Languages.getLanguageForShortCode("ca-ES").createDefaultJLanguageTool();

  private final List<String> ruleIds = Arrays.asList("QUE_INICIAL_AMBACCENT_NOVERB",
    "QUE_INICIAL_SENSEACCENT_HO", "QUE_INICIAL_AMBACCENT_VERB", "MES2");
  
  @Override
  protected JLanguageTool getJLanguageTool() {
    return lt;
  }

  protected List<String> getRuleIdsToCheck() {
    return ruleIds;
  }
}
