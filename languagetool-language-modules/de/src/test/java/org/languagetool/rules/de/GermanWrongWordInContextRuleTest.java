/* LanguageTool, a natural language style checker 
 * Copyright (C) 2012 Markus Brenneis
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

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;

/**
 * @author Markus Brenneis
 */
public class GermanWrongWordInContextRuleTest {

  private JLanguageTool lt;
  private GermanWrongWordInContextRule rule;
  
  @Before
  public void setUp() throws IOException {
    lt = new JLanguageTool(Languages.getLanguageForShortCode("de-DE"));
    rule = new GermanWrongWordInContextRule(null);
  }

  @Test
  public void testRule() throws IOException {
    // Laiche/Leiche
    assertBad("Eine Laiche ist ein toter Körper.");
    assertGood("Eine Leiche ist ein toter Körper.");
    assertGood("Die Leichen der Verstorbenen wurden ins Wasser geworfen.");
    
    // Lid/Lied
    assertGood("Ihre Lider sind entzündet.");
    assertGood("Er hat entzündete Lider.");
    assertGood("Wir singen gemeinsam Lieder.");
    assertGood("Lieder singen wir.");
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Lider singen wir."))[0].getFromPos());
    assertEquals(11, rule.match(lt.getAnalyzedSentence("Ihre Lieder sind entzündet."))[0].getToPos());
    assertEquals("Lider", rule.match(lt.getAnalyzedSentence("Er hat entzündete Lieder."))[0].getSuggestedReplacements().get(0));
    assertEquals("Lieder", rule.match(lt.getAnalyzedSentence("Wir singen gemeinsam Lider."))[0].getSuggestedReplacements().get(0));

    // malen/mahlen
    assertGood("Ich soll Bilder einer Mühle malen.");
    assertGood("Ich male ein Bild einer Mühle.");
    assertGood("Das Bild zeigt eine mahlende Mühle.");
    assertGood("Eine mahlende Mühle zeigt das Bild.");
    assertGood("Wenn du mal etwas Mehl brauchst, kannst du zu mir kommen.");
    assertBad("Weizen ausmalen.");
    assertBad("Ich mahle das Bild aus.");
    assertBad("Eine Mühle wird zum Malen verwendet.");
    assertBad("Das gemalene Korn aus der Mühle ist gut.");
    assertBad("Zum Malen verwendet man eine Mühle.");
    assertBad("Du musst das Bild ausmahlen.");
    assertBad("Wir haben das im Kunstunterricht gemahlt.");
    assertBad("Er hat ein schönes Selbstporträt gemahlt.");
    assertEquals("gemahlen", rule.match(lt.getAnalyzedSentence("Das Korn wird in den Mühlen gemalen."))[0].getSuggestedReplacements().get(0));
    assertEquals("malten", rule.match(lt.getAnalyzedSentence("Wir mahlten im Kunstunterricht."))[0].getSuggestedReplacements().get(0));

    // Mine/Miene
    assertGood("Er verzieht keine Miene.");
    assertGood("Er verzieht keine Miene.");
    assertGood("Die Explosion der Mine.");
    assertGood("Die Mine ist explodiert.");
    assertGood("Er versucht, keine Miene zu verziehen.");
    assertGood("Sie sollen weiter Minen eingesetzt haben.");
    assertGood("Er verzieht sich nach Bekanntgabe der Mineralölsteuerverordnung.");
    assertBad("Er verzieht keine Mine.");
    assertBad("Mit unbewegter Mine.");
    assertBad("Er setzt eine kalte Mine auf.");
    assertBad("Er sagt, die unterirdische Miene sei zusammengestürzt.");
    assertBad("Die Miene ist eingestürzt.");
    assertBad("Die Sprengung mit Mienen ist toll.");
    assertBad("Der Bleistift hat eine Miene.");
    assertBad("Die Mienen sind gestern Abend explodiert.");
    assertBad("Die Miene des Kugelschreibers ist leer.");
    assertEquals("Minen", rule.match(lt.getAnalyzedSentence("Er hat das mit den Mienen weggesprengt."))[0].getSuggestedReplacements().get(0));
    assertEquals("Miene", rule.match(lt.getAnalyzedSentence("Er versucht, keine Mine zu verziehen."))[0].getSuggestedReplacements().get(0));

    // Saite/Seite
    assertGood("Die Seiten des Buches sind beschrieben.");
    assertGood("Dieses Buch über die Gitarre hat nur sechs Seiten.");
    assertGood("Diese Gitarre hat sechs Saiten.");
    assertGood("Die UNO muss andere Saiten aufziehen.");
    assertGood("Eine Gitarre hat Saiten, aber keine Seiten.");
    assertGood("Die Saiten des Violoncellos sind kurz.");
    assertGood("Dieses Buch über die Gitarre hat nur sechs Seiten.");
    assertGood("Eine Seite und eine scharfe Suppe.");
    assertBad("Die Saiten des Buches sind beschrieben.");
    assertBad("Die Seiten des Klaviers werden angeschlagen.");
    assertBad("Die Seiten der Kurzhalsgeige sind gerissen.");
    assertBad("Die Seiten des Kontrabasses sind gerissen.");
    assertBad("Bei der UNO müssen andere Seiten aufgezogen werden.");
    assertBad("Die Seiten des Violoncellos sind kurz.");
    assertEquals("Saite", rule.match(lt.getAnalyzedSentence("Die E-Gitarre hat eine sechste Seite."))[0].getSuggestedReplacements().get(0));
    assertEquals("Seiten", rule.match(lt.getAnalyzedSentence("Dieses Buch hat sechs Saiten."))[0].getSuggestedReplacements().get(0));

    // Neutron/Neuron
    assertGood("Nervenzellen nennt man Neuronen");
    assertGood("Das Neutron ist elektisch neutral");
    assertBad("Atomkerne bestehen aus Protonen und Neuronen");
    assertBad("Über eine Synapse wird das Neutron mit einer bestimmten Zelle verknüpft und nimmt mit der lokal zugeordneten postsynaptischen Membranregion eines Dendriten Signale auf.");
    assertEquals("Neutronen", rule.match(lt.getAnalyzedSentence("Protonen und Neuronen sind Bausteine des Atomkerns"))[0].getSuggestedReplacements().get(0));
    assertEquals("Neurons", rule.match(lt.getAnalyzedSentence("Das Axon des Neutrons ..."))[0].getSuggestedReplacements().get(0));
    
    // Wunde/Winde
    assertGood("Das Seil läuft durch eine Winde.");
    assertGood("Eine blutende Wunde");
    assertBad("Es kamen Keime in die Winde.");
    assertBad("Möglicherweise wehen die Wunde gerade nicht günstig.");

    // betäuben/bestäuben
    assertGood("Er war durch die Narkose betäubt.");
    assertGood("Die Biene bestäubt die Blume.");
    assertBad("Den Kuchen mit Puderzucker betäuben.");
    assertBad("Von Drogen bestäubt spürte er keine Schmerzen.");

    // ver(r)eisen
    assertGood("Er verreist stets mit leichtem Gepäck.");
    assertGood("Die Warze wurde vereist.");
    assertBad("Nach Diktat vereist.");
    assertBad("Die Tragfläche war verreist.");
  }

  private void assertGood(String sentence) throws IOException {
    assertEquals(0, rule.match(lt.getAnalyzedSentence(sentence)).length);
  }

  private void assertBad(String sentence) throws IOException {
    assertEquals(1, rule.match(lt.getAnalyzedSentence(sentence)).length);
  }

}
