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

import java.io.IOException;

import junit.framework.TestCase;

import org.languagetool.JLanguageTool;
import org.languagetool.Language;

/**
 * @author Markus Brenneis
 */
public class GermanWrongWordInContextRuleTest extends TestCase {

  public void testRule() throws IOException {
    GermanWrongWordInContextRule rule = new GermanWrongWordInContextRule(null);
    JLanguageTool langTool = new JLanguageTool(Language.GERMAN);
    // Lid/Lied
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Ihre Lider sind entzündet.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Er hat entzündete Lider.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Wir singen gemeinsam Lieder.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Lieder singen wir.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Lider singen wir."))[0].getFromPos());
    assertEquals(11, rule.match(langTool.getAnalyzedSentence("Ihre Lieder sind entzündet."))[0].getToPos());
    assertEquals("Lider", rule.match(langTool.getAnalyzedSentence("Er hat entzündete Lieder."))[0].getSuggestedReplacements().get(0));
    assertEquals("Lieder", rule.match(langTool.getAnalyzedSentence("Wir singen gemeinsam Lider."))[0].getSuggestedReplacements().get(0));

    // malen/mahlen
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Ich soll Bilder einer Mühle malen.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Ich male ein Bild einer Mühle.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Das Bild zeigt eine mahlende Mühle.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Eine mahlende Mühle zeigt das Bild.")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Weizen ausmalen.")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Ich mahle das Bild aus.")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Eine Mühle wird zum Malen verwendet.")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Das gemalene Korn aus der Mühle ist gut.")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Zum Malen verwendet man eine Mühle.")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Du musst das Bild ausmahlen.")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Wir haben das im Kunstunterricht gemahlt.")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Er hat ein schönes Selbstporträt gemahlt.")).length);
    assertEquals("gemahlen", rule.match(langTool.getAnalyzedSentence("Das Korn wird in den Mühlen gemalen."))[0].getSuggestedReplacements().get(0));
    assertEquals("malten", rule.match(langTool.getAnalyzedSentence("Wir mahlten im Kunstunterricht."))[0].getSuggestedReplacements().get(0));

    // Mine/Miene
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Er verzieht keine Miene.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Die Explosion der Mine.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Die Mine ist explodiert.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Er versucht, keine Miene zu verziehen.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Sie sollen weiter Minen eingesetzt haben.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Er verzieht sich nach Bekanntgabe der Mineralölsteuerverordnung.")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Er verzieht keine Mine.")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Mit unbewegter Mine.")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Er setzt eine kalte Mine auf.")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Er sagt, die unterirdische Miene sei zusammengestürzt.")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Die Miene ist eingestürzt.")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Die Sprengung mit Mienen ist toll.")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Der Bleistift hat eine Miene.")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Die Mienen sind gestern Abend explodiert.")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Die Miene des Kugelschreibers ist leer.")).length);
    assertEquals("Minen", rule.match(langTool.getAnalyzedSentence("Er hat das mit den Mienen weggesprengt."))[0].getSuggestedReplacements().get(0));
    assertEquals("Miene", rule.match(langTool.getAnalyzedSentence("Er versucht, keine Mine zu verziehen."))[0].getSuggestedReplacements().get(0));

    // Saite/Seite
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Die Seiten des Buches sind beschrieben.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Dieses Buch über die Gitarre hat nur sechs Seiten.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Diese Gitarre hat sechs Saiten.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Die UNO muss andere Saiten aufziehen.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Eine Gitarre hat Saiten, aber keine Seiten.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Die Saiten des Violoncellos sind kurz.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Dieses Buch über die Gitarre hat nur sechs Seiten.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Eine Seite und eine scharfe Suppe.")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Die Saiten des Buches sind beschrieben.")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Die Seiten des Klaviers werden angeschlagen.")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Die Seiten der Kurzhalsgeige sind gerissen.")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Die Seiten des Kontrabasses sind gerissen.")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Bei der UNO müssen andere Seiten aufgezogen werden.")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Die Seiten des Violoncellos sind kurz.")).length);
    assertEquals("Saite", rule.match(langTool.getAnalyzedSentence("Die E-Gitarre hat eine sechste Seite."))[0].getSuggestedReplacements().get(0));
    assertEquals("Seiten", rule.match(langTool.getAnalyzedSentence("Dieses Buch hat sechs Saiten."))[0].getSuggestedReplacements().get(0));
  }
  
}
