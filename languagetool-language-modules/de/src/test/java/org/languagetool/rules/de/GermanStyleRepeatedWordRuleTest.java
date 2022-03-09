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
import org.languagetool.*;
import org.languagetool.rules.Rule;

/**
 * @author Fred Kruse
 */
public class GermanStyleRepeatedWordRuleTest {
  
  private Language lang = Languages.getLanguageForShortCode("de-DE");

  @Test
  public void testRule() throws IOException {
    JLanguageTool lt = new JLanguageTool(lang);
    setUpRule(lt);

    assertEquals(2, lt.check("Der alte Mann wohnte in einem großen Haus. Es stand in einem großen Garten.").size());
    assertEquals(0, lt.check("Der alte Mann wohnte in einem großen Haus. Es stand in einem weitläufigen Garten.").size());
    assertEquals(0, lt.check("Endlos lang zog sich der Ton dahin, aber schließlich verklang er doch.").size());
    assertEquals(0, lt.check("Die Au leuchtete im Blitz, die Glühwürmchen flimmerten vor Aufregung.").size());
    assertEquals(0, lt.check("Sie sind alle in der gleichen Art hergestellt. Nur bei einem Artefakt wurde eine andere Vorgehensweise angewandt.").size());
    assertEquals(0, lt.check("Ich erkannte keinen Lidschlag zu spät, dass es nicht in ihrer Absicht lag.").size());
    assertEquals(0, lt.check("Dieser Schritt brachte uns näher an die Lösung. Der nächste würde das Problem für immer aus der Welt schaffen.").size());
    assertEquals(0, lt.check("Er lehnte seinen Wanderstab gegen die Wand.").size());
    assertEquals(0, lt.check("Zweifel lagen in der Luft, ob es richtig war, diesen Weg einzuschlagen.").size());
    assertEquals(0, lt.check("Der Donnerhall verklang nur langsam in meinen Ohren. Das war mir in all den Jahren meines Lebens noch nicht passiert.").size());
    assertEquals(3, lt.check("Der Schiffsmotor, der im Heck des Schiffs eingebaut war, röhrte. Auf Hochtouren lief der Motor.").size());
    assertEquals(2, lt.check("Der Buntspecht stolzierte den Baum hoch. Schon klopfte der Specht.").size());
    assertEquals(2, lt.check("Rotbraun war die Farbe der Haselnuss. Der Horizont schimmerte rot.").size());
  }

  private void setUpRule(JLanguageTool lt) {
    for (Rule rule : lt.getAllRules()) {
      lt.disableRule(rule.getId());
    }
    GermanStyleRepeatedWordRule rule = new GermanStyleRepeatedWordRule(TestTools.getMessages(lang.getShortCode()),
        lang, new UserConfig());
    lt.addRule(rule);
    lt.enableRule(rule.getId());
  }

}
