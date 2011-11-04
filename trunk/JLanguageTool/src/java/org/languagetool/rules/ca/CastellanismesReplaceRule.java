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
package de.danielnaber.languagetool.rules.ca;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

import de.danielnaber.languagetool.rules.AbstractSimpleReplaceRule;

/**
 * A rule that matches words or phrases which should not be used and suggests
 * correct ones instead.
 *
 * Catalan implementations for Castelianisms, kept separate for an individual
 * error message.
 * Loads the list of words from <code>rules/ca/castellanismes.txt</code>.
 *
 * @author Jimmy O'Regan
 *
 * Based on pl/SimpleReplaceRule.java
 */
public class CastellanismesReplaceRule extends AbstractSimpleReplaceRule {

  public static final String CATALAN_CASTELLANISMES_REPLACE_RULE = "CA_CASTELLANISMES_REPLACE";

  private static final String FILE_NAME = "/ca/castellanismes.txt";
  // locale used on case-conversion
  private static final Locale caLocale = new Locale("ca");

  @Override
  public final String getFileName() {
    return FILE_NAME;
  }

  public CastellanismesReplaceRule(final ResourceBundle messages) throws IOException {
    super(messages);
  }

  @Override
  public final String getId() {
    return CATALAN_CASTELLANISMES_REPLACE_RULE;
  }

  @Override
  public String getDescription() {
    return "Barbarismes (Castellanismes)";
  }

  @Override
  public String getShort() {
    return "Castellanismes";
  }

  @Override
  public String getSuggestion() {
    return " es un castellanisme, cal dir: ";
  }

  /**
   * use case-insensitive matching.
   */
  @Override
  public boolean isCaseSensitive() {
    return false;
  }

  /**
   * locale used on case-conversion
   */
  @Override
  public Locale getLocale() {
    return caLocale;
  }

}
