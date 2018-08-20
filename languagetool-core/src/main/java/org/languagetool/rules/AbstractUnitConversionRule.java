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

package org.languagetool.rules;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static tech.units.indriya.unit.Units.*;

import javax.measure.Unit;
import javax.measure.quantity.*;

public abstract class AbstractUnitConversionRule {
  protected static final Map<Pattern, Unit> unitPatterns = new HashMap<>();

  protected static final Unit[] metricUnits = new Unit[] { METRE, KILOGRAM, KILOMETRE_PER_HOUR };


  protected static NumberFormat getNumberFormat() {
    DecimalFormat df = new DecimalFormat();
    df.setMaximumFractionDigits(4);
    df.setRoundingMode(RoundingMode.HALF_UP);
    return df;
  }

  static {
    unitPatterns.put(Pattern.compile("(\\d+)kg"), KILOGRAM);
    unitPatterns.put(Pattern.compile("(\\d+)g"), GRAM);
    unitPatterns.put(Pattern.compile("(\\d+)t"), KILOGRAM.multiply(1e3));

    Unit<Mass> POUND = KILOGRAM.multiply(0.45359237);
    unitPatterns.put(Pattern.compile("(\\d+)lb"), POUND);
    unitPatterns.put(Pattern.compile("(\\d+)oz"), POUND.divide(12));

    unitPatterns.put(Pattern.compile("(\\d+)km"), METRE.multiply(1e3));
    unitPatterns.put(Pattern.compile("(\\d+)m"), METRE);
    unitPatterns.put(Pattern.compile("(\\d+)dm"), METRE.divide(1e1));
    unitPatterns.put(Pattern.compile("(\\d+)cm"), METRE.divide(1e2));
    unitPatterns.put(Pattern.compile("(\\d+)mm"), METRE.divide(1e3));
    unitPatterns.put(Pattern.compile("(\\d+)µm"), METRE.divide(1e6));
    unitPatterns.put(Pattern.compile("(\\d+)nm"), METRE.divide(1e9));

    Unit<Length> FEET = METRE.multiply(0.3048);
    Unit<Length> MILE = FEET.multiply(5280);
    unitPatterns.put(Pattern.compile("(\\d+)(ft|′|')"), FEET);
    unitPatterns.put(Pattern.compile("(\\d+)mi"), MILE);
    unitPatterns.put(Pattern.compile("(\\d+)yd"), FEET.multiply(3));
    unitPatterns.put(Pattern.compile("(\\d+)(in|\"|″)"), FEET.divide(12));

    unitPatterns.put(Pattern.compile("(\\d+)(km/h|kmh)"), KILOMETRE_PER_HOUR);
    unitPatterns.put(Pattern.compile("(\\d+)(mph)"), MILE.divide(HOUR));
  }

  static String convertToMetric(String measurement) throws IllegalArgumentException {
    Unit unit = null;
    double value = 0;
    for (Pattern pattern : unitPatterns.keySet()) {
      Matcher matcher = pattern.matcher(measurement);
      if (matcher.matches()) {
        unit = unitPatterns.get(pattern);
        value = Double.parseDouble(matcher.group(1));
        break;
      }
    }
    if (unit == null) {
      throw new IllegalArgumentException("Could not parse measurement: " + measurement);
    }
    for (Unit metric : metricUnits) {
      if (unit.isCompatible(metric)) {
        double converted = unit.getConverterTo(metric).convert(value);
        long rounded = Math.round(converted);
        //noinspection FloatingPointEquality
        if (converted == rounded) {
          return rounded + metric.getSymbol();
        }
        return getNumberFormat().format(converted) + metric.getSymbol();
      }
    }
    throw new IllegalArgumentException("Unit " + unit + " is not convertible to any metric unit.");
  }
}
