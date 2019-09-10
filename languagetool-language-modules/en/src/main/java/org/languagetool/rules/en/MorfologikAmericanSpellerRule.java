/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Marcin Miłkowski (http://www.languagetool.org)
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

import org.languagetool.Experimental;
import org.languagetool.Language;
import org.languagetool.UserConfig;
import org.languagetool.languagemodel.LanguageModel;

import java.io.IOException;
import java.util.*;

public final class MorfologikAmericanSpellerRule extends AbstractEnglishSpellerRule {

  public static final String RULE_ID = "MORFOLOGIK_RULE_EN_US";

  private static final String RESOURCE_FILENAME = "/en/hunspell/en_US.dict";
  private static final String LANGUAGE_SPECIFIC_PLAIN_TEXT_DICT = "en/hunspell/spelling_en-US.txt";
  private static final Map<String,String> BRITISH_ENGLISH = loadWordlist("en/en-US-GB.txt", 1);

  public MorfologikAmericanSpellerRule(ResourceBundle messages, Language language) throws IOException {
    super(messages, language, null, Collections.emptyList());
  }

  @Override
  protected VariantInfo isValidInOtherVariant(String word) {
    String otherVariant = BRITISH_ENGLISH.get(word);
    if (otherVariant != null) {
      return new VariantInfo("British English", otherVariant);
    }
    return null;
  }

  /**
   * @since 4.2
   */
  public MorfologikAmericanSpellerRule(ResourceBundle messages, Language language, UserConfig userConfig, List<Language> altLanguages) throws IOException {
    super(messages, language, userConfig, altLanguages);
  }

  /**
   * @since 4.5
   */
  @Experimental
  public MorfologikAmericanSpellerRule(ResourceBundle messages, Language language, UserConfig userConfig, List<Language> altLanguages, LanguageModel languageModel) throws IOException {
    super(messages, language, userConfig, altLanguages, languageModel);
  }

  @Override
  public String getFileName() {
    return RESOURCE_FILENAME;
  }

  @Override
  public String getId() {
    return RULE_ID;
  }

  @Override
  public String getLanguageVariantSpellingFileName() {
    return LANGUAGE_SPECIFIC_PLAIN_TEXT_DICT;
  }

  @Override
  protected List<String> getAdditionalTopSuggestions(List<String> suggestions, String word) throws IOException {
    if ("automize".equals(word)) {
      return Arrays.asList("automate");
    } else if ("automized".equals(word)) {
      return Arrays.asList("automated");
    } else if ("automizing".equals(word)) {
      return Arrays.asList("automating");
    } else if ("automizes".equals(word)) {
      return Arrays.asList("automates");
    }
    return super.getAdditionalTopSuggestions(suggestions, word);
  }

}
