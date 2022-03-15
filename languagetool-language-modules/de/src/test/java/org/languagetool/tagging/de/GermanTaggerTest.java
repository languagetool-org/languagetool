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
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;

import java.io.IOException;
import java.util.*;

import static org.hamcrest.CoreMatchers.is;

@SuppressWarnings("ConstantConditions")
public class GermanTaggerTest {

  private final GermanTagger tagger = new GermanTagger();

  @Test
  public void testLemmaOfForDashCompounds() throws IOException {
    AnalyzedTokenReadings aToken = tagger.lookup("Zahn-Arzt-Verband");
    List<String> lemmas = new ArrayList<>();
    for (AnalyzedToken analyzedToken : aToken) {
      lemmas.add(analyzedToken.getLemma());
    }
    Assertions.assertTrue(lemmas.contains("Zahnarztverband"));
  }
  
  @Test
  public void testGenderGap() throws IOException {
    // https://github.com/languagetool-org/languagetool/issues/2417
    Assertions.assertTrue(tagger.tag(Arrays.asList("viele", "Freund", "*", "innen")).get(1).hasPartialPosTag(":PLU:FEM"));
    Assertions.assertTrue(tagger.tag(Arrays.asList("viele", "Freund", "_", "innen")).get(1).hasPartialPosTag(":PLU:FEM"));
    Assertions.assertTrue(tagger.tag(Arrays.asList("viele", "Freund", ":", "innen")).get(1).hasPartialPosTag(":PLU:FEM"));
    Assertions.assertTrue(tagger.tag(Arrays.asList("viele", "Freund", "/", "innen")).get(1).hasPartialPosTag(":PLU:FEM"));
    Assertions.assertTrue(tagger.tag(Arrays.asList("jede", "*", "r", "Mitarbeiter", "*", "in")).get(0).hasPartialPosTag("PRO:IND:NOM:SIN:FEM"));
    Assertions.assertTrue(tagger.tag(Arrays.asList("jede", "*", "r", "Mitarbeiter", "*", "in")).get(0).hasPartialPosTag("PRO:IND:NOM:SIN:MAS"));
    Assertions.assertTrue(tagger.tag(Arrays.asList("jede", "*", "r", "Mitarbeiter", "*", "in")).get(3).hasPartialPosTag("SUB:NOM:SIN:FEM"));
    Assertions.assertTrue(tagger.tag(Arrays.asList("jede", "*", "r", "Mitarbeiter", "*", "in")).get(3).hasPartialPosTag("SUB:NOM:SIN:MAS"));
  }
  
  @Test
  public void testIgnoreDomain() throws IOException {
    List<AnalyzedTokenReadings> aToken = tagger.tag(Arrays.asList("bundestag", ".", "de"));
    Assertions.assertFalse(aToken.get(0).isTagged());
  }

  @Test
  public void testIgnoreImperative() throws IOException {
    List<AnalyzedTokenReadings> aToken = tagger.tag(Collections.singletonList("zehnfach"));
    Assertions.assertFalse(aToken.get(0).isTagged());
  }

