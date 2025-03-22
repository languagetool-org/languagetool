/* LanguageTool, a natural language style checker 
 * Copyright (C) 2015 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.de;

import org.junit.Test;

import static junit.framework.Assert.fail;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class LineExpanderTest {

  private final LineExpander exp = new LineExpander();

  @Test
  public void testExpansion() {
    assertSetsEqual(expand(""), "[]");
    assertSetsEqual(expand("Das"), "[Das]");
    assertSetsEqual(expand("Tisch/E"), "[Tisch, Tische]");
    assertSetsEqual(expand("Tische/N"), "[Tische, Tischen]");
    assertSetsEqual(expand("Auto/S"), "[Auto, Autos]");
    assertSetsEqual(expand("klein/A"), "[klein, kleine, kleiner, kleines, kleinen, kleinem]");
    assertSetsEqual(expand("x/NSE"), "[x, xn, xs, xe]");
    assertSetsEqual(expand("x/NA"), "[x, xn, xe, xer, xes, xen, xem]");
    assertSetsEqual(expand("viertjüngste/A"), "[viertjüngste, viertjüngster, viertjüngstes, viertjüngsten, viertjüngstem]");
    assertSetsEqual(expand("Das  #foo"), "[Das]");
    assertSetsEqual(expand("Tisch/E  #bla #foo"), "[Tisch, Tische]");
    assertSetsEqual(expand("Goethestraße/T"), "[Goethestraße, Goethestr.]");
    assertSetsEqual(expand("Goethestrasse/T"), "[Goethestrasse, Goethestr.]");
    assertSetsEqual(expand("Zwingenberger Stra\u00DFe/T"), "[Zwingenberger Stra\u00DFe, Zwingenberger Str.]");
    assertSetsEqual(expand("Zwingenberger Strasse/T"), "[Zwingenberger Strasse, Zwingenberger Str.]");

    assertSetsEqual(expand("Escape\\/N"), "[Escape/N]");
    //assertThat(expand("Escape\\/N/S"), is("[Escape/N, Escape/Ns]"));  // combination of escape and flag not supported yet

    assertSetsEqual(expand("rüber_machen  #bla #foo"), "[rübermach, rübergemacht, rübermachest, rübermachst, rübermache, " +
                      "rübermachen, rübermachet, rübermachte, rübermachend, rübermachten, rübermacht, rübermachtest, " +
                      "rübermachtet, rüberzumachen, Rübermachens]");
    assertSetsEqual(expand("rüber_verschicken"), "[rüberverschickend, rüberverschickst, rüberverschick, rüberverschickest, " +
                      "rüberverschicktest, rüberverschicke, rüberverschicket, rüberverschickte, rüberverschicktet, rüberverschickten, " +
                      "rüberverschicken, rüberverschickt, rüberzuverschicken, Rüberverschickens]");
    assertSetsEqual(expand("escape\\_machen"), "[escape_machen]");

    try {
      expand("rüber/invalidword");
      fail();
    } catch (RuntimeException expected) {}
  }

  private String expand(String line) {
    return exp.expandLine(line).toString();
  }

  private void assertSetsEqual(String expected, String actual) {
    // Convert the strings to sets by parsing the bracket notation
    Set<String> expectedSet = parseStringToSet(expected);
    Set<String> actualSet = parseStringToSet(actual);
    
    // Compare the sets
    assertThat(actualSet, is(expectedSet));
  }
  private Set<String> parseStringToSet(String bracketString) {
    // Remove brackets and split by comma
    String content = bracketString.substring(1, bracketString.length() - 1);
    if (content.trim().isEmpty()) {
        return new HashSet<>();
    }
    
    // Split by comma and trim each element
    String[] elements = content.split(",");
    return Arrays.stream(elements)
            .map(String::trim)
            .collect(Collectors.toSet());
  }

}
