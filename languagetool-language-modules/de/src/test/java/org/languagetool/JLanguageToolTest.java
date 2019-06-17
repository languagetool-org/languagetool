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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.languagetool.language.German;
import org.languagetool.language.GermanyGerman;
import org.languagetool.rules.RuleMatch;

public class JLanguageToolTest {

  @Test
  public void testGerman() throws IOException {
    JLanguageTool tool = new JLanguageTool(new German());
    assertEquals(0, tool.check("Ein Test, der keine Fehler geben sollte.").size());
    assertEquals(1, tool.check("Ein Test Test, der Fehler geben sollte.").size());
    tool.setListUnknownWords(true);
    // no spelling mistakes as we have not created a variant:
    assertEquals(0, tool.check("I can give you more a detailed description").size());
    //test unknown words listing
    assertEquals("[I, can, description, detailed, give, more, you]", tool.getUnknownWords().toString());    
  }

  @Test
  public void testGermanyGerman() throws IOException {
    JLanguageTool tool = new JLanguageTool(new GermanyGerman());
    assertEquals(0, tool.check("Ein Test, der keine Fehler geben sollte.").size());
    assertEquals(1, tool.check("Ein Test Test, der Fehler geben sollte.").size());
    tool.setListUnknownWords(true);
    // German rule has no effect with English error, but they are spelling mistakes:
    assertEquals(6, tool.check("I can give you more a detailed description").size());
    //test unknown words listing
    assertEquals("[I, can, description, detailed, give, more, you]", tool.getUnknownWords().toString());
  }

  @Test
  public void testPositionsWithGerman() throws IOException {
    JLanguageTool tool = new JLanguageTool(new GermanyGerman());
    List<RuleMatch> matches = tool.check("Stundenkilometer");
    assertEquals(1, matches.size());
    RuleMatch match = matches.get(0);
    assertEquals(0, match.getLine());
    assertEquals(1, match.getColumn());
  }
  
  @Test
  public void testCleanOverlappingWithGerman() throws IOException {
    JLanguageTool tool = new JLanguageTool(new GermanyGerman());
    // Juxtaposed errors in "TRGS - Technische" should not be removed.
    List<RuleMatch> matches = tool.check("TRGS - Technische Regeln für Gefahrstoffe");
    assertEquals(3, matches.size());
  }
  
  

}
