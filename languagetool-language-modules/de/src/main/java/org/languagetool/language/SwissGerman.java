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

import org.languagetool.rules.Rule;
import org.languagetool.rules.de.SwissGermanSpellerRule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

@SuppressWarnings("deprecation")
public class SwissGerman extends German {

  @Override
  public String[] getCountries() {
    return new String[]{"CH"};
  }

  @Override
  public String getName() {
    return "German (Swiss)";
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages) throws IOException {
    List<Rule> rules = new ArrayList<>(super.getRelevantRules(messages));
    rules.add(new SwissGermanSpellerRule(messages, this));
    return rules;
  }
  
}
