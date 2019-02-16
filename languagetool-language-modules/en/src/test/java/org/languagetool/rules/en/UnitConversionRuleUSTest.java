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
import org.languagetool.rules.AbstractUnitConversionRule;
import org.languagetool.rules.UnitConversionRuleTestHelper;

import java.io.IOException;

public class UnitConversionRuleUSTest {

  private final UnitConversionRuleTestHelper unitConversionRuleTestHelper = new UnitConversionRuleTestHelper();

  @Test
  public void match() throws IOException {
    Language lang = Languages.getLanguageForShortCode("en-US");
    JLanguageTool lt = new JLanguageTool(lang);
    AbstractUnitConversionRule rule = new UnitConversionRuleUS(JLanguageTool.getMessageBundle(lang));
    unitConversionRuleTestHelper.assertMatches("I just drank 3 pints.", 1, "1.42 l", rule, lt);
    unitConversionRuleTestHelper.assertMatches("I am 6 feet (2.02 m) tall.", 1, "1.83 meters", rule, lt);
    unitConversionRuleTestHelper.assertMatches("16 kilometers (10 miles)", 1, null, rule, lt);
    unitConversionRuleTestHelper.assertMatches("16 kilometers (ca. 10 miles)", 0, null, rule, lt);
    unitConversionRuleTestHelper.assertMatches("16 kilometers (9.94 mi)", 0, null, rule, lt);
  }

  private void assertMatches(String input, int expectedMatches, String converted, AbstractUnitConversionRule rule, JLanguageTool lt) throws IOException {
    unitConversionRuleTestHelper.assertMatches(input, expectedMatches, converted, rule, lt);
  }
}
