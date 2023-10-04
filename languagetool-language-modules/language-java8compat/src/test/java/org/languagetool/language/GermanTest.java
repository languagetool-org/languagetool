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
package org.languagetool.language;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Premium;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class GermanTest {

  @Test
  public void testGermanyGerman() throws IOException {
    JLanguageTool lt = new JLanguageTool(new GermanyGerman());
    assertEquals(0, lt.check("Ein Test, der keine Fehler geben sollte.").size());
    assertEquals(1, lt.check("Ein Test Test, der Fehler geben sollte.").size());
    lt.setListUnknownWords(true);
    // German rule has no effect with English error, but they are spelling mistakes:
    if (Premium.isPremiumVersion()) {
      assertEquals(7, lt.check("I can give you more a detailed description").size());
    } else {
      assertEquals(6, lt.check("I can give you more a detailed description").size());
    }
    //test unknown words listing
    assertEquals("[I, can, description, detailed, give, more, you]", lt.getUnknownWords().toString());
  }

  @Test
  public void testPositionsWithGerman() throws IOException {
    JLanguageTool lt = new JLanguageTool(new GermanyGerman());
    List<RuleMatch> matches = lt.check("Stundenkilometer");
    assertEquals(1, matches.size());
    RuleMatch match = matches.get(0);
    assertEquals(0, match.getLine());
    assertEquals(1, match.getColumn());
  }

  @Test
  public void testGermanVariants() throws IOException {
    String sentence = "Ein Test, der keine Fehler geben sollte.";
    String sentence2 = "Ein Test Test, der Fehler geben sollte.";
    JLanguageTool lt = new JLanguageTool(new GermanyGerman());
    assertEquals(0, lt.check(sentence).size());
    assertEquals(1, lt.check(sentence2).size());

    lt = new JLanguageTool(new SwissGerman());
    assertEquals(0, lt.check(sentence).size());
    assertEquals(1, lt.check(sentence2).size());

    lt = new JLanguageTool(new AustrianGerman());
    assertEquals(0, lt.check(sentence).size());
    assertEquals(1, lt.check(sentence2).size());
  }
}
