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
package org.languagetool.rules.de;

import org.languagetool.Language;
import org.languagetool.rules.AbstractSimpleReplaceRule2;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * A rule that matches words which should not be used and suggests
 * correct ones instead. German implementation.
 * Loads the relevant words from <code>rules/de/replace.txt</code>.
 */
public class SimpleReplaceRule extends AbstractSimpleReplaceRule2 {

  private static final Locale DE_LOCALE = new Locale("DE");

  public SimpleReplaceRule(ResourceBundle messages, Language language) {
    super(messages, language);
  }

  @Override
  public List<String> getFileNames() {
    return Arrays.asList("/de/replace.txt", "/de/replace_custom.txt");
  }

  @Override
  public final String getId() {
    return "DE_SIMPLE_REPLACE";
  }

  @Override
  public String getDescription() {
    return "Prüft auf bestimmte falsche Wörter/Phrasen";
  }

  @Override
  public String getShort() {
    return "Wrong word";
  }

  @Override
  public String getMessage() {
    return "Meinten Sie vielleicht $suggestions?";
    //return "Dieses Wort steht in der Liste der nicht erlaubten Wörter.";
  }

  @Override
  public String getSuggestionsSeparator() {
    return ", ";
  }

  @Override
  public boolean isCaseSensitive() {
    return false;
  }
  
  @Override
  public Locale getLocale() {
    return DE_LOCALE;
  }

}