  @Test
  public void testTagger() throws IOException {
    AnalyzedTokenReadings aToken = tagger.lookup("Haus");
    Assertions.assertEquals("Haus[Haus/SUB:AKK:SIN:NEU, Haus/SUB:DAT:SIN:NEU, Haus/SUB:NOM:SIN:NEU]", toSortedString(aToken));
    Assertions.assertEquals("Haus", aToken.getReadings().get(0).getLemma());
    Assertions.assertEquals("Haus", aToken.getReadings().get(1).getLemma());
    Assertions.assertEquals("Haus", aToken.getReadings().get(2).getLemma());

    AnalyzedTokenReadings aToken2 = tagger.lookup("Hauses");
    Assertions.assertEquals("Hauses[Haus/SUB:GEN:SIN:NEU]", toSortedString(aToken2));
    Assertions.assertEquals("Haus", aToken2.getReadings().get(0).getLemma());

    Assertions.assertNull(tagger.lookup("hauses"));
    Assertions.assertNull(tagger.lookup("Groß"));

    Assertions.assertEquals("Lieblingsbuchstabe[Lieblingsbuchstabe/SUB:NOM:SIN:MAS]", toSortedString(tagger.lookup("Lieblingsbuchstabe")));

    AnalyzedTokenReadings aToken3 = tagger.lookup("großer");
    Assertions.assertEquals("großer[groß/ADJ:DAT:SIN:FEM:GRU:SOL, groß/ADJ:GEN:PLU:FEM:GRU:SOL, groß/ADJ:GEN:PLU:MAS:GRU:SOL, " +
            "groß/ADJ:GEN:PLU:NEU:GRU:SOL, groß/ADJ:GEN:SIN:FEM:GRU:SOL, groß/ADJ:NOM:SIN:MAS:GRU:IND, " +
            "groß/ADJ:NOM:SIN:MAS:GRU:SOL]", toSortedString(tagger.lookup("großer")));
    Assertions.assertEquals("groß", aToken3.getReadings().get(0).getLemma());

    // checks for github issue #635: Some German verbs on the beginning of a sentences are identified only as substantive
    Assertions.assertTrue(tagger.tag(Collections.singletonList("Haben"), true).toString().contains("VER"));
    Assertions.assertTrue(tagger.tag(Collections.singletonList("Können"), true).toString().contains("VER"));
    Assertions.assertTrue(tagger.tag(Collections.singletonList("Gerade"), true).toString().contains("ADJ"));

    // from both german.dict and added.txt:
    AnalyzedTokenReadings aToken4 = tagger.lookup("Interessen");
    Assertions.assertEquals("Interessen[Interesse/SUB:AKK:PLU:NEU, Interesse/SUB:DAT:PLU:NEU, " +
                    "Interesse/SUB:GEN:PLU:NEU, Interesse/SUB:NOM:PLU:NEU]", toSortedString(aToken4));
    Assertions.assertEquals("Interesse", aToken4.getReadings().get(0).getLemma());
    Assertions.assertEquals("Interesse", aToken4.getReadings().get(1).getLemma());
    Assertions.assertEquals("Interesse", aToken4.getReadings().get(2).getLemma());
    Assertions.assertEquals("Interesse", aToken4.getReadings().get(3).getLemma());

    // words that are not in the dictionary but that are recognized thanks to noun splitting:
    AnalyzedTokenReadings aToken5 = tagger.lookup("Donaudampfschiff");
    Assertions.assertEquals("Donaudampfschiff[Donaudampfschiff/SUB:AKK:SIN:NEU, Donaudampfschiff/SUB:DAT:SIN:NEU, " +
            "Donaudampfschiff/SUB:NOM:SIN:NEU]", toSortedString(aToken5));
    Assertions.assertEquals("Donaudampfschiff", aToken5.getReadings().get(0).getLemma());
    Assertions.assertEquals("Donaudampfschiff", aToken5.getReadings().get(1).getLemma());

    AnalyzedTokenReadings aToken6 = tagger.lookup("Häuserkämpfe");
    Assertions.assertEquals("Häuserkämpfe[Häuserkampf/SUB:AKK:PLU:MAS, Häuserkampf/SUB:GEN:PLU:MAS, Häuserkampf/SUB:NOM:PLU:MAS]", toSortedString(aToken6));
    Assertions.assertEquals("Häuserkampf", aToken6.getReadings().get(0).getLemma());
    Assertions.assertEquals("Häuserkampf", aToken6.getReadings().get(1).getLemma());
    Assertions.assertEquals("Häuserkampf", aToken6.getReadings().get(2).getLemma());

    AnalyzedTokenReadings aToken7 = tagger.lookup("Häuserkampfes");
    Assertions.assertEquals("Häuserkampfes[Häuserkampf/SUB:GEN:SIN:MAS]", toSortedString(aToken7));
    Assertions.assertEquals("Häuserkampf", aToken7.getReadings().get(0).getLemma());

    AnalyzedTokenReadings aToken8 = tagger.lookup("Häuserkampfs");
    Assertions.assertEquals("Häuserkampfs[Häuserkampf/SUB:GEN:SIN:MAS]", toSortedString(aToken8));
    Assertions.assertEquals("Häuserkampf", aToken8.getReadings().get(0).getLemma());

    AnalyzedTokenReadings aToken9 = tagger.lookup("Lieblingsfarben");
    Assertions.assertEquals("Lieblingsfarben[Lieblingsfarbe/SUB:AKK:PLU:FEM, Lieblingsfarbe/SUB:DAT:PLU:FEM, " +
            "Lieblingsfarbe/SUB:GEN:PLU:FEM, Lieblingsfarbe/SUB:NOM:PLU:FEM]", toSortedString(aToken9));
    Assertions.assertEquals("Lieblingsfarbe", aToken9.getReadings().get(0).getLemma());

    AnalyzedTokenReadings aToken10 = tagger.lookup("Autolieblingsfarben");
    Assertions.assertEquals("Autolieblingsfarben[Autolieblingsfarbe/SUB:AKK:PLU:FEM, Autolieblingsfarbe/SUB:DAT:PLU:FEM, " +
            "Autolieblingsfarbe/SUB:GEN:PLU:FEM, Autolieblingsfarbe/SUB:NOM:PLU:FEM]", toSortedString(aToken10));
    Assertions.assertEquals("Autolieblingsfarbe", aToken10.getReadings().get(0).getLemma());

    AnalyzedTokenReadings aToken11 = tagger.lookup("übrigbleibst");
    Assertions.assertEquals("übrigbleibst[übrigbleiben/VER:2:SIN:PRÄ:NON:NEB]", toSortedString(aToken11));
    Assertions.assertEquals("übrigbleiben", aToken11.getReadings().get(0).getLemma());

    AnalyzedTokenReadings aToken12 = tagger.lookup("IT-Dienstleistungsunternehmen");
    Assertions.assertTrue(aToken12.getReadings().get(0).getPOSTag().matches("SUB.*"));
    Assertions.assertEquals("IT-Dienstleistungsunternehmen", aToken12.getReadings().get(0).getLemma());

    AnalyzedTokenReadings aToken13 = tagger.lookup("Entweder-oder");
    Assertions.assertTrue(aToken13.getReadings().get(0).getPOSTag().matches("SUB.*"));
    Assertions.assertEquals("Entweder-oder", aToken13.getReadings().get(0).getLemma());

    AnalyzedTokenReadings aToken14 = tagger.lookup("Verletzter");
    Assertions.assertEquals("SUB:NOM:SIN:MAS:ADJ", aToken14.getReadings().get(0).getPOSTag());
    Assertions.assertEquals("Verletzter", aToken14.getReadings().get(0).getLemma());
    Assertions.assertEquals("SUB:GEN:PLU:MAS:ADJ", aToken14.getReadings().get(1).getPOSTag());

    AnalyzedTokenReadings aToken15 = tagger.lookup("erzkatholisch");
    Assertions.assertEquals("ADJ:PRD:GRU", aToken15.getReadings().get(0).getPOSTag());

    AnalyzedTokenReadings aToken16 = tagger.lookup("unerbeten");
    Assertions.assertEquals("PA2:PRD:GRU:VER", aToken16.getReadings().get(0).getPOSTag());

    AnalyzedTokenReadings aToken17 = tagger.lookup("under");
    Assertions.assertNull(aToken17);
    
    // tag old forms
    AnalyzedTokenReadings aToken18 = tagger.lookup("Zuge");
    Assertions.assertEquals("Zuge[Zug/SUB:DAT:SIN:MAS]", toSortedString(aToken18));
    AnalyzedTokenReadings aToken19 = tagger.lookup("Tische");
    Assertions.assertEquals("Tische[Tisch/SUB:AKK:PLU:MAS, Tisch/SUB:DAT:SIN:MAS, Tisch/SUB:GEN:PLU:MAS, Tisch/SUB:NOM:PLU:MAS]", toSortedString(aToken19));

    Assertions.assertNull(tagger.lookup("vanillig-karamelligen"));
  }

