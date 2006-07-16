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
    assertEquals("Haus[Nomen/Akk/Sin/Neu, Nomen/Dat/Sin/Neu, Nomen/Nom/Sin/Neu]", aToken.toString());
    aToken = tagger.lookup("Hauses");
    assertEquals("Hauses[Nomen/Gen/Sin/Neu]", aToken.toString());
    aToken = tagger.lookup("hauses");
    assertNull(aToken);
    aToken = tagger.lookup("Groß");
    assertNull(aToken);
    aToken = tagger.lookup("großer");
    //assertEquals(6, aToken.getReadingslength());
    assertEquals("großer[Adjektiv/Dat/Sin/Fem, Adjektiv/Gen/Plu/Fem, " +
        "Adjektiv/Gen/Plu/Mas, Adjektiv/Gen/Plu/Neu, " +
        "Adjektiv/Gen/Sin/Fem, Adjektiv/Nom/Sin/Mas]", aToken.toString());
  }
  
}
