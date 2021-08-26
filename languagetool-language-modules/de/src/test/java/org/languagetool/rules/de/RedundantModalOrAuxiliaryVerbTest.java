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
package org.languagetool.rules.de;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.TestTools;
import org.languagetool.rules.Rule;

public class RedundantModalOrAuxiliaryVerbTest {
  private Language lang = Languages.getLanguageForShortCode("de-DE");

  @Test
  public void testRule() throws IOException {
    JLanguageTool lt = new JLanguageTool(lang);
    setUpRule(lt);
    //  matches
    assertEquals(1, lt.check("Erst werde ich die Preise vergleichen und erst dann werde ich entscheiden, ob ich die Kamera kaufe.").size());
    assertEquals(1, lt.check("Sie hat das Foto von mir als kleinem Jungen angeschaut und hat gelacht.").size());
    assertEquals(1, lt.check("Ich bin gern in eurer Mitte und ich bin gern zu Gast bei euch.").size());
    assertEquals(1, lt.check("Da ich nun einmal bin, wer ich bin und was ich bin, kann ich es nicht übers Herz bringen.").size());
    assertEquals(1, lt.check("Wann ist jemand kühn und wann ist jemand tollkühn?").size());
    assertEquals(1, lt.check("Tom ist um halb drei angekommen und Mary ist kurze Zeit später angekommen.").size());
    assertEquals(1, lt.check("Das Essen ist gut und der Service hier ist gut.").size());
    assertEquals(1, lt.check("Wir müssen wissen, was wir tun sollen und wie wir es tun sollen.").size());
    assertEquals(1, lt.check("Ich muss unbedingt wissen, was zu tun ist, wie es zu tun ist und wann es zu tun ist.").size());
    assertEquals(1, lt.check("Er erzählte ihr, eine böse Hexe habe ihn in einen Frosch verwandelt und nur sie allein habe ihn retten können und morgen würden sie gemeinsam in sein Reich fahren.").size());
    //  no match
    assertEquals(0, lt.check("„nicht dürfen“ und „nicht müssen“ dürfen nicht miteinander verwechselt werden.").size());
    assertEquals(0, lt.check("Ich mag Physik und Mathematik mag ich noch mehr.").size());
    assertEquals(0, lt.check("Aus Angst vor den Zeitungen sind Politiker langweilig und am Ende sind sie selbst für die Zeitungen zu langweilig.").size());
    assertEquals(0, lt.check("Ändert der Akkusativ wirklich die Bedeutung eines Satzes: Sie hat Aids oder Aids hat sie?").size());
    assertEquals(0, lt.check("Tom hat eine Tüte Äpfel gekauft und er hat an einem Tag ein Drittel davon gegessen.").size());
    assertEquals(0, lt.check("Mein Vaterland ist mein Stadtviertel und nun ist es beinahe nicht wiedererkennbar.").size());
    assertEquals(0, lt.check("Johnny hat Alice vorgeschlagen und sie hat akzeptiert.").size());
    assertEquals(0, lt.check("Unsere Zeit hier ist begrenzt und sie ist wertvoll.").size());
    assertEquals(0, lt.check("Manchmal ist die Wahrheit nützlich und manchmal ist sie unnütz.").size());
    assertEquals(0, lt.check("Treue ist irgendwo absolut oder sie ist gar nichts.").size());
    assertEquals(0, lt.check("Sie mag niemanden und niemand mag sie.").size());
    assertEquals(0, lt.check("Eines Tages wird die USA eine Frau als Präsidenten wählen und das wird kein schönes Schauspiel sein.").size());
    assertEquals(0, lt.check("Mein Name ist Mary und das ist Tom.").size());
    assertEquals(0, lt.check("Er hat seine Augen langsam geöffnet und dann hat sie ihn geküsst.").size());
    assertEquals(0, lt.check("Die Flaneure haben es prinzipiell nicht eilig und sind immer für ein Schwätzchen zu haben.").size());
    assertEquals(0, lt.check("Es sind Tendenzen erkennbar und diese Tendenzen sind leider nicht ermutigend.").size());
    assertEquals(0, lt.check("Ja ist ja und nein ist nein, das nennt man Offenheit.").size());
    assertEquals(0, lt.check("Wie werden einmal unsere Namen hinter den Erfindern des Fliegens und dergleichen vergessen werden?").size());
    assertEquals(0, lt.check("In den Chroniken ist über die Flut von 1342 zu lesen, dass im Mainzer Dom einem Mann das Wasser bis zur Brust gestanden habe und dass man in Köln mit Booten über die Stadtmauern habe fahren können.").size());
    assertEquals(0, lt.check("Sie stellt sicher, dass die gerissenen Bänder nicht belastet werden können und dass das Gelenk dennoch bewegt werden kann.").size());
  }

  private void setUpRule(JLanguageTool lt) {
    for (Rule rule : lt.getAllRules()) {
      lt.disableRule(rule.getId());
    }
    RedundantModalOrAuxiliaryVerb rule = new RedundantModalOrAuxiliaryVerb(TestTools.getMessages(lang.getShortCode()));
    lt.addRule(rule);
    lt.enableRule(rule.getId());
  }


}
