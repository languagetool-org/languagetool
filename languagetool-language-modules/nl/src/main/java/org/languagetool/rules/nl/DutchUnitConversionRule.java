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

import org.languagetool.rules.AbstractUnitConversionRule;
import org.languagetool.rules.Example;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import static tech.units.indriya.unit.Units.*;

/**
 * Localized from the German version by Mark Baas
 * @since 4.3
 */
public class DutchUnitConversionRule extends AbstractUnitConversionRule {

	private final NumberFormat format;

	public DutchUnitConversionRule(ResourceBundle messages) {
		super(messages);
		setDefaultTempOff(); // Temp off for now
		format = NumberFormat.getNumberInstance(Locale.GERMANY);
		format.setMaximumFractionDigits(2);
		format.setRoundingMode(RoundingMode.HALF_UP);
		addExamplePair(Example.wrong("Ik ben <marker>6 voet</marker> lang."),
					Example.fixed("Ik ben <marker>6 voet (1,83 m)</marker> lang."));

		addUnit("kilo(gram)?", KILOGRAM, "kilogram", 1, true);
		addUnit("gram", KILOGRAM, "gram", 1e-3, true);
		addUnit("ton", KILOGRAM, "ton", 1e3, true);
		addUnit("pond", POUND, "pond", 1, false);

		addUnit("mijl(en)?", MILE, "mijl", 1, false);
		addUnit("yard", YARD, "yard", 1, false);
		addUnit("voet", FEET, "voet", 1, false);
		addUnit("(inch|duim)?", INCH, "inch", 1, false);

		addUnit("kilometer per uur", KILOMETRE_PER_HOUR, "kilometer per uur", 1, true);
		addUnit("mijl per uur", MILE.divide(HOUR), "mijl per uur", 1, false);

		addUnit("meter", METRE, "meter", 1, true);
		addUnit("kilometer", METRE, "kilometer", 1e3, true);
		//addUnit("decimeter", METRE, "decimeter", 1e-1, false); // metric, but should not be suggested
		addUnit("centimeter", METRE, "centimeter", 1e-2, true);
		addUnit("millimeter", METRE, "millimeter", 1e-3, true);
		addUnit("micrometer", METRE, "micrometer", 1e-6, true);
		addUnit("nanometer", METRE, "nanometer", 1e-9, true);
		addUnit("picometer", METRE, "picometer", 1e-12, true);
		addUnit("femtometer", METRE, "femtometer", 1e-15, true);

		addUnit("vierkante meter", SQUARE_METRE, "vierkante meter", 1, true);
		addUnit("vierkante kilometer", SQUARE_METRE,  "vierkante kilometer",  1e6, true);
		addUnit("hectare", SQUARE_METRE, "hectare", 1e4, true);
		addUnit("(are|vierkante decimeter)?", SQUARE_METRE, "are", 1e2, true);
		addUnit("vierkante centimeter", SQUARE_METRE, "vierkante centimeter", 1e-4, true);
		addUnit("vierkante millimeter", SQUARE_METRE, "vierkante millimeter", 1e-6, true);
		addUnit("vierkante micrometer", SQUARE_METRE, "vierkante micrometer", 1e-12, true);
		addUnit("vierkante nanometer", SQUARE_METRE,  "vierkante nanometer",  1e-18, true);

		addUnit("kubieke meter", CUBIC_METRE, "kubieke meter", 1, true);
		addUnit("kubieke kilometer", CUBIC_METRE, "kubieke kilometer",  1e9, true);
		//addUnit("kubieke decimeter", CUBIC_METRE, "kubieke decimeter",  1e-3, false/*true*/); // Metric, but not commonly used
		addUnit("kubieke centimeter", CUBIC_METRE,"kubieke centimeter", 1e-6, true);
		addUnit("kubieke millimeter", CUBIC_METRE,"kubieke millimeter", 1e-9, true);
		addUnit("kubieke micrometer", CUBIC_METRE,"kubieke micrometer", 1e-18, true);
		addUnit("kubieke nanometer", CUBIC_METRE, "kubieke nanometer",  1e-27, true);

		addUnit("liter", LITRE, "liter", 1, true);
		addUnit("centiliter", LITRE, "centiliter", 1e-2, true);
		addUnit("milliliter", LITRE, "milliliter", 1e-3, true);

		addUnit("(?:graden)? Fahrenheit", FAHRENHEIT, "graden Fahrenheit", 1, false);
		addUnit("(?:graden)? Celsius", CELSIUS, "graden Celsius", 1, true);
	}

  @Override
	public String getId() {
		return "METRISCHE_EENHEDEN";
	}

	@Override
	public String getDescription() {
		return "Geeft suggesties en controleert indicaties van het metrische equivalent voor bepaalde meeteenheden.";
	}

	@Override
	protected String getMessage(Message message) {
		switch(message) {
			case CHECK:
				return "Deze conversie lijkt incorrect. Wilt u deze automatisch laten corrigeren?";
			case SUGGESTION:
				return "Wilt u automatisch een conversie naar het metrieke stelsel toevoegen?";
			case CHECK_UNKNOWN_UNIT:
				return "De eenheid die bij deze omrekening is gebruikt, werd niet herkend.";
			case UNIT_MISMATCH:
				return "Deze eenheden zijn niet compatibel.";
			default:
				throw new RuntimeException("Unknown message type: " + message);
		}
	}

	@Override
	protected String getShortMessage(Message message) {
		switch(message) {
			case CHECK:
				return "Incorrecte eenheidsconversie. Automatisch corrigeren?";
			case SUGGESTION:
				return "Metrisch equivalent toevoegen?";
			case CHECK_UNKNOWN_UNIT:
				return "Onbekende eenheid.";
			case UNIT_MISMATCH:
				return "Incompatibele eenheden.";
			default:
				throw new RuntimeException("Unknown message type: " + message);
		}
	}

	@Override
	protected NumberFormat getNumberFormat() {
		return format;
	}

}
