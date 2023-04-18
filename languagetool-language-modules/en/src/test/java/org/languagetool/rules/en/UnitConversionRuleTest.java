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

package org.languagetool.rules.en;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.rules.UnitConversionRuleTestHelper;

import java.io.IOException;

public class UnitConversionRuleTest {

  private final UnitConversionRuleTestHelper helper = new UnitConversionRuleTestHelper();

  /* still problematic:
  16 kilometers (10 miles)
  84 meters (276 feet)
  (26°11'E)
  (2/3 fl oz)
  20 miles (32 km)
  EveR-1's
  95 lb/ft3
  Apollo 8's profile had two different
  Electric trolleys (at 12 miles per hour) became the main transportation
   8 to 75 centimetres (3 to 30 inches)
  55 km (34 miles)
   */

  @Test
  public void match() throws IOException {
    Language lang = Languages.getLanguageForShortCode("en");
    JLanguageTool lt = new JLanguageTool(lang);
    UnitConversionRule rule = new UnitConversionRule(JLanguageTool.getMessageBundle(lang));
    helper.assertMatches("I am 6 feet tall.", 1, "1.83 m", rule, lt);
    helper.assertMatches("I am 6 feet (2.02 m) tall.", 1, "1.83 m", rule, lt);
    helper.assertMatches("I am 6 feet (1.82 m) tall.", 0, null, rule, lt);
    helper.assertMatches("Heat up to 18\u00A0°C (64.4 °F).", 0, null, rule, lt);
    helper.assertMatches("Heat up to 18\u00A0°C (64.4\u00A0°F).", 0, null, rule, lt);
    helper.assertMatches("Heat up to 18 °C (64.4\u00A0°F).", 0, null, rule, lt);
    helper.assertMatches("The path is 100 miles long.", 1, "160.93 km", rule, lt);
    helper.assertMatches("The path is 100 miles (160.93 km) long.", 0, null, rule, lt);
    helper.assertMatches("The shipment weighs 10,000.75 pounds.", 1, "4.54 t", rule, lt);
    helper.assertMatches("She is 5'6\".", 1, "1.68 m", rule, lt);
    helper.assertMatches("My new apartment is 500 sq ft.", 1, "46.45 m²", rule, lt);
    helper.assertMatches("It is 100 degrees Fahrenheit outside.", 1, "37.78 °C", rule, lt);
    helper.assertMatches("It is 100 °F outside.", 1, "37.78 °C", rule, lt);
    helper.assertMatches("It is -22 °F outside.", 1, "-30 °C", rule, lt);
    helper.assertMatches("It is -22 degrees Fahrenheit outside.", 1, "-30 °C", rule, lt);

    // https://github.com/languagetool-org/languagetool/issues/2357
    helper.assertMatches("Millions watched the 1989's Superbowl.", 0, null, rule, lt);

    // https://github.com/languagetool-org/languagetool/issues/2255
    helper.assertMatches("Coordinates: 34°09′22″N 118°7′55″W", 0, null, rule, lt);
  }

}
