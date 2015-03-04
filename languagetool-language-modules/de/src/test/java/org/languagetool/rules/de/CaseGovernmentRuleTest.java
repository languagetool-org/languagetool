/* LanguageTool, a natural language style checker
 * Copyright (C) 2015 Daniel Naber (http://www.danielnaber.de)
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

import org.junit.Ignore;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.German;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.*;

import static junit.framework.TestCase.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.languagetool.rules.de.CaseGovernmentRule.*;

public class CaseGovernmentRuleTest {

  private final JLanguageTool lt = new JLanguageTool(new German());
  private final CaseGovernmentRule rule;

  public CaseGovernmentRuleTest() {
    rule = new CaseGovernmentRule(TestTools.getEnglishMessages());
  }

  @Test
  public void testCheckCasesTEMP() throws IOException {
    //GermanChunker.setDebug(true);
    //rule.setDebug(true);
    //assertGood("Gib mir ein Taschentuch.");
    //assertGood("Gibt es hier in der Nähe eine Jugendherberge?");  // "in der Nähe eine Jugendherberge" -> PP, which is wrong
    //assertGood("Gib mir Zeit, dir alles zu geben, was ich habe!");
    //assertGood("Ich gebe dir mein Wort.");
    //assertGood("Wenn es keine Lösung gibt, dann gibt es kein Probleme.");
    //assertGood("Eine grobe Untersuchung seiner Zähne gab zu erkennen, das alles gut war.");
  }

  @Test
  public void testCheckCases() throws IOException {
    assertOkay("NOM", "NOM");
    assertOkay("NOM", "NOM?");

    assertOkay("AKK,NOM AKK", "NOM AKK");
    assertOkay("AKK,NOM AKK", "NOM AKK?");
    assertOkay("AKK,NOM AKK", "AKK NOM");
    assertOkay("AKK,NOM AKK", "AKK NOM?");

    assertOkay("AKK,NOM AKK,NOM", "NOM AKK");
    assertOkay("AKK,NOM AKK,NOM", "AKK NOM");
    assertOkay("AKK,NOM AKK,NOM", "AKK NOM GEN?");
    assertOkay("AKK,NOM AKK,NOM", "AKK NOM GEN? GEN?");

    assertOkay("AKK AKK,NOM", "NOM AKK");
    assertOkay("AKK AKK,NOM", "AKK NOM");
    assertOkay("AKK AKK,NOM", "AKK NOM AKK?");
    assertOkay("AKK AKK,NOM", "AKK NOM GEN?");
    assertOkay("AKK AKK,NOM", "AKK NOM GEN? AKK?");

    assertOkay("AKK,NOM AKK", "AKK AKK");
    assertOkay("AKK,NOM AKK", "NOM AKK");
    assertOkay("AKK,NOM AKK", "AKK NOM");
    assertOkay("AKK,NOM AKK GEN", "NOM AKK GEN");
    assertOkay("AKK,NOM AKK GEN", "AKK NOM GEN");
    assertOkay("AKK,NOM AKK GEN", "GEN AKK NOM");
    assertOkay("AKK,NOM AKK GEN", "GEN AKK NOM GEN?");

    assertMissing("NOM", "");
    assertMissing("NOM", "AKK");
    assertMissing("AKK,NOM AKK", "GEN");
    assertMissing("AKK,NOM AKK", "NOM GEN");
    assertMissing("AKK,NOM AKK", "NOM AKK GEN");
    assertMissing("AKK,NOM AKK", "NOM AKK GEN");
  }

  private void assertOkay(String sentencesCases, String expectedCases) {
    assertTrue(rule.checkCases(makeAnalyzedChunk(sentencesCases), makeList(expectedCases)));
  }

  private void assertMissing(String sentencesCases, String expectedCases) {
    assertFalse(rule.checkCases(makeAnalyzedChunk(sentencesCases), makeList(expectedCases)));
  }

  private List<AnalyzedChunk> makeAnalyzedChunk(String s) {
    List<AnalyzedChunk> result = new ArrayList<>();
    String[] parts = s.split(" ");
    for (String part : parts) {
      String[] subParts = part.split(",");
      List<Case> list = new ArrayList<>();
      for (String subPart : subParts) {
        list.add(Case.valueOf(subPart));
      }
      AnalyzedChunk analyzedChunk = new AnalyzedChunk(new Chunk("fake chunk", false), new HashSet<>(list));
      result.add(analyzedChunk);
    }
    return result;
  }

  private List<ValencyData> makeList(String s) {
    if (s.isEmpty()) {
      return Collections.emptyList();
    } else {
      String[] split = s.split(" ");
      List<ValencyData> result = new ArrayList<>();
      for (String part : split) {
        if (part.endsWith("?")) {
          result.add(new ValencyData(Case.valueOf(part.substring(0, part.length()-1)), false));
        } else {
          result.add(new ValencyData(Case.valueOf(part), true));
        }
      }
      return result;
    }
  }

  @Test
  @Ignore("interactive use only")
  public void testGetChunks() throws IOException {
    show("Das ist ein Haus.");
    show("Das ist ein großes Haus.");
    show("Das ist ein schönes großes Haus.");
    show("Das Haus.");
    show("Das 1999 erbaute Haus.");
    show("Das im Jahr 1999 erbaute Haus.");
    show("Das von der Regierung erbaute Haus.");
    show("Das von der Regierung im Jahr 1999 erbaute Haus.");
    show("Der Hund und die von der Regierung geprüfte Katze sind schön.");
    show("Ich muss dem Hund Futter geben.");
  }

  private void show(String text) throws IOException {
    System.out.println(text + " -> " + rule.getChunks(lt.getAnalyzedSentence(text)));
  }

  @Test
  public void testRule() throws IOException {
    assertGood("Die Frau gibt ihr Geld.");
    assertGood("Die Frau gibt ihr Geld dem Freund ihres Mannes.");
    assertGood("Die Frau gibt ihr Geld einem Obdachlosen.");

    // Passiv - TODO: diese Sätze auch checken
    assertNullResult("Das Geld der Frau wird dem Obdachlosen gegeben.");
    assertNullResult("Das Geld wird dem Obdachlosen von der Frau gegeben.");

    // andere Satzstellung:
    assertGood("Ihr Geld gibt die Frau.");
    assertGood("Ihr Geld gibt die Frau dem Mann.");
    assertGood("Ihr Geld gibt die Frau dem Freund ihres Mannes.");
    assertGood("Dem Mann gibt die Frau ihr Geld.");
    assertGood("Dem Freund ihres Mannes gibt die Frau ihr Geld.");

    assertBad("Die Frau gibt ihr Geld einen Obdachlosen.");
    assertBad("Die Frau gibt ihr Geld eines Obdachlosen.");
    assertBad("Die Frau gibt ihr Geld ein Obdachlosen.");
    assertBad("Die Frau gibt ihres Geldes.");
    assertBad("Der Frau gibt ihres Geldes.");

    assertGood("Und die Frau gibt ihrem Bruder den Hut.");
    assertGood("Und die Frau gibt ihrem Bruder einen Hut.");
    assertGood("Und die Frau gibt ihrem Bruder einen alten Hut.");

    assertBad("Die Frau gibt ihren Bruder den Hut.");
    assertBad("Und die Frau gibt ihren Bruder den Hut.");
    assertBad("Und die Frau gibt ihrem Bruder einem alten Hut.");

    assertBad("Den Hut gibt die Frau ihren Bruder.");
    assertGood("Dem Hut gibt die Frau ihren Bruder.");  // Semantik Quark, Grammatik okay!
    assertBad("Ihren Bruder gibt die Frau den Hut.");

    assertGood("Ein Test sollte Fehler geben.");
    assertGood("Ein Test gibt Fehler.");
    assertGood("Ein Test, der Fehler geben sollte.");

    assertGood("Es sollte mindestens einen Satz geben.");
    assertGood("In einem Wörterbuch wie diesem sollte es mindestens einen Satz geben.");
    assertGood("Es gibt da ein Problem, das du nicht siehst.");
    assertGood("Es kann auch grüne Autos geben.");
    assertGood("Es kann durchaus auch grüne Autos geben.");
    assertGood("Es gibt immer Dinge, die ich nie lernen werde");
    assertGood("„Halbwahrheiten“ gibt es nicht.");
    assertGood("Es gibt Standpunkte");
    assertGood("Es gibt unterschiedliche Standpunkte");
    assertGood("Zu dieser Frage gibt es Standpunkte");
    assertGood("Zu dieser Frage gibt es unterschiedliche Standpunkte");
    assertGood("Es gibt Dinge zu tun");
    assertGood("Es gibt viele Dinge zu tun");
    assertGood("Es gibt zu viele Dinge zu tun");
    assertGood("Es gibt zu wichtige Dinge zu tun");

    assertGood("Dann gibt man Natriumdihydrogenphosphat zu Wasser.");   // Natriumdihydrogenphosphat is unknown
    assertGood("Dann gibt man Natriumdihydrogenphosphat (NaH2PO4) zu Wasser.");
  }

  @Test
  public void testGetAnalyzedChunks() throws IOException {
    List<AnalyzedChunk> chunks1 = rule.getAnalyzedChunks(Arrays.asList(
            new Chunk("das Haus", false),
            new Chunk("den Teller", false)
    ));
    assertTrue(chunks1.get(0).cases.contains(Case.NOM));
    assertTrue(chunks1.get(0).cases.contains(Case.AKK));
    assertTrue(chunks1.get(1).cases.contains(Case.AKK));
    assertThat(chunks1.size(), is(2));

    List<AnalyzedChunk> chunks2 = rule.getAnalyzedChunks(Arrays.asList(
            new Chunk("die Frau", false),
            new Chunk("die Wasser", true)  // "die Frau, die Wasser trinkt" -> "die Wasser" ist keine NP
    ));
    assertTrue(chunks2.get(0).cases.contains(Case.NOM));  // "die Frau"
    assertTrue(chunks2.get(0).cases.contains(Case.AKK));
    assertTrue(chunks2.get(1).cases.contains(Case.NOM));  // nur "Wasser" wir beachtet wegen dem Komma, also völlig ambig
    assertTrue(chunks2.get(1).cases.contains(Case.AKK));
    assertTrue(chunks2.get(1).cases.contains(Case.GEN));
    assertTrue(chunks2.get(1).cases.contains(Case.DAT));
    assertThat(chunks2.size(), is(2));
  }

  private void assertGood(String sentence) throws IOException {
    RuleMatch[] result = rule.match(lt.getAnalyzedSentence(sentence));
    assertThat("Got " + Arrays.toString(result), result.length, is(0));
  }

  private void assertNullResult(String sentence) throws IOException {
    CaseGovernmentRule.CheckResult result = rule.checkGovernment(lt.getAnalyzedSentence(sentence));
    assertNull(result);
  }

  private void assertBad(String sentence) throws IOException {
    RuleMatch[] result = rule.match(lt.getAnalyzedSentence(sentence));
    assertThat(result.length, is(1));
  }

}