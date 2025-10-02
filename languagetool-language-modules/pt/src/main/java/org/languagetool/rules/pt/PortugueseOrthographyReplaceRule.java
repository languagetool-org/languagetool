/* LanguageTool, a natural language style checker
 * Copyright (C) 2005-2015 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.pt;

import org.languagetool.Language;
import org.languagetool.rules.AbstractSimpleReplaceRule;
import org.languagetool.rules.Categories;
import org.languagetool.rules.ITSIssueType;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Rule for simple and frequent one-to-one spelling fixes in Portuguese.
 *
 * @author p-goulart
 */
public class PortugueseOrthographyReplaceRule extends AbstractSimpleReplaceRule {

  public static final String PORTUGUESE_SIMPLE_REPLACE_ORTHOGRAPHY_RULE = "PT_SIMPLE_REPLACE_ORTHOGRAPHY";

  private static final Map<String, List<String>> wrongWords = loadFromPath("/pt/replace_orthography.txt");
  private static final Locale PT_LOCALE = new Locale("pt");

  @Override
  public Map<String, List<String>> getWrongWords() {
    return wrongWords;
  }

  public PortugueseOrthographyReplaceRule(ResourceBundle messages, Language language) {
    super(messages, language);
    super.setCategory(Categories.TYPOS.getCategory(messages));
    setLocQualityIssueType(ITSIssueType.Misspelling);
    useSubRuleSpecificIds();
  }

  @Override
  public String getId() {
    return PORTUGUESE_SIMPLE_REPLACE_ORTHOGRAPHY_RULE;
  }

  @Override
  public boolean isCaseSensitive() {
    return false;
  }

  @Override
  public Locale getLocale() {
    return PT_LOCALE;
  }

  @Override
  public String getDescription() {
    return messages.getString("desc_spelling");
  }

  @Override
  public String getShort() {
    return messages.getString("desc_spelling_short");
  }

  @Override
  public String getMessage(String token, List<String> replacements) {
    return messages.getString("spelling");
  }

}
