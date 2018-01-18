/* LanguageTool, a natural language style checker 
 * Copyright (C) 2012 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.de;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import org.junit.Ignore;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.AustrianGerman;
import org.languagetool.language.German;
import org.languagetool.language.GermanyGerman;
import org.languagetool.language.SwissGerman;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.spelling.hunspell.HunspellRule;

import morfologik.fsa.FSA;
import morfologik.fsa.builders.CFSA2Serializer;
import morfologik.fsa.builders.FSABuilder;
import morfologik.speller.Speller;
import morfologik.stemming.Dictionary;

public class GermanSpellerRuleTest {

  private static final GermanyGerman GERMAN_DE = new GermanyGerman();
  private static final SwissGerman GERMAN_CH = new SwissGerman();

  //
  // NOTE: also manually run SuggestionRegressionTest when the suggestions are changing!
  //
  
  @Test
  public void filterForLanguage() {
    GermanSpellerRule rule = new GermanSpellerRule(TestTools.getMessages("de"), GERMAN_DE);
    List<String> list1 = new ArrayList<>(Arrays.asList("Mafiosi s", "foo"));
    rule.filterForLanguage(list1);
    assertThat(list1, is(Arrays.asList("foo")));

    List<String> list2 = new ArrayList<>(Arrays.asList("-bar", "foo"));
    rule.filterForLanguage(list2);
    assertThat(list2, is(Arrays.asList("foo")));

    GermanSpellerRule ruleCH = new GermanSpellerRule(TestTools.getMessages("de"), GERMAN_CH);
    List<String> list3 = new ArrayList<>(Arrays.asList("Muße", "foo"));
    ruleCH.filterForLanguage(list3);
    assertThat(list3, is(Arrays.asList("Musse", "foo")));
  }

  @Test
  public void testSortSuggestion() throws Exception {
    GermanSpellerRule rule = new GermanSpellerRule(TestTools.getMessages("de"), GERMAN_DE);
    assertThat(rule.sortSuggestionByQuality("fehler", Arrays.asList("fehla", "xxx", "Fehler")).toString(),
            is("[Fehler, fehla, xxx]"));
    assertThat(rule.sortSuggestionByQuality("mülleimer", Arrays.asList("Mülheimer", "-mülheimer", "Melkeimer", "Mühlheimer", "Mülleimer")).toString(),
            is("[Mülleimer, Mülheimer, -mülheimer, Melkeimer, Mühlheimer]"));
  }

  @Test
  public void testProhibited() throws Exception {
    GermanSpellerRule rule = new GermanSpellerRule(TestTools.getMessages("de"), GERMAN_DE);
    rule.getSuggestions("");  // needed to force a proper init
    assertTrue(rule.isProhibited("Standart-Test"));
    assertTrue(rule.isProhibited("Weihnachtfreier"));
    assertFalse(rule.isProhibited("Standard-Test"));
  }

  @Test
  public void testGetAdditionalTopSuggestions() throws Exception {
    GermanSpellerRule rule = new GermanSpellerRule(TestTools.getMessages("de"), GERMAN_DE);
    JLanguageTool lt = new JLanguageTool(GERMAN_DE);
    assertThat(rule.match(lt.getAnalyzedSentence("konservierungsstoffe"))[0].getSuggestedReplacements().toString(), is("[Konservierungsstoffe]"));
    assertThat(rule.match(lt.getAnalyzedSentence("konservierungsstoffstatistik"))[0].getSuggestedReplacements().toString(), is("[Konservierungsstoffstatistik]"));
    assertThat(rule.match(lt.getAnalyzedSentence("konservierungsstoffsasdsasda"))[0].getSuggestedReplacements().size(), is(0));
    assertThat(rule.match(lt.getAnalyzedSentence("Ventrolateral")).length, is(0));
    assertThat(rule.match(lt.getAnalyzedSentence("Majonäse."))[0].getSuggestedReplacements().toString(), is("[Mayonnaise.]"));
    assertFirstSuggestion("Ist Ventrolateral", "ventrolateral", rule, lt);
    assertFirstSuggestion("denkte", "dachte", rule, lt);
    assertFirstSuggestion("schwimmte", "schwamm", rule, lt);
    assertFirstSuggestion("gehte", "ging", rule, lt);
    assertFirstSuggestion("greifte", "griff", rule, lt);
    assertFirstSuggestion("geschwimmt", "geschwommen", rule, lt);
    assertFirstSuggestion("gegeht", "gegangen", rule, lt);
    assertFirstSuggestion("getrinkt", "getrunken", rule, lt);
    assertFirstSuggestion("gespringt", "gesprungen", rule, lt);
    assertFirstSuggestion("geruft", "gerufen", rule, lt);
    assertFirstSuggestion("Au-pair-Agentr", "Au-pair-Agentur", rule, lt); // "Au-pair" from spelling.txt 
    assertFirstSuggestion("Netflix-Flm", "Netflix-Film", rule, lt); // "Netflix" from spelling.txt
    assertFirstSuggestion("Bund-Länder-Kommissio", "Bund-Länder-Kommission", rule, lt);
    assertFirstSuggestion("Emailaccount", "E-Mail-Account", rule, lt);
    assertFirstSuggestion("Emailacount", "E-Mail-Account", rule, lt);
    assertFirstSuggestion("millionmal", "Million Mal", rule, lt);
    assertFirstSuggestion("millionenmal", "Millionen Mal", rule, lt);
    assertFirstSuggestion("geupdated", "upgedatet", rule, lt);
    assertFirstSuggestion("rosanen", "rosa", rule, lt);
    assertFirstSuggestion("missionariesierung", "Missionierung", rule, lt);
    assertFirstSuggestion("angehangener", "angehängter", rule, lt);
    assertFirstSuggestion("aufgehangene", "aufgehängte", rule, lt);
    assertFirstSuggestion("Germanistiker", "Germanist", rule, lt);
    assertFirstSuggestion("Germanistikern", "Germanisten", rule, lt);
    assertFirstSuggestion("Germanistikerin", "Germanistin", rule, lt);
    assertFirstSuggestion("erhöherung", "Erhöhung", rule, lt);
    assertFirstSuggestion("aufjedenfall", "auf jeden Fall", rule, lt);
    assertFirstSuggestion("Aufjedenfall", "Auf jeden Fall", rule, lt);
    assertFirstSuggestion("funkzunierende", "funktionierende", rule, lt);
    assertFirstSuggestion("funkzuniert", "funktioniert", rule, lt);
    assertFirstSuggestion("Majonäse", "Mayonnaise", rule, lt);
    assertFirstSuggestion("Salatmajonäse", "Salatmayonnaise", rule, lt);
    assertFirstSuggestion("Physiklaborants", "Physiklaboranten", rule, lt);
    assertFirstSuggestion("interkurelle", "interkulturelle", rule, lt);
    assertFirstSuggestion("Zuende", "Zu Ende", rule, lt);
    assertFirstSuggestion("zuende", "zu Ende", rule, lt);
    assertFirstSuggestion("wolt", "wollt", rule, lt);
    assertFirstSuggestion("allmähliges", "allmähliches", rule, lt);
    assertFirstSuggestion("Allmähllig", "Allmählich", rule, lt);
    assertFirstSuggestion("Probiren", "Probieren", rule, lt);
    assertFirstSuggestion("gesetztreu", "gesetzestreu", rule, lt);
    assertFirstSuggestion("wikiche", "wirkliche", rule, lt);
    assertFirstSuggestion("kongratulierst", "gratulierst", rule, lt);
    assertFirstSuggestion("Makeup", "Make-up", rule, lt);
    assertFirstSuggestion("profesionehlle", "professionelle", rule, lt);
    assertFirstSuggestion("professionählles", "professionelles", rule, lt);
    assertFirstSuggestion("gehnemigung", "Genehmigung", rule, lt);
    assertFirstSuggestion("Korrigierungen", "Korrekturen", rule, lt);
    assertFirstSuggestion("Ticketresawihrung", "Ticketreservierung", rule, lt);
    assertFirstSuggestion("gin", "ging", rule, lt);
    assertFirstSuggestion("Gleichrechtige", "Gleichberechtigte", rule, lt);
    assertFirstSuggestion("unnützliche", "unnütze", rule, lt);
    assertFirstSuggestion("hälst", "hältst", rule, lt);
    assertFirstSuggestion("erhälst", "erhältst", rule, lt);
    assertFirstSuggestion("Verstehendnis", "Verständnis", rule, lt);
    assertFirstSuggestion("Wohlfühlsein", "Wellness", rule, lt);
    assertFirstSuggestion("schmetrlinge", "Schmetterlinge", rule, lt);
    assertFirstSuggestion("einlamienirte", "laminierte", rule, lt);
    assertFirstSuggestion("Assecoires", "Accessoires", rule, lt);
    assertFirstSuggestion("Vorraussetzungen", "Voraussetzungen", rule, lt);
    assertFirstSuggestion("aufwechselungsreichem", "abwechslungsreichem", rule, lt);
    assertFirstSuggestion("nachwievor", "nach wie vor", rule, lt);
    assertFirstSuggestion("letztenendes", "letzten Endes", rule, lt);
    assertFirstSuggestion("mitanader", "miteinander", rule, lt);
    assertFirstSuggestion("nocheimal", "noch einmal", rule, lt);
    assertFirstSuggestion("konflikationen", "Komplikationen", rule, lt);
    assertFirstSuggestion("unswar", "und zwar", rule, lt);
    assertFirstSuggestion("fomelare", "Formulare", rule, lt);
    assertFirstSuggestion("inordnung", "in Ordnung", rule, lt);
    assertFirstSuggestion("inbälde", "in Bälde", rule, lt);
    assertFirstSuggestion("unaufbesichtigt", "unbeaufsichtigt", rule, lt);
    assertFirstSuggestion("uberaschend", "überraschend", rule, lt);
    assertFirstSuggestion("uberagendes", "überragendes", rule, lt);
    assertFirstSuggestion("unabsichtiges", "unabsichtliches", rule, lt);
    assertFirstSuggestion("organisatives", "organisatorisches", rule, lt);
    assertFirstSuggestion("Medallion", "Medaillon", rule, lt);
    assertFirstSuggestion("diagnosiere", "diagnostiziere", rule, lt);
    assertFirstSuggestion("diagnoziert", "diagnostiziert", rule, lt);
    assertFirstSuggestion("durschnittliche", "durchschnittliche", rule, lt);
    assertFirstSuggestion("durschnitliche", "durchschnittliche", rule, lt);
    assertFirstSuggestion("durchnitliche", "durchschnittliche", rule, lt);
    assertFirstSuggestion("Durschnittswerte", "Durchschnittswerte", rule, lt);
    assertFirstSuggestion("Durschnittsbürgers", "Durchschnittsbürgers", rule, lt);
    assertFirstSuggestion("Heileit", "Highlight", rule, lt);
    assertFirstSuggestion("todesbedrohende", "lebensbedrohende", rule, lt);
    assertFirstSuggestion("todesbedrohliches", "lebensbedrohliches", rule, lt);
    assertFirstSuggestion("folklorisch", "folkloristisch", rule, lt);
    assertFirstSuggestion("Religiösischen", "Religiösen", rule, lt);
    assertFirstSuggestion("reschaschiert", "recherchiert", rule, lt);
    assertFirstSuggestion("bißjen", "bisschen", rule, lt);
    assertFirstSuggestion("bisien", "bisschen", rule, lt);
    assertFirstSuggestion("Gruessen", "Grüßen", rule, lt);
    assertFirstSuggestion("Matschscheibe", "Mattscheibe", rule, lt);
    assertFirstSuggestion("Pearl-Harbour", "Pearl Harbor", rule, lt);
    assertFirstSuggestion("Autonomität", "Autonomie", rule, lt);
    assertFirstSuggestion("Kompatibelkeit", "Kompatibilität", rule, lt);
    assertFirstSuggestion("WiFi-Direkt", "Wi-Fi Direct", rule, lt);
    assertFirstSuggestion("Wi-Fi-Direct", "Wi-Fi Direct", rule, lt);
    assertFirstSuggestion("hofen", "hoffen", rule, lt);
    assertFirstSuggestion("frustuck", "Frühstück", rule, lt);
    assertFirstSuggestion("recourcen", "Ressourcen", rule, lt);
    assertFirstSuggestion("familliarisches", "familiäres", rule, lt);
    assertFirstSuggestion("sommerverie", "Sommerferien", rule, lt);
    assertFirstSuggestion("thelepatie", "Telepathie", rule, lt);
    assertFirstSuggestion("artz", "Arzt", rule, lt);
    assertFirstSuggestion("berücksichtung", "Berücksichtigung", rule, lt);
    assertFirstSuggestion("okey", "okay", rule, lt);
    assertFirstSuggestion("Energiesparung", "Energieeinsparung", rule, lt);
    assertFirstSuggestion("Deluxe-Version", "De-luxe-Version", rule, lt);
    assertFirstSuggestion("De-luxe-Champagnr", "De-luxe-Champagner", rule, lt);
  }

  @Test
  public void testAddIgnoreWords() throws Exception {
    MyGermanSpellerRule rule = new MyGermanSpellerRule(TestTools.getMessages("de"), GERMAN_DE);
    rule.addIgnoreWords("Fußelmappse");
    JLanguageTool lt = new JLanguageTool(GERMAN_DE);
    assertCorrect("Fußelmappse", rule, lt);
    rule.addIgnoreWords("Fußelmappse/N");
    assertCorrect("Fußelmappse", rule, lt);
    assertCorrect("Fußelmappsen", rule, lt);
    rule.addIgnoreWords("Toggeltröt/NS");
    assertCorrect("Toggeltröt", rule, lt);
    assertCorrect("Toggeltröts", rule, lt);
    assertCorrect("Toggeltrötn", rule, lt);
    MyGermanSpellerRule ruleCH = new MyGermanSpellerRule(TestTools.getMessages("de"), GERMAN_CH);
    ruleCH.addIgnoreWords("Fußelmappse/N");
    assertCorrect("Fusselmappse", ruleCH, lt);
    assertCorrect("Fusselmappsen", ruleCH, lt);
  }

  private void assertCorrect(String word, MyGermanSpellerRule rule, JLanguageTool lt) throws IOException {
    assertThat(rule.match(lt.getAnalyzedSentence(word)).length, is(0));
  }

  private void assertFirstSuggestion(String input, String expected, GermanSpellerRule rule, JLanguageTool lt) throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(input));
    assertThat(matches[0].getSuggestedReplacements().get(0), is(expected));
  }

  @Test
  public void testDashAndHyphen() throws Exception {
    HunspellRule rule = new GermanSpellerRule(TestTools.getMessages("de"), GERMAN_DE);
    JLanguageTool lt = new JLanguageTool(GERMAN_DE);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Ist doch - gut")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Ist doch -- gut")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Stil- und Grammatikprüfung gut")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Stil-, Text- und Grammatikprüfung gut")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Er liebt die Stil-, Text- und Grammatikprüfung.")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Stil-, Text- und Grammatikprüfung")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Stil-, Text- oder Grammatikprüfung")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Miet- und Zinseinkünfte")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("SPD- und CDU-Abgeordnete")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Haupt- und Nebensatz")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Vertuschungs- und Bespitzelungsmaßnahmen")).length); // remove "s" from "Vertuschungs" before spell check
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Au-pair-Agentur")).length); // compound with ignored word from spelling.txt
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Netflix-Film")).length); // compound with ignored word from spelling.txt
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Bund-Länder-Kommission")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Des World Wide Webs")).length); // expanded multi-word entry from spelling.txt
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Der westperuanische Ferienort.")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("„Pumpe“-Nachfolge")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("ÖVP- und FPÖ-Chefverhandler")).length); // first part is from spelling.txt

    assertEquals(1, rule.match(lt.getAnalyzedSentence("Miet und Zinseinkünfte")).length);
    assertEquals(1, rule.match(lt.getAnalyzedSentence("Stil- und Grammatik gut")).length);
    assertEquals(1, rule.match(lt.getAnalyzedSentence("Flasch- und Grammatikprüfung gut")).length);
    //assertEquals(1, rule.match(langTool.getAnalyzedSentence("Haupt- und Neben")).length);  // hunspell accepts this :-(

    // check acceptance of words in ignore.txt ending with "-*"
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Dual-Use-Güter")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Dual-Use- und Wirtschaftsgüter")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Test-Dual-Use")).length);
    assertEquals(1, rule.match(lt.getAnalyzedSentence("Dual-Use")).length);
  }

  @Test
  public void testGetSuggestionsFromSpellingTxt() throws Exception {
    MyGermanSpellerRule ruleGermany = new MyGermanSpellerRule(TestTools.getMessages("de"), GERMAN_DE);
    assertThat(ruleGermany.getSuggestions("Ligafußboll").toString(), is("[Ligafußball, Ligafußballs]"));  // from spelling.txt
    assertThat(ruleGermany.getSuggestions("free-and-open-source").toString(), is("[]"));  // to prevent OutOfMemoryErrors: do not create hyphenated compounds consisting of >3 parts
    MyGermanSpellerRule ruleSwiss = new MyGermanSpellerRule(TestTools.getMessages("de"), GERMAN_CH);
    assertThat(ruleSwiss.getSuggestions("Ligafußboll").toString(), is("[Ligafussball, Ligafussballs]"));
    assertThat(ruleSwiss.getSuggestions("konfliktbereid").toString(), is("[konfliktbereit, konfliktbereite]"));
    assertThat(ruleSwiss.getSuggestions("konfliktbereitel").toString(),
               is("[konfliktbereiten, konfliktbereite, konfliktbereiter, konfliktbereitem, konfliktbereites, konfliktbereit]"));
  }

  @Test
  public void testIgnoreWord() throws Exception {
    MyGermanSpellerRule ruleGermany = new MyGermanSpellerRule(TestTools.getMessages("de"), GERMAN_DE);
    assertTrue(ruleGermany.doIgnoreWord("einPseudoWortFürLanguageToolTests"));  // from ignore.txt
    assertTrue(ruleGermany.doIgnoreWord("Wichtelmännchen"));            // from spelling.txt
    assertTrue(ruleGermany.doIgnoreWord("Wichtelmännchens"));           // from spelling.txt with suffix
    assertFalse(ruleGermany.doIgnoreWord("wichtelmännchen"));           // from spelling.txt, no reason to accept it as lowercase
    assertFalse(ruleGermany.doIgnoreWord("wichtelmännchens"));          // from spelling.txt with suffix, no reason to accept it as lowercase
    assertTrue(ruleGermany.doIgnoreWord("vorgehängt"));                 // from spelling.txt
    assertTrue(ruleGermany.doIgnoreWord("vorgehängten"));               // from spelling.txt with suffix
    assertTrue(ruleGermany.doIgnoreWord("Vorgehängt"));                 // from spelling.txt, it's lowercase there but we accept uppercase at idx = 0
    assertTrue(ruleGermany.doIgnoreWord("Vorgehängten"));               // from spelling.txt with suffix, it's lowercase there but we accept uppercase at idx = 0
    assertTrue(ruleGermany.doIgnoreWord("Wichtelmännchen-vorgehängt")); // from spelling.txt formed hyphenated compound
    assertTrue(ruleGermany.doIgnoreWord("Wichtelmännchen-Au-pair"));    // from spelling.txt formed hyphenated compound
    assertTrue(ruleGermany.doIgnoreWord("Fermi-Dirac-Statistik"));      // from spelling.txt formed hyphenated compound
    assertTrue(ruleGermany.doIgnoreWord("Au-pair-Wichtelmännchen"));    // from spelling.txt formed hyphenated compound
    assertTrue(ruleGermany.doIgnoreWord("Secondhandware"));             // from spelling.txt formed compound
    assertTrue(ruleGermany.doIgnoreWord("Feynmandiagramme"));           // from spelling.txt formed compound
    assertTrue(ruleGermany.doIgnoreWord("Helizitätsoperator"));         // from spelling.txt formed compound
    assertTrue(ruleGermany.doIgnoreWord("Wodkaherstellung"));           // from spelling.txt formed compound
    assertTrue(ruleGermany.doIgnoreWord("Latte-macchiato-Glas"));       // from spelling.txt formed compound
    assertTrue(ruleGermany.doIgnoreWord("No-Name-Hersteller"));         // from spelling.txt formed compound
    assertFalse(ruleGermany.doIgnoreWord("Helizitätso"));               // from spelling.txt formed compound (second part is too short)
    assertFalse(ruleGermany.doIgnoreWord("Feynmand"));                  // from spelling.txt formed compound (second part is too short)
    assertFalse(ruleGermany.doIgnoreWord("Hundhütte"));                 // compound formed from two valid words, but still incorrect
    assertFalse(ruleGermany.doIgnoreWord("Frauversteher"));             // compound formed from two valid words, but still incorrect
    assertFalse(ruleGermany.doIgnoreWord("Wodkasglas"));                // compound formed from two valid words, but still incorrect
    assertFalse(ruleGermany.doIgnoreWord("Author"));
    assertFalse(ruleGermany.doIgnoreWord("SecondhandWare"));            // from spelling.txt formed compound
    assertFalse(ruleGermany.doIgnoreWord("MHDware"));                   // from spelling.txt formed compound
    MyGermanSpellerRule ruleSwiss = new MyGermanSpellerRule(TestTools.getMessages("de"), GERMAN_CH);
    assertTrue(ruleSwiss.doIgnoreWord("einPseudoWortFürLanguageToolTests"));
    assertFalse(ruleSwiss.doIgnoreWord("Ligafußball"));        // 'ß' never accepted for Swiss
  }

  private static class MyGermanSpellerRule extends GermanSpellerRule {
    MyGermanSpellerRule(ResourceBundle messages, German language) throws IOException {
      super(messages, language);
      init();
    }
    boolean doIgnoreWord(String word) throws IOException {
      return super.ignoreWord(Collections.singletonList(word), 0);
    }
  }

  // note: copied from HunspellRuleTest
  @Test
  public void testRuleWithGermanyGerman() throws Exception {
    HunspellRule rule = new GermanSpellerRule(TestTools.getMessages("de"), GERMAN_DE);
    JLanguageTool lt = new JLanguageTool(GERMAN_DE);
    commonGermanAsserts(rule, lt);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Der äußere Übeltäter.")).length);  // umlauts
    assertEquals(1, rule.match(lt.getAnalyzedSentence("Der äussere Übeltäter.")).length);
    // TODO: this is a false alarm:
    //assertEquals(0, rule.match(langTool.getAnalyzedSentence("Die Mozart'sche Sonate.")).length);
  }

  // note: copied from HunspellRuleTest
  @Test
  public void testRuleWithAustrianGerman() throws Exception {
    AustrianGerman language = new AustrianGerman();
    HunspellRule rule = new GermanSpellerRule(TestTools.getMessages("de"), language);
    JLanguageTool lt = new JLanguageTool(language);
    commonGermanAsserts(rule, lt);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Der äußere Übeltäter.")).length);  // umlauts
    assertEquals(1, rule.match(lt.getAnalyzedSentence("Der äussere Übeltäter.")).length);
  }

  // note: copied from HunspellRuleTest
  @Test
  public void testRuleWithSwissGerman() throws Exception {
    SwissGerman language = new SwissGerman();
    HunspellRule rule = new GermanSpellerRule(TestTools.getMessages("de"), language);
    JLanguageTool lt = new JLanguageTool(language);
    commonGermanAsserts(rule, lt);
    assertEquals(1, rule.match(lt.getAnalyzedSentence("Der äußere Übeltäter.")).length);  // ß not allowed in Swiss
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Der äussere Übeltäter.")).length);  // ss is used instead of ß
  }
  
  // note: copied from HunspellRuleTest
  private void commonGermanAsserts(HunspellRule rule, JLanguageTool lt) throws IOException {
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Der Waschmaschinentestversuch")).length);  // compound
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Der Waschmaschinentest-Versuch")).length);  // compound
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Der Arbeitnehmer")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Die Verhaltensänderung")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Er bzw. sie.")).length); // abbreviations

    assertEquals(1, rule.match(lt.getAnalyzedSentence("Der Waschmaschinentest-Dftgedgs")).length);
    assertEquals(1, rule.match(lt.getAnalyzedSentence("Der Dftgedgs-Waschmaschinentest")).length);
    assertEquals(1, rule.match(lt.getAnalyzedSentence("Der Waschmaschinentestdftgedgs")).length);
    assertEquals(1, rule.match(lt.getAnalyzedSentence("Der Waschmaschinentestversuch orkt")).length);
    assertEquals(1, rule.match(lt.getAnalyzedSentence("Der Arbeitsnehmer")).length);  // wrong interfix
    assertEquals(1, rule.match(lt.getAnalyzedSentence("Die Verhaltenänderung")).length);  // missing interfix
    assertEquals(1, rule.match(lt.getAnalyzedSentence("Er bw. sie.")).length); // abbreviations (bzw.)
    assertEquals(2, rule.match(lt.getAnalyzedSentence("Der asdegfue orkt")).length);
    assertEquals(1, rule.match(lt.getAnalyzedSentence("rumfangreichen")).length);
  }
  
  @Test
  public void testGetSuggestions() throws Exception {
    HunspellRule rule = new GermanSpellerRule(TestTools.getMessages("de"), GERMAN_DE);

    assertCorrection(rule, "Hauk", "Haus", "Haut");
    assertCorrection(rule, "Eisnbahn", "Eisbahn", "Eisenbahn"); 
    assertCorrection(rule, "Rechtschreipreform", "Rechtschreibreform");
    assertCorrection(rule, "Theatrekasse", "Theaterkasse");
    assertCorrection(rule, "Traprennen", "Trabrennen");
    assertCorrection(rule, "Autuverkehr", "Autoverkehr");
    assertCorrection(rule, "Rechtschreibprüfun", "Rechtschreibprüfung");
    assertCorrection(rule, "Rechtschreib-Prüfun", "Rechtschreib-Prüfung");
    assertCorrection(rule, "bw.", "bzw.");
    assertCorrection(rule, "kan", "kann", "an");
    assertCorrection(rule, "kan.", "kann.", "an.");
    assertCorrection(rule, "Einzahlungschein", "Einzahlungsschein");
    assertCorrection(rule, "Arbeitamt", "Arbeitet", "Arbeitsamt");
    assertCorrection(rule, "Ordnungshütter", "Ordnungshüter");

    //TODO: requires morfologik-speller change (suggestions for known words):
    //assertCorrection(rule, "Arbeitamt", "Arbeitsamt");

    assertCorrection(rule, "Autoverkehrr", "Autoverkehr");

    assertCorrection(rule, "hasslich", "hässlich", "fasslich");
    assertCorrection(rule, "Struße", "Strauße", "Straße", "Sträuße");
    
    assertCorrection(rule, "gewohnlich", "gewöhnlich");
    assertCorrection(rule, "gawöhnlich", "gewöhnlich");
    assertCorrection(rule, "gwöhnlich", "gewöhnlich");
    assertCorrection(rule, "geewöhnlich", "gewöhnlich");
    assertCorrection(rule, "gewönlich", "gewöhnlich");
    
    assertCorrection(rule, "außergewöhnkich", "außergewöhnlich");
    assertCorrection(rule, "agressiv", "aggressiv");
    assertCorrection(rule, "agressivster", "aggressivster");
    assertCorrection(rule, "agressiver", "aggressiver");
    assertCorrection(rule, "agressive", "aggressive");
    
    assertCorrection(rule, "Algorythmus", "Algorithmus");
    assertCorrection(rule, "Algorhythmus", "Algorithmus");
    
    assertCorrection(rule, "Amalgan", "Amalgam");
    assertCorrection(rule, "Amaturenbrett", "Armaturenbrett");
    assertCorrection(rule, "Aquise", "Akquise");
    assertCorrection(rule, "Artzt", "Arzt");
    
    assertCorrection(rule, "aufgrunddessen", "aufgrund dessen");
    
    assertCorrection(rule, "barfuss", "barfuß");
    assertCorrection(rule, "Batallion", "Bataillon");
    assertCorrection(rule, "Medallion", "Medaillon");
    assertCorrection(rule, "Scheisse", "Scheiße");
    assertCorrection(rule, "Handselvertreter", "Handelsvertreter");
    
    assertCorrection(rule, "aul", "auf");
    assertCorrection(rule, "Icj", "Ich");   // only "ich" (lowercase) is in the lexicon
    //assertCorrection(rule, "Ihj", "Ich");   // only "ich" (lowercase) is in the lexicon - does not work because of the limit

    // three part compounds:
    assertCorrection(rule, "Handelsvertretertrffen", "Handelsvertretertreffen");
    assertCorrection(rule, "Handelsvartretertreffen", "Handelsvertretertreffen");
    assertCorrection(rule, "Handelsvertretertriffen", "Handelsvertretertreffen");
    assertCorrection(rule, "Handelsvertrtertreffen", "Handelsvertretertreffen");
    assertCorrection(rule, "Handselvertretertreffen", "Handelsvertretertreffen");

    assertCorrection(rule, "Arbeidszimmer", "Arbeitszimmer");
    assertCorrection(rule, "Postleidzahl", "Postleitzahl");
    assertCorrection(rule, "vorallem", "vor allem");
    assertCorrection(rule, "wieviel", "wie viel");
    assertCorrection(rule, "wieviele", "wie viele");
    assertCorrection(rule, "wievielen", "wie vielen");
    assertCorrection(rule, "undzwar", "und zwar");

    // TODO: compounds with errors in more than one part
    // totally wrong jwordsplitter split: Hands + elvertretertreffn:
    //assertCorrection(rule, "Handselvertretertreffn", "Handelsvertretertreffen");
  }

  @Test
  public void testGetSuggestionWithPunctuation() throws Exception {
    GermanSpellerRule rule = new GermanSpellerRule(TestTools.getMessages("de"), GERMAN_DE);
    JLanguageTool lt = new JLanguageTool(GERMAN_DE);
    assertFirstSuggestion("informationnen.", "Informationen.", rule, lt);
    assertFirstSuggestion("Kundigungsfrist.", "Kündigungsfrist.", rule, lt);
    assertFirstSuggestion("aufgeregegt.", "aufgeregt.", rule, lt);
    assertFirstSuggestion("informationnen...", "Informationen...", rule, lt);
    assertFirstSuggestion("arkbeiten-", "arbeiten", rule, lt);
    //assertFirstSuggestion("arkjbeiten-", "arbeiten", rule, lt);
    // commas are actually not part of the word, so the suggestion doesn't include them:
    assertFirstSuggestion("informationnen,", "Informationen", rule, lt);
  }
  
  @Test
  public void testGetSuggestionOrder() throws Exception {
    HunspellRule rule = new GermanSpellerRule(TestTools.getMessages("de"), GERMAN_DE);
    assertCorrectionsByOrder(rule, "heisst", "heißt");  // "heißt" should be first
    assertCorrectionsByOrder(rule, "heissen", "heißen");
    assertCorrectionsByOrder(rule, "müßte", "musste", "müsste");
    assertCorrectionsByOrder(rule, "schmohren", "Lehmohren", "schmoren");
    assertCorrectionsByOrder(rule, "Fänomen", "Phänomen");
    assertCorrectionsByOrder(rule, "homofob", "homophob");
    assertCorrectionsByOrder(rule, "ueber", "über");
    //assertCorrectionsByOrder(rule, "uebel", "übel");
    assertCorrectionsByOrder(rule, "Aerger", "Ärger");
    assertCorrectionsByOrder(rule, "Walt", "Wald");
    assertCorrectionsByOrder(rule, "Rythmus", "Rhythmus");
    assertCorrectionsByOrder(rule, "Rytmus", "Rhythmus");
    assertCorrectionsByOrder(rule, "is", "IS", "in", "im", "ist");  // 'ist' should actually be preferred...
    assertCorrectionsByOrder(rule, "Fux", "Fuchs");  // fixed in morfologik 2.1.4
    assertCorrectionsByOrder(rule, "schänken", "Schänken");  // "schenken" is missing
  }
  
  @Test
  @Ignore("testing a potential bug in Morfologik")
  public void testMorfologikSpeller() throws Exception {
    List<byte[]> lines = new ArrayList<>();
    lines.add("die".getBytes());
    lines.add("ist".getBytes());
    byte[] info = ("fsa.dict.separator=+\n" +
                   "fsa.dict.encoding=utf-8\n" +
                   "fsa.dict.frequency-included=true\n" +
                   "fsa.dict.encoder=SUFFIX").getBytes();
    Dictionary dict = getDictionary(lines, new ByteArrayInputStream(info));
    Speller speller = new Speller(dict, 2);
    System.out.println(speller.findReplacements("is"));  // why do both "die" and "ist" have a distance of 1 in the CandidateData constructor?
  }

  @Test
  @Ignore("testing Morfologik directly, with LT dictionary (de_DE.dict) but no LT-specific code")
  public void testMorfologikSpeller2() throws Exception {
    Dictionary dict = Dictionary.read(JLanguageTool.getDataBroker().getFromResourceDirAsUrl("/de/hunspell/de_DE.dict"));
    runTests(dict, "Fux");
  }

  @Test
  @Ignore("testing Morfologik directly, with hard-coded dictionary but no LT-specific code")
  public void testMorfologikSpellerWithSpellingTxt() throws Exception {
    String inputWord = "schänken";  // expected to work (i.e. also suggest 'schenken'), but doesn't
    List<String> dictWords = Arrays.asList("schenken", "Schänken");
    List<byte[]> dictWordsAsBytes = new ArrayList<>();
    for (String entry : dictWords) {
      dictWordsAsBytes.add(entry.getBytes("utf-8"));
    }
    dictWordsAsBytes.sort(FSABuilder.LEXICAL_ORDERING);
    FSA fsa = FSABuilder.build(dictWordsAsBytes);
    ByteArrayOutputStream fsaOutStream = new CFSA2Serializer().serialize(fsa, new ByteArrayOutputStream());
    //FileOutputStream fos = new FileOutputStream("/tmp/morfologik.dict");
    //fos.write(fsaOutStream.toByteArray());
    ByteArrayInputStream fsaInStream = new ByteArrayInputStream(fsaOutStream.toByteArray());
    String infoFile = "fsa.dict.speller.replacement-pairs=ä e\n" +
                      "fsa.dict.encoder=SUFFIX\n" +
                      "fsa.dict.separator=+\n" +
                      "fsa.dict.encoding=utf-8\n" +
                      "fsa.dict.speller.ignore-diacritics=false\n";
    InputStream is = new ByteArrayInputStream(infoFile.getBytes("utf-8"));
    Dictionary dict = Dictionary.read(fsaInStream, is);
    runTests(dict, inputWord);
  }

  @Test
  public void testPosition() throws IOException{
    HunspellRule rule = new GermanSpellerRule(TestTools.getMessages("de"), GERMAN_DE);
    JLanguageTool lt = new JLanguageTool(GERMAN_DE);
    RuleMatch[] match1 = rule.match(lt.getAnalyzedSentence(
            "Er ist entsetzt, weil beim 'Wiederaufbau' das original-gotische Achsfenster mit reichem Maßwerk ausgebaut " +
            "und an die südliche TeStWoRt gesetzt wurde."));
    assertThat(match1.length, is(1));
    assertThat(match1[0].getFromPos(), is(126));
    assertThat(match1[0].getToPos(), is(134));
  }

  private void runTests(Dictionary dict, String input) {
    Speller speller1 = new Speller(dict);
    System.out.println(input + " isMisspelled()     : " + speller1.isMisspelled(input));
    System.out.println(input + " isInDictionary()   : " + speller1.isInDictionary(input));
    System.out.println(input + " getFrequency()     : " + speller1.getFrequency(input));
    System.out.println(input + " replaceRunOnWords(): " + speller1.replaceRunOnWords(input));
    for (int maxDist = 1; maxDist <= 3; maxDist++) {
      Speller speller = new Speller(dict, maxDist);
      List<String> replacements = speller.findReplacements(input);
      System.out.println("maxDist=" + maxDist + ": " + input + " => " + replacements);
    }
  }

  private Dictionary getDictionary(List<byte[]> lines, InputStream infoFile) throws IOException {
    Collections.sort(lines, FSABuilder.LEXICAL_ORDERING);
    FSA fsa = FSABuilder.build(lines);
    ByteArrayOutputStream fsaOutStream = new CFSA2Serializer().serialize(fsa, new ByteArrayOutputStream());
    ByteArrayInputStream fsaInStream = new ByteArrayInputStream(fsaOutStream.toByteArray());
    return Dictionary.read(fsaInStream, infoFile);
  }

  private void assertCorrection(HunspellRule rule, String input, String... expectedTerms) throws IOException {
    List<String> suggestions = rule.getSuggestions(input);
    for (String expectedTerm : expectedTerms) {
      assertTrue("Not found: '" + expectedTerm + "' in: " + suggestions, suggestions.contains(expectedTerm));
    }
  }
  
  private void assertCorrectionsByOrder(HunspellRule rule, String input, String... expectedTerms) throws IOException {
    List<String> suggestions = rule.getSuggestions(input);
    int i = 0;
    for (String expectedTerm : expectedTerms) {
      assertTrue("Not found at position " + i + ": '" + expectedTerm + "' in: " + suggestions, suggestions.get(i).equals(expectedTerm));
      i++;
    }
  }
  
}