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

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.language.German;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class CaseGovernmentRuleTest {

  private final JLanguageTool lt = new JLanguageTool(new German());
  private final CaseGovernmentRule rule;

  public CaseGovernmentRuleTest() {
    rule = new CaseGovernmentRule();
  }

  @Test
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
  public void run() throws IOException {
    //String text = "Der schwarze Sattel. Der Sattel des Fahrrads. Lässt sich das arrangieren? Dem Hund Wasser geben.";
    //String text = "Der Hund, der Eier legt.";
    //String text = "Meine Tante interessiert das wenig.";
    //String text = "Die Frau gibt ihrem Bruder den Hut ihres Mannes.";

    //String text = "Der Sitz des Fahrrads passt gut.";
    //String text = "Der Sitz dem Fahrrad passt gut.";
    //String text = "Dort stehen die Autos, die Geld verschlingen";
    //String text = "Dort steht mein Fahrrad.";

    /*CaseGovernmentRule.CheckResult result = rule.run("Die Frau gibt ihrem Bruder den Hut.");
    System.out.println("chunks=" + result);
    assertThat(result.getMissingSlots().size(), is(0));
    assertThat(result.getUnexpectedSlots().size(), is(0));*/

    //CaseGovernmentRule.CheckResult result2 = rule.run("Und die Frau gibt.");
    //CaseGovernmentRule.CheckResult result2 = rule.run("Und die Frau gibt ihrem Bruder den Hut.");
    //CaseGovernmentRule.CheckResult result2 = rule.run("Die Frau gibt ihren Bruder den Hut.");
    //CaseGovernmentRule.CheckResult result2 = rule.run("Und die Frau gibt ihren Bruder den Hut.");
    //System.out.println("chunks=" + result2);
    //assertThat(result.getMissingSlots().size(), is(0));
    //assertThat(result.getUnexpectedSlots().size(), is(0));

    //CaseGovernmentRule.CheckResult result2 = rule.run("Die Frau gibt ihrem Bruder, der lange verschwunden war, den Hut.");
    //assertResult("Die Frau gibt ihren Bruder, der lange Haare hat, den Hut.", "[]", "[AKK, AKK]");
    //assertResult("Die Frau gibt ihren Bruder den Hut.", "[]", "[AKK, AKK]");
    //assertResult("Die Frau gibt ihrem Bruder den Hut.", "[]", "[]");
    //assertResult("Die Frau gibt ihr Geld.", "[]", "[]");
    assertResult("Die Frau gibt ihr Geld einem Obdachlosen.", "[]", "[]");

    // über erwartete Valenzen iterieren und prüfen, ob erfüllt (egal wo)


    // TODO: wir brauchen Valenzdaten für Verben z.B. "geben: Subjekt (im Nominativ), Dativ-Objekt, Akkusativ-Objekt, das Genitiv-Attribut"
    // dann prüfen, ob die geforderten Kasus auch vorliegen, sonst Fehler
  }

  private void assertResult(String sentence, String expectedMissing, String expectedUnexpected) throws IOException {
    CaseGovernmentRule.CheckResult result2 = rule.run(lt.getAnalyzedSentence(sentence));
    //System.out.println("chunks=" + result2);
    assertMissing(result2, expectedMissing);
    assertUnexpected(result2, expectedUnexpected);
  }

  private void assertMissing(CaseGovernmentRule.CheckResult result, String expected) {
    assertThat(result.getMissingSlots().toString(), is(expected));
  }

  private void assertUnexpected(CaseGovernmentRule.CheckResult result, String expected) {
    assertThat(result.getUnexpectedSlots().toString(), is(expected));
  }

}