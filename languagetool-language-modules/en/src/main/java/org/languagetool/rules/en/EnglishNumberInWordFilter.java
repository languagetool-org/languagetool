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
package org.languagetool.rules.en;

import org.languagetool.JLanguageTool;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.rules.AbstractNumberInWordFilter;

import java.io.IOException;
import java.util.*;

public class EnglishNumberInWordFilter extends AbstractNumberInWordFilter {

  private static MorfologikAmericanSpellerRule englishSpellerRule;

  public EnglishNumberInWordFilter() throws IOException {
    super(new AmericanEnglish());
    ResourceBundle messages = JLanguageTool.getDataBroker().getResourceBundle(JLanguageTool.MESSAGE_BUNDLE, new Locale(language.getShortCode()));
    if (englishSpellerRule == null) {
      englishSpellerRule = new MorfologikAmericanSpellerRule(messages, new AmericanEnglish());
    }
  }
  
  @Override
  public boolean isMisspelled(String word) throws IOException {
    //return !EnglishTagger.INSTANCE.tag(Arrays.asList(word)).get(0).isTagged();
    return englishSpellerRule.isMisspelled(word);
  }

  @Override
  protected List<String> getSuggestions(String word) throws IOException {
    return englishSpellerRule.getSpellingSuggestions(word);
  }
}
