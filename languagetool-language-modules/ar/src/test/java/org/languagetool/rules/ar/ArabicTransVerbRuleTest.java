/*
 * LanguageTool, a natural language style checker
 * Copyright (C) 2021 Sohaib Afifi, Taha Zerrouki
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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.TestTools;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;

public class ArabicTransVerbRuleTest {
  private ArabicTransVerbRule rule;
  private JLanguageTool lt;

  @BeforeEach
  public void setUp() throws IOException {
    rule = new ArabicTransVerbRule(TestTools.getEnglishMessages());
    lt = new JLanguageTool(Languages.getLanguageForShortCode("ar"));
  }

  @Test
  public void testRule() throws IOException {

    // correct sentences:
    assertCorrect("كان أَفَاضَ في الحديث");

    // errors:
    assertIncorrect("كان أفاض من الحديث");
    assertIncorrect("لقد أفاضت من الحديث");
    assertIncorrect("لقد أفاضت الحديث");
    assertIncorrect("كان أفاضها الحديث");
    assertIncorrect("إذ استعجل الأمر");
  }

  private void assertCorrect(String sentence) throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(sentence));
    Assertions.assertEquals(0, matches.length);
  }

  private void assertIncorrect(String sentence) throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(sentence));
    Assertions.assertEquals(1, matches.length);
  }

}
