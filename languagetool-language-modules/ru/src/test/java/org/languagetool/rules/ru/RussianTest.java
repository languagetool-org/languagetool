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
package org.languagetool.rules.ru;

import org.junit.Test;
import org.languagetool.LanguageSpecificTest;
import org.languagetool.language.Russian;

import java.io.IOException;
import java.util.Arrays;

public class RussianTest extends LanguageSpecificTest {
  
  @Test
  public void testLanguage() throws IOException {
    // NOTE: this text needs to be kept in sync with config.ts -> DEMO_TEXTS:
    String s = "Вставьте ваш текст сюда .. или проверьте этот текстт. Релиз LanguageTool 4.7 состоялся в четверг 28 сентября 2019 года.";
    Russian lang = new Russian();
    testDemoText(lang, s,
      Arrays.asList( "DOUBLE_PUNCTUATION", "UPPERCASE_SENTENCE_START", "MORFOLOGIK_RULE_RU_RU", "DATE_WEEKDAY1")
    );
    runTests(lang);
  }
}
