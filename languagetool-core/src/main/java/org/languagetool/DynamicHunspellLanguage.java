/* LanguageTool, a natural language style checker
 * Copyright (C) 2019 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool;

import org.jetbrains.annotations.NotNull;
import org.languagetool.rules.Rule;
import org.languagetool.rules.spelling.hunspell.HunspellRule;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

class DynamicHunspellLanguage extends DynamicLanguage {

  DynamicHunspellLanguage(String name, String code, File dictPath) {
    super(name, code, dictPath);
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) throws IOException {
    HunspellRule r = new HunspellRule(JLanguageTool.getMessageBundle(Languages.getLanguageForShortCode("en-US")), this, userConfig) {
      @Override
      public String getId() {
        return code.toUpperCase() + "_SPELLER_RULE";
      }
      @NotNull
      @Override
      protected String getDictFilenameInResources(String langCountry) {
        return dictPath.getAbsolutePath().replaceAll(".dic$", "");
      }
      @Override
      public String getSpellingFileName() {
        return null;
      }
    };
    return Collections.singletonList(r);
  }

}
