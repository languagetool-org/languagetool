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

import org.languagetool.rules.AbstractUnitConversionRule;
import org.languagetool.rules.Example;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import static tech.units.indriya.unit.Units.*;

/**
 * @since 4.3
 */
public class UnitConversionRule extends AbstractUnitConversionRule {

  private final NumberFormat format;

  public UnitConversionRule(ResourceBundle messages) {
    super(messages);
    format = NumberFormat.getNumberInstance(Locale.GERMANY);
    format.setMaximumFractionDigits(2);
    format.setRoundingMode(RoundingMode.HALF_UP);
    addExamplePair(Example.wrong("Ich bin <marker>6 Fuß</marker> groß."),
                   Example.fixed("Ich bin <marker>6 Fuß (1,83 m)</marker> groß."));

    addUnit("Kilo(gramm)?", KILOGRAM, "Kilogramm", 1, true);
    addUnit("Gramm", KILOGRAM, "Gramm", 1e-3, true);
    addUnit("Tonnen?", KILOGRAM, "Tonnen", 1e3, true);
    addUnit("Pfund", POUND, "Pfund", 1, false);

    addUnit("Meilen?", MILE, "Meile", 1, false);
    addUnit("Yard", YARD, "Yard", 1, false);
    addUnit("Fuß", FEET, "Fuß", 1, false);
    addUnit("Zoll", INCH, "Zoll", 1, false);

    addUnit("(Kilometer pro Stunde|Stundenkilometer)", KILOMETRE_PER_HOUR, "Kilometer pro Stunde", 1, true);
    addUnit("Meilen pro Stunde", MILE.divide(HOUR), "Meilen pro Stunde", 1, false);

    addUnit("Meter", METRE, "Meter", 1, true);
    addUnit("Kilometer", METRE, "Kilometer", 1e3, true);
    addUnit("Dezimeter", METRE, "Dezimeter", 1e-1, false); // metric, but should not be suggested
    addUnit("Zentimeter", METRE, "Zentimeter", 1e-2, true);
    addUnit("Millimeter", METRE, "Millimeter", 1e-3, true);
    addUnit("Mikrometer", METRE, "Mikrometer", 1e-6, true);
    addUnit("Nanometer", METRE, "Nanometer", 1e-9, true);
    addUnit("Pikometer", METRE, "Pikometer", 1e-12, true);
    addUnit("Femtometer", METRE, "Femtometer", 1e-15, true);

    addUnit("Quadratmeter", SQUARE_METRE, "Quadratmeter", 1, true);
    addUnit("Hektar", SQUARE_METRE, "Hektar", 1e4, true);
    addUnit("Ar", SQUARE_METRE, "Ar", 1e2, true);
    addUnit("Quadratkilometer", SQUARE_METRE,  "Quadratkilometer",  1e6, true);
    addUnit("Quadratdezimeter", SQUARE_METRE,  "Quadratdezimeter",  1e-2, false/*true*/); // Metric, but not commonly used
    addUnit("Quadratzentimeter", SQUARE_METRE, "Quadratzentimeter", 1e-4, true);
    addUnit("Quadratmillimeter", SQUARE_METRE, "Quadratmillimeter", 1e-6, true);
    addUnit("Quadratmikrometer", SQUARE_METRE, "Quadratmikrometer", 1e-12, true);
    addUnit("Quadratnanometer", SQUARE_METRE,  "Quadratnanometer",  1e-18, true);

    addUnit("Kubikmeter", CUBIC_METRE,     "Kubikmeter",      1, true);
    addUnit("Kubikkilometer", CUBIC_METRE, "Kubikkilometer",  1e9, true);
    addUnit("Kubikdezimeter", CUBIC_METRE, "Kubikdezimeter",  1e-3, false/*true*/); // Metric, but not commonly used
    addUnit("Kubikzentimeter", CUBIC_METRE,"Kubikzentimeter", 1e-6, true);
    addUnit("Kubikmillimeter", CUBIC_METRE,"Kubikmillimeter", 1e-9, true);
    addUnit("Kubikmikrometer", CUBIC_METRE,"Kubikmikrometer", 1e-18, true);
    addUnit("Kubiknanometer", CUBIC_METRE, "Kubiknanometer",  1e-27, true);

    addUnit("Liter", LITRE, "Liter", 1, true);
    addUnit("Milliliter", LITRE, "Milliliter", 1e-3, true);

    addUnit( "(?:Grad)? Fahrenheit", FAHRENHEIT, "Grad Fahrenheit", 1, false);
    addUnit( "(?:Grad)? Celsius", CELSIUS, "Grad Celsius", 1, true);
  }

  @Override
  public String getId() {
    return "EINHEITEN_METRISCH";
  }

  @Override
  public String getDescription() {
    return "Schlägt vor oder überprüft Angaben des metrischen Äquivalentes bei bestimmten Maßeinheiten.";
  }

  @Override
  protected String getMessage(Message message) {
    switch(message) {
      case CHECK:
        return "Diese Umrechnung scheint falsch zu sein. Wollen Sie sie automatisch korrigieren lassen?";
      case SUGGESTION:
        return "Wollen Sie eine Umwandlung ins metrische System automatisch hinzufügen?";
      case CHECK_UNKNOWN_UNIT:
        return "Die in dieser Umrechnung verwendete Einheit wurde nicht erkannt.";
      case UNIT_MISMATCH:
        return "Diese Einheiten sind nicht kompatibel.";
      default:
        throw new RuntimeException("Unknown message type: " + message);
    }
  }

  @Override
  protected String getShortMessage(Message message) {
    switch(message) {
      case CHECK:
        return "Falsche Umrechung. Automatisch korrigieren?";
      case SUGGESTION:
        return "Metrisches Äquivalent hinzufügen?";
      case CHECK_UNKNOWN_UNIT:
        return "Unbekannte Einheit.";
      case UNIT_MISMATCH:
        return "Inkompatible Einheiten.";
      default:
        throw new RuntimeException("Unknown message type: " + message);
    }
  }

  @Override
  protected NumberFormat getNumberFormat() {
    return format;
  }

}
