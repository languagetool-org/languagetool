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
 * Catalan implementations for accentuation errors.
 * This is basically the same as CastellanismesReplaceRule.java
 * with a different error message.
 *
 * Loads the list of words from <code>rules/ca/accentuacio.txt</code>.
 *
 * TODO: Some of the entries are proper names (Greek gods, etc.), which
 * aren't currently checked.
 *
 * @author Jimmy O'Regan
 *
 * Based on pl/SimpleReplaceRule.java
 */
public class AccentuacioReplaceRule extends AbstractSimpleReplaceRule {

  public static final String CATALAN_ACCENTUACIO_REPLACE_RULE = "CA_ACCENTUACIO_REPLACE";

  private static final String FILE_NAME = "/ca/accentuacio.txt";
  // locale used on case-conversion
  private static final Locale CA_LOCALE = new Locale("ca");

  @Override
  public final String getFileName() {
    return FILE_NAME;
  }

  public AccentuacioReplaceRule(final ResourceBundle messages) throws IOException {
    super(messages);
  }

  @Override
  public final String getId() {
    return CATALAN_ACCENTUACIO_REPLACE_RULE;
  }

  @Override
  public String getDescription() {
    return "Errors d'accentuació";
  }

  @Override
  public String getShort() {
    return "Accentuació";
  }

  @Override
  public String getSuggestion() {
    return " es un error d'accentuació, cal dir: ";
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
    return CA_LOCALE;
  }

}
