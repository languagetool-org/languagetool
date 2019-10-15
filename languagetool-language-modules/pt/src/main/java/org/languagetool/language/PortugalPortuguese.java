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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.languagetool.Language;
import org.languagetool.UserConfig;
import org.languagetool.rules.*;
import org.languagetool.rules.pt.PostReformPortugueseCompoundRule;
import org.languagetool.rules.pt.PostReformPortugueseDashRule;
import org.languagetool.rules.pt.PortugalPortugueseReplaceRule;
import org.languagetool.rules.pt.PortugueseAgreementReplaceRule;

public class PortugalPortuguese extends Portuguese {

  @Override
  public String getName() {
    return "Portuguese (Portugal)";
  }

  @Override
  public String[] getCountries() {
    return new String[]{"PT"};
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) throws IOException {
    List<Rule> rules = new ArrayList<>();
    rules.addAll(super.getRelevantRules(messages, userConfig, motherTongue, altLanguages));
    rules.add(new PostReformPortugueseCompoundRule(messages));
    rules.add(new PostReformPortugueseDashRule());
    rules.add(new PortugalPortugueseReplaceRule(messages));
    rules.add(new PortugueseAgreementReplaceRule(messages));
    return rules;
  }

  @Override
  public int getPriorityForId(String id) {
    switch (id) {
      case "PT_COMPOUNDS_POST_REFORM":         return  1;
      case "PORTUGUESE_OLD_SPELLING_INTERNAL": return -9;
    }
    return super.getPriorityForId(id);
  }
}
