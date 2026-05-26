/* LanguageTool, a natural language style checker
 * Copyright (C) 2026 LanguageTool contributors
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
package org.languagetool.rules.az;

import org.languagetool.language.Azerbaijani;
import org.languagetool.rules.AbstractSimpleReplaceRule2;
import org.languagetool.rules.Categories;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Maps common ASCII-typed Azerbaijani word forms (without the diacritics
 * ə, ş, ı, ğ, ö, ü, ç) to their canonical Latin spelling. Many writers type
 * Azerbaijani on keyboards that don't have the AZ layout, dropping the
 * diacritics; this rule surfaces the proper form as a suggestion.
 *
 * Word list is loaded from {@code /az/replace.txt}.
 */
public class AzerbaijaniSimpleReplaceRule extends AbstractSimpleReplaceRule2 {

  public static final String AZ_SIMPLE_REPLACE_RULE = "AZ_SIMPLE_REPLACE";

  private static final String FILE_NAME = "/az/replace.txt";
  private static final Locale AZ_LOCALE = new Locale("az");

  @Override
  public final List<String> getFileNames() {
    return Collections.singletonList(FILE_NAME);
  }

  public AzerbaijaniSimpleReplaceRule(ResourceBundle messages) throws IOException {
    super(messages, new Azerbaijani());
    super.setCategory(Categories.TYPOS.getCategory(messages));
  }

  @Override
  public final String getId() {
    return AZ_SIMPLE_REPLACE_RULE;
  }

  @Override
  public String getDescription() {
    return "Azərbaycan dilində ASCII şəklində yazılmış sözləri düzgün diakritikalı formaya əvəz edir";
  }

  @Override
  public String getShort() {
    return "Düzgün yazılışı təklif edin";
  }

  @Override
  public String getMessage() {
    return "Bu sözün düzgün yazılışı diakritika ilədir.";
  }

  @Override
  public String getSuggestionsSeparator() {
    return " və ya ";
  }

  @Override
  public Locale getLocale() {
    return AZ_LOCALE;
  }

}
