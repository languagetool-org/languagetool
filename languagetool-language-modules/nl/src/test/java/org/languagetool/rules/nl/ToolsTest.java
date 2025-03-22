/* LanguageTool, a natural language style checker
 * Copyright (C) 2023 Daniel Naber (http://www.danielnaber.de)
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

import java.util.Arrays;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ToolsTest {

  @Test
  public void testBasicConcatenation() {
    assertThat(Tools.glueParts(Arrays.asList("huis", "deur")), is("huisdeur"));
    assertThat(Tools.glueParts(Arrays.asList("tv", "programma")), is("tv-programma"));
    assertThat(Tools.glueParts(Arrays.asList("auto2", "deurs")), is("auto2-deurs"));
    assertThat(Tools.glueParts(Arrays.asList("zee", "eend")), is("zee-eend"));
    assertThat(Tools.glueParts(Arrays.asList("mms", "eend")), is("mms-eend"));
    assertThat(Tools.glueParts(Arrays.asList("EersteKlas", "service")), is("EersteKlasservice"));
    assertThat(Tools.glueParts(Arrays.asList("3D", "printer")), is("3Dprinter"));
    assertThat(Tools.glueParts(Arrays.asList("groot", "moeder", "huis")), is("grootmoederhuis"));
    assertThat(Tools.glueParts(Arrays.asList("sport", "tv", "uitzending")), is("sport-tv-uitzending"));
    assertThat(Tools.glueParts(Arrays.asList("auto-", "pilot")), is("auto-pilot"));
    assertThat(Tools.glueParts(Arrays.asList("foto", "5d", "camera")), is("foto-5dcamera"));
    assertThat(Tools.glueParts(Arrays.asList("xyZ", "xyz")), is("xyZ-xyz"));
    assertThat(Tools.glueParts(Arrays.asList("xyZ", "Xyz")), is("xyZ-Xyz"));
    assertThat(Tools.glueParts(Arrays.asList("xyz", "Xyz")), is("xyz-Xyz"));
    assertThat(Tools.glueParts(Arrays.asList("xxx-z", "yyy")), is("xxx-z-yyy"));
  }

}