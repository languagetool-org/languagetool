/* LanguageTool, a natural language style checker
 * Copyright (C) 2019 Sohaib Afifi, Taha Zerrouki
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
package org.languagetool.rules.ar;

import org.languagetool.rules.AbstractSimpleReplaceRule;
import org.languagetool.rules.Categories;
import org.languagetool.rules.ITSIssueType;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * A rule that matches words or phrases which should not be used and suggests
 * correct ones instead.
 *
 * @author Sohaib AFIFI
 */
public class ArabicContractionSpellingRule extends AbstractSimpleReplaceRule {

  public static final String CONTRACTION_SPELLING_RULE = "AR_CONTRACTION_SPELLING";

  private static final Map<String, List<String>> wrongWords = loadFromPath("/ar/contractions.txt");
  private static final Locale AR_LOCALE = new Locale("ar");

  public ArabicContractionSpellingRule(ResourceBundle messages) throws IOException {
    super(messages);
    super.setCategory(Categories.TYPOS.getCategory(messages));
    setLocQualityIssueType(ITSIssueType.Misspelling);
  }

  @Override
  protected Map<String, List<String>> getWrongWords() {
    return wrongWords;
  }

  @Override
  public final String getId() {
    return CONTRACTION_SPELLING_RULE;
  }

  @Override
  public String getDescription() {
    return "Spelling of Arabic contractions";
  }

  @Override
  public String getShort() {
    // TODO : change to messages.getString
    return "\u062e\u0637\u0623 \u0625\u0645\u0644\u0627\u0626\u064a";
  }

  @Override
  public boolean isDictionaryBasedSpellingRule() {
    return false;
  }

  @Override
  public String getMessage(String tokenStr, List<String> replacements) {
    return "\u062e\u0637\u0623 \u0625\u0645\u0644\u0627\u0626\u064a \u0645\u062d\u062a\u0645\u0644";
  }

  @Override
  public boolean isCaseSensitive() {
    return false;
  }

  @Override
  public Locale getLocale() {
    return AR_LOCALE;
  }

}
