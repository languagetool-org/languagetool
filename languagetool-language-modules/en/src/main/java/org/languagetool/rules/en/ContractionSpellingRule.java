/* LanguageTool, a natural language style checker
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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
import java.util.Locale;
import java.util.ResourceBundle;

import org.languagetool.rules.AbstractSimpleReplaceRule;
import org.languagetool.rules.Category;

/**
 * A rule that matches words or phrases which should not be used and suggests
 * correct ones instead.
 *
 * @author Marcin Miłkowski
 * @since 2.5
 */
public class ContractionSpellingRule extends AbstractSimpleReplaceRule {

  public static final String CONTRACTION_SPELLING_RULE = "EN_CONTRACTION_SPELLING";

  private static final String FILE_NAME = "/en/contractions.txt";
  // locale used on case-conversion
  private static final Locale EN_LOCALE = new Locale("en");

  @Override
  public final String getFileName() {
    return FILE_NAME;
  }

  public ContractionSpellingRule(final ResourceBundle messages) throws IOException {
    super(messages);
    super.setCategory(new Category("Possible Typo"));
    setLocQualityIssueType("misspelling");
  }

  @Override
  public final String getId() {
    return CONTRACTION_SPELLING_RULE;
  }

  @Override
  public String getDescription() {
    return "Spelling of English contractions";
  }

  @Override
  public String getShort() {
    return "Spelling mistake";
  }

  @Override
  public boolean isSpellingRule() {
    return true;
  }

  @Override
  public String getMessage(String tokenStr, List<String> replacements) {
    return "Possible spelling mistake found";
  }

  /**
   * use case-sensitive matching.
   */
  @Override
  public boolean isCaseSensitive() {
    return true;
  }

  /**
   * locale used on case-conversion
   */
  @Override
  public Locale getLocale() {
    return EN_LOCALE;
  }

}
