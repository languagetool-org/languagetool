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

import org.languagetool.tools.Tools;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import static tech.units.indriya.unit.Units.*;

/**
 * @since 4.3
 */
public class UnitConversionRuleUS extends UnitConversionRule {

  public UnitConversionRuleUS(ResourceBundle messages) {
    super(messages);
    setUrl(Tools.getUrl("https://languagetool.org/insights/post/imperial-metric-system/"));

    format = NumberFormat.getNumberInstance(Locale.US);
    format.setMaximumFractionDigits(2);
    format.setRoundingMode(RoundingMode.HALF_UP);

    addUnit("(kilometre|kilometer)s? per hour", KILOMETRE_PER_HOUR, "kilometers per hour", 1, true);

    addUnit("kilomet(re|er)s?", METRE, "kilometers", 1e3, true);
    addUnit("met(re|er)s?", METRE, "meters", 1e0, true);
    addUnit("decimet(re|er)s?", METRE, "decimeters", 1e-1, false/*true*/); // Metric, but not commonly used
    addUnit("centimet(re|er)s?", METRE, "centimeters", 1e-2, true);
    addUnit("millimet(re|er)s?", METRE, "micrometers", 1e-3, true);
    addUnit("micromet(re|er)s?", METRE, "micrometers", 1e-6, true);
    addUnit("nanomet(re|er)s?", METRE, "nanometers", 1e-9, true);


    addUnit("square met(re|er)s?", SQUARE_METRE, "square meters", 1, true);
    addUnit("square kilomet(re|er)s?", SQUARE_METRE, "square kilometers", 1e6, true);
    addUnit("square decimet(re|er)s?", SQUARE_METRE, "square decimeters", 1e-2, false/*true*/); // Metric, but not commonly used
    addUnit("square centimet(re|er)s?", SQUARE_METRE, "square centimeters", 1e-4, true);
    addUnit("square millimet(re|er)s?", SQUARE_METRE, "square millimeters", 1e-6, true);
    addUnit("square micromet(re|er)s?", SQUARE_METRE, "square micrometers", 1e-12, true);
    addUnit("square nanomet(re|er)s?", SQUARE_METRE, "square nanometers", 1e-18, true);

    addUnit("cubic met(re|er)s?", CUBIC_METRE, "cubic meters", 1, true);
    addUnit("cubic kilomet(re|er)s?", CUBIC_METRE, "cubic kilometers", 1e9, true);
    addUnit("cubic decimet(re|er)s?", CUBIC_METRE, "cubic decimeters", 1e-3, false/*true*/); // Metric, but not commonly used
    addUnit("cubic centimet(re|er)s?", CUBIC_METRE, "cubic centimeters", 1e-6, true);
    addUnit("cubic millimet(re|er)s?", CUBIC_METRE, "cubic millimeters", 1e-9, true);
    addUnit("cubic micromet(re|er)s?", CUBIC_METRE, "cubic micrometers", 1e-18, true);
    addUnit("cubic nanomet(re|er)s?", CUBIC_METRE, "cubic nanometers", 1e-27, true);

    addUnit("lit(re|er)s?", LITRE, "liters", 1, true);
    addUnit("millilit(re|er)s?", LITRE, "milliliters", 1e-3, true);

    addUnit("qt\\.", US_QUART, "qt.", 1, false);
    addUnit("gal", US_GALLON, "gal", 1, false);
    addUnit("pt", US_PINT, "pt", 1, false);
    addUnit("cup", US_CUP, "cups", 1, false);
    addUnit("(?:fl.? oz.?|oz. fl.)", US_FL_OUNCE, "fl oz", 1, false);

    addUnit("quarts?", US_QUART, "quarts", 1, false);
    addUnit("gallons?", US_GALLON, "gallons", 1, false);
    addUnit("pints?", US_PINT, "pints", 1, false);
    addUnit("cups?", US_CUP, "cups", 1, false);
    addUnit("(fluid )?ounces?", US_FL_OUNCE, "fluid ounces", 1, false);
  }

  @Override public String getId() {
    return "METRIC_UNITS_EN_US";
  }
}
