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

package org.languagetool.rules.de;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.rules.AbstractUnitConversionRule;
import org.languagetool.rules.UnitConversionRuleTestHelper;

import java.io.IOException;

public class UnitConversionRuleTest {

  /* Still problematic:
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
    Language lang = Languages.getLanguageForShortCode("de");
    JLanguageTool lt = new JLanguageTool(lang);
    UnitConversionRule rule = new UnitConversionRule(JLanguageTool.getMessageBundle(lang));
    assertMatches("Ich bin 6 Fuß groß.", 1, "1,83 Meter", rule, lt);
    assertMatches("Ich bin 6 Fuß (2,02 m) groß.", 1, "1,83 Meter", rule, lt);
    assertMatches("Ich bin 6 Fuß (1,82 m) groß.", 0, null, rule, lt);
    assertMatches("Der Kostenvoranschlag hatte eine Höhe von 1.800 Pfund Sterling.", 0, null, rule, lt);
    assertMatches("Der Weg ist 100 Meilen lang.", 1, "160,93 Kilometer", rule, lt);
    assertMatches("Der Weg ist 10 km (20 Meilen) lang.", 1, "6,21", rule, lt);
    assertMatches("Der Weg ist 10 km (6,21 Meilen) lang.", 0, null, rule, lt);
    assertMatches("Der Weg ist 100 Meilen (160,93 Kilometer) lang.", 0, null, rule, lt);
    assertMatches("Die Ladung ist 10.000,75 Pfund schwer.", 1, "4,54 Tonnen", rule, lt);
    assertMatches("Sie ist 5'6\" groß.", 1, "1,68 m", rule, lt);
    assertMatches("Meine neue Wohnung ist 500 sq ft groß.", 1, "46,45 Quadratmeter", rule, lt);
    assertMatches("Zwischen 330'000 und 500'000/600", 0, null, rule, lt);
  }

  private void assertMatches(String input, int expectedMatches, String converted, AbstractUnitConversionRule rule, JLanguageTool lt) throws IOException {
    unitConversionRuleTestHelper.assertMatches(input, expectedMatches, converted, rule, lt);
  }
}
