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

package org.languagetool.rules.nl;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.rules.AbstractUnitConversionRule;
import org.languagetool.rules.UnitConversionRuleTestHelper;

import java.io.IOException;

public class DutchUnitConversionRuleTest {

	/* Localized from the German version by Mark Baas
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
		Language lang = Languages.getLanguageForShortCode("nl");
		JLanguageTool lt = new JLanguageTool(lang);
		DutchUnitConversionRule rule = new DutchUnitConversionRule(JLanguageTool.getMessageBundle(lang));
		assertMatches("Ik ben 6 voet lang.", 1, "1,83 meter", rule, lt);
		assertMatches("Ik ben 6 voet (2,02 m) lang.", 1, "1,83 meter", rule, lt);
		assertMatches("Ik ben 6 voet (1,82 m) lang.", 0, null, rule, lt);
		assertMatches("De weg is 100 mijl lang.", 1, "160,93 kilometer", rule, lt);
		assertMatches("De weg is 10 km (20 mijlen) lang.", 1, "6,21", rule, lt);
		assertMatches("De weg is 10 km (6,21 mijl) lang.", 0, null, rule, lt);
		assertMatches("De weg is 100 mijl (160,93 kilometer) lang.", 0, null, rule, lt);
		assertMatches("De lading is 10.000,75 pond zwaar.", 1, "4,54 ton", rule, lt);
		assertMatches("Zij is 5'6\" lang.", 1, "1,68 m", rule, lt);
		assertMatches("Mijn nieuwe huis heeft een oppervlakte van 500 sq ft.", 1, "46,45 vierkante meter", rule, lt);
		assertMatches("Tussen 330'000 en 500'000/600", 0, null, rule, lt);
	}

	private void assertMatches(String input, int expectedMatches, String converted, AbstractUnitConversionRule rule, JLanguageTool lt) throws IOException {
		unitConversionRuleTestHelper.assertMatches(input, expectedMatches, converted, rule, lt);
	}
}
