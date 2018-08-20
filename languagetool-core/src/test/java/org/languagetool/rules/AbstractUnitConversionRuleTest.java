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

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class AbstractUnitConversionRuleTest {


  @Test
  public void convertToMetric() {
    assertThat(AbstractUnitConversionRule.convertToMetric("1ft"), is("0.3048m"));
    assertThat(AbstractUnitConversionRule.convertToMetric("2ft"), is(2*0.3048+"m"));
    assertThat(AbstractUnitConversionRule.convertToMetric("1m"), is("1m"));
    assertThat(AbstractUnitConversionRule.convertToMetric("100mph"), is("160.9343"));
    try {
      AbstractUnitConversionRule.convertToMetric("1xyz");
      fail("Expected IllegalArgumentException to be thrown");
    } catch(IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("Could not parse measurement"));
    }
  }
}
