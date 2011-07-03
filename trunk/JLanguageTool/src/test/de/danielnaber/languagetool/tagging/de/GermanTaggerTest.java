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
import java.util.List;

import de.danielnaber.languagetool.JLanguageTool;

import junit.framework.TestCase;
import morfologik.stemming.Dictionary;
import morfologik.stemming.DictionaryLookup;
import morfologik.stemming.WordData;

/**
 * @author Daniel Naber
 */
public class GermanTaggerTest extends TestCase {

  public void testTagger() throws IOException {
    GermanTagger tagger = new GermanTagger();
    
    AnalyzedGermanTokenReadings aToken = tagger.lookup("Haus");
    assertEquals("Haus[SUB:AKK:SIN:NEU, SUB:DAT:SIN:NEU, SUB:NOM:SIN:NEU]", aToken.toSortedString());
    assertEquals("Haus", aToken.getReadings().get(0).getLemma());
    assertEquals("Haus", aToken.getReadings().get(1).getLemma());
    assertEquals("Haus", aToken.getReadings().get(2).getLemma());
    
    aToken = tagger.lookup("Hauses");
    assertEquals("Hauses[SUB:GEN:SIN:NEU]", aToken.toSortedString());
    assertEquals("Haus", aToken.getReadings().get(0).getLemma());
    
    aToken = tagger.lookup("hauses");
    assertNull(aToken);
    
    aToken = tagger.lookup("Groß");
    assertNull(aToken);
    
    aToken = tagger.lookup("großer");
    assertEquals("großer[ADJ:DAT:SIN:FEM:GRU:SOL, ADJ:GEN:PLU:FEM:GRU:SOL, " +
        "ADJ:GEN:PLU:MAS:GRU:SOL, ADJ:GEN:PLU:NEU:GRU:SOL, " +
        "ADJ:GEN:SIN:FEM:GRU:SOL, ADJ:NOM:SIN:MAS:GRU:IND, ADJ:NOM:SIN:MAS:GRU:SOL]", aToken.toSortedString());
    assertEquals("groß", aToken.getReadings().get(0).getLemma());
    
    // from both german.dict and added.txt:
    aToken = tagger.lookup("Interessen");
    assertEquals("Interessen[SUB:AKK:PLU:NEU, SUB:DAT:PLU:NEU, SUB:GEN:PLU:NEU, SUB:NOM:PLU:NEU]",
        aToken.toSortedString());
    assertEquals("Interesse", aToken.getReadings().get(0).getLemma());
    assertEquals("Interesse", aToken.getReadings().get(1).getLemma());
    assertEquals("Interesse", aToken.getReadings().get(2).getLemma());
    assertEquals("Interesse", aToken.getReadings().get(3).getLemma());
    
    // words that are not in the dictionary but that are recognized thanks to noun splitting:
    aToken = tagger.lookup("Donaudampfschiff");
    assertEquals("Donaudampfschiff[SUB:AKK:SIN:NEU, SUB:DAT:SIN:NEU, SUB:NOM:SIN:NEU]",
        aToken.toSortedString());
    assertEquals("Donaudampfschiff", aToken.getReadings().get(0).getLemma());
    assertEquals("Donaudampfschiff", aToken.getReadings().get(1).getLemma());
    
    aToken = tagger.lookup("Häuserkämpfe");
    assertEquals("Häuserkämpfe[SUB:AKK:PLU:MAS, SUB:GEN:PLU:MAS, SUB:NOM:PLU:MAS]",
        aToken.toSortedString());
    assertEquals("Häuserkampf", aToken.getReadings().get(0).getLemma());
    assertEquals("Häuserkampf", aToken.getReadings().get(1).getLemma());
    assertEquals("Häuserkampf", aToken.getReadings().get(2).getLemma());
    
    aToken = tagger.lookup("Häuserkampfes");
    assertEquals("Häuserkampfes[SUB:GEN:SIN:MAS]", aToken.toSortedString());
    assertEquals("Häuserkampf", aToken.getReadings().get(0).getLemma());
    
    aToken = tagger.lookup("Häuserkampfs");
    assertEquals("Häuserkampfs[SUB:GEN:SIN:MAS]", aToken.toSortedString());
    assertEquals("Häuserkampf", aToken.getReadings().get(0).getLemma());

    aToken = tagger.lookup("Lieblingsfarben");
    assertEquals("Lieblingsfarben[SUB:AKK:PLU:FEM, SUB:DAT:PLU:FEM, SUB:GEN:PLU:FEM, " +
    		"SUB:NOM:PLU:FEM]", aToken.toSortedString());
    assertEquals("Lieblingsfarbe", aToken.getReadings().get(0).getLemma());

    aToken = tagger.lookup("Autolieblingsfarben");
    assertEquals("Autolieblingsfarben[SUB:AKK:PLU:FEM, SUB:DAT:PLU:FEM, SUB:GEN:PLU:FEM, " +
            "SUB:NOM:PLU:FEM]", aToken.toSortedString());
    assertEquals("Autolieblingsfarbe", aToken.getReadings().get(0).getLemma());

    aToken = tagger.lookup("übrigbleibst");
    assertEquals("übrigbleibst[VER:2:SIN:PRÄ:NON:NEB]", aToken.toSortedString());
    assertEquals("übrigbleiben", aToken.getReadings().get(0).getLemma());
  }

