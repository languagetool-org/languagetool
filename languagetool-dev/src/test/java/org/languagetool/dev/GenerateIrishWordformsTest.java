/* LanguageTool, a natural language style checker
 * Copyright (C) 2020 Jim O'Regan
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
package org.languagetool.dev;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;


public class GenerateIrishWordformsTest {
  private static final Map<String, String[]> nounGuesses = new HashMap<>();
  static {
    nounGuesses.put("óir", new String[]{"m", "óir", "óra", "óirí", "óirí"});
    nounGuesses.put("eoir", new String[]{"m", "eoir", "eora", "eoirí", "eoirí"});
    nounGuesses.put("eálaí", new String[]{"m", "eálaí", "eálaí", "eálaithe", "eálaithe"});
  }

  @Test
  public void testGetEndingsRegex() {
    assertEquals("(.+)(eálaí|eoir|óir)$", GenerateIrishWordforms.getEndingsRegex(nounGuesses));
  }

  @Test
  public void testGuessIrishFSTNounClassSimple() {
    assertEquals("Nm3-1", GenerateIrishWordforms.guessIrishFSTNounClassSimple("blagadóir"));
  }

  @Test
  public void testExtractEnWiktionaryNounTemplate() {
    String a = "bádóirí, type: {{ga-decl-m3|b|ádóir|ádóra|ádóirí}}";
    Map<String, String> aMap = GenerateIrishWordforms.extractEnWiktionaryNounTemplate(a);
    assertEquals("b", aMap.get("stem"));
    assertEquals("ádóirí", aMap.get("pl.gen"));
  }
}
