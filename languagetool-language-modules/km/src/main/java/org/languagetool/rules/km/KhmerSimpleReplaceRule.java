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
package org.languagetool.rules.km;

import org.languagetool.language.Khmer;
import org.languagetool.rules.AbstractSimpleReplaceRule2;
import org.languagetool.rules.Categories;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * A rule that matches words which should not be used and suggests correct ones instead. 
 * Khmer implementations. Loads the list of words from
 * <code>/km/coherency.txt</code>.
 */
public class KhmerSimpleReplaceRule extends AbstractSimpleReplaceRule2 {

  public static final String KHMER_SIMPLE_REPLACE_RULE = "KM_SIMPLE_REPLACE";

  private static final String FILE_NAME = "/km/coherency.txt";
  private static final Locale KM_LOCALE = new Locale("km");  // locale used on case-conversion
  
  @Override
  public final String getFileName() {
    return FILE_NAME;
  }

  public KhmerSimpleReplaceRule(ResourceBundle messages) throws IOException {
    super(messages, new Khmer());
    super.setCategory(Categories.MISC.getCategory(messages));
  }

  @Override
  public final String getId() {
    return KHMER_SIMPLE_REPLACE_RULE;
  }

  @Override
  public String getDescription() {
    return "Words or groups of words that are incorrect or obsolete";
  }

  @Override
  public String getShort() {
    return "Consider following the spelling of Chuon Nath";
  }

  @Override
  public String getSuggestion() {
    return " Consider following the spelling of Chuon Nath ";
  }

  @Override
  public String getSuggestionsSeparator() {
    return " or ";
  }

  @Override
  public Locale getLocale() {
    return KM_LOCALE;
  }

}
