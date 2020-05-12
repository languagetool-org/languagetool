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
package org.languagetool.rules.gl;

import org.junit.jupiter.api.Test;
import org.languagetool.LanguageSpecificTest;
import org.languagetool.language.Galician;

import java.io.IOException;
import java.util.Arrays;

public class GalicianTest extends LanguageSpecificTest {
  
  @Test
  public void testLanguage() throws IOException {
    // NOTE: this text needs to be kept in sync with WelcomeController.php's getDefaultDemoTexts():
    String s = "Esta vai a ser unha mostra de de exemplo para amosar  o funcionamento de LanguageTool.";
    Galician lang = new Galician();
    testDemoText(lang, s,
      Arrays.asList("IR_A_INF", "WORD_REPETITION", "WHITESPACE_RULE")
    );
    runTests(lang);
  }
}
