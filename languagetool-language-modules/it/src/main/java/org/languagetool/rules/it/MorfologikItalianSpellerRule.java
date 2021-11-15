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

package org.languagetool.rules.it;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.languagetool.Language;
import org.languagetool.UserConfig;
import org.languagetool.rules.SuggestedReplacement;
import org.languagetool.rules.spelling.morfologik.MorfologikSpellerRule;
import org.languagetool.tools.StringTools;

public final class MorfologikItalianSpellerRule extends MorfologikSpellerRule {

  private static final String RESOURCE_FILENAME = "/it/hunspell/it_IT.dict";

  public MorfologikItalianSpellerRule(ResourceBundle messages, Language language, UserConfig userConfig,
      List<Language> altLanguages) throws IOException {
    super(messages, language, userConfig, altLanguages);
  }

  @Override
  public String getFileName() {
    return RESOURCE_FILENAME;
  }

  @Override
  public String getId() {
    return "MORFOLOGIK_RULE_IT_IT";
  }

  protected List<SuggestedReplacement> orderSuggestions(List<SuggestedReplacement> suggestions, String word) {
    List<SuggestedReplacement> newSuggestions = new ArrayList<>();
    List<String> originalSuggestionsStr = new ArrayList<>();
    for (SuggestedReplacement suggestion : suggestions) {
      originalSuggestionsStr.add(suggestion.getReplacement());
    }
    for (SuggestedReplacement suggestion : suggestions) {
      String suggestionStr = suggestion.getReplacement();
      // original word is not capitalized, suggestion is capitalized,
      // and lowercase suggestion also exists
      if (!StringTools.isCapitalizedWord(word) && StringTools.isCapitalizedWord(suggestionStr)
          && (originalSuggestionsStr.contains(suggestionStr.toLowerCase()))) {
        continue;
      }
      newSuggestions.add(suggestion);
    }
    return newSuggestions;

  }

}
