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

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;

public class LineExpanderTest {

  private final LineExpander exp = new LineExpander();

  @Test
  public void testExpansion() {
    MatcherAssert.assertThat(expand(""), is("[]"));
    MatcherAssert.assertThat(expand("Das"), is("[Das]"));
    MatcherAssert.assertThat(expand("Tisch/E"), is("[Tisch, Tische]"));
    MatcherAssert.assertThat(expand("Tische/N"), is("[Tische, Tischen]"));
    MatcherAssert.assertThat(expand("Auto/S"), is("[Auto, Autos]"));
    MatcherAssert.assertThat(expand("klein/A"), is("[klein, kleine, kleiner, kleines, kleinen, kleinem]"));
    MatcherAssert.assertThat(expand("x/NSE"), is("[x, xn, xs, xe]"));
    MatcherAssert.assertThat(expand("x/NA"), is("[x, xn, xe, xer, xes, xen, xem]"));
    MatcherAssert.assertThat(expand("viertjüngste/A"), is("[viertjüngste, viertjüngster, viertjüngstes, viertjüngsten, viertjüngstem]"));
    MatcherAssert.assertThat(expand("Das  #foo"), is("[Das]"));
    MatcherAssert.assertThat(expand("Tisch/E  #bla #foo"), is("[Tisch, Tische]"));

    MatcherAssert.assertThat(expand("Escape\\/N"), is("[Escape/N]"));
    //assertThat(expand("Escape\\/N/S"), is("[Escape/N, Escape/Ns]"));  // combination of escape and flag not supported yet

    MatcherAssert.assertThat(expand("rüber_machen  #bla #foo"), is("[rübermach, rübergemacht, rübermachest, rübermachst, rübermache, " +
                      "rübermachen, rübermachet, rübermachte, rübermachend, rübermachten, rübermacht, rübermachtest, " +
                      "rübermachtet, rüberzumachen, Rübermachens]"));
    MatcherAssert.assertThat(expand("rüber_verschicken"), is("[rüberverschickend, rüberverschickst, rüberverschick, rüberverschickest, " +
                      "rüberverschicktest, rüberverschicke, rüberverschicket, rüberverschickte, rüberverschicktet, rüberverschickten, " +
                      "rüberverschicken, rüberverschickt, rüberzuverschicken, Rüberverschickens]"));
    MatcherAssert.assertThat(expand("escape\\_machen"), is("[escape_machen]"));

    try {
      expand("rüber/invalidword");
      Assertions.fail();
    } catch (RuntimeException expected) {}
  }

  private String expand(String line) {
    return exp.expandLine(line).toString();
  }

}
