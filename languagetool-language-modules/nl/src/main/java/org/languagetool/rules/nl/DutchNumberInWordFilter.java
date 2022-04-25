/* LanguageTool, a natural language style checker
 * Copyright (C) 2021 Stefan Viol
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
package org.languagetool.rules.nl;

import org.languagetool.JLanguageTool;
import org.languagetool.language.Dutch;
import org.languagetool.rules.AbstractNumberInWordFilter;

import java.io.IOException;
import java.util.*;

public class DutchNumberInWordFilter extends AbstractNumberInWordFilter {

  private static MorfologikDutchSpellerRule dutchSpellerRule;

  public DutchNumberInWordFilter() throws IOException {
    super(new Dutch());
    ResourceBundle messages = JLanguageTool.getDataBroker().getResourceBundle(JLanguageTool.MESSAGE_BUNDLE, new Locale(language.getShortCode()));
    if (dutchSpellerRule == null) {
      dutchSpellerRule = new MorfologikDutchSpellerRule(messages, new Dutch(), null, Collections.emptyList());
    }
  }
  
  @Override
  public boolean isMisspelled(String word) throws IOException {
    return dutchSpellerRule.isMisspelled(word);
  }

  @Override
  protected List<String> getSuggestions(String word) throws IOException {
    return dutchSpellerRule.getSpellingSuggestions(word);
  }
}
