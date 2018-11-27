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
package org.languagetool.tagging.de;

import morfologik.stemming.Dictionary;
import morfologik.stemming.DictionaryLookup;
import morfologik.stemming.WordData;
import org.junit.Test;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;

@SuppressWarnings("ConstantConditions")
public class GermanTaggerTest {

  @Test
  public void testLemmaOfForDashCompounds() throws IOException {
    GermanTagger tagger = new GermanTagger();
    AnalyzedTokenReadings aToken = tagger.lookup("Zahn-Arzt-Verband");
    List<String> lemmas = new ArrayList<>();
    for (AnalyzedToken analyzedToken : aToken) {
      lemmas.add(analyzedToken.getLemma());
    }
    assertTrue(lemmas.contains("Zahnarztverband"));
  }
  
  @Test
  public void testTagger() throws IOException {
    GermanTagger tagger = new GermanTagger();

    AnalyzedTokenReadings aToken = tagger.lookup("Haus");
    assertEquals("Haus[Haus/SUB:AKK:SIN:NEU, Haus/SUB:DAT:SIN:NEU, Haus/SUB:NOM:SIN:NEU]", toSortedString(aToken));
    assertEquals("Haus", aToken.getReadings().get(0).getLemma());
    assertEquals("Haus", aToken.getReadings().get(1).getLemma());
    assertEquals("Haus", aToken.getReadings().get(2).getLemma());

    AnalyzedTokenReadings aToken2 = tagger.lookup("Hauses");
    assertEquals("Hauses[Haus/SUB:GEN:SIN:NEU]", toSortedString(aToken2));
    assertEquals("Haus", aToken2.getReadings().get(0).getLemma());

    assertNull(tagger.lookup("hauses"));
    assertNull(tagger.lookup("Groß"));

    assertEquals("Lieblingsbuchstabe[Lieblingsbuchstabe/SUB:NOM:SIN:MAS]", toSortedString(tagger.lookup("Lieblingsbuchstabe")));

    AnalyzedTokenReadings aToken3 = tagger.lookup("großer");
    assertEquals("großer[groß/ADJ:DAT:SIN:FEM:GRU:SOL, groß/ADJ:GEN:PLU:FEM:GRU:SOL, groß/ADJ:GEN:PLU:MAS:GRU:SOL, " +
            "groß/ADJ:GEN:PLU:NEU:GRU:SOL, groß/ADJ:GEN:SIN:FEM:GRU:SOL, groß/ADJ:NOM:SIN:MAS:GRU:IND, " +
            "groß/ADJ:NOM:SIN:MAS:GRU:SOL]", toSortedString(tagger.lookup("großer")));
    assertEquals("groß", aToken3.getReadings().get(0).getLemma());

    // checks for github issue #635: Some German verbs on the beginning of a sentences are identified only as substantive
    assertTrue(tagger.tag(Collections.singletonList("Haben"), true).toString().contains("VER"));
    assertTrue(tagger.tag(Collections.singletonList("Können"), true).toString().contains("VER"));
    assertTrue(tagger.tag(Collections.singletonList("Gerade"), true).toString().contains("ADJ"));

    // from both german.dict and added.txt:
    AnalyzedTokenReadings aToken4 = tagger.lookup("Interessen");
    assertEquals("Interessen[Interesse/SUB:AKK:PLU:NEU, Interesse/SUB:DAT:PLU:NEU, " +
                    "Interesse/SUB:GEN:PLU:NEU, Interesse/SUB:NOM:PLU:NEU]",
            toSortedString(aToken4));
    assertEquals("Interesse", aToken4.getReadings().get(0).getLemma());
    assertEquals("Interesse", aToken4.getReadings().get(1).getLemma());
    assertEquals("Interesse", aToken4.getReadings().get(2).getLemma());
    assertEquals("Interesse", aToken4.getReadings().get(3).getLemma());

    // words that are not in the dictionary but that are recognized thanks to noun splitting:
    AnalyzedTokenReadings aToken5 = tagger.lookup("Donaudampfschiff");
    assertEquals("Donaudampfschiff[Donaudampfschiff/SUB:AKK:SIN:NEU, Donaudampfschiff/SUB:DAT:SIN:NEU, " +
            "Donaudampfschiff/SUB:NOM:SIN:NEU]", toSortedString(aToken5));
    assertEquals("Donaudampfschiff", aToken5.getReadings().get(0).getLemma());
    assertEquals("Donaudampfschiff", aToken5.getReadings().get(1).getLemma());

    AnalyzedTokenReadings aToken6 = tagger.lookup("Häuserkämpfe");
    assertEquals("Häuserkämpfe[Häuserkampf/SUB:AKK:PLU:MAS, Häuserkampf/SUB:GEN:PLU:MAS, Häuserkampf/SUB:NOM:PLU:MAS]",
            toSortedString(aToken6));
    assertEquals("Häuserkampf", aToken6.getReadings().get(0).getLemma());
    assertEquals("Häuserkampf", aToken6.getReadings().get(1).getLemma());
    assertEquals("Häuserkampf", aToken6.getReadings().get(2).getLemma());

    AnalyzedTokenReadings aToken7 = tagger.lookup("Häuserkampfes");
    assertEquals("Häuserkampfes[Häuserkampf/SUB:GEN:SIN:MAS]", toSortedString(aToken7));
    assertEquals("Häuserkampf", aToken7.getReadings().get(0).getLemma());

    AnalyzedTokenReadings aToken8 = tagger.lookup("Häuserkampfs");
    assertEquals("Häuserkampfs[Häuserkampf/SUB:GEN:SIN:MAS]", toSortedString(aToken8));
    assertEquals("Häuserkampf", aToken8.getReadings().get(0).getLemma());

    AnalyzedTokenReadings aToken9 = tagger.lookup("Lieblingsfarben");
    assertEquals("Lieblingsfarben[Lieblingsfarbe/SUB:AKK:PLU:FEM, Lieblingsfarbe/SUB:DAT:PLU:FEM, " +
            "Lieblingsfarbe/SUB:GEN:PLU:FEM, Lieblingsfarbe/SUB:NOM:PLU:FEM]", toSortedString(aToken9));
    assertEquals("Lieblingsfarbe", aToken9.getReadings().get(0).getLemma());

    AnalyzedTokenReadings aToken10 = tagger.lookup("Autolieblingsfarben");
    assertEquals("Autolieblingsfarben[Autolieblingsfarbe/SUB:AKK:PLU:FEM, Autolieblingsfarbe/SUB:DAT:PLU:FEM, " +
            "Autolieblingsfarbe/SUB:GEN:PLU:FEM, Autolieblingsfarbe/SUB:NOM:PLU:FEM]", toSortedString(aToken10));
    assertEquals("Autolieblingsfarbe", aToken10.getReadings().get(0).getLemma());

    AnalyzedTokenReadings aToken11 = tagger.lookup("übrigbleibst");
    assertEquals("übrigbleibst[übrigbleiben/VER:2:SIN:PRÄ:NON:NEB]", toSortedString(aToken11));
    assertEquals("übrigbleiben", aToken11.getReadings().get(0).getLemma());

    AnalyzedTokenReadings aToken12 = tagger.lookup("IT-Dienstleistungsunternehmen");
    assertTrue(aToken12.getReadings().get(0).getPOSTag().matches("SUB.*"));
    assertEquals("IT-Dienstleistungsunternehmen", aToken12.getReadings().get(0).getLemma());

    AnalyzedTokenReadings aToken13 = tagger.lookup("Entweder-oder");
    assertTrue(aToken13.getReadings().get(0).getPOSTag().matches("SUB.*"));
    assertEquals("Entweder-oder", aToken13.getReadings().get(0).getLemma());

    AnalyzedTokenReadings aToken14 = tagger.lookup("Verletzter");
    assertTrue(aToken14.getReadings().get(0).getPOSTag().equals("SUB:NOM:SIN:MAS:ADJ"));
    assertEquals("Verletzter", aToken14.getReadings().get(0).getLemma());
    assertTrue(aToken14.getReadings().get(1).getPOSTag().equals("SUB:GEN:PLU:MAS:ADJ"));

    AnalyzedTokenReadings aToken15 = tagger.lookup("erzkatholisch");
    assertTrue(aToken15.getReadings().get(0).getPOSTag().equals("ADJ:PRD:GRU"));
  }

