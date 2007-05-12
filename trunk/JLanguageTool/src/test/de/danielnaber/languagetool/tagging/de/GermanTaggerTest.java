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
package de.danielnaber.languagetool.tagging.de;

import java.io.IOException;

import junit.framework.TestCase;

/**
 * @author Daniel Naber
 */
public class GermanTaggerTest extends TestCase {

  public void testTagger() throws IOException {
    GermanTagger tagger = new GermanTagger();
    AnalyzedGermanTokenReadings aToken = tagger.lookup("Haus");
    assertEquals("Haus[SUB:AKK:SIN:NEU, SUB:DAT:SIN:NEU, SUB:NOM:SIN:NEU]", aToken.toString());
    aToken = tagger.lookup("Hauses");
    assertEquals("Hauses[SUB:GEN:SIN:NEU]", aToken.toString());
    aToken = tagger.lookup("hauses");
    assertNull(aToken);
    aToken = tagger.lookup("Groß");
    assertNull(aToken);
    aToken = tagger.lookup("großer");
    assertEquals("großer[ADJ:DAT:SIN:FEM:GRU:SOL, ADJ:GEN:PLU:FEM:GRU:SOL, " +
        "ADJ:GEN:PLU:MAS:GRU:SOL, ADJ:GEN:PLU:NEU:GRU:SOL, " +
        "ADJ:GEN:SIN:FEM:GRU:SOL, ADJ:NOM:SIN:MAS:GRU:IND, ADJ:NOM:SIN:MAS:GRU:SOL]", aToken.toString());
    // from both german.dict and added.txt:
    aToken = tagger.lookup("Interessen");
    assertEquals("Interessen[SUB:DAT:PLU:NEU, SUB:NOM:PLU:NEU, SUB:AKK:PLU:NEU, SUB:GEN:PLU:NEU]",
        aToken.toString());
    //
    aToken = tagger.lookup("Donaudampfschiff");
    assertEquals("Donaudampfschiff[SUB:AKK:SIN:NEU, SUB:DAT:SIN:NEU, SUB:NOM:SIN:NEU]",
        aToken.toString());
    aToken = tagger.lookup("Häuserkämpfe");
    assertEquals("Häuserkämpfe[SUB:AKK:PLU:MAS, SUB:GEN:PLU:MAS, SUB:NOM:PLU:MAS]",
        aToken.toString());
  }
  
}
