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
package org.languagetool.rules.patterns;

import org.junit.Test;
import org.languagetool.FakeLanguage;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class MatchStateTest {
  
  @Test
  public void testConvertCase() {
    MatchState startUpper = getMatchState(Match.CaseConversion.STARTUPPER);
    assertNull(startUpper.convertCase(null, "Y", new FakeLanguage("en")));
    assertThat(startUpper.convertCase("", "Y", new FakeLanguage("en")), is(""));
    assertThat(startUpper.convertCase("x", "Y", new FakeLanguage("en")), is("X"));
    assertThat(startUpper.convertCase("xxx", "Yyy", new FakeLanguage("en")), is("Xxx"));
    // special case for Dutch:
    assertThat(startUpper.convertCase("ijsselmeer", "Uppercase", new FakeLanguage("nl")), is("IJsselmeer"));
    assertThat(startUpper.convertCase("ijsselmeer", "lowercase", new FakeLanguage("nl")), is("IJsselmeer"));
    assertThat(startUpper.convertCase("ij", "Uppercase", new FakeLanguage("nl")), is("IJ"));

    MatchState preserve = getMatchState(Match.CaseConversion.PRESERVE);
    assertThat(preserve.convertCase("xxx", "Yyy", new FakeLanguage("en")), is("Xxx"));
    assertThat(preserve.convertCase("xxx", "yyy", new FakeLanguage("en")), is("xxx"));
    assertThat(preserve.convertCase("xxx", "YYY", new FakeLanguage("en")), is("XXX"));
    // special case for Dutch:
    assertThat(preserve.convertCase("ijsselmeer", "Uppercase", new FakeLanguage("nl")), is("IJsselmeer"));
    assertThat(preserve.convertCase("ijsselmeer", "lowercase", new FakeLanguage("nl")), is("ijsselmeer"));
    assertThat(preserve.convertCase("ijsselmeer", "ALLUPPER", new FakeLanguage("nl")), is("IJSSELMEER"));

    MatchState startLower = getMatchState(Match.CaseConversion.STARTLOWER);
    assertThat(startLower.convertCase("xxx", "YYY", new FakeLanguage("en")), is("xxx"));
    assertThat(startLower.convertCase("xxx", "yyy", new FakeLanguage("en")), is("xxx"));
    assertThat(startLower.convertCase("xxx", "Yyy", new FakeLanguage("en")), is("xxx"));
    assertThat(startLower.convertCase("XXX", "Yyy", new FakeLanguage("en")), is("xXX"));
    assertThat(startLower.convertCase("Xxx", "Yyy", new FakeLanguage("en")), is("xxx"));
  }

  private MatchState getMatchState(Match.CaseConversion conversion) {
    return new MatchState(new Match("", "", false, "", "", conversion, false, false, Match.IncludeRange.NONE), null);
  }

}