  // make sure we use the version of the POS data that was extended with post spelling reform data
  @Test
  public void testExtendedTagger() throws IOException {
    GermanTagger tagger = new GermanTagger();

    assertEquals("Kuß[Kuß/SUB:AKK:SIN:MAS, Kuß/SUB:DAT:SIN:MAS, Kuß/SUB:NOM:SIN:MAS]", toSortedString(tagger.lookup("Kuß")));
    assertEquals("Kuss[Kuss/SUB:AKK:SIN:MAS, Kuss/SUB:DAT:SIN:MAS, Kuss/SUB:NOM:SIN:MAS]", toSortedString(tagger.lookup("Kuss")));

    assertEquals("Haß[Haß/SUB:AKK:SIN:MAS, Haß/SUB:DAT:SIN:MAS, Haß/SUB:NOM:SIN:MAS]", toSortedString(tagger.lookup("Haß")));
    assertEquals("Hass[Hass/SUB:AKK:SIN:MAS, Hass/SUB:DAT:SIN:MAS, Hass/SUB:NOM:SIN:MAS]", toSortedString(tagger.lookup("Hass")));

    assertEquals("muß[müssen/VER:MOD:1:SIN:PRÄ, müssen/VER:MOD:3:SIN:PRÄ]", toSortedString(tagger.lookup("muß")));
    assertEquals("muss[müssen/VER:MOD:1:SIN:PRÄ, müssen/VER:MOD:3:SIN:PRÄ]", toSortedString(tagger.lookup("muss")));
  }

