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

  private final UnitConversionRuleTestHelper helper = new UnitConversionRuleTestHelper();

  @Test
  public void match() throws IOException {
    Language lang = Languages.getLanguageForShortCode("en-US");
    JLanguageTool lt = new JLanguageTool(lang);
    AbstractUnitConversionRule rule = new UnitConversionRuleUS(JLanguageTool.getMessageBundle(lang));
    helper.assertMatches("I just drank 3 pints.", 1, "1.42 l", rule, lt);
    helper.assertMatches("I am 6 feet (2.02 m) tall.", 1, "1.83 meters", rule, lt);
    helper.assertMatches("16 kilometers (10 miles)", 1, null, rule, lt);
    helper.assertMatches("16 kilometers (ca. 10 miles)", 0, null, rule, lt);
    helper.assertMatches("16 kilometers (9.94 mi)", 0, null, rule, lt);
    // matched by antipattern;
    helper.assertMatches("Use 3-5 pounds of butter.", 0, null, rule, lt);
    helper.assertMatches("It's 1/4 mile away", 0, null, rule, lt);
    helper.assertMatches("A 2:1 cup", 0, null, rule, lt);
    helper.assertMatches("1⁄4 cup", 0, null, rule, lt);
    // matched by special case in the case as long as "x ft y inch" is not parsed properly:
    helper.assertMatches("It is 0.76 metres (2 ft 6 inch)", 0, null, rule, lt);
  }

}
