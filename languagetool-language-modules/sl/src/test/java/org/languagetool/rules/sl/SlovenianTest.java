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
package org.languagetool.rules.sl;

import org.junit.jupiter.api.Test;
import org.languagetool.LanguageSpecificTest;
import org.languagetool.language.Slovenian;

import java.io.IOException;
import java.util.Arrays;

public class SlovenianTest extends LanguageSpecificTest {
  
  @Test
  public void testLanguage() throws IOException {
    // NOTE: this text needs to be kept in sync with WelcomeController.php's getDefaultDemoTexts():
    String s = "Tukaj vnesite svoje besedilo... Pa poglejmo primer besedila s nekaj napakami ki jih lahko razpozna orodje LanguageTool; ko opazite napake, jih lahko enostavno popiravite. ( Obenem se izvrši tudi preverjanje črkovanja črkovanja.";
    Slovenian lang = new Slovenian();
    testDemoText(lang, s,
      Arrays.asList("TROPIČJETRI", "PREDLOG_S", "KI_BREZ_VEJICE", "MORFOLOGIK_RULE_SL_SI", "COMMA_PARENTHESIS_WHITESPACE", "WORD_REPEAT_RULE")
    );
    runTests(lang);
  }
}
