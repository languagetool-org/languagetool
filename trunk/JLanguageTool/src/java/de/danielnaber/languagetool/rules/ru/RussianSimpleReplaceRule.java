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
package de.danielnaber.languagetool.rules.ru;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

import de.danielnaber.languagetool.rules.AbstractSimpleReplaceRule;

/**
 * A rule that matches words or phrases which should not be used and suggests
 * correct ones instead. 
 *
 * Russian implementations. Loads the
 * relevant words from <code>rules/ru/replace.txt</code>.
 *
 * @author  Yakov Reztsov
 */
public class RussianSimpleReplaceRule extends AbstractSimpleReplaceRule {

  private static final String FILE_NAME = "/ru/replace.txt";

  // locale used on case-conversion
  private static final Locale RU_LOCALE = new Locale("ru");

  @Override
  public final String getFileName() {
    return FILE_NAME;
  }

  public RussianSimpleReplaceRule(final ResourceBundle messages) throws IOException {
    super(messages);
  }

  @Override
  public final String getId() {
    return "RU_SIMPLE_REPLACE";
  }

  @Override
  public String getDescription() {
    return "Поиск ошибочных слов/фраз";
  }

  @Override
  public String getShort() {
    return "Ошибка?";
  }

  @Override
  public String getSuggestion() {
    return " - ошибочное слово/фраза, исправление: ";
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
    return RU_LOCALE;
  }
}
