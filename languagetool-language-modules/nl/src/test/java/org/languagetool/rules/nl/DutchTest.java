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
    // NOTE: this text needs to be kept in sync with WelcomeController.php's getDefaultDemoTexts():
    String s = "Languagetool doet van zelfsprekend veel meer dan spellingcontrole. Het ziet het ook fouten die minder inde gaten lopen, die je zelf geen eens ziet. De meldingen komen uit regels die door vrijwilligers gemaakt zijn aan de hand van suggesties van gebruikers en tips van taaldeskundigen. Ondanks het feit dat er veel aandacht aan de regels wordt besteed, blijven suggesties altijd welkom op het forum of op Twitter: @languagetool_nl. Probeer het rustig zelf eens uit hier, of download een van de plugins op deze pagina.";
    Dutch lang = new Dutch();
    testDemoText(lang, s,
      Arrays.asList("MORFOLOGIK_RULE_NL_NL", "NL_SIMPLE_REPLACE", "IN_DE", "GEEN_EENS", "AAN_DE_HAND_VAN", "ONDANKS_HET_FEIT_DAT", "MORFOLOGIK_RULE_NL_NL")
    );
    runTests(lang);
  }
}
