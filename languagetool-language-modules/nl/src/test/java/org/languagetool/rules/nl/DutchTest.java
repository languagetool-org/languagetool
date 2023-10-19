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
package org.languagetool.rules.nl;

import org.junit.Test;
import org.languagetool.LanguageSpecificTest;
import org.languagetool.language.Dutch;

import java.io.IOException;
import java.util.Arrays;

public class DutchTest extends LanguageSpecificTest {
  
  @Test
  public void testLanguage() throws IOException {
      // NOTE: this text needs to be kept in sync with config.ts -> DEMO_TEXTS:
    String s = "Schrijf of plak hier je tekst om deze al typende te check. Vergissingen worden gemarkeerd met verschillende kleuren: spelvouten laten we rood ondersteept zien. Grammaticafouten daarentegen markeren we met geel. LanguageTool laat stijlwesties zo optimaal mogelijk zien in het blauw. wist u al dat u synoniemen kunt oproepen met een dubbelklik op een woord ? LanguageTool is een absolute musthave voor het schrijven van perfecte tekst. Bij voorbeeld om een collega te vertellen wat er vrijdag 3 Maart 2007 gebeurd is.";
    Dutch lang = new Dutch();
    testDemoText(lang, s,
      Arrays.asList("TE_ZNW", "MORFOLOGIK_RULE_NL_NL", "MORFOLOGIK_RULE_NL_NL", "MORFOLOGIK_RULE_NL_NL", "ZO_OPTIMAAL_MOGELIJK", "UPPERCASE_SENTENCE_START", "SPATIE_LEESTEKEN",  "NL_SIMPLE_REPLACE_MUSTHAVE", "BIJ_VOORBEELD", "DATE_WEEKDAY")
    );
    runTests(lang, null, "ýùźăŽČĆÅıøğåšĝÇİŞŠčžć±ą+-₃");
  }
}