  @Test
  public void testTaggerBaseforms() throws IOException {
    GermanTagger tagger = new GermanTagger();

    List<AnalyzedToken> readings1 = tagger.lookup("übrigbleibst").getReadings();
    assertEquals(1, readings1.size());
    assertEquals("übrigbleiben", readings1.get(0).getLemma());

    List<AnalyzedToken> readings2 = tagger.lookup("Haus").getReadings();
    assertEquals(3, readings2.size());
    assertEquals("Haus", readings2.get(0).getLemma());
    assertEquals("Haus", readings2.get(1).getLemma());
    assertEquals("Haus", readings2.get(2).getLemma());

    List<AnalyzedToken> readings3 = tagger.lookup("Häuser").getReadings();
    assertEquals(3, readings3.size());
    assertEquals("Haus", readings3.get(0).getLemma());
    assertEquals("Haus", readings3.get(1).getLemma());
    assertEquals("Haus", readings3.get(2).getLemma());
  }

  @Test
  public void testTag() throws IOException {
    GermanTagger tagger = new GermanTagger();
    List<String> upperCaseWord = Arrays.asList("Das");
    List<AnalyzedTokenReadings> readings = tagger.tag(upperCaseWord, false);
    assertEquals("[Das[Das/null*]]", readings.toString());
    List<AnalyzedTokenReadings> readings2 = tagger.tag(upperCaseWord, true);
    assertTrue(readings2.toString().startsWith("[Das[der/ART:"));
  }

  @Test
  public void testTagWithManualDictExtension() throws IOException {
    // words not originally in Morphy but added in LT 1.8 (moved from added.txt to german.dict)
    GermanTagger tagger = new GermanTagger();
    List<AnalyzedTokenReadings> readings = tagger.tag(Collections.singletonList("Wichtigtuerinnen"));
    assertEquals("[Wichtigtuerinnen[Wichtigtuerin/SUB:AKK:PLU:FEM*," +
            "Wichtigtuerin/SUB:DAT:PLU:FEM*,Wichtigtuerin/SUB:GEN:PLU:FEM*,Wichtigtuerin/SUB:NOM:PLU:FEM*]]", readings.toString());
  }

  @Test
  public void testDictionary() throws IOException {
    Dictionary dictionary = Dictionary.read(
            JLanguageTool.getDataBroker().getFromResourceDirAsUrl("/de/german.dict"));
    DictionaryLookup dl = new DictionaryLookup(dictionary);
    for (WordData wd : dl) {
      if (wd.getTag() == null || wd.getTag().length() == 0) {
        System.err.println("**** Warning: the word " + wd.getWord() + "/" + wd.getStem()
                + " lacks a POS tag in the dictionary.");
      }
    }
  }

  /**
   * Returns a string representation like {@code toString()}, but sorts
   * the elements alphabetically.
   */
  public static String toSortedString(AnalyzedTokenReadings tokenReadings) {
    StringBuilder sb = new StringBuilder(tokenReadings.getToken());
    Set<String> elements = new TreeSet<>();
    sb.append('[');
    for (AnalyzedToken reading : tokenReadings) {
      if (!elements.contains(reading.toString())) {
        elements.add(reading.toString());
      }
    }
    sb.append(String.join(", ", elements));
    sb.append(']');
    return sb.toString();
  }
}
