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

import org.junit.Test;
import org.languagetool.JLanguageTool;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class AbstractUnitConversionRuleTest {


  //@Test
  //public void formatMeasurement() {
  //  AbstractUnitConversionRule filter = new AbstractUnitConversionRule(JLanguageTool.getMessageBundle()) {
  //    @Override
  //    public String getId() {
  //      return null;
  //    }
  //
  //    @Override
  //    public String getDescription() {
  //      return null;
  //    }
  //  };
  //  assertThat(filter.formatMeasurement("1", "ft"), is("0.3048m"));
  //  assertThat(filter.formatMeasurement("2", "ft"), is(2*0.3048+"m"));
  //  assertThat(filter.formatMeasurement("1", "m"), is("1m"));
  //  assertThat(filter.formatMeasurement("100", "mph"), is("160.9343km/h"));
  //  try {
  //    filter.formatMeasurement("1", "xyz");
  //    fail("Expected IllegalArgumentException to be thrown");
  //  } catch(IllegalArgumentException e) {
  //    assertTrue(e.getMessage().contains("Could not parse measurement"));
  //  }
  //  try {
  //    filter.formatMeasurement("foobar", "m");
  //    fail("Expected IllegalArgumentException to be thrown");
  //  } catch(NumberFormatException e) {
  //    assertTrue(e.getMessage().contains("foobar"));
  //  }
  //}
}
