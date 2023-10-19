package org.languagetool.rules.fr;

import org.languagetool.AnalyzedTokenReadings;

/* LanguageTool, a natural language style checker 
 * Copyright (C) 2020 Daniel Naber (http://www.danielnaber.de)
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

import org.languagetool.language.French;
import org.languagetool.rules.AbstractSimpleReplaceRule2;
import org.languagetool.rules.Categories;
import org.languagetool.rules.Category;
import org.languagetool.rules.CategoryId;
import org.languagetool.rules.ITSIssueType;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * A rule that matches words which should not be used and suggests correct ones
 * instead.
 * 
 * Loads the relevant words from <code>rules/fr/replace_anglicism.txt</code>.
 * 
 * @author Jaume Ortolà
 */
public class AnglicismReplaceRule extends AbstractSimpleReplaceRule2 {

  private static final String FILE_NAME = "/fr/replace_anglicism.txt";
  private static final Locale FR_LOCALE = new Locale("FR");

  public AnglicismReplaceRule(final ResourceBundle messages) throws IOException {
    super(messages, new French());
    super.setCategory(new Category(new CategoryId("CAT_ANGLICISMES"), this.getDescription()));
    setLocQualityIssueType(ITSIssueType.Style);
    super.useSubRuleSpecificIds();
  }

  @Override
  public final String getId() {
    return "FR_SIMPLE_REPLACE_ANGLICISM";
  }

  @Override
  public String getDescription() {
    return "Anglicismes (calques, emprunts directs, etc.)";
  }

  @Override
  public String getShort() {
    return "Anglicismes";
  }

  @Override
  public boolean isCaseSensitive() {
    return false;
  }

  @Override
  public Locale getLocale() {
    return FR_LOCALE;
  }

  @Override
  public List<String> getFileNames() {
    return Arrays.asList(FILE_NAME);
  }

  @Override
  public String getMessage() {
    return "Alternatives possibles pour cet anglicisme :";
  }

  @Override
  public String getSuggestionsSeparator() {
    return " ou ";
  }

  @Override
  public URL getUrl() {
    return null;
  }

  @Override
  protected boolean isTokenException(AnalyzedTokenReadings atr) {
    // proper nouns tagged in multiwords are exceptions
    return atr.hasPosTagStartingWith("Z") || atr.isImmunized() || atr.isIgnoredBySpeller();
  }

}
