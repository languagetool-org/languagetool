/*
 * LanguageTool, a natural language style checker
 * Copyright (C) 2020 Sohaib Afifi, Taha Zerrouki
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

import org.languagetool.language.Arabic;
import org.languagetool.rules.AbstractSimpleReplaceRule2;
import org.languagetool.rules.Categories;
import org.languagetool.rules.Example;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;


/**
 * A rule that matches words which are homophones and suggests easier to understand alternatives.
 *
 * @author Sohaib Afifi
 * @author Taha Zerrouki
 * @since 5.0
 */
public class ArabicHomophonesRule extends AbstractSimpleReplaceRule2 {

  public static final String AR_HOMOPHONES_REPLACE = "AR_HOMOPHONES_REPLACE";
  private static final String RESOURCE_FILENAME = "ar/homophones.txt";
  private static final Locale AR_LOCALE = new Locale("ar");

  public ArabicHomophonesRule(final ResourceBundle messages) {
    super(messages, new Arabic());
    super.setCategory(Categories.CONFUSED_WORDS.getCategory(messages));
    addExamplePair(Example.wrong("<marker>ضن</marker>"),
                   Example.fixed("<marker>ظن</marker>"));
  }

  @Override
  public List<String> getFileNames() {
    return Collections.singletonList(RESOURCE_FILENAME);
  }

  @Override
  public final String getId() {
    return AR_HOMOPHONES_REPLACE;
  }

  @Override
  public String getDescription() {
    return "كلمات متشابهة لفظا للتوضيح، يرجى التحقق منها مثل تشابه الظاء والضاد.";
  }

  @Override
  public String getShort() {
    return "كلمات متشابهة لفظا يرجى التحقق منها";
  }

  @Override
  public String getMessage() {
    return "قل $suggestions";
  }

  @Override
  public String getSuggestionsSeparator() {
    return " أو  ";
  }

  @Override
  public Locale getLocale() {
    return AR_LOCALE;
  }

}

