/* LanguageTool, a natural language style checker 
 * Copyright (C) 2013 Daniel Naber (http://www.danielnaber.de)
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
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.tagging.de.GermanToken;

import static org.hamcrest.CoreMatchers.is;

public class GermanHelperTest {
  
  @Test
  public void testHasReadingOfType() throws Exception {
    AnalyzedTokenReadings readings = new AnalyzedTokenReadings(new AnalyzedToken("der", "ART:DEF:DAT:SIN:FEM", null), 0);
    Assertions.assertTrue(GermanHelper.hasReadingOfType(readings, GermanToken.POSType.DETERMINER));
    Assertions.assertFalse(GermanHelper.hasReadingOfType(readings, GermanToken.POSType.NOMEN));
  }
  
  @Test
  public void testGetDeterminerNumber() throws Exception {
    MatcherAssert.assertThat(GermanHelper.getDeterminerNumber("ART:DEF:DAT:SIN:FEM"), is("SIN"));
  }

  @Test
  public void testGetDeterminerDefiniteness() throws Exception {
    MatcherAssert.assertThat(GermanHelper.getDeterminerDefiniteness("ART:DEF:DAT:SIN:FEM"), is("DEF"));
  }

  @Test
  public void testGetDeterminerCase() throws Exception {
    MatcherAssert.assertThat(GermanHelper.getDeterminerCase("ART:DEF:DAT:SIN:FEM"), is("DAT"));
  }

  @Test
  public void testGetDeterminerGender() throws Exception {
    MatcherAssert.assertThat(GermanHelper.getDeterminerGender(null), is(""));
    MatcherAssert.assertThat(GermanHelper.getDeterminerGender(""), is(""));
    MatcherAssert.assertThat(GermanHelper.getDeterminerGender("ART:DEF:DAT:SIN:FEM"), is("FEM"));
  }
  
}
