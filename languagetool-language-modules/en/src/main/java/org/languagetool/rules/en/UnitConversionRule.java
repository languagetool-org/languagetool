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

import org.languagetool.rules.AbstractUnitConversionRule;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import static tech.units.indriya.unit.Units.*;

/**
 * @since 4.3
 */
public class UnitConversionRule extends AbstractUnitConversionRule {


  protected NumberFormat format;

  public UnitConversionRule(ResourceBundle messages) {
    super(messages);
    format = NumberFormat.getNumberInstance(Locale.ENGLISH);
    format.setMaximumFractionDigits(2);
    format.setRoundingMode(RoundingMode.HALF_UP);
    addUnit("miles per hour", MILE.divide(HOUR), "miles per hour", 1, false);

    addUnit("kilograms?", KILOGRAM, "kilogram", 1e0, true);
    addUnit("grams?", KILOGRAM, "gram", 1e-3, true);
    addUnit("tons?", KILOGRAM, "ton", 1e3, true);

    addUnit("pounds?", POUND, "pounds", 1, false);
    addUnit("ounces?", OUNCE, "ounces", 1, false);

    addUnit("feet", FEET, "feet", 1, false);
    addUnit("miles?", MILE, "miles", 1, false);
    addUnit("yards?", YARD, "yards", 1, false);
    addUnit("inch(es)?", INCH, "inches", 1, false);

    addUnit( "(?:degrees?)? Fahrenheit", FAHRENHEIT, "degree Fahrenheit", 1, false);
    addUnit( "(?:degrees?)? Celsius", CELSIUS, "degree Celsius", 1, true);
  }

  @Override
  public String getId() {
    return "METRIC_UNITS_EN_GENERAL";
  }

  @Override
  public String getDescription() {
    return "Suggests or checks conversion of units to their metric equivalents.";
  }

  @Override
  protected NumberFormat getNumberFormat() {
    return format;
  }
}
