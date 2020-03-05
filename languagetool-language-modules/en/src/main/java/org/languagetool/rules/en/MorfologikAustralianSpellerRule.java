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

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.languagetool.GlobalConfig;
import org.languagetool.Language;
import org.languagetool.UserConfig;
import org.languagetool.languagemodel.LanguageModel;

public final class MorfologikAustralianSpellerRule extends AbstractEnglishSpellerRule {

  private static final String RESOURCE_FILENAME = "/en/hunspell/en_AU.dict";
  private static final String LANGUAGE_SPECIFIC_PLAIN_TEXT_DICT = "en/hunspell/spelling_en-AU.txt";
  private static final Map<String,String> US_ENGLISH = loadWordlist("en/en-US-GB.txt", 0);

  public MorfologikAustralianSpellerRule(ResourceBundle messages,
                                         Language language, UserConfig userConfig, List<Language> altLanguages) throws IOException {
    super(messages, language, userConfig, altLanguages);
  }

  /**
   * @since 4.9
   */
  public MorfologikAustralianSpellerRule(ResourceBundle messages, Language language, GlobalConfig globalConfig, UserConfig userConfig, List<Language> altLanguages, LanguageModel languageModel, Language motherTongue) throws IOException {
    super(messages, language, globalConfig, userConfig, altLanguages, languageModel, motherTongue);
  }

  @Override
  protected VariantInfo isValidInOtherVariant(String word) {
    String otherVariant = US_ENGLISH.get(word.toLowerCase());
    if (otherVariant != null) {
      return new VariantInfo("American English", otherVariant);
    }
    return null;
  }

  @Override
  public String getFileName() {
    return RESOURCE_FILENAME;
  }

  @Override
  public String getId() {
    return "MORFOLOGIK_RULE_EN_AU";
  }

  @Override
  public String getLanguageVariantSpellingFileName() {
    return LANGUAGE_SPECIFIC_PLAIN_TEXT_DICT;
  }
}
