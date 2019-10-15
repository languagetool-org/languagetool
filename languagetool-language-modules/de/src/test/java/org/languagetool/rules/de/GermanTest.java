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
package org.languagetool.rules.de;

import org.junit.Test;
import org.languagetool.LanguageSpecificTest;
import org.languagetool.language.GermanyGerman;
import org.languagetool.rules.WordListValidatorTest;

import java.io.IOException;
import java.util.Arrays;

public class GermanTest extends LanguageSpecificTest {
  
  @Test
  public void testLanguage() throws IOException {
    // NOTE: this text needs to be kept in sync with WelcomeController.php's getDefaultDemoTexts():
    String s = "Fügen Sie hier Ihren Text ein. Klicken Sie nach der Prüfung auf die farbig unterlegten Textstellen. oder nutzen Sie diesen Text als Beispiel für ein Paar Fehler , die LanguageTool erkennen kann: Ihm wurde Angst und bange. Mögliche stilistische Probleme werden blau hervorgehoben: Das ist besser wie vor drei Jahren. Eine Rechtschreibprüfun findet findet übrigens auch statt. Donnerstag, den 23.01.2019 war gutes Wetter. Die Beispiel endet hier.";
    GermanyGerman lang = new GermanyGerman();
    testDemoText(lang, s,
      Arrays.asList("UPPERCASE_SENTENCE_START", "EIN_PAAR", "COMMA_PARENTHESIS_WHITESPACE", "ANGST_UND_BANGE", "KOMP_WIE", "GERMAN_SPELLER_RULE", "SAGT_RUFT", "DATUM_WOCHENTAG", "DE_AGREEMENT")
    );
    runTests(lang);
  }
}
