/*
 *  LanguageTool, a natural language style checker
 *  * Copyright (C) 2018 Fabian Richter
 *  *
 *  * This library is free software; you can redistribute it and/or
 *  * modify it under the terms of the GNU Lesser General Public
 *  * License as published by the Free Software Foundation; either
 *  * version 2.1 of the License, or (at your option) any later version.
 *  *
 *  * This library is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  * Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public
 *  * License along with this library; if not, write to the Free Software
 *  * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 *  * USA
 *
 */

package org.languagetool.rules.pt;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.rules.AbstractUnitConversionRule;
import org.languagetool.rules.UnitConversionRuleTestHelper;

import java.io.IOException;

public class PortugueseUnitConversionRuleTest {

  /* Localized from the German version by Tiago F. Santos
   Still problematic:
   Der Weg ist 10 km (20 Meilen) lang.
   6'682 Hektar
   zahlende Gebühr betrug bis zum 4. Juli 2005 5 Pfund,
   7,92 inch = 0,201168 m = 20,1168 cm
   Brennwert 210 kJ/100 g (50 kcal/100 g).
   69.852 Fuß (über 21 Kilometer)
   Als inoffizieller Nachfolger der 64'er
   ihre Flughöhe lag bei bis zu 18.000 m (60.000 ft).
   5.808,5 km (3.610 Meilen)
   3 000 Meilen lang
   */

  private final UnitConversionRuleTestHelper unitConversionRuleTestHelper = new UnitConversionRuleTestHelper();

  @Test
  public void match() throws IOException {
    Language lang = Languages.getLanguageForShortCode("pt");
    JLanguageTool lt = new JLanguageTool(lang);
    PortugueseUnitConversionRule rule = new PortugueseUnitConversionRule(JLanguageTool.getMessageBundle(lang));
    assertMatches("Eu tenho 6 pés de altura.", 1, "1,83 metros", rule, lt);
    assertMatches("Eu tenho 6 pés (2,02 m) de altura.", 1, "1,83 metros", rule, lt);
    assertMatches("Eu tenho 6 pés (1,82 m) de altura.", 0, null, rule, lt);assertMatches("A via tem 100 milhas de comprimento.", 1, "160,93 quilómetros", rule, lt);
    assertMatches("A via tem 10 km (20 milhas) de comprimento.", 1, "6,21", rule, lt);
    assertMatches("A via tem 10 km (6,21 milhas) de comprimento.", 0, null, rule, lt);
    assertMatches("A via tem 100 milhas (160,93 quilómetros) de comprimento.", 0, null, rule, lt);
    assertMatches("A carga é de 10.000 libras.", 1, "4,54 toneladas", rule, lt);
    assertMatches("Isto tem 5'6\" de altura.", 1, "1,68 m", rule, lt);
    assertMatches("O meu novo apartamento tem 500 sq ft de área.", 1, "46,45 metros quadrados", rule, lt);
    assertMatches("Sendo a latitude 8º 32' 00\" e a longitude 39º 22' 49\".", 0, null, rule, lt);
    assertMatches("Sendo a latitude 8º32'00\" e a longitude 39º22'49\".", 0, null, rule, lt);
  }

  private void assertMatches(String input, int expectedMatches, String converted, AbstractUnitConversionRule rule, JLanguageTool lt) throws IOException {
    unitConversionRuleTestHelper.assertMatches(input, expectedMatches, converted, rule, lt);
  }
}
