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

import org.languagetool.language.English;
import org.languagetool.rules.AbstractSimpleReplaceRule2;
import org.languagetool.rules.Categories;
import org.languagetool.rules.Example;
import org.languagetool.rules.ITSIssueType;
import org.languagetool.tools.Tools;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import java.net.URL;

/**
 * A rule that matches words which require specific diacritics (e.g, {@code a la} instead of {@code à la}).
 *
 * @author Tiago F. Santos 
 * @since 4.8
 */
public class EnglishDiacriticsRule extends AbstractSimpleReplaceRule2 {

  public static final String EN_DIACRITICS_REPLACE = "EN_DIACRITICS_REPLACE";

  private static final String FILE_NAME = "/en/diacritics.txt";
  private static final Locale EN_LOCALE = new Locale("en");  // locale used on case-conversion

  @Override
  public final List<String> getFileNames() {
    return Collections.singletonList(FILE_NAME);
  }

  public EnglishDiacriticsRule(ResourceBundle messages) throws IOException {
    super(messages, new English());
    // setDefaultOff();
    super.setCategory(Categories.TYPOS.getCategory(messages));
    setLocQualityIssueType(ITSIssueType.Misspelling);
    addExamplePair(Example.wrong("<marker>blase</marker>"),
                   Example.fixed("<marker>blasé</marker>"));
  }

  @Override
  public final String getId() {
    return EN_DIACRITICS_REPLACE;
  }

  @Override
  public String getDescription() {
    return "Words with diacritics";
  }

  @Override
  public String getShort() {
    return "The original word has a diacritic";
  }

  @Override
  public String getMessage() {
    return "'$match' is an imported foreign expression, which originally has a diacritic. Consider using $suggestions.";
  }

  @Override
  public String getSuggestionsSeparator() {
    return " or ";
  }

  @Override
  public URL getUrl() {
    return Tools.getUrl("https://en.wikipedia.org/wiki/English_terms_with_diacritical_marks");
  }

  @Override
  public Locale getLocale() {
    return EN_LOCALE;
  }

}
