/* LanguageTool, a natural language style checker
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package org.languagetool.rules.nl;

import org.junit.Test;
import org.languagetool.rules.FakeRule;
import org.languagetool.rules.RuleMatch;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class DateCheckFilterTest {

  private final RuleMatch match = new RuleMatch(new FakeRule(), null, 0, 10, "message");
  private final DateCheckFilter filter = new DateCheckFilter();

  @Test
  public void testAccept() throws Exception {
    assertNull(filter.acceptRuleMatch(match, makeMap("2014", "8" ,"23", "zaterdag"), null));  // correct date
    assertNotNull(filter.acceptRuleMatch(match, makeMap("2014", "8" ,"23", "zondag"), null));  // incorrect date
  }

  private Map<String, String> makeMap(String year, String month, String dayOfMonth, String weekDay) {
    Map<String,String> map = new HashMap<>();
    map.put("year", year);
    map.put("month", month);
    map.put("day", dayOfMonth);
    map.put("weekDay", weekDay);
    return map;
  }

}
