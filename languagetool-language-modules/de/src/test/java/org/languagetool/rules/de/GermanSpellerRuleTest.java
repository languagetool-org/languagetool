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

import morfologik.fsa.FSA;
import morfologik.fsa.builders.CFSA2Serializer;
import morfologik.fsa.builders.FSABuilder;
import morfologik.speller.Speller;
import morfologik.stemming.Dictionary;
import org.junit.Ignore;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.TestTools;
import org.languagetool.language.AustrianGerman;
import org.languagetool.language.German;
import org.languagetool.language.GermanyGerman;
import org.languagetool.language.SwissGerman;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.spelling.hunspell.HunspellRule;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Stream;

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class GermanSpellerRuleTest {

  private static final GermanyGerman GERMAN_DE = (GermanyGerman) Languages.getLanguageForShortCode("de-DE");
  private static final SwissGerman GERMAN_CH = (SwissGerman) Languages.getLanguageForShortCode("de-CH");

  //
  // NOTE: also manually run SuggestionRegressionTest when the suggestions are changing!
  //
  
  @Test
  public void testArtig() throws IOException {
    GermanSpellerRule rule = new GermanSpellerRule(TestTools.getMessages("de"), GERMAN_DE);
    accept("zigarrenartig", rule);
    accept("zigarrenartige", rule);
    accept("zigarrenartiger", rule);
    accept("zigarrenartiges", rule);
    accept("zigarrenartigen", rule);
    accept("zigarrenartigem", rule);
    accept("handlungsartig", rule);
    dontAccept("zigarrenartigex", rule);
    dontAccept("handlungartig", rule);
    dontAccept("arbeitartig", rule);
    dontAccept("kostefrei", rule);
    dontAccept("reglemäßig", rule);
    dontAccept("hatfrei", rule);
    dontAccept("geruchhemmend", rule);
    dontAccept("aberabhängig", rule);
  }

  private void accept(String word, GermanSpellerRule rule) throws IOException {
    assertTrue(rule.ignoreWord(Collections.singletonList(word), 0));
  }

  private void dontAccept(String word, GermanSpellerRule rule) throws IOException {
    assertFalse(rule.ignoreWord(Collections.singletonList(word), 0));
  }

  @Test
  public void testGetOnlySuggestions() throws IOException {
    GermanSpellerRule rule = new GermanSpellerRule(TestTools.getMessages("de"), GERMAN_DE);
    assertThat(rule.getOnlySuggestions("autentisch").size(), is(1));
    assertThat(rule.getOnlySuggestions("autentisch").get(0).getReplacement(), is("authentisch"));
    assertThat(rule.getOnlySuggestions("autentischeres").size(), is(1));
    assertThat(rule.getOnlySuggestions("autentischeres").get(0).getReplacement(), is("authentischeres"));
    assertThat(rule.getOnlySuggestions("Autentischere").size(), is(1));
    assertThat(rule.getOnlySuggestions("Autentischere").get(0).getReplacement(), is("Authentischere"));
    JLanguageTool lt = new JLanguageTool(GERMAN_DE);
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence("Eine autentische Sache."));
    assertThat(matches.length, is(1));
    assertThat(matches[0].getSuggestedReplacements().size(), is(1));
    assertThat(matches[0].getSuggestedReplacements().get(0), is("authentische"));
  }

  @Test
  public void testFilterForLanguage() {
    GermanSpellerRule rule = new GermanSpellerRule(TestTools.getMessages("de"), GERMAN_DE);
    List<String> list1 = new ArrayList<>(Arrays.asList("Mafiosi s", "foo"));
    rule.filterForLanguage(list1);
    assertThat(list1, is(Arrays.asList("foo")));

    List<String> list2 = new ArrayList<>(Arrays.asList("-bar", "foo"));
    rule.filterForLanguage(list2);
    assertThat(list2, is(Arrays.asList("foo")));

    GermanSpellerRule ruleCH = new SwissGermanSpellerRule(TestTools.getMessages("de"), GERMAN_CH);
    List<String> list3 = new ArrayList<>(Arrays.asList("Muße", "foo"));
    ruleCH.filterForLanguage(list3);
    assertThat(list3, is(Arrays.asList("Musse", "foo")));
  }

  @Test
  public void testSortSuggestion() {
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
    assertTrue(rule.isProhibited("Standart-Test"));  // entry with ".*" in prohibited.txt
    assertTrue(rule.isProhibited("Blindarmentzündung"));  // entry with ".*" in prohibited.txt
    assertTrue(rule.isProhibited("Weihnachtfreier"));
    assertFalse(rule.isProhibited("Standard-Test"));
    assertTrue(rule.isProhibited("Abstellgreis"));
    assertTrue(rule.isProhibited("Abstellgreise"));
    assertTrue(rule.isProhibited("Abstellgreisen"));
    assertTrue(rule.isProhibited("Landstreckenflüge"));
    assertTrue(rule.isProhibited("Landstreckenflügen"));
    assertTrue(rule.isProhibited("Abdominalgangion"));
    assertTrue(rule.isProhibited("Abdominalgangionen"));
    assertTrue(rule.isProhibited("Badegas"));  // non-expanded entry in prohibited.txt
    assertTrue(rule.isProhibited("Aktienkur")); // non-expanded entry in prohibited.txt
    assertTrue(rule.isProhibited("Stellungsnahmen")); // expanded entry in prohibited.txt
    assertTrue(rule.isProhibited("Varietee")); // expanded entry in prohibited.txt
    assertTrue(rule.isProhibited("Varietees")); // expanded entry in prohibited.txt
    assertTrue(rule.isProhibited("Feuerwerksartigel")); // entry with ".*" at line start in prohibited.txt
    assertTrue(rule.isProhibited("Feuerwerksartigeln")); // entry with ".*" at line start in prohibited.txt
    assertTrue(rule.isProhibited("Feuerwerksartigels")); // entry with ".*" at line start in prohibited.txt
  }

  @Test
  public void testFilterBadSuggestions() throws Exception {
    GermanSpellerRule rule = new GermanSpellerRule(TestTools.getMessages("de"), GERMAN_DE);
    JLanguageTool lt = new JLanguageTool(GERMAN_DE);
    assertNotSuggestion("nicht in euerer", "i neuerer", rule, lt);
    assertNotSuggestion("Ich rauche ne Zigarette", "rauchen e", rule, lt);
  }

  @Test
  public void testGetAdditionalTopSuggestions() throws Exception {
    GermanSpellerRule rule = new GermanSpellerRule(TestTools.getMessages("de"), GERMAN_DE);
    JLanguageTool lt = new JLanguageTool(GERMAN_DE);
    assertThat(rule.match(lt.getAnalyzedSentence("konservierungsstoffstatistik"))[0].getSuggestedReplacements().toString(), is("[Konservierungsstoffstatistik]"));
    assertThat(rule.match(lt.getAnalyzedSentence("konservierungsstoffsasdsasda"))[0].getSuggestedReplacements().size(), is(0));
    assertThat(rule.match(lt.getAnalyzedSentence("Ventrolateral")).length, is(0));
    assertThat(rule.match(lt.getAnalyzedSentence("haben -sehr")).length, is(0));
    assertThat(rule.match(lt.getAnalyzedSentence("Kleindung")).length, is(1));  // ignored due to ignoreCompoundWithIgnoredWord(), but still in ignore.txt -> ignore.txt must override this
    assertThat(rule.match(lt.getAnalyzedSentence("Majonäse."))[0].getSuggestedReplacements().toString(), is("[Mayonnaise]"));
    assertFirstSuggestion("Schöler-", "Schüler-", rule, lt);
    assertFirstSuggestion("wars.", "war's", rule, lt);
    assertFirstSuggestion("konservierungsstoffe", "Konservierungsstoffe", rule, lt);
//    assertFirstSuggestion("Ist Ventrolateral", "ventrolateral", rule, lt);
    assertFirstSuggestion("denkte", "dachte", rule, lt);
    assertFirstSuggestion("schwimmte", "schwamm", rule, lt);
    assertFirstSuggestion("gehte", "ging", rule, lt);
    assertFirstSuggestion("greifte", "griff", rule, lt);
    assertFirstSuggestion("geschwimmt", "geschwommen", rule, lt);
    assertFirstSuggestion("gegeht", "gegangen", rule, lt);
    assertFirstSuggestion("getrinkt", "getrunken", rule, lt);
    assertFirstSuggestion("gespringt", "gesprungen", rule, lt);
    assertFirstSuggestion("geruft", "gerufen", rule, lt);
//    assertFirstSuggestion("Au-pair-Agentr", "Au-pair-Agentur", rule, lt); // "Au-pair" from spelling.txt 
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
    assertFirstSuggestion("Mayonese", "Mayonnaise", rule, lt);
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
    assertFirstSuggestion("korregierungen", "Korrekturen", rule, lt);
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
    assertFirstSuggestion("immoment", "im Moment", rule, lt);
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
    assertFirstSuggestion("einfühlsvoller", "einfühlsamer", rule, lt);
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
    assertFirstSuggestion("Sensibelkeit", "Sensibilität", rule, lt);
    assertFirstSuggestion("Flexibelkeit", "Flexibilität", rule, lt);
    assertFirstSuggestion("WiFi-Direkt", "Wi-Fi Direct", rule, lt);
    assertFirstSuggestion("Wi-Fi-Direct", "Wi-Fi Direct", rule, lt);
    assertFirstSuggestion("hofen", "hoffen", rule, lt);
    assertFirstSuggestion("frustuck", "Frühstück", rule, lt);
    assertFirstSuggestion("recourcen", "Ressourcen", rule, lt);
    assertFirstSuggestion("familiärische", "familiäre", rule, lt);
    assertFirstSuggestion("familliarisches", "familiäres", rule, lt);
    assertFirstSuggestion("sommerverie", "Sommerferien", rule, lt);
    assertFirstSuggestion("thelepatie", "Telepathie", rule, lt);
    assertFirstSuggestion("artz", "Arzt", rule, lt);
    assertFirstSuggestion("berücksichtung", "Berücksichtigung", rule, lt);
    assertFirstSuggestion("okey", "okay", rule, lt);
    assertFirstSuggestion("Energiesparung", "Energieeinsparung", rule, lt);
    assertFirstSuggestion("Deluxe-Version", "De-luxe-Version", rule, lt);
    assertFirstSuggestion("De-luxe-Champagnr", "De-luxe-Champagner", rule, lt);
    assertFirstSuggestion("problemhafte", "problembehaftete", rule, lt);
    assertFirstSuggestion("solltes", "solltest", rule, lt);
    assertFirstSuggestion("Kilimanjaro", "Kilimandscharo", rule, lt);
    assertFirstSuggestion("unzerbrechbare", "unzerbrechliche", rule, lt);
    assertFirstSuggestion("voraussichtige", "voraussichtliche", rule, lt);
    assertFirstSuggestion("Aleine", "Alleine", rule, lt);
    assertFirstSuggestion("abenzu", "ab und zu", rule, lt);
    assertFirstSuggestion("ergeitz", "Ehrgeiz", rule, lt);
    assertFirstSuggestion("chouch", "Couch", rule, lt);
    assertFirstSuggestion("kontaktfreundliche", "kontaktfreudige", rule, lt);
    assertFirstSuggestion("angestegt", "angesteckt", rule, lt);
    assertFirstSuggestion("festellt", "feststellt", rule, lt);
    assertFirstSuggestion("liqide", "liquide", rule, lt);
    assertFirstSuggestion("gelessen", "gelesen", rule, lt);
    assertFirstSuggestion("Getrixe", "Getrickse", rule, lt);
    assertFirstSuggestion("Naricht", "Nachricht", rule, lt);
    assertFirstSuggestion("konektschen", "Connection", rule, lt);
    assertFirstSuggestion("Neukundenaquise", "Neukundenakquise", rule, lt);
    assertFirstSuggestion("Gehorsamkeitsverweigerung", "Gehorsamsverweigerung", rule, lt);
    assertFirstSuggestion("leinensamens", "Leinsamens", rule, lt);
    assertFirstSuggestion("Oldheimer", "Oldtimer", rule, lt);
    assertFirstSuggestion("verhing", "verhängte", rule, lt);
    assertFirstSuggestion("vorallendingen", "vor allen Dingen", rule, lt);
    assertFirstSuggestion("unternehmenslüstige", "unternehmungslustige", rule, lt);
    assertFirstSuggestion("proffesionaler", "professioneller", rule, lt);
    assertFirstSuggestion("gesundliches", "gesundheitliches", rule, lt);
    assertFirstSuggestion("eckelt", "ekelt", rule, lt);
    assertFirstSuggestion("geherte", "geehrte", rule, lt);
    assertFirstSuggestion("Kattermesser", "Cuttermesser", rule, lt);
    assertFirstSuggestion("antisemitistischer", "antisemitischer", rule, lt);
    assertFirstSuggestion("unvorsehbares", "unvorhersehbares", rule, lt);
    assertFirstSuggestion("Würtenberg", "Württemberg", rule, lt);
    assertFirstSuggestion("Baden-Würtenbergs", "Baden-Württembergs", rule, lt);
    assertFirstSuggestion("Rechtsschreibungsfehlern", "Rechtschreibfehlern", rule, lt);
    assertFirstSuggestion("indifiziert", "identifiziert", rule, lt);
    assertFirstSuggestion("verblüte", "verblühte", rule, lt);
    assertFirstSuggestion("dreitem", "drittem", rule, lt);
    assertFirstSuggestion("zukuenftliche", "zukünftige", rule, lt);
    assertFirstSuggestion("schwarzwälderkirschtorte", "Schwarzwälder Kirschtorte", rule, lt);
    assertFirstSuggestion("kolegen", "Kollegen", rule, lt);
    assertFirstSuggestion("gerechtlichkeit", "Gerechtigkeit", rule, lt);
    assertFirstSuggestion("Zuverlässlichkeit", "Zuverlässigkeit", rule, lt);
    assertFirstSuggestion("Krankenhausen", "Krankenhäusern", rule, lt);
    assertFirstSuggestion("jedwilliger", "jedweder", rule, lt);
    assertFirstSuggestion("Betriebsratzimmern", "Betriebsratszimmern", rule, lt);
    assertFirstSuggestion("ausiehst", "aussiehst", rule, lt);
    assertFirstSuggestion("unterbemittelnde", "minderbemittelte", rule, lt);
    assertFirstSuggestion("koregiert", "korrigiert", rule, lt);
    assertFirstSuggestion("Gelangenheitsbestätigungen", "Gelangensbestätigungen", rule, lt);
    assertFirstSuggestion("mitenand", "miteinander", rule, lt);
    assertFirstSuggestion("hinunher", "hin und her", rule, lt);
    assertFirstSuggestion("Xter", "X-ter", rule, lt);
    assertFirstSuggestion("Kaufentfehlung", "Kaufempfehlung", rule, lt);
    assertFirstSuggestion("unverzeilige", "unverzeihliche", rule, lt);
    assertFirstSuggestion("Addons", "Add-ons", rule, lt);
    assertFirstSuggestion("Mitgliederinnen", "Mitglieder", rule, lt);
    assertFirstSuggestion("Feinleiner", "Fineliner", rule, lt);
    assertFirstSuggestion("größester", "größter", rule, lt);
    assertFirstSuggestion("verhäufte", "gehäufte", rule, lt);
    assertFirstSuggestion("naheste", "nächste", rule, lt);
    assertFirstSuggestion("fluoreszenzierend", "fluoreszierend", rule, lt);
    assertFirstSuggestion("revalierender", "rivalisierender", rule, lt);
    assertFirstSuggestion("häherne", "härene", rule, lt);
    assertFirstSuggestion("Portfolien", "Portfolios", rule, lt);
    assertFirstSuggestion("Nivo", "Niveau", rule, lt);
    assertFirstSuggestion("dilletanten", "Dilettanten", rule, lt);
    assertFirstSuggestion("intersannt", "interessant", rule, lt);
    assertFirstSuggestion("allereinzigstem", "einzigem", rule, lt);
    assertFirstSuggestion("Einzigste", "Einzige", rule, lt);
    assertFirstSuggestion("namenhafte", "namhafte", rule, lt);
    assertFirstSuggestion("homeophatisch", "homöopathisch", rule, lt);
    assertFirstSuggestion("verswindet", "verschwindet", rule, lt);
    assertFirstSuggestion("Durschnitt", "Durchschnitt", rule, lt);
    assertFirstSuggestion("Durchnitts", "Durchschnitts", rule, lt);
    assertFirstSuggestion("überdurschnittlichem", "überdurchschnittlichem", rule, lt);
    assertFirstSuggestion("Unterdurschnittlicher", "Unterdurchschnittlicher", rule, lt);
    assertFirstSuggestion("höchstwahrliche", "höchstwahrscheinliche", rule, lt);
    assertFirstSuggestion("vidasehen", "wiedersehen", rule, lt);
    assertFirstSuggestion("striktliches", "striktes", rule, lt);
    assertFirstSuggestion("preventiert", "verhindert", rule, lt);
    assertFirstSuggestion("zurverfügung", "zur Verfügung", rule, lt);
    assertFirstSuggestion("trationelle", "traditionelle", rule, lt);
    assertFirstSuggestion("achsiales", "axiales", rule, lt);
    assertFirstSuggestion("famiele", "Familie", rule, lt);
    assertFirstSuggestion("miters", "Mieters", rule, lt);
    assertFirstSuggestion("besigen", "besiegen", rule, lt);
    assertFirstSuggestion("verziehrte", "verzierte", rule, lt);
    assertFirstSuggestion("pieken", "piken", rule, lt); // Duden insists on this spelling
    assertFirstSuggestion("Erstsemesterin", "Erstsemester", rule, lt);
    assertFirstSuggestion("zauberlicher", "zauberischer", rule, lt);
    assertFirstSuggestion("assessoars", "Accessoires", rule, lt);
    assertFirstSuggestion("könntes", "könntest", rule, lt);
    assertFirstSuggestion("Casemangement", "Case Management", rule, lt);
    assertFirstSuggestion("Anolierung", "Annullierung", rule, lt);
    assertFirstSuggestion("Liaisonen", "Liaisons", rule, lt);
    assertFirstSuggestion("kinderlichem", "kindlichem", rule, lt);
    assertFirstSuggestion("wiedersprichst", "widersprichst", rule, lt);
    assertFirstSuggestion("unproffesionele", "unprofessionelle", rule, lt);
    assertFirstSuggestion("gefrustuckt", "gefrühstückt", rule, lt);
    assertFirstSuggestion("Durführung", "Durchführung", rule, lt);
    assertFirstSuggestion("verheielte", "verheilte", rule, lt);
    assertFirstSuggestion("ausgewönlich", "außergewöhnlich", rule, lt);
    assertFirstSuggestion("unausweichbaren", "unausweichlichen", rule, lt);
    assertFirstSuggestion("Dampfschiffahrtskapitän", "Dampfschifffahrtskapitän", rule, lt);
    assertFirstSuggestion("Helfes-Helfern", "Helfershelfern", rule, lt);
    assertFirstSuggestion("Intelligentsbestie", "Intelligenzbestie", rule, lt);
    assertFirstSuggestion("avantgardische", "avantgardistische", rule, lt);
    assertFirstSuggestion("gewohnheitsbedürftigen", "gewöhnungsbedürftigen", rule, lt);
    assertFirstSuggestion("patroliert", "patrouilliert", rule, lt);
    assertFirstSuggestion("beidiges", "beides", rule, lt);
    assertFirstSuggestion("Propagandierte", "Propagierte", rule, lt);
    assertFirstSuggestion("revolutioniesiert", "revolutioniert", rule, lt);
    assertFirstSuggestion("Copyride", "Copyright", rule, lt);
    assertFirstSuggestion("angesehende", "angesehene", rule, lt);
    assertFirstSuggestion("angesehendsten", "angesehensten", rule, lt);
    assertFirstSuggestion("frühstücksbüfé", "Frühstücksbuffet", rule, lt);
    assertFirstSuggestion("deutsprachiger", "deutschsprachiger", rule, lt);
    assertFirstSuggestion("gehäckelten", "gehäkelten", rule, lt);
    assertFirstSuggestion("Alterego", "Alter Ego", rule, lt);
    assertFirstSuggestion("Makeupstylistin", "Make-up-Stylistin", rule, lt);
    assertFirstSuggestion("islamophobische", "islamophobe", rule, lt);
    assertFirstSuggestion("Fedbäck", "Feedback", rule, lt);
    assertFirstSuggestion("desöfterem", "des Öfteren", rule, lt);
    assertFirstSuggestion("momentmal", "Moment mal", rule, lt);
    assertFirstSuggestion("eingängliche", "eingängige", rule, lt);
    assertFirstSuggestion("kusengs", "Cousins", rule, lt);
    assertFirstSuggestion("Influenzer", "Influencer", rule, lt);
    assertFirstSuggestion("kaperzität", "Kapazität", rule, lt);
    assertFirstSuggestion("ausversehendlich", "aus Versehen", rule, lt);
    assertFirstSuggestion("tränern", "Trainern", rule, lt);
    assertFirstSuggestion("Teiming", "Timing", rule, lt);
    assertFirstSuggestion("inzinierung", "Inszenierung", rule, lt);
    assertFirstSuggestion("weireten", "weiteren", rule, lt);
    assertFirstSuggestion("Nivoschalters", "Niveauschalters", rule, lt);
    assertFirstSuggestion("exhibitionischer", "exhibitionistischer", rule, lt);
    assertFirstSuggestion("geschalten", "geschaltet", rule, lt);
    assertFirstSuggestion("unterschiebenes", "unterschriebenes", rule, lt);
    assertFirstSuggestion("Umbekwehmer", "Unbequemer", rule, lt);
    assertFirstSuggestion("Unbequemliche", "Unbequeme", rule, lt);
    assertFirstSuggestion("unbequemlichstes", "unbequemstes", rule, lt);
    assertFirstSuggestion("Desatören", "Deserteuren", rule, lt);
    assertFirstSuggestion("Panelen", "Paneelen", rule, lt);
    assertFirstSuggestion("Deja-Vue", "Déjà-vu", rule, lt);
    assertFirstSuggestion("Dejavou", "Déjà-vu", rule, lt);
    assertFirstSuggestion("Cremefraiche", "Crème fraîche", rule, lt);
    assertFirstSuggestion("aragemont", "Arrangement", rule, lt);
    assertFirstSuggestion("Diseing", "Design", rule, lt);
    assertFirstSuggestion("Lieradresse", "Lieferadresse", rule, lt);
    assertFirstSuggestion("Boykutierung", "Boykottierung", rule, lt);
    assertFirstSuggestion("rethorisch", "rhetorisch", rule, lt);
    assertFirstSuggestion("anschliessliche", "anschließende", rule, lt);
    assertFirstSuggestion("Überstreitung", "Überschreitung", rule, lt);
    assertFirstSuggestion("werkzeug.", "Werkzeug", rule, lt);
    assertFirstSuggestion("Wärkzeug.", "Werkzeug", rule, lt);
    assertFirstSuggestion("Fußgängerunterweg", "Fußgängerunterführung", rule, lt);
    assertFirstSuggestion("Ingineuer", "Ingenieur", rule, lt);
    assertFirstSuggestion("Panacotta", "Panna cotta", rule, lt);
    assertFirstSuggestion("Ärcker", "Erker", rule, lt);
    assertFirstSuggestion("genrealistischer", "generalistischer", rule, lt);
    assertFirstSuggestion("schweinerosane", "schweinchenrosa", rule, lt);
    assertFirstSuggestion("anstecklichen", "ansteckenden", rule, lt);
    assertFirstSuggestion("geflechtetes", "geflochtenes", rule, lt);
    assertFirstSuggestion("ärtlichem", "ärztlichem", rule, lt);
    assertFirstSuggestion("großzüges", "großzügiges", rule, lt);
    assertFirstSuggestion("Einbahnfrei", "Einwandfrei", rule, lt);
    assertFirstSuggestion("einbandfreier", "einwandfreier", rule, lt);
    assertFirstSuggestion("Beanstandigung", "Beanstandung", rule, lt);
    assertFirstSuggestion("Beanstandigungen", "Beanstandungen", rule, lt);
    assertFirstSuggestion("zweiundhalb", "zweieinhalb", rule, lt);
    assertFirstSuggestion("dreiundhalb", "dreieinhalb", rule, lt);
    assertFirstSuggestion("Zuguterletzt", "Zu guter Letzt", rule, lt);
    assertFirstSuggestion("guterletzt", "guter Letzt", rule, lt);
    assertFirstSuggestion("unfährer", "unfairer", rule, lt);
    assertFirstSuggestion("unfäre", "unfaire", rule, lt);
    assertFirstSuggestion("medikatöses", "medikamentöses", rule, lt);
    assertFirstSuggestion("versendliches", "versehentliches", rule, lt);
    assertFirstSuggestion("Nootbooks", "Notebooks", rule, lt);
    assertFirstSuggestion("Eigtl", "Eigtl.", rule, lt);
    assertFirstSuggestion("pflanzige", "pflanzliche", rule, lt);
    assertFirstSuggestion("geblogt", "gebloggt", rule, lt);
    assertFirstSuggestion("ähliche", "ähnliche", rule, lt);
    assertFirstSuggestion("entfängt", "empfängt", rule, lt);
    assertFirstSuggestion("verewiglichte", "verewigte", rule, lt);
    assertFirstSuggestion("zeritifierte", "zertifizierte", rule, lt);
    assertFirstSuggestion("gerähte", "Geräte", rule, lt);
    assertFirstSuggestion("pirsing", "Piercing", rule, lt);
    assertFirstSuggestion("behilfreiches", "behilfliches", rule, lt);
    assertFirstSuggestion("einsichtbar", "einsehbar", rule, lt);
    assertFirstSuggestion("vollrichtest", "verrichtest", rule, lt);
    assertFirstSuggestion("Vollrichtet", "Verrichtet", rule, lt);
    assertFirstSuggestion("bedingslosem", "bedingungslosem", rule, lt);
    assertFirstSuggestion("überstenden", "berstenden", rule, lt);
    assertFirstSuggestion("abbonierst", "abonnierst", rule, lt);
    assertFirstSuggestion("Apeliere", "Appelliere", rule, lt);
    assertFirstSuggestion("voltieschiert", "voltigiert", rule, lt);
    assertFirstSuggestion("meistverkaufteste", "meistverkaufte", rule, lt);
    assertFirstSuggestion("unleshaftem", "unleserlichem", rule, lt);
    assertFirstSuggestion("glaubenswürdig", "glaubwürdig", rule, lt);
    assertFirstSuggestion("nivovolle", "niveauvolle", rule, lt);
    assertFirstSuggestion("niwovoller", "niveauvoller", rule, lt);
    assertFirstSuggestion("notgezwungender", "notgedrungener", rule, lt);
    assertFirstSuggestion("misstraurig", "misstrauisch", rule, lt);
    assertFirstSuggestion("Aux-Anschluss", "AUX-Anschluss", rule, lt);
    assertFirstSuggestion("Wi", "Wie", rule, lt);
    assertFirstSuggestion("Verspäterung", "Verspätung", rule, lt);
    assertFirstSuggestion("groesste", "größte", rule, lt);
    assertFirstSuggestion("tefonische", "telefonische", rule, lt);
    assertFirstSuggestion("optimalisiert", "optimiert", rule, lt);
    assertFirstSuggestion("introvertisches", "introvertiertes", rule, lt);
    assertFirstSuggestion("Entercotte", "Entrecôte", rule, lt);
    assertFirstSuggestion("hirachie", "Hierarchie", rule, lt);
    assertFirstSuggestion("amierte", "armierte", rule, lt);
    assertFirstSuggestion("Versiehrten", "Versierten", rule, lt);
    assertFirstSuggestion("durchsichtbar", "durchsichtig", rule, lt);
    assertFirstSuggestion("offensichtiges", "offensichtliches", rule, lt);
    assertFirstSuggestion("zurverfühgung", "zur Verfügung", rule, lt);
    assertFirstSuggestion("Verständlichkeitsfragen", "Verständnisfragen", rule, lt);
    assertFirstSuggestion("Bewusstliches", "Bewusstes", rule, lt);
    assertFirstSuggestion("leidensvolle", "leidvolle", rule, lt);
    assertFirstSuggestion("augensichtlich", "augenscheinlich", rule, lt);
    assertFirstSuggestion("Krankenbrüdern", "Krankenpflegern", rule, lt);
    assertFirstSuggestion("Lan-Kabel", "LAN-Kabel", rule, lt);
    assertFirstSuggestion("perfekteste", "perfekte", rule, lt);
    assertFirstSuggestion("gleichtig", "gleichzeitig", rule, lt);
    assertFirstSuggestion("vorsichgeht", "vor sich geht", rule, lt);
    assertFirstSuggestion("Simkarte", "SIM-Karte", rule, lt);
    assertFirstSuggestion("Pineingabe", "PIN-Eingabe", rule, lt);
    assertFirstSuggestion("carnivorischen", "karnivoren", rule, lt);
    assertFirstSuggestion("Zaubererinnen", "Zauberinnen", rule, lt);
    assertFirstSuggestion("erroriere", "eruiere", rule, lt);
    assertFirstSuggestion("erroriert", "eruiert", rule, lt);
    assertFirstSuggestion("Second-Hand-Shops", "Secondhandshops", rule, lt);
    assertFirstSuggestion("mediterranischer", "mediterraner", rule, lt);
    assertFirstSuggestion("unterschreibungsfähige", "unterschriftsfähige", rule, lt);
    assertFirstSuggestion("interplementiert", "implementiert", rule, lt);
    assertFirstSuggestion("hochalterlich", "hochmittelalterlich", rule, lt);
    assertFirstSuggestion("posiniert", "positioniert", rule, lt);
    assertFirstSuggestion("russophobische", "russophobe", rule, lt);
    assertFirstSuggestion("unsachmässiger", "unsachgemäßer", rule, lt);
    assertFirstSuggestion("modernisches", "modernes", rule, lt);
    assertFirstSuggestion("intapretation", "Interpretation", rule, lt);
    assertFirstSuggestion("Rethorikkurses", "Rhetorikkurses", rule, lt);
    assertFirstSuggestion("Deprisonen", "Depressionen", rule, lt);
    
    assertFirstSuggestion("battalione", "Bataillone", rule, lt);
    assertFirstSuggestion("Besuchungsverbot", "Besuchsverbot", rule, lt);
    assertFirstSuggestion("widersprüchiges", "widersprüchliches", rule, lt);
    assertFirstSuggestion("camelionhafte", "chamäleonhafte", rule, lt);
    assertFirstSuggestion("angehangenen", "angehängten", rule, lt);
    assertFirstSuggestion("spätrige", "spätere", rule, lt);
    assertFirstSuggestion("faustigen", "faustdicken", rule, lt);
    assertFirstSuggestion("Belastungsekgs", "Belastungs-EKGs", rule, lt);
    assertFirstSuggestion("-Teex", "Tee", rule, lt);
    assertFirstSuggestion("- Teex", "Tee", rule, lt);
    assertFirstSuggestion("- Kaffeex", "Kaffee", rule, lt);
    assertFirstSuggestion("E -Commerce", "E-Commerce", rule, lt);
    assertFirstSuggestion("Intranzparentheit", "Intransparenz", rule, lt);
    assertFirstSuggestion("aufkeinenfal", "auf keinen Fall", rule, lt);
    assertFirstSuggestion("unverantwortunglosen", "verantwortungslosen", rule, lt);
    assertFirstSuggestion("unverantwortungslose", "verantwortungslose", rule, lt);
    assertFirstSuggestion("neuliche", "neuerliche", rule, lt);
    assertFirstSuggestion("kompensioniert", "kompensiert", rule, lt);
    assertFirstSuggestion("desinfektionierender", "desinfizierender", rule, lt);
    assertFirstSuggestion("Desinfektioniert", "Desinfiziert", rule, lt);
    assertFirstSuggestion("desinfektionierst", "desinfizierst", rule, lt);
    assertFirstSuggestion("Neuhichkeit", "Neuigkeit", rule, lt);
    assertFirstSuggestion("neuhichkeiten", "Neuigkeiten", rule, lt);
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
    assertCorrect("Coronapatienten", rule, lt);
    assertCorrect("Coronapatienten.", rule, lt);
  }

  private void assertCorrect(String word, MyGermanSpellerRule rule, JLanguageTool lt) throws IOException {
    assertThat(rule.match(lt.getAnalyzedSentence(word)).length, is(0));
  }

  private void assertFirstSuggestion(String input, String expected, GermanSpellerRule rule, JLanguageTool lt) throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(input));
    if (expected == null) {
      assertThat("Matches: " + matches[0].getSuggestedReplacements(), matches[0].getSuggestedReplacements().size(), is(0));
    } else {
      assertThat("Matches: " + matches.length + ", Suggestions of first match: " +
        matches[0].getSuggestedReplacements(), matches[0].getSuggestedReplacements().get(0), is(expected));
    }
  }

  private void assertNotSuggestion(String input, String notExpected, GermanSpellerRule rule, JLanguageTool lt) throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(input));
    for (RuleMatch match : matches) {
      assertFalse("Found unexpected suggestion '" + notExpected + "' for input '" + input + "'", match.getSuggestedReplacements().contains(notExpected));
    }
  }

  @Test
  public void testDashAndHyphenEtc() throws Exception {
    HunspellRule rule = new GermanSpellerRule(TestTools.getMessages("de"), GERMAN_DE);
    JLanguageTool lt = new JLanguageTool(GERMAN_DE);
    TestTools.disableAllRulesExcept(lt, GermanSpellerRule.RULE_ID);

    assertEquals(2, lt.check("\uFEFF-Product Development Coordinator").size());
    assertEquals(2, lt.check("-Product Development Coordinator").size());
    assertEquals(1, lt.check("\uFEFF-Einx Test").size());
    assertEquals(0, lt.check("\uFEFF-Ein Test").size());
    assertEquals(0, lt.check("Gratis E\uFEFF-Book").size());
    
    // "-" as bullet point with no space:
    assertEquals(0, lt.check("-Tee\n\n-Kaffee").size());
    List<RuleMatch> matches1 = lt.check("-Teex\n\n-Kaffee");
    assertEquals(1, matches1.size());
    assertEquals(1, matches1.get(0).getFromPos());
    assertEquals(5, matches1.get(0).getToPos());
    List<RuleMatch> matches2 = lt.check("- Teex\n\n-Kaffee");
    assertEquals(1, matches2.size());
    assertEquals(2, matches2.get(0).getFromPos());
    assertEquals(6, matches2.get(0).getToPos());
    
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Ist doch - gut")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Ist doch -- gut")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Stil- und Grammatikprüfung gut")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Oliven- und Mandelöl")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Stil-, Text- und Grammatikprüfung gut")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Er liebt die Stil-, Text- und Grammatikprüfung.")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Stil-, Text- und Grammatikprüfung")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Stil-, Text- oder Grammatikprüfung")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Hierzu zählen Einkommen-, Körperschaft- sowie Gewerbesteuer.")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Miet- und Zinseinkünfte")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("SPD- und CDU-Abgeordnete")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Haupt- und Nebensatz")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Vertuschungs- und Bespitzelungsmaßnahmen")).length); // remove "s" from "Vertuschungs" before spell check
//    assertEquals(0, rule.match(lt.getAnalyzedSentence("Au-pair-Agentur")).length); // compound with ignored word from spelling.txt
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Netflix-Film")).length); // compound with ignored word from spelling.txt
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Bund-Länder-Kommission")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Des World Wide Webs")).length); // expanded multi-word entry from spelling.txt
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Der westperuanische Ferienort.")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("„Pumpe“-Nachfolge")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("\"Pumpe\"-Nachfolge")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("ÖVP- und FPÖ-Chefverhandler")).length); // first part is from spelling.txt
    assertEquals(0, rule.match(lt.getAnalyzedSentence("α-Strahlung")).length); // compound with ignored word from spelling.txt
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Primär-α-Mischkristallen")).length); // compound with ignored word from spelling.txt
    assertEquals(0, rule.match(lt.getAnalyzedSentence("supergut")).length); // elativ meaning "sehr gut"
    assertEquals(0, rule.match(lt.getAnalyzedSentence("90°-Winkel")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Kosten- und Kreditmanagement")).length);

    assertEquals(1, rule.match(lt.getAnalyzedSentence("Miet und Zinseinkünfte")).length);
    assertEquals(1, rule.match(lt.getAnalyzedSentence("Stil- und Grammatik gut")).length);
    assertEquals(1, rule.match(lt.getAnalyzedSentence("Flasch- und Grammatikprüfung gut")).length);
    //assertEquals(1, rule.match(langTool.getAnalyzedSentence("Haupt- und Neben")).length);  // hunspell accepts this :-(

    // check acceptance of words in ignore.txt ending with "-*"
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Dual-Use-Güter")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Dual-Use- und Wirtschaftsgüter")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Test-Dual-Use")).length);
    assertEquals(1, rule.match(lt.getAnalyzedSentence("Dual-Use")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Einpseudowortmitßfürlanguagetooltests-Auto")).length);

    // from spelling.txt, also accepted as uppercase variant:
    assertEquals(0, rule.match(lt.getAnalyzedSentence("-ablenkungsfrei")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("-ablenkungsfreies")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("-Ablenkungsfrei")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("-Ablenkungsfreies")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("- Ablenkungsfrei")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("- Ablenkungsfreies")).length);
    assertEquals(1, rule.match(lt.getAnalyzedSentence("abmantelung")).length);  // only as uppercase in spelling.txt
    assertEquals(1, rule.match(lt.getAnalyzedSentence("- abmantelung")).length);
    assertEquals(1, rule.match(lt.getAnalyzedSentence("machtS")).length);
    assertEquals(1, rule.match(lt.getAnalyzedSentence("All-Inclusive-Preis")).length);
    assertEquals(1, rule.match(lt.getAnalyzedSentence("dRanging")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Das Draufklicken")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Des Draufklickens")).length);
    assertEquals(1, rule.match(lt.getAnalyzedSentence("Des draufklickens")).length);

    // originally from spelling.txt:
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Wichtelmännchen")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Wichtelmännchens")).length);
    assertEquals(1, rule.match(lt.getAnalyzedSentence("wichtelmännchen")).length);  // no reason to accept it as lowercase
    assertEquals(1, rule.match(lt.getAnalyzedSentence("wichtelmännchens")).length);  // no reason to accept it as lowercase

    assertEquals(0, rule.match(lt.getAnalyzedSentence("vorgehängt")).length);  // from spelling.txt
    assertEquals(0, rule.match(lt.getAnalyzedSentence("vorgehängten")).length);  // from spelling.txt with suffix
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Vorgehängt")).length);  // from spelling.txt, it's lowercase there but we accept uppercase at idx = 0
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Vorgehängten")).length);  // from spelling.txt with suffix, it's lowercase there but we accept uppercase at idx = 0
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Wichtelmännchen-vorgehängt")).length);  // from spelling.txt formed hyphenated compound
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Wichtelmännchen-Au-pair")).length);  // from spelling.txt formed hyphenated compound

    assertEquals(0, rule.match(lt.getAnalyzedSentence("Fermi-Dirac-Statistik")).length);  // from spelling.txt formed hyphenated compound
