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
import org.languagetool.Languages;
import org.languagetool.TestTools;

public class WiederVsWiderRuleTest {

  private final WiederVsWiderRule rule = new WiederVsWiderRule(TestTools.getMessages("de"));

  @Test
  public void testRule() throws IOException {
    JLanguageTool lt = new JLanguageTool(Languages.getLanguageForShortCode("de-DE"));

    assertGood("Das spiegelt wider, wie es wieder läuft.", lt);
    assertGood("Das spiegelt die Situation gut wider.", lt);
    assertGood("Das spiegelt die Situation.", lt);
    assertGood("Immer wieder spiegelt das die Situation.", lt);
    assertGood("Immer wieder spiegelt das die Situation wider.", lt);
    assertGood("Das spiegelt wieder wider, wie es läuft.", lt);

    assertBad("Das spiegelt wieder, wie es wieder läuft.", lt);
    assertBad("Sie spiegeln das Wachstum der Stadt wieder.", lt);
    assertBad("Das spiegelt die Situation gut wieder.", lt);
    assertBad("Immer wieder spiegelt das die Situation wieder.", lt);
    assertBad("Immer wieder spiegelte das die Situation wieder.", lt);
  }

  private void assertGood(String text, JLanguageTool lt) throws IOException {
    assertEquals(0, rule.match(lt.getAnalyzedSentence(text)).length);
  }

  private void assertBad(String text, JLanguageTool lt) throws IOException {
    assertEquals(1, rule.match(lt.getAnalyzedSentence(text)).length);
  }

}