  // make sure we use the version of the POS data that was extended with post spelling reform data
  public void testExtendedTagger() throws IOException {
    GermanTagger tagger = new GermanTagger();

    assertEquals("Kuß[SUB:AKK:SIN:MAS, SUB:DAT:SIN:MAS, SUB:NOM:SIN:MAS]", tagger.lookup("Kuß").toSortedString());
    assertEquals("Kuss[SUB:AKK:SIN:MAS, SUB:DAT:SIN:MAS, SUB:NOM:SIN:MAS]", tagger.lookup("Kuss").toSortedString());

    assertEquals("Haß[SUB:AKK:SIN:MAS, SUB:DAT:SIN:MAS, SUB:NOM:SIN:MAS]", tagger.lookup("Haß").toSortedString());
    assertEquals("Hass[SUB:AKK:SIN:MAS, SUB:DAT:SIN:MAS, SUB:NOM:SIN:MAS]", tagger.lookup("Hass").toSortedString());

    assertEquals("muß[VER:MOD:1:SIN:PRÄ, VER:MOD:3:SIN:PRÄ]", tagger.lookup("muß").toSortedString());
    assertEquals("muss[VER:MOD:1:SIN:PRÄ, VER:MOD:3:SIN:PRÄ]", tagger.lookup("muss").toSortedString());
  }

  public void testTaggerBaseforms() throws IOException {
    GermanTagger tagger = new GermanTagger();
    
    List<AnalyzedGermanToken> readings = tagger.lookup("übrigbleibst").getGermanReadings();
    assertEquals(1, readings.size());
    assertEquals("übrigbleiben", readings.get(0).getLemma());

    readings = tagger.lookup("Haus").getGermanReadings();
    assertEquals(3, readings.size());
    assertEquals("Haus", readings.get(0).getLemma());
    assertEquals("Haus", readings.get(1).getLemma());
    assertEquals("Haus", readings.get(2).getLemma());

    readings = tagger.lookup("Häuser").getGermanReadings();
    assertEquals(3, readings.size());
    assertEquals("Haus", readings.get(0).getLemma());
    assertEquals("Haus", readings.get(1).getLemma());
    assertEquals("Haus", readings.get(2).getLemma());
  }
  
  public void testDictionary() throws IOException {    
    final Dictionary dictionary = Dictionary.read(
        JLanguageTool.getDataBroker().getFromResourceDirAsUrl("/de/german.dict"));    
    final DictionaryLookup dl = new DictionaryLookup(dictionary);
    for (WordData wd : dl) {
      if (wd.getTag() == null || wd.getTag().length() == 0) {
        System.err.println("**** Warning: the word " + wd.getWord() + "/" + wd.getStem()
                + " lacks a POS tag in the dictionary.");
      }
    }    
  }
  
}
