/* LanguageTool, a natural language style checker 
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool;

import org.junit.Test;
import org.languagetool.language.Catalan;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class JLanguageToolTest {

  @Test
  public void testCleanOverlappingErrors() throws IOException {
    Language lang = new Catalan();
    JLanguageTool tool = new JLanguageTool(lang);
    List<RuleMatch> matches = tool.check("prosper");
    assertEquals(1, matches.size());
    assertEquals("CA_SIMPLE_REPLACE_BALEARIC", matches.get(0).getRule().getId());

    matches = tool.check("Potser siga el millor");
    assertEquals(1, matches.size());
    assertEquals("POTSER_SIGUI", matches.get(0).getRule().getId());

  }

  @Test
  public void testAdvancedTypography() throws IOException {
    Language lang = new Catalan();
    assertEquals(lang.toAdvancedTypography("És l'\"hora\"!"), "És l’«hora»!");
    assertEquals(lang.toAdvancedTypography("És l''hora'!"), "És l’‘hora’!");
    assertEquals(lang.toAdvancedTypography("És l'«hora»!"), "És l’«hora»!");
    assertEquals(lang.toAdvancedTypography("És l''hora'."), "És l’‘hora’.");
    assertEquals(lang.toAdvancedTypography("Cal evitar el \"'lo' neutre\"."), "Cal evitar el «‘lo’ neutre».");
    assertEquals(lang.toAdvancedTypography("És \"molt 'important'\"."), "És «molt ‘important’».");
    assertEquals(lang.toAdvancedTypography("Si és del v. 'haver'."), "Si és del v.\u00a0‘haver’.");
    assertEquals(lang.toAdvancedTypography("Amb el so de 's'."), "Amb el so de ‘s’.");

    assertEquals(lang.adaptSuggestion("L'IEC"), "L'IEC");
    assertEquals(lang.adaptSuggestion("te estimava"), "t'estimava");
    assertEquals(lang.adaptSuggestion("el Albert"), "l'Albert");
    assertEquals(lang.adaptSuggestion("l'Albert"), "l'Albert");
    assertEquals(lang.adaptSuggestion("l'«Albert»"), "l'«Albert»");
    assertEquals(lang.adaptSuggestion("l’«Albert»"), "l’«Albert»");
    assertEquals(lang.adaptSuggestion("l'\"Albert\""), "l'\"Albert\"");
    assertEquals(lang.adaptSuggestion("m'tancava"), "em tancava");
    assertEquals(lang.adaptSuggestion("s'tancava"), "es tancava");
    assertEquals(lang.adaptSuggestion("l'R+D"), "l'R+D");
    assertEquals(lang.adaptSuggestion("l'FBI"), "l'FBI");

  }

}