//    assertEquals(0, rule.match(lt.getAnalyzedSentence("Au-pair-Wichtelmännchen")).length);  // from spelling.txt formed hyphenated compound
//    assertEquals(0, rule.match(lt.getAnalyzedSentence("Secondhandware")).length);  // from spelling.txt formed compound
//    assertEquals(0, rule.match(lt.getAnalyzedSentence("Feynmandiagramme")).length);  // from spelling.txt formed compound
//    assertEquals(0, rule.match(lt.getAnalyzedSentence("Helizitätsoperator")).length);  // from spelling.txt formed compound
//    assertEquals(0, rule.match(lt.getAnalyzedSentence("Wodkaherstellung")).length);  // from spelling.txt formed compound
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Latte-macchiato-Glas")).length);  // formelery from spelling.txt formed compound
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Werkverträgler-Glas")).length);  // from spelling.txt formed compound
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Werkverträglerglas")).length);  // from spelling.txt formed compound
    assertEquals(1, rule.match(lt.getAnalyzedSentence("Werkverträglerdu")).length);  // from spelling.txt formed "compound" with last part too short
//    assertEquals(0, rule.match(lt.getAnalyzedSentence("No-Name-Hersteller")).length);  // from spelling.txt formed compound
    assertEquals(1, rule.match(lt.getAnalyzedSentence("Helizitätso")).length);  // from spelling.txt formed compound (second part is too short)
    assertEquals(1, rule.match(lt.getAnalyzedSentence("Feynmand")).length);  // from spelling.txt formed compound (second part is too short)

    assertEquals(1, rule.match(lt.getAnalyzedSentence("Einpseudowortmitssfürlanguagetooltests-Auto")).length);
    HunspellRule ruleCH = new GermanSpellerRule(TestTools.getMessages("de"), GERMAN_CH);
    assertEquals(1, ruleCH.match(lt.getAnalyzedSentence("Einpseudowortmitßfürlanguagetooltests-Auto")).length);
    assertEquals(0, ruleCH.match(lt.getAnalyzedSentence("Einpseudowortmitssfürlanguagetooltests-Auto")).length);

    // bullet points in Google Docs:
    assertEquals(0, rule.match(lt.getAnalyzedSentence("\uFEFFAblenkungsfreie Schreibumgebung")).length);
    
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Die blablaxx.mp3 und das sdifguds.avi bzw. die XYZXYZ.AVI")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Ausgestrahlt von 3sat")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Ein 32stel eines Loses")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Der Verlust eines 32stels eines Loses")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Ein 5tel eines Loses")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Der Verlust eines 5tels eines Loses")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("ein 100stel-Millimeter")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("ein 5tel-Gramm")).length);
    assertEquals(1, rule.match(lt.getAnalyzedSentence("ein 100stel-Milimeter")).length);
    assertEquals(1, rule.match(lt.getAnalyzedSentence("ein 5tel-Grömm")).length);
    assertEquals(1, rule.match(lt.getAnalyzedSentence("ein 5tl-Gramm")).length);
    assertEquals(1, rule.match(lt.getAnalyzedSentence("ein 100stl-Millimeter")).length);
    assertEquals(1, rule.match(lt.getAnalyzedSentence("ein tl-Gramm")).length);
    assertEquals(1, rule.match(lt.getAnalyzedSentence("ein stl-Millimeter")).length);
    assertEquals(1, rule.match(lt.getAnalyzedSentence("ein tel-Gramm")).length);
    assertEquals(1, rule.match(lt.getAnalyzedSentence("ein stel-Millimeter")).length);
    assertEquals(1, rule.match(lt.getAnalyzedSentence("Bitte stel dich dazu")).length);
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
               is("[konfliktbereite, konfliktbereiten, konfliktbereitem, konfliktbereiter, konfliktbereites, konfliktbereit]"));
  }

  @Test
  public void testIgnoreWord() throws Exception {
    MyGermanSpellerRule ruleGermany = new MyGermanSpellerRule(TestTools.getMessages("de"), GERMAN_DE);
    assertTrue(ruleGermany.doIgnoreWord("einPseudoWortFürLanguageToolTests"));  // from ignore.txt
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
      super(messages, language, null, null);
      ensureInitialized();
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
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Die Mozart'sche Sonate.")).length);
  }

  // note: copied from HunspellRuleTest
  @Test
  public void testRuleWithAustrianGerman() throws Exception {
    AustrianGerman language = (AustrianGerman) Languages.getLanguageForShortCode("de-AT");
    HunspellRule rule = new AustrianGermanSpellerRule(TestTools.getMessages("de"), language);
    JLanguageTool lt = new JLanguageTool(language);
    commonGermanAsserts(rule, lt);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Der äußere Übeltäter.")).length);  // umlauts
    assertEquals(1, rule.match(lt.getAnalyzedSentence("Der äussere Übeltäter.")).length);
  }

  // note: copied from HunspellRuleTest
  @Test
  public void testRuleWithSwissGerman() throws Exception {
    SwissGerman language = (SwissGerman) Languages.getLanguageForShortCode("de-CH");
    HunspellRule rule = new SwissGermanSpellerRule(TestTools.getMessages("de"), language);
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
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Die Standarte")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Die Standarten")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Die Standartenführer")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Der Standard")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Der Standardversuch")).length);

    assertEquals(1, rule.match(lt.getAnalyzedSentence("Der Waschmaschinentest-Dftgedgs")).length);
    assertEquals(1, rule.match(lt.getAnalyzedSentence("Der Dftgedgs-Waschmaschinentest")).length);
    assertEquals(1, rule.match(lt.getAnalyzedSentence("Der Waschmaschinentestdftgedgs")).length);
    assertEquals(1, rule.match(lt.getAnalyzedSentence("Der Waschmaschinentestversuch orkt")).length);
    assertEquals(1, rule.match(lt.getAnalyzedSentence("Der Arbeitsnehmer")).length);  // wrong interfix
    assertEquals(1, rule.match(lt.getAnalyzedSentence("Die Verhaltenänderung")).length);  // missing interfix
    assertEquals(1, rule.match(lt.getAnalyzedSentence("Er bw. sie.")).length); // abbreviations (bzw.)
    assertEquals(2, rule.match(lt.getAnalyzedSentence("Der asdegfue orkt")).length);
    assertEquals(1, rule.match(lt.getAnalyzedSentence("rumfangreichen")).length);
    assertEquals(1, rule.match(lt.getAnalyzedSentence("Der Standart")).length);
    assertEquals(1, rule.match(lt.getAnalyzedSentence("Der Standartversuch")).length);
    assertEquals(1, rule.match(lt.getAnalyzedSentence("Halterun")).length);
    assertEquals(1, rule.match(lt.getAnalyzedSentence("Wandhalterun")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Halterung")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Wandhalterung")).length);
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
    assertCorrection(rule, "inneremedizin", "innere Medizin");
    assertCorrection(rule, "innereMedizin", "innere Medizin");
    //assertCorrection(rule, "Inneremedizin", "Innere Medizin");
    assertCorrection(rule, "InnereMedizin", "Innere Medizin");
    assertCorrection(rule, "gleichgroß", "gleich groß");

    //TODO: requires morfologik-speller change (suggestions for known words):
    assertCorrection(rule, "Arbeitamt", "Arbeitsamt");

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
    assertCorrection(rule, "Ambei", "Anbei");
    assertCorrection(rule, "Kostn-", "Kosten-");
    assertCorrection(rule, "Adam-Ries-Nachfahrenbuch");

    // TODO: compounds with errors in more than one part
    // totally wrong jwordsplitter split: Hands + elvertretertreffn:
    //assertCorrection(rule, "Handselvertretertreffn", "Handelsvertretertreffen");
  }

  @Test
  public void testSuggestions() throws Exception {
    GermanSpellerRule rule = new GermanSpellerRule(TestTools.getMessages("de"), GERMAN_DE);
    JLanguageTool lt = new JLanguageTool(GERMAN_DE);
    assertFirstSuggestion("informationnen.", "Informationen", rule, lt);
    assertFirstSuggestion("Kundigungsfrist.", "Kündigungsfrist", rule, lt);
    assertFirstSuggestion("aufgeregegt.", "aufgeregt", rule, lt);
    assertFirstSuggestion("informationnen...", "Informationen..", rule, lt);  // not 100% perfect, but we can live with this...
    assertFirstSuggestion("arkbeiten-", "arbeiten", rule, lt);
    //assertFirstSuggestion("arkjbeiten-", "arbeiten", rule, lt);
    // commas are actually not part of the word, so the suggestion doesn't include them:
    assertFirstSuggestion("informationnen,", "Informationen", rule, lt);
    assertFirstSuggestion("ALT-TARIF,", null, rule, lt);
    assertNotSuggestion("Pseudo-Rebellentum", "Pseudo- Rebellentum", rule, lt);
    assertNotSuggestion("Pseudo-Rebellentum", "Pseudo--Rebellentum", rule, lt);
    assertNotSuggestion("Virtualisierungs-Layer", "Virtualisierungs--Layer", rule, lt);
    assertNotSuggestion("Mediations-Background", "Mediation s-Background", rule, lt);
    assertFirstSuggestion("ALT-ÜBERSICHT,", null, rule, lt);
    assertFirstSuggestion("Sakralkultur,", null, rule, lt);
    assertFirstSuggestion("Auschwitzmythxs,", null, rule, lt);  // correction prevented by lcDoNotSuggestWords
    assertFirstSuggestion("Wursteinalgen", "Wursteinlagen", rule, lt);  // "algen" was accepted in de_DE.dic as compound part, we removed it
    assertFirstSuggestion("Wursteinalge", "Wursteinlage", rule, lt);    // "algen" was accepted in de_DE.dic as compound part, we removed it
  }
  
  @Test
  public void testGetSuggestionOrder() throws Exception {
    HunspellRule rule = new GermanSpellerRule(TestTools.getMessages("de"), GERMAN_DE);
    assertCorrectionsByOrder(rule, "heisst", "heißt");  // "heißt" should be first
    assertCorrectionsByOrder(rule, "heissen", "heißen");
    assertCorrectionsByOrder(rule, "müßte", "musste", "müsste");
    assertCorrectionsByOrder(rule, "schmohren", "schmoren");
    assertCorrectionsByOrder(rule, "Fänomen", "Phänomen");
    assertCorrectionsByOrder(rule, "homofob", "homophob");
    assertCorrectionsByOrder(rule, "ueber", "über");
    //assertCorrectionsByOrder(rule, "uebel", "übel");
    assertCorrectionsByOrder(rule, "Aerger", "Ärger");
    assertCorrectionsByOrder(rule, "Walt", "Wald");
    assertCorrectionsByOrder(rule, "Rythmus", "Rhythmus");
    assertCorrectionsByOrder(rule, "Rytmus", "Rhythmus");
    assertCorrectionsByOrder(rule, "is", "IS", "die", "in", "im", "ist");  // 'ist' should actually be preferred...
    assertCorrectionsByOrder(rule, "Fux", "Fuchs");  // fixed in morfologik 2.1.4
    assertCorrectionsByOrder(rule, "schänken", "Schänken");  // "schenken" is missing
  }
  
  @Test
  public void testIsMisspelled() {
    HunspellRule rule = new GermanSpellerRule(TestTools.getMessages("de"), GERMAN_DE);
    assertTrue(rule.isMisspelled("dshfsdhsdf"));
    assertTrue(rule.isMisspelled("Haussarbeit"));
    assertTrue(rule.isMisspelled("Überschus"));
    assertTrue(rule.isMisspelled("Überschussen"));

    assertFalse(rule.isMisspelled("Hausarbeit"));
    assertFalse(rule.isMisspelled("Überschuss"));
    assertFalse(rule.isMisspelled("Überschüsse"));

    assertTrue(rule.isMisspelled("Spielzugcomputer"));
    assertTrue(rule.isMisspelled("Spielzugcomputern"));
    assertFalse(rule.isMisspelled("Spielzug"));
    assertFalse(rule.isMisspelled("Spielzugs"));

    assertTrue(rule.isMisspelled("Studentenschafte"));
    assertTrue(rule.isMisspelled("Steuereigenschafte"));
    assertFalse(rule.isMisspelled("Studentenschaften"));
    assertFalse(rule.isMisspelled("Steuereigenschaften"));
    assertFalse(rule.isMisspelled("Eigenschaften"));
    assertFalse(rule.isMisspelled("wirtschafte"));
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
      dictWordsAsBytes.add(entry.getBytes(StandardCharsets.UTF_8));
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
    InputStream is = new ByteArrayInputStream(infoFile.getBytes(StandardCharsets.UTF_8));
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
      assertTrue("Not found: '" + expectedTerm + "' in: " + suggestions + " for input '" + input + "'", suggestions.contains(expectedTerm));
    }
    if (expectedTerms.length == 0 && suggestions.size() > 0) {
      fail("Didn't expect suggestions at all for '" + input + "', got: " + suggestions);
    }
  }
  
  private void assertCorrectionsByOrder(HunspellRule rule, String input, String... expectedTerms) throws IOException {
    List<String> suggestions = rule.getSuggestions(input);
    int i = 0;
    for (String expectedTerm : expectedTerms) {
      assertTrue("Not found at position " + i + ": '" + expectedTerm + "' in: " + suggestions + " for input '" + input + "'", suggestions.get(i).equals(expectedTerm));
      i++;
    }
  }

  @Test
  public void testErrorLimitReached() throws IOException {
    HunspellRule rule1 = new GermanSpellerRule(TestTools.getMessages("de"), GERMAN_DE);
    JLanguageTool lt = new JLanguageTool(GERMAN_DE);
    RuleMatch[] matches1 = rule1.match(lt.getAnalyzedSentence("Ein schöner Satz."));
    assertThat(matches1.length, is(0));
    RuleMatch[] matches2 = rule1.match(lt.getAnalyzedSentence("But this is English."));
    assertThat(matches2.length, is(4));
    assertNull(matches2[0].getErrorLimitLang());
    assertNull(matches2[1].getErrorLimitLang());
    assertThat(matches2[2].getErrorLimitLang(), is("zz"));  // 'en' is not known in this module, thus 'zz'
    RuleMatch[] matches3 = rule1.match(lt.getAnalyzedSentence("Und er sagte, this is a good test."));
    assertThat(matches3.length, is(4));
    assertNull(matches3[3].getErrorLimitLang());
  }

  /**
   * number of suggestions seems to depend on previously checked text.
   * fixed by not reusing morfologik Speller object
  */
  @Test
  public void testMorfologikSuggestionsWorkaround() throws IOException {
    HunspellRule rule1 = new GermanSpellerRule(TestTools.getMessages("de"), GERMAN_DE);
    HunspellRule rule2 = new GermanSpellerRule(TestTools.getMessages("de"), GERMAN_DE);
    JLanguageTool lt = new JLanguageTool(GERMAN_DE);

    String sentence1 = "Das Absinken der Motordrehzahl bfi größeren Geschwindigkeiten war.";
    String sentence2 = "Welche die Eidgenossenschaft ls Staatenbund wiederhergestellt hat.";

    RuleMatch[] matches11 = rule1.match(lt.getAnalyzedSentence(sentence1));
    RuleMatch[] matches12 = rule1.match(lt.getAnalyzedSentence(sentence2));

    RuleMatch[] matches22 = rule2.match(lt.getAnalyzedSentence(sentence2));
    RuleMatch[] matches21 = rule2.match(lt.getAnalyzedSentence(sentence1));

    assertTrue(Stream.of(matches11, matches12, matches21, matches22).allMatch(arr -> arr.length == 1));

    assertEquals(matches11[0].getSuggestedReplacements().size(), matches21[0].getSuggestedReplacements().size());
    assertEquals(matches12[0].getSuggestedReplacements().size(), matches22[0].getSuggestedReplacements().size());

    // a bug caused "bie" to be ignored:
    RuleMatch[] matches20 = rule1.match(lt.getAnalyzedSentence("laut Beispielen bie"));
    assertThat(matches20.length, is(1));
  }
}
