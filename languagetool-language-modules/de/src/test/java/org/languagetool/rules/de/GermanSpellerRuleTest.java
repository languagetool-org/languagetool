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
import morfologik.fsa.builders.FSABuilder;
import morfologik.fsa.builders.CFSA2Serializer;
import morfologik.speller.Speller;
import morfologik.stemming.Dictionary;
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class GermanSpellerRuleTest {

  private static final GermanyGerman GERMAN_DE = new GermanyGerman();
  private static final SwissGerman GERMAN_CH = new SwissGerman();

  @Test
  public void testSortSuggestion() throws Exception {
    GermanSpellerRule rule = new GermanSpellerRule(TestTools.getMessages("de"), GERMAN_DE);
    assertThat(rule.sortSuggestionByQuality("fehler", Arrays.asList("Fehler", "fehl er", "fehle r")).toString(),
            is("[Fehler, fehl er]"));
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
    RuleMatch[] matches1 = rule.match(lt.getAnalyzedSentence("konservierungsstoffe"));
    assertThat(matches1[0].getSuggestedReplacements().toString(), is("[Konservierungsstoffe]"));
    RuleMatch[] matches2 = rule.match(lt.getAnalyzedSentence("konservierungsstoffstatistik"));
    assertThat(matches2[0].getSuggestedReplacements().toString(), is("[Konservierungsstoffstatistik]"));
    RuleMatch[] matches3 = rule.match(lt.getAnalyzedSentence("konservierungsstoffsasdsasda"));
    assertThat(matches3[0].getSuggestedReplacements().size(), is(0));
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
  }

  @Test
  public void testAddIgnoreWords() throws Exception {
    MyGermanSpellerRule rule = new MyGermanSpellerRule(TestTools.getMessages("de"), GERMAN_DE);
    Set<String> set = new HashSet<>();
    rule.addIgnoreWords("Fußelmappse", set);
    assertTrue(set.contains("Fußelmappse"));
    rule.addIgnoreWords("Fußelmappse/N", set);
    assertTrue(set.contains("Fußelmappse"));
    assertTrue(set.contains("Fußelmappsen"));
    rule.addIgnoreWords("Toggeltröt/NS", set);
    assertTrue(set.contains("Toggeltröt"));
    assertTrue(set.contains("Toggeltröts"));
    assertTrue(set.contains("Toggeltrötn"));
    rule.addIgnoreWords("Toggeltröt/NS", set);
    MyGermanSpellerRule ruleCH = new MyGermanSpellerRule(TestTools.getMessages("de"), GERMAN_CH);
    ruleCH.addIgnoreWords("Fußelmappse/N", set);
    assertTrue(set.contains("Fusselmappse"));
    assertTrue(set.contains("Fusselmappsen"));
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
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Haupt- und Nebensatz")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Au-pair-Agentur")).length); // compound with ignored word from spelling.txt
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Netflix-Film")).length); // compound with ignored word from spelling.txt
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Bund-Länder-Kommission")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Der westperuanische Ferienort.")).length);

    assertEquals(1, rule.match(lt.getAnalyzedSentence("Miet und Zinseinkünfte")).length);
    assertEquals(1, rule.match(lt.getAnalyzedSentence("Stil- und Grammatik gut")).length);
    assertEquals(1, rule.match(lt.getAnalyzedSentence("Flasch- und Grammatikprüfung gut")).length);
    //assertEquals(1, rule.match(langTool.getAnalyzedSentence("Haupt- und Neben")).length);  // hunspell accepts this :-(
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
    assertTrue(ruleGermany.doIgnoreWord("vorgehängt"));                 // from spelling.txt
    assertTrue(ruleGermany.doIgnoreWord("vorgehängten"));               // from spelling.txt with suffix
    assertTrue(ruleGermany.doIgnoreWord("Wichtelmännchen-vorgehängt")); // from spelling.txt formed hyphenated compound
    assertTrue(ruleGermany.doIgnoreWord("Wichtelmännchen-Au-pair"));    // from spelling.txt formed hyphenated compound
    assertTrue(ruleGermany.doIgnoreWord("Fermi-Dirac-Statistik"));      // from spelling.txt formed hyphenated compound
    assertTrue(ruleGermany.doIgnoreWord("Au-pair-Wichtelmännchen"));    // from spelling.txt formed hyphenated compound
    assertTrue(ruleGermany.doIgnoreWord("Secondhandware"));             // from spelling.txt formed compound
    assertTrue(ruleGermany.doIgnoreWord("Feynmandiagramme"));           // from spelling.txt formed compound
    assertTrue(ruleGermany.doIgnoreWord("Helizitätsoperator"));         // from spelling.txt formed compound
    assertFalse(ruleGermany.doIgnoreWord("Helizitätso"));               // from spelling.txt formed compound (second part is too short)
    assertFalse(ruleGermany.doIgnoreWord("Feynmand"));                  // from spelling.txt formed compound (second part is too short)
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
  }
  
  @Test
  public void testGetSuggestions() throws Exception {
    HunspellRule rule = new GermanSpellerRule(TestTools.getMessages("de"), GERMAN_DE);

    assertCorrection(rule, "Hauk", "Haus", "Haut");
    assertCorrection(rule, "Eisnbahn", "Einbahn", "Eisbahn", "Eisenbahn"); 
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
    
    // use to work with jwordsplitter 3.4: too many other suggestions with Levenshtein=2
    //assertCorrection(rule, "Handselvertreter", "Handelsvertreter");
    //assertCorrection(rule, "Handselvertretertreffen", "Handelsvertretertreffen");
    
    assertCorrection(rule, "aul", "auf");
    assertCorrection(rule, "Icj", "Ich");   // only "ich" (lowercase) is in the lexicon
    //assertCorrection(rule, "Ihj", "Ich");   // only "ich" (lowercase) is in the lexicon - does not work because of the limit

    // three part compounds:
    assertCorrection(rule, "Handelsvertretertrffen", "Handelsvertretertreffen");
    assertCorrection(rule, "Handelsvartretertreffen", "Handelsvertretertreffen");
    assertCorrection(rule, "Handelsvertretertriffen", "Handelsvertretertreffen");
      
    // this won't work as jwordsplitter splits into Handelsvertrter + Treffen but
    // the Hunspell dict doesn't contain "Handelsvertreter", thus it's a known limitation
    // because jwordsplitter doesn't use the same dictionary as Hunspell:
    // assertCorrection(rule, "Handelsvertrtertreffen", "Handelsvertretertreffen");

    // TODO: compounds with errors in more than one part
    // totally wrong jwordsplitter split: Hands + elvertretertreffn:
    //assertCorrection(rule, "Handselvertretertreffn", "Handelsvertretertreffen");
  }

  @Test
  public void testGetSuggestionOrder() throws Exception {
    HunspellRule rule = new GermanSpellerRule(TestTools.getMessages("de"), GERMAN_DE);
    assertCorrectionsByOrder(rule, "heisst", "heißt");  // "heißt" should be first
    assertCorrectionsByOrder(rule, "heissen", "heißen");
    assertCorrectionsByOrder(rule, "müßte", "müsste");
    assertCorrectionsByOrder(rule, "schmohren", "schmoren");
    assertCorrectionsByOrder(rule, "Fänomen", "Phänomen");
    assertCorrectionsByOrder(rule, "homofob", "homophob");
    assertCorrectionsByOrder(rule, "ueber", "über");
    assertCorrectionsByOrder(rule, "uebel", "übel");
    assertCorrectionsByOrder(rule, "Aerger", "Ärger");
    assertCorrectionsByOrder(rule, "Walt", "Wald");
    assertCorrectionsByOrder(rule, "Rythmus", "Rhythmus");
    assertCorrectionsByOrder(rule, "Rytmus", "Rhythmus");
    assertCorrectionsByOrder(rule, "is", "iss", "in", "im", "ist");  // 'ist' should actually be preferred...
  }
  
  @Test
  @Ignore("testing a potential bug in Morfologik")
  public void testMorfologikSpeller() throws Exception {
    List<byte[]> lines = new ArrayList<>();
    lines.add("die".getBytes());
    lines.add("ist".getBytes());
    byte[] info = "fsa.dict.separator=+\nfsa.dict.encoding=utf-8\nfsa.dict.frequency-included=true".getBytes();
    Dictionary dict = getDictionary(lines, new ByteArrayInputStream(info));
    Speller speller = new Speller(dict, 2);
    System.out.println(speller.findReplacements("is"));  // why do both "die" and "ist" have a distance of 1 in the CandidateData constructor?
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
