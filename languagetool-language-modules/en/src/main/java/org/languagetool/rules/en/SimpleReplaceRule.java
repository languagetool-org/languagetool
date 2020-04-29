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
package org.languagetool.rules.en;

import org.languagetool.rules.AbstractSimpleReplaceRule;
import org.languagetool.rules.Categories;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * A rule that matches words which should not be used and suggests
 * correct ones instead. English implementation.
 * Loads the relevant words from <code>rules/en/replace.txt</code>.
 */
public class SimpleReplaceRule extends AbstractSimpleReplaceRule {

  private static final Map<String, List<String>> wrongWords = loadFromPath("/en/replace.txt");
  private static final Locale EN_LOCALE = new Locale("EN");

  @Override
  protected Map<String, List<String>> getWrongWords() {
    return wrongWords;
  }
  
  public SimpleReplaceRule(final ResourceBundle messages) {
    super(messages);
    super.setCategory(Categories.MISC.getCategory(messages));
    this.setCheckLemmas(false);
  }  

  @Override
  public final String getId() {
    return "EN_SIMPLE_REPLACE";
  }

  @Override
  public boolean isCaseSensitive() {
    return false;
  }
  
  @Override
  public Locale getLocale() {
    return EN_LOCALE;
  }

}
