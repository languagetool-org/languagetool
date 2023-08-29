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
 * A rule that matches redundant expression.
 * Arabic implementations. Loads the list of words from
 * <code>/ar/redundancies.txt</code>.
 *
 * @author Sohaib Afifi
 * @author Taha Zerrouki
 * @since 5.0
 */
public class ArabicRedundancyRule extends AbstractSimpleReplaceRule2 {

  public static final String AR_REDUNDANCY_REPLACE = "AR_REDUNDANCY_REPLACE";

  private static final String FILE_NAME = "/ar/redundancies.txt";
  private static final Locale AR_LOCALE = new Locale("ar");  // locale used on case-conversion

  public ArabicRedundancyRule(ResourceBundle messages) {
    super(messages, new Arabic());
    super.setCategory(Categories.REDUNDANCY.getCategory(messages));
    setLocQualityIssueType(ITSIssueType.Style);
    addExamplePair(Example.wrong("<marker>سوف لن</marker>"),
                   Example.fixed("<marker>لن</marker>"));
  }

  @Override
  public final List<String> getFileNames() {
    return Collections.singletonList(FILE_NAME);
  }

  @Override
  public final String getId() {
    return AR_REDUNDANCY_REPLACE;
  }

  @Override
  public String getDescription() {
    return "1. تكرار (عام)";
  }

  @Override
  public String getShort() {
    return "تكرار";
  }

  @Override
  public String getMessage() {
    return "'$match' تعبير فيه تكرار.في بعض الحالات، يستحسن استعمال $suggestions";
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
