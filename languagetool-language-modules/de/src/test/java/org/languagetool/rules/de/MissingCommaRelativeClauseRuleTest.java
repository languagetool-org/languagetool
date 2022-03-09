/* LanguageTool, a natural language style checker
 * Copyright (C) 2013 Daniel Naber (http://danielnaber.de/)
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
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.TestTools;
import org.languagetool.rules.RuleMatch;

public class MissingCommaRelativeClauseRuleTest {

  @Test
  public void testMatch() throws Exception {
    JLanguageTool lt = new JLanguageTool(Languages.getLanguageForShortCode("de-DE"));
    MissingCommaRelativeClauseRule rule = new MissingCommaRelativeClauseRule(TestTools.getMessages("de"));
    
    assertMatch("Das Auto das am Straßenrand steht parkt im Halteverbot.", 4, 12, rule, lt);
    assertMatch("Das Auto das am Straßenrand steht, parkt im Halteverbot.", 4, 12, rule, lt);
    assertMatch("Das Auto in dem der Mann sitzt, parkt im Halteverbot.", 4, 15, rule, lt);
    assertMatch("Das Auto in dem der Mann sitzt parkt im Halteverbot.", 4, 15, rule, lt);
    assertMatch("Die Frau die vor dem Auto steht hat schwarze Haare.", 4, 12, rule, lt);
    assertMatch("Die Frau die vor dem Auto steht, hat schwarze Haare.", 4, 12, rule, lt);
    assertMatch("Alles was ich habe, ist ein Buch.", 0, 9, rule, lt);
    
    assertNoMatch("Computer machen die Leute dumm.", rule, lt);
    assertNoMatch("Die Unstimmigkeit zwischen den Geschichten der zwei Unfallbeteiligten war groß.", rule, lt);
    assertNoMatch("Ebenso darf keine schwerere Strafe als die zum Zeitpunkt der Begehung der strafbaren Handlung angedrohte Strafe verhängt werden.", rule, lt);
    assertNoMatch("Als dritte Gruppe lassen sich Aminosäuren fassen, die der Organismus anstelle dieser in Proteine einbaut.", rule, lt);
    assertNoMatch("Selbst wenn das alles perfekt verlustfrei wäre, hätte ich nichts gewonnen.", rule, lt);
    assertNoMatch("Die Studenten, deren Urteil am stärksten von dem der Profis abwich, waren sich sicher, einen guten von einem schlechten unterscheiden zu können.", rule, lt);
    assertNoMatch("Die Studenten, deren Urteil am stärksten durch das der Profis beeinflusst wurde, waren sich sicher, einen guten von einem schlechten unterscheiden zu können.", rule, lt);

    rule = new MissingCommaRelativeClauseRule(TestTools.getMessages("de"), true);
    
    assertMatch("Das Auto, das am Straßenrand steht parkt im Halteverbot.", 29, 40, rule, lt);
    assertMatch("Das Auto, in dem der Mann sitzt parkt im Halteverbot.", 26, 37, rule, lt);
    assertMatch("Die Frau, die vor dem Auto steht hat schwarze Haare.", 27, 36, rule, lt);
    assertMatch("Alles, was ich habe ist ein Buch.", 15, 23, rule, lt);
    assertMatch("In diesem Prozess sind aber Entwicklungsschritte ja integriert, die wir Psychiater glaube ich auch gut kennen.", 72, 93, rule, lt);
    assertMatch("Alles, was du für die Spieße brauchst sind Tortellini, getrocknete Tomaten, Cherrytomaten und Mozzarellakugeln.", 29, 42, rule, lt);

    assertNoMatch(".... das war eher so das, was alle im Browser deaktiviert hatten, weil man dachte, über Javascript wird irgendwie Schadsoftware eingeschleust.", rule, lt);
    assertNoMatch("Ich habe einige Fehler begangen, die ich vermeiden hätte können sollen.", rule, lt);
    assertNoMatch("Doch die Rolle, die Mohammed bin Salman in Riad spielt, ist zwiespältig.", rule, lt);
    assertNoMatch("Laut Josef Peter Burg gehörte das Haus, aus dem er samt Familie geworfen wurde, Werner und Sigrid Bahlke, während er darin wohnte und darauf aufpasste.", rule, lt);
    assertNoMatch("Darüber hinaus stellt die klassische Metaphysik eine Grundfrage, die sich etwa wie folgt formulieren lässt: Warum ist überhaupt Seiendes und nicht vielmehr Nichts?", rule, lt);
    assertNoMatch("Wenn du alles, was du meinst nicht zu können, von anderen erledigen lässt, wirst du es niemals selbst lernen.", rule, lt);
    assertNoMatch("Er hat einen Zeitraum durchlebt, in dem seine Gedanken verträumt auf den weiten Feldern der Mysterien umherirrten.", rule, lt);
    assertNoMatch("Es ist die Wiederkehr der Panikmache, die der neue Nationalismus mit dem der Sprachreiniger verbindet und die Geschichte der Sprachreinigung zu einem Lehrstück macht.", rule, lt);   
    assertNoMatch("Gesuche können von Institutionen, Organisationen, Vereinen und Gruppierungen gestellt werden, die im Kanton Luzern domiziliert sind.", rule, lt);
    assertNoMatch("Die Klausel kann zudem nur Gleichrang mit Verbindlichkeiten des Schuldners herstellen, die vom Gesetz in der Insolvenz nicht privilegiert sind, so dass die Klausel nichts an der gesetzlich vorgesehenen Rangfolge im Insolvenzverfahren ändert.", rule, lt);
    assertNoMatch("Plan von Maßnahmen, mit denen das Ansteckungsrisiko während des Aufenthalts an einem Ort verringert werden soll", rule, lt);
    assertNoMatch("Aus diesem Grund sind die Wörter nicht direkt übersetzt, stattdessen wird der Zustand oder die Situation beschrieben in der die Wörter benutzt werden.", rule, lt);
    assertNoMatch("Kryptographisch sichere Verfahren sind dann solche, für die es keine bessere Methode zum Brechen der Sicherheit als das Faktorisieren einer großen Zahl gibt, insbesondere kann der private nicht aus dem öffentlichen Schlüssel errechnet werden.", rule, lt);
    assertNoMatch("Wayne Jancik begrenzt seine One-Hit-Wonders in den USA auf die Billboard-Top-40-Pop-Hitparade mit einer „Ruhe-Periode“ von 5 Jahren, innerhalb derer kein weiterer Hit desselben Interpreten in die Top 40 gelangen darf.", rule, lt);
  }
  
  protected void assertNoMatch(String input, MissingCommaRelativeClauseRule rule, JLanguageTool lt) throws IOException {
    assertThat(rule.match(lt.getAnalyzedSentence(input)).length, is(0));
  }

  protected void assertMatch(String input, int from, int to, MissingCommaRelativeClauseRule rule, JLanguageTool lt) throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(input));
    assertThat(matches.length, is(1));
    assertThat(matches[0].getFromPos(), is(from));
    assertThat(matches[0].getToPos(), is(to));
  }

}
