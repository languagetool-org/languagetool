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
package org.languagetool.rules.pl;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.languagetool.rules.*;

/**
 * A rule that matches words or phrases which should not be used and suggests
 * correct ones instead.
 *
 * Polish implementations. Loads the list of words from
 * <code>rules/pl/replace.txt</code>.
 *
 * @author Marcin Miłkowski
 */
public class SimpleReplaceRule extends AbstractSimpleReplaceRule {

  public static final String POLISH_SIMPLE_REPLACE_RULE = "PL_SIMPLE_REPLACE";

  private static final Map<String, List<String>> wrongWords = loadFromPath("/pl/replace.txt");
  private static final Locale PL_LOCALE = new Locale("pl");

  @Override
  public Map<String, List<String>> getWrongWords() {
    return wrongWords;
  }

  public SimpleReplaceRule(ResourceBundle messages) throws IOException {
    super(messages);
    setLocQualityIssueType(ITSIssueType.Misspelling);
    setCategory(new Category(new CategoryId("PRAWDOPODOBNE_LITEROWKI"), "Prawdopodobne literówki"));
    setCheckLemmas(false);
    addExamplePair(Example.wrong("Uspokój <marker>sei</marker>."),
                   Example.fixed("Uspokój <marker>się</marker>."));
  }

  @Override
  public final String getId() {
    return POLISH_SIMPLE_REPLACE_RULE;
  }

  @Override
  public String getDescription() {
    return "Typowe literówki i niepoprawne wyrazy (domowi, sie, niewiadomo, duh, cie…)";
  }

  @Override
  public String getShort() {
    return "Literówka";
  }

  @Override
  public String getMessage(String tokenStr, List<String> replacements) {
    return "Wyraz „" + tokenStr + "” to najczęściej literówka; poprawnie pisze się: "
        + String.join(", ", replacements) + ".";
  }

  @Override
  public boolean isCaseSensitive() {
    return false;
  }

  @Override
  public Locale getLocale() {
    return PL_LOCALE;
  }

}
