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

import org.junit.Test;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.tagging.de.GermanToken;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class GermanHelperTest {
  
  @Test
  public void testHasReadingOfType() throws Exception {
    AnalyzedTokenReadings readings = new AnalyzedTokenReadings(new AnalyzedToken("der", "ART:DEF:DAT:SIN:FEM", null), 0);
    assertTrue(GermanHelper.hasReadingOfType(readings, GermanToken.POSType.DETERMINER));
    assertFalse(GermanHelper.hasReadingOfType(readings, GermanToken.POSType.NOMEN));
  }
  
  @Test
  public void testGetDeterminerNumber() throws Exception {
    assertThat(GermanHelper.getDeterminerNumber("ART:DEF:DAT:SIN:FEM"), is("SIN"));
  }

  @Test
  public void testGetDeterminerDefiniteness() throws Exception {
    assertThat(GermanHelper.getDeterminerDefiniteness("ART:DEF:DAT:SIN:FEM"), is("DEF"));
  }

  @Test
  public void testGetDeterminerCase() throws Exception {
    assertThat(GermanHelper.getDeterminerCase("ART:DEF:DAT:SIN:FEM"), is("DAT"));
  }

  @Test
  public void testGetDeterminerGender() throws Exception {
    assertThat(GermanHelper.getDeterminerGender(null), is(""));
    assertThat(GermanHelper.getDeterminerGender(""), is(""));
    assertThat(GermanHelper.getDeterminerGender("ART:DEF:DAT:SIN:FEM"), is("FEM"));
  }
  
}
