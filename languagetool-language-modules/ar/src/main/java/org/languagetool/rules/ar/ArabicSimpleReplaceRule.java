/*
 * LanguageTool, a natural language style checker
 * Copyright (C) 2019 Sohaib Afifi, Taha Zerrouki
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
package org.languagetool.rules.ar;

import org.languagetool.Language;
import org.languagetool.rules.AbstractSimpleReplaceRule2;
import org.languagetool.rules.Categories;
import org.languagetool.rules.Example;

import java.util.Locale;
import java.util.ResourceBundle;

public class ArabicSimpleReplaceRule extends AbstractSimpleReplaceRule2 {
  
  public static final String RULE_ID = "AR_SIMPLE_REPLACE";
  
  private static final String RESOURCE_FILENAME = "ar/replaces.txt";

  public ArabicSimpleReplaceRule(ResourceBundle messages, Language language) {
    super(messages, language);
    super.setCategory(Categories.CONFUSED_WORDS.getCategory(messages));
    addExamplePair(Example.wrong("<marker>إنشاء</marker>"),
                   Example.fixed("<marker>إن شاء</marker>"));
  }

  @Override
  public String getFileName() {
    return RESOURCE_FILENAME;
  }

  @Override
  public String getId() {
    return RULE_ID;
  }

  @Override
  public String getDescription() {
    return "A rule that matches words which should not be used and suggests correct ones instead";
  }

  @Override
  public String getShort() {
    // TODO : change to messages.getString
    return "\u062e\u0637\u0623 \u0625\u0645\u0644\u0627\u0626\u064a";
  }

  @Override
  public boolean isDictionaryBasedSpellingRule() {
    return false;
  }


  @Override
  public boolean isCaseSensitive() {
    return false;
  }

  /**
   * @return A string where {@code $match} will be replaced with the matching word
   * and {@code $suggestions} will be replaced with the alternatives. This is the string
   * shown to the user.
   */
  @Override
  public String getSuggestion() {
    return "قل $suggestions";
  }

  /**
   * @return the word used to separate multiple suggestions; used only before last suggestion, the rest are comma-separated.
   */
  @Override
  public String getSuggestionsSeparator() {
    return " أو  ";
  }

  /**
   * locale used on case-conversion
   */
  @Override
  public Locale getLocale() {
    return new Locale("ar");
  }
}
