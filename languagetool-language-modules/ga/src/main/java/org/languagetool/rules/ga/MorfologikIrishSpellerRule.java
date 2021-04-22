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

package org.languagetool.rules.ga;

import java.io.IOException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import org.languagetool.Language;
import org.languagetool.UserConfig;
import org.languagetool.rules.Categories;
import org.languagetool.rules.Example;
import org.languagetool.rules.spelling.morfologik.MorfologikSpellerRule;
import org.languagetool.tagging.ga.Utils;

public final class MorfologikIrishSpellerRule extends MorfologikSpellerRule {

  private static final String RESOURCE_FILENAME = "/ga/hunspell/ga_IE.dict";

  private static final Pattern IRISH_TOKENIZING_CHARS = Pattern.compile("-");

  public MorfologikIrishSpellerRule(ResourceBundle messages,
                                     Language language, UserConfig userConfig, List<Language> altLanguages) throws IOException {
    super(messages, language, userConfig, altLanguages);
    super.ignoreWordsWithLength = 1;
    setCategory(Categories.TYPOS.getCategory(messages));
    addExamplePair(Example.wrong("Tá <marker>botun</marker> san abairt seo."),
                   Example.fixed("Tá <marker>botún</marker> san abairt seo."));
    // this.setIgnoreTaggedWords();
  }

  @Override
  public String getFileName() {
    return RESOURCE_FILENAME;
  }

  @Override
  public String getId() {
    return "MORFOLOGIK_RULE_GA_IE";
  }

  @Override
  public Pattern tokenizingPattern() {
    return IRISH_TOKENIZING_CHARS;
  }

  @Override
  public boolean isMisspelled(String word) throws IOException {
    String checkWord = word;
    if (Utils.isAllMathsChars(word)) {
      checkWord = Utils.simplifyMathematical(word);
    } else if (Utils.isAllHalfWidthChars(word)) {
      checkWord = Utils.halfwidthLatinToLatin(word);
    }
    return super.isMisspelled(checkWord);
  }

}
