/* LanguageTool, a natural language style checker
 * Copyright (C) 2020 Daniel Naber (http://www.danielnaber.de)
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
import org.languagetool.rules.de.GermanCompoundRule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static org.languagetool.JLanguageTool.getDataBroker;

@SuppressWarnings("deprecation")
public abstract class NonSwissGerman extends German {

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) throws IOException {
    List<Rule> rules = new ArrayList<>(super.getRelevantRules(messages, userConfig, motherTongue, altLanguages));
    rules.add(new GermanCompoundRule(messages, this, userConfig));
    return rules;
  }

  public List<String> getRuleFileNames() {
    List<String> ruleFileNames = super.getRuleFileNames();
    ruleFileNames.add(getDataBroker().getRulesDir() + "/de/de-DE-AT/grammar.xml");  // rules relevant for de-DE and de-AT but not de-CH
    return ruleFileNames;
  }

}
