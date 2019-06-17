/* LanguageTool, a natural language style checker 
 * Copyright (C) 2017 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.de;

import org.languagetool.UserConfig;
import org.languagetool.language.German;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.rules.spelling.CachingWordListLoader;

import java.io.IOException;
import java.util.Collections;
import java.util.ResourceBundle;

/**
 * @since 3.9
 */
public class SwissGermanSpellerRule extends GermanSpellerRule {

  private final CachingWordListLoader wordListLoader = new CachingWordListLoader();
  private static final String LANGUAGE_SPECIFIC_PLAIN_TEXT_DICT = "de/hunspell/spelling-de-CH.txt";
  
  public SwissGermanSpellerRule(ResourceBundle messages, German language) {
    this(messages, language, null, null);
  }

  /**
   * @since 4.2
   */
  public SwissGermanSpellerRule(ResourceBundle messages, German language, UserConfig userConfig, LanguageModel lm) {
    super(messages, language, userConfig, LANGUAGE_SPECIFIC_PLAIN_TEXT_DICT, Collections.emptyList(), lm);
  }

  @Override
  public String getId() {
    return "SWISS_GERMAN_SPELLER_RULE";
  }

  @Override
  protected void init() throws IOException {
    super.init();
    for (String ignoreWord : wordListLoader.loadWords("/de/hunspell/spelling-de-CH.txt")) {
      addIgnoreWords(ignoreWord);
    }
  }

  @Override
  public String getLanguageVariantSpellingFileName() {
    return LANGUAGE_SPECIFIC_PLAIN_TEXT_DICT;
  }
}
