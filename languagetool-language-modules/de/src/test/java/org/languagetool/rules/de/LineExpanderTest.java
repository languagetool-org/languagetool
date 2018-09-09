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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class LineExpanderTest {

  private final LineExpander exp = new LineExpander();

  @Test
  public void testExpansion() {
    assertThat(expand(""), is("[]"));
    assertThat(expand("Das"), is("[Das]"));
    assertThat(expand("Tisch/E"), is("[Tisch, Tische]"));
    assertThat(expand("Tische/N"), is("[Tische, Tischen]"));
    assertThat(expand("Auto/S"), is("[Auto, Autos]"));
    assertThat(expand("klein/A"), is("[klein, kleine, kleiner, kleines, kleinen, kleinem]"));
    assertThat(expand("x/NSE"), is("[x, xn, xs, xe]"));
    assertThat(expand("x/NA"), is("[x, xn, xe, xer, xes, xen, xem]"));
    assertThat(expand("Das  #foo"), is("[Das]"));
    assertThat(expand("Tisch/E  #bla #foo"), is("[Tisch, Tische]"));
  }

  private String expand(String line) {
    return exp.expandLine(line).toString();
  }

}