  // make sure we use the version of the POS data that was extended with post spelling reform data
  @Test
  public void testExtendedTagger() throws IOException {
    Assertions.assertEquals("Kuß[Kuß/SUB:AKK:SIN:MAS, Kuß/SUB:DAT:SIN:MAS, Kuß/SUB:NOM:SIN:MAS]", toSortedString(tagger.lookup("Kuß")));
    Assertions.assertEquals("Kuss[Kuss/SUB:AKK:SIN:MAS, Kuss/SUB:DAT:SIN:MAS, Kuss/SUB:NOM:SIN:MAS]", toSortedString(tagger.lookup("Kuss")));

    Assertions.assertEquals("Haß[Haß/SUB:AKK:SIN:MAS, Haß/SUB:DAT:SIN:MAS, Haß/SUB:NOM:SIN:MAS]", toSortedString(tagger.lookup("Haß")));
    Assertions.assertEquals("Hass[Hass/SUB:AKK:SIN:MAS, Hass/SUB:DAT:SIN:MAS, Hass/SUB:NOM:SIN:MAS]", toSortedString(tagger.lookup("Hass")));
  }

  @Test
  public void testAfterColon() throws IOException {
    // a colon doesn't start a new sentence in LT, but often it should, so we check the special case for that
    List<AnalyzedTokenReadings> tags = tagger.tag(Arrays.asList("Er", "sagte", ":", "Als", "Erstes", "würde", "ich"));
    Assertions.assertEquals(7, tags.size());
    Assertions.assertEquals("Als", tags.get(3).getToken());
    Assertions.assertEquals(4, tags.get(3).getReadings().size());
  }

