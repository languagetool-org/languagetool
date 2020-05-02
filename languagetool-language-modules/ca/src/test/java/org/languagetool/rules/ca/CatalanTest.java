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
package org.languagetool.rules.ca;

import org.junit.Test;
import org.languagetool.LanguageSpecificTest;
import org.languagetool.language.Catalan;

import java.io.IOException;
import java.util.Arrays;

public class CatalanTest extends LanguageSpecificTest {
  
  @Test
  public void testLanguage() throws IOException {
    // NOTE: this text needs to be kept in sync with WelcomeController.php's getDefaultDemoTexts():
    String s = "Introduïu açí el vostre text. o feu servir aquest texts com a a exemple per a alguns errades que LanguageTool hi pot detectat.";
    Catalan lang = new Catalan();
    testDemoText(lang, s,
      Arrays.asList("MORFOLOGIK_RULE_CA_ES", "UPPERCASE_SENTENCE_START", "CONCORDANCES_DET_NOM", "CATALAN_WORD_REPEAT_RULE", "CONCORDANCES_DET_NOM", "VERB_SEGUIT_DINFINITIU")
    );
    runTests(lang, null, "·");
  }
}
