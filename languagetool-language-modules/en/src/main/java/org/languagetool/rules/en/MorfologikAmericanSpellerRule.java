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

import org.languagetool.GlobalConfig;
import org.languagetool.Language;
import org.languagetool.UserConfig;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.rules.SuggestedReplacement;

import java.io.IOException;
import java.util.*;

public final class MorfologikAmericanSpellerRule extends AbstractEnglishSpellerRule {

  public static final String RULE_ID = "MORFOLOGIK_RULE_EN_US";

  private static final String RESOURCE_FILENAME = "/en/hunspell/en_US.dict";
  private static final String LANGUAGE_SPECIFIC_PLAIN_TEXT_DICT = "en/hunspell/spelling_en-US.txt";
  private static final Map<String,String> BRITISH_ENGLISH = loadWordlist("en/en-US-GB.txt", 1);

  public MorfologikAmericanSpellerRule(ResourceBundle messages, Language language) throws IOException {
    this(messages, language, null, null, Collections.emptyList(), null, null);
  }

  /**
   * @since 4.2
   */
  public MorfologikAmericanSpellerRule(ResourceBundle messages, Language language, UserConfig userConfig, List<Language> altLanguages) throws IOException {
    this(messages, language, null, userConfig, altLanguages, null, null);
  }

  /**
   * @since 4.9
   */
  public MorfologikAmericanSpellerRule(ResourceBundle messages, Language language, GlobalConfig globalConfig, UserConfig userConfig, List<Language> altLanguages, LanguageModel languageModel, Language motherTongue) throws IOException {
    super(messages, language, globalConfig, userConfig, altLanguages, languageModel, motherTongue);
  }

  @Override
  protected VariantInfo isValidInOtherVariant(String word) {
    String otherVariant = BRITISH_ENGLISH.get(word.toLowerCase());
    if (otherVariant != null) {
      return new VariantInfo("British English", otherVariant);
    }
    return null;
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
  protected List<SuggestedReplacement> getAdditionalTopSuggestions(List<SuggestedReplacement> suggestions, String word) throws IOException {
    List<String> s = null;
    if ("automize".equals(word)) {
      s =  Arrays.asList("automate");
    } else if ("automized".equals(word)) {
      s =  Arrays.asList("automated");
    } else if ("automizing".equals(word)) {
      s =  Arrays.asList("automating");
    } else if ("automizes".equals(word)) {
      s =  Arrays.asList("automates");
    }
    if (s != null) {
      return SuggestedReplacement.convert(s);
    } else {
      return super.getAdditionalTopSuggestions(suggestions, word);
    }
  }

}
