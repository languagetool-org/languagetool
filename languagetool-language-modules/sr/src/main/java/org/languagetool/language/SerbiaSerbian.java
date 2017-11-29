/* LanguageTool, a natural language style checker
 * Copyright (C) 2007 Daniel Naber (http://www.danielnaber.de)
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


import org.languagetool.rules.Rule;
import org.languagetool.rules.sr.ekavian.SimpleGrammarEkavianReplaceRule;
import org.languagetool.rules.sr.ekavian.MorfologikEkavianSpellerRule;
import org.languagetool.rules.sr.ekavian.SimpleStyleEkavianReplaceRule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/** @since 4.0 */
public class SerbiaSerbian extends Serbian {

  @Override
  public String[] getCountries() {
    return new String[]{"RS"};
  }

  @Override
  public String getName() {
    return "Serbian (Serbia)";
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages) throws IOException {
    List<Rule> rules = new ArrayList<>();
    rules.addAll(super.getRelevantRules(messages));
    rules.add(new MorfologikEkavianSpellerRule(messages, this));
    rules.add(new SimpleGrammarEkavianReplaceRule(messages));
    rules.add(new SimpleStyleEkavianReplaceRule(messages));
    return rules;
  }
}
