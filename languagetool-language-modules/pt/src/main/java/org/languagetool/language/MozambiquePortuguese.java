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

import org.languagetool.Language;
import org.languagetool.UserConfig;
import org.languagetool.rules.Rule;
import org.languagetool.rules.pt.PreReformPortugueseCompoundRule;
import org.languagetool.rules.pt.PreReformPortugueseDashRule;

import java.io.IOException;
import java.util.*;

/**
 * @since 3.6
 */
public class MozambiquePortuguese extends Portuguese {

  @Override
  public String[] getCountries() {
    return new String[]{"MZ"};
  }

  @Override
  public String getName() {
    return "Portuguese (Moçambique preAO)";
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) throws IOException {
    List<Rule> rules = new ArrayList<>();
    rules.addAll(super.getRelevantRules(messages, userConfig, motherTongue, altLanguages));
    rules.add(new PreReformPortugueseCompoundRule(messages, this, userConfig));
    rules.add(new PreReformPortugueseDashRule(messages));
    return rules;
  }

}
