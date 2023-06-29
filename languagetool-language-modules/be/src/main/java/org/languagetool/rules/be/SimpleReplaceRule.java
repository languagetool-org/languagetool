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
package org.languagetool.rules.be;

import org.languagetool.rules.AbstractSimpleReplaceRule2;
import org.languagetool.rules.Example;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.languagetool.language.Belarusian;
import org.languagetool.rules.ITSIssueType;
import org.languagetool.rules.Category;
import org.languagetool.rules.CategoryId;

/**
 * A rule that matches words or phrases which should not be used and suggests
 * correct ones instead. 
 *
 * Belarusian implementations. Loads the
 * relevant words from <code>rules/be/replace.txt</code>.
 * 
 * Based on Russian implementation of the rule
 */
 

public class SimpleReplaceRule extends AbstractSimpleReplaceRule2 {

  public static final String BELARUSIAN_SIMPLE_REPLACE_RULE = "BE_SIMPLE_REPLACE";
  
  private static final Locale BE_LOCALE = new Locale("be");

  public SimpleReplaceRule(ResourceBundle messages) throws IOException {
    super(messages, new Belarusian());
    setLocQualityIssueType(ITSIssueType.Misspelling);
    setCategory(new Category(new CategoryId("MISC"), "Агульныя правілы"));
    addExamplePair(Example.wrong("<marker>З большага</marker>, гэта быў добры дзень."),
                   Example.fixed("<marker>Збольшага</marker>, гэта быў добры дзень."));
  }

  @Override
  public List<String> getFileNames() {
    return Collections.singletonList("/be/replace.txt");
  }

  @Override
  public final String getId() {
    return BELARUSIAN_SIMPLE_REPLACE_RULE;
  }

  @Override
  public String getDescription() {
    return "Пошук прастамоўяў і памылковых фраз";
  }

  @Override
  public String getShort() {
    return "Памылка?";
  }

  @Override
  public String getMessage() {
    return "«$match» — памылка, нелітаратурны выраз або прастамоўе, правільна: $suggestions";
  }

  @Override
  public String getSuggestionsSeparator() {
    return ", ";
  }

  @Override
  public Locale getLocale() {
    return BE_LOCALE;
  }
}