  @Test
  public void testTaggerBaseforms() throws IOException {
    List<AnalyzedToken> readings1 = tagger.lookup("übrigbleibst").getReadings();
    Assertions.assertEquals(1, readings1.size());
    Assertions.assertEquals("übrigbleiben", readings1.get(0).getLemma());

    List<AnalyzedToken> readings2 = tagger.lookup("Haus").getReadings();
    Assertions.assertEquals(3, readings2.size());
    Assertions.assertEquals("Haus", readings2.get(0).getLemma());
    Assertions.assertEquals("Haus", readings2.get(1).getLemma());
    Assertions.assertEquals("Haus", readings2.get(2).getLemma());

    List<AnalyzedToken> readings3 = tagger.lookup("Häuser").getReadings();
    Assertions.assertEquals(3, readings3.size());
    Assertions.assertEquals("Haus", readings3.get(0).getLemma());
    Assertions.assertEquals("Haus", readings3.get(1).getLemma());
    Assertions.assertEquals("Haus", readings3.get(2).getLemma());
  }

  @Test
  public void testTag() throws IOException {
    List<String> upperCaseWord = Collections.singletonList("Das");
    List<AnalyzedTokenReadings> readings = tagger.tag(upperCaseWord, false);
    Assertions.assertEquals("[Das[Das/null*]]", readings.toString());
    List<AnalyzedTokenReadings> readings2 = tagger.tag(upperCaseWord, true);
    Assertions.assertTrue(readings2.toString().startsWith("[Das[der/ART:"));
  }

