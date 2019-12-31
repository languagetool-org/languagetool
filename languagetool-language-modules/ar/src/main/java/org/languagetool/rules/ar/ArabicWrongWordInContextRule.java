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

import org.languagetool.rules.Example;
import org.languagetool.rules.WrongWordInContextRule;

import java.util.ResourceBundle;

/**
 * @author Sohaib AFIFI
 * @since 5.0
 */
public class ArabicWrongWordInContextRule extends WrongWordInContextRule {

  public static final String ARABIC_WRONG_WORD_IN_CONTEXT = "ARABIC_WRONG_WORD_IN_CONTEXT";

  private static final String FILE_NAME = "/ar/wrongWordInContext.txt";

  public ArabicWrongWordInContextRule(ResourceBundle messages) {
    super(messages);
    addExamplePair(Example.wrong("من سوء <marker>الضن</marker> بالله ترك الأمر بالمعروف."),
      Example.fixed("من سوء <marker>الظن</marker> بالله ترك الأمر بالمعروف."));
  }

  @Override
  protected String getCategoryString() {
    return "كلمات متشابهة";
  }

  @Override
  public String getId() {
    return this.ARABIC_WRONG_WORD_IN_CONTEXT;
  }

  @Override
  public String getDescription() {
    return "كلمات شائعةمتشابهة (ظل/ضلَ, رؤيا/رؤية الخ.)";
  }

  @Override
  protected String getFilename() {
    return this.FILE_NAME;
  }

  @Override
  protected String getMessageString() {
    return "احتمال كلمة متشابهة: هل تقصد <suggestion>$SUGGESTION</suggestion> بدلا من '$WRONGWORD'?";
  }

  @Override
  protected String getShortMessageString() {
    return "احتمال كلمة متشابهة";
  }

  @Override
  protected String getLongMessageString() {
    return "احتمال كلمة متشابهة: هل تقصد <suggestion>$SUGGESTION</suggestion> (= $EXPLANATION_SUGGESTION) بدلا من '$WRONGWORD' (= $EXPLANATION_WRONGWORD)?";
  }
}
