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

import org.junit.Before;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.TestTools;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class ArabicTransVerbIndirectToDirectRuleTest {
  private ArabicTransVerbIndirectToDirectRule rule;
  private JLanguageTool lt;

  @Before
  public void setUp() throws IOException {
    rule = new ArabicTransVerbIndirectToDirectRule(TestTools.getEnglishMessages());
    lt = new JLanguageTool(Languages.getLanguageForShortCode("ar"));
  }

  @Test
  public void testRule() throws IOException {
    // Correct sentences:
    assertCorrect("تحرى الموضوع");
    assertCorrect("تحرى الضابط القضية");
    assertCorrect("تحرى  المسألة");
    assertCorrect("يتحرونه");
//    // errors:
    assertIncorrect("تحرى الضابط عن القضية", 1);
    assertIncorrect("تحرى عن المسألة", 1);
    assertIncorrect("يتحرون عنه", 4);
  }

  private void assertCorrect(String sentence) throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(sentence));
    assertEquals(0, matches.length);
  }

  private void assertIncorrect(String sentence, int index) throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(sentence));
    assertEquals(index, matches.length);
  }

}

