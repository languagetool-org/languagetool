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
package org.languagetool.rules.spelling.hunspell;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.languagetool.Language;
import org.languagetool.UserConfig;
import org.languagetool.rules.CorrectExample;
import org.languagetool.rules.IncorrectExample;

/**
 * Like {@link HunspellRule}, but does not offer suggestions for incorrect words
 * as that is very slow with Hunspell.
 */
public class HunspellNoSuggestionRule extends HunspellRule {

  public static final String RULE_ID = "HUNSPELL_NO_SUGGEST_RULE";

  public HunspellNoSuggestionRule(ResourceBundle messages, Language language, UserConfig userConfig, List<Language> altLanguages) {
    super(messages, language, userConfig, altLanguages);
  }

  /**
   * @since 3.3
   */
  public HunspellNoSuggestionRule(ResourceBundle messages, Language language, UserConfig userConfig, List<Language> altLanguages, IncorrectExample incorrectExample, CorrectExample correctedExample) {
    super(messages, language, userConfig, altLanguages);
    addExamplePair(incorrectExample, correctedExample);
  }

  @Override
  public String getId() {
    return RULE_ID;
  }

  @Override
  public String getDescription() {
    return messages.getString("desc_spelling_no_suggestions");
  }

  @Override
  public List<String> getSuggestions(String word) throws IOException {
    return new ArrayList<>();
  }
  
}
