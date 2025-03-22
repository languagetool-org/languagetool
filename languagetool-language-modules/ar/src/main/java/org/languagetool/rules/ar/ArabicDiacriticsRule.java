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

import org.languagetool.language.Arabic;
import org.languagetool.rules.AbstractSimpleReplaceRule2;
import org.languagetool.rules.Categories;
import org.languagetool.rules.Example;
import org.languagetool.rules.ITSIssueType;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * A rule that matches words which are complex and suggests easier to understand alternatives.
 *
 * @author Sohaib Afifi
 * @author Taha Zerrouki
 * @since 5.0
 */
public class ArabicDiacriticsRule extends AbstractSimpleReplaceRule2 {

  public static final String AR_DIACRITICS_REPLACE = "AR_DIACRITICS_REPLACE";

  private static final String FILE_NAME = "/ar/diacritics.txt";
  private static final Locale AR_LOCALE = new Locale("ar");

  public ArabicDiacriticsRule(ResourceBundle messages) {
    super(messages, new Arabic());
    setCategory(Categories.STYLE.getCategory(messages));
    setLocQualityIssueType(ITSIssueType.Style);
    addExamplePair(Example.wrong("<marker>تجربة</marker>"),
                   Example.fixed("<marker>تجرِِبة</marker>"));
  }

  @Override
  public final List<String> getFileNames() {
    return Collections.singletonList(FILE_NAME);
  }

  @Override
  public final String getId() {
    return AR_DIACRITICS_REPLACE;
  }

  @Override
  public String getDescription() {
    return "كلمات مشكولة للتوضيح";
  }

  @Override
  public String getShort() {
    return "كلمات يستحسن أن تشكّل لتصحيح نطقها";
  }

  @Override
  public String getMessage() {
    return "'$match' كلمة يشيع نطقها نطقا خاطئا لذا نقترح تشكيلها كالآتي: $suggestions";
  }

  @Override
  public String getSuggestionsSeparator() {
    return " أو ";
  }

  @Override
  public Locale getLocale() {
    return AR_LOCALE;
  }

}
