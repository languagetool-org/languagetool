/* LanguageTool, a natural language style checker 
 * Copyright (C) 2016 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.ru;

import org.junit.Test;
import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Russian;

import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class RussianWordCoherencyRuleTest {

  private final JLanguageTool lt = new JLanguageTool(new Russian());

  @Test
  public void testRule() throws IOException {
    assertGood("По шкале Цельсия абсолютному нулю соответствует температура −273,15 °C.");
    assertGood("По шкале Цельсия абсолютному нулю соответствует температура −273,15 °C.");
    assertError("По шкале Цельсия абсолютному нулю соответствует температура −273,15 °C или ноль по шкале Кельвина.");
  }

  @Test
  public void testCallIndependence() throws IOException {
    assertGood("Абсолютный нуль.");
    assertGood("Ноль по шкале Кельвина.");  // this won't be noticed, the calls are independent of each other
  }

  private void assertError(String s) throws IOException {
    RussianWordCoherencyRule rule = new RussianWordCoherencyRule(TestTools.getEnglishMessages());
    AnalyzedSentence analyzedSentence = lt.getAnalyzedSentence(s);
    assertEquals(1, rule.match(Collections.singletonList(analyzedSentence)).length);
  }

  private void assertGood(String s) throws IOException {
    RussianWordCoherencyRule rule = new RussianWordCoherencyRule(TestTools.getEnglishMessages());
    AnalyzedSentence analyzedSentence = lt.getAnalyzedSentence(s);
    assertEquals(0, rule.match(Collections.singletonList(analyzedSentence)).length);
  }

  @Test
  public void testRuleCompleteTexts() throws IOException {
    assertEquals(0, lt.check("По шкале Цельсия абсолютному нулю соответствует температура −273,15 °C или нуль по шкале Кельвина.").size());
    assertEquals(1, lt.check("По шкале Цельсия абсолютному нулю соответствует температура −273,15 °C или ноль по шкале Кельвина.").size());
    // cross-paragraph checks:
    assertEquals(1, lt.check("Абсолютный нуль.\n\nСовсем недостижим. И ноль по шкале Кельвина.").size());
  }

}
