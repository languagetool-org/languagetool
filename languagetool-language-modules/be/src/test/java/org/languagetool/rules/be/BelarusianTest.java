/* LanguageTool, a natural language style checker
 * Copyright (C) 2019 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.be;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.LanguageSpecificTest;
import org.languagetool.language.Belarusian;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class BelarusianTest extends LanguageSpecificTest {
  
  @Test
  public void testLanguage() throws IOException {
    // NOTE: this text needs to be kept in sync with config.ts -> DEMO_TEXTS:
    String s = "Паспрабуйце напісаць нейкі тэкст з памылкамі, а LanguageTool паспрабуе паказаць нейкия найбольш распаусюджаныя памылки.";
    Belarusian lang = new Belarusian();
    testDemoText(lang, s,
      Arrays.asList("I", "U_KAROTKAJE", "I")
    );
    runTests(lang);
  }

  /**
   * Verifies that both spellings of «гэта значыць» stay within one sentence,
   * ordinary sentences still split, and standalone «зн.» is not accepted.
   */
  @Test
  public void testAbbreviationSentenceBoundary() throws IOException {
    Belarusian lang = new Belarusian();
    JLanguageTool lt = new JLanguageTool(lang);
    for (String abbreviation : Arrays.asList("г.зн.", "г. зн.")) {
      String sentence = "Гэта значыць, " + abbreviation + " кожным атрыбутам.";
      assertEquals(Arrays.asList(sentence), lang.getSentenceTokenizer().tokenize(sentence));
      assertEquals(0, lt.check(sentence).size());
    }
    assertEquals(Arrays.asList("Гэта сказ. ", "Тут іншы сказ."),
      lang.getSentenceTokenizer().tokenize("Гэта сказ. Тут іншы сказ."));
    assertEquals(1, lt.check("Гэта зн.").stream()
      .filter(match -> match.getRule().getId().equals("MORFOLOGIK_RULE_BE_BY")).count());
  }

}