  @Test
  public void testTagWithManualDictExtension() throws IOException {
    // words not originally in Morphy but added in LT 1.8 (moved from added.txt to german.dict)
    List<AnalyzedTokenReadings> readings = tagger.tag(Collections.singletonList("Wichtigtuerinnen"));
    Assertions.assertEquals("[Wichtigtuerinnen[Wichtigtuerin/SUB:AKK:PLU:FEM*," +
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

  @Test
  public void testIsWeiseException() {
    Assertions.assertFalse(tagger.isWeiseException("überweise"));
    Assertions.assertFalse(tagger.isWeiseException("verweise"));
    Assertions.assertFalse(tagger.isWeiseException("eimerweise"));
    Assertions.assertFalse(tagger.isWeiseException("meterweise"));
    Assertions.assertFalse(tagger.isWeiseException("literweise"));
    Assertions.assertFalse(tagger.isWeiseException("blätterweise"));
    Assertions.assertFalse(tagger.isWeiseException("erweise"));

    Assertions.assertTrue(tagger.isWeiseException("lustigerweise"));
    Assertions.assertTrue(tagger.isWeiseException("idealerweise"));
  }

  @Test
  public void testPrefixVerbsFromSpellingTxt() throws IOException {
    List<AnalyzedTokenReadings> result1 = tagger.tag(Collections.singletonList("herumgeben"));
    MatcherAssert.assertThat(result1.size(), is(1));
    MatcherAssert.assertThat(result1.get(0).getReadings().size(), is(5));
    String res1 = result1.toString();
    Assertions.assertTrue(res1.contains("herumgeben/VER:1:PLU:KJ1:NON*"));
    Assertions.assertTrue(res1.contains("herumgeben/VER:1:PLU:PRÄ:NON*"));
    Assertions.assertTrue(res1.contains("herumgeben/VER:3:PLU:KJ1:NON*"));
    Assertions.assertTrue(res1.contains("herumgeben/VER:3:PLU:PRÄ:NON*"));
    Assertions.assertTrue(res1.contains("herumgeben/VER:INF:NON*"));
    Assertions.assertFalse(res1.contains("ADJ:"));
    Assertions.assertFalse(res1.contains("PA1:"));
    Assertions.assertFalse(res1.contains("PA2:"));

    List<AnalyzedTokenReadings> result2 = tagger.tag(Collections.singletonList("herumgab"));
    MatcherAssert.assertThat(result2.size(), is(1));
    MatcherAssert.assertThat(result2.get(0).getReadings().size(), is(2));
    String res2 = result2.toString();
    Assertions.assertTrue(res2.contains("herumgeben/VER:1:SIN:PRT:NON*"));
    Assertions.assertTrue(res2.contains("herumgeben/VER:3:SIN:PRT:NON*"));
    Assertions.assertFalse(res2.contains("ADJ:"));

    List<AnalyzedTokenReadings> result3 = tagger.tag(Collections.singletonList("zurückgeschickt"));
    MatcherAssert.assertThat(result3.size(), is(1));
    MatcherAssert.assertThat(result3.get(0).getReadings().size(), is(2));
    String res3 = result3.toString();
    Assertions.assertTrue(res3.contains("zurückschicken/VER:PA2:SFT*"));
    Assertions.assertTrue(res3.contains("PA2:PRD:GRU:VER*"));
    Assertions.assertFalse(res3.contains("ADJ:"));

    List<AnalyzedTokenReadings> result4 = tagger.tag(Collections.singletonList("abzuschicken"));
    MatcherAssert.assertThat(result4.size(), is(1));
    MatcherAssert.assertThat(result4.get(0).getReadings().size(), is(5));
    String res4 = result4.toString();
    Assertions.assertTrue(res4.contains("abschicken/VER:1:PLU:KJ1:SFT*"));
    Assertions.assertTrue(res4.contains("abschicken/VER:1:PLU:PRÄ:SFT*"));
    Assertions.assertTrue(res4.contains("abschicken/VER:3:PLU:KJ1:SFT*"));
    Assertions.assertTrue(res4.contains("abschicken/VER:3:PLU:PRÄ:SFT*"));
    Assertions.assertFalse(res4.contains("ADJ:"));

    List<AnalyzedTokenReadings> result5 = tagger.tag(Collections.singletonList("Mitmanagen"));
    MatcherAssert.assertThat(result5.size(), is(1));
    MatcherAssert.assertThat(result5.get(0).getReadings().size(), is(3));
    String res5 = result5.toString();
    Assertions.assertTrue(res5.contains("Mitmanagen/SUB:NOM:SIN:NEU:INF"));
    Assertions.assertTrue(res5.contains("Mitmanagen/SUB:AKK:SIN:NEU:INF"));
    Assertions.assertTrue(res5.contains("Mitmanagen/SUB:DAT:SIN:NEU:INF"));
    Assertions.assertFalse(res5.contains("ADJ:"));

    List<AnalyzedTokenReadings> result6 = tagger.tag(Collections.singletonList("Mitmanagens"));
    MatcherAssert.assertThat(result6.size(), is(1));
    MatcherAssert.assertThat(result6.get(0).getReadings().size(), is(1));
    String res6 = result6.toString();
    Assertions.assertTrue(res6.contains("Mitmanagen/SUB:GEN:SIN:NEU:INF"));
    Assertions.assertFalse(res6.contains("ADJ:"));

    List<AnalyzedTokenReadings> result7 = tagger.tag(Collections.singletonList("Wegstrecken"));
    MatcherAssert.assertThat(result7.size(), is(1));
    MatcherAssert.assertThat(result7.get(0).getReadings().size(), is(7));
    String res7 = result7.toString();
    Assertions.assertFalse(res7.contains("Wegstrecken/SUB:GEN:SIN:NEU:INF"));
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
      elements.add(reading.toString());
    }
    sb.append(String.join(", ", elements));
    sb.append(']');
    return sb.toString();
  }
}
