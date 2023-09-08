/* LanguageTool, a natural language style checker
 * Copyright (C) 2023 Jaume Ortolà
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

package org.languagetool.tools;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

public class DiffsAsMatchesTest {

  @Test
  public void testDiffsAsMatches() throws IOException {
    DiffsAsMatches diffsAsMatches = new DiffsAsMatches();

    String original = "This are a sentence with too mistakes.";
    String revised = "This is a sentence with two mistakes.";
    List<PseudoMatch> matches = diffsAsMatches.getPseudoMatches(original, revised);
    assertEquals(matches.size(), 2);
    assertEquals(matches.get(0).getReplacements().toString(), "[is]");
    assertEquals(matches.get(0).getFromPos(), 5);
    assertEquals(matches.get(0).getToPos(), 8);
    assertEquals(matches.get(1).getReplacements().toString(), "[two]");
    assertEquals(matches.get(1).getFromPos(), 25);
    assertEquals(matches.get(1).getToPos(), 28);

    original = "I am going to er remove one word.";
    revised = "I am going to remove one word.";
    matches = diffsAsMatches.getPseudoMatches(original, revised);
    assertEquals(1, matches.size());
    assertEquals("[]", matches.get(0).getReplacements().toString());
    assertEquals(14, matches.get(0).getFromPos());
    assertEquals(17, matches.get(0).getToPos());

    original = "And I am going to remove one word.";
    revised = "I am going to remove one word.";
    matches = diffsAsMatches.getPseudoMatches(original, revised);
    assertEquals(1, matches.size());
    assertEquals("[]", matches.get(0).getReplacements().toString());
    assertEquals(0, matches.get(0).getFromPos());
    assertEquals(4, matches.get(0).getToPos());

    original = "I am going to add word.";
    revised = "I am going to add one word.";
    matches = diffsAsMatches.getPseudoMatches(original, revised);
    assertEquals(1, matches.size());
    assertEquals("[add one]", matches.get(0).getReplacements().toString());
    assertEquals(14, matches.get(0).getFromPos());
    assertEquals(17, matches.get(0).getToPos());

    original = "a word at the start.";
    revised = "Add a word at the start.";
    matches = diffsAsMatches.getPseudoMatches(original, revised);
    assertEquals(1, matches.size());
    assertEquals("[Add a]", matches.get(0).getReplacements().toString());
    assertEquals(0, matches.get(0).getFromPos());
    assertEquals(1, matches.get(0).getToPos());

    original = "Add word at position 1.";
    revised = "Add a word at position 1.";
    matches = diffsAsMatches.getPseudoMatches(original, revised);
    assertEquals(1, matches.size());
    assertEquals("[Add a]", matches.get(0).getReplacements().toString());
    assertEquals(0, matches.get(0).getFromPos());
    assertEquals(3, matches.get(0).getToPos());
    
    original = "Esta serealiza cada semana.";
    revised = "Esta se realiza cada semana.";
    matches = diffsAsMatches.getPseudoMatches(original, revised);
    assertEquals(1, matches.size());
    assertEquals("[se realiza]", matches.get(0).getReplacements().toString());
    assertEquals(5, matches.get(0).getFromPos());
    assertEquals(14, matches.get(0).getToPos());
    
    original = "Una cosa,una altra.";
    revised = "Una cosa, una altra.";
    matches = diffsAsMatches.getPseudoMatches(original, revised);
    assertEquals(1, matches.size());
    assertEquals("[cosa, ]", matches.get(0).getReplacements().toString());
    assertEquals(4, matches.get(0).getFromPos());
    assertEquals(9, matches.get(0).getToPos());
    
    original = "Que el año nuevo empezó.";
    revised = "El año nuevo empezó.";
    matches = diffsAsMatches.getPseudoMatches(original, revised);
    assertEquals(1, matches.size());
    assertEquals("[El]", matches.get(0).getReplacements().toString());
    assertEquals(0, matches.get(0).getFromPos());
    assertEquals(6, matches.get(0).getToPos());
    
    original = "¡Holà ! Estamos aquí.";
    revised = "¡Hola! Estamos aquí.";
    matches = diffsAsMatches.getPseudoMatches(original, revised);
    assertEquals(1, matches.size());
    assertEquals("[¡Hola!]", matches.get(0).getReplacements().toString());
    assertEquals(0, matches.get(0).getFromPos());
    assertEquals(7, matches.get(0).getToPos());
    
    original = "I truely reserve.";
    revised = "I truly deserve.";
    matches = diffsAsMatches.getPseudoMatches(original, revised);
    assertEquals(2, matches.size());
    assertEquals("[truly]", matches.get(0).getReplacements().toString());
    assertEquals(2, matches.get(0).getFromPos());
    assertEquals(8, matches.get(0).getToPos());
    assertEquals("[deserve]", matches.get(1).getReplacements().toString());
    assertEquals(9, matches.get(1).getFromPos());
    assertEquals(16, matches.get(1).getToPos());
    
    original = "}Describes how plants are important";
    revised = "It describes how plants are important";
    matches = diffsAsMatches.getPseudoMatches(original, revised);
    assertEquals(1, matches.size());
    assertEquals("[It describes]", matches.get(0).getReplacements().toString());
    assertEquals(0, matches.get(0).getFromPos());
    assertEquals(10, matches.get(0).getToPos());
    
  }

}
