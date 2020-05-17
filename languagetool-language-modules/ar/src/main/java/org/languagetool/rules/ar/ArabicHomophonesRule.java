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
import org.languagetool.rules.AbstractSimpleReplaceRule;
import org.languagetool.rules.Categories;
import org.languagetool.rules.Example;
import org.languagetool.rules.ITSIssueType;

import java.io.IOException;
import java.util.Locale;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;



/**
 * A rule that matches words which are homophones and suggests easier to understand alternatives.
 *
 * @author Sohaib Afifi
 * @author Taha Zerrouki
 * @since 5.0
 */
 
 public class ArabicHomophonesRule extends AbstractSimpleReplaceRule {

  private static final Map<String, List<String>> wrongWords = loadFromPath("/ar/homophones.txt");
  public  static final String AR_HOMOPHONES_REPLACE = "AR_HOMOPHONES_REPLACE";  
  private static final Locale AR_LOCALE = new Locale("ar");

  @Override
  protected Map<String, List<String>> getWrongWords() {
    return wrongWords;
  }
  
  public ArabicHomophonesRule(final ResourceBundle messages) {
    super(messages);
    super.setCategory(Categories.STYLE.getCategory(messages));
    this.setCheckLemmas(true);
//     setLocQualityIssueType(ITSIssueType.Style);
    addExamplePair(Example.wrong("<marker>ضن</marker>"),
      Example.fixed("<marker>ظن</marker>"));    
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
  public Locale getLocale() {
    return AR_LOCALE;
  }

  @Override
    public String getMessage(String tokenStr, List<String> replacements) {
    return tokenStr + " كلمة تتشابه مع: "
        + String.join(", ", replacements) + ".";
  }
  
}